package com.wanshi.app.page.videolive;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import com.wanshi.app.R;

import org.kymjs.kjframe.KJActivity;

/**
 * 项目名称：蜂鸟金融
 * 类描述：
 * 创建人：zhangchao
 * 创建时间：2016-07-05 16:30
 * 修改人：zhangchao
 * 修改时间：2016-07-05 16:30
 * 修改备注：
 */
public class LiveVideoActivity  extends KJActivity {
    public View rootView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        rootView = findViewById(R.id.root_content);

        LiveVideoFragment fragment = new LiveVideoFragment();

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.layout_video_play, fragment)
                .commit();

        new ConversationDialogFragment().show(getSupportFragmentManager(), "ConversationDialogFragment");
    }

    @Override
    public void setRootView() {
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_live);
    }
    @Override
    public void initWidget() {
        super.initWidget();
    }
}

