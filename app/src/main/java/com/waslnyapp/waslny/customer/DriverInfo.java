package com.waslnyapp.waslny.customer;

import android.view.View;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

public class DriverInfo {

    String name;
    String service;
    private com.google.firebase.auth.FirebaseAuth FirebaseAuth;
    private Boolean driverFound = false;
    private String driverFoundID;

    private void getDriverInfo(){
        DatabaseReference mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers");
        mCustomerDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0) {

                    Map<String, Object> driverMap = (Map<String, Object>) dataSnapshot.getValue();
                    if (driverFound) {
                        return;
                    }
                        driverFound = true;
                        driverFoundID = dataSnapshot.getKey();


                        if (dataSnapshot.child("Name") != null) {
                            name = dataSnapshot.child("Name").getValue().toString();
                        }
                        if (dataSnapshot.child("Service") != null) {
                            service = dataSnapshot.child("Service").getValue().toString();
                        }
                    }

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
}

