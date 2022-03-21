package com.is1423.socialmedia;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.is1423.socialmedia.adapter.AdapterMessage;
import com.is1423.socialmedia.common.Constant;
import com.is1423.socialmedia.domain.Message;
import com.is1423.socialmedia.domain.User;
import com.is1423.socialmedia.notifications.APIService;
import com.is1423.socialmedia.notifications.Client;
import com.is1423.socialmedia.notifications.Data;
import com.is1423.socialmedia.notifications.Response;
import com.is1423.socialmedia.notifications.Sender;
import com.is1423.socialmedia.notifications.Token;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;

public class MessageActivity extends AppCompatActivity {

    //views
    RecyclerView recyclerView;
    ImageView profileCiv;
    TextView nameTv, userStatusTv;
    EditText messageEt;
    ImageButton sendBtn, sendImageBtn;

    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference userDbRef;
    DatabaseReference userRefForSeen;

    //for checking if user has seen message or not
    ValueEventListener seenListener;

    List<Message> messageList;
    AdapterMessage adapterMessage;

    String partnerUid;
    String currentUserUid;
    String partnerImg;

    APIService apiService;
    boolean notify = false;

    //permissions array
    String[] cameraPermissions;
    String[] storagePermissions;
    Uri imageUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        initView();

        initPermissionsArrays();

        //Layout for RecyclerView
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);

        //recyclerview properties
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        apiService = Client.getRetrofit(Constant.FCM.URL).create(APIService.class);

        /*using intent to pass user's uid
         * from uid => get profile picture, name and start chatting*/
        Intent intent = getIntent();
        partnerUid = intent.getStringExtra(Constant.COMMON_KEY.PARTNER_UID_KEY);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        userDbRef = firebaseDatabase.getReference(Constant.TABLE.USER);

        Query userQuery = userDbRef.orderByChild(Constant.USER_TABLE_FIELD.UID).equalTo(partnerUid);
        userQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //check until required info is received
                for (DataSnapshot ds : snapshot.getChildren()) {
                    //get data
                    String name = ds.child(Constant.USER_TABLE_FIELD.NAME).getValue() + "";
                    partnerImg = ds.child(Constant.USER_TABLE_FIELD.IMAGE).getValue() + "";

                    if (Objects.nonNull(ds.child(Constant.USER_TABLE_FIELD.ONLINE_STATUS).getValue())) {
                        String onlineStatus = "" + ds.child(Constant.USER_TABLE_FIELD.ONLINE_STATUS).getValue();
                        userStatusTv.setText(onlineStatus);
                    }
                    nameTv.setText(name);
                    try {
                        Picasso.get().load(partnerImg).placeholder(R.drawable.ic_default_img_white).into(profileCiv);
                    } catch (Exception e) {
                        Picasso.get().load(R.drawable.ic_default_img_white).into(profileCiv);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //click btn to sent message
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notify = true;
                //get text from edit text
                String message = messageEt.getText().toString().trim();
                if (TextUtils.isEmpty(message)) {
                    Toast.makeText(MessageActivity.this, "Cannot send empty message...", Toast.LENGTH_SHORT);
                } else {
                    sendMessage(message);
                }
                messageEt.setText("");
            }
        });

        sendImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImagePickDialog();
            }
        });

        readMessage();
        seenMessage();
    }

    @Override
    protected void onStart() {
        checkUserStatus();
        updateOnlineStatus(Constant.USER_STATUS.ONLINE);
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        updateOnlineStatus(Constant.USER_STATUS.OFFLINE);
        userRefForSeen.removeEventListener(seenListener);

    }

    @Override
    protected void onResume() {
        updateOnlineStatus(Constant.USER_STATUS.ONLINE);
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        //hide searchview
        menu.findItem(R.id.action_search).setVisible(false);
        menu.findItem(R.id.action_add_post).setVisible(false);
        menu.findItem(R.id.action_logout).setVisible(false);
        menu.findItem(R.id.action_create_group).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //get item id
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            firebaseAuth.signOut();
            checkUserStatus();
            updateOnlineStatus(Constant.USER_STATUS.OFFLINE);
        }
        return super.onOptionsItemSelected(item);
    }

    private void seenMessage() {
        userRefForSeen = FirebaseDatabase.getInstance().getReference(Constant.TABLE.MESSAGE);
        seenListener = userRefForSeen.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Message message = ds.getValue(Message.class);
                    if (message.getReceiver().equals(currentUserUid) && message.getSender().equals(partnerUid)) {
                        Map<String, Object> hasSeenHashMap = new HashMap<>();
                        hasSeenHashMap.put(Constant.MESSAGE_TABLE_FIELD.IS_SEEN, true);
                        ds.getRef().updateChildren(hasSeenHashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void readMessage() {
        messageList = new ArrayList<>();
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference(Constant.TABLE.MESSAGE);
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Message message = ds.getValue(Message.class);
                    if ((message.getReceiver().equals(currentUserUid) && message.getSender().equals(partnerUid))
                            || (message.getReceiver().equals(partnerUid) && message.getSender().equals(currentUserUid))) {
                        messageList.add(message);
                    }

                    //adapter
                    adapterMessage = new AdapterMessage(MessageActivity.this, messageList, partnerImg);
                    adapterMessage.notifyDataSetChanged();
                    recyclerView.setAdapter(adapterMessage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendMessage(String message) {
        //save to database
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        String sendDatetime = String.valueOf(System.currentTimeMillis());
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put(Constant.MESSAGE_TABLE_FIELD.SENDER, currentUserUid);
        hashMap.put(Constant.MESSAGE_TABLE_FIELD.RECEIVER, partnerUid);
        hashMap.put(Constant.MESSAGE_TABLE_FIELD.MESSAGE, message);
        hashMap.put(Constant.MESSAGE_TABLE_FIELD.SEND_DATETIME, sendDatetime);
        hashMap.put(Constant.MESSAGE_TABLE_FIELD.IS_SEEN, false);
        hashMap.put(Constant.MESSAGE_TABLE_FIELD.TYPE, Constant.MESSAGE_TYPE.TEXT);
        reference.child(Constant.TABLE.MESSAGE).push().setValue(hashMap);

        DatabaseReference database = FirebaseDatabase.getInstance().getReference(Constant.TABLE.USER).child(currentUserUid);
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (notify) {
                    sendNotification(partnerUid, user.getName(), message);
                }
                notify = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        DatabaseReference messageListDbRef = FirebaseDatabase.getInstance().getReference(Constant.TABLE.MESSAGE_LIST)
                .child(currentUserUid)
                .child(partnerUid);
        messageListDbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    messageListDbRef.child(Constant.MESSAGE_LIST_TABLE_FIELD.ID).setValue(partnerUid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        DatabaseReference messageListDbRef2 = FirebaseDatabase.getInstance().getReference(Constant.TABLE.MESSAGE_LIST)
                .child(partnerUid)
                .child(currentUserUid);

        messageListDbRef2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    messageListDbRef2.child(Constant.MESSAGE_LIST_TABLE_FIELD.ID).setValue(currentUserUid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendImageMessage(Uri imageUri) {
        notify = true;
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Sending image...");
        progressDialog.show();

        String currentTime = System.currentTimeMillis() + "";
        String fileNameAndPath = "ChatImage/" + "post_" + currentTime;

        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(fileNameAndPath);
        storageReference.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful()) ;
                        String downloadUri = uriTask.getResult().toString();
                        if (uriTask.isSuccessful()) {
                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

                            Map<String, Object> map = new HashMap<>();
                            map.put(Constant.MESSAGE_TABLE_FIELD.SENDER, currentUserUid);
                            map.put(Constant.MESSAGE_TABLE_FIELD.RECEIVER, partnerUid);
                            map.put(Constant.MESSAGE_TABLE_FIELD.MESSAGE, downloadUri);
                            map.put(Constant.MESSAGE_TABLE_FIELD.SEND_DATETIME, currentTime);
                            map.put(Constant.MESSAGE_TABLE_FIELD.TYPE, Constant.MESSAGE_TYPE.IMAGE);
                            map.put(Constant.MESSAGE_TABLE_FIELD.IS_SEEN, false);

                            databaseReference.child(Constant.TABLE.MESSAGE).push().setValue(map);

                            progressDialog.dismiss();

                            //send noti
                            DatabaseReference userDbRef = FirebaseDatabase.getInstance().getReference(Constant.TABLE.USER).child(currentUserUid);
                            userDbRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    User user = snapshot.getValue(User.class);
                                    if (notify) {
                                        sendNotification(partnerUid, user.getName(), "Send a photo...");
                                    }
                                    notify = false;
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                            DatabaseReference messageListDbRef = FirebaseDatabase.getInstance().getReference(Constant.TABLE.MESSAGE_LIST)
                                    .child(currentUserUid)
                                    .child(partnerUid);
                            messageListDbRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (!snapshot.exists()) {
                                        messageListDbRef.child(Constant.MESSAGE_LIST_TABLE_FIELD.ID).setValue(partnerUid);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                            DatabaseReference messageListDbRef2 = FirebaseDatabase.getInstance().getReference(Constant.TABLE.MESSAGE_LIST)
                                    .child(partnerUid)
                                    .child(currentUserUid);

                            messageListDbRef2.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (!snapshot.exists()) {
                                        messageListDbRef2.child(Constant.MESSAGE_LIST_TABLE_FIELD.ID).setValue(currentUserUid);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(MessageActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void sendNotification(String partnerUid, String name, String message) {
        DatabaseReference allTokens = FirebaseDatabase.getInstance().getReference(Constant.TABLE.TOKEN);
        Query query = allTokens.orderByKey().equalTo(partnerUid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Token token = ds.getValue(Token.class);
                    Data data = new Data(currentUserUid, name + ": " + message, getString(R.string.new_message), partnerUid, R.drawable.ic_default_img_primary);
                    Sender sender = new Sender(data, token.getToken());
                    apiService.sendNotification(sender)
                            .enqueue(new Callback<Response>() {
                                @Override
                                public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
                                    Toast.makeText(MessageActivity.this, "" + response.message(), Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onFailure(Call<Response> call, Throwable t) {

                                }
                            });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void initView() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");
        recyclerView = findViewById(R.id.message_recyclerView);
        profileCiv = findViewById(R.id.profileCiv);
        nameTv = findViewById(R.id.nameTv);
        userStatusTv = findViewById(R.id.userStatusTv);
        messageEt = findViewById(R.id.messageEt);
        sendBtn = findViewById(R.id.sendBtn);
        sendImageBtn = findViewById(R.id.sendImageBtn);
    }

    private void checkUserStatus() {
        //get current user
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            currentUserUid = user.getUid();

        } else {
            //user not signed in, go main
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    private void updateOnlineStatus(String status) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.TABLE.USER).child(currentUserUid);
        Map<String, Object> map = new HashMap<>();
        map.put(Constant.USER_TABLE_FIELD.ONLINE_STATUS, status);
        reference.updateChildren(map);
    }

    private void initPermissionsArrays() {
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
    }

    private void showImagePickDialog() {
        //dialog containing options: Camera/Gallery
        String options[] = {Constant.IMAGE_SOURCE_OPTIONS.CAMERA, Constant.IMAGE_SOURCE_OPTIONS.GALLERY};
        //alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick image from");
        //set items
        builder.setItems(options, onImagePickDialogClickedListener);
        //create and show dialog
        builder.create().show();
    }

    DialogInterface.OnClickListener onImagePickDialogClickedListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            if (i == 0) {
                if (!checkCameraPermission()) {
                    requestCameraPermission();
                } else {
                    pickFromCamera();
                }
            }
            if (i == 1) {
                if (!checkStoragePermission()) {
                    requestStoragePermission();
                } else {
                    pickFromGallery();
                }
            }
        }
    };

    private boolean checkStoragePermission() {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, storagePermissions, Constant.REQUEST_CODE.STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED)
                && ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, cameraPermissions, Constant.REQUEST_CODE.CAMERA_REQUEST_CODE);
    }

    private void pickFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, Constant.REQUEST_CODE.IMAGE_PICK_GALLERY_CODE);
    }

    private void pickFromCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Temp Pic");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Temp Description");

        imageUri = this.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(cameraIntent, Constant.REQUEST_CODE.IMAGE_PICK_CAMERA_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Constant.REQUEST_CODE.CAMERA_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && storageAccepted) {
                        pickFromCamera();
                    } else {
                        Toast.makeText(this, "Camera & Storage both permissions are necessary...", Toast.LENGTH_SHORT).show();
                    }
                } else {
                }
            }
            break;
            case Constant.REQUEST_CODE.STORAGE_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted) {
                        pickFromGallery();
                    } else {
                        Toast.makeText(this, "Storage permission is necessary...", Toast.LENGTH_SHORT).show();
                    }
                } else {
                }
                break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == Constant.REQUEST_CODE.IMAGE_PICK_GALLERY_CODE) {
                imageUri = data.getData();
                sendImageMessage(imageUri);
            } else if (requestCode == Constant.REQUEST_CODE.IMAGE_PICK_CAMERA_CODE) {
                sendImageMessage(imageUri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}