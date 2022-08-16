package com.example.watchApp.pizzawatchface.mqtt;

import android.app.ActivityManager;
import android.content.Context;
import android.nfc.Tag;
import android.util.Log;

public class ServiceUtil {


    public static boolean isServiceRunning(Context context, Class<?> serviceClass){
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for(ActivityManager.RunningServiceInfo serviceInfo : manager.getRunningServices(Integer.MAX_VALUE)){
            if(serviceInfo.service.getPackageName().equals(context.getPackageName())
                    && serviceClass.getName().equals(serviceInfo.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
