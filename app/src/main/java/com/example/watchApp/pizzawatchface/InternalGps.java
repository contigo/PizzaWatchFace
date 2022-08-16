package com.example.watchApp.pizzawatchface;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.GnssStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

/**
 * Use internal GPS receiver to determine location.
 */
public class InternalGps implements LocationListener {
    private final static int rate_amb = 20000;
    private final static int rate_nor =  1000;

    private boolean running;
    private com.google.android.gms.location.LocationListener statusListener;
    private LocationManager locationManager;
    private Activity Activity;

    public InternalGps (Activity ma)
    {
        Activity = ma;
        locationManager = Activity.getSystemService (LocationManager.class);
        if (locationManager == null) {
            Toast.makeText(Activity.getApplicationContext(),  "no location manager" , Toast.LENGTH_SHORT).show();
        }
    }

    public boolean startSensor ()
    {
        if (ActivityCompat.checkSelfPermission (Activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions (Activity,
                    new String[] { Manifest.permission.ACCESS_FINE_LOCATION },
                    10101);
            return false;
        }

        int rate =  rate_amb ;
        locationManager.requestLocationUpdates (LocationManager.GPS_PROVIDER, rate, 0.0F, this);
        running = true;
        return true;
    }



    @Override  // LocationListener
    public void onLocationChanged (Location location)
    {
        System.out.println(">>>>>>>>>> ONLOCATION "+ location.getAccuracy() +", lat:"+ location.getLatitude() +", lon:"+ location.getLongitude());
//        statusListener.onLocationReceived (location);
    }

    @Override  // LocationListener
    public void onStatusChanged (String provider, int status, Bundle extras)
    { }

    @Override  // LocationListener
    public void onProviderEnabled (String provider)
    { }

    @Override  // LocationListener
    public void onProviderDisabled (String provider)
    { }

}
