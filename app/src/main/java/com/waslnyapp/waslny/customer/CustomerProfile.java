package com.waslnyapp.waslny.customer;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.waslnyapp.waslny.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CustomerProfile extends AppCompatActivity {

    private EditText EdtName, EdtPhone;
    private Button btnSave;
    private ImageView ProfilePhoto, Back;

    private String userKey , CustomerName , CustomerPhone , ProfileImageUrl;

    private FirebaseAuth FirebaseAuth;
    private DatabaseReference CustomerReference;

    private Uri resultUri;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_profile);

        EdtName = (EditText) findViewById(R.id.name);
        EdtPhone = (EditText) findViewById(R.id.phone);
        ProfilePhoto = (ImageView) findViewById(R.id.profileImage);
        Back = (ImageView) findViewById(R.id.back);

        btnSave = (Button) findViewById(R.id.save);

        FirebaseAuth = FirebaseAuth.getInstance();
        userKey = FirebaseAuth.getCurrentUser().getUid();
        CustomerReference = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(userKey);

        getUserInfo(); // get user info from database

        ProfilePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, 1);
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserInformation(); // save user info to database
            }
        });

        Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                return;
            }
        });
    }
    private void getUserInfo(){ // get user info from database
        CustomerReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if(map.get("Name")!=null){
                        CustomerName = map.get("Name").toString();
                        EdtName.setText(CustomerName);
                    }
                    if(map.get("Phone")!=null){
                        CustomerPhone = map.get("Phone").toString();
                        EdtPhone.setText(CustomerPhone);
                    }
                    if(map.get("ProfilePhotoPath")!=null){
                        ProfileImageUrl = map.get("ProfilePhotoPath").toString();
                        Glide.with(getApplication()).load(ProfileImageUrl).into(ProfilePhoto);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }



    private void saveUserInformation() { // save user info to database
        CustomerName = EdtName.getText().toString();
        CustomerPhone = EdtPhone.getText().toString();

        Map userInfo = new HashMap();
        userInfo.put("Name", CustomerName);
        userInfo.put("Phone", CustomerPhone);
        CustomerReference.updateChildren(userInfo);

        //save img in storage
        if(resultUri != null) {

            StorageReference filePath = FirebaseStorage.getInstance().getReference().child("profile_images").child(userKey);
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(), resultUri);
            } catch (IOException e) {
                e.printStackTrace();
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos);
            byte[] data = baos.toByteArray();
            UploadTask uploadTask = filePath.putBytes(data);

            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    finish();
                    return;
                }
            });
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();

                    Map newImage = new HashMap();
                    newImage.put("ProfilePhotoPath", downloadUrl.toString());
                    CustomerReference.updateChildren(newImage);

                    finish();
                    return;
                }
            });
        }else{
            finish();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && resultCode == Activity.RESULT_OK){
            final Uri imageUri = data.getData();
            resultUri = imageUri; // save the img
            ProfilePhoto.setImageURI(resultUri);
        }
    }
}
