package com.example.heroalert;

public class Action {

    private String actionTitle;
    private String address;
    private double latitude;
    private double longitude;

    public Action(String actionTitle, String address, double latitude, double longitude) {
        this.actionTitle = actionTitle;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getActionTitle() {
        return this.actionTitle;
    }

    public String getAddress() {
        return this.address;
    }

    public double getLatitude () { return this.latitude;}

    public double getLongitude () { return this.longitude;}

}
