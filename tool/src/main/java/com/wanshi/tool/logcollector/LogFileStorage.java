package com.wanshi.tool.logcollector;

import android.content.Context;
import android.util.Log;

import com.wanshi.tool.utils.AppUtil;
import com.wanshi.tool.utils.LogUtil;

import java.io.File;
import java.io.FileOutputStream;

/**
 * @author jiabin
 */
public class LogFileStorage {

    public static final String LOG_SUFFIX = ".log";
    private static final String TAG = LogFileStorage.class.getName();
    private static final String CHARSET = "UTF-8";

    private static LogFileStorage sInstance;

    private Context mContext;

    private String path;// = Environment.getExternalStorageDirectory().getAbsolutePath()+ File.separator+"yijibang/log";

    private LogFileStorage(Context ctx, String path) {
        mContext = ctx.getApplicationContext();
        this.path = path;
    }

    public static synchronized LogFileStorage getInstance(Context ctx, String path) {
        if (ctx == null) {
            LogUtil.e(TAG, "Context is null");
            return null;
        }
        if (sInstance == null) {

            sInstance = new LogFileStorage(ctx, path);
        }
        return sInstance;
    }

    public File getUploadLogFile() {
        File dir = new File(path);//mContext.getFilesDir();
        File logFile = new File(dir, AppUtil.getMid(mContext)
                + LOG_SUFFIX);
        if (logFile.exists()) {
            return logFile;
        } else {
            return null;
        }
    }

    public boolean deleteUploadLogFile() {
        File dir = new File(path);//mContext.getFilesDir();
        File logFile = new File(dir, AppUtil.getMid(mContext)
                + LOG_SUFFIX);
        return logFile.delete();
    }

//    public boolean saveLogFile2Internal(String logString) {
//        try {
//            File dir = new File(path);//mContext.getFilesDir();
//            if (!dir.exists()) {
//                dir.mkdirs();
//            }
//            File logFile = new File(dir, AppUtil.getMid(mContext)
//                    + LOG_SUFFIX);
//            FileOutputStream fos = new FileOutputStream(logFile, true);
//            fos.write(logString.getBytes(CHARSET));
//            fos.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//            LogUtil.e(TAG, "saveLogFile2Internal failed!");
//            return false;
//        }
//        return true;
//    }

    public boolean saveLogFile2SDcard(String logString, boolean isAppend) {
        if (!AppUtil.isSDcardExsit()) {
            LogUtil.e(TAG, "sdcard not exist");
            return false;
        }
        try {
            File logDir = getExternalLogDir();
            if (!logDir.exists()) {
                logDir.mkdirs();
            }

            File logFile = new File(logDir, AppUtil.getMid(mContext)
                    + LOG_SUFFIX);
            /*if (!isAppend) {
				if (logFile.exists() && !logFile.isFile())
					logFile.delete();
			}*/
            LogUtil.d(TAG, logFile.getPath());

            FileOutputStream fos = new FileOutputStream(logFile, isAppend);
            fos.write(logString.getBytes(CHARSET));
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "saveLogFile2SDcard failed!");
            return false;
        }
        return true;
    }

    private File getExternalLogDir() {
        File logDir = AppUtil.getExternalDir(mContext, "Log");
        LogUtil.d(TAG, logDir.getPath());
        return logDir;
    }
}