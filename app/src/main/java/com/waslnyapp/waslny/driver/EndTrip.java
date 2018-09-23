package com.waslnyapp.waslny.driver;


import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.stepstone.apprating.AppRatingDialog;
import com.stepstone.apprating.listener.RatingDialogListener;
import com.waslnyapp.waslny.PaymentsActivity;
import com.waslnyapp.waslny.R;

import java.util.Arrays;

public class EndTrip extends FragmentActivity implements OnMapReadyCallback,
        RatingDialogListener {

    private GoogleMap Map;
    Location LastLocation;
    LocationRequest LocationClienRequest;

    // Obtain the SupportMapFragment and get notified when the map is ready to be used.

    private FusedLocationProviderClient mFusedLocationClient ;

    String driverId;
    private LatLng destinationLatLng;

    private DatabaseReference driverRatingoDb;
    private DatabaseReference historyRideInfoDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end_trip);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        driverId = getIntent().getExtras().getString("driverFoundID");
        destinationLatLng = new LatLng(0.0,0.0);
        driverRatingoDb = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers");
        //historyRideInfoDb = FirebaseDatabase.getInstance().getReference().child("Rating").child(rideId);

        //RatingBar ratingBar = findViewById(R.id.ratingBar);

        Button btn_pay = findViewById(R.id.pay);
        Button btn_rate = findViewById(R.id.rating);

        // when user click Waslny call button
        btn_pay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EndTrip.this, PaymentsActivity.class);
                startActivity(intent);
                finish();
            }
        });

        //when click Call By Phone Button
        btn_rate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                setDriverRating();

            }
        });
    }


    private void setDriverRating() {

        new AppRatingDialog.Builder()
                .setPositiveButtonText("Rate")
                .setNegativeButtonText("Cancel")
                .setDefaultRating(1)
                .setNoteDescriptions(Arrays.asList("Vary bad","Not good","good","Vary Good","Excellent"))
                .setTitle("Rate Driver")
                .setDescription("Please select some stars and give your feedback")
                .setTitleTextColor(R.color.colorPrimary)
                .setNoteDescriptionTextColor(R.color.colorPrimary)
                .setHint("Write feedback ..")
                .setHintTextColor(R.color.white)
                .setCommentTextColor(R.color.white)
                .setCommentBackgroundColor(R.color.colorPrimary)
                .setWindowAnimation(R.style.RatingDialogFadeAnim)
                .create(EndTrip.this)
                .show();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Map = googleMap;

        LocationClienRequest = new LocationRequest();
        LocationClienRequest.setInterval(1000);
        LocationClienRequest.setFastestInterval(1000);
        LocationClienRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){

            }else{
                checkLocationPermission();
            }
        }

        mFusedLocationClient.requestLocationUpdates(LocationClienRequest, mLocationCallback, Looper.myLooper());
        Map.setMyLocationEnabled(true);
    }

    LocationCallback mLocationCallback = new LocationCallback(){
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for(Location location : locationResult.getLocations()){
                if(getApplicationContext()!=null){
                    LastLocation = location;

                    LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());

                    Map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    Map.animateCamera(CameraUpdateFactory.zoomTo(11));

                }
            }
        }
    };

    private void checkLocationPermission() {
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                new android.app.AlertDialog.Builder(this)
                        .setTitle("give permission")
                        .setMessage("give permission message")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(EndTrip.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                            }
                        })
                        .create()
                        .show();
            }
            else{
                ActivityCompat.requestPermissions(EndTrip.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode){
            // If request is cancelled, the result arrays are empty.
            case 1:{
                if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                        mFusedLocationClient.requestLocationUpdates(LocationClienRequest, mLocationCallback, Looper.myLooper());
                        Map.setMyLocationEnabled(true);
                    }
                } else{
                    Toast.makeText(getApplicationContext(), "Please provide the permission", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }


    @Override
    public void onNegativeButtonClicked() {

    }

    @Override
    public void onNeutralButtonClicked() {

    }

    @Override
    public void onPositiveButtonClicked(int rating, String s) {

        driverId = driverRatingoDb.getKey();
        driverRatingoDb.child(driverId).child("rating").setValue(rating);

        Toast.makeText(this, "Thank you for Rate", Toast.LENGTH_SHORT).show();



    }
}