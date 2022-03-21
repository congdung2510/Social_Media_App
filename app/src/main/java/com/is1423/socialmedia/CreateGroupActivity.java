package com.is1423.socialmedia;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.is1423.socialmedia.common.Constant;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CreateGroupActivity extends AppCompatActivity {
    //actionbar
    private ActionBar actionBar;

    //firebase
    private FirebaseAuth firebaseAuth;

    //views
    private ImageView groupIconCiv;
    private EditText groupTitleEt, groupDescriptionEt;
    private FloatingActionButton createGroupBtn;

    //permissions
    String cameraPermissions[];
    String storagePermissions[];

    //uri of picked image
    Uri image_uri;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        initActionBar();
        initViews();
        initArraysOfPermissions();

        firebaseAuth = FirebaseAuth.getInstance();
        checkUser();

        groupIconCiv.setOnClickListener(groupIconOnClickListener);
        createGroupBtn.setOnClickListener(createGroupBtnOnClickListener);
    }

    private void initViews() {
        groupIconCiv = findViewById(R.id.groupIconCiv);
        groupTitleEt = findViewById(R.id.groupTitleEt);
        groupDescriptionEt = findViewById(R.id.groupDescriptionEt);
        createGroupBtn = findViewById(R.id.createGroupBtn);
    }

    private void checkUser() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (Objects.nonNull(user)) {
            actionBar.setSubtitle(user.getEmail());
        }
    }

    private void initActionBar() {
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setTitle("Create Group");
    }

    private void initArraysOfPermissions() {
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
    }

    private View.OnClickListener groupIconOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            showImagePickDialog();
        }
    };

    private View.OnClickListener createGroupBtnOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            createNewGroup();
        }
    };

    private void createNewGroup() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Creating Group");

        String groupTitle = groupTitleEt.getText().toString().trim();
        String groupDescription = groupDescriptionEt.getText().toString().trim();
        String createdDateTime = "" + System.currentTimeMillis();

        if(TextUtils.isEmpty(groupTitle)){
            Toast.makeText(this, "Group title required...", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.show();

        if(Objects.isNull(image_uri)){
            createGroup(createdDateTime, groupTitle, groupDescription, "");
        }else {
            String fileNameAndPath = "Group_Icon/image" + createdDateTime;
            StorageReference storageReference = FirebaseStorage.getInstance().getReference(fileNameAndPath);
            storageReference.putFile(image_uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful());
                            Uri downloadUri = uriTask.getResult();
                            if(uriTask.isSuccessful()){
                                createGroup(createdDateTime, groupTitle, groupDescription, String.valueOf(downloadUri));
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(CreateGroupActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void createGroup(String createdDateTime, String groupTitle, String groupDescription, String icon) {
        Map<String, String> map = new HashMap<>();
        map.put(Constant.GROUP_TABLE_FIELD.ID, createdDateTime);
        map.put(Constant.GROUP_TABLE_FIELD.TITLE, groupTitle);
        map.put(Constant.GROUP_TABLE_FIELD.DESCRIPTION, groupDescription);
        map.put(Constant.GROUP_TABLE_FIELD.ICON, icon);
        map.put(Constant.GROUP_TABLE_FIELD.CREATED_DATETIME, createdDateTime);
        map.put(Constant.GROUP_TABLE_FIELD.CREATED_BY, firebaseAuth.getUid());

        //create group
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(Constant.TABLE.GROUP);
        databaseReference.child(createdDateTime).setValue(map)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //member info
                        Map<String, String> map = new HashMap<>();
                        map.put(Constant.PARTICIPANTS_FIELD.UID, firebaseAuth.getUid());
                        map.put(Constant.PARTICIPANTS_FIELD.ROLE, Constant.GROUP_MEMBER_ROLE.CREATOR);
                        map.put(Constant.PARTICIPANTS_FIELD.JOIN_TIME, createdDateTime);

                        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference(Constant.TABLE.GROUP);
                        dbRef.child(createdDateTime)
                                .child(Constant.GROUP_TABLE_FIELD.PARTICIPANTS)
                                .child(firebaseAuth.getUid())
                                .setValue(map)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        progressDialog.dismiss();
                                        Toast.makeText(CreateGroupActivity.this, "Group created", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressDialog.dismiss();
                                        Toast.makeText(CreateGroupActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(CreateGroupActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showImagePickDialog() {
        //dialog containing options: Camera/Gallery
        String options[] = {Constant.IMAGE_SOURCE_OPTIONS.CAMERA, Constant.IMAGE_SOURCE_OPTIONS.GALLERY};
        //alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(CreateGroupActivity.this);
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

    private boolean checkStoragePermission() {
        //check if storage permission is enabled or not
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
    }

    private boolean checkCameraPermission() {
        //check if storage permission is enabled or not
        boolean isCameraGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == (PackageManager.PERMISSION_GRANTED);

        boolean isWriteStorageGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);

        return isCameraGranted && isWriteStorageGranted;
    }

    private void requestStoragePermission() {
        //request runtime storage permission
        ActivityCompat.requestPermissions(this, storagePermissions, Constant.REQUEST_CODE.STORAGE_REQUEST_CODE);
    }

    private void requestCameraPermission() {
        //request runtime storage permission
        ActivityCompat.requestPermissions(this, cameraPermissions, Constant.REQUEST_CODE.CAMERA_REQUEST_CODE);
    }

    private void pickFromCamera() {
        //Intent of picking image from device camera
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Temp Pic");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Temp Description");

        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

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
    public boolean onSupportNavigateUp() {
        return super.onSupportNavigateUp();
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
                        Toast.makeText(this, "Please enable camera & storage permission", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(this, "Please enable storage permission", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //called after picking image
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == Constant.REQUEST_CODE.IMAGE_PICK_GALLERY_CODE) {
                image_uri = data.getData();
                groupIconCiv.setImageURI(image_uri);
            }
            if (requestCode == Constant.REQUEST_CODE.IMAGE_PICK_CAMERA_CODE) {
                groupIconCiv.setImageURI(image_uri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}