package com.riot.mqttGeolocationExample.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.google.gson.Gson;
import com.riot.mqttGeolocationExample.models.Coordinates;

public class LocationService extends Service {

    private final LocationServiceBinder binder = new LocationServiceBinder();
    private final String TAG = "LocationService";
    private LocationListener locationListener;
    private LocationManager locationManager;
    private NotificationManager notificationManager;

    private final int LOCATION_INTERVAL = 500;
    private final int LOCATION_DISTANCE = 1;

    private CustomMqttService mqtt = new CustomMqttService(this);

    public class LocationServiceBinder extends Binder {
        public LocationService getService() {
            return LocationService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    private class LocationListener implements android.location.LocationListener {
        private final String TAG = "LocationListener";
        private Location lastLocation;

        public LocationListener(String provider) {
            lastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location) {
            lastLocation = location;
            Coordinates coordinates = new Coordinates(
                    location.getLatitude(),
                    location.getLongitude(),
                    location.getAltitude());

            Gson gson = new Gson();
            String json = gson.toJson(coordinates);
            Log.i("JSON",json);
            mqtt.pub(json);
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.e(TAG, "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.e(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.e(TAG, "onStatusChanged: " + status);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_NOT_STICKY;
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "Created");
        startForeground(12345678, getNotification());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (locationManager != null)
            try {
                locationManager.removeUpdates(locationListener);
            } catch (Exception ex) {
                Log.i(TAG, "fail to remove location listners, ignore", ex);
            }
    }

    private void initializeLocationManager() {
        Log.i(TAG, "initializeLocationManager - LOCATION_INTERVAL: " +
                LOCATION_INTERVAL + " LOCATION_DISTANCE: " + LOCATION_DISTANCE);
        if (locationManager == null) {
            locationManager = (LocationManager) getApplicationContext()
                    .getSystemService(Context.LOCATION_SERVICE);
        }
    }

    public void startTracking() {
        initializeLocationManager();
        locationListener = new LocationListener(LocationManager.GPS_PROVIDER);
        mqtt.connect();

        try {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    LOCATION_INTERVAL,
                    LOCATION_DISTANCE,
                    locationListener);
        } catch (SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }

    }

    public void stopTracking() {
        this.onDestroy();
        mqtt.disconnect();
    }

    private Notification getNotification() {

        NotificationChannel channel = new NotificationChannel("channel_01", "GPS Channel", NotificationManager.IMPORTANCE_DEFAULT);

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);

        Notification.Builder builder = new Notification.Builder(getApplicationContext(), "channel_01").setAutoCancel(true);

        return builder.build();
    }


}

