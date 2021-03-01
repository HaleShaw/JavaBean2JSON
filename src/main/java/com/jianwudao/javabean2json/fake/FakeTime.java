package com.jianwudao.javabean2json.fake;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;

public class FakeTime extends FakeTemporal implements FakeService {

    @Override
    public Object random() {
        return LocalTime
                .ofInstant(Instant.ofEpochMilli((long) super.random()), ZoneId.systemDefault())
                .format(super.timeFormatter);
    }

    @Override
    public Object def() {
        return LocalTime
                .ofInstant(Instant.ofEpochMilli((long) super.def()), ZoneId.systemDefault())
                .format(super.timeFormatter);
    }
}
