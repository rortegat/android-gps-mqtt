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
import android.util.Log;
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

        //Get view objects
        statusText = (TextView) findViewById(R.id.statusText);
        startButton = (Button) findViewById(R.id.startButton);
        stopButton = (Button) findViewById(R.id.stopButton);

        checkLocationPermissions();

        //Actions when start button is clicked
        startButton.setOnClickListener(v -> {
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

    public void checkLocationPermissions() {
        //if we have permission to access to gps location
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //start location service
            startLocationService();
        } else {
            //If do not have location access then request permissions
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.i("CODE", String.valueOf(requestCode));
        for (String p : permissions)
            Log.i("PERMISSIONS", String.valueOf(p));
        for (int g : grantResults)
            Log.i("RESULTS", String.valueOf(g));
        boolean granted = false;
        if (requestCode == 1) {
            //For each permission requested
            for (int i = 0, len = permissions.length; i < len; i++) {
                String permission = permissions[i];
                // If user denied the permission
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    //Check if you asked for the same permissions before
                    boolean showRationale = shouldShowRequestPermissionRationale(permission);
                    //If user cheked "nevers ask again"
                    if (!showRationale) {
                        startButton.setEnabled(false);
                        stopButton.setEnabled(false);
                        statusText.setText("No access to GPS");
                        Toast.makeText(this, "GPS Location access was rejected", Toast.LENGTH_LONG);
                    }
                    //If user hasn't checked for "never ask again"
                    else checkLocationPermissions();
                }
                //user grants permissions
                else granted = true;
            }
            //If user grants permissions
            if(granted) startLocationService();

        }
    }

    private void startLocationService() {
        final Intent locationIntent = new Intent(this.getApplication(), LocationService.class);
        this.getApplication().startService(locationIntent);
        this.getApplication().bindService(locationIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            String name = className.getClassName();
            if (name.endsWith("LocationService")) {
                gps = ((LocationService.LocationServiceBinder) service).getService();
                statusText.setText("READY");
                startButton.setEnabled(true);
                stopButton.setEnabled(false);
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            if (className.getClassName().equals("LocationService")) {
                gps = null;
                statusText.setText("NOT READY");
                startButton.setEnabled(false);
                stopButton.setEnabled(true);
            }
        }
    };
}
