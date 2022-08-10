package com.example.watchApp.pizzawatchface.mqtt;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.example.watchApp.pizzawatchface.databinding.ActivityMqttBinding;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;

import static com.example.watchApp.pizzawatchface.Constants.TAG;


public class MqttActivity extends Activity {

    private TextView mTextView;
    private ActivityMqttBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMqttBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mTextView = binding.text;

        mqttConnect();
    }

    MqttAndroidClient client;
    public void mqttConnect(){
//        String serverURL = "tcp://dev.sosea.co.kr:1885";
        String serverURL = "tcp://192.168.10.121:1883" ; //"tcp://broker.hivemq.com:1883";
        String clientId = MqttClient.generateClientId() +"_ykchoi";

         client =  new MqttAndroidClient(this.getApplicationContext(), serverURL,   clientId);
        try {
            Log.d(TAG, ">>>>>>>>> mqttConnect start " + serverURL);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1);
            options.setMaxInflight(65535);
//            IMqttToken token = client.connect(options);

            IMqttToken token = client.connect();
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    Log.d(TAG, ">>>>>>>>> onSuccess "+ asyncActionToken.toString());
                    mTextView.setText("onSuccess");
                    setTopic();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Log.d(TAG, ">>>>> onFailure "+ exception.getMessage());
                    mTextView.setText("onFailure "+ exception.getMessage());
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

public void setTopic(){
    String topic = "/test";
    String payload = "this is test Data! "+ (Math.random() * 1000);
    byte[] encodedPayload = new byte[0];
    try {
        encodedPayload = payload.getBytes("UTF-8");
        MqttMessage message = new MqttMessage(encodedPayload);
        client.publish(topic, message);
    } catch (UnsupportedEncodingException | MqttException e) {
        e.printStackTrace();
    }
}

}