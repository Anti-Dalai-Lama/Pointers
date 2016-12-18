package com.blablaarthur.pointers;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class FileActivity extends AppCompatActivity {

    TextView tw;
    Button button;
    boolean file_allowed = false;
    boolean play_service = false;
    private static final int READ_REQUEST_CODE = 42;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[] {
                            Manifest.permission.READ_EXTERNAL_STORAGE
                    },
                    222);
        }
        else {
            file_allowed = true;
        }

        button = (Button) findViewById(R.id.button);
        tw = (TextView) findViewById(R.id.textView);


        if(googlePlayServicesAvailable()){
            Toast.makeText(this, "Play Services available", Toast.LENGTH_LONG).show();
            play_service = true;
        }


    }

    public void getFile(View v){
        if(file_allowed && play_service) {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("text/xml");
            startActivityForResult(intent, READ_REQUEST_CODE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {

        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                Log.i("A_R_T", "Uri: " + uri.toString());
                //readXML(uri);
                Intent myIntent = new Intent(FileActivity.this,
                        MapActivity.class);
                myIntent.putExtra("uri", uri);
                startActivityForResult(myIntent, 100);
            }
        }
    }



    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == 222) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                file_allowed = true;
                Toast.makeText(this,"Access allowed", Toast.LENGTH_LONG).show();
            }
            else if (grantResults[0] == PackageManager.PERMISSION_DENIED){
                Toast.makeText(this,"Access denied", Toast.LENGTH_LONG).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public boolean googlePlayServicesAvailable(){
        GoogleApiAvailability api = GoogleApiAvailability.getInstance();
        int isAvailable = api.isGooglePlayServicesAvailable(this);
        if(isAvailable == ConnectionResult.SUCCESS){
            return true;
        }
        else if(api.isUserResolvableError(isAvailable)){
            Dialog dialog = api.getErrorDialog(this, isAvailable, 0);
            dialog.show();
        }
        else {
            Toast.makeText(this, "Can't connect to Play Services", Toast.LENGTH_LONG).show();
        }
        return false;
    }
}
