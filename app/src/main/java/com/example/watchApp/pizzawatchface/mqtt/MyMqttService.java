package com.example.watchApp.pizzawatchface.mqtt;

import static com.example.watchApp.pizzawatchface.Constants.TAG;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.TintTypedArray;
import androidx.core.app.NotificationCompat;

import com.example.watchApp.pizzawatchface.LocationActivity;
import com.example.watchApp.pizzawatchface.R;
import com.example.watchApp.pizzawatchface.util.AppInfoCallback;
import com.example.watchApp.pizzawatchface.util.AppInfoInterface;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnSuccessListener;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class MyMqttService extends Service implements LocationListener {
    public static final String ACTION_START ="action.start";
    public static final String ACTION_END="action.end";

    @Override
    public void onCreate() {
        super.onCreate();

        // PendingIntent를 이용하면 포그라운드 서비스 상태에서 알림을 누르면 앱의 MainActivity를 다시 열게 된다.
        Intent testIntent = new Intent(getApplicationContext(), LocationActivity.class);
        PendingIntent pendingIntent= PendingIntent.getActivity(this, 0, testIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        // 오래오 윗버젼일 때는 아래와 같이 채널을 만들어 Notification과 연결해야 한다.
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("channel", "play!!",   NotificationManager.IMPORTANCE_DEFAULT);

            // Notification과 채널 연걸
            NotificationManager mNotificationManager = ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE));
            mNotificationManager.createNotificationChannel(channel);

            // Notification 세팅
            NotificationCompat.Builder notification
                    = new NotificationCompat.Builder(getApplicationContext(), "channel")
                    .setContentTitle("현재 실행 중인 앱 이름")
                    .setSmallIcon(R.drawable.icon_soscall)
                    .setContentIntent(pendingIntent)
                    .setContentText("ContentText 입니다 ");

            // id 값은 0보다 큰 양수가 들어가야 한다.
            mNotificationManager.notify(1, notification.build());
            // foreground에서 시작
            startForeground(1, notification.build());
        }

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG , ">>>>>>>>>>>>>>>> start MyMqttService startCommond "+ intent.getAction());
        switch (intent.getAction()){
            case ACTION_START:
                checkHandler();
                break;
            case ACTION_END:
                Toast.makeText(getApplicationContext() , "서비스 종료", Toast.LENGTH_SHORT).show();

                stopSelf();
                break;
        }
        return START_NOT_STICKY;
    }

    public void wakeUp(){
        PowerManager powerManager = (PowerManager)getSystemService(Context.POWER_SERVICE);
        @SuppressLint("InvalidWakeLockTag") PowerManager.WakeLock  wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "wakeup_service");
        wakeLock.acquire();
        Log.d(TAG , ">>>>>>>>>>>>> wakeup ");

    }

    private Retrofit retrofit;
    public void getAppInfoData(){
        if (retrofit == null) {
            String url = "http://dev.sosea.co.kr/";
            retrofit = new Retrofit.Builder()
                    .baseUrl(url)
//                .addConverterFactory(new NullOnEmptyConverterFactory())
                    .addConverterFactory(GsonConverterFactory.create())
                    .callbackExecutor(Executors.newSingleThreadExecutor())
                    .build();
        }


        AppInfoInterface appInfoInterface = retrofit.create(AppInfoInterface.class);
        appInfoInterface.getInfoCallback("1" , "com.ahranta.android.emergency.dev.user").enqueue(new Callback<AppInfoCallback>() {
            @Override
            public void onResponse(Call<AppInfoCallback> call, Response<AppInfoCallback> response) {
                Log.d(TAG, ">>>>>>>>>>>getAppInfoData onResponse "+ countTime +", "+ response.body());
            }

            @Override
            public void onFailure(Call<AppInfoCallback> callb, Throwable t) {
                Log.d(TAG, ">>>>>>>>>>>getAppInfoData onFailure t: "+ countTime +", " + t.getMessage() );
            }
        });

/*      try {
            String str =  appInfoInterface.getLocationoCallback("1" , "com.ahranta.android.emergency.dev.user").execute().body().toString();
            Log.d(TAG , ">>>>>>>>>>>> getLocationoCallback "+ str);
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }

    private int countTime = 0;
    public void checkHandler(){
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                countTime = countTime + 1;
//                Log.d(TAG , ">>>>>>>>>>>>>>>>>>>>> timer count "+ countTime);
                wakeUp();
                getAppInfoData();
            }
        }, 3000, 60 * 1000 * 10);

    }

    public FusedLocationProviderClient fusedLocationClient;
    @SuppressLint("MissingPermission")
    private void getLocation(){
        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for( Location location : locationResult.getLocations() ) {
                    Log.d(TAG,">>>>>>>>>>>> service LocationResult " +countTime +", lat:" + location.getLatitude() + ", lon:" + location.getLongitude());
                }
            }
        };

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(5000);
        locationRequest.setPriority(Priority.PRIORITY_HIGH_ACCURACY);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
        fusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback,
                Looper.getMainLooper());
    }



//현재 상태가 온라인인지 확인
    public boolean isOnline = false;
    public void getNetworkInfo(){
        int MIN_BANDWIDTH_KBPS = 320;
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        Network activeNetwork = connectivityManager.getActiveNetwork();
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected() ){
            isOnline = true;
        }else{
            isOnline = false;
        }

        if (activeNetwork != null) {
            int bandwidth =  connectivityManager.getNetworkCapabilities(activeNetwork).getLinkDownstreamBandwidthKbps();

            if (bandwidth < MIN_BANDWIDTH_KBPS) {
                // Request a high-bandwidth network
                Log.d(TAG , ">>>>>>>>>>> high-bandwidth network "+ bandwidth);
            } else {
                // You already are on a high-bandwidth network, so start your network request
                Log.d(TAG , ">>>>>>>>>>> low-bandwidth network " + bandwidth);
            }
        } else {
            // No active network
            Log.d(TAG , ">>>>>>>>>>> No active network "+ isOnline);
//            getNetworkRequest(connectivityManager);
        }
    }



    @Override
    public void onLocationChanged(@NonNull Location location) {
        Log.d(TAG , ">>>>>>>>> onLocationChanged "+ location);
    }
}
