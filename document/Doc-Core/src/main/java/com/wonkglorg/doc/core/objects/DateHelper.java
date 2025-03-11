package com.wonkglorg.doc.core.objects;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateHelper {
    public static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static LocalDateTime parseDateTime(String dateTime) {
        return LocalDateTime.parse(dateTime, formatter);
    }

    public static String fromDateTime(LocalDateTime dateTime) {
        return dateTime.format(formatter);
    }


}
