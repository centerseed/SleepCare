package com.barry.sleepcare.utils;


import java.sql.Date;
import java.text.SimpleDateFormat;

public class TimeStrUtils {
    static SimpleDateFormat mDateTimeSdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    static SimpleDateFormat mDateSdf = new SimpleDateFormat("yyyy/MM/dd");
    static SimpleDateFormat mTimteSdf = new SimpleDateFormat("HH:mm:ss");

    public static String getDateTimeStr(long timestamp) {
        try {
            Date netDate = (new Date(timestamp));
            return mDateTimeSdf.format(netDate);
        } catch (Exception ex) {
            return "Invalid Time";
        }
    }

    public static String getDateStr(long timestamp) {
        try {
            Date netDate = (new Date(timestamp));
            return mDateSdf.format(netDate);
        } catch (Exception ex) {
            return "Invalid Time";
        }
    }

    public static String getTimeStr(long timestamp) {
        try {
            Date netDate = (new Date(timestamp));
            return mTimteSdf.format(netDate);
        } catch (Exception ex) {
            return "Invalid Time";
        }
    }
}
