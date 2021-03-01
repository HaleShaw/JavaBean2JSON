package com.jianwudao.javabean2json;

import com.jianwudao.javabean2json.fake.FakeService;

public class JavaBean2JSONRandomAction extends JavaBean2JSONAction {

    @Override
    protected Object getFakeValue(FakeService fakeService) {
        return fakeService.random();
    }
}
