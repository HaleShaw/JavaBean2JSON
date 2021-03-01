package com.jianwudao.javabean2json.fake;

import java.util.Random;

public class FakeString implements FakeService {

    private final Random random=new Random();

    @Override
    public Object random() {
        return getRandomString(random.nextInt(10));
    }

    @Override
    public Object def() {
        return "";
    }

    private String getRandomString(int length){
        StringBuffer sb=new StringBuffer();
        String str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        for(int i=0;i<length;i++){
            int number=random.nextInt(str.length());
            sb.append(str.charAt(number));
        }
        return sb.toString();
    }
}
