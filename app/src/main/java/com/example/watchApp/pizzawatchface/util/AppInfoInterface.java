package com.example.watchApp.pizzawatchface.util;


import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface AppInfoInterface {
    //http://dev.sosea.co.kr/device/getAppInfo.do?type=1&p=com.ahranta.android.emergency.dev.user
    @GET("device/getAppInfo.do")
    Call<AppInfoCallback> getInfoCallback(
            @Query("type") String type ,
            @Query("p") String p
    );

    @GET("device/getAppInfo.do")
    Call<AppInfoCallback> getLocationoCallback(
            @Query("type") String type ,
            @Query("p") String p
    );
}