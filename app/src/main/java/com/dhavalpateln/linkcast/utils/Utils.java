package com.dhavalpateln.linkcast.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Utils {
    public static String getCurrentTime() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        String formattedDate = df.format(c.getTime());
        return formattedDate;
    }
}
