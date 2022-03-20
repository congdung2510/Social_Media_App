package com.is1423.socialmedia.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.is1423.socialmedia.R;
import com.is1423.socialmedia.common.Constant;
import com.is1423.socialmedia.domain.Message;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class AdapterMessage extends RecyclerView.Adapter<AdapterMessage.MyHolder> {
    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;
    Context context;
    List<Message> messageList;
    String imageUrl;
    FirebaseUser firebaseUser;


    public AdapterMessage(Context context, List<Message> messageList, String imageUrl) {
        this.context = context;
        this.messageList = messageList;
        this.imageUrl = imageUrl;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_right, parent, false);
            return new MyHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_left, parent, false);
            return new MyHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        String message = messageList.get(position).getMessage();
        if(Objects.nonNull(messageList.get(position).getSendDatetime())){
            String time = messageList.get(position).getSendDatetime();

            Calendar cal = Calendar.getInstance(Locale.ENGLISH);
            cal.setTimeInMillis(Long.parseLong(time));
            String sendDatetime = DateFormat.format("dd/MM/yyyy hh:mm aa", cal).toString();
            holder.timeTv.setText(sendDatetime);
        }

        //set data
        holder.messageTv.setText(message);
        try {
            Picasso.get().load(imageUrl).into(holder.profileIv);
        } catch (Exception e) {

        }

        deleteMessageClicked(holder, position);

        if (position == messageList.size() - 1) {
            if (messageList.get(position).isSeen()) {
                holder.isSeenTv.setText(Constant.MESSAGE_STATUS.SEEN);
            } else {
                holder.isSeenTv.setText(Constant.MESSAGE_STATUS.DELIVERED);
            }
        } else {
            holder.isSeenTv.setVisibility(View.GONE);
        }
    }

    private void deleteMessageClicked(MyHolder holder, int position) {
        holder.messageLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete");
                builder.setMessage("Are you sure to delete this message?");
                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        deleteMessage(position);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

                builder.create().show();
            }
        });
    }

    private void deleteMessage(int position) {
        String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String msgSentDatetime = messageList.get(position).getSendDatetime();
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference(Constant.TABLE.MESSAGE);
        Query query = dbRef.orderByChild(Constant.MESSAGE_TABLE_FIELD.SEND_DATETIME).equalTo(msgSentDatetime);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()){
                    if(ds.child(Constant.MESSAGE_TABLE_FIELD.SENDER).getValue().equals(currentUid)){
                        Map<String, Object> map = new HashMap<>();
                        map.put(Constant.MESSAGE_TABLE_FIELD.MESSAGE, Constant.MESSAGE_COMMON.DELETED);
                        ds.getRef().updateChildren(map);

                        Toast.makeText(context, "message deleted...", Toast.LENGTH_SHORT).show();
                    }else {
                        Toast.makeText(context, "You can delete only your message", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    @Override
    public int getItemViewType(int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (messageList.get(position).getSender().equals(firebaseUser.getUid())) {
            return MSG_TYPE_RIGHT;
        } else return MSG_TYPE_LEFT;
    }

    //view holder class
    class MyHolder extends RecyclerView.ViewHolder {
        //view
        ImageView profileIv;
        TextView messageTv, timeTv, isSeenTv;
        LinearLayout messageLayout; //for click listener to show delete

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            initView();
        }

        private void initView() {
            profileIv = itemView.findViewById(R.id.profileIv);
            messageTv = itemView.findViewById(R.id.messageTv);
            timeTv = itemView.findViewById(R.id.timeTv);
            isSeenTv = itemView.findViewById(R.id.isSeenTv);
            messageLayout = itemView.findViewById(R.id.messageLayout);
        }
    }
}
