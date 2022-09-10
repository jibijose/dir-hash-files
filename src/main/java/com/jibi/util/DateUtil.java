package com.jibi.util;

import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
public class DateUtil {

    private static String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss SSS zzz";
    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_FORMAT);

    public static String format(Date date) {
        return simpleDateFormat.format(date);
    }

    public static Date parse(String dateString) {
        try {
            return simpleDateFormat.parse(dateString);
        } catch (ParseException parseException) {
            log.warn("Parsing date error {}", dateString, parseException);
            return null;
        }
    }

}
