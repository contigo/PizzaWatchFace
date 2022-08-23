package com.example.watchApp.pizzawatchface;

import static com.example.watchApp.pizzawatchface.Constants.TAG;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.os.PowerManager;
import android.os.SystemClock;
import android.provider.AlarmClock;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.work.OneTimeWorkRequest;
import androidx.work.OutOfQuotaPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.example.watchApp.pizzawatchface.databinding.ActivityLocationBinding;
import com.example.watchApp.pizzawatchface.http.HttpRequest;
import com.example.watchApp.pizzawatchface.http.HttpUtils;
import com.example.watchApp.pizzawatchface.mqtt.AlarmBroadcastReceiver;
import com.example.watchApp.pizzawatchface.mqtt.MyMqttService;
import com.example.watchApp.pizzawatchface.util.AppInfoCallback;
import com.example.watchApp.pizzawatchface.util.AppInfoInterface;
import com.example.watchApp.pizzawatchface.work.NetWorkCheckWorker;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Converter;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LocationActivity extends Activity implements  View.OnClickListener, LocationListener  {
    private Button btn1 , btn2, btn3;
    private TextView mText;

    public  FusedLocationProviderClient fusedLocationClient;
    private ActivityLocationBinding binding;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Keep the Wear screen always on (for testing only!)
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        super.onCreate(savedInstanceState);

        binding = ActivityLocationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mText = binding.text;
        btn1 = binding.btn1;
        btn2 = binding.btn2;
        btn3 = binding.btn3;

        btn1.setOnClickListener(this);
        btn2.setOnClickListener(this);
        btn3.setOnClickListener(this);


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION },
                    1);
            return;
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(LocationActivity.this);
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            Log.d(TAG,">>>>>>>>>>>> location lat:" + location.getLatitude() + ", lon:" + location.getLongitude());
                            mText.setText("lat:" + location.getLatitude() + ", lon:" + location.getLongitude());
                        }else{
                            Log.d(TAG,">>>>>>>>>>>> location is null");
                            mText.setText(" location is null");
                        }
                    }
                });

        initRet();
        if(!isIgnoringBatteryOptimizations(this)) {
            showToast("베터리 최적화 권한!!");
            Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        }else{
            showToast("베터리 최적화!");
        }
    }



    public boolean isIgnoringBatteryOptimizations(Context context) {
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return powerManager.isIgnoringBatteryOptimizations(context.getPackageName());
        }
        return true;
    }

    @SuppressLint("MissingPermission")
    public void updateLocation(){
//        업데이트에 시간이 오래걸린다
        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    showToast("locationResult is null ");
                    return;
                }
                for (int i = 0 ; i < locationResult.getLocations().size() ; i++ ) {

                    Location location = locationResult.getLocations().get(i);
                    Log.d(TAG , ">>>>>>>>>>>>>>> locationResult "+locationResult.getLocations().size() +", index  " + i +", location "+ location);
                    showToast(">>>>>> onLocationResult lat:" + location.getLatitude() + ", lon:" + location.getLongitude() );
                }
            }
        };

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(5000);
        locationRequest.setPriority(Priority.PRIORITY_HIGH_ACCURACY);

        if(fusedLocationClient == null ){
            Log.d(TAG, ">>>>>>>>>>>>> fuse Client nul");
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        }

        fusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback,
                Looper.getMainLooper());
        Log.d(TAG, ">>>>>>>>>>> updateLocation start");



    }



    private void requestAppIfo(){
        String pName = "com.ahranta.android.emergency.dev.user";
        HttpRequest requestAppInfo = new HttpRequest()
                .setUrl("http://dev.sosea.co.kr/device/getAppInfo.do")
                .setMethod(HttpUtils.Method.GET)
                .addParameter("type", String.valueOf(1))
                .addParameter("p",  pName)
                .setConnectTimeout(10)
                .setResponseBody(HttpUtils.ResponseBody.GSON)
                .setUseUiThread(true)
                .setListener(new HttpUtils.HttpResponseListener(){
                    @Override
                    public void onSuccess(HttpRequest req, Object result){

                        try{
                            JsonObject json = (JsonObject)result;
                            Log.e(TAG,  ">>>>> info.do" +result );

                        }
                        catch(Exception e){
                            Log.e(TAG,  e.getMessage());
                            onFailure(req);
                        }
                    }
                    @Override
                    public void onFailure(HttpRequest req){

                    }
                });
        requestAppInfo.execute(this);


    }

    private void requestEmergencyCallLocationResult(String  rmsg){
        Gson gson = new Gson();
        String msg = "{resultCode=1, uid='46cab33cc5094501a17089b8dcdacc06', provider=2, latitude=37.5198471, longitude=126.8902605, postalCode='150-093', countryCode='KR', locality='Seoul', addr='대한민국 서울특별시 영등포구 문래동3가 문래로', engAddr='Mullae-ro, Mullaedong 3(sam)-ga, Yeongdeungpo-gu, Seoul, South Korea', accuracy=16.834, timestamp=1660199327935}";
        String message = gson.toJson(rmsg);
        Log.d(TAG , ">>>>>>> message "+ message);


        HttpRequest req = new HttpRequest()
                .setUrl("http://dev.sosea.co.kr/device/emergency/call/location/result.do" )
                .setMethod(HttpUtils.Method.POST)
                .setResponseBody(HttpUtils.ResponseBody.GSON)
                .setUseUiThread(true)
//                .addParameterMap(null)
                .addParameter("message", message)
                .setListener(new HttpUtils.HttpResponseListener(){
                    @Override
                    public void onSuccess(com.example.watchApp.pizzawatchface.http.HttpRequest req, Object result) {
                        try{
                            JsonObject json = (JsonObject)result;
                            Log.d(TAG , ">>>>> result "+ result);
                        }
                        catch(Exception e){
                            Log.d(TAG, e.getMessage() );
                            onFailure(req);
                        }
                        finally{
                        }
                    }

                    @Override
                    public void onFailure(com.example.watchApp.pizzawatchface.http.HttpRequest req) {

                    }
                });

        req.execute(LocationActivity.this);
    }





    public void showToast(String str ){
        Toast.makeText(LocationActivity.this, ">>> "+ str , Toast.LENGTH_SHORT).show();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(fusedLocationClient != null )
            fusedLocationClient = null;
    }

    private int countTime = 0;
    public void checkHandler(){
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                countTime = countTime + 1;
                Log.d(TAG , ">>>>>>>>>>>>>>>>>>>>> getAppInfoData timer count "+ countTime);
                getAppInfoData();
            }
        }, 3000, 60 * 1000);

    }

    Retrofit retrofit;
    public void initRet(){
        if(retrofit != null ) return;

        String url = "http://dev.sosea.co.kr/";
         retrofit = new Retrofit.Builder()
                .baseUrl(url)
//                .addConverterFactory(new NullOnEmptyConverterFactory())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public void getAppInfoData(){
        AppInfoInterface appInfoInterface = retrofit.create(AppInfoInterface.class);
        appInfoInterface.getInfoCallback("1" , "com.ahranta.android.emergency.dev.user").enqueue(new Callback<AppInfoCallback>() {
            @Override
            public void onResponse(Call<AppInfoCallback> call, Response<AppInfoCallback> response) {
                Log.d(TAG, ">>>>>>>>>>>getAppInfoData onResponse "+ response.body());
            }

            @Override
            public void onFailure(Call<AppInfoCallback> callb, Throwable t) {
                Log.d(TAG, ">>>>>>>>>>>getAppInfoData onFailure t:"+ t.getMessage() );
            }
        });

    }

    @Override
    public void onClick(View v) {
        if(v.getId() == btn1.getId()){
            startService(MyMqttService.ACTION_START);
        }else if(v.getId() == btn2.getId()){
          stopService();
          endAlarmManager();
        }else if(v.getId() == btn3.getId()){
             startAlarmManager();
        }
    }

    private void endAlarmManager() {
        if (alarmIntent != null && alarmMgr != null) {
            Log.d(TAG, ">>>>>>>>> endAlarm " + alarmIntent);
            alarmMgr.cancel(alarmIntent);
            isSetAlarm = false;
        }
            Intent intent = new Intent( getApplicationContext(), AlarmBroadcastReceiver.class);
            intent.setAction(AlarmBroadcastReceiver.END_BROADCAST);
            sendBroadcast(intent);
    }


    // 사운드 모드 설정
    private void setSoundMode() {
        AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE); //진동모드로 변경

        String mode = null;
        switch (audioManager.getRingerMode()){
            case AudioManager.RINGER_MODE_NORMAL: mode ="노말모드"; break;
            case AudioManager.RINGER_MODE_VIBRATE: mode ="진동모드"; break;
            case AudioManager.RINGER_MODE_SILENT: mode ="무음모드"; break;
        }

        showToast("현재 사운드 설정 : "+mode );
    }


    private boolean isSetAlarm = false;
    private PendingIntent alarmIntent;
    private AlarmManager alarmMgr;
    private  void startAlarmManager(){
        startService(MyMqttService.ACTION_BROADCAST);  //서비스를 실행한다

        isSetAlarm = true;
        Intent intent = new Intent( getApplicationContext(), AlarmBroadcastReceiver.class);
        intent.setAction("appInfo.do");

         alarmIntent = PendingIntent.getBroadcast(this, 356 , intent,   PendingIntent.FLAG_UPDATE_CURRENT);
         alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
//        if (alarmMgr!= null) {
//            alarmMgr.cancel(alarmIntent);
//        }

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis()  );
//        cal.add(Calendar.SECOND , 5);
        long tritime = cal.getTimeInMillis() + 5000;
//        alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, tritime,  60 * 1000  , alarmIntent);
        alarmMgr.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, tritime,  alarmIntent);

        Log.d(TAG , ">>>>>>>>>>>> start alarm "+ cal.getTime());
    }


    private void startService(){
        startService("");
    }

//서비스 동작 실행
    private void startService(String action){
        Intent intent = new Intent(getApplicationContext(), MyMqttService.class);
        if(!TextUtils.isEmpty(action)  )intent.setAction(action);

        startForegroundService(intent);
    }

    //서비스 동작 중단
    private void stopService(){
        Intent intent = new Intent(getApplicationContext(), MyMqttService.class);
        intent.setAction(MyMqttService.ACTION_END);
        startService(intent);
    }


    public void checkNetworkInfo(){
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean isWifiConn = false;
        boolean isMobileConn = false;
        for (Network network : connMgr.getAllNetworks()) {
            NetworkInfo networkInfo = connMgr.getNetworkInfo(network);
            if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                isWifiConn |= networkInfo.isConnected();
            }
            if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                isMobileConn |= networkInfo.isConnected();
            }
        }
        String str = "Wifi connected: " + isWifiConn +" Mobile connected: " + isMobileConn;
        Log.d(TAG, str);
        showToast(str);

    }

    public boolean isOnline() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {

    }


    //리턴값중 null이 있을경우 사용
    public class NullOnEmptyConverterFactory extends Converter.Factory {

        @Override
        public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
            final Converter<ResponseBody, ?> delegate = retrofit.nextResponseBodyConverter(this, type, annotations);
            return new Converter<ResponseBody, Object>() {
                @Override
                public Object convert(ResponseBody body) throws IOException {
                    if (body.contentLength() == 0) return null;
                    return delegate.convert(body);                }
            };
        }
    }
}
