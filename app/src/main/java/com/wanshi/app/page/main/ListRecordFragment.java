package com.wanshi.app.page.main;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.GetCallback;
import com.wanshi.app.R;
import com.wanshi.app.adapter.ListLiveAdapter;
import com.wanshi.app.bean.Room;
import com.wanshi.app.page.base.BaseFragment;
import com.wanshi.app.page.video.RecordVideoViewPlayingActivity;
import com.wanshi.app.widget.emptyview.EmptyRecyclerView;
import com.wanshi.app.widget.emptyview.EmptyView;
import com.wanshi.app.widget.refresh.BGAMoocStyleRefreshViewHolder;
import com.wanshi.app.widget.refresh.DividerGridItemDecoration;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

import cn.bingoogolapple.refreshlayout.BGARefreshLayout;

public class ListRecordFragment extends BaseFragment implements BGARefreshLayout.BGARefreshLayoutDelegate {

    private EmptyRecyclerView recyclerView;
    private ListLiveAdapter adapter;
    private BGARefreshLayout mSwipeRefreshWidget;
    private EmptyView empty;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_live, null, false);
        mContext = getActivity();
        ((TextView) view.findViewById(R.id.textTitleBar)).setText("精彩回放");
        mSwipeRefreshWidget = (BGARefreshLayout) view.findViewById(R.id.swipe_refresh_widget);
        recyclerView = (EmptyRecyclerView) view.findViewById(R.id.gridviewLive);
        empty = (EmptyView) view.findViewById(R.id.empty);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(mContext, 2));
        recyclerView.addItemDecoration(new DividerGridItemDecoration(mContext));
        adapter = new ListLiveAdapter(getActivity());
        recyclerView.setAdapter(adapter);
        recyclerView.setEmptyView(mSwipeRefreshWidget,empty);
        empty.showLoading();
        adapter.setOnItemClickListener(new ListLiveAdapter.OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(View view, Room data) {
                try {
                    String url = URLDecoder.decode((data).getUrlStreamAddr(), "utf-8");

                    Intent intent = new Intent(getActivity(), RecordVideoViewPlayingActivity.class);
                    intent.setData(Uri.parse(url));
                    intent.putExtra("conversationId",data.getConversationId());
                    startActivity(intent);

                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        });
        initRefreshLayout(mSwipeRefreshWidget);
        onBGARefreshLayoutBeginRefreshing(mSwipeRefreshWidget);

        return view;
    }

    private void initRefreshLayout(BGARefreshLayout refreshLayout) {
        // 为BGARefreshLayout设置代理
        refreshLayout.setDelegate(this);
        // 设置下拉刷新和上拉加载更多的风格     参数1：应用程序上下文，参数2：是否具有上拉加载更多功能
        BGAMoocStyleRefreshViewHolder refreshViewHolder = new BGAMoocStyleRefreshViewHolder(mContext, true);
        refreshViewHolder.setOriginalImage(R.mipmap.ic_launcher);
        refreshViewHolder.setUltimateColor(R.color.colorAccent);
        // 设置下拉刷新和上拉加载更多的风格
        refreshLayout.setRefreshViewHolder(refreshViewHolder);


//        // 为了增加下拉刷新头部和加载更多的通用性，提供了以下可选配置选项  -------------START
//        // 设置正在加载更多时不显示加载更多控件
//        // mRefreshLayout.setIsShowLoadingMoreView(false);
//        // 设置正在加载更多时的文本
//        refreshViewHolder.setLoadingMoreText(loadingMoreText);
//        // 设置整个加载更多控件的背景颜色资源id
//        refreshViewHolder.setLoadMoreBackgroundColorRes(loadMoreBackgroundColorRes);
//        // 设置整个加载更多控件的背景drawable资源id
//        refreshViewHolder.setLoadMoreBackgroundDrawableRes(loadMoreBackgroundDrawableRes);
//        // 设置下拉刷新控件的背景颜色资源id
//        refreshViewHolder.setRefreshViewBackgroundColorRes(refreshViewBackgroundColorRes);
//        // 设置下拉刷新控件的背景drawable资源id
//        refreshViewHolder.setRefreshViewBackgroundDrawableRes(refreshViewBackgroundDrawableRes);
//        // 设置自定义头部视图（也可以不用设置）     参数1：自定义头部视图（例如广告位）， 参数2：上拉加载更多是否可用
//        mRefreshLayout.setCustomHeaderView(mBanner, false);
        // 可选配置  -------------END
    }





    @Override
    public void onBGARefreshLayoutBeginRefreshing(BGARefreshLayout bgaRefreshLayout) {
        AVQuery<AVObject> query = new AVQuery<>("Room");
        query.setLimit(10); // 限制最多10个结果
        query.orderByDescending("createdAt");
        empty.showLoading();
        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {
                adapter.clear();
                if(e != null){
                    mSwipeRefreshWidget.endRefreshing();
                    empty.showEmpty();
                }
                for (AVObject item : list) {
                    final Room room = new Room();
                    room.setId(item.getObjectId());
                    room.setUrlStreamAddr(item.getString("stream_addr"));
                    AVObject avObject = item.getAVObject("conversation");
                    if(avObject ==null){
                        logd( "此房间没有聊天室");
                    }else{
                        room.setConversationId(item.getAVObject("conversation").getObjectId());
                        logd( "此房间有聊天室,id:"+item.getAVObject("conversation").getObjectId());
                    }
                    AVQuery<AVUser> query = AVUser.getQuery();
                    query.getInBackground(item.getAVObject("anchor").getObjectId(), new GetCallback<AVUser>() {
                        @Override
                        public void done(AVUser avUser, AVException e) {
                            if(e != null){
                                mSwipeRefreshWidget.endRefreshing();
                                empty.showEmpty();
                            }
                            room.setUrlRoomIcon(avUser.getString("portrait"));
                            room.setRoomName(avUser.getString("nick"));
                            adapter.add(room);
                            mSwipeRefreshWidget.endRefreshing();
                            empty.showEmpty();
                        }
                    });
                }

            }
        });

    }


    @Override
    public boolean onBGARefreshLayoutBeginLoadingMore(BGARefreshLayout bgaRefreshLayout) {
        AVQuery<AVObject> query = new AVQuery<AVObject>("Room");
        query.setSkip(adapter.getItemCount()); // 忽略前10个
        query.orderByDescending("updatedAt");
        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {
                for (AVObject item : list) {
                    logd("Room:" + item.toJSONObject().toString());
                    final Room room = new Room();
                    room.setId(item.getObjectId());
                    room.setUrlStreamAddr(item.getString("stream_addr"));
                    AVQuery<AVUser> query = AVUser.getQuery();
                    query.getInBackground(item.getAVObject("anchor").getObjectId(), new GetCallback<AVUser>() {
                        @Override
                        public void done(AVUser avUser, AVException e) {
                            logd("User:" + avUser.toString());
                            logd("portrait:" + avUser.getString("portrait"));
                            logd("nick:" + avUser.getString("nick"));
                            room.setUrlRoomIcon(avUser.getString("portrait"));
                            room.setRoomName(avUser.getString("nick"));
                            adapter.add(room);
                        }
                    });
                }
                empty.showEmpty();
                mSwipeRefreshWidget.endLoadingMore();
            }
        });
        return true;
    }
}
