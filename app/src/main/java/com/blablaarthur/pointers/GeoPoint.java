package com.blablaarthur.pointers;

/**
 * Created by Артур on 18.12.2016.
 */

public class GeoPoint {
    public String Title;
    public double Latitude;
    public double Longitude;

    public GeoPoint(String title, double lat, double lng){
        Title = title;
        Latitude = lat;
        Longitude = lng;
    }

    @Override
    public String toString() {
        return Title + " " + Latitude + " " + Longitude;
    }
}
