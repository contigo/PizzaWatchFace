package com.example.watchApp.pizzawatchface.util;

import android.content.Context;

import java.text.DecimalFormat;

public class FormatUtils {

    public static String formatDistance(double meters){
        return formatDistance((float) meters);
    }

    public static String formatDistance(float meters){

        if(meters < 1000){
            return ((int) meters) + " m";
        }
        else if(meters < 10000){
            return formatDec(meters / 1000f, 1) + " km";
        }
        else{
            return ((int) (meters / 1000f)) + " km";
        }
    }

    private static String formatDec(float val, int dec){

        int factor = (int) Math.pow(10, dec);

        int front = (int) (val);
        int back = (int) Math.abs(val * (factor)) % factor;

        return front + "." + back;
    }

    public static String formatFileSize(Context context, long size){

        if(size <= 0)
            return "0";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }
}
