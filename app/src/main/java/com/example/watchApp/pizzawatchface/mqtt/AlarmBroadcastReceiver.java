package com.example.watchApp.pizzawatchface.mqtt;


import static com.example.watchApp.pizzawatchface.Constants.TAG;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.example.watchApp.pizzawatchface.Constants;
import com.example.watchApp.pizzawatchface.util.AppInfoCallback;
import com.example.watchApp.pizzawatchface.util.AppInfoInterface;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class AlarmBroadcastReceiver extends BroadcastReceiver {
    public double mLatitude = 0;
    public double mLongitude = 0;

    private final long UPDATE_INTERVAL_IN_MILLISECONDS = 6 * 1000   * 10;
    private final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = 60 * 1000 * 5 ;

    public static final int DEFAULT_TIMER_INTEVAL = 60 * 1000 * 10;
    public FusedLocationProviderClient fusedLocationClient;
    private LocationCallback mLocationCallback;
    private Retrofit retrofit;

    public static final String END_BROADCAST ="alarmmanager.endAlarm";

    public AlarmBroadcastReceiver() {
        super();

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = format.format(Calendar.getInstance().getTime());
        Log.d(Constants.TAG , ">>>>>>>>> onReceiver  time:"+time +", action:" +intent.getAction() +", "+ intent);

        if(intent.getAction().equals(END_BROADCAST))
        {

        }else{
            getAppInfoData();
            loopAlarm(context , intent);
        }
    }


    private void loopAlarm (Context context, Intent intent ){
        if(intent.getAction().equals(END_BROADCAST)) return;

        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 356 , intent,   PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis()  );
//        cal.add(Calendar.MILLISECOND ,  6 * 1000 * 10 );
        long tritime = cal.getTimeInMillis() + DEFAULT_TIMER_INTEVAL;  //60초

        alarmMgr.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, tritime,  alarmIntent);
    }

    private void getLocation(Context context) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

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
//                    Log.d(TAG,">>>>>> broadcast LocationResult " +countTime +", lat:" + mLatitude + ", lon:" + mLongitude +", speed "+ speed);
                }
            }
        };

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        locationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        locationRequest.setPriority(Priority.PRIORITY_HIGH_ACCURACY);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context );
        fusedLocationClient.requestLocationUpdates(locationRequest,
                mLocationCallback,
                Looper.getMainLooper());
    }


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
        appInfoInterface.getLocationoCallback("1", "com.ahranta.android.emergency.dev.user", lat , lon, "receiver", 5).enqueue(new Callback<AppInfoCallback>() {
            @Override
            public void onResponse(Call<AppInfoCallback> call, Response<AppInfoCallback> response) {
                Log.d(TAG, ">>>>>>>>>>>getAppInfoData broad onResponse , fstop" + response.body());
            }

            @Override
            public void onFailure(Call<AppInfoCallback> callb, Throwable t) {
                Log.d(TAG, ">>>>>>>>>>>getAppInfoData broad onFailure t: , " + t.getMessage());
            }
        });
    }
}
