package com.dhavalpateln.linkcast.database.anilist;

import android.util.Log;

import com.dhavalpateln.linkcast.database.room.almaldata.AlMalMetaData;
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

    /*public JSONObject fetchData(String query, String variables) {
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
    }*/

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

    public AlMalMetaData getAlMalMetaData(String malID, boolean isAnime) {
        AlMalMetaData result = new AlMalMetaData();
        result.setId(malID);
        try {
            JSONObject payload = new JSONObject();
            payload.put("variables", new JSONObject("{'idMal': " + malID + "}"));
            payload.put("query", "query ($idMal: Int) {\n" +
                    "  Media (idMal: $idMal, type: " + (isAnime ? "ANIME" : "MANGA") + ") { \n" +
                    "        bannerImage\n" +
                    "        title {\n" +
                    "            romaji\n" +
                    "            english\n" +
                    "        }\n" +
                    "        format\n" +
                    "        status\n" +
                    "        description\n" +
                    "        startDate {\n" +
                    "            year\n" +
                    "            month\n" +
                    "            day\n" +
                    "        }\n" +
                    "        endDate {\n" +
                    "            year\n" +
                    "            month\n" +
                    "            day\n" +
                    "        }\n" +
                    "        season\n" +
                    "        episodes\n" +
                    "        duration\n" +
                    "        chapters\n" +
                    "        synonyms\n" +
                    "    }\n" +
                    "}");

            JSONObject jsonResult = fetchData(payload);
            if(jsonResult == null)  return result;
            JSONObject data = jsonResult.getJSONObject("data").getJSONObject("Media");

            result.setEngName(data.getJSONObject("title").getString("english"));
            result.setName(data.getJSONObject("title").getString("romaji"));
            if(isAnime) result.setTotalEpisodes(data.getString("episodes"));
            else result.setTotalEpisodes(data.getString("chapters"));
            result.setStatus(data.getString("status"));
            result.setUrl("https://myanimelist.net/" + (isAnime ? "anime" : "manga") + "/" + malID);
            result.setImageURL(data.getString("bannerImage"));
            JSONObject startDate = data.getJSONObject("startDate");
            result.setAirDate(startDate.getString("year") + "-" + startDate.getString("month") + "-" + startDate.getString("day"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

}
