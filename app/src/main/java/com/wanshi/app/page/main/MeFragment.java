package com.wanshi.app.page.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.avos.avoscloud.feedback.FeedbackAgent;
import com.wanshi.app.R;
import com.wanshi.app.config.Contants;
import com.wanshi.app.page.base.BaseFragment;
import com.wanshi.app.page.me.QQLoginActivity;

public class MeFragment extends BaseFragment implements View.OnClickListener {
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_me, null, false);
        mContext = getActivity();
        initView(view);
        return view;
    }
    private void initView(View view) {
        ((TextView) view.findViewById(R.id.textTitleBar)).setText("我");
        view.findViewById(R.id.llIndividualAccount).setOnClickListener(this);
        view.findViewById(R.id.llHelp).setOnClickListener(this);
        view.findViewById(R.id.llAboutUs).setOnClickListener(this);
        view.findViewById(R.id.llFeedback).setOnClickListener(this);
        view.findViewById(R.id.llSetting).setOnClickListener(this);
        view.findViewById(R.id.llCustomService).setOnClickListener(this);

    }


    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.llIndividualAccount){
           Intent intent = new Intent(mContext, QQLoginActivity.class);
            startActivityForResult(intent, Contants.requestDefault);
        }else if(v.getId() == R.id.llFeedback){
            FeedbackAgent agent = new FeedbackAgent(mContext);
            agent.startDefaultThreadActivity();
        }
    }

}
