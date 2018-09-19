package com.waslnyapp.waslny.driver;

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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

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

public class DriverProfile extends AppCompatActivity {

    private EditText EdtName, EdtPhone, EdtCar;
    private Button  btnSave;
    private ImageView ProfilePhoto ,Back;
    private RadioGroup RadioGroup;

    private String userKey , Name , Phone , CarName , Service , ProfileImageUrl;

    private DatabaseReference DriverReference;
    private FirebaseAuth FirebaseAuth;

    private Uri resultUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_profile);

        EdtName = (EditText) findViewById(R.id.name);
        EdtPhone = (EditText) findViewById(R.id.phone);
        EdtCar = (EditText) findViewById(R.id.car);
        ProfilePhoto = (ImageView) findViewById(R.id.profileImage);
        Back = (ImageView) findViewById(R.id.back);

        RadioGroup = (RadioGroup) findViewById(R.id.radioGroup);

        btnSave = (Button) findViewById(R.id.save);

        FirebaseAuth = FirebaseAuth.getInstance();
        userKey = FirebaseAuth.getCurrentUser().getUid();
        DriverReference = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(userKey);

        getUserInfo();

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
                saveUserInformation();
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
    private void getUserInfo(){
        DriverReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if(map.get("Name")!=null){
                        Name = map.get("Name").toString();
                        EdtName.setText(Name);
                    }
                    if(map.get("Phone")!=null){
                        Phone = map.get("Phone").toString();
                        EdtPhone.setText(Phone);
                    }
                    if(map.get("CarName")!=null){
                        CarName = map.get("CarName").toString();
                        EdtCar.setText(CarName);
                    }
                    if(map.get("Service")!=null){
                        Service = map.get("Service").toString();
                        switch (Service){
                            case"WasX":
                                RadioGroup.check(R.id.WasX);
                                break;
                            case"Was2x":
                                RadioGroup.check(R.id.Was2x);
                                break;
                            case"Was3x":
                                RadioGroup.check(R.id.Was3X);
                                break;
                        }
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



    private void saveUserInformation() {
        Name = EdtName.getText().toString();
        Phone = EdtPhone.getText().toString();
        CarName = EdtCar.getText().toString();

        int selectId = RadioGroup.getCheckedRadioButtonId();

        final RadioButton radioButton = (RadioButton) findViewById(selectId);

        if (radioButton.getText() == null){
            return;
        }

        if (Name.equals("") || Phone.equals("") || CarName.equals("") || radioButton.equals("") ) {
            Toast.makeText(getApplicationContext(), "Must Fill Out All Fields", Toast.LENGTH_LONG).show();
        }else {
            Service = radioButton.getText().toString();
            Map userInfo = new HashMap();
            userInfo.put("Name", Name);
            userInfo.put("Phone", Phone);
            userInfo.put("CarName", CarName);
            userInfo.put("Service", Service);
            DriverReference.updateChildren(userInfo);

            if (resultUri != null) {

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
                        DriverReference.updateChildren(newImage);

                        finish();
                        return;
                    }
                });
            } else {
                finish();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && resultCode == Activity.RESULT_OK){
            final Uri imageUri = data.getData();
            resultUri = imageUri;
            ProfilePhoto.setImageURI(resultUri);
        }
    }
}
