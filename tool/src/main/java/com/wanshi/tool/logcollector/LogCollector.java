package com.wanshi.tool.logcollector;

import android.content.Context;


/**
 * 
 * @author jiabin
 *
 */
public class LogCollector {

private static final String TAG = LogCollector.class.getName();

	public static boolean DEBUG = false;
	private static String Upload_Url;
	
	private static Context mContext;
	
	private static String mPath;
	
	private static boolean isInit = false;
	


	public static void init(Context c ,String path){
		if(c == null){
			return;
		}
		if(isInit){
			return;
		}
		mContext = c;
		mPath = path;
		CrashHandler crashHandler = CrashHandler.getInstance(mContext, mPath);
		crashHandler.init();
		isInit = true;
	}
	public static void setDebugMode(boolean isDebug){
		DEBUG = isDebug;
	}
}
