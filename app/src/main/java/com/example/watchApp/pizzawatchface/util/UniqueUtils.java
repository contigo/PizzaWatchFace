package com.example.watchApp.pizzawatchface.util;

import android.content.Context;
import android.provider.Settings;
import android.telephony.TelephonyManager;


import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class UniqueUtils {


    public static final int FLAG_GET_DASH = 1;

    public static String getRandUUID(){
        return getRandUUID(0);
    }

    public static String getRandUUID(int flags){
        String uuid = UUID.randomUUID().toString();

        if(!FlagUtils.bitwise(flags, FLAG_GET_DASH))
            uuid = uuid.replaceAll("-", "");

        return uuid;
    }

    public static String getDeviceUUID(Context context){

        UUID uuid = null;
        final String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        if(!"9774d56d682e549c".equals(androidId)){
            uuid = UUID.nameUUIDFromBytes(androidId.getBytes(StandardCharsets.UTF_8));
        }
        else{
            final String deviceId = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
            uuid = deviceId != null ? UUID.nameUUIDFromBytes(deviceId.getBytes(StandardCharsets.UTF_8)) : UUID.randomUUID();
        }

        return uuid.toString().replace("-", "");
    }
}
