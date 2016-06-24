package com.wanshi.app.cache;

import android.content.Context;

import com.wanshi.tool.utils.SPUtil;


/**
 * Created by zhangchao on 2015/12/22.
 */
public class SharePreferenceUtil {

  private static final String keyUserName ="userName";


    //用户电话号码
    public static void setUserName(Context context, String phone) {
        SPUtil.put(context, keyUserName, phone);
    }

    public static String getUserName(Context context) {
        return SPUtil.get(context, keyUserName, "");
    }




}
