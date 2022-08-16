package com.example.watchApp.pizzawatchface.util;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AppInfoCallback {

    @SerializedName("versionCode")
    @Expose
    public String versionCode;

    @SerializedName("versionName")
    @Expose
    public String versionName;

    @SerializedName("description")
    @Expose
    public String description;

    @SerializedName("redirectFileDownload")
    @Expose
    public String redirectFileDownload;

    @SerializedName("fileUri")
    @Expose
    public String fileUri;

    @SerializedName("lastNoticeSeq")
    @Expose
    public String lastNoticeSeq;

    @SerializedName("featureFlags")
    @Expose
    public String featureFlags;

    @Override
    public String toString() {
        return "AppInfoCallback{" +
                "versionCode='" + versionCode + '\'' +
                ", versionName='" + versionName + '\'' +
                ", description='" + description + '\'' +
                ", redirectFileDownload='" + redirectFileDownload + '\'' +
                ", fileUri='" + fileUri + '\'' +
                ", lastNoticeSeq='" + lastNoticeSeq + '\'' +
                ", featureFlags='" + featureFlags + '\'' +
                '}';
    }
}