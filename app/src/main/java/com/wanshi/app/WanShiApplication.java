package com.wanshi.app;

import android.app.Application;

import com.avos.avoscloud.AVOSCloud;
import com.facebook.drawee.backends.pipeline.Fresco;

public class WanShiApplication  extends Application {

//    private final OkHttpClient client = new OkHttpClient();
//    private String url ="http://180.76.177.27:8899/update";
    @Override
    public void onCreate() {
        super.onCreate();

        // 初始化参数依次为 this, AppId, AppKey
        AVOSCloud.initialize(this, "6XAMX8kyMgRHupTjhaWaRuJE-gzGzoHsz", "vxeSU5GS0qTN7dxK93yeMnBb");
        Fresco.initialize(this);

    }

}