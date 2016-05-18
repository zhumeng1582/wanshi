package com.wanshi.tool.utils;

import android.view.View;
import android.view.ViewGroup;

/**
 * 项目名称：蜂鸟金融
 * 类描述：
 * 创建人：Memory
 * 创建时间：2016/2/29 18:58
 * 修改人：Memory
 * 修改时间：2016/2/29 18:58
 * 修改备注：
 */
public class ViewGroupEnabledUtil {

    /**
     * 方法setViewGroupEnabled  的功能描述：ViewGroup的子控件是否能够enable
     * @param
     * @param
     */

    public static void setViewGroupEnabled(ViewGroup layout, boolean enable) {
        layout.setEnabled(enable);
        for (int i = 0; i < layout.getChildCount(); i++) {
            View child = layout.getChildAt(i);
            if (child instanceof ViewGroup) {
                setViewGroupEnabled((ViewGroup) child, enable);
            } else {
                child.setEnabled(enable);
            }
        }
    }
}
