package com.is1423.socialmedia;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.is1423.socialmedia.fragment.HomeFragment;
import com.is1423.socialmedia.fragment.MessageListFragment;
import com.is1423.socialmedia.fragment.ProfileFragment;
import com.is1423.socialmedia.fragment.UsersFragment;

public class DashboardActivity extends AppCompatActivity {

    //firebase auth
    FirebaseAuth firebaseAuth;

    //views
    BottomNavigationView bottomNavigationView;

    ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        //actionbar and its title
        actionBar = getSupportActionBar();
        actionBar.setTitle("Profile");

        //init
        firebaseAuth = FirebaseAuth.getInstance();
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(onItemSelectedListener);

        //default
        actionBar.setTitle("Home");
        HomeFragment homeFragment = new HomeFragment();
        FragmentTransaction homeFt = getSupportFragmentManager().beginTransaction();
        homeFt.replace(R.id.content, homeFragment, "");
    }

    private NavigationBarView.OnItemSelectedListener onItemSelectedListener = new NavigationBarView.OnItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.nav_home:
                    //home fragment transaction
                    actionBar.setTitle("Home");
                    HomeFragment homeFragment = new HomeFragment();
                    FragmentTransaction homeFt = getSupportFragmentManager().beginTransaction();
                    homeFt.replace(R.id.content, homeFragment, "");
                    homeFt.commit();
                    return true;
                case R.id.nav_profile:
                    //profile fragment transaction
                    actionBar.setTitle("Profile");
                    ProfileFragment profileFragment = new ProfileFragment();
                    FragmentTransaction profileFt = getSupportFragmentManager().beginTransaction();
                    profileFt.replace(R.id.content, profileFragment, "");
                    profileFt.commit();
                    return true;
                case R.id.nav_users:
                    //profile fragment transaction
                    actionBar.setTitle("Users");
                    UsersFragment usersFragment = new UsersFragment();
                    FragmentTransaction usersFt = getSupportFragmentManager().beginTransaction();
                    usersFt.replace(R.id.content, usersFragment, "");
                    usersFt.commit();
                    return true;
                case R.id.nav_message:
                    //message fragment transaction
                    actionBar.setTitle("Message");
                    MessageListFragment messageListFragment = new MessageListFragment();
                    FragmentTransaction messageFt = getSupportFragmentManager().beginTransaction();
                    messageFt.replace(R.id.content, messageListFragment, "");
                    messageFt.commit();
                    return true;
            }
            return false;
        }
    };

    private void checkUserStatus() {
        //get current user
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            //user signed in => stay here
        } else {
            //user not signed in, go main
            startActivity(new Intent(DashboardActivity.this, MainActivity.class));
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onStart() {
        checkUserStatus();
        super.onStart();
    }
}