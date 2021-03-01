package com.jianwudao.javabean2json.fake;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

public class FakeDate extends FakeTemporal implements FakeService {

    @Override
    public Object random() {
        return LocalDate
                .ofInstant(Instant.ofEpochMilli((long) super.random()), ZoneId.systemDefault())
                .format(super.dateFormatter);
    }

    @Override
    public Object def() {
        return LocalDate
                .ofInstant(Instant.ofEpochMilli((long) super.def()), ZoneId.systemDefault())
                .format(super.dateFormatter);
    }

}
