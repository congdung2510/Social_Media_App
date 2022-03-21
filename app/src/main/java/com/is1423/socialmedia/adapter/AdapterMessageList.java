package com.is1423.socialmedia.adapter;

import android.content.Context;
import android.content.Intent;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.is1423.socialmedia.R;
import com.is1423.socialmedia.common.Constant;
import com.is1423.socialmedia.domain.MessageList;
import com.is1423.socialmedia.domain.User;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class AdapterMessageList extends RecyclerView.Adapter<AdapterMessageList.MyHolder>{
    Context context;
    List<User> userList;
    private Map<String, String> lastMessageMap;

    public AdapterMessageList(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;
        lastMessageMap = new HashMap<>();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_message_list, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        String partnerUid = userList.get(position).getUid();
        String userImage = userList.get(position).getImage();
        String userName = userList.get(position).getName();
        String lastMessage = lastMessageMap.get(partnerUid);

        holder.nameTv.setText(userName);
        if(Objects.isNull(lastMessage)||lastMessage.equals("default")){
            holder.lastMessageTv.setVisibility(View.GONE);
        }else {
            holder.lastMessageTv.setVisibility(View.VISIBLE);
            holder.lastMessageTv.setText(lastMessage);
        }

        try {
            Picasso.get().load(userImage).placeholder(R.drawable.ic_default_img_primary).into(holder.profileIv);
        }catch (Exception e){
            Picasso.get().load(R.drawable.ic_default_img_primary).into(holder.profileIv);
        }

        if(userList.get(position).getOnlineStatus().equals(Constant.USER_STATUS.ONLINE)){
            holder.onlineStatusCiv.setImageResource(R.drawable.circle_online_status);
        }else {
            holder.onlineStatusCiv.setImageResource(R.drawable.circle_offline_status);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, MessageList.class);
                intent.putExtra(Constant.COMMON_KEY.PARTNER_UID_KEY, partnerUid);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public void setLastMessageMap(String userId, String lastMessage){
        lastMessageMap.put(userId, lastMessage);
    }

    class MyHolder extends RecyclerView.ViewHolder{
        ImageView profileIv, onlineStatusCiv;
        TextView nameTv, lastMessageTv;


        public MyHolder(@NonNull View itemView) {
            super(itemView);

            initView();
        }

        private void initView() {
            profileIv = itemView.findViewById(R.id.profile_iv);
            onlineStatusCiv = itemView.findViewById(R.id.online_status_civ);
            nameTv = itemView.findViewById(R.id.name_tv);
            lastMessageTv = itemView.findViewById(R.id.last_message_tv);
        }
    }
}
