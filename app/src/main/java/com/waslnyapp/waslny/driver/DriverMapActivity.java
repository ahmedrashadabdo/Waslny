//package com.waslnyapp.waslny.driver;
//
//import android.Manifest;
//import android.content.DialogInterface;
//import android.content.Intent;
//import android.content.pm.PackageManager;
//import android.location.Location;
//import android.os.Build;
//import android.os.Looper;
//import android.support.annotation.NonNull;
//import android.support.v4.app.ActivityCompat;
//import android.support.v4.app.FragmentActivity;
//import android.os.Bundle;
//import android.support.v4.content.ContextCompat;
//import android.support.v7.app.AlertDialog;
//import android.view.View;
//import android.widget.Button;
//import android.widget.CompoundButton;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.Switch;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.bumptech.glide.Glide;
//import com.directions.route.AbstractRouting;
//import com.directions.route.Route;
//import com.directions.route.RouteException;
//import com.directions.route.Routing;
//import com.directions.route.RoutingListener;
//import com.firebase.geofire.GeoFire;
//import com.firebase.geofire.GeoLocation;
//import com.github.clans.fab.FloatingActionButton;
//import com.github.clans.fab.FloatingActionMenu;
//import com.google.android.gms.location.FusedLocationProviderClient;
//import com.google.android.gms.location.LocationCallback;
//import com.google.android.gms.location.LocationRequest;
//import com.google.android.gms.location.LocationResult;
//import com.google.android.gms.location.LocationServices;
//import com.google.android.gms.maps.CameraUpdateFactory;
//import com.google.android.gms.maps.GoogleMap;
//import com.google.android.gms.maps.OnMapReadyCallback;
//import com.google.android.gms.maps.SupportMapFragment;
//import com.google.android.gms.maps.model.BitmapDescriptorFactory;
//import com.google.android.gms.maps.model.LatLng;
//import com.google.android.gms.maps.model.Marker;
//import com.google.android.gms.maps.model.MarkerOptions;
//import com.google.android.gms.maps.model.Polyline;
//import com.google.android.gms.maps.model.PolylineOptions;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.database.DataSnapshot;
//import com.google.firebase.database.DatabaseError;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.database.ValueEventListener;
//import com.waslnyapp.waslny.SavedActivity;
//import com.waslnyapp.waslny.HomeActivity;
//import com.waslnyapp.waslny.R;
//import com.waslnyapp.waslny.customer.CustomerMapActivity;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//public class DriverMapActivity extends FragmentActivity implements OnMapReadyCallback, RoutingListener {
//
//    private GoogleMap Map;
//    Location LastLocation;
//    LocationRequest LocationDriverRequest;
//
//    private FusedLocationProviderClient mFusedLocationClient;
//
//    private FloatingActionMenu menuDown;
//    private FloatingActionButton btn_Profile, btn_Logout,btn_Saved;
//    private Button  btn_RideStatus,btn_RideReject;
//
//    private Switch DriverWorking;
//
//    private int status = 0;
//
//    private String customerId = "", destination;
//    private LatLng destinationLatLng, pickupLatLng;
//    private float rideDistance;
//
//    private Boolean isLoggingOut = false;
//
//    private SupportMapFragment mapFragment;
//
//    private LinearLayout showCustomerInfo;
//
//    private ImageView CustomerProfilePhoto;
//
//    private TextView tvCustomerName, tvCustomerPhone, tvCustomerDestination;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_driver_map);
//        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
//        polylines = new ArrayList<>();
//
//
//        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
//
//        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
//        mapFragment.getMapAsync(this);
//
//        menuDown = findViewById(R.id.menu_down);
//
//        showCustomerInfo = findViewById(R.id.customerInfo);
//
//        CustomerProfilePhoto = findViewById(R.id.customerProfileImage);
//
//        tvCustomerName = findViewById(R.id.customerName);
//        tvCustomerPhone = findViewById(R.id.customerPhone);
//        tvCustomerDestination = findViewById(R.id.customerDestination);
//
//        DriverWorking = findViewById(R.id.workingSwitch);
//        DriverWorking.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                if (isChecked){
//                    connectDriver();
//                }else{
//                    disconnectDriver();
//                }
//            }
//        });
//
//        btn_Profile = findViewById(R.id.profile);
//        btn_Logout = findViewById(R.id.logout);
//        btn_Saved = findViewById(R.id.saved);
//        btn_RideStatus = findViewById(R.id.rideStatus);
//        btn_RideStatus.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                switch(status){
//                    case 1:
//                        status=2;
//                        erasePolylines();
//                        if(destinationLatLng.latitude!=0.0 && destinationLatLng.longitude!=0.0){
//                            getRouteToMarker(destinationLatLng);
//                        }
//                        btn_RideStatus.setText("Drive Completed");
//
//                        break;
//                    case 2:
//                        recordRide();
//                        endRide();
//                        break;
//                }
//            }
//        });
//
//        btn_RideReject = findViewById(R.id.reject);
//        btn_RideReject.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
//                DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(userId).child("customerRequest");
//                driverRef.removeValue();
//
//                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("customerRequest");
//                GeoFire geoFire = new GeoFire(ref);
//                geoFire.removeLocation(customerId);
//                customerId="";
//                rideDistance = 0;
//
//                if(pickupMarker != null){
//                    pickupMarker.remove();
//                }
//                if (assignedCustomerPickupLocationRefListener != null){
//                    assignedCustomerPickupLocationRef.removeEventListener(assignedCustomerPickupLocationRefListener);
//                }
//                showCustomerInfo.setVisibility(View.GONE);
//                tvCustomerName.setText("");
//                tvCustomerPhone.setText("");
//                tvCustomerDestination.setText("");
//                CustomerProfilePhoto.setImageResource(R.mipmap.ic_default_user);
//            }
//        });
//
//        btn_Logout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                isLoggingOut = true;
//
//                disconnectDriver();
//
//                FirebaseAuth.getInstance().signOut();
//                Intent intent = new Intent(DriverMapActivity.this, HomeActivity.class);
//                startActivity(intent);
//                finish();
//                return;
//            }
//        });
//        btn_Profile.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(DriverMapActivity.this, EndTrip.class);
//                startActivity(intent);
//                return;
//            }
//        });
//        btn_Saved.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(DriverMapActivity.this, SavedActivity.class);
//                intent.putExtra("customerOrDriver", "Drivers");
//                startActivity(intent);
//                return;
//            }
//        });
//        getAssignedCustomer();
//    }
//    //get request user id to driver
//    private void getAssignedCustomer(){
//        String driverId = FirebaseAuth.getInstance().getCurrentUser().getUid();
//        DatabaseReference assignedCustomerRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverId).child("customerRequest").child("customerRideId");
//        assignedCustomerRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                if(dataSnapshot.exists()){
//                    status = 1;
//                    customerId = dataSnapshot.getValue().toString();
//                    getAssignedCustomerPickupLocation(); // get assgin user pickup location
//                    getAssignedCustomerDestination();
//                    getAssignedCustomerInfo(); //get assgin user Information On Driver's Activity
//                }
//                // driver canceled request notice
//                else{
//                    endRide();
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//            }
//        });
//    }
//
//    Marker pickupMarker;
//    private DatabaseReference assignedCustomerPickupLocationRef;
//    private ValueEventListener assignedCustomerPickupLocationRefListener;
//    // get assgin user pickup location
//    private void getAssignedCustomerPickupLocation(){
//        String driverId = FirebaseAuth.getInstance().getCurrentUser().getUid();
//        assignedCustomerPickupLocationRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverId).child("customerRequest").child("l");
//        assignedCustomerPickupLocationRefListener = assignedCustomerPickupLocationRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                if(dataSnapshot.exists() && !customerId.equals("")){
//                    List<Object> map = (List<Object>) dataSnapshot.getValue();
//                    double locationLat = 0;
//                    double locationLng = 0;
//                    if(map.get(0) != null){
//                        locationLat = Double.parseDouble(map.get(0).toString());
//                    }
//                    if(map.get(1) != null){
//                        locationLng = Double.parseDouble(map.get(1).toString());
//                    }
//                    pickupLatLng = new LatLng(locationLat,locationLng);
//                    pickupMarker = Map.addMarker(new MarkerOptions().position(pickupLatLng).title("pickup location").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_pickup)));
//                    getRouteToMarker(pickupLatLng);
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//            }
//        });
//    }
//
//    private void getRouteToMarker(LatLng pickupLatLng) {
//        if (pickupLatLng != null && LastLocation != null){
//            Routing routing = new Routing.Builder()
//                    .travelMode(AbstractRouting.TravelMode.DRIVING)
//                    .withListener(this)
//                    .alternativeRoutes(false)
//                    .waypoints(new LatLng(LastLocation.getLatitude(), LastLocation.getLongitude()), pickupLatLng)
//                    .build();
//            routing.execute();
//        }
//    }
//
//    private void getAssignedCustomerDestination(){
//        String driverId = FirebaseAuth.getInstance().getCurrentUser().getUid();
//        DatabaseReference assignedCustomerRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverId).child("customerRequest");
//        assignedCustomerRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                if(dataSnapshot.exists()) { // first save the destination in map database
//                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
//                    if(map.get("Destination")!=null){ // dispaly the destination in driver screen
//                        destination = map.get("Destination").toString();
//                        tvCustomerDestination.setText(destination);
//                    }
//                    else{
//                        tvCustomerDestination.setText("");
//                    }
//                    // second get the destination
//                    Double destinationLat = 0.0;
//                    Double destinationLng = 0.0;
//                    if(map.get("destinationLat") != null){
//                        destinationLat = Double.valueOf(map.get("destinationLat").toString());
//                    }
//                    if(map.get("destinationLng") != null){
//                        destinationLng = Double.valueOf(map.get("destinationLng").toString());
//                        destinationLatLng = new LatLng(destinationLat, destinationLng);
//                    }
//
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//            }
//        });
//    }
//
//    //get Customer's Information On Driver's Activity
//    private void getAssignedCustomerInfo(){
//        showCustomerInfo.setVisibility(View.VISIBLE);
//        DatabaseReference mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(customerId);
//        mCustomerDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
//                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
//                    if(map.get("Name")!=null){
//                        tvCustomerName.setText(map.get("Name").toString());
//                    }
//                    if(map.get("Phone")!=null){
//                        tvCustomerPhone.setText(map.get("Phone").toString());
//                    }
//                    if(map.get("ProfilePhotoPath")!=null){
//                        Glide.with(getApplication()).load(map.get("ProfilePhotoPath").toString()).into(CustomerProfilePhoto);
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//            }
//        });
//    }
//
//
//    private void endRide(){
//        btn_RideStatus.setText("picked customer"); // when customer picked to driver , the driver picked customer
//        erasePolylines();
//
//        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
//        DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(userId).child("customerRequest");
//        driverRef.removeValue();
//
//        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("customerRequest");
//        GeoFire geoFire = new GeoFire(ref);
//        geoFire.removeLocation(customerId);
//        customerId="";
//        rideDistance = 0;
//
//        if(pickupMarker != null){
//            pickupMarker.remove();
//        }
//        if (assignedCustomerPickupLocationRefListener != null){
//            assignedCustomerPickupLocationRef.removeEventListener(assignedCustomerPickupLocationRefListener);
//        }
//        showCustomerInfo.setVisibility(View.GONE);
//        tvCustomerName.setText("");
//        tvCustomerPhone.setText("");
//        tvCustomerDestination.setText("");
//        CustomerProfilePhoto.setImageResource(R.mipmap.ic_default_user);
//    }
//
//    private void recordRide(){
//        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
//        DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(userId).child("Saved");
//        DatabaseReference customerRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(customerId).child("Saved");
//        DatabaseReference historyRef = FirebaseDatabase.getInstance().getReference().child("Saved");
//        String requestId = historyRef.push().getKey();
//        driverRef.child(requestId).setValue(true);
//        customerRef.child(requestId).setValue(true);
//
//        HashMap map = new HashMap();
//        map.put("driver", userId);  // saving driver in driver
//        map.put("customer", customerId); // saving customerId in customer
//        map.put("rating", 0);
//        map.put("timestamp", getCurrentTimestamp()); // saving timestamp in diver -- > saved --> time
//        map.put("destination", destination);
//        //map.put("location/from/lat", pickupLatLng.latitude);
//        //map.put("location/from/lng", pickupLatLng.longitude);
//        map.put("location/to/lat", destinationLatLng.latitude);
//        map.put("location/to/lng", destinationLatLng.longitude);
//        map.put("distance", rideDistance);
//        historyRef.child(requestId).updateChildren(map);
//    }
//
//    // getting the current time
//    private Long getCurrentTimestamp() {
//        Long timestamp = System.currentTimeMillis()/1000;
//        return timestamp;
//    }
//
//
//    @Override
//    public void onMapReady(GoogleMap googleMap) {
//        Map = googleMap;
//
//        LocationDriverRequest = new LocationRequest();
//        LocationDriverRequest.setInterval(1000); //Refresh map since 1 second
//        LocationDriverRequest.setFastestInterval(1000);
//        LocationDriverRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//
//        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
//            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
//
//            }else{
//                checkLocationPermission();
//            }
//        }
//    }
//
//
//    LocationCallback mLocationCallback = new LocationCallback(){
//        @Override
//        public void onLocationResult(LocationResult locationResult) {
//            for(Location location : locationResult.getLocations()){
//                if(getApplicationContext()!=null){
//
//                    if(!customerId.equals("") && LastLocation!=null && location != null){
//                        rideDistance += LastLocation.distanceTo(location)/1000;
//                    }
//
//
//                    LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
//                    Map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
//                    Map.animateCamera(CameraUpdateFactory.zoomTo(11));
//
//                    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
//                    DatabaseReference refAvailable = FirebaseDatabase.getInstance().getReference("driversAvailable");
//                    DatabaseReference refWorking = FirebaseDatabase.getInstance().getReference("driversWorking");
//                    GeoFire geoFireAvailable = new GeoFire(refAvailable);
//                    GeoFire geoFireWorking = new GeoFire(refWorking);
//
//                    switch (customerId){
//                        case "": // empty = no user assigned pick up call and driver no picked user
//                            geoFireWorking.removeLocation(userId);
//                            geoFireAvailable.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()));
//                            break;
//
//                        default:
//                            geoFireAvailable.removeLocation(userId);
//                            geoFireWorking.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()));
//                            break;
//                    }
//                }
//            }
//        }
//    };
//
//    // check Location Permission to use and display the map
//    private void checkLocationPermission() {
//        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
//            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
//                new AlertDialog.Builder(this)
//                        .setTitle("give permission")
//                        .setMessage("give permission message")
//                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialogInterface, int i) {
//                                ActivityCompat.requestPermissions(DriverMapActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
//                            }
//                        })
//                        .create()
//                        .show();
//            }
//            else{
//                ActivityCompat.requestPermissions(DriverMapActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
//            }
//        }
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        switch(requestCode){
//            case 1:{
//                if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
//                    if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
//                        mFusedLocationClient.requestLocationUpdates(LocationDriverRequest, mLocationCallback, Looper.myLooper());
//                        Map.setMyLocationEnabled(true);
//                    }
//                } else{
//                    Toast.makeText(getApplicationContext(), "Please provide the permission", Toast.LENGTH_LONG).show();
//                }
//                break;
//            }
//        }
//    }
//
//    // when driver
//    private void connectDriver(){
//        checkLocationPermission();
//        mFusedLocationClient.requestLocationUpdates(LocationDriverRequest, mLocationCallback, Looper.myLooper());
//        Map.setMyLocationEnabled(true);
//    }
//
//    // when driver click logout button
//    private void disconnectDriver(){
//        if(mFusedLocationClient != null){
//            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
//        }
//        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
//        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("driversAvailable");
//
//        GeoFire geoFire = new GeoFire(ref);
//        geoFire.removeLocation(userId);
//    }
//
//    private List<Polyline> polylines;
//    private static final int[] COLORS = new int[]{R.color.primary_dark_material_light};
//    @Override
//    public void onRoutingFailure(RouteException e) {
//        if(e != null) {
//            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
//        }else {
//            Toast.makeText(this, "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
//        }
//    }
//    @Override
//    public void onRoutingStart() {
//    }
//    @Override
//    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {
//        if(polylines.size()>0) {
//            for (Polyline poly : polylines) {
//                poly.remove();
//            }
//        }
//
//        polylines = new ArrayList<>();
//        //add route(s) to the map.
//        for (int i = 0; i <route.size(); i++) {
//
//            //In case of more than 5 alternative routes
//            int colorIndex = i % COLORS.length;
//
//            PolylineOptions polyOptions = new PolylineOptions();
//            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
//            polyOptions.width(10 + i * 3);
//            polyOptions.addAll(route.get(i).getPoints());
//            Polyline polyline = Map.addPolyline(polyOptions);
//            polylines.add(polyline);
//
//            Toast.makeText(getApplicationContext(),"Route "+ (i+1) +": distance - "+ route.get(i).getDistanceValue()+": duration - "+ route.get(i).getDurationValue(),Toast.LENGTH_SHORT).show();
//        }
//
//    }
//    @Override
//    public void onRoutingCancelled() {
//    }
//    private void erasePolylines(){
//        for(Polyline line : polylines){
//            line.remove();
//        }
//        polylines.clear();
//    }
//
//    /*private void showDialog(){
//
//        //Code Snippet For Alert Dialog With Action
//        //Set Message and Title
//        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(DriverMapActivity.this);
//        builder.setMessage("Your Trip Has Ended")
//                .setTitle("Message");
//
//        //Set When SEND Button Click
//        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//
//                Toast.makeText(DriverMapActivity.this, "Information Send Successfully!", Toast.LENGTH_SHORT).show();
//            }
//        });
//
//        //Set When Cancel Button Click
//                *//*builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//
//                        //Dismissing the alertDialog
//                        dialogInterface.dismiss();
//                    }
//                });
//*//*
//        android.app.AlertDialog dialog = builder.create();
//        dialog.show();
//
//
//    }*/
//
//}

package com.waslnyapp.waslny.driver;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.waslnyapp.waslny.SavedActivity;
import com.waslnyapp.waslny.HomeActivity;
import com.waslnyapp.waslny.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DriverMapActivity extends FragmentActivity implements OnMapReadyCallback, RoutingListener {

    private GoogleMap Map;
    Location LastLocation;
    LocationRequest LocationDriverRequest;

    private FusedLocationProviderClient mFusedLocationClient;

    private FloatingActionMenu menuDown;
    private FloatingActionButton btn_Profile, btn_Logout,btn_Saved;
    private Button  btn_RideStatus,btn_RideReject;

    private Switch DriverWorking;

    private int status = 0;

    private String customerId = "", destination;
    String userId ;
    private LatLng destinationLatLng, pickupLatLng;
    private float rideDistance;

    private Boolean isLoggingOut = false;

    private SupportMapFragment mapFragment;

    private LinearLayout showCustomerInfo;

    private ImageView CustomerProfilePhoto;

    private TextView tvCustomerName, tvCustomerPhone, tvCustomerDestination;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        polylines = new ArrayList<>();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        menuDown = findViewById(R.id.menu_down);

        showCustomerInfo = findViewById(R.id.customerInfo);

        CustomerProfilePhoto = findViewById(R.id.customerProfileImage);

        tvCustomerName = findViewById(R.id.customerName);
        tvCustomerPhone = findViewById(R.id.customerPhone);
        tvCustomerDestination = findViewById(R.id.customerDestination);

        DriverWorking = findViewById(R.id.workingSwitch);
        DriverWorking.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    connectDriver();
                }else{
                    disconnectDriver();
                }
            }
        });

        btn_Profile = findViewById(R.id.profile);
        btn_Logout = findViewById(R.id.logout);
        btn_Saved = findViewById(R.id.saved);
        btn_RideStatus = findViewById(R.id.rideStatus);
        btn_RideStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch(status){
                    case 1:
                        status=2;
                        erasePolylines();
                        if(destinationLatLng.latitude!=0.0 && destinationLatLng.longitude!=0.0){
                            getRouteToMarker(destinationLatLng);
                        }

                        DatabaseReference driverRefCopy = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(userId);
                        driverRefCopy.child("Picked").setValue("true");
                        btn_RideStatus.setText("Drive Completed");

                        break;
                    case 2:
                        recordRide();
                        endRide();
                        break;
                }
            }
        });

        btn_RideReject = findViewById(R.id.reject);
        btn_RideReject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(userId).child("customerRequest");
                driverRef.removeValue();
                DatabaseReference driverRefCopy = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(userId);
                driverRefCopy.child("Picked").removeValue();

                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("customerRequest");
                GeoFire geoFire = new GeoFire(ref);
                geoFire.removeLocation(customerId);
                customerId="";
                rideDistance = 0;

                if(pickupMarker != null){
                    pickupMarker.remove();
                }
                if (assignedCustomerPickupLocationRefListener != null){
                    assignedCustomerPickupLocationRef.removeEventListener(assignedCustomerPickupLocationRefListener);
                }
                showCustomerInfo.setVisibility(View.GONE);
                tvCustomerName.setText("");
                tvCustomerPhone.setText("");
                tvCustomerDestination.setText("");
                CustomerProfilePhoto.setImageResource(R.mipmap.ic_default_user);
            }
        });

        btn_Logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isLoggingOut = true;

                disconnectDriver();

                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(DriverMapActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            }
        });
        btn_Profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DriverMapActivity.this, DriverProfile.class);
                startActivity(intent);
            }
        });
        btn_Saved.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DriverMapActivity.this, SavedActivity.class);
                intent.putExtra("customerOrDriver", "Drivers");
                startActivity(intent);
            }
        });
        getAssignedCustomer();
    }
    //get request user id to driver
    private void getAssignedCustomer(){
        String driverId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference assignedCustomerRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverId).child("customerRequest").child("customerRideId");
        assignedCustomerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    status = 1;
                    customerId = dataSnapshot.getValue().toString();
                    getAssignedCustomerPickupLocation(); // get assgin user pickup location
                    getAssignedCustomerDestination();
                    getAssignedCustomerInfo(); //get assgin user Information On Driver's Activity
                }
                // driver canceled request notice
                else{
                    endRide();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    Marker pickupMarker;
    private DatabaseReference assignedCustomerPickupLocationRef;
    private ValueEventListener assignedCustomerPickupLocationRefListener;
    // get assgin user pickup location
    private void getAssignedCustomerPickupLocation(){
        assignedCustomerPickupLocationRef = FirebaseDatabase.getInstance().getReference().child("customerRequest").child(customerId).child("l");
        assignedCustomerPickupLocationRefListener = assignedCustomerPickupLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && !customerId.equals("")){
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLng = 0;
                    if(map.get(0) != null){
                        locationLat = Double.parseDouble(map.get(0).toString());
                    }
                    if(map.get(1) != null){
                        locationLng = Double.parseDouble(map.get(1).toString());
                    }
                    pickupLatLng = new LatLng(locationLat,locationLng);
                    pickupMarker = Map.addMarker(new MarkerOptions().position(pickupLatLng).title("pickup location").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_pickup)));
                    getRouteToMarker(pickupLatLng);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void getRouteToMarker(LatLng pickupLatLng) {
        if (pickupLatLng != null && LastLocation != null){
            Routing routing = new Routing.Builder()
                    .travelMode(AbstractRouting.TravelMode.DRIVING)
                    .withListener(this)
                    .alternativeRoutes(false)
                    .waypoints(new LatLng(LastLocation.getLatitude(), LastLocation.getLongitude()), pickupLatLng)
                    .build();
            routing.execute();
        }
    }

    private void getAssignedCustomerDestination(){
        String driverId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference assignedCustomerRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverId).child("customerRequest");
        assignedCustomerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) { // first save the destination in map database
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if(map.get("Destination")!=null){ // dispaly the destination in driver screen
                        destination = map.get("Destination").toString();
                        tvCustomerDestination.setText(destination);
                    }
                    else{
                        tvCustomerDestination.setText("");
                    }
                    // second get the destination
                    Double destinationLat = 0.0;
                    Double destinationLng = 0.0;
                    if(map.get("destinationLat") != null){
                        destinationLat = Double.valueOf(map.get("destinationLat").toString());
                    }
                    if(map.get("destinationLng") != null){
                        destinationLng = Double.valueOf(map.get("destinationLng").toString());
                        destinationLatLng = new LatLng(destinationLat, destinationLng);
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    //get Customer's Information On Driver's Activity
    private void getAssignedCustomerInfo(){
        showCustomerInfo.setVisibility(View.VISIBLE);
        DatabaseReference mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(customerId);
        mCustomerDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if(map.get("Name")!=null){
                        tvCustomerName.setText(map.get("Name").toString());
                    }
                    if(map.get("Phone")!=null){
                        tvCustomerPhone.setText(map.get("Phone").toString());
                    }
                    if(map.get("ProfilePhotoPath")!=null){
                        Glide.with(getApplication()).load(map.get("ProfilePhotoPath").toString()).into(CustomerProfilePhoto);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }


    private void endRide(){
        btn_RideStatus.setText("picked passenger"); // when customer picked to driver , the driver picked customer
        erasePolylines();

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(userId).child("customerRequest");
        driverRef.removeValue();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("customerRequest");
        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(customerId);
        customerId="";
        rideDistance = 0;

        if(pickupMarker != null){
            pickupMarker.remove();
        }
        if (assignedCustomerPickupLocationRefListener != null){
            assignedCustomerPickupLocationRef.removeEventListener(assignedCustomerPickupLocationRefListener);
        }
        showCustomerInfo.setVisibility(View.GONE);
        tvCustomerName.setText("");
        tvCustomerPhone.setText("");
        tvCustomerDestination.setText("");
        CustomerProfilePhoto.setImageResource(R.mipmap.ic_default_user);
    }

    private void recordRide(){

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(userId).child("Saved");
        DatabaseReference customerRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(customerId).child("Saved");
        DatabaseReference historyRef = FirebaseDatabase.getInstance().getReference().child("Saved");
        String requestId = historyRef.push().getKey();
        DatabaseReference driverRefCopy = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(userId);

        driverRef.child(requestId).setValue(true);
        driverRefCopy.child("requestId").removeValue();
        driverRefCopy.child("requestId").child(requestId).setValue(requestId);
        customerRef.child(requestId).setValue(true);

        HashMap map = new HashMap();
        map.put("driver", userId);  // saving driver in driver
        map.put("customer", customerId); // saving customerId in customer
        map.put("rating", 0);
        map.put("timestamp", getCurrentTimestamp()); // saving timestamp in diver -- > saved --> time
        map.put("destination", destination);
        map.put("location/from/lat", pickupLatLng.latitude);
        map.put("location/from/lng", pickupLatLng.longitude);
        map.put("location/to/lat", destinationLatLng.latitude);
        map.put("location/to/lng", destinationLatLng.longitude);
        map.put("distance", rideDistance);
        historyRef.child(requestId).updateChildren(map);
    }

    // getting the current time
    private Long getCurrentTimestamp() {
        Long timestamp = System.currentTimeMillis()/1000;
        return timestamp;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        Map = googleMap;

        LocationDriverRequest = new LocationRequest();
        LocationDriverRequest.setInterval(1000); //Refresh map since 1 second
        LocationDriverRequest.setFastestInterval(1000);
        LocationDriverRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){

            }else{
                checkLocationPermission();
            }
        }
    }


    LocationCallback mLocationCallback = new LocationCallback(){
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for(Location location : locationResult.getLocations()){
                if(getApplicationContext()!=null){

                    if(!customerId.equals("") && LastLocation!=null && location != null){
                        rideDistance += LastLocation.distanceTo(location)/1000;
                    }


                    LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
                    Map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    Map.animateCamera(CameraUpdateFactory.zoomTo(11));

                    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DatabaseReference refAvailable = FirebaseDatabase.getInstance().getReference("driversAvailable");
                    DatabaseReference refWorking = FirebaseDatabase.getInstance().getReference("driversWorking");
                    GeoFire geoFireAvailable = new GeoFire(refAvailable);
                    GeoFire geoFireWorking = new GeoFire(refWorking);

                    switch (customerId){
                        case "": // empty = no user assigned pick up call and driver no picked user
                            geoFireWorking.removeLocation(userId);
                            geoFireAvailable.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()));
                            break;

                        default:
                            geoFireAvailable.removeLocation(userId);
                            geoFireWorking.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()));
                            break;
                    }
                }
            }
        }
    };

    // check Location Permission to use and display the map
    private void checkLocationPermission() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setTitle("give permission")
                        .setMessage("give permission message")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(DriverMapActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                            }
                        })
                        .create()
                        .show();
            }
            else{
                ActivityCompat.requestPermissions(DriverMapActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode){
            case 1:{
                if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                        mFusedLocationClient.requestLocationUpdates(LocationDriverRequest, mLocationCallback, Looper.myLooper());
                        Map.setMyLocationEnabled(true);
                    }
                } else{
                    Toast.makeText(getApplicationContext(), "Please provide the permission", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }

    // when driver
    private void connectDriver(){
        checkLocationPermission();
        mFusedLocationClient.requestLocationUpdates(LocationDriverRequest, mLocationCallback, Looper.myLooper());
        Map.setMyLocationEnabled(true);
    }

    // when driver click logout button
    private void disconnectDriver(){
        if(mFusedLocationClient != null){
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("driversAvailable");

        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(userId);
    }

    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.primary_dark_material_light};
    @Override
    public void onRoutingFailure(RouteException e) {
        if(e != null) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(this, "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onRoutingStart() {
    }
    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {
        if(polylines.size()>0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();
        //add route(s) to the map.
        for (int i = 0; i <route.size(); i++) {

            //In case of more than 5 alternative routes
            int colorIndex = i % COLORS.length;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = Map.addPolyline(polyOptions);
            polylines.add(polyline);

            Toast.makeText(getApplicationContext(),"Route "+ (i+1) +": distance - "+ route.get(i).getDistanceValue()+": duration - "+ route.get(i).getDurationValue(),Toast.LENGTH_SHORT).show();
        }

    }
    @Override
    public void onRoutingCancelled() {
    }
    private void erasePolylines(){
        for(Polyline line : polylines){
            line.remove();
        }
        polylines.clear();
    }

}