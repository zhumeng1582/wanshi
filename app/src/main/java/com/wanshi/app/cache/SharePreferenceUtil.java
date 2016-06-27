package com.wanshi.app.cache;

import android.content.Context;
import android.text.TextUtils;

import com.wanshi.tool.utils.SPUtil;

public class SharePreferenceUtil {

  private static final String keyUserName ="userName";


    //用户电话号码
    public static void setUserName(Context context, String name) {
        if(!TextUtils.isEmpty(name)){
            SPUtil.put(context, keyUserName, name);
        }else if(TextUtils.isEmpty(getUserName(context))){
            SPUtil.put(context, keyUserName, "游客"+System.currentTimeMillis());
        }
    }

    public static String getUserName(Context context) {
        return SPUtil.get(context, keyUserName, "");
    }
}
