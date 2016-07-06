package com.wanshi.app.page.videolive;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.Process;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.baidu.cyberplayer.core.BVideoView;
import com.baidu.cyberplayer.core.BVideoView.OnCompletionListener;
import com.baidu.cyberplayer.core.BVideoView.OnErrorListener;
import com.baidu.cyberplayer.core.BVideoView.OnInfoListener;
import com.baidu.cyberplayer.core.BVideoView.OnPlayingBufferCacheListener;
import com.baidu.cyberplayer.core.BVideoView.OnPreparedListener;
import com.umeng.analytics.MobclickAgent;
import com.wanshi.app.R;
import com.wanshi.app.config.Contants;
import com.wanshi.tool.utils.logger.Logger;

import org.kymjs.kjframe.ui.BindView;
import org.kymjs.kjframe.ui.KJFragment;
import org.kymjs.kjframe.utils.KJLoger;

public class LiveVideoFragment extends KJFragment implements OnPreparedListener, OnCompletionListener,
        OnErrorListener, OnInfoListener, OnPlayingBufferCacheListener {

    private String mVideoSource = null;

    @BindView(id = R.id.controlbar)
    private LinearLayout mController;
    @BindView(id = R.id.textCurrentTime)
    private TextView mCurrPostion;

    /**
     * 记录播放位置
     */
    private int mLastPos = 0;

    /**
     * 播放状态
     */
    private enum PLAYER_STATUS {
        PLAYER_IDLE, PLAYER_PREPARING, PLAYER_PREPARED,
    }

    private PLAYER_STATUS mPlayerStatus = PLAYER_STATUS.PLAYER_IDLE;

    @BindView(id = R.id.video_view)
    private BVideoView mVV;
    private EventHandler mEventHandler;
    private HandlerThread mHandlerThread;
    private final Object SYNC_Playing = new Object();
    private WakeLock mWakeLock = null;
    private static final String POWER_LOCK = "LiveVideoFragment";
    private boolean mIsHwDecode = false;
    private final int EVENT_PLAY = 0;
    private final int UI_EVENT_UPDATE_CURRPOSITION = 1;


    class EventHandler extends Handler {
        public EventHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case EVENT_PLAY:
                     // 如果已经播放了，等待上一次播放结束
                    if (mPlayerStatus != PLAYER_STATUS.PLAYER_IDLE) {
                        synchronized (SYNC_Playing) {
                            try {
                                SYNC_Playing.wait();
                                KJLoger.debug("wait player status to idle");
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    mVV.setVideoPath(mVideoSource);
                    //续播，如果需要如此
                    if (mLastPos > 0) {
                        mVV.seekTo(mLastPos);
                        mLastPos = 0;
                    }
                    //显示或者隐藏缓冲提示
                    mVV.showCacheInfo(true);
                    //开始播放
                    mVV.start();
                    mPlayerStatus = PLAYER_STATUS.PLAYER_PREPARING;
                    break;
                default:
                    break;
            }
        }
    }

    Handler mUIHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                //更新进度及时间
                case UI_EVENT_UPDATE_CURRPOSITION:
                    int currPosition = mVV.getCurrentPosition();
                    updateTextViewWithTimeFormat(mCurrPostion, currPosition);
                    mUIHandler.sendEmptyMessageDelayed(UI_EVENT_UPDATE_CURRPOSITION, 200);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected View inflaterView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        View view = inflater.inflate(R.layout.fragment_live_video, container, false);
        return view;
    }

    @Override
    public void initWidget(View view) {
        super.initWidget(view);



        PowerManager pm = (PowerManager) getActivity().getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, POWER_LOCK);

        mIsHwDecode = getActivity().getIntent().getBooleanExtra("isHW", false);
        Uri uriPath = getActivity().getIntent().getData();
        if (null != uriPath) {
            String scheme = uriPath.getScheme();
            if (null != scheme) {
                mVideoSource = uriPath.toString();
            } else {
                mVideoSource = uriPath.getPath();
            }
        }


        initUI();

        /**
         * 开启后台事件处理线程
         */
        mHandlerThread = new HandlerThread("event handler thread", Process.THREAD_PRIORITY_BACKGROUND);
        mHandlerThread.start();
        mEventHandler = new EventHandler(mHandlerThread.getLooper());


    }




    /**
     * 初始化界面
     */
    private void initUI() {

        registerCallbackForControl();

        /**
         * 设置ak
         */
        BVideoView.setAK(Contants.AK);

        /**
         * 注册listener
         */
        mVV.setOnPreparedListener(this);
        mVV.setOnCompletionListener(this);
        mVV.setOnErrorListener(this);
        mVV.setOnInfoListener(this);

        /**
         * 设置解码模式
         */
        mVV.setDecodeMode(mIsHwDecode ? BVideoView.DECODE_HW : BVideoView.DECODE_SW);
    }

    /**
     * 为控件注册回调处理函数
     */
    private void registerCallbackForControl() {
//        mPlaybtn.setOnClickListener(new OnClickListener() {
//            public void onClick(View v) {
//                if (mVV.isPlaying()) {
//                    mPlaybtn.setImageResource(R.drawable.btn_style_play);
//                    //暂停播放
//                    mVV.pause();
//                } else {
//                    mPlaybtn.setImageResource(R.drawable.btn_style_pause);
//                    //继续播放
//                    mVV.resume();
//                }
//
//            }
//        });
//        btnFullScreen.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(llConversation.getVisibility() == View.VISIBLE){
//                    llConversation.setVisibility(View.GONE);
//                    btnFullScreen.setImageResource(R.drawable.btn_style_zoom_out);
//                }else{
//                    llConversation.setVisibility(View.VISIBLE);
//                    btnFullScreen.setImageResource(R.drawable.btn_style_zoom_in);
//                }
//            }
//        });
    }

    private void updateTextViewWithTimeFormat(TextView view, int second) {
        int hh = second / 3600;
        int mm = second % 3600 / 60;
        int ss = second % 60;
        String strTemp = null;
        if (0 != hh) {
            strTemp = String.format("%02d:%02d:%02d", hh, mm, ss);
        } else {
            strTemp = String.format("%02d:%02d", mm, ss);
        }
        view.setText(strTemp);
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(getActivity());
        //在停止播放前 你可以先记录当前播放的位置,以便以后可以续播
        if (mPlayerStatus == PLAYER_STATUS.PLAYER_PREPARED) {
            mLastPos = mVV.getCurrentPosition();
            mVV.stopPlayback();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(getActivity());
        if (null != mWakeLock && (!mWakeLock.isHeld())) {
            mWakeLock.acquire();
        }
        //发起一次播放任务,当然您不一定要在这发起
        mEventHandler.sendEmptyMessage(EVENT_PLAY);
    }

    private long mTouchTime;
    private boolean barShow = true;

//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        if (event.getAction() == MotionEvent.ACTION_DOWN)
//            mTouchTime = System.currentTimeMillis();
//        else if (event.getAction() == MotionEvent.ACTION_UP) {
//            long time = System.currentTimeMillis() - mTouchTime;
//            if (time < 400) {
//                updateControlBar(!barShow);
//            }
//        }
//
//        return true;
//    }

    public void updateControlBar(boolean show) {

        if (show) {
            mController.setVisibility(View.VISIBLE);
        } else {
            mController.setVisibility(View.INVISIBLE);
        }
        barShow = show;
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //退出后台事件处理线程
        mHandlerThread.quit();

    }

    @Override
    public boolean onInfo(int what, int extra) {
        // TODO Auto-generated method stub
        switch (what) {
            //开始缓冲
            case BVideoView.MEDIA_INFO_BUFFERING_START:
                break;
            //结束缓冲
            case BVideoView.MEDIA_INFO_BUFFERING_END:
                break;
            default:
                break;
        }
        return true;
    }

    //当前缓冲的百分比， 可以配合onInfo中的开始缓冲和结束缓冲来显示百分比到界面
    @Override
    public void onPlayingBufferCache(int percent) {

    }

    //播放出错
    @Override
    public boolean onError(int what, int extra) {
        Logger.d( "onError");
        synchronized (SYNC_Playing) {
            SYNC_Playing.notify();
        }
        mPlayerStatus = PLAYER_STATUS.PLAYER_IDLE;
        mUIHandler.removeMessages(UI_EVENT_UPDATE_CURRPOSITION);
        return true;
    }

    //播放完成
    @Override
    public void onCompletion() {
        Logger.d( "onCompletion");
        synchronized (SYNC_Playing) {
            SYNC_Playing.notify();
        }
        mPlayerStatus = PLAYER_STATUS.PLAYER_IDLE;
        mUIHandler.removeMessages(UI_EVENT_UPDATE_CURRPOSITION);
    }

    //准备播放就绪
    @Override
    public void onPrepared() {
        // TODO Auto-generated method stub
        Logger.d("onPrepared");
        mPlayerStatus = PLAYER_STATUS.PLAYER_PREPARED;
        mUIHandler.sendEmptyMessage(UI_EVENT_UPDATE_CURRPOSITION);
    }
}
