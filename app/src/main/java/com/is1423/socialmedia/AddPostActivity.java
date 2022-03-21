package com.is1423.socialmedia;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.is1423.socialmedia.common.Constant;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AddPostActivity extends AppCompatActivity {
    ActionBar actionBar;
    FirebaseAuth firebaseAuth;
    FirebaseUser currentUser;
    DatabaseReference userDbRef;

    //view
    EditText titleEt, descriptionEt;
    ImageView imageIv;
    Button uploadBtn;

    //permissions array
    String[] cameraPermissions;
    String[] storagePermissions;

    Uri image_uri = null;

    //user info
    String name, email, uid, userImage;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        actionBar = getSupportActionBar();
        actionBar.setTitle("Add New Post");
        //enable back button in actionBar
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        initPermissionsArrays();

        progressDialog = new ProgressDialog(this);

        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        checkUserStatus();

        actionBar.setSubtitle(email);

        getCurrentUserInfo();

        initViews();

        imageIv.setOnClickListener(onImageIvClickedListener);

        uploadBtn.setOnClickListener(onUploadBtnClickedListener);
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkUserStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkUserStatus();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.action_add_post).setVisible(false);
        menu.findItem(R.id.action_search).setVisible(false);
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

    private void checkUserStatus() {
        //get current user
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            //user signed in => stay here
            email = user.getEmail();
            uid = user.getUid();
        } else {
            //user not signed in, go main
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    private void updateOnlineStatus(String status) {
        String uid = currentUser.getUid();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constant.TABLE.USER).child(uid);
        Map<String, Object> map = new HashMap<>();
        map.put(Constant.USER_TABLE_FIELD.ONLINE_STATUS, status);
        reference.updateChildren(map);
    }

    private void initPermissionsArrays() {
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
    }

    private void getCurrentUserInfo() {
        userDbRef = FirebaseDatabase.getInstance().getReference(Constant.TABLE.USER);
        Query query = userDbRef.orderByChild(Constant.USER_TABLE_FIELD.EMAIL).equalTo(email);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    name = "" + ds.child(Constant.USER_TABLE_FIELD.NAME).getValue();
                    email = "" + ds.child(Constant.USER_TABLE_FIELD.EMAIL).getValue();
                    userImage = "" + ds.child(Constant.USER_TABLE_FIELD.IMAGE).getValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void initViews() {
        titleEt = findViewById(R.id.postTitleEt);
        descriptionEt = findViewById(R.id.postDescriptionEt);
        imageIv = findViewById(R.id.postImageIv);
        uploadBtn = findViewById(R.id.postUploadBtn);
    }

    View.OnClickListener onImageIvClickedListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            showImagePickDialog();
        }
    };

    View.OnClickListener onUploadBtnClickedListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String title = titleEt.getText().toString().trim();
            String description = descriptionEt.getText().toString().trim();
            if (TextUtils.isEmpty(title)) {
                Toast.makeText(AddPostActivity.this, "Title required...", Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(description)) {
                Toast.makeText(AddPostActivity.this, "Description required...", Toast.LENGTH_SHORT).show();
            }
            if (Objects.isNull(image_uri)) {
                uploadData(title, description, "noImage");
            } else {
                uploadData(title, description, String.valueOf(image_uri));
            }
        }
    };

    private void uploadData(String title, String description, String image_uri) {
        progressDialog.setMessage("Posting...");
        progressDialog.show();

        String now = String.valueOf(System.currentTimeMillis());
        String filePathAndName = "Posts/" + "post_" + now;

        if (!image_uri.equals("noImage")) {
            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(filePathAndName);
            storageReference.putFile(Uri.parse(image_uri))
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful()) ;
                            String downloadUri = uriTask.getResult().toString();
                            if (uriTask.isSuccessful()) {
                                savePost(now, title, description,downloadUri);
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(AddPostActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            savePost(now, title, description, "noImage");
        }
    }

    private void savePost(String now, String title, String description, String downloadUri){
        Map<Object, String> map = new HashMap<>();
        map.put(Constant.POSTING_TABLE_FIELD.UID, uid);
        map.put(Constant.POSTING_TABLE_FIELD.USER_NAME, name);
        map.put(Constant.POSTING_TABLE_FIELD.USER_EMAIL, email);
        map.put(Constant.POSTING_TABLE_FIELD.USER_IMAGE, userImage);
        map.put(Constant.POSTING_TABLE_FIELD.POST_ID, now);
        map.put(Constant.POSTING_TABLE_FIELD.TITLE, title);
        map.put(Constant.POSTING_TABLE_FIELD.DESCRIPTION, description);
        map.put(Constant.POSTING_TABLE_FIELD.POST_IMAGE, downloadUri);
        map.put(Constant.POSTING_TABLE_FIELD.POST_DATETIME, now);

        DatabaseReference postDbRef = FirebaseDatabase.getInstance().getReference(Constant.TABLE.POST);
        postDbRef.child(now).setValue(map)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        Toast.makeText(AddPostActivity.this, "Post published", Toast.LENGTH_SHORT).show();
                        titleEt.setText("");
                        descriptionEt.setText("");
                        imageIv.setImageURI(null);
                        image_uri=null;
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(AddPostActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
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

    private void pickFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, Constant.REQUEST_CODE.IMAGE_PICK_GALLERY_CODE);
    }

    private void pickFromCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Temp Pic");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Temp Description");

        image_uri = this.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(cameraIntent, Constant.REQUEST_CODE.IMAGE_PICK_CAMERA_CODE);
    }

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
                image_uri = data.getData();
                imageIv.setImageURI(image_uri);
            } else if (requestCode == Constant.REQUEST_CODE.IMAGE_PICK_CAMERA_CODE) {
                imageIv.setImageURI(image_uri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}