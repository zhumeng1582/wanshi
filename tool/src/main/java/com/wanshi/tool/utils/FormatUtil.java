package com.wanshi.tool.utils;

import android.text.TextUtils;

import java.text.DecimalFormat;

/**
 * Created by Memory on 2016/1/4.
 */
public class FormatUtil {

    //格式化金钱格式
    public static String amountFormat(double money) {

        DecimalFormat decimalFormat = new DecimalFormat("##,###,##0.00");
        String amountFormat = decimalFormat.format(money);
        return amountFormat;
    }

    //利率格式化
    public static String profitFormat(double money) {
        DecimalFormat decimalFormat = new DecimalFormat("######0.00");
        return decimalFormat.format(money * 100);

    }

    public static int String2Int(String value) {
        if (TextUtils.isEmpty(value)) {
            return 0;
        }

        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    return -1;
    }
    public static double String2Double(String value) {
        if (TextUtils.isEmpty(value)) {
            return 0;
        }

        try {
            return Double.valueOf(value);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return -1;
    }
}
