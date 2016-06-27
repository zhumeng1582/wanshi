package com.wanshi.app;

import android.app.Application;
import android.os.Environment;

import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.okhttp.OkHttpClient;
import com.avos.avoscloud.okhttp.Request;
import com.avos.avoscloud.okhttp.Response;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.umeng.analytics.MobclickAgent;
import com.wanshi.app.cache.SharePreferenceUtil;
import com.wanshi.tool.logcollector.LogCollector;
import com.wanshi.tool.utils.logger.LogLevel;
import com.wanshi.tool.utils.logger.Logger;

import java.io.File;
import java.io.IOException;

public class WanShiApplication  extends Application {

    private final static String TAG = "WanShiApplication";
    private final OkHttpClient client = new OkHttpClient();
    private String url ="http://180.76.177.27:8899/update";

    public static final String crashLogFilePath = Environment.getExternalStorageDirectory().getAbsolutePath()+ File.separator+"wanshi/log";
    @Override
    public void onCreate() {
        super.onCreate();

        // 初始化参数依次为 this, AppId, AppKey
        AVOSCloud.initialize(this, "6XAMX8kyMgRHupTjhaWaRuJE-gzGzoHsz", "vxeSU5GS0qTN7dxK93yeMnBb");
        Fresco.initialize(this);
        MobclickAgent.setScenarioType(this, MobclickAgent.EScenarioType.E_UM_NORMAL);
        Logger.init("WanShi") .methodCount(3).logLevel(LogLevel.FULL);
        LogCollector.setDebugMode(true);
		LogCollector.init(getApplicationContext(), crashLogFilePath);// params

//        try {
//            run(url);
//        } catch (IOException e) {
//            e.printStackTrace();
//            KJLoger.debug(TAG, "onCompletion");
//        }


        AVUser currentUser = AVUser.getCurrentUser();
        if (currentUser != null) {
            SharePreferenceUtil.setUserName(this,AVUser.getCurrentUser().getString("nick"));
        } else {
            SharePreferenceUtil.setUserName(this,"");
        }
    }

    String run(String url) throws IOException {
        Request request = new Request.Builder().url(url).build();
        Response response = client.newCall(request).execute();
        if (response.isSuccessful()) {
            return response.body().string();
        } else {
            throw new IOException("Unexpected code " + response);
        }
    }
}