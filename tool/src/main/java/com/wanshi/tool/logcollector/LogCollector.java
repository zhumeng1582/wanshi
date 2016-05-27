package com.wanshi.tool.logcollector;

import android.content.Context;

import com.wanshi.tool.logcollector.capture.CrashHandler;
import com.wanshi.tool.logcollector.utils.Constants;
import com.wanshi.tool.logcollector.utils.LogHelper;


/**
 * 
 * @author jiabin
 *
 */
public class LogCollector {

private static final String TAG = LogCollector.class.getName();

	
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
		
		CrashHandler crashHandler = CrashHandler.getInstance(c,path);
		crashHandler.init();
		
		isInit = true;
		
	}
	

	
	public static void setDebugMode(boolean isDebug){
		Constants.DEBUG = isDebug;
		LogHelper.enableDefaultLog = isDebug;
	}
}
