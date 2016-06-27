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
import com.wanshi.app.page.video.LiveVideoViewPlayingActivity;
import com.wanshi.app.widget.emptyview.EmptyRecyclerView;
import com.wanshi.app.widget.emptyview.EmptyView;
import com.wanshi.app.widget.refresh.BGAMoocStyleRefreshViewHolder;
import com.wanshi.app.widget.refresh.DividerGridItemDecoration;
import com.wanshi.tool.utils.logger.Logger;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

import cn.bingoogolapple.refreshlayout.BGARefreshLayout;
public class ListLiveFragment extends BaseFragment implements BGARefreshLayout.BGARefreshLayoutDelegate {
    private EmptyRecyclerView recyclerView;
    private ListLiveAdapter adapter;
    private BGARefreshLayout mSwipeRefreshWidget;
    private EmptyView empty;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_live, null, false);
        mContext = getActivity();
        ((TextView) view.findViewById(R.id.textTitleBar)).setText("正在直播");
        mSwipeRefreshWidget = (BGARefreshLayout) view.findViewById(R.id.swipe_refresh_widget);
        recyclerView = (EmptyRecyclerView) view.findViewById(R.id.gridviewLive);
        empty = (EmptyView) view.findViewById(R.id.empty);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(mContext, 2));
        recyclerView.addItemDecoration(new DividerGridItemDecoration(mContext));

        adapter = new ListLiveAdapter(mContext);

        recyclerView.setEmptyView(mSwipeRefreshWidget,empty);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(new ListLiveAdapter.OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(View view, Room data) {
                try {
                    String url = URLDecoder.decode((data).getUrlStreamAddr(), "utf-8");

                    Intent intent = new Intent(getActivity(), LiveVideoViewPlayingActivity.class);
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

                mSwipeRefreshWidget.endRefreshing();
                if(e !=null){
                    empty.showError();
                    return;
                }else{
                    empty.showEmpty();
                }
                if((list==null)||(list.isEmpty())){
                    return;
                }
                adapter.clear();

                for (AVObject item : list) {
                    final Room room = new Room();
                    room.setId(item.getObjectId());
                    room.setUrlStreamAddr(item.getString("stream_addr"));
                    AVObject avObject = item.getAVObject("conversation");
                    if (avObject == null) {
                        Logger.d("此房间没有聊天室");
                    } else {
                        room.setConversationId(item.getAVObject("conversation").getObjectId());
                        Logger.d("此房间有聊天室,id:" + item.getAVObject("conversation").getObjectId());
                    }
                    AVQuery<AVUser> query = AVUser.getQuery();
                    query.getInBackground(item.getAVObject("anchor").getObjectId(), new GetCallback<AVUser>() {
                        @Override
                        public void done(AVUser avUser, AVException e) {
                            if((e != null)||(avUser==null)){
                                return;
                            }
                            room.setUrlRoomIcon(avUser.getString("portrait"));
                            room.setRoomName(avUser.getString("nick"));
                            adapter.add(room);
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
                mSwipeRefreshWidget.endRefreshing();
                if(e !=null){
                    empty.showError();
                    return;
                }else{
                    empty.showEmpty();
                }
                if((list==null)||(list.isEmpty())){
                    return;
                }
                for (AVObject item : list) {
                    Logger.d("Room:" + item.toJSONObject().toString());
                    final Room room = new Room();
                    room.setId(item.getObjectId());
                    room.setUrlStreamAddr(item.getString("stream_addr"));
                    AVQuery<AVUser> query = AVUser.getQuery();
                    query.getInBackground(item.getAVObject("anchor").getObjectId(), new GetCallback<AVUser>() {
                        @Override
                        public void done(AVUser avUser, AVException e) {
                            if((e != null)||(avUser==null)){
                                return;
                            }
                            room.setUrlRoomIcon(avUser.getString("portrait"));
                            room.setRoomName(avUser.getString("nick"));
                            adapter.add(room);

                        }
                    });
                }

            }
        });
        return true;
    }
}
