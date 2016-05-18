package com.wanshi.app.page.main;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wanshi.app.R;
import com.wanshi.app.page.base.BaseFragment;
public class MeFragment extends BaseFragment {
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_me, null, false);
        mContext = getActivity();
        ((TextView) view.findViewById(R.id.textTitleBar)).setText("我");

        return view;
    }


}
