package com.wanshi.wanshi.adapter;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.wanshi.wanshi.R;
import com.wanshi.wanshi.bean.Room;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public class ListLiveAdapter extends RecyclerView.Adapter<ListLiveAdapter.MyViewHolder>  implements View.OnClickListener {

    private List<Room> listRoom;
    private Context mContext;//用于接收传递过来的Context对象
    private OnRecyclerViewItemClickListener mOnItemClickListener = null;

    public ListLiveAdapter(Context context) {
        super();
        this.mContext = context;
        this.listRoom =  new ArrayList<>();
    }

    public void add(LinkedList<Room> listRoom){
        this.listRoom.addAll(listRoom);
        this.notifyDataSetChanged();
    }
    public void add(Room room){
        this.listRoom.add(room);
        this.notifyDataSetChanged();
    }
    public void clear(){
        this.listRoom.clear();
        this.notifyDataSetChanged();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_list_live, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        view.setOnClickListener(this);
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position)
    {

        holder.textRoomName.setText(listRoom.get(position).getRoomName()+"("+listRoom.get(position).getConversationId()+")");
        holder.imageRoomIcon.setImageURI(Uri.parse(listRoom.get(position).getUrlRoomIcon()));
        holder.itemView.setTag(listRoom.get(position));
    }

    @Override
    public int getItemCount()
    {
        return listRoom.size();
    }

    @Override
    public void onClick(View v) {
        if (mOnItemClickListener != null) {
            //注意这里使用getTag方法获取数据
            mOnItemClickListener.onItemClick(v,(Room)v.getTag());
        }
    }
    public void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    //define interface
    public static interface OnRecyclerViewItemClickListener {
        void onItemClick(View view , Room data);
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView textRoomName;
        SimpleDraweeView imageRoomIcon;
        public MyViewHolder(View view)
        {
            super(view);
            textRoomName = (TextView) view.findViewById(R.id.textRoomName);
            imageRoomIcon = (SimpleDraweeView) view.findViewById(R.id.imageRoomIcon);
        }
    }

}

