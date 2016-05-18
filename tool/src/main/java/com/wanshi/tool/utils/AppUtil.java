package com.wanshi.tool.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 跟App相关的辅助类
 */
public class AppUtil {

    private static final String TAG = AppUtil.class.getName();

    private AppUtil() {
        /* cannot be instantiated */
        throw new UnsupportedOperationException("cannot be instantiated");

    }

    /**
     * 获取程序外部(sd)的目录
     *
     * @param mContext
     * @return
     */
    public static File getExternalDir(Context mContext, String dirName) {
        final String cacheDir = "/Android/data/" + mContext.getPackageName()
                + "/";
        return new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                + cacheDir + dirName + "/");
    }

    public static boolean isSDcardExsit() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }


    public static String getCurrentTime() {
        long currentTime = System.currentTimeMillis();
        Date date = new Date(currentTime);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String time = sdf.format(date);
        return time;
    }

    public static String getVerName(Context mContext) {
        PackageManager pm = mContext.getPackageManager();
        PackageInfo pi;
        try {
            pi = pm.getPackageInfo(mContext.getPackageName(), PackageManager.GET_ACTIVITIES);
        } catch (NameNotFoundException e) {
            Log.e(TAG, "Error while collect package info", e);
            e.printStackTrace();
            return "error";
        }
        if (pi == null) {
            return "error1";
        }
        String versionName = pi.versionName;
        if (versionName == null) {
            return "not set";
        }
        return versionName;
    }

    //将设备信息组装到文字里面
    public static String getDeviceInfo(Context mContext) {
        String lineSeparator = "\r\n";
        String logTime = "time:" + AppUtil.getCurrentTime();
        String appVerName = "appVerName:" + AppUtil.getVerName(mContext);
        String appVerCode = "appVerCode:" + AppUtil.getVersion(mContext);
        String OsVer = "OsVer:" + Build.VERSION.RELEASE;
        String vendor = "vendor:" + Build.MANUFACTURER;
        String model = "model:" + Build.MODEL;
        String mid = "mid:" + AppUtil.getMid(mContext);
        StringBuilder sb = new StringBuilder();
        sb.append("&gotoMarket---").append(lineSeparator);
        sb.append(logTime).append(lineSeparator);
        sb.append(appVerName).append(lineSeparator);
        sb.append(appVerCode).append(lineSeparator);
        sb.append(OsVer).append(lineSeparator);
        sb.append(vendor).append(lineSeparator);
        sb.append(model).append(lineSeparator);
        sb.append(mid).append(lineSeparator);
        sb.append("&end---").append(lineSeparator).append(lineSeparator);
        return sb.toString();
    }

    // 获取当前版本号
    public static String getVersion(Context context) {
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            return info.versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }


    public static String getMid(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String imei = tm.getDeviceId();
        String AndroidID = android.provider.Settings.System.getString(context.getContentResolver(), "android_id");
        String serialNo = getDeviceSerialForMid2();
        String m2 = getMD5Str("" + imei + AndroidID + serialNo);
        return m2;
    }

    public static String getDeviceID(Context context) {
        TelephonyManager telmgr;
        try{
            telmgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            return telmgr.getDeviceId();
        }
        catch (SecurityException e) {
            e.printStackTrace();
            return "000000000000000";
        }

    }

    private static String getDeviceSerialForMid2() {
        String serial = "";
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class);
            serial = (String) get.invoke(c, "ro.serialno");
        } catch (Exception ignored) {
        }
        return serial;
    }

    public static String getMD5Str(String str) {
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update(str.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "";
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "";
        }
        byte[] byteArray = messageDigest.digest();
        StringBuilder md5StrBuff = new StringBuilder();
        for (int i = 0; i < byteArray.length; i++) {
            if (Integer.toHexString(0xFF & byteArray[i]).length() == 1)
                md5StrBuff.append("0").append(Integer.toHexString(0xFF & byteArray[i]));
            else
                md5StrBuff.append(Integer.toHexString(0xFF & byteArray[i]));
        }


        return md5StrBuff.toString();
    }

    public static boolean hasPermission(Context context) {
        if (context != null) {
            boolean b1 = hasPermission(context,"android.permission.INTERNET");
            boolean b2 =  hasPermission(context,"android.permission.READ_PHONE_STATE");
            boolean b3 = hasPermission(context,"android.permission.WRITE_EXTERNAL_STORAGE");
            boolean b4 =hasPermission(context,"android.permission.ACCESS_NETWORK_STATE");
            boolean b5 =  hasPermission(context,"android.permission.ACCESS_WIFI_STATE");

            if (!b1 || !b2 || !b3 || !b4 || !b5) {
                Log.d(TAG, "没有添加权限");
                Toast.makeText(context.getApplicationContext(), "没有添加权限", Toast.LENGTH_SHORT).show();
            }
            return b1 && b2 && b3 && b4 && b5;
        }

        return false;
    }



    public static Intent getMarketIntent(Context paramContext) {
        StringBuilder localStringBuilder = new StringBuilder().append("market://details?id=");
        String str = paramContext.getPackageName();
        localStringBuilder.append(str);
        Uri localUri = Uri.parse(localStringBuilder.toString());
        return new Intent("android.intent.action.VIEW", localUri);
    }

    /**
     * 方法  的功能描述：直接跳转不判断是否存在市场应用
     * @param paramContext 上下文
     * @param paramString uri
     */
    public static void gotoMarket(Context paramContext, String paramString) {
        Uri localUri = Uri.parse(paramString);
        Intent localIntent = new Intent("android.intent.action.VIEW", localUri);
        localIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        paramContext.startActivity(localIntent);
    }

    public static boolean isMarketExist(Context paramContext, Intent paramIntent) {
        List<ResolveInfo> localList = paramContext.getPackageManager().queryIntentActivities(paramIntent, PackageManager.GET_INTENT_FILTERS);
        return ((localList != null) && (localList.size() > 0));
    }

    /**
     * 方法 isApkDebugable 的功能描述：判断APK是否处于调试模式
     * @param context 上下文
     */
    public static boolean isApkDebugable(Context context) {
        try {
            ApplicationInfo info= context.getApplicationInfo();
            return (info.flags& ApplicationInfo.FLAG_DEBUGGABLE)!=0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    /**
     * 方法 isApkDebugable 的功能描述：判断APK是否处于调试模式
     * @param context 上下文
     * @param packageName 包名
     */
    public static boolean isApkDebugable(Context context,String packageName) {
        try {
            PackageInfo pkginfo = context.getPackageManager().getPackageInfo(
                    packageName, 1);
            if (pkginfo != null ) {
                ApplicationInfo info= pkginfo.applicationInfo;
                return (info.flags&ApplicationInfo.FLAG_DEBUGGABLE)!=0;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    public static boolean hasPermission(Context mContext,String permission){
        PackageManager pm = mContext.getPackageManager();
        return (PackageManager.PERMISSION_GRANTED ==
                pm.checkPermission(permission, mContext.getPackageName()));

    }
    public static boolean isMNC(){
        return Build.VERSION.SDK_INT>=23;
    }

    public static void call(Context context, String phoneNumber) {
        context.startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumber)));
    }

    public static void installApk(Context context, String appFullName) {
            Uri uri = Uri.fromFile(new File(appFullName));
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setDataAndType(uri, "application/vnd.android.package-archive");
            context.startActivity(intent);
    }
    public static boolean isMainProcess(Context context) {
        ActivityManager am = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE));
        List<ActivityManager.RunningAppProcessInfo> processInfos = am.getRunningAppProcesses();
        String mainProcessName = context.getPackageName();
        int myPid = android.os.Process.myPid();
        for (ActivityManager.RunningAppProcessInfo info : processInfos) {
            if (info.pid == myPid && mainProcessName.equals(info.processName)) {
                return true;
            }
        }
        return false;
    }

}
