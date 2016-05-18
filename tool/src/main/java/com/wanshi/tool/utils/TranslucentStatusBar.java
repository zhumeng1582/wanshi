package com.wanshi.tool.utils;

import android.app.Activity;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.view.WindowManager;

public class TranslucentStatusBar {

	public static void set(Activity mContext){
		//判断当前SDK版本号，如果是4.4以上，就是支持沉浸式状态栏的
		if (VERSION.SDK_INT >= VERSION_CODES.KITKAT) {
			mContext.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
			mContext.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
		}
	}
}
