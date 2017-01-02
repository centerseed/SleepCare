package com.barry.sleepcare.utils;


import java.sql.Date;
import java.text.SimpleDateFormat;

public class TimeStrUtils {
    static SimpleDateFormat mDataTimeSdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss\"");

    public static String getDateTimeStr(long timestamp) {
        try {
            Date netDate = (new Date(timestamp));
            return mDataTimeSdf.format(netDate);
        } catch (Exception ex) {
            return "Invalid Time";
        }
    }
}
