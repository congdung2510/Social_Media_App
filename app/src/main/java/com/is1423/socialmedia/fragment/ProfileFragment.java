package com.is1423.socialmedia.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
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
import com.is1423.socialmedia.AddPostActivity;
import com.is1423.socialmedia.CreateGroupActivity;
import com.is1423.socialmedia.MainActivity;
import com.is1423.socialmedia.R;
import com.is1423.socialmedia.common.Constant;
import com.squareup.picasso.Picasso;

import java.net.URI;
import java.security.Key;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {

    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    StorageReference storageReference;
    //path: storage user profile/cover photo
    String storagePath = "Users_Profile_Cover_Imgs/";

    //views
    ImageView avatarTv, coverIv;
    TextView nameTv, emailTv, phoneTv;
    FloatingActionButton floatingActionButton;

    ProgressDialog pd;

    //arrays of permissions to be requested
    String cameraPermissions[];
    String storagePermissions[];

    //uri of picked image
    Uri image_uri;

    //for checking profile or cover photo
    String profileOrCover;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        initFirebase();
        initArraysOfPermissions();
        initView(view);
        pd = new ProgressDialog(getActivity());

          /* get current user info by uid/email
        using orderByChild query => show detail from a node
        whose key named email and currently signed in email have same value
        It will search all nodes, where key matches => get detail */
        Query query = databaseReference.orderByChild(Constant.USER_TABLE_FIELD.EMAIL).equalTo(user.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //check until required data get
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    getCurrentUserInfo(dataSnapshot);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //floating action button clicked
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showEditProfileDialog();
            }
        });
        return view;
    }

    private void initFirebase() {
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference(Constant.TABLE.USER);
        storageReference = FirebaseStorage.getInstance().getReference();
    }

    private void initArraysOfPermissions() {
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
    }

    private void initView(View view) {
        avatarTv = view.findViewById(R.id.avatarTv);
        nameTv = view.findViewById(R.id.nameTv);
        phoneTv = view.findViewById(R.id.phoneTv);
        emailTv = view.findViewById(R.id.emailTv);
        coverIv = view.findViewById(R.id.coverIv);
        floatingActionButton = view.findViewById(R.id.fab);
    }

    private void getCurrentUserInfo(DataSnapshot dataSnapshot) {
        //get data
        String name = "" + dataSnapshot.child(Constant.USER_TABLE_FIELD.NAME).getValue();
        String email = "" + dataSnapshot.child(Constant.USER_TABLE_FIELD.EMAIL).getValue();
        String phone = "" + dataSnapshot.child(Constant.USER_TABLE_FIELD.PHONE).getValue();
        String image = "" + dataSnapshot.child(Constant.USER_TABLE_FIELD.IMAGE).getValue();
        String cover = "" + dataSnapshot.child(Constant.USER_TABLE_FIELD.COVER).getValue();

        //set data
        nameTv.setText(name);
        emailTv.setText(email);
        phoneTv.setText(phone);
        try {
            Picasso.get().load(image).into(avatarTv);
        } catch (Exception e) {
            //e -> set default image
            Picasso.get().load(R.drawable.ic_add_a_photo_black).into(avatarTv);
        }
        try {
            Picasso.get().load(cover).into(coverIv);
        } catch (Exception e) {
        }
    }

    private void showEditProfileDialog() {
        //options to show in dialog
        String options[] = {Constant.EDIT_PROFILE_OPTION.CHANGE_PROFILE_PHOTO,
                Constant.EDIT_PROFILE_OPTION.CHANGE_COVER_PHOTO,
                Constant.EDIT_PROFILE_OPTION.EDIT_NAME,
                Constant.EDIT_PROFILE_OPTION.EDIT_PHONE_NUMBER,
                Constant.EDIT_PROFILE_OPTION.CHANGE_PASSWORD};
        //alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        //set title
        builder.setTitle("Choose Action");
        //set items
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //handle dialog item click
                if (i == 0) {
                    //Edit Profile Photo
                    pd.setTitle("Updating Profile Photo");
                    profileOrCover = "image";
                    showImagePicDialog();
                } else if (i == 1) {
                    //Edit Cover Photo
                    pd.setMessage("Updating Cover Photo");
                    profileOrCover = "cover";
                    showImagePicDialog();

                } else if (i == 2) {
                    ///Edit Name
                    pd.setMessage("Updating Name");
                    showNamePhoneUpdateDialog("name");

                } else if (i == 3) {
                    ///Edit Phone Number
                    pd.setMessage("Updating Phone Number");
                    showNamePhoneUpdateDialog("phone");
                } else if (i == 4) {
                    ///Edit Phone Number
                    pd.setMessage("Changing Password");
                    showChangePasswordDialog();
                }
            }
        });

        //create and show dialog
        builder.create().show();
    }

    private void showChangePasswordDialog() {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.update_password_dialog, null);
        EditText passwordEt = view.findViewById(R.id.passwordEt);
        EditText newPasswordEt = view.findViewById(R.id.newPasswordEt);
        Button updatePasswordBtn = view.findViewById(R.id.updatePasswordBtn);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);

        AlertDialog dialog = builder.create();
        dialog.show();
        updatePasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String oldPassword = passwordEt.getText().toString().trim();
                String newPassword = newPasswordEt.getText().toString().trim();
                if (TextUtils.isEmpty(oldPassword)) {
                    Toast.makeText(getActivity(), "Old password required...", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (newPassword.length() < 8) {
                    Toast.makeText(getActivity(), "Password length must at least 6 characters...", Toast.LENGTH_SHORT).show();
                    return;
                }
                dialog.dismiss();
                updatePassword(oldPassword, newPassword);
            }
        });
    }

    private void updatePassword(String oldPassword, String newPassword) {
        pd.show();
        FirebaseUser fUser = firebaseAuth.getCurrentUser();
        AuthCredential authCredential = EmailAuthProvider.getCredential(user.getEmail(), oldPassword);
        user.reauthenticate(authCredential)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        user.updatePassword(newPassword)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        pd.dismiss();
                                        Toast.makeText(getActivity(), "Password Updated", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(getActivity(), "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        Toast.makeText(getActivity(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showImagePicDialog() {
        //dialog containing options: Camera/Gallery
        String options[] = {Constant.IMAGE_SOURCE_OPTIONS.CAMERA, Constant.IMAGE_SOURCE_OPTIONS.GALLERY};
        //alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        //set title
        builder.setTitle("Pick image from");
        //set items
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //handle dialog item click
                if (i == 0) {
                    //Camera clicked
                    if (!checkCameraPermission()) {
                        requestCameraPermission();
                    } else {
                        pickFromCamera();
                    }

                } else if (i == 1) {
                    //Gallery clicked
                    if (!checkStoragePermission()) {
                        requestStoragePermission();
                    } else {
                        pickFromGallery();
                    }
                }
            }
        });

        //create and show dialog
        builder.create().show();
    }

    private void showNamePhoneUpdateDialog(String key) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Update " + key);

        //layout of dialog
        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(10, 10, 10, 10);

        //edit text
        EditText editText = new EditText(getActivity());
        editText.setHint("Enter " + key);
        linearLayout.addView(editText);

        builder.setView(linearLayout);

        //button
        builder
                .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String value = editText.getText().toString().trim();
                        if (!TextUtils.isEmpty(value)) {
                            pd.show();
                            Map<String, Object> result = new HashMap<>();
                            result.put(key, value);

                            databaseReference.child(user.getUid()).updateChildren(result)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            pd.dismiss();
                                            Toast.makeText(getActivity(), "Updated", Toast.LENGTH_SHORT);
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            pd.dismiss();
                                            Toast.makeText(getActivity(), "" + e.getMessage(), Toast.LENGTH_SHORT);
                                        }
                                    });
                        } else {
                            Toast.makeText(getActivity(), "Please enter " + key, Toast.LENGTH_SHORT);
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
        builder.create().show();
    }

    private boolean checkStoragePermission() {
        //check if storage permission is enabled or not
        return ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
    }

    private void requestStoragePermission() {
        //request runtime storage permission
        requestPermissions(storagePermissions, Constant.REQUEST_CODE.STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission() {
        //check if storage permission is enabled or not
        boolean isCameraGranted = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                == (PackageManager.PERMISSION_GRANTED);

        boolean isWriteStorageGranted = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);

        return isCameraGranted && isWriteStorageGranted;
    }

    private void requestCameraPermission() {
        //request runtime storage permission
        requestPermissions(cameraPermissions, Constant.REQUEST_CODE.CAMERA_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //when user press Allow or Deny from permission request dialog
        switch (requestCode) {
            case Constant.REQUEST_CODE.CAMERA_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && writeStorageAccepted) {
                        pickFromCamera();
                    } else {
                        Toast.makeText(getActivity(), "Please enable camera & storage permission", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;

            case Constant.REQUEST_CODE.STORAGE_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean writeStorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (writeStorageAccepted) {
                        pickFromGallery();
                    } else {
                        Toast.makeText(getActivity(), "Please enable storage permission", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
        }
    }

    private void pickFromCamera() {
        //Intent of picking image from device camera
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Temp Pic");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Temp Description");

        image_uri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        //intent to start camera
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(cameraIntent, Constant.REQUEST_CODE.IMAGE_PICK_CAMERA_CODE);
    }

    private void pickFromGallery() {
        //pick from gallery
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, Constant.REQUEST_CODE.IMAGE_PICK_GALLERY_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //called after picking image
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == Constant.REQUEST_CODE.IMAGE_PICK_GALLERY_CODE) {
                image_uri = data.getData();
                uploadProfileCoverPhoto(image_uri);
            }
            if (requestCode == Constant.REQUEST_CODE.IMAGE_PICK_CAMERA_CODE) {
                uploadProfileCoverPhoto(image_uri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadProfileCoverPhoto(Uri uri) {
        String filePathAndName = storagePath + "" + profileOrCover + "_" + user.getUid();
        StorageReference storageReference2nd = storageReference.child(filePathAndName);
        storageReference2nd.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //image is uploaded to storage
                        //get url => store in user's database
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful()) ;
                        Uri downloadUri = uriTask.getResult();

                        //if image is uploaded & url is received
                        if (uriTask.isSuccessful()) {
                            Map<String, Object> results = new HashMap<>();
                            results.put(profileOrCover, downloadUri.toString());
                            databaseReference.child(user.getUid()).updateChildren(results)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            pd.dismiss();
                                            Toast.makeText(getActivity(), "Image updated...", Toast.LENGTH_LONG);
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            pd.dismiss();
                                            Toast.makeText(getActivity(), "Error while updating image...", Toast.LENGTH_LONG);
                                        }
                                    });
                        } else {
                            pd.dismiss();
                            Toast.makeText(getActivity(), "Some errror occured", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
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
        menu.findItem(R.id.action_logout).setVisible(false);
        super.onCreateOptionsMenu(menu, menuInflater);
    }
}
