package com.wanshi.app.page.main;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.githang.viewpagerindicator.IconPagerAdapter;
import com.githang.viewpagerindicator.IconTabPageIndicator;
import com.wanshi.app.R;
import com.wanshi.app.page.base.BaseFragment;
import com.wanshi.tool.utils.StatusBarUtil;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends FragmentActivity {

    private ViewPager mViewPager;
    private IconTabPageIndicator mIndicator;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StatusBarUtil.setTranslucent(this);
        initViews();
    }

    private void initViews() {
        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mIndicator = (IconTabPageIndicator) findViewById(R.id.indicator);
        List<BaseFragment> fragments = initFragments();
        FragmentAdapter adapter = new FragmentAdapter(fragments, getSupportFragmentManager());
        mViewPager.setAdapter(adapter);
        mIndicator.setViewPager(mViewPager);
    }

    private List<BaseFragment> initFragments() {
        List<BaseFragment> fragments = new ArrayList<BaseFragment>();

        ListLiveFragment liveFragment = new ListLiveFragment();
        liveFragment.setTitle("直播");
        liveFragment.setIconId(R.drawable.tab_live_selector);
        fragments.add(liveFragment);

        ListRecordFragment recordFragment = new ListRecordFragment();
        recordFragment.setTitle("录播");
        recordFragment.setIconId(R.drawable.tab_record_selector);
        fragments.add(recordFragment);

        MeFragment meFragment = new MeFragment();
        meFragment.setTitle("我");
        meFragment.setIconId(R.drawable.tab_user_selector);
        fragments.add(meFragment);

        return fragments;
    }

    class FragmentAdapter extends FragmentPagerAdapter implements IconPagerAdapter {
        private List<BaseFragment> mFragments;

        public FragmentAdapter(List<BaseFragment> fragments, FragmentManager fm) {
            super(fm);
            mFragments = fragments;
        }

        @Override
        public Fragment getItem(int i) {
            return mFragments.get(i);
        }

        @Override
        public int getIconResId(int index) {
            return mFragments.get(index).getIconId();
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragments.get(position).getTitle();
        }
    }

}