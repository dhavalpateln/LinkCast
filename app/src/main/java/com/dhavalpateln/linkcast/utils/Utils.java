package com.dhavalpateln.linkcast.utils;

import android.content.Context;
import android.net.ConnectivityManager;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Utils {

    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

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

    public static byte[] hexlify(byte[] bytes) {
        byte[] result = new byte[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            result[j * 2] = (byte) hexArray[v >>> 4];
            result[j * 2 + 1] = (byte) hexArray[v & 0x0F];
        }
        return result;
    }

    public static byte[] unhexlify(String hexString) {
        byte[] result = new byte[hexString.length() / 2];
        for ( int j = 0; j < hexString.length(); j+=2 ) {
            result[j / 2] = (byte) ((Character.digit(hexString.charAt(j), 16) << 4)
                    + Character.digit(hexString.charAt(j+1), 16));
        }
        return result;
    }

    public static int getRandomInt(int low, int high) {
        return ThreadLocalRandom.current().nextInt(low, high);
    }
}
