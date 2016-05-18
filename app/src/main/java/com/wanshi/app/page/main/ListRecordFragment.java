package com.wanshi.app.page.main;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
import com.avos.avoscloud.okhttp.OkHttpClient;
import com.avos.avoscloud.okhttp.Request;
import com.avos.avoscloud.okhttp.Response;
import com.orhanobut.logger.Logger;
import com.wanshi.app.R;
import com.wanshi.app.adapter.ListLiveAdapter;
import com.wanshi.app.bean.Room;
import com.wanshi.app.page.base.BaseFragment;
import com.wanshi.app.page.video.RecordVideoViewPlayingActivity;
import com.wanshi.app.widget.refresh.BGAMoocStyleRefreshViewHolder;
import com.wanshi.app.widget.refresh.DividerGridItemDecoration;

import org.kymjs.kjframe.utils.KJLoger;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

import cn.bingoogolapple.refreshlayout.BGARefreshLayout;

/**
 * User: Geek_Soledad(msdx.android@qq.com)
 * Date: 2014-08-27
 * Time: 09:01
 * FIXME
 */
public class ListRecordFragment extends BaseFragment implements BGARefreshLayout.BGARefreshLayoutDelegate {

    private final static String TAG = ListRecordFragment.class.getSimpleName();
    private static Activity mContext;
    private TextView textTitleBar;
    private RecyclerView recyclerView;
    private ListLiveAdapter adapter;
    private BGARefreshLayout mSwipeRefreshWidget;
    private final OkHttpClient client = new OkHttpClient();
    private String url ="http://180.76.177.27:8899/update";


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_live, null, false);
        mContext = getActivity();
        ((TextView) view.findViewById(R.id.textTitleBar)).setText("精彩回放");
        mSwipeRefreshWidget = (BGARefreshLayout) view.findViewById(R.id.swipe_refresh_widget);
        recyclerView = (RecyclerView) view.findViewById(R.id.gridviewLive);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(mContext, 2));
        recyclerView.addItemDecoration(new DividerGridItemDecoration(mContext));

        adapter = new ListLiveAdapter(getActivity());
        recyclerView.setAdapter(adapter);




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



    public void refreshing() throws Exception {
        Request request = new Request.Builder().url(url).build();
        Response response = client.newCall(request).execute();
        if(response.isSuccessful()){
            AVQuery<AVObject> query = new AVQuery<>("Room");
            query.setLimit(10); // 限制最多10个结果
            query.orderByDescending("createdAt");
            query.findInBackground(new FindCallback<AVObject>() {
                @Override
                public void done(List<AVObject> list, AVException e) {
                    adapter.clear();
                    for (AVObject item : list) {
                        Log.d(TAG, "Room:" + item.toJSONObject().toString());
                        final Room room = new Room();
                        room.setId(item.getObjectId());
                        room.setUrlStreamAddr(item.getString("stream_addr"));
                        AVObject avObject = item.getAVObject("conversation");
                        if(avObject ==null){
                            KJLoger.log(TAG, "此房间没有聊天室");
                        }else{
                            room.setConversationId(item.getAVObject("conversation").getObjectId());
                            KJLoger.log(TAG, "此房间有聊天室,id:"+item.getAVObject("conversation").getObjectId());
                        }
                        AVQuery<AVUser> query = AVUser.getQuery();
                        query.getInBackground(item.getAVObject("anchor").getObjectId(), new GetCallback<AVUser>() {
                            @Override
                            public void done(AVUser avUser, AVException e) {
                                Logger.d(TAG, "User:" + avUser.toString());
                                Logger.d(TAG, "portrait:" + avUser.getString("portrait"));
                                Logger.d(TAG, "nick:" + avUser.getString("nick"));
                                room.setUrlRoomIcon(avUser.getString("portrait"));
                                room.setRoomName(avUser.getString("nick"));
                                adapter.add(room);
                                mSwipeRefreshWidget.endRefreshing();
                            }
                        });
                    }
                }
            });
        }
    }

    @Override
    public void onBGARefreshLayoutBeginRefreshing(BGARefreshLayout bgaRefreshLayout) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    refreshing();
                } catch (Exception e) {
                    e.printStackTrace();
                    mSwipeRefreshWidget.endRefreshing();
                }
            }
        }).start();

    }
    public void more() throws Exception {
        Request request = new Request.Builder().url(url).build();
        Response response = client.newCall(request).execute();
        if(response.isSuccessful()){
            AVQuery<AVObject> query = new AVQuery<AVObject>("Room");
            query.setSkip(adapter.getItemCount()); // 忽略前10个
            query.orderByDescending("updatedAt");
            query.findInBackground(new FindCallback<AVObject>() {
                @Override
                public void done(List<AVObject> list, AVException e) {
                    for (AVObject item : list) {
                        Log.d(TAG, "Room:" + item.toJSONObject().toString());
                        final Room room = new Room();
                        room.setId(item.getObjectId());
                        room.setUrlStreamAddr(item.getString("stream_addr"));
                        AVQuery<AVUser> query = AVUser.getQuery();
                        query.getInBackground(item.getAVObject("anchor").getObjectId(), new GetCallback<AVUser>() {
                            @Override
                            public void done(AVUser avUser, AVException e) {
                                Logger.d(TAG, "User:" + avUser.toString());
                                Logger.d(TAG, "portrait:" + avUser.getString("portrait"));
                                Logger.d(TAG, "nick:" + avUser.getString("nick"));
                                room.setUrlRoomIcon(avUser.getString("portrait"));
                                room.setRoomName(avUser.getString("nick"));
                                adapter.add(room);
                                mSwipeRefreshWidget.endLoadingMore();
                            }
                        });
                    }
                }
            });
        }
    }


    @Override
    public boolean onBGARefreshLayoutBeginLoadingMore(BGARefreshLayout bgaRefreshLayout) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    more();

                } catch (Exception e) {
                    e.printStackTrace();
                    mSwipeRefreshWidget.endLoadingMore();
                }
            }
        }).start();
        return true;
    }
}
