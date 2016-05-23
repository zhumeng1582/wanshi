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
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.avos.avoscloud.im.v2.AVIMClient;
import com.avos.avoscloud.im.v2.AVIMConversation;
import com.avos.avoscloud.im.v2.AVIMException;
import com.avos.avoscloud.im.v2.AVIMMessage;
import com.avos.avoscloud.im.v2.AVIMMessageManager;
import com.avos.avoscloud.im.v2.AVIMReservedMessageType;
import com.avos.avoscloud.im.v2.AVIMTypedMessage;
import com.avos.avoscloud.im.v2.AVIMTypedMessageHandler;
import com.avos.avoscloud.im.v2.callback.AVIMClientCallback;
import com.avos.avoscloud.im.v2.callback.AVIMConversationCallback;
import com.avos.avoscloud.im.v2.callback.AVIMMessagesQueryCallback;
import com.avos.avoscloud.im.v2.messages.AVIMImageMessage;
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

public class LiveVideoViewPlayingActivity extends KJActivity implements OnPreparedListener, OnCompletionListener,
        OnErrorListener, OnInfoListener, OnPlayingBufferCacheListener {

    private final String TAG = "LiveVideoViewPlayingActivity";

    private String AK = "381715e304f04aceb2d7eb274cad3e27";   //请录入您的AK !!!

    private String mVideoSource = null;
    private ImageButton mPlaybtn = null;
    private ImageButton btnFullScreen = null;
    private LinearLayout mController = null;
    private LinearLayout llConversation = null;
    private TextView mCurrPostion = null;
    public static final int REQUEST_CODE_GETIMAGE_BYSDCARD = 0x1;

    private KJChatKeyboard box;
    private ListView mRealListView;

    List<org.kymjs.chat.bean.Message> datas = new ArrayList<>();
    private ChatAdapter adapter;

    private String avatarFrom = "http://www.iconpng.com/png/possible_android_4.5/chrome.png";
    private String avatarTo = "http://www.iconpng.com/png/webdev-seo/chrome3.png";

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
    private String conversationId;

    private final int EVENT_PLAY = 0;
    private final int UI_EVENT_UPDATE_CURRPOSITION = 1;
    private AVIMConversation conversation;
    private AVIMClient tom;
    private String name ="游客";

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
                                KJLoger.debug(TAG, "wait player status to idle");
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
    public void setRootView() {
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_playing_live);
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
            joinConversation();
        }else{
            findViewById(R.id.llConversation).setVisibility(View.GONE);
            findViewById(R.id.btnFullScreen).setEnabled(false);
        }

        initUI();

        /**
         * 开启后台事件处理线程
         */
        mHandlerThread = new HandlerThread("event handler thread", Process.THREAD_PRIORITY_BACKGROUND);
        mHandlerThread.start();
        mEventHandler = new EventHandler(mHandlerThread.getLooper());

        box = (KJChatKeyboard) findViewById(R.id.chat_msg_input_box);
        mRealListView = (ListView) findViewById(R.id.chat_listview);

        mRealListView.setSelector(android.R.color.transparent);
        initMessageInputToolBox();
        initListView();
    }

    private void joinConversation() {
        tom = AVIMClient.getInstance(name);
        tom.open(new AVIMClientCallback() {

            @Override
            public void done(AVIMClient client, AVIMException e) {
                if (e == null) {
                    //登录成功
                    conversation = client.getConversation(conversationId);
                    conversation.join(new AVIMConversationCallback() {
                        @Override
                        public void done(AVIMException e) {
                            if (e == null) {
                                //加入成功
                                KJLoger.log(TAG, "加入成功聊天室,id:" + conversationId);
                                conversation.queryMessages(10, new AVIMMessagesQueryCallback() {
                                    @Override
                                    public void done(List<AVIMMessage> messages, AVIMException e) {
                                        if (e == null) {
                                            for (AVIMMessage item : messages) {
                                                org.kymjs.chat.bean.Message message = new org.kymjs.chat.bean.Message(org.kymjs.chat.bean.Message.MSG_TYPE_TEXT,
                                                        org.kymjs.chat.bean.Message.MSG_STATE_SUCCESS, item.getFrom(), avatarFrom, "Jerry", avatarTo,
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
        AVIMMessageManager.registerMessageHandler(AVIMTextMessage.class,new CustomAVIMMessageHandler());

    }


    /**
     * 初始化界面
     */
    private void initUI() {
        mPlaybtn = (ImageButton) findViewById(R.id.btnPlay);
        btnFullScreen = (ImageButton) findViewById(R.id.btnFullScreen);
        mController = (LinearLayout) findViewById(R.id.controlbar);
        llConversation = (LinearLayout) findViewById(R.id.llConversation);
        mCurrPostion = (TextView) findViewById(R.id.textCurrentTime);
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
                if (mVV.isPlaying()) {
                    mPlaybtn.setImageResource(R.drawable.btn_style_play);
                    //暂停播放
                    mVV.pause();
                } else {
                    mPlaybtn.setImageResource(R.drawable.btn_style_pause);
                    //继续播放
                    mVV.resume();
                }

            }
        });
        btnFullScreen.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(llConversation.getVisibility() == View.VISIBLE){
                    llConversation.setVisibility(View.GONE);
                }else{
                    llConversation.setVisibility(View.VISIBLE);
                }
            }
        });
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
        super.onPause();
        //在停止播放前 你可以先记录当前播放的位置,以便以后可以续播
        if (mPlayerStatus == PLAYER_STATUS.PLAYER_PREPARED) {
            mLastPos = mVV.getCurrentPosition();
            mVV.stopPlayback();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        KJLoger.debug(TAG, "onResume");
        if (null != mWakeLock && (!mWakeLock.isHeld())) {
            mWakeLock.acquire();
        }
        //发起一次播放任务,当然您不一定要在这发起
        mEventHandler.sendEmptyMessage(EVENT_PLAY);
    }

    private long mTouchTime;
    private boolean barShow = true;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
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
        //退出后台事件处理线程
        mHandlerThread.quit();
        conversation.quit(new AVIMConversationCallback() {
            @Override
            public void done(AVIMException e) {
                KJLoger.debug(TAG, "退出聊天室");
            }
        });
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
        KJLoger.debug(TAG, "onError");
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
        KJLoger.debug(TAG, "onCompletion");
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
                conversation.sendMessage(msg, new AVIMConversationCallback() {

                    @Override
                    public void done(AVIMException e) {
                        org.kymjs.chat.bean.Message message;
                        if (e == null) {

                            message = new org.kymjs.chat.bean.Message(org.kymjs.chat.bean.Message.MSG_TYPE_TEXT,
                                    org.kymjs.chat.bean.Message.MSG_STATE_SUCCESS,
                                    name, avatarFrom, "Jerry", avatarTo, content, true, true, new Date());


                        } else {
                            message = new org.kymjs.chat.bean.Message(org.kymjs.chat.bean.Message.MSG_TYPE_TEXT,
                                    org.kymjs.chat.bean.Message.MSG_STATE_FAIL,
                                    name, avatarFrom, "Jerry", avatarTo, content, true, true, new Date());
                        }
                        datas.add(message);
                        adapter.refresh(datas);
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

    public  class CustomAVIMMessageHandler extends AVIMTypedMessageHandler<AVIMTypedMessage> {
        //接收到消息后的处理逻辑

        @Override
        public void onMessage(AVIMTypedMessage message,AVIMConversation conversation,AVIMClient client){

            org.kymjs.chat.bean.Message message1;
            // 请按自己需求改写
            switch(AVIMReservedMessageType.getAVIMReservedMessageType(message.getMessageType())) {
                case TextMessageType:
                    AVIMTextMessage textMsg = (AVIMTextMessage)message;
                    message1 = new org.kymjs.chat.bean.Message(org.kymjs.chat.bean.Message.MSG_TYPE_TEXT,org.kymjs.chat.bean.Message.MSG_STATE_SUCCESS, name, avatarFrom, "Jerry", avatarTo, textMsg.getText(), false, true, new Date());
                    break;
                case ImageMessageType:
                    AVIMImageMessage imageMsg = (AVIMImageMessage)message;
                    message1 = new org.kymjs.chat.bean.Message(org.kymjs.chat.bean.Message.MSG_TYPE_PHOTO,org.kymjs.chat.bean.Message.MSG_STATE_SUCCESS, name, avatarFrom, "Jerry", avatarTo, imageMsg.getFileUrl(), false, true, new Date());
                    break;
                default:
                    message1 = new org.kymjs.chat.bean.Message(org.kymjs.chat.bean.Message.MSG_TYPE_TEXT,org.kymjs.chat.bean.Message.MSG_STATE_SUCCESS, name, avatarFrom, "Jerry", avatarTo, "数据类型错误", false, true, new Date());
                    break;
            }

            datas.add(message1);
            adapter.refresh(datas);
        }
        public void onMessageReceipt(AVIMTypedMessage message,AVIMConversation conversation,AVIMClient client){

        }
    }


}
