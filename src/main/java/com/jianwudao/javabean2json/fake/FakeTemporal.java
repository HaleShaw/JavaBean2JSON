package com.jianwudao.javabean2json.fake;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class FakeTemporal implements FakeService {

    public final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    @Override
    public Object random() {
        LocalDateTime now = LocalDateTime.now();
        long begin = now.plusYears(10).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long end = now.minusYears(10).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        return begin + (long) (Math.random() * (end - begin));
    }

    @Override
    public Object def() {
        return Instant.now().toEpochMilli();
    }
}
