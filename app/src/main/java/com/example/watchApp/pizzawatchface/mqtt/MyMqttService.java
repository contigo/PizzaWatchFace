package com.example.watchApp.pizzawatchface.mqtt;

import static com.example.watchApp.pizzawatchface.Constants.TAG;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.example.watchApp.pizzawatchface.LocationActivity;
import com.example.watchApp.pizzawatchface.R;
import com.example.watchApp.pizzawatchface.util.AppInfoCallback;
import com.example.watchApp.pizzawatchface.util.AppInfoInterface;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class MyMqttService extends Service{
    public static final String ACTION_START = "action.start";
    public static final String ACTION_END = "action.end";
    public static final String ACTION_BROADCAST = "action.broadcast";

    private static final int MESSGE_TIMER_START = 10000;
    private static final int MESSGE_TIMER_REPEAT = MESSGE_TIMER_START + 1;
    private static final int MESSGE_TIMER_STOP = MESSGE_TIMER_START + 2;



    //location 인터벌
    private final long UPDATE_INTERVAL_IN_MILLISECONDS = 60 * 1000   * 10;
    private final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = 60 * 1000 * 5 ;

    public Timer mTimer = null;
    public static final int DEFAULT_TIMER_INTERVAL = 60 * 1000 * 10;
    public double mLatitude = 0;
    public double mLongitude = 0;

    public FusedLocationProviderClient fusedLocationClient;

    private TimerHandler timerHandler;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG , ">>>>>>>>>> service onCreate ");
        // PendingIntent를 이용하면 포그라운드 서비스 상태에서 알림을 누르면 앱의 MainActivity를 다시 열게 된다.
        Intent testIntent = new Intent(getApplicationContext(), LocationActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, testIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        // 오래오 윗버젼일 때는 아래와 같이 채널을 만들어 Notification과 연결해야 한다.
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("channel", "play!!", NotificationManager.IMPORTANCE_DEFAULT);

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

        getLocation();
        timerHandler = new TimerHandler(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, ">>>>>>>>>>>>>>>> start MyMqttService startCommond " + intent.getAction());
        switch (intent.getAction()) {
            case ACTION_START:
//                checkHandler();
                Message msg = new Message();
                msg.what = MESSGE_TIMER_START;
                timerHandler.sendMessage(msg);
                break;
            case ACTION_END:
                stop();
                break;
            case ACTION_BROADCAST:
                Log.d(TAG , ">>>>>>>>>>>> 스타트 브로드캐스트 시그널");
                getLocation();
                break;

        }
        return START_NOT_STICKY;
    }

    //서비스 중단
    private void stop() {
        Log.d(TAG, ">>>>>>>>>>>>>>>>> service stop");
        if (mTimer != null) {
            mTimer.cancel();
            Toast.makeText(getApplicationContext(), "서비스 종료", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "서비스가 종료되었습니다", Toast.LENGTH_SHORT).show();
        }

        fusedLocationClient.removeLocationUpdates(mLocationCallback);
        stopSelf();
        if(timerHandler != null){
            timerHandler.sendEmptyMessage(MESSGE_TIMER_STOP);
            timerHandler.removeMessages(MESSGE_TIMER_REPEAT);
        }
    }

    public void wakeUp() {
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        @SuppressLint("InvalidWakeLockTag") PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "wakeup_service");
        wakeLock.acquire();
    }

    private Retrofit retrofit;

    public void getAppInfoData() {
        if (retrofit == null) {
            String url = "http://dev.sosea.co.kr/";
            retrofit = new Retrofit.Builder()
                    .baseUrl(url)
//                .addConverterFactory(new NullOnEmptyConverterFactory())
                    .addConverterFactory(GsonConverterFactory.create())
                    .callbackExecutor(Executors.newSingleThreadExecutor())
                    .build();
        }

        String lat = String.valueOf(mLatitude);
        String lon = String.valueOf(mLongitude);

        AppInfoInterface appInfoInterface = retrofit.create(AppInfoInterface.class);
        appInfoInterface.getLocationoCallback("1", "com.ahranta.android.emergency.dev.user", lat , lon, "service" , 4).enqueue(new Callback<AppInfoCallback>() {
            @Override
            public void onResponse(Call<AppInfoCallback> call, Response<AppInfoCallback> response) {
                Log.d(TAG, ">>>>>>>>>>>getAppInfoData service onResponse " + countTime + ", fstop" +
                        "" + response.body());
            }

            @Override
            public void onFailure(Call<AppInfoCallback> callb, Throwable t) {
                Log.d(TAG, ">>>>>>>>>>>getAppInfoData service onFailure t: " + countTime + ", " + t.getMessage());
            }
        });
    }

    private int countTime = 0;
    public void checkHandler() {
        if (mTimer != null) return;

        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                countTime = countTime + 1;
//                Log.d(TAG , ">>>>>>>>>>>>>>>>>>>>> timer count "+ countTime);
                wakeUp();
                getAppInfoData();
            }
        }, 3000, DEFAULT_TIMER_INTERVAL);

    }



    LocationCallback mLocationCallback = null;
    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if(mLocationCallback != null) return;

        final Location[] location = {new Location(LocationManager.FUSED_PROVIDER)};
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                location[0] = locationResult.getLastLocation();

                if (location[0] != null) {
                    double speed = location[0].getSpeed() * 3.6 ;
                    mLatitude =  location[0].getLatitude();
                    mLongitude =  location[0].getLongitude();
//                    Log.d(TAG,">>>>>> service LocationResult " +countTime +", lat:" + mLatitude + ", lon:" + mLongitude +", speed "+ speed);
                }
            }
        };

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        locationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        locationRequest.setPriority(Priority.PRIORITY_HIGH_ACCURACY);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
        fusedLocationClient.requestLocationUpdates(locationRequest,
                mLocationCallback,
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
        }
    }


    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);

        Log.d(TAG, ">>>>>> onTaskedremoved called");

        PendingIntent service = PendingIntent.getService(
                getApplicationContext(),
                1001,
                new Intent(getApplicationContext(), MyMqttService.class),  PendingIntent.FLAG_ONE_SHOT);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, 5000, service);
    }



    private class TimerHandler extends Handler{
        private MyMqttService service = null;

        public TimerHandler( MyMqttService service) {
            this.service = service;
        }


        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what){
                case MESSGE_TIMER_START:
                    this.sendEmptyMessageDelayed(MESSGE_TIMER_REPEAT , DEFAULT_TIMER_INTERVAL);
                    break;
                case MESSGE_TIMER_STOP:
                    this.removeMessages(MESSGE_TIMER_REPEAT);
                    break;
                case MESSGE_TIMER_REPEAT:
                    service.wakeUp();
                    service.getAppInfoData();
                    this.sendEmptyMessageDelayed(MESSGE_TIMER_REPEAT , DEFAULT_TIMER_INTERVAL);
                    break;
            }

        }
    }

}
