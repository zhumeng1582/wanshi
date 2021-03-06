package com.wanshi.app.adapter;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.wanshi.app.R;
import com.wanshi.app.bean.Room;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public class ListLiveAdapter extends RecyclerView.Adapter<ListLiveAdapter.Holder>  implements View.OnClickListener {

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
    public Holder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_list_live, parent, false);
        Holder holder = new Holder(view);
        view.setOnClickListener(this);
        return holder;
    }

    @Override
    public void onBindViewHolder(Holder holder, int position)
    {
        if(TextUtils.isEmpty(listRoom.get(position).getConversationId())){
            holder.textRoomName.setText(listRoom.get(position).getRoomName()+"(x)");
        }else{
            holder.textRoomName.setText(listRoom.get(position).getRoomName());
        }

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
    public interface OnRecyclerViewItemClickListener {
        void onItemClick(View view , Room data);
    }

    class Holder extends RecyclerView.ViewHolder {
        TextView textRoomName;
        SimpleDraweeView imageRoomIcon;
        public Holder(View view)
        {
            super(view);
            textRoomName = (TextView) view.findViewById(R.id.textRoomName);
            imageRoomIcon = (SimpleDraweeView) view.findViewById(R.id.imageRoomIcon);
        }
    }

}

