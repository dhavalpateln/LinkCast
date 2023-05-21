package com.dhavalpateln.linkcast.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.Build;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Base64;
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

    public static byte[] stringToBytes(String data) {
        return data.getBytes(Charset.forName("UTF-8"));
    }
    public static String bytesToString(byte[] data) {
        return new String(data, StandardCharsets.UTF_8);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static byte[] b64decode(String data) {
        return Base64.getDecoder().decode(data);
    }

    public static byte[] extractBytes(byte[] data, int start, int end) {
        if(start < 0 || end > data.length)  {
            throw new IndexOutOfBoundsException("Out of bound data");
        }
        int length = end - start;
        byte[] result = new byte[length];
        for(int i = 0; i < length; i++) {
            result[i] = data[start + i];
        }
        return result;
    }

    public static int getRandomInt(int low, int high) {
        return ThreadLocalRandom.current().nextInt(low, high);
    }

    public static boolean compareNullableStrings(String a, String b) {
        if(a == b) return true;
        if(a == null || b == null) return false;
        return a.equals(b);
    }

    public static boolean isNumeric(String num) {
        if(num == null) return false;
        try {
            double n = Double.parseDouble(num);
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

    public static void startScaleAnimation(View view) {
        startScaleAnimation(view, 150);
    }
    public static void startScaleAnimation(View view, long durationMillis) {
        ScaleAnimation anim = new ScaleAnimation(
                0.0f,
                1.0f,
                0.0f,
                1.0f,
                Animation.RELATIVE_TO_SELF,
                0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f
        );
        anim.setDuration(durationMillis);
        view.startAnimation(anim);
    }

    public static void loadImage(Context context, ImageView view, String url) {
        Glide.with(context)
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(view);
    }

    public static int findIndexOf(String value, String[] items) {
        return findIndexOf(value, items, true);
    }

    public static int findIndexOf(String value, String[] items, boolean ignoreCase) {
        for(int i = 0; i < items.length; i++) {
            if(ignoreCase && items[i].equalsIgnoreCase(value))  return i;
            else if(!ignoreCase && items[i].equals(value))  return i;
        }
        return -1;
    }
}
