package com.jianwudao.javabean2json.fake;

import java.util.Random;

public class FakeInteger implements FakeService {

    private final Random random = new Random();

    @Override
    public Object random() {
        return random.nextInt(100);
    }

    @Override
    public Object def() {
        return 0;
    }
}
