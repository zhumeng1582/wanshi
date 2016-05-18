package com.wanshi.wanshi;

import android.app.Application;
import android.util.Log;

import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.im.v2.AVIMClient;
import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.AVIMException;
import com.avos.avoscloud.im.v2.AVIMMessage;
import com.avos.avoscloud.im.v2.AVIMMessageHandler;
import com.avos.avoscloud.im.v2.AVIMMessageManager;
import com.avos.avoscloud.im.v2.callback.AVIMClientCallback;
import com.avos.avoscloud.im.v2.messages.AVIMTextMessage;
import com.avos.avoscloud.okhttp.OkHttpClient;
import com.avos.avoscloud.okhttp.Request;
import com.avos.avoscloud.okhttp.Response;
import com.facebook.drawee.backends.pipeline.Fresco;

public class WanShiApplication  extends Application {

//    private final OkHttpClient client = new OkHttpClient();
//    private String url ="http://180.76.177.27:8899/update";
    public static class CustomMessageHandler extends AVIMMessageHandler {
        //接收到消息后的处理逻辑
        @Override
        public void onMessage(AVIMMessage message,AVIMConversation conversation,AVIMClient client){
            if(message instanceof AVIMTextMessage){
                Log.d("Tom & Jerry", ((AVIMTextMessage) message).getText());
            }
        }

        public void onMessageReceipt(AVIMMessage message,AVIMConversation conversation,AVIMClient client){

        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // 初始化参数依次为 this, AppId, AppKey
        AVOSCloud.initialize(this, "6XAMX8kyMgRHupTjhaWaRuJE-gzGzoHsz", "vxeSU5GS0qTN7dxK93yeMnBb");
        Fresco.initialize(this);
        //注册默认的消息处理逻辑
        AVIMMessageManager.registerDefaultMessageHandler(new CustomMessageHandler());
        jerryReceiveMsgFromTom();

//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    execute();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();

    }

    public void jerryReceiveMsgFromTom() {
        //Jerry登录
        AVIMClient jerry = AVIMClient.getInstance("Jerry");
        jerry.open(new AVIMClientCallback() {

            @Override
            public void done(AVIMClient client, AVIMException e) {
                if (e == null) {
                    //登录成功后的逻辑
                }
            }
        });
    }

//    public void execute() throws Exception {
//        Request request = new Request.Builder().url(url).build();
//        Response response = client.newCall(request).execute();
//        if(response.isSuccessful()){
//            System.out.println(response.code());
//            System.out.println(response.body().string());
//        }
//    }
}