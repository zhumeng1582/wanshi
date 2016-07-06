package com.wanshi.app.page.videolive;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.RelativeLayout;

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
import com.wanshi.app.R;
import com.wanshi.app.cache.SharePreferenceUtil;
import com.wanshi.tool.utils.logger.Logger;

import org.kymjs.chat.ChatActivity;
import org.kymjs.chat.OnOperationListener;
import org.kymjs.chat.adapter.ChatAdapter;
import org.kymjs.chat.bean.Emojicon;
import org.kymjs.chat.bean.Faceicon;
import org.kymjs.chat.emoji.DisplayRules;
import org.kymjs.chat.widget.KJChatKeyboard;
import org.kymjs.kjframe.KJActivity;
import org.kymjs.kjframe.ui.BindView;
import org.kymjs.kjframe.ui.ViewInject;
import org.kymjs.kjframe.utils.FileUtils;
import org.kymjs.kjframe.utils.KJLoger;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * 项目名称：蜂鸟金融
 * 类描述：
 * 创建人：zhangchao
 * 创建时间：2016-07-05 15:40
 * 修改人：zhangchao
 * 修改时间：2016-07-05 15:40
 * 修改备注：
 */
public class ConversationActivity extends KJActivity {
    @BindView(id = R.id.llConversation)
    private RelativeLayout llConversation;
    @BindView(id = R.id.chat_msg_input_box)
    private KJChatKeyboard keyboard;
    @BindView(id = R.id.chat_listview)
    private ListView mRealListView;
    private ChatAdapter adapter;
    private String avatarFrom = "http://www.iconpng.com/png/possible_android_4.5/chrome.png";
    private String avatarTo = "http://www.iconpng.com/png/webdev-seo/chrome3.png";
    public static final int REQUEST_CODE_GETIMAGE_BYSDCARD = 0x1;
    private AVIMConversation conversation;
    private AVIMClient me;
    private String name ="游客";
    private String conversationId;

    @Override
    public void setRootView() {
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
       
        setContentView(R.layout.fragment_live_conversation);
       
    }

    @Override
    public void initWidget() {

        name = SharePreferenceUtil.getUserName(this);
        if (TextUtils.isEmpty(name)) {
            name = "游客" + System.currentTimeMillis();
            SharePreferenceUtil.setUserName(this, name);
        }

        conversationId = "577b9ba52e958a0054902464";//this.getIntent().getStringExtra("conversationId");
        if (!TextUtils.isEmpty(conversationId)) {
            joinConversation();
        }

        mRealListView.setSelector(android.R.color.transparent);
        initMessageInputToolBox();
        initListView();
        keyboard.hideKeyboard(this);


    }


    private void joinConversation() {
        me = AVIMClient.getInstance(name);
        me.open(new AVIMClientCallback() {

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
                                Logger.d("加入成功聊天室,id:" + conversationId);
                                conversation.queryMessages(10, new AVIMMessagesQueryCallback() {
                                    @Override
                                    public void done(List<AVIMMessage> messages, AVIMException e) {
                                        if (e == null) {
                                            for (AVIMMessage item : messages) {
                                                org.kymjs.chat.bean.Message message = new org.kymjs.chat.bean.Message(org.kymjs.chat.bean.Message.MSG_TYPE_TEXT,
                                                        org.kymjs.chat.bean.Message.MSG_STATE_SUCCESS, item.getFrom(), avatarFrom, item.getFrom(), avatarTo,
                                                        ((AVIMTextMessage) item).getText(), name.equals(item.getFrom()), true, new Date(item.getTimestamp()));
                                                adapter.add(message);
                                            }
                                            //滑动到底部
                                            mRealListView.setSelection(adapter.getCount() - 1);
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
    private void initMessageInputToolBox() {
        keyboard.setOnOperationListener(new OnOperationListener() {
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
                                    name, avatarFrom, msg.getFrom(), avatarTo, content, true, true, new Date());


                        } else {
                            message = new org.kymjs.chat.bean.Message(org.kymjs.chat.bean.Message.MSG_TYPE_TEXT,
                                    org.kymjs.chat.bean.Message.MSG_STATE_FAIL,
                                    name, avatarFrom, msg.getFrom(), avatarTo, content, true, true, new Date());
                        }
                        adapter.add(message);
                    }
                });

            }

            @Override
            public void selectedFace(Faceicon content) {
                org.kymjs.chat.bean.Message message = new org.kymjs.chat.bean.Message(org.kymjs.chat.bean.Message.MSG_TYPE_FACE, org.kymjs.chat.bean.Message.MSG_STATE_SUCCESS,
                        name, "avatar", "Jerry", "avatar", content.getPath(), true, true, new
                        Date());
                adapter.add(message);
                createReplayMsg(message);
            }

            @Override
            public void selectedEmoji(Emojicon emoji) {
                keyboard.getEditTextBox().append(emoji.getValue());
            }

            @Override
            public void selectedBackSpace(Emojicon back) {
                DisplayRules.backspace(keyboard.getEditTextBox());
            }

            @Override
            public void selectedFunction(int index) {
                switch (index) {
                    case 0:
//                        goToAlbum();
                        ViewInject.toast("跳转相册");
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

        keyboard.setFaceData(faceCagegory);
        mRealListView.setOnTouchListener(getOnTouchListener());
    }

    private void initListView() {

        adapter = new ChatAdapter(this, getOnChatItemClickListener());
        mRealListView.setAdapter(adapter);
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
                keyboard.hideLayout();
                keyboard.hideKeyboard(ConversationActivity.this);
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
                KJLoger.debug(adapter.getDatas().get(position).getContent() + "点击图片的");
                ViewInject.toast(ConversationActivity.this, adapter.getDatas().get(position).getContent() + "点击图片的");
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
                    message1 = new org.kymjs.chat.bean.Message(org.kymjs.chat.bean.Message.MSG_TYPE_TEXT,org.kymjs.chat.bean.Message.MSG_STATE_SUCCESS, name, avatarFrom, message.getFrom(), avatarTo, textMsg.getText(), false, true, new Date());
                    break;
                case ImageMessageType:
                    AVIMImageMessage imageMsg = (AVIMImageMessage)message;
                    message1 = new org.kymjs.chat.bean.Message(org.kymjs.chat.bean.Message.MSG_TYPE_PHOTO,org.kymjs.chat.bean.Message.MSG_STATE_SUCCESS, name, avatarFrom, message.getFrom(), avatarTo, imageMsg.getFileUrl(), false, true, new Date());
                    break;
                default:
                    message1 = new org.kymjs.chat.bean.Message(org.kymjs.chat.bean.Message.MSG_TYPE_TEXT,org.kymjs.chat.bean.Message.MSG_STATE_SUCCESS, name, avatarFrom, message.getFrom(), avatarTo, "数据类型错误", false, true, new Date());
                    break;
            }

            adapter.add(message1);
        }
        public void onMessageReceipt(AVIMTypedMessage message,AVIMConversation conversation,AVIMClient client){

        }
    }


    private void createReplayMsg(org.kymjs.chat.bean.Message message) {
        final org.kymjs.chat.bean.Message reMessage = new org.kymjs.chat.bean.Message(message.getType(), org.kymjs.chat.bean.Message.MSG_STATE_SUCCESS, name,
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
                            adapter.add(reMessage);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
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
    public void onDestroy() {
        super.onDestroy();
        if(conversation!= null){
            conversation.quit(new AVIMConversationCallback() {
                @Override
                public void done(AVIMException e) {
                    Logger.d( "退出聊天室");
                }
            });
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == REQUEST_CODE_GETIMAGE_BYSDCARD) {
            Uri dataUri = data.getData();
            if (dataUri != null) {
                File file = FileUtils.uri2File(this, dataUri);
                org.kymjs.chat.bean.Message message = new org.kymjs.chat.bean.Message(org.kymjs.chat.bean.Message.MSG_TYPE_PHOTO, org.kymjs.chat.bean.Message.MSG_STATE_SUCCESS,
                        name, "avatar", "Jerry",
                        "avatar", file.getAbsolutePath(), true, true, new Date());
                adapter.add(message);
            }
        }
    }
}
