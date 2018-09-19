package com.waslnyapp.waslny;

import android.annotation.SuppressLint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.waslnyapp.waslny.SavedRecyclerView.SavedAdapter;
import com.waslnyapp.waslny.SavedRecyclerView.SavedObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;


public class SavedActivity extends AppCompatActivity {
    private String customerOrDriver, userId;

    private RecyclerView mHistoryRecyclerView;
    private RecyclerView.Adapter mHistoryAdapter;
    private RecyclerView.LayoutManager mHistoryLayoutManager;

    private TextView mBalance;
    private ImageView Back;

    private Double Balance = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved);

        mBalance = findViewById(R.id.balance);

        mHistoryRecyclerView = (RecyclerView) findViewById(R.id.historyRecyclerView);
        mHistoryRecyclerView.setNestedScrollingEnabled(false); //RecyclerView setup
        mHistoryRecyclerView.setHasFixedSize(true);
        mHistoryLayoutManager = new LinearLayoutManager(SavedActivity.this);
        mHistoryRecyclerView.setLayoutManager(mHistoryLayoutManager);
        mHistoryAdapter = new SavedAdapter(getDataSetHistory(), SavedActivity.this);
        mHistoryRecyclerView.setAdapter(mHistoryAdapter);
        Back = (ImageView) findViewById(R.id.back);

        // reciving data from CustomerMapActivity
        customerOrDriver = getIntent().getExtras().getString("customerOrDriver");
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        getUserHistoryIds(); //get rides that user was part of

        Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                return;
            }
        });


        if(customerOrDriver.equals("Drivers")){ //x
            mBalance.setVisibility(View.VISIBLE);
        }
    }

    //Get Ride Trip Information
    private void getUserHistoryIds() {
        DatabaseReference userHistoryDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(customerOrDriver).child(userId).child("Saved");
        userHistoryDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot history : dataSnapshot.getChildren()){
                        FetchRideInformation(history.getKey());
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void FetchRideInformation(String rideKey) {
        DatabaseReference historyDatabase = FirebaseDatabase.getInstance().getReference().child("Saved").child(rideKey);
        historyDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()){
                    String rideId = dataSnapshot.getKey(); // get the user key
                    Long timestamp = 0L; //the time as defult
                    String distance = "";
                    String distanation = "";
                    Double ridePrice = 0.0;


                    // get the timestamp if isnot null
                    if(dataSnapshot.child("timestamp").getValue() != null){
                        timestamp = Long.valueOf(dataSnapshot.child("timestamp").getValue().toString());
                    }

                    if(dataSnapshot.child("destination").getValue() != null){
                        distanation = dataSnapshot.child("destination").getValue().toString();
                    }

                    if(dataSnapshot.child("distance").getValue() != null){
                            distance = dataSnapshot.child("distance").getValue().toString();
                            ridePrice = (Double.valueOf(distance) * 0.4);
                            Balance += ridePrice;
                            mBalance.setText("Balance: " + String.valueOf(Balance) + " $");
                        }


                    // get the ride key and date from SavedObject class
                    SavedObject obj = new SavedObject(rideId, getDate(timestamp),distanation);
                    resultsHistory.add(obj);
                    mHistoryAdapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    // convert timestamp in database to date
    private String getDate(Long time) {
        Calendar cal = Calendar.getInstance(Locale.getDefault());
        cal.setTimeInMillis(time*1000);
        String date = DateFormat.format("MM-dd-yyyy hh:mm", cal).toString();
        return date;
    }

    // save SavedObject (rideid and time) in array called resultsHistory 0 = rideid ,  1 = time
    private ArrayList resultsHistory = new ArrayList<SavedObject>(); // init
    private ArrayList<SavedObject> getDataSetHistory() {
        return resultsHistory;
    }

}
