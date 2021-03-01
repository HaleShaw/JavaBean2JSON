package com.jianwudao.javabean2json;

import com.google.common.collect.Lists;
import com.google.gson.GsonBuilder;
import com.intellij.notification.*;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtil;
import com.jianwudao.javabean2json.fake.*;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public abstract class JavaBean2JSONAction extends AnAction {

    private final NotificationGroup notificationGroup =
            NotificationGroupManager.getInstance().getNotificationGroup("javabean2json.NotificationGroup");

    @NonNls
    private final Map<String, FakeService> normalTypes = new HashMap<>(12);

    private final GsonBuilder gsonBuilder = new GsonBuilder().setPrettyPrinting();

    public JavaBean2JSONAction() {
        FakeDecimal fakeDecimal = new FakeDecimal();
        FakeDateTime fakeDateTime = new FakeDateTime();

        normalTypes.put("Boolean", new FakeBoolean());
        normalTypes.put("Float", fakeDecimal);
        normalTypes.put("Double", fakeDecimal);
        normalTypes.put("BigDecimal", fakeDecimal);
        normalTypes.put("Number", new FakeInteger());
        normalTypes.put("Character", new FakeChar());
        normalTypes.put("CharSequence", new FakeString());
        normalTypes.put("Date", fakeDateTime);
        normalTypes.put("Temporal", new FakeTemporal());
        normalTypes.put("LocalDateTime", fakeDateTime);
        normalTypes.put("LocalDate", new FakeDate());
        normalTypes.put("LocalTime", new FakeTime());
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        if (editor == null){
            notifyWarning("Get editor failed!",project);
            return;
        }
        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
        if (psiFile == null){
            notifyWarning("Get file failed!",project);
            return;
        }
        PsiElement elementAt = psiFile.findElementAt(editor.getCaretModel().getOffset());
        PsiClass selectedClass = PsiTreeUtil.getContextOfType(elementAt, PsiClass.class);
        if (selectedClass == null){
            notifyWarning("Get selected class failed!",project);
            return;
        }
        try {
            Map<String, Object> fields = getFields(selectedClass);
            String json = gsonBuilder.create().toJson(fields);
            StringSelection selection = new StringSelection(json);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(selection, selection);
            String message = "Convert " + selectedClass.getName() + " to JSON success. Copied to clipboard.";
            Notification success = notificationGroup.createNotification(message, NotificationType.INFORMATION);
            Notifications.Bus.notify(success, project);
        } catch (OutOfReferenceException ex) {
            notifyWarning(ex.getMessage(),project);
        } catch (Exception ex) {
            Notification error = notificationGroup.createNotification("Convert to JSON failed.", NotificationType.ERROR);
            Notifications.Bus.notify(error, project);
        }
    }

    protected abstract Object getFakeValue(FakeService fakeService);

    /**
     * Show the warning notification.
     * @param message warning message.
     * @param project Project.
     */
    private void notifyWarning(String message, Project project){
        Notification warn = notificationGroup.createNotification(message, NotificationType.WARNING);
        Notifications.Bus.notify(warn, project);
    }

    private Map<String, Object> getFields(PsiClass psiClass) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (psiClass == null) {
            return map;
        }
        for (PsiField field : psiClass.getAllFields()) {
            map.put(field.getName(), typeResolve(field.getType(), 0));
        }
        return map;
    }


    private Object typeResolve(PsiType type, int level) {
        level = ++level;

        // Primitive Type.
        if (type instanceof PsiPrimitiveType) {
            return getPrimitiveTypeValue(type);
        } else if (type instanceof PsiArrayType) {
            // Array Type.
            return getArrayTypeValue(type, level);
        } else {
            // Reference Type.
            return getReferenceTypeValue(type, level);
        }
    }

    /**
     * Get the value of the reference type.
     * @param type PsiType.
     * @param level maximum reference level.
     * @return the value of the reference type.
     */
    private Object getReferenceTypeValue(PsiType type, int level) {
        Map<String, Object> map = new LinkedHashMap<>();
        PsiClass psiClass = PsiUtil.resolveClassInClassTypeOnly(type);
        if (psiClass == null) {
            return map;
        }
        if (psiClass.isEnum()) {
            // Enum Type.
            for (PsiField field : psiClass.getFields()) {
                if (field instanceof PsiEnumConstant) {
                    return field.getName();
                }
            }
            return "";
        } else {
            List<String> fieldTypeNames = new ArrayList<>();
            PsiType[] types = type.getSuperTypes();
            fieldTypeNames.add(type.getPresentableText());
            fieldTypeNames.addAll(Arrays.stream(types).map(PsiType::getPresentableText).collect(Collectors.toList()));

            // Iterable Type.
            if (fieldTypeNames.stream().anyMatch(s -> s.startsWith("Collection") || s.startsWith("Iterable"))) {
                return getIterableTypeValue(type, level);
            } else {
                // Object Type.
                List<String> retain = new ArrayList<>(fieldTypeNames);
                retain.retainAll(normalTypes.keySet());
                if (!retain.isEmpty()) {
                    return this.getFakeValue(normalTypes.get(retain.get(0)));
                } else {
                    if (level > 500) {
                        throw new OutOfReferenceException("The reference of this class exceeds maximum limit, or it has nested references!");
                    }
                    for (PsiField field : psiClass.getAllFields()) {
                        map.put(field.getName(), typeResolve(field.getType(), level));
                    }
                    return map;
                }
            }
        }
    }


    /**
     * Get the value of the array type.
     * @param type PsiType.
     * @param level maximum reference level.
     * @return the value of the array type.
     */
    @NotNull
    private ArrayList<Object> getArrayTypeValue(PsiType type, int level) {
        PsiType deepType = type.getDeepComponentType();
        return Lists.newArrayList(typeResolve(deepType, level));
    }

    /**
     * Get the value of the iterable type.
     * @param type PsiType.
     * @param level maximum reference level.
     * @return the value of the iterable type.
     */
    @NotNull
    private Object getIterableTypeValue(PsiType type, int level) {
        PsiType deepType = PsiUtil.extractIterableTypeParameter(type, false);
        return Lists.newArrayList(typeResolve(deepType, level));
    }

    /**
     * Get the value of the primitive type.
     * @param type PsiType.
     * @return the value of the type.
     */
    private Object getPrimitiveTypeValue(PsiType type) {
        switch (type.getCanonicalText()) {
            case "boolean":
                return this.getFakeValue(normalTypes.get("Boolean"));
            case "byte":
            case "short":
            case "int":
            case "long":
                return this.getFakeValue(normalTypes.get("Number"));
            case "float":
            case "double":
                return this.getFakeValue(normalTypes.get("BigDecimal"));
            case "char":
                return this.getFakeValue(normalTypes.get("Character"));
            default:
                return null;
        }
    }
}
