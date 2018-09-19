package com.waslnyapp.waslny.driver;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.waslnyapp.waslny.R;
import com.waslnyapp.waslny.customer.CustomerMapActivity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CallDriver extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap Map;
    Location LastLocation;
    LocationRequest LocationClienRequest;
    private FusedLocationProviderClient mFusedLocationClient;

    private TextView tvDriverName, tvDriverPhone, tvDriverCarName;
    private ImageView DriverProfilePhoto;
    private Button btn_CallByApp, btn_CallByPhone,btn_CancelReq;

    private LatLng pickupClientLocation;
    private Boolean requestBol = false;
    private Marker pickupClientMarker;
    private SupportMapFragment mapFragment;

    String destination, requestService,driverid;
    private LatLng destinationLatLng;

    private DatabaseReference driveHasEndedRef;
    private ValueEventListener driveHasEndedRefListener;

    private DatabaseReference DriverReference;
    private FirebaseAuth FirebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_call);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        destinationLatLng = new LatLng(0.0,0.0);

        DriverProfilePhoto = (ImageView) findViewById(R.id.driverProfileImage);
        tvDriverName = (TextView) findViewById(R.id.driverName);
        tvDriverPhone = (TextView) findViewById(R.id.driverPhone);
        tvDriverCarName = (TextView) findViewById(R.id.driverCar);

        /*FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        driverid = FirebaseAuth.getUid();*/

        btn_CallByApp = (Button) findViewById(R.id.callapp);
        btn_CallByPhone = (Button) findViewById(R.id.callphone);
        btn_CancelReq = (Button) findViewById(R.id.cancelreq);

        // when user click waslny call button
        btn_CallByApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(driverid != null && !driverid.isEmpty()){

                    DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverid).child("customerRequest");
                    String customerId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    HashMap map = new HashMap();
                    map.put("customerRideId", customerId);// put current user id in map
                    map.put("Destination", destination); // put the destination request from auto complete place
                    map.put("destinationLat", destinationLatLng.latitude);
                    map.put("destinationLng", destinationLatLng.longitude);
                    driverRef.updateChildren(map);// make change and update in database

                    /*getHasRideEnded();*/ // cancel customer request when the customer click cancel
                    btn_CancelReq.setVisibility(View.VISIBLE);
                    btn_CallByApp.setVisibility(View.GONE);
                    /*btn_CallByApp.setText("Cancle Request");*/// change call text when click button to Looking for Driver Location

                }

            }
        });


        btn_CancelReq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*btn_CancelReq.setText("Call by App");*/
                btn_CancelReq.setVisibility(View.GONE);
                btn_CallByApp.setVisibility(View.VISIBLE);

                if(driverid != null && !driverid.isEmpty()){

                    DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverid).child("customerRequest");
                    driverRef.removeValue();
            }

            }
        });

        //when click Call By Phone Buttin
        btn_CallByPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent callIntent=new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:"+tvDriverPhone.getText().toString()));
                startActivity(callIntent);
            }
        });

        //get intent to get driver info when press onit
        if(getIntent() != null){

            driverid = getIntent().getStringExtra("driverId");

                        getDriverInfo(driverid);

        }

    }

    private String driverFoundID;
    private void getDriverInfo(final String driverid) {
        DatabaseReference mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverid)/*.child(driverFoundID)*/;
        mCustomerDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0) {

                    if (dataSnapshot.child("Name") != null) {
                        tvDriverName.setText(dataSnapshot.child("Name").getValue().toString());
                    }
                    if (dataSnapshot.child("Phone") != null) {
                        tvDriverPhone.setText(dataSnapshot.child("Phone").getValue().toString());
                    }
                    if (dataSnapshot.child("CarName") != null) {
                        tvDriverCarName.setText(dataSnapshot.child("CarName").getValue().toString());
                    }
                    if (dataSnapshot.child("ProfilePhotoPath").getValue() != null) {
                        Glide.with(getApplication()).load(dataSnapshot.child("ProfilePhotoPath").getValue().toString()).into(DriverProfilePhoto);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    GeoQuery geoQuery;
    private DatabaseReference driverLocationRef;
    private ValueEventListener driverLocationRefListener;
    private void endRide(final String driverid){
        // remove the user request listener so remove user request node from database
        geoQuery.removeAllListeners();
        driverLocationRef.removeEventListener(driverLocationRefListener);
        driveHasEndedRef.removeEventListener(driveHasEndedRefListener);
        // remove the user request from drivers node in database
        if (driverid != null){
            DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverid).child("customerRequest");
            driverRef.removeValue();
            driverFoundID = null;
        }

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("customerRequest");
        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(userId);
        // remove the pickup user request
        if(pickupClientMarker != null){
            pickupClientMarker.remove();
        }

        btn_CallByApp.setText("Call By App");
        // remove the Driver Marker in user map

        tvDriverName.setText("");
        tvDriverPhone.setText("");
        tvDriverCarName.setText("");
        DriverProfilePhoto.setImageResource(R.mipmap.ic_default_user);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Map = googleMap;

        LocationClienRequest = new LocationRequest();
        LocationClienRequest.setInterval(1000);
        LocationClienRequest.setFastestInterval(1000);
        LocationClienRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
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
                                ActivityCompat.requestPermissions(CallDriver.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                            }
                        })
                        .create()
                        .show();
            }
            else{
                ActivityCompat.requestPermissions(CallDriver.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
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



}