package com.wanshi.tool.utils;

import android.content.Context;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 打开或关闭软键盘
 *
 * @author zhy
 */
public class KeyBoardUtil {
//    /**
//     * 打卡软键盘
//     *
//     * @param mEditText 输入框
//     * @param mContext  上下文
//     */
//    public static void openKeybord(EditText mEditText, Context mContext) {
//        InputMethodManager imm = (InputMethodManager) mContext
//                .getSystemService(Context.INPUT_METHOD_SERVICE);
//        imm.showSoftInput(mEditText, InputMethodManager.RESULT_SHOWN);
//        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,
//                InputMethodManager.HIDE_IMPLICIT_ONLY);
//    }
//
//    /**
//     * 关闭软键盘
//     *
//     * @param mEditText 输入框
//     * @param mContext  上下文
//     */
//    public static void closeKeybord(EditText mEditText, Context mContext) {
//        InputMethodManager imm = (InputMethodManager) mContext
//                .getSystemService(Context.INPUT_METHOD_SERVICE);
//
//        imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
//    }

    public static void openKeybord(final EditText mEditText, final Context mContext) {

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                InputMethodManager inputManager = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.showSoftInput(mEditText, InputMethodManager.RESULT_UNCHANGED_SHOWN);
            }
        }, 300);
    }

    public static void closeKeybord(EditText mEditText, Context mContext) {
        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mEditText.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
    }
}
