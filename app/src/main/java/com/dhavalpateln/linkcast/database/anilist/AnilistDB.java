package com.dhavalpateln.linkcast.database.anilist;

import android.util.Log;

import com.dhavalpateln.linkcast.utils.SimpleHttpClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

public class AnilistDB {

    private static AnilistDB instance;
    private String apiUrl;
    private Map<String, JSONObject> cache;

    private AnilistDB() {
        this.apiUrl = "https://graphql.anilist.co/";
        this.cache = new HashMap<>();
    }

    public static AnilistDB getInstance() {
        if(instance == null)    instance = new AnilistDB();
        return instance;
    }

    public JSONObject fetchData(JSONObject query) {
        if(!this.cache.containsKey(query)) {
            try {
                HttpURLConnection urlConnection = SimpleHttpClient.getURLConnection(this.apiUrl);
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Accept", "application/json");
                SimpleHttpClient.setPayload(urlConnection, query);
                String resString = SimpleHttpClient.getResponse(urlConnection);
                JSONObject response = new JSONObject(resString);
                this.cache.put(query.toString(), response);
            } catch (IOException | JSONException e) {
                Log.d("AnilistDB", "Exception " + e);
                return null;
            }
        }
        return this.cache.get(query.toString());
    }

    public String getBannerImage(String malID, boolean isAnime) {
        try {
            JSONObject payload = new JSONObject();
            payload.put("variables", new JSONObject("{'idMal': " + malID + "}"));
            payload.put("query", "query ($idMal: Int) { \n" +
                    "  Media (idMal: $idMal, type: " + (isAnime ? "ANIME" : "MANGA") + ") { \n" +
                    "    idMal\n" +
                    "    bannerImage\n" +
                    "  }\n" +
                    "}");

            JSONObject result = fetchData(payload);
            if(result == null)  return null;
            JSONObject data = result.getJSONObject("data").getJSONObject("Media");
            if(data.getString("bannerImage") != null) {
                return data.getString("bannerImage");
            }

        } catch (JSONException e) {
            return null;
        }
        return null;
    }

}
