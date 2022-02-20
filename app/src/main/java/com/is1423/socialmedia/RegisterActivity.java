package com.is1423.socialmedia;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    //views
    EditText mEmailEt, mPasswordEt;
    Button mRegisterBtn;
    TextView mHaveAccountTv;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //actionbar and its title
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Create Account");

        //enable back button
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        initViews();

        mAuth = FirebaseAuth.getInstance();

        //handle register btn click
        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerWithEmailAndPassword();
            }
        });

        //handle login textview click listener
        mHaveAccountTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish();
            }
        });
    }

    private void registerWithEmailAndPassword() {
        //input email, password
        String email = mEmailEt.getText().toString().trim();
        String password = mPasswordEt.getText().toString().trim();

        //validate
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            //set error, focus on email EditText
            mEmailEt.setError("Invalid Email!");
            mEmailEt.setFocusable(true);
        } else if (password.length() < 8) {
            //set error, focus on password EditText
            mPasswordEt.setError("Password length at least 8 characters");
            mPasswordEt.setFocusable(true);
        } else {
            registerUser(email, password);
        }
    }

    private void initViews() {
        mEmailEt = findViewById(R.id.emailEt);
        mPasswordEt = findViewById(R.id.passwordEt);
        mRegisterBtn = findViewById(R.id.RegisterBtn);
        mHaveAccountTv = findViewById(R.id.have_accountTv);
    }

    private void registerUser(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            saveNewUser();

                            startActivity(new Intent(RegisterActivity.this, DashboardActivity.class));
                            finish();
                        } else {
                            Toast.makeText(RegisterActivity.this, "Authentication failed", Toast.LENGTH_LONG).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //show error message
                        Toast.makeText(RegisterActivity.this, "" + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveNewUser() {
        //sign in success
        FirebaseUser user = mAuth.getCurrentUser();

        //get user email and uid from auth
        String email = user.getEmail();
        String uid = user.getUid();
        //using HashMap to store user infor
        Map<Object,String> hashMap = new HashMap<>();
        hashMap.put("uid", uid);
        hashMap.put("email", email);
        hashMap.put("name", "");
        hashMap.put("phone", "");
        hashMap.put("image", "");
        hashMap.put("cover", "");

        //firebase database instance
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        //path to store user data named "User"
        DatabaseReference reference = database.getReference("User");
        //put data within hashmap in database
        reference.child(uid).setValue(hashMap);

        Toast.makeText(RegisterActivity.this, "Registered...\n" + user.getEmail(), Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        //go previous activity
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}