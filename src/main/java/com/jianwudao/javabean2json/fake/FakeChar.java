package com.jianwudao.javabean2json.fake;

public class FakeChar implements FakeService {

    @Override
    public Object random() {
        return '0';
    }

    @Override
    public Object def() {
        return '0';
    }

}
