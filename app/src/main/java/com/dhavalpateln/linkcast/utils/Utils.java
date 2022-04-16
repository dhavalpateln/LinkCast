package com.dhavalpateln.linkcast.utils;

import android.content.Context;
import android.net.ConnectivityManager;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Utils {
    public static String getCurrentTime() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        String formattedDate = df.format(c.getTime());
        return formattedDate;
    }

    public static String getCurrentTime(String pattern) {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat(pattern);
        String formattedDate = df.format(c.getTime());
        return formattedDate;
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }

    private static boolean isInternetAvailable() {
        try {
            InetAddress address = InetAddress.getByName("www.google.com");
            return !address.equals("");
        } catch (UnknownHostException e) {
            // Log error
        }
        return false;
    }

    public static String capFirstLetters(String s) {
        String[] words = s.split(" ");
        String result = "";
        for(String word: words) {
            if(word.length() == 0) continue;
            result += word.substring(0, 1).toUpperCase() + word.substring(1) + " ";
        }
        return result;
    }

    public static boolean isInternetConnected(Context context) {
        return isNetworkAvailable(context) && isInternetAvailable();
    }
}
