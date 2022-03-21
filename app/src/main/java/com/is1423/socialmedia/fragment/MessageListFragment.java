package com.is1423.socialmedia.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.is1423.socialmedia.CreateGroupActivity;
import com.is1423.socialmedia.MainActivity;
import com.is1423.socialmedia.R;
import com.is1423.socialmedia.adapter.AdapterMessageList;
import com.is1423.socialmedia.common.Constant;
import com.is1423.socialmedia.domain.Message;
import com.is1423.socialmedia.domain.MessageList;
import com.is1423.socialmedia.domain.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MessageListFragment extends Fragment {
    FirebaseAuth firebaseAuth;
    FirebaseUser currentUser;
    DatabaseReference databaseReference;

    RecyclerView messageListRecyclerView;
    List<MessageList> messageLists;
    List<User> userList;
    AdapterMessageList adapterMessageList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_message_list, container, false);

        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference(Constant.TABLE.MESSAGE_LIST).child(currentUser.getUid());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageLists.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    MessageList messageList = ds.getValue(MessageList.class);
                    messageLists.add(messageList);
                }
                loadChats();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        messageListRecyclerView = view.findViewById(R.id.message_list_recycler_view);
        messageLists = new ArrayList<>();
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true); //to show menu option in fragment
        super.onCreate(savedInstanceState);
    }

    /*inflate options menu*/
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        //inflating menu
        menuInflater.inflate(R.menu.menu_main, menu);

        menu.findItem(R.id.action_add_post).setVisible(false);

        super.onCreateOptionsMenu(menu, menuInflater);
    }

    /*handle menu item clicks*/
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //get item id
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            firebaseAuth.signOut();
            checkUserStatus();
            updateOnlineStatus(Constant.USER_STATUS.OFFLINE);
        }else if(id==R.id.action_create_group){
            startActivity(new Intent(getActivity(), CreateGroupActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadChats() {
        userList = new ArrayList<>();
        databaseReference = FirebaseDatabase.getInstance().getReference(Constant.TABLE.USER);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    User user = ds.getValue(User.class);
                    for (MessageList messageList : messageLists) {
                        if (Objects.nonNull(user.getUid()) && user.getUid().equals(messageList.getId())) {
                            userList.add(user);
                            break;
                        }
                    }
                    adapterMessageList = new AdapterMessageList(getContext(), userList);
                    messageListRecyclerView.setAdapter(adapterMessageList);
                    for (int i = 0; i < userList.size(); i++) {
                        getLastMassage(userList.get(i).getUid());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getLastMassage(String userId) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.TABLE.MESSAGE);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String lastMessage = "default";
                for (DataSnapshot ds : snapshot.getChildren()
                ) {
                    Message message = ds.getValue(Message.class);
                    if (Objects.isNull(message)) {
                        continue;
                    }
                    String sender = message.getSender();
                    String receiver = message.getReceiver();
                    if (Objects.isNull(sender) || Objects.isNull(receiver)) {
                        continue;
                    }
                    if (receiver.equals(currentUser.getUid()) && sender.equals(userId) ||
                            receiver.equals(userId) && sender.equals(currentUser.getUid())) {
                        if (message.getType().equals(Constant.MESSAGE_TYPE.IMAGE)) {
                            lastMessage = "Sent a photo";
                        } else {
                            lastMessage = message.getMessage();
                        }
                    }
                }
                adapterMessageList.setLastMessageMap(userId, lastMessage);
                adapterMessageList.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void checkUserStatus() {
        //get current user
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            //user signed in => stay here
        } else {
            //user not signed in, go main
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        }
    }

    private void updateOnlineStatus(String status) {
        String uid = currentUser.getUid();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.TABLE.USER).child(uid);
        Map<String, Object> map = new HashMap<>();
        map.put(Constant.USER_TABLE_FIELD.ONLINE_STATUS, status);
        reference.updateChildren(map);
    }
}