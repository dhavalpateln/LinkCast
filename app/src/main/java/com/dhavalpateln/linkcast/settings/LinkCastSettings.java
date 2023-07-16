package com.dhavalpateln.linkcast.settings;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

public class LinkCastSettings {
    private static LinkCastSettings instance;
    private SharedPreferences prefs;

    private LinkCastSettings(Context context) {
        prefs = context.getSharedPreferences("com.dhavalpateln.linkcast_preferences", Context.MODE_PRIVATE);
    }

    public static synchronized LinkCastSettings getInstance(Context context) {
        if (instance == null) {
            instance = new LinkCastSettings(context);
        }
        return instance;
    }

    public void saveString(String key, String value) {
        prefs.edit().putString(key, value).apply();
    }

    public String getString(String key, String defaultValue) {
        return prefs.getString(key, defaultValue);
    }
}
