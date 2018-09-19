package com.waslnyapp.waslny.SavedRecyclerView;

public class SavedObject {
    private String rideId;
    private String time;
    private String rideLocation;

    public SavedObject(String rideId, String time, String rideLocation){
        this.rideId = rideId;
        this.time = time;
        this.rideLocation = rideLocation;
    }

    public String getrideLocation(){return rideLocation;}
    public void setrideLocation(String rideLocation) {
        this.rideLocation = rideLocation;
    }

    public String getRideId(){return rideId;}
    public void setRideId(String rideId) {
        this.rideId = rideId;
    }

    public String getTime(){return time;}
    public void setTime(String time) {
        this.time = time;
    }
}
