package com.riot.mqttGeolocationExample.services;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;

public class CustomMqttService {

    private String HOST = "192.168.1.100";
    private String PORT = "1883";
    private String TOPIC = "android/gps";

    private String TAG = "MQTT";
    private MqttAndroidClient client;
    private Context context;

    public CustomMqttService(Context context) {
        this.context = context;
    }

    public void connect() {

        String connectionUri = "tcp://" + HOST + ":" + PORT;
        String clientId = MqttClient.generateClientId();

        client = new MqttAndroidClient(context, connectionUri, clientId);

        try {
            IMqttToken token = client.connect();
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    Log.d(TAG, "success");
                    Toast.makeText(context, "Mqtt connected!!", Toast.LENGTH_LONG).show();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Log.d(TAG, "failure: " + exception.getMessage());
                    Toast.makeText(context, "Mqtt connection failed", Toast.LENGTH_LONG).show();
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        if (client != null && client.isConnected())
            try {
                client.disconnect();
            } catch (MqttException e) {
                e.printStackTrace();
            }
    }

    public void pub(String msg) {
        try {
            client.publish(TOPIC, msg.getBytes(), 0, false);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

}
