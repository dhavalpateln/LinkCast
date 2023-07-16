package com.dhavalpateln.linkcast.database.room;

import androidx.room.TypeConverter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Converters {

    @TypeConverter
    public static String mapToString(Map<String, String> data) {
        JSONObject obj = new JSONObject();
        for(Map.Entry<String, String> entry: data.entrySet()) {
            try {
                obj.put(entry.getKey(), entry.getValue());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return obj.toString();
    }

    @TypeConverter
    public static Map<String, String> stringToMap(String data) {
        Map<String, String> result = new HashMap<>();
        try {
            JSONObject obj = new JSONObject(data);
            Iterator<String> iterator = obj.keys();
            while(iterator.hasNext()) {
                String key = iterator.next();
                result.put(key, obj.getString(key));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }
}
