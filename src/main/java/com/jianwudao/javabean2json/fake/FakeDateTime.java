package com.jianwudao.javabean2json.fake;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class FakeDateTime extends FakeTemporal implements FakeService {

    @Override
    public Object random() {
        return LocalDateTime
                .ofInstant(Instant.ofEpochMilli((long) super.random()), ZoneId.systemDefault())
                .format(super.dateTimeFormatter);
    }

    @Override
    public Object def() {
        return LocalDateTime
                .ofInstant(Instant.ofEpochMilli((long) super.def()), ZoneId.systemDefault())
                .format(super.dateTimeFormatter);
    }
}
