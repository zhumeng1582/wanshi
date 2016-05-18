package com.wanshi.tool.utils;

import android.text.TextUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by zhangchao on 2015/12/22.
 */
public class DateTimeUtil {


    public static final SimpleDateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
    public static final SimpleDateFormat DATE_FORMAT_DATE = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
    public static final SimpleDateFormat MONTH_DATE_FORMAT_DATE = new SimpleDateFormat("MM-dd HH:mm", Locale.CHINA);

    private DateTimeUtil() {
        throw new AssertionError();
    }

    /**
     * long time to string
     *
     * @param timeInMillis 时间
     * @param dateFormat   需要转化的格式
     * @return String
     */
    public static String getTime(long timeInMillis, SimpleDateFormat dateFormat) {
        return dateFormat.format(new Date(timeInMillis));
    }

    /**
     * long time to string, format is {@link #DEFAULT_DATE_FORMAT}
     *
     * @param timeInMillis 时间
     * @return String
     */
    public static String getTime(long timeInMillis) {
        return getTime(timeInMillis, DEFAULT_DATE_FORMAT);
    }

    /**
     * long time to string, format is {@link #DEFAULT_DATE_FORMAT}
     *
     * @param timeInMillis
     * @return
     */
    public static String getDateStr(long timeInMillis) {
        return getTime(timeInMillis, DEFAULT_DATE_FORMAT);
    }

    /**
     * long time to string, format is {@link #DEFAULT_DATE_FORMAT}
     *
     * @param timeInMillis
     * @return
     */
    public static String getMonthDate(long timeInMillis) {
        return getTime(timeInMillis, MONTH_DATE_FORMAT_DATE);
    }

    /**
     * long time to string, format is {@link #DEFAULT_DATE_FORMAT}
     *
     * @param timeInMillis
     * @return
     */
    public static String getDate(long timeInMillis) {
        return getTime(timeInMillis, DATE_FORMAT_DATE);
    }

    /**
     * long time to string, format is {@link #DEFAULT_DATE_FORMAT}
     *
     * @param timeInMillis
     * @return
     */
    public static String getDate(String timeInMillis) {
        return getTime(Long.parseLong(timeInMillis), DATE_FORMAT_DATE);
    }

    /**
     * get current time in milliseconds
     *
     * @return
     */
    public static long getCurrentTimeInLong() {
        return System.currentTimeMillis();
    }

    /**
     * get current time in milliseconds, format is {@link #DEFAULT_DATE_FORMAT}
     *
     * @return
     */
    public static String getCurrentTimeInString() {
        return getTime(getCurrentTimeInLong());
    }

    /**
     * get current time in milliseconds
     *
     * @return
     */
    public static String getCurrentTimeInString(SimpleDateFormat dateFormat) {
        return getTime(getCurrentTimeInLong(), dateFormat);
    }

    /**
     * 将日期格式的字符串转换为长整型
     *
     * @param date
     * @return
     */
    public static long convertDate2long(String date) {
        try {
            if (!TextUtils.isEmpty(date)) {
                return DATE_FORMAT_DATE.parse(date).getTime();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 将日期格式的字符串转换为长整型
     *
     * @param dateTime
     * @return
     */
    public static long convertDateTime2long(String dateTime) {
        try {
            if (!TextUtils.isEmpty(dateTime)) {
                return DEFAULT_DATE_FORMAT.parse(dateTime).getTime();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }


    public static long[] timesformatSecond(long second) {
        long mday = 0, mhour = 0, mmin = 0, msecond = 0;
        if (second >= 0) {
            mday = (int) (second / (60 * 60 * 24));
            mhour = (int) (second / (60 * 60) - mday * 24);
            mmin = (int) (second / 60 - mhour * 60 - mday * 60 * 24);
            msecond = (int) (second - mmin * 60 - mhour * 60 * 60 - mday * 60 * 60 * 24);
        }
        return new long[]{mday, mhour, mmin, msecond};
    }

}
