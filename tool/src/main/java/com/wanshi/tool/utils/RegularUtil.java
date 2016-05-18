package com.wanshi.tool.utils;

import android.text.TextUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by admin on 2015/12/8.
 */
public class RegularUtil {
    public static boolean isMobileNO(String value) {
//        String  MOBILE = "^1(3[0-9]|5[0-35-9]|8[025-9]|70|77)\\d{8}$";
//        String  CM = "^1(34[0-8]|(3[5-9]|5[017-9]|8[278])\\d)\\d{7}$";
//        String  CU = "^1(3[0-2]|5[256]|8[56])\\d{8}$";
//        String  CT = "^1((33|53|8[09])[0-9]|349)\\d{7}$";

        if (TextUtils.isEmpty(value))
            return false;
//        Pattern p = Pattern.compile("^((13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$");
        Pattern p = Pattern.compile("^(1)\\d{10}$");
        Matcher m = p.matcher(value);
        return m.matches();
    }

    public static boolean checkIdCard(String idCard) {
        if (TextUtils.isEmpty(idCard))
            return false;
       // String regex = "[1-9]\\d{13,16}[xX0-9]{1}";
        //Pattern p = Pattern.compile("(^[1-9]\\\\d{7}((0\\\\d)|(1[0-2]))(([0|1|2]\\\\d)|3[0-1])\\\\d{3}$)|(^[1-9]\d{5}[1-9]\d{3}((0\d)|(1[0-2]))(([0|1|2]\d)|3[0-1])\d{3}([0-9]|X)$)");
        Pattern p = Pattern.compile("^(\\d{15}$|^\\d{18}$|^\\d{17}(\\d|X|x))$");
        Matcher m = p.matcher(idCard);
        return m.matches();
    }

    public static boolean isLoginPassword(String value) {
        if (TextUtils.isEmpty(value))
            return false;
        // Pattern p = Pattern.compile("^[a-zA-Z0-9]{6,16}$");
        Pattern p = Pattern.compile("^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{6,16}$");
        Matcher m = p.matcher(value);
        return m.matches();
    }

    public static Matcher getSmsCodeMatcher(String value) {
        if (TextUtils.isEmpty(value))
            return null;
        Pattern p = Pattern.compile("(\\d{6})");
        return p.matcher(value);
    }

    public static boolean isSmsCode(String value) {
        if (TextUtils.isEmpty(value))
            return false;
        Pattern p = Pattern.compile("(\\d{6})");
        return p.matcher(value).matches();
    }


}
