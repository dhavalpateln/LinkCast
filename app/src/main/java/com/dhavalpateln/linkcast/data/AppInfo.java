package com.dhavalpateln.linkcast.data;

import com.google.firebase.database.DataSnapshot;

public class AppInfo {
    private static AppInfo appInfo;

    private String apkVersion;
    private String winVersion;
    private String apkURL;
    private String winURL;

    private AppInfo() {

    }

    public static AppInfo getInstance() {
        if(appInfo == null) {
            appInfo = new AppInfo();
        }
        return appInfo;
    }

    public void updateData(DataSnapshot dataSnapshot) {
        this.apkVersion = dataSnapshot.child("version").getValue().toString();
        this.apkURL = dataSnapshot.child("link").getValue().toString();
        this.winVersion = dataSnapshot.child("webappver").getValue().toString();
        this.winURL = dataSnapshot.child("winlauncher").getValue().toString();
    }

    public String getApkVersion() {
        return apkVersion;
    }

    public String getWinVersion() {
        return winVersion;
    }

    public String getApkURL() {
        return apkURL;
    }

    public String getWinURL() {
        return winURL;
    }
}
