package com.is1423.socialmedia.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.is1423.socialmedia.AddPostActivity;
import com.is1423.socialmedia.MainActivity;
import com.is1423.socialmedia.MessageActivity;
import com.is1423.socialmedia.R;
import com.is1423.socialmedia.common.Constant;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {
    FirebaseAuth firebaseAuth;
    FirebaseUser fUser;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        firebaseAuth = FirebaseAuth.getInstance();
        fUser = firebaseAuth.getCurrentUser();
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
        menu.findItem(R.id.action_create_group).setVisible(false);
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
        }
        if (id == R.id.action_add_post) {
            startActivity(new Intent(getActivity(), AddPostActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    private void checkUserStatus() {

        if (fUser != null) {
            //user signed in => stay here
        } else {
            //user not signed in, go main
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        }
    }

    private void updateOnlineStatus(String status) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.TABLE.USER).child(fUser.getUid());
        Map<String, Object> map = new HashMap<>();
        map.put(Constant.USER_TABLE_FIELD.ONLINE_STATUS, status);
        reference.updateChildren(map);
    }
}