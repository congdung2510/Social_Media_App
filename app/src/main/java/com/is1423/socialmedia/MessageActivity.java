package com.is1423.socialmedia;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class MessageActivity extends AppCompatActivity {
    //view from xml
    Toolbar toolbar;
    RecyclerView recyclerView;
    ImageView profileCiv;
    TextView nameTv, userStatusTv;
    EditText messageEt;
    ImageButton sendBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        initView();
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
    }
}