package com.wanshi.app.page.base;

import android.app.Activity;

import com.orhanobut.logger.Logger;

/**
 * 项目名称：蜂鸟金融
 * 类描述：
 * 创建人：zhangchao
 * 创建时间：2016-05-18 11:13
 * 修改人：zhangchao
 * 修改时间：2016-05-18 11:13
 * 修改备注：
 */
public class BaseActivity extends Activity {
    protected String TAG =  getClass().getSimpleName();
    protected Activity mContext = this;
    protected void logd(String message){
        Logger.d(TAG, message);
    }
}
