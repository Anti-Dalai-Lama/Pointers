package com.blablaarthur.pointers;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    static GoogleMap mGoogleMap;
    Uri uri;
    List<GeoPoint> points;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Intent currentIntent = getIntent();
        uri = currentIntent.getParcelableExtra("uri");

        points = new ArrayList<>();
    }

    @Override
    public void onStart(){
        super.onStart();
        readXML(uri);
        initMap();
        if(isOnline(this)){
            createRoute();
        }
    }

    public void initMap(){
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);
    }

    public void readXML(Uri uri){
        try {
            InputStream stream = getContentResolver().openInputStream(uri);
            BufferedReader in = new BufferedReader(new InputStreamReader(stream));

            String tmp = "";

            XmlPullParser xpp = XmlPullParserFactory.newInstance().newPullParser();
            xpp.setInput(in);
            String t = "";
            double lat = 0;
            double lng = 0;

            boolean title = false;
            boolean la = false;
            boolean lo = false;

            while (xpp.getEventType() != XmlPullParser.END_DOCUMENT) {
                switch (xpp.getEventType()) {
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        if(xpp.getName().equals("title")){
                            title = true;
                        }
                        else if(xpp.getName().equals("longitude")){
                            lo = true;
                        }
                        else if(xpp.getName().equals("latitude")){
                            la = true;
                        }
//                        Log.d("A_R_T", "START_TAG: name = " + xpp.getName()
//                                + ", depth = " + xpp.getDepth() + ", attrCount = "
//                                + xpp.getAttributeCount());
                        break;
                    // конец тэга
                    case XmlPullParser.END_TAG:
                        if(xpp.getName().equals("point")){
                            points.add(new GeoPoint(t,lat,lng));
                        }
                        else if(xpp.getName().equals("title")){
                            title = false;
                        }
                        else if(xpp.getName().equals("longitude")){
                            lo = false;
                        }
                        else if(xpp.getName().equals("latitude")){
                            la = false;
                        }
                        break;
                    case XmlPullParser.TEXT:
                        //Log.d("A_R_T", "text = " + xpp.getText());
                        if(title){
                            t = xpp.getText();
                        }
                        else if(lo){
                            lng = Double.valueOf(xpp.getText());
                        }
                        else if(la){
                            lat = Double.valueOf(xpp.getText());
                        }
                        break;
                    default:
                        break;
                }
                xpp.next();
            }
            in.close();

            for(GeoPoint gp : points){
                Log.d("A_R_T", gp.toString());
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        setAndZoomMarkers();
    }

    public void setAndZoomMarkers(){
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (GeoPoint point : points) {
            LatLng ll = new LatLng(point.Latitude, point.Longitude);
            builder.include(ll);
            MarkerOptions mop = new MarkerOptions()
                    .title(point.Title)
                    .position(ll);
            mGoogleMap.addMarker(mop);
        }
        LatLngBounds bounds = builder.build();
        int padding = 40;
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        mGoogleMap.moveCamera(cu);
    }


    private void createRoute() {
        for(int i = 0; i < points.size()-1; i++){
            String googleUrl = getUrl(points.get(i), points.get(i+1));
            FetchUrl fetch = new FetchUrl();
            fetch.execute(googleUrl);
        }
    }

    private String getUrl(GeoPoint origin, GeoPoint dest) {
        String str_origin = "origin=" + origin.Latitude + "," + origin.Longitude;
        String str_dest = "destination=" + dest.Latitude + "," + dest.Longitude;
        String sensor = "sensor=false";
        String parameters = str_origin + "&" + str_dest + "&" + sensor;
        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;
        return url;
    }


    public boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        //should check null because in airplane mode it will be null
        return (netInfo != null && netInfo.isConnected());
    }
}
