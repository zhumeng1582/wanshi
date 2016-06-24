package com.wanshi.app.page.me;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.LogInCallback;
import com.avos.sns.SNS;
import com.avos.sns.SNSBase;
import com.avos.sns.SNSCallback;
import com.avos.sns.SNSException;
import com.avos.sns.SNSType;
import com.wanshi.app.cache.SharePreferenceUtil;
import com.wanshi.app.page.base.BaseActivity;

/**
 * 项目名称：蜂鸟金融
 * 类描述：
 * 创建人：zhangchao
 * 创建时间：2016-05-17 10:40
 * 修改人：zhangchao
 * 修改时间：2016-05-17 10:40
 * 修改备注：
 */
public class QQLoginActivity extends BaseActivity {

    // onCreate 中初始化，并且登录
    @Override
    public void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
//        AVOSCloud.initialize(this, "5Xl6WhjbLXKXCHEu9J4SUROE-gzGzoHsz", "pQCx7p2IiyLg4EESnpnCW6IS");
        AVOSCloud.initialize(this, "6XAMX8kyMgRHupTjhaWaRuJE-gzGzoHsz", "vxeSU5GS0qTN7dxK93yeMnBb");
        // callback 函数
        final SNSCallback myCallback = new SNSCallback() {
            @Override
            public void done(SNSBase object, SNSException e) {
                if (e == null) {
                    Log.d(TAG,"登录成功");
                    SNS.loginWithAuthData(object.userInfo(), new LogInCallback<AVUser>() {
                        @Override
                        public void done(AVUser avUser, AVException e) {
                            Log.d(TAG,"注册用户名: "+avUser.getUsername());
                            AVUser.getCurrentUser().put("nick", "用户");
                            AVUser.getCurrentUser().saveInBackground();
                            SharePreferenceUtil.setUserName(mContext,"用户");
                            finish();
                        }
                    });

                }else{
                    e.printStackTrace();
                    Log.d(TAG,"登录失败");
                }
            }
        };

        // 关联
        try {
            SNS.setupPlatform(this, SNSType.AVOSCloudSNSQQ, "1105334255", "", "https://leancloud.cn/1.1/sns/callback/wjo8pe6ezrkh47dx");
        } catch (AVException e) {
            e.printStackTrace();
        }
//        SNS.logout(this,SNSType.AVOSCloudSNSQQ);
        SNS.loginWithCallback(mContext, SNSType.AVOSCloudSNSQQ, myCallback);
    }

    // 当登录完成后，请调用 SNS.onActivityResult(requestCode, resultCode, data, type);
    // 这样你的回调用将会被调用到
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        SNS.onActivityResult(requestCode, resultCode, data, SNSType.AVOSCloudSNSQQ);
    }
}
