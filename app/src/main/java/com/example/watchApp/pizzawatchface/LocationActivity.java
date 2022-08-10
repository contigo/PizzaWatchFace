package com.example.watchApp.pizzawatchface;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.watchApp.pizzawatchface.databinding.ActivityLocationBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnSuccessListener;

import androidx.core.app.ActivityCompat;

import static com.example.watchApp.pizzawatchface.Constants.TAG;

public class LocationActivity extends Activity {
    private ImageButton imgButton;
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
        imgButton = binding.imgicon;
        imgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateLocation();
            }
        });


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION },
                    1);
            return;
        }else {
            Toast.makeText(LocationActivity.this, "!!!!!!!!! p check!!!!! ", Toast.LENGTH_SHORT).show();
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(LocationActivity.this);
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            Log.d(TAG,">>>>>>>>>>>> lat:" + location.getLatitude() + ", lon:" + location.getLongitude());
                            mText.setText("lat:" + location.getLatitude() + ", lon:" + location.getLongitude());
                        }else{
                            Log.d(TAG,">>>>>>>>>>>> location is null");
                            mText.setText(" location is null");
                        }
                    }
                });
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


    public void showToast(String str ){
        Toast.makeText(LocationActivity.this, ">>>>>>>>>>>>>>> "+ str , Toast.LENGTH_SHORT).show();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(fusedLocationClient != null )
            fusedLocationClient = null;
    }
}
