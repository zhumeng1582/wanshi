package com.wanshi.app.page.base;

import android.app.Activity;
import android.support.v4.app.Fragment;

import com.orhanobut.logger.Logger;

/**
 * User: Geek_Soledad(msdx.android@qq.com)
 * Date: 2014-08-27
 * Time: 09:01
 * FIXME
 */
public class BaseFragment extends Fragment {
    protected String TAG =  getClass().getSimpleName();
    protected Activity mContext = getActivity();
    private String title;
    private int iconId;
    protected void logd(String message){
        Logger.d(TAG, message);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getIconId() {
        return iconId;
    }

    public void setIconId(int iconId) {
        this.iconId = iconId;
    }
}
