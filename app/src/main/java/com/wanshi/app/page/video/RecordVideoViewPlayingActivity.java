package com.wanshi.app.page.video;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.Process;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.avos.avoscloud.im.v2.AVIMClient;
import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.AVIMException;
import com.avos.avoscloud.im.v2.AVIMMessage;
import com.avos.avoscloud.im.v2.callback.AVIMClientCallback;
import com.avos.avoscloud.im.v2.callback.AVIMConversationCallback;
import com.avos.avoscloud.im.v2.callback.AVIMMessagesQueryCallback;
import com.avos.avoscloud.im.v2.messages.AVIMTextMessage;
import com.baidu.cyberplayer.core.BVideoView;
import com.baidu.cyberplayer.core.BVideoView.OnCompletionListener;
import com.baidu.cyberplayer.core.BVideoView.OnErrorListener;
import com.baidu.cyberplayer.core.BVideoView.OnInfoListener;
import com.baidu.cyberplayer.core.BVideoView.OnPlayingBufferCacheListener;
import com.baidu.cyberplayer.core.BVideoView.OnPreparedListener;
import com.wanshi.app.R;
import org.kymjs.chat.ChatActivity;
import org.kymjs.chat.OnOperationListener;
import org.kymjs.chat.adapter.ChatAdapter;
import org.kymjs.chat.bean.Emojicon;
import org.kymjs.chat.bean.Faceicon;
import org.kymjs.chat.emoji.DisplayRules;
import org.kymjs.chat.widget.KJChatKeyboard;
import org.kymjs.kjframe.KJActivity;
import org.kymjs.kjframe.ui.ViewInject;
import org.kymjs.kjframe.utils.FileUtils;
import org.kymjs.kjframe.utils.KJLoger;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class RecordVideoViewPlayingActivity extends KJActivity implements OnPreparedListener, OnCompletionListener,
        OnErrorListener, OnInfoListener, OnPlayingBufferCacheListener {

    private final String TAG = "LiveVideoViewPlayingActivity";

    /**
     * 您的AK
     * 请到http://console.bce.baidu.com/iam/#/iam/accesslist获取
     */
    private String AK = "381715e304f04aceb2d7eb274cad3e27";   //请录入您的AK !!!

    private String mVideoSource = null;

    private ImageButton mPlaybtn = null;
    private ImageButton mPrebtn = null;
    private ImageButton mForwardbtn = null;

    private LinearLayout mController = null;

    private SeekBar mProgress = null;
    private TextView mDuration = null;
    private TextView mCurrPostion = null;
    public static final int REQUEST_CODE_GETIMAGE_BYSDCARD = 0x1;

    private KJChatKeyboard box;
    private ListView mRealListView;

    List<org.kymjs.chat.bean.Message> datas = new ArrayList<org.kymjs.chat.bean.Message>();
    private ChatAdapter adapter;

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

    private BVideoView mVV = null;

    private EventHandler mEventHandler;
    private HandlerThread mHandlerThread;

    private final Object SYNC_Playing = new Object();

    private WakeLock mWakeLock = null;
    private static final String POWER_LOCK = "LiveVideoViewPlayingActivity";

    private boolean mIsHwDecode = false;
    private String conversationId = null;

    private final int EVENT_PLAY = 0;
    private final int UI_EVENT_UPDATE_CURRPOSITION = 1;
    private AVIMConversation conv;
    private String name ="游客";

    class EventHandler extends Handler {
        public EventHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case EVENT_PLAY:
                    /**
                     * 如果已经播放了，等待上一次播放结束
                     */
                    if (mPlayerStatus != PLAYER_STATUS.PLAYER_IDLE) {
                        synchronized (SYNC_Playing) {
                            try {
                                SYNC_Playing.wait();
                                KJLoger.debug(TAG, "wait player status to idle");
                            } catch (InterruptedException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                    }

                    /**
                     * 设置播放url
                     */
                    mVV.setVideoPath(mVideoSource);

                    /**
                     * 续播，如果需要如此
                     */
                    if (mLastPos > 0) {

                        mVV.seekTo(mLastPos);
                        mLastPos = 0;
                    }

                    /**
                     * 显示或者隐藏缓冲提示
                     */
                    mVV.showCacheInfo(true);

                    /**
                     * 开始播放
                     */
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
                /**
                 * 更新进度及时间
                 */
                case UI_EVENT_UPDATE_CURRPOSITION:
                    int currPosition = mVV.getCurrentPosition();
                    int duration = mVV.getDuration();
                    updateTextViewWithTimeFormat(mCurrPostion, currPosition);
                    updateTextViewWithTimeFormat(mDuration, duration);
                    mProgress.setMax(duration);
                    if (mVV.isPlaying()) {
                        mProgress.setProgress(currPosition);
                    }
                    mUIHandler.sendEmptyMessageDelayed(UI_EVENT_UPDATE_CURRPOSITION, 200);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void setRootView() {
        setContentView(R.layout.activity_record_playing);
    }

    @Override
    public void initWidget() {
        super.initWidget();

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, POWER_LOCK);

        mIsHwDecode = getIntent().getBooleanExtra("isHW", false);
        Uri uriPath = getIntent().getData();
        if (null != uriPath) {
            String scheme = uriPath.getScheme();
            if (null != scheme) {
                mVideoSource = uriPath.toString();
            } else {
                mVideoSource = uriPath.getPath();
            }
        }

        conversationId = getIntent().getStringExtra("conversationId");
        if (!TextUtils.isEmpty(conversationId)) {
            addConversation();
        }else{
            findViewById(R.id.llConversation).setVisibility(View.GONE);

        }

        initUI();

        /**
         * 开启后台事件处理线程
         */
        mHandlerThread = new HandlerThread("event handler thread",
                Process.THREAD_PRIORITY_BACKGROUND);
        mHandlerThread.start();
        mEventHandler = new EventHandler(mHandlerThread.getLooper());

        box = (KJChatKeyboard) findViewById(org.kymjs.chat.R.id.chat_msg_input_box);
        mRealListView = (ListView) findViewById(org.kymjs.chat.R.id.chat_listview);

        mRealListView.setSelector(android.R.color.transparent);
        initMessageInputToolBox();
        initListView();
    }

    private void addConversation() {
        AVIMClient tom = AVIMClient.getInstance(name);
        tom.open(new AVIMClientCallback() {

            @Override
            public void done(AVIMClient client, AVIMException e) {
                if (e == null) {
                    //登录成功
                    conv = client.getConversation(conversationId);
                    conv.join(new AVIMConversationCallback() {
                        @Override
                        public void done(AVIMException e) {
                            if (e == null) {
                                //加入成功
                                KJLoger.log(TAG, "加入成功聊天室,id:" + conversationId);
                                conv.queryMessages(10, new AVIMMessagesQueryCallback() {
                                    @Override
                                    public void done(List<AVIMMessage> messages, AVIMException e) {
                                        if (e == null) {
                                            for (AVIMMessage item : messages) {
                                                org.kymjs.chat.bean.Message message = new org.kymjs.chat.bean.Message(org.kymjs.chat.bean.Message.MSG_TYPE_TEXT,
                                                        org.kymjs.chat.bean.Message.MSG_STATE_SUCCESS, item.getFrom(), "avatar", "Jerry", "avatar",
                                                        ((AVIMTextMessage) item).getText(), name.equals(item.getFrom()), true, new Date(item.getTimestamp()));
                                                datas.add(message);
                                                adapter.refresh(datas);
                                            }
                                        }
                                    }
                                });
                            }
                        }
                    });
                }
            }


        });

    }


    /**
     * 初始化界面
     */
    private void initUI() {
        mPlaybtn = (ImageButton) findViewById(R.id.play_btn);
        mPrebtn = (ImageButton) findViewById(R.id.pre_btn);
        mForwardbtn = (ImageButton) findViewById(R.id.next_btn);
        mController = (LinearLayout) findViewById(R.id.controlbar);

        mProgress = (SeekBar) findViewById(R.id.media_progress);
        mDuration = (TextView) findViewById(R.id.time_total);
        mCurrPostion = (TextView) findViewById(R.id.time_current);


        registerCallbackForControl();

        /**
         * 设置ak
         */
        BVideoView.setAK(AK);

        /**
         *获取BVideoView对象
         */
        mVV = (BVideoView) findViewById(R.id.video_view);

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
        mPlaybtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // TODO Auto-generated method stub

                if (mVV.isPlaying()) {
                    mPlaybtn.setImageResource(R.drawable.play_btn_style);
                    /**
                     * 暂停播放
                     */
                    mVV.pause();
                } else {
                    mPlaybtn.setImageResource(R.drawable.pause_btn_style);
                    /**
                     * 继续播放
                     */
                    mVV.resume();
                }

            }
        });

        /**
         * 实现切换示例
         */
        mPrebtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                /**
                 * 如果已经播放了，等待上一次播放结束
                 */
                if (mPlayerStatus != PLAYER_STATUS.PLAYER_IDLE) {
                    mVV.stopPlayback();
                }

                /**
                 * 发起一次新的播放任务
                 */
                if (mEventHandler.hasMessages(EVENT_PLAY))
                    mEventHandler.removeMessages(EVENT_PLAY);
                mEventHandler.sendEmptyMessage(EVENT_PLAY);
            }
        });


        mForwardbtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // TODO Auto-generated method stub

            }
        });

        OnSeekBarChangeListener osbc1 = new OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                // TODO Auto-generated method stub
                //KJLoger.debug(TAG, "progress: " + progress);
                updateTextViewWithTimeFormat(mCurrPostion, progress);
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
                /**
                 * SeekBar开始seek时停止更新
                 */
                mUIHandler.removeMessages(UI_EVENT_UPDATE_CURRPOSITION);
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
                int iseekPos = seekBar.getProgress();
                /**
                 * SeekBark完成seek时执行seekTo操作并更新界面
                 *
                 */
                mVV.seekTo(iseekPos);
                KJLoger.debug(TAG, "seek to " + iseekPos);
                mUIHandler.sendEmptyMessage(UI_EVENT_UPDATE_CURRPOSITION);
            }
        };
        mProgress.setOnSeekBarChangeListener(osbc1);
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
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        /**
         * 在停止播放前 你可以先记录当前播放的位置,以便以后可以续播
         */
        if (mPlayerStatus == PLAYER_STATUS.PLAYER_PREPARED) {
            mLastPos = mVV.getCurrentPosition();
            mVV.stopPlayback();
        }
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        KJLoger.debug(TAG, "onResume");
        if (null != mWakeLock && (!mWakeLock.isHeld())) {
            mWakeLock.acquire();
        }
        /**
         * 发起一次播放任务,当然您不一定要在这发起
         */
        mEventHandler.sendEmptyMessage(EVENT_PLAY);
    }

    private long mTouchTime;
    private boolean barShow = true;

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        // TODO Auto-generated method stub
        if (event.getAction() == MotionEvent.ACTION_DOWN)
            mTouchTime = System.currentTimeMillis();
        else if (event.getAction() == MotionEvent.ACTION_UP) {
            long time = System.currentTimeMillis() - mTouchTime;
            if (time < 400) {
                updateControlBar(!barShow);
            }
        }

        return true;
    }

    public void updateControlBar(boolean show) {

        if (show) {
            mController.setVisibility(View.VISIBLE);
        } else {
            mController.setVisibility(View.INVISIBLE);
        }
        barShow = show;
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        /**
         * 退出后台事件处理线程
         */
        mHandlerThread.quit();
    }

    @Override
    public boolean onInfo(int what, int extra) {
        // TODO Auto-generated method stub
        switch (what) {
            /**
             * 开始缓冲
             */
            case BVideoView.MEDIA_INFO_BUFFERING_START:
                break;
            /**
             * 结束缓冲
             */
            case BVideoView.MEDIA_INFO_BUFFERING_END:
                break;
            default:
                break;
        }
        return true;
    }

    /**
     * 当前缓冲的百分比， 可以配合onInfo中的开始缓冲和结束缓冲来显示百分比到界面
     */
    @Override
    public void onPlayingBufferCache(int percent) {
        // TODO Auto-generated method stub

    }

    /**
     * 播放出错
     */
    @Override
    public boolean onError(int what, int extra) {
        // TODO Auto-generated method stub
        KJLoger.debug(TAG, "onError");
        synchronized (SYNC_Playing) {
            SYNC_Playing.notify();
        }
        mPlayerStatus = PLAYER_STATUS.PLAYER_IDLE;
        mUIHandler.removeMessages(UI_EVENT_UPDATE_CURRPOSITION);
        return true;
    }

    /**
     * 播放完成
     */
    @Override
    public void onCompletion() {
        // TODO Auto-generated method stub
        KJLoger.debug(TAG, "onCompletion");
        synchronized (SYNC_Playing) {
            SYNC_Playing.notify();
        }
        mPlayerStatus = PLAYER_STATUS.PLAYER_IDLE;
        mUIHandler.removeMessages(UI_EVENT_UPDATE_CURRPOSITION);
    }

    /**
     * 准备播放就绪
     */
    @Override
    public void onPrepared() {
        // TODO Auto-generated method stub
        KJLoger.debug(TAG, "onPrepared");
        mPlayerStatus = PLAYER_STATUS.PLAYER_PREPARED;
        mUIHandler.sendEmptyMessage(UI_EVENT_UPDATE_CURRPOSITION);
    }

    private void initMessageInputToolBox() {
        box.setOnOperationListener(new OnOperationListener() {
            @Override
            public void send(final String content) {

                final AVIMTextMessage msg = new AVIMTextMessage();
                msg.setText(content);
                conv.sendMessage(msg, new AVIMConversationCallback() {

                    @Override
                    public void done(AVIMException e) {
                        if (e == null) {
                            org.kymjs.chat.bean.Message message = new org.kymjs.chat.bean.Message(org.kymjs.chat.bean.Message.MSG_TYPE_TEXT, org.kymjs.chat.bean.Message.MSG_STATE_SUCCESS,
                                    name, "avatar", "Jerry",
                                    "avatar", content, true, true, new Date());
                            datas.add(message);
                            adapter.refresh(datas);

                        } else {
                            org.kymjs.chat.bean.Message message = new org.kymjs.chat.bean.Message(org.kymjs.chat.bean.Message.MSG_TYPE_TEXT, org.kymjs.chat.bean.Message.MSG_STATE_FAIL,
                                    name, "avatar", "Jerry",
                                    "avatar", content, true, true, new Date());
                            datas.add(message);
                            adapter.refresh(datas);
                        }
                    }
                });

            }

            @Override
            public void selectedFace(Faceicon content) {
                org.kymjs.chat.bean.Message message = new org.kymjs.chat.bean.Message(org.kymjs.chat.bean.Message.MSG_TYPE_FACE, org.kymjs.chat.bean.Message.MSG_STATE_SUCCESS,
                        "Tom", "avatar", "Jerry", "avatar", content.getPath(), true, true, new
                        Date());
                datas.add(message);
                adapter.refresh(datas);
                createReplayMsg(message);
            }

            @Override
            public void selectedEmoji(Emojicon emoji) {
                box.getEditTextBox().append(emoji.getValue());
            }

            @Override
            public void selectedBackSpace(Emojicon back) {
                DisplayRules.backspace(box.getEditTextBox());
            }

            @Override
            public void selectedFunction(int index) {
                switch (index) {
                    case 0:
                        goToAlbum();
                        break;
                    case 1:
                        ViewInject.toast("跳转相机");
                        break;
                }
            }
        });

        List<String> faceCagegory = new ArrayList<>();
//        File faceList = FileUtils.getSaveFolder("chat");
        File faceList = new File("");
        if (faceList.isDirectory()) {
            File[] faceFolderArray = faceList.listFiles();
            for (File folder : faceFolderArray) {
                if (!folder.isHidden()) {
                    faceCagegory.add(folder.getAbsolutePath());
                }
            }
        }

        box.setFaceData(faceCagegory);
        mRealListView.setOnTouchListener(getOnTouchListener());
    }

    private void initListView() {

        adapter = new ChatAdapter(this, datas, getOnChatItemClickListener());
        mRealListView.setAdapter(adapter);
    }

    private void createReplayMsg(org.kymjs.chat.bean.Message message) {
        final org.kymjs.chat.bean.Message reMessage = new org.kymjs.chat.bean.Message(message.getType(), org.kymjs.chat.bean.Message.MSG_STATE_SUCCESS, "Tom",
                "avatar", "Jerry", "avatar", message.getType() == org.kymjs.chat.bean.Message.MSG_TYPE_TEXT ? "返回:"
                + message.getContent() : message.getContent(), false,
                true, new Date());
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000 * (new Random().nextInt(3) + 1));
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            datas.add(reMessage);
                            adapter.refresh(datas);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && box.isShow()) {
            box.hideLayout();
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    /**
     * 跳转到选择相册界面
     */
    private void goToAlbum() {
        Intent intent;
        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, "选择图片"),
                    REQUEST_CODE_GETIMAGE_BYSDCARD);
        } else {
            intent = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, "选择图片"),
                    REQUEST_CODE_GETIMAGE_BYSDCARD);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == REQUEST_CODE_GETIMAGE_BYSDCARD) {
            Uri dataUri = data.getData();
            if (dataUri != null) {
                File file = FileUtils.uri2File(aty, dataUri);
                org.kymjs.chat.bean.Message message = new org.kymjs.chat.bean.Message(org.kymjs.chat.bean.Message.MSG_TYPE_PHOTO, org.kymjs.chat.bean.Message.MSG_STATE_SUCCESS,
                        "Tom", "avatar", "Jerry",
                        "avatar", file.getAbsolutePath(), true, true, new Date());
                datas.add(message);
                adapter.refresh(datas);
            }
        }
    }

    /**
     * 若软键盘或表情键盘弹起，点击上端空白处应该隐藏输入法键盘
     *
     * @return 会隐藏输入法键盘的触摸事件监听器
     */
    private View.OnTouchListener getOnTouchListener() {
        return new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                box.hideLayout();
                box.hideKeyboard(aty);
                return false;
            }
        };
    }

    /**
     * @return 聊天列表内存点击事件监听器
     */
    private ChatActivity.OnChatItemClickListener getOnChatItemClickListener() {
        return new ChatActivity.OnChatItemClickListener() {
            @Override
            public void onPhotoClick(int position) {
                KJLoger.debug(datas.get(position).getContent() + "点击图片的");
                ViewInject.toast(aty, datas.get(position).getContent() + "点击图片的");
            }

            @Override
            public void onTextClick(int position) {
            }

            @Override
            public void onFaceClick(int position) {
            }
        };
    }

}
