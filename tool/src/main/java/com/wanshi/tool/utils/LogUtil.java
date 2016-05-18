package com.wanshi.tool.utils;

import android.text.TextUtils;
import android.util.Log;

import com.wanshi.tool.Tool;


/**
 * Log统一管理类
 */
public class LogUtil {

    private LogUtil() {
        /* cannot be instantiated */
        throw new UnsupportedOperationException("cannot be instantiated");
    }

    private static final String TAG = "LogUtil";

    // 下面四个是默认tag的函数
    public static void i(String msg) {
        if ((Tool.DEBUG)&&(!TextUtils.isEmpty(msg)))
            Log.i(TAG, msg);
        else if((Tool.DEBUG)&&(TextUtils.isEmpty(msg)))
            Log.i(TAG, "<--------------------msg为空----------------------->");
    }

    public static void d(String msg) {
        if ((Tool.DEBUG)&&(!TextUtils.isEmpty(msg)))
            Log.d(TAG, msg);
        else if((Tool.DEBUG)&&(TextUtils.isEmpty(msg)))
            Log.d(TAG, "<--------------------msg为空----------------------->");
    }

    public static void e(String msg) {
        if ((Tool.DEBUG)&&(!TextUtils.isEmpty(msg)))
            Log.e(TAG, msg);
        else if((Tool.DEBUG)&&(TextUtils.isEmpty(msg)))
            Log.e(TAG, "<--------------------msg为空----------------------->");
    }

    public static void v(String msg) {
        if ((Tool.DEBUG)&&(!TextUtils.isEmpty(msg)))
            Log.v(TAG, msg);
        else if((Tool.DEBUG)&&(TextUtils.isEmpty(msg)))
            Log.v(TAG, "<--------------------msg为空----------------------->");
    }

    // 下面是传入自定义tag的函数
    public static void i(String tag, String msg) {
        if ((Tool.DEBUG)&&(!TextUtils.isEmpty(msg)))
            Log.i(tag, msg);
        else if((Tool.DEBUG)&&(TextUtils.isEmpty(msg)))
            Log.i(tag, "<--------------------msg为空----------------------->");
    }

    public static void d(String tag, String msg) {
        if ((Tool.DEBUG)&&(!TextUtils.isEmpty(msg)))
            Log.d(tag, msg);
        else if((Tool.DEBUG)&&(TextUtils.isEmpty(msg)))
            Log.d(tag, "<--------------------msg为空----------------------->");
    }

    public static void e(String tag, String msg) {
        if ((Tool.DEBUG)&&(!TextUtils.isEmpty(msg)))
            Log.e(tag, msg);
        else if((Tool.DEBUG)&&(TextUtils.isEmpty(msg)))
            Log.e(tag, "<--------------------msg为空----------------------->");
    }

    public static void v(String tag, String msg) {
        if ((Tool.DEBUG)&&(!TextUtils.isEmpty(msg)))
            Log.v(tag, msg);
        else if((Tool.DEBUG)&&(TextUtils.isEmpty(msg)))
            Log.v(tag, "<--------------------msg为空----------------------->");
    }
}