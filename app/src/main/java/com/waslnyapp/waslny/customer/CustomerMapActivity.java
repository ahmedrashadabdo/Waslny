package com.waslnyapp.waslny.customer;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.github.clans.fab.FloatingActionButton;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.stepstone.apprating.AppRatingDialog;
import com.stepstone.apprating.listener.RatingDialogListener;
import com.waslnyapp.waslny.PaymentsActivity;
import com.waslnyapp.waslny.SavedActivity;
import com.waslnyapp.waslny.HomeActivity;
import com.waslnyapp.waslny.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CustomerMapActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnInfoWindowClickListener, RatingDialogListener {
    Context context = CustomerMapActivity.this;
    private final static String TAG = "CustomerMapActivity";

    private GoogleMap Map;
    Location LastLocation;
    LocationRequest LocationClienRequest;
    private FusedLocationProviderClient mFusedLocationClient;
    private FloatingActionButton btn_Profile, btn_Saved, btn_Logout;
    private Button btn_CallRequest;
    String DID;
    RelativeLayout rlSearchPlace;
    private LatLng pickupClientLocation;
    private Boolean requestBol = false;
    private Marker pickupClientMarker;
    private SupportMapFragment mapFragment;
    private String destination, requestService;
    private LatLng destinationLatLng;
    private LinearLayout showDriverInfo;
    private LinearLayout endtrip;
    private ImageView driverProfileImage;
    private TextView driverName, driverPhone, driverCar;
    private RadioGroup RadioGroup;
    private RatingBar RatingBar;
    private Button btn_CallByApp, btn_CallByPhone,btn_CancelReq;
    private Button btn_pay,btn_rating, btn_exit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_costumer_map);
        Log.d(TAG,"onCreate start");
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        destinationLatLng = new LatLng(0.0,0.0);

        //Driver Information layout
        showDriverInfo = findViewById(R.id.driverInfo);
        btn_CallByApp = findViewById(R.id.callapp);
        btn_CallByPhone = findViewById(R.id.callphone);
        btn_CancelReq = findViewById(R.id.cancelreq);
        driverProfileImage = findViewById(R.id.driverProfileImage);
        driverName = findViewById(R.id.driverName);
        driverPhone = findViewById(R.id.driverPhone);
        driverCar = findViewById(R.id.driverCar);
        RatingBar = findViewById(R.id.ratingBar);

        RadioGroup = findViewById(R.id.radioGroup);
        RadioGroup.check(R.id.UberX);

        //Floating Action Buttons
        btn_Profile = findViewById(R.id.profile);
        btn_Saved = findViewById(R.id.saved);
        btn_Logout = findViewById(R.id.logout);

        //endtrip layout
        endtrip = findViewById(R.id.endtrip);

        btn_pay = findViewById(R.id.pay);
        btn_rating = findViewById(R.id.rating);
        btn_exit = findViewById(R.id.end);





        RelativeLayout rlSearchPlace = findViewById(R.id.rlSearchPlacement);
        rlSearchPlace.setVisibility (View.VISIBLE);


        btn_CallRequest = findViewById(R.id.request);

        btn_Logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(CustomerMapActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // when user click waslny call button
        btn_CallRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (requestBol){ // if we want to cancel the request
                    endRide();
                }else{
                    int selectId = RadioGroup.getCheckedRadioButtonId();
                    final RadioButton radioButton = findViewById(selectId);
                    if (radioButton.getText() == null){
                        return;
                    }
                    requestService = radioButton.getText().toString();

                    // if we want to call the request
                    requestBol = true;
                    String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("customerRequest");
                    GeoFire geoFire = new GeoFire(ref);
                    geoFire.setLocation(userId, new GeoLocation(LastLocation.getLatitude(), LastLocation.getLongitude()));
                    pickupClientLocation = new LatLng(LastLocation.getLatitude(), LastLocation.getLongitude());
                    pickupClientMarker = Map.addMarker(new MarkerOptions().position(pickupClientLocation).title("Pickup Here").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_pickup)));
                    btn_CallRequest.setText("Getting your Driver....");

                    getClosestDriver(); // find the closest driver to user
                }
            }
        });
        btn_Profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CustomerMapActivity.this, CustomerProfile.class);
                startActivity(intent);
            }
        });

        btn_Saved.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CustomerMapActivity.this, SavedActivity.class);
                intent.putExtra("customerOrDriver", "Customers");
                startActivity(intent);
            }
        });


        //Place Autocomplete Fragment
        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {

            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                destination = place.getName().toString();
                destinationLatLng = place.getLatLng();
            }
            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
            }
        });

    }

    private void getCurrentDriver(String driverFoundID) {
        Log.d(TAG, "getCurrentDriver: "+driverFoundID);


        final String driverId = driverFoundID;
        DatabaseReference mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverFoundID);
        mCustomerDatabase.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverId).child("customerRequest");
                String customerId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                HashMap<String, Object> map = new HashMap<String, Object>();
                map.put("customerRideId", customerId);// put current user id in map
                map.put("Destination", destination); // put the destination request from auto complete place
                map.put("destinationLat", destinationLatLng.latitude);
                map.put("destinationLng", destinationLatLng.longitude);
                driverRef.updateChildren(map);// make change and update in database
                btn_CallRequest.setText("Looking for Driver ");// change call text when click button to Looking for Driver Location

                getDriverAcceptor();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getDriverAcceptor(){
        DatabaseReference driverRefCopy = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(DID);
        driverRefCopy.child("Picked").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) { // first save the destination in map database

                    btn_CallRequest.setText("Driver accept your request");
                    getCurrentDriverLocation(DID);// display driver location in user map
                    getHasRideEnded(); // cancel customer request when the customer click cancel
                    }
                }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private int radius = 1;
    private Boolean driverFound = false;
    private String driverFoundID;

    GeoQuery geoQuery;
    private void getClosestDriver(){ // to find driver for request
        Log.d(TAG, "getClosestDriver ");

        DatabaseReference driverLocation = FirebaseDatabase.getInstance().getReference().child("driversAvailable");
        GeoFire geoFire = new GeoFire(driverLocation);
        geoQuery = geoFire.queryAtLocation(new GeoLocation(pickupClientLocation.latitude, pickupClientLocation.longitude), radius);
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            // case 1 if driver found
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if (!driverFound && requestBol){
                    DatabaseReference mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(key);
                    mCustomerDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                                Map<String, Object> driverMap = (Map<String, Object>) dataSnapshot.getValue();
                                if (driverFound){
                                    return;
                                }

                                if(driverMap.get("Service").equals(requestService)){
                                    driverFound = true;
                                    driverFoundID = dataSnapshot.getKey();

                                    DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverFoundID).child("customerRequest");
                                    String customerId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                    HashMap<String, Object> map = new HashMap<String, Object>();
                                    map.put("customerRideId", customerId);// put current user id in map
                                    map.put("Destination", destination); // put the destination request from auto complete place
                                    map.put("destinationLat", destinationLatLng.latitude);
                                    map.put("destinationLng", destinationLatLng.longitude);
                                    driverRef.updateChildren(map);// make change and update in database

                                    getDriverLocation();// display driver location in user map
                                    getDriverInfo();
                                    getHasRideEnded(); // cancel customer request when the customer click cancel
                                    btn_CallRequest.setText("Looking for Driver Location....");// change call text when click button to Looking for Driver Location
                                }
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }
            // case 2 if driver not found
            @Override
            public void onGeoQueryReady() {
                if (!driverFound)
                {
                    radius++;
                    getClosestDriver();
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    /*-------------------------------------------- Map specific functions -----
    |  Function(s) getDriverLocation
    |
    |  Purpose:  Get's most updated driver location and it's always checking for movements.
    |
    |  Note:
    |	   Even tho we used geofire to push the location of the driver we can use a normal
    |      Listener to get it's location with no problem.
    |
    |      0 -> Latitude
    |      1 -> Longitudde
    |
    *-------------------------------------------------------------------*/
    private Marker mDriverMarker;
    private DatabaseReference driverLocationRef;
    private ValueEventListener driverLocationRefListener;
    //display driver Location on User Map
    private void getDriverLocation(){
        Log.d(TAG, "getDriverLocation ");

        driverLocationRef = FirebaseDatabase.getInstance().getReference().child("driversWorking").child(driverFoundID).child("l");
        driverLocationRefListener = driverLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && requestBol){
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLng = 0;
                    if(map.get(0) != null){
                        locationLat = Double.parseDouble(map.get(0).toString());
                    }
                    if(map.get(1) != null){
                        locationLng = Double.parseDouble(map.get(1).toString());
                    }
                    LatLng driverLatLng = new LatLng(locationLat,locationLng);
                    if(mDriverMarker != null){
                        mDriverMarker.remove();
                    }
                    // distance between driver and pickup user location
                    Location loc1 = new Location("");
                    loc1.setLatitude(pickupClientLocation.latitude);
                    loc1.setLongitude(pickupClientLocation.longitude);

                    Location loc2 = new Location("");
                    loc2.setLatitude(driverLatLng.latitude);
                    loc2.setLongitude(driverLatLng.longitude);

                    float distance = loc1.distanceTo(loc2);

                    if (distance<100){
                        btn_CallRequest.setText("Driver's Here");// to notice when driver arrive
                    }else{
                        btn_CallRequest.setText("Driver Found: " + String.valueOf(distance));
                    }

                    // display the driver in user map
                    mDriverMarker = Map.addMarker(new MarkerOptions().position(driverLatLng).title("your driver").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_car)));
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }

    /*-------------------------------------------- getDriverInfo -----
    |  Function(s) getDriverInfo
    |
    |  Purpose:  Get all the user information that we can get from the user's database.
    |
    |  Note: --
    |
    *-------------------------------------------------------------------*/
    private void getDriverInfo(){
        Log.d(TAG, "getDriverInfo ");

//        showDriverInfo.setVisibility(View.VISIBLE);
        DatabaseReference mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverFoundID);
        mCustomerDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "getDriverInfo onDataChange");
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    Log.d(TAG, "getDriverInfo onDataChange exists & ChildrenCount()>0");

                    if(dataSnapshot.child("Name")!=null){
                        driverName.setText(dataSnapshot.child("Name").getValue().toString());
                    }
                    if(dataSnapshot.child("Phone")!=null){
                        driverPhone.setText(dataSnapshot.child("Phone").getValue().toString());
                    }
                    if(dataSnapshot.child("CarName")!=null){
                        driverCar.setText(dataSnapshot.child("CarName").getValue().toString());
                    }
                    if(dataSnapshot.child("ProfilePhotoPath").getValue()!=null){
                        Glide.with(getApplication()).load(dataSnapshot.child("ProfilePhotoPath").getValue().toString()).into(driverProfileImage);
                    }

                    int ratingSum = 0;
                    float ratingsTotal = 0;
                    float ratingsAvg = 0;
                    for (DataSnapshot child : dataSnapshot.child("rating").getChildren()){
                        ratingSum = ratingSum + Integer.valueOf(child.getValue().toString());
                        ratingsTotal++;
                    }
                    if(ratingsTotal!= 0){
                        ratingsAvg = ratingSum/ratingsTotal;
                        RatingBar.setRating(ratingsAvg);
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    // cancel customer request when the customer click cancell
    private DatabaseReference driveHasEndedRef;
    private ValueEventListener driveHasEndedRefListener;
    private void getHasRideEnded(){
        Log.d(TAG, "getHasRideEnded ");
        DID = driverFoundID;
        driveHasEndedRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverFoundID).child("customerRequest").child("customerRideId");

        driveHasEndedRefListener = driveHasEndedRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "getHasRideEnded onDataChange");

                if(dataSnapshot.exists()){
                    Log.d(TAG, "getHasRideEnded onDataChange exists");

                }else{
                    Log.d(TAG, "getHasRideEnded onDataChange not exists");

                    endRide(); // cancel customer request when the customer click cancel
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }

    private void endRide(){
        Log.d(TAG, "endRide ");

        Toast.makeText(context, "Your trep end", Toast.LENGTH_SHORT).show();
        showDriverInfo.setVisibility(View.GONE);
        endtrip.setVisibility(View.VISIBLE);

        btn_rating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "endRide btn_rating OnClickListener");
                setDriverRating();
            }
        });

        btn_pay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "endRide btn_pay OnClickListener");
                Intent i_pay = new Intent(context, PaymentsActivity.class);
                startActivity(i_pay);
                finish();
            }
        });

        btn_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "endRide btn_exit OnClickListener");

                endtrip.setVisibility(View.GONE);
                requestBol = false;
                driverLocationRef.removeEventListener(driverLocationRefListener);
                driveHasEndedRef.removeEventListener(driveHasEndedRefListener);
                // remove the user request from drivers node in database
                if (driverFoundID != null){
                    DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverFoundID).child("customerRequest");
                    driverRef.removeValue();
                    driverFoundID = null;
                }

                driverFound = false;
                radius = 1;
                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("customerRequest");
                GeoFire geoFire = new GeoFire(ref);
                geoFire.removeLocation(userId);
                btn_CallRequest.setText("Call Waslny");
                driverName.setText("");
                driverPhone.setText("");
                driverCar.setText("");
                driverProfileImage.setImageResource(R.mipmap.ic_default_user);
            }
        });

    }

    /*-------------------------------------------- Map specific functions -----
    |  Function(s) onMapReady, buildGoogleApiClient, onLocationChanged, onConnected
    |
    |  Purpose:  Find and update user's location.
    |
    |  Note:
    |	   The update interval is set to 1000Ms and the accuracy is set to PRIORITY_HIGH_ACCURACY,
    |      If you're having trouble with battery draining too fast then change these to lower values
    |
    |
    *-------------------------------------------------------------------*/
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady");

        Map = googleMap;
        LocationClienRequest = new LocationRequest();
        LocationClienRequest.setInterval(1000);
        LocationClienRequest.setFastestInterval(1000);
        LocationClienRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        Map.setOnInfoWindowClickListener(this);
        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){

            }else{
                checkLocationPermission();
            }
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

                        if(!getDriversAroundStarted)
                            getDriversAround();
                    }
                }
            }
        };
        mFusedLocationClient.requestLocationUpdates(LocationClienRequest, mLocationCallback, Looper.myLooper());
        Map.setMyLocationEnabled(true);

    }

    /*-------------------------------------------- onRequestPermissionsResult -----
    |  Function onRequestPermissionsResult
    |
    |  Purpose:  Get permissions for our app if they didn't previously exist.
    |
    |  Note:
    |	requestCode: the nubmer assigned to the request that we've made. Each
    |                request has it's own unique request code.
    |
    *-------------------------------------------------------------------*/
    private void checkLocationPermission() {
        Log.d(TAG, "checkLocationPermission");

        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                new android.app.AlertDialog.Builder(this)
                        .setTitle("give permission")
                        .setMessage("give permission message")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(CustomerMapActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                            }
                        })
                        .create()
                        .show();
            }
            else{
                ActivityCompat.requestPermissions(CustomerMapActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "onRequestPermissionsResult");

        switch(requestCode){
            // If request is cancelled, the result arrays are empty.
            case 1:{
                if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                        LocationCallback mLocationCallback = new LocationCallback(){
                            @Override
                            public void onLocationResult(LocationResult locationResult) {
                                for(Location location : locationResult.getLocations()){
                                    if(getApplicationContext()!=null){
                                        LastLocation = location;
                                        LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
                                        Map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                                        Map.animateCamera(CameraUpdateFactory.zoomTo(11));

                                        if(!getDriversAroundStarted)
                                            getDriversAround();
                                    }
                                }
                            }
                        };
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

    boolean getDriversAroundStarted = false;
    List<Marker> markers = new ArrayList<Marker>();
    private void getDriversAround(){
        Log.d(TAG, "getDriversAround");
        Map.clear();
        getDriversAroundStarted = true;
        DatabaseReference driverLocation = FirebaseDatabase.getInstance().getReference().child("driversAvailable");
        GeoFire geoFire = new GeoFire(driverLocation);
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(LastLocation.getLongitude(), LastLocation.getLatitude()), 999999999);
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                Log.d(TAG, "getDriversAround onKeyEntered");

                for(Marker markerIt : markers){
                    if(Objects.equals(markerIt.getTag(), key))
                        return;
                }
                //get marker location not change in green color
                LatLng driverLocation = new LatLng(location.latitude, location.longitude);

                Marker mDriverMarker = Map.addMarker(new MarkerOptions()
                        .position(driverLocation)
                        .title("Driver")
                        .snippet(key)
                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_car)));
                mDriverMarker.setTag(key);

                markers.add(mDriverMarker);
            }

            @Override
            public void onKeyExited(String key) {
                Log.d(TAG, "getDriversAround onKeyExited");

                for(Marker markerIt : markers){
                    if(markerIt.getTag().equals(key)){
                        //remove marker location
                        markerIt.remove();
                    }
                }
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                Log.d(TAG, "getDriversAround onKeyMoved");
//
//                for(Marker markerIt : markers){
//                    if(markerIt.getTag().equals(key)){
//                        //update marker location in yellow color
//                        markerIt.setPosition(new LatLng(location.latitude, location.longitude));
//                    }
//                }
            }

            @Override
            public void onGeoQueryReady() {
                Log.d(TAG, "getDriversAround onGeoQueryReady");
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
                Log.d(TAG, "getDriversAround onGeoQueryError");
            }
        });
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        Log.d(TAG, "onInfoWindowClick");
        if (!marker.getTitle().equals("Pickup Here")) ;
        {

            driverFound = false;
            driverFoundID = marker.getSnippet().toString();
            DID = driverFoundID;
            showDriverInfo.setVisibility(View.VISIBLE);
            getDriverInfo();
            btn_CallByPhone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG," btn_CallByPhone");
                    Intent callIntent=new Intent(Intent.ACTION_DIAL);
                    callIntent.setData(Uri.parse("tel:"+ driverPhone.getText().toString()));
                    startActivity(callIntent);
                }
            });

            btn_CallByApp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (requestBol){ // if we want to cancel the request
                        Log.d(TAG, "btn_CallByApp requestBol True");
                        endRide();
                    }else{
                        Log.d(TAG, "btn_CallByApp requestBol False");
                        // if we want to call the request
                        requestBol = true;
                        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("customerRequest");
                        GeoFire geoFire = new GeoFire(ref);
                        geoFire.setLocation(userId, new GeoLocation(LastLocation.getLatitude(), LastLocation.getLongitude()));
                        pickupClientLocation = new LatLng(LastLocation.getLatitude(), LastLocation.getLongitude());
                        pickupClientMarker = Map.addMarker(new MarkerOptions().position(pickupClientLocation).title("Pickup Here").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_pickup)));
                        btn_CallRequest.setText("Getting your Driver....");
                        getCurrentDriver(driverFoundID); // find the closest driver to user
                    }
                }
            });
        }
    }

    private void getCurrentDriverLocation(String driverFoundID) {
        Log.d(TAG,"getCurrentDriverLocation of ("+ driverFoundID +") ");
        DID = driverFoundID;
        driverLocationRef = FirebaseDatabase.getInstance().getReference().child("driversWorking").child(driverFoundID).child("l");
        driverLocationRefListener = driverLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Log.d(TAG,"getCurrentDriverLocation onDataChange dataSnapshot.exists (True)");
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLng = 0;
                    if (map.get(0) != null) {
                        locationLat = Double.parseDouble(map.get(0).toString());
                    }
                    if (map.get(1) != null) {
                        locationLng = Double.parseDouble(map.get(1).toString());
                    }
                    LatLng driverLatLng = new LatLng(locationLat, locationLng);
                    if (mDriverMarker != null) {
                        mDriverMarker.remove();
                    }
                    // distance between driver and pickup user location
                    Location loc1 = new Location("");
                    loc1.setLatitude(pickupClientLocation.latitude);
                    loc1.setLongitude(pickupClientLocation.longitude);

                    Location loc2 = new Location("");
                    loc2.setLatitude(driverLatLng.latitude);
                    loc2.setLongitude(driverLatLng.longitude);

                    float distance = loc1.distanceTo(loc2);

                    if (distance < 100) {
                        btn_CallRequest.setText("Driver's Here");// to notice when driver arrive
                    } else {
                        btn_CallRequest.setText("Driver Found: " + String.valueOf(distance));
                    }

                    // display the driver in user map
                    mDriverMarker = Map.addMarker(new MarkerOptions().position(driverLatLng).title("your driver").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_car)));
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

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
                .create(CustomerMapActivity.this)
                .show();
    }

    @Override
    public void onNegativeButtonClicked() {

    }

    @Override
    public void onNeutralButtonClicked() {

    }

    @Override
    public void onPositiveButtonClicked(int i, String s) {
        DatabaseReference driverRefCopy = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(DID).child("requestId");
        Log.d(TAG,"onPositiveButtonClicked DID = "+DID);
        final int rat =i;
        driverRefCopy.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String v = dataSnapshot.getValue().toString();
                String requestId = v.substring(1,21);
                Log.d(TAG,"onPositiveButtonClicked requestId=("+requestId+ ")");
                DatabaseReference historyRef = FirebaseDatabase.getInstance().getReference().child("Saved");
                HashMap<String, Object> map = new HashMap<String, Object>();
                map.put("rating", rat);
                historyRef.child(requestId).updateChildren(map);
                DatabaseReference driverRefCopy = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(DID);
                driverRefCopy.child("Picked").removeValue();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onBackPressed() {
        Log.d(TAG,"onBackPressed");
        showDriverInfo.setVisibility(View.GONE);
        driverFound=false;
        driverFoundID=null;

    }
}