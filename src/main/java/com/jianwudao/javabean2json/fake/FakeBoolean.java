package com.jianwudao.javabean2json.fake;

import java.util.Random;

public class FakeBoolean implements FakeService {

    private final Random random = new Random();

    @Override
    public Object random() {
        return random.nextBoolean();
    }

    @Override
    public Object def() {
        return false;
    }
}
