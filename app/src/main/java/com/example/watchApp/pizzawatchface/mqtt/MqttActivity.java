package com.example.watchApp.pizzawatchface.mqtt;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.watchApp.pizzawatchface.databinding.ActivityMqttBinding;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;

import static com.example.watchApp.pizzawatchface.Constants.TAG;


public class MqttActivity extends Activity implements  MqttCallback{

    private TextView mTextView;
    private ActivityMqttBinding binding;
    private Button mBtnConnect;
    private boolean isConnect = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMqttBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mTextView = binding.text;
        mBtnConnect = binding.btnConnect;
        mBtnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isConnect)
//                    setTopic();
                    startMqttService();
                else
                    Toast.makeText(MqttActivity.this, "서버와 연결 실패",  Toast.LENGTH_SHORT ).show();
            }
        });

        mqttConnect();
    }


    public void startMqttService(){
            Intent intent = new Intent(MqttActivity.this , MyMqttService.class);
        if(!ServiceUtil.isServiceRunning(this , MyMqttService.class) ){
            intent.setAction(MyMqttService.ACTION_START);
            showToast("서비스 시작");
        }else{
            showToast("기존서비스를 종료합니다.");
            intent.setAction(MyMqttService.ACTION_END);
        }
            startService(intent);
    }


    MqttAndroidClient client;
    public void mqttConnect(){
//        String serverURL = "tcp://dev.sosea.co.kr:1885";
        String serverURL = "tcp://192.168.10.123:1883" ; //"tcp://broker.hivemq.com:1883";
        String clientId = MqttClient.generateClientId() +"_ykchoi";

         client =  new MqttAndroidClient(this.getApplicationContext(), serverURL,   clientId);
         client.setCallback(this);
        try {
            Log.d(TAG, ">>>>>>>>> mqttConnect start " + serverURL);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1);
            options.setMaxInflight(65535);
            options.setKeepAliveInterval(100000);
            options.setAutomaticReconnect(true);
            options.setConnectionTimeout(100000);
//            IMqttToken token = client.connect(options);

            IMqttToken token = client.connect();
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    Log.d(TAG, ">>>>>>>>> onSuccess "+ asyncActionToken.getMessageId());
                    mTextView.setText("onSuccess");
                    isConnect = true;
                    setTopic();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Log.d(TAG, ">>>>> onFailure "+ exception.getMessage());
                    mTextView.setText("onFailure "+ exception.getMessage());
                    isConnect = true;
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

    @Override
    public void connectionLost(Throwable cause) {
        Log.d(TAG , ">>>>>>>>>> connectionLost");
        Toast.makeText(MqttActivity.this, " connectionLost 서버와 연결 실패",  Toast.LENGTH_SHORT ).show();
        try {
            client.connect();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        Log.d(TAG, "messageArrived: "+ topic +", message:" + message );
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        Log.d(TAG, ">>>>>>>>>>>>>>>> deliveryComplete: "+ token.getMessageId());
    }

    public void showToast(String str ){
        Toast.makeText( getApplicationContext(), ">>>>>>>>>> "+ str , Toast.LENGTH_SHORT).show();
    }

}