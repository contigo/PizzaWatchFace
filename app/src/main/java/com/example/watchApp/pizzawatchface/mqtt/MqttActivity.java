package com.example.watchApp.pizzawatchface.mqtt;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.telecom.TelecomManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.watchApp.pizzawatchface.R;
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

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


public class MqttActivity extends Activity implements  MqttCallback ,  ActivityCompat.OnRequestPermissionsResultCallback{

    private TextView mTextView;
    private ActivityMqttBinding binding;
    private Button mBtnConnect;
    private boolean isConnect = true;

    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

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
                sendCall();
            }

        });

//        mqttConnect();
        if (!checkPermissions()) {
            requestPermissions();
        }

        int check = ContextCompat.checkSelfPermission(this,  Manifest.permission.CALL_PHONE );
        String result = check == PackageManager.PERMISSION_DENIED? "퍼미션 체크 싪패" : "퍼미션 체크 성공";
        Log.d(TAG , ">>>>>>>>>>>>>>>> check P "+ result);


    }


    private void sendCall() {
        Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:02119"));
        callIntent.putExtra(TelecomManager.EXTRA_START_CALL_WITH_SPEAKERPHONE, false);

        startActivity(callIntent);
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



    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,  Manifest.permission.CALL_PHONE);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CALL_PHONE);
        if (shouldProvideRationale) {
            Log.d(TAG, ">>>>>>>>>>> Displaying permission rationale to provide additional context.");
        } else {
            Log.d(TAG, ">>>>>>>>>>>>> Requesting permission");
            ActivityCompat.requestPermissions(MqttActivity.this, new String[]{Manifest.permission.CALL_PHONE, Manifest.permission.READ_PHONE_STATE}, REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,   @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.i(TAG, ">>>>>>>>> onRequestPermissionResult");
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {

            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "Permission granted.");
            } else {
                Log.d(TAG , ">>>>>>>>> 권한설정 실패!!!!");
            }
        }
    }


}