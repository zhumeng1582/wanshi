package com.wanshi.app.page.base;

import android.app.Activity;

import org.kymjs.kjframe.ui.KJFragment;

/**
 * User: Geek_Soledad(msdx.android@qq.com)
 * Date: 2014-08-27
 * Time: 09:01
 * FIXME
 */
public abstract class BaseFragment extends KJFragment {
    protected String TAG =  getClass().getSimpleName();
    protected Activity mContext = getActivity();
    private String title;
    private int iconId;

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
