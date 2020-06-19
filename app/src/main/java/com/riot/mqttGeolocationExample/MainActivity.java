package com.riot.mqttGeolocationExample;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.riot.mqttGeolocationExample.services.LocationService;

public class MainActivity extends AppCompatActivity {

    public TextView statusText;
    private Button startButton;
    private Button stopButton;

    public LocationService gps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Check for location user permissions
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startLocationService();
        } else {
            //If do not have location access then request permissions
            ActivityCompat.requestPermissions(this, new String[] {
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION },1);
        }

        //Get view objects
        statusText = (TextView) findViewById(R.id.statusText);
        startButton = (Button) findViewById(R.id.startButton);
        stopButton= (Button) findViewById(R.id.stopButton);

        //Actions when start button is clicked
        startButton.setOnClickListener(v ->{
            gps.startTracking();
            startButton.setEnabled(false);
            stopButton.setEnabled(true);
        });

        //Actions when stop button is clicked
        stopButton.setOnClickListener(v -> {
            gps.stopTracking();
            startButton.setEnabled(true);
            stopButton.setEnabled(false);
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1:
                //If user accepts location access start tracking
                if(grantResults.length > 0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    startLocationService();
                }
                else {
                    // permission denied, let user knows that we need those permissions
                    statusText.setText("No GPS Access");
                    startButton.setEnabled(false);
                    stopButton.setEnabled(false);
                    Toast.makeText(MainActivity.this, "This app need gps permisions", Toast.LENGTH_LONG).show();
                }
        }
    }

    private void startLocationService(){
        final Intent locationIntent = new Intent(this.getApplication(), LocationService.class);
        this.getApplication().startService(locationIntent);
        this.getApplication().bindService(locationIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            String name = className.getClassName();
            if (name.endsWith("LocationService")) {
                gps = ((LocationService.LocationServiceBinder) service).getService();
                startButton.setEnabled(true);
                stopButton.setEnabled(false);
                statusText.setText("Ready");
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            if (className.getClassName().equals("LocationService")) {
                gps = null;
                startButton.setEnabled(false);
                stopButton.setEnabled(false);
                statusText.setText("Not Ready");
            }
        }
    };
}
