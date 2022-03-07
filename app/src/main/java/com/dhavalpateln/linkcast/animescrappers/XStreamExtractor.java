package com.dhavalpateln.linkcast.animescrappers;

import android.util.Log;

import com.dhavalpateln.linkcast.database.AnimeLinkData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class XStreamExtractor extends AnimeScrapper {

    private String TAG = "XStream";

    public XStreamExtractor(String baseUrl) {
        super(baseUrl);
    }

    @Override
    public boolean isCorrectURL(String url) {
        return false;
    }

    @Override
    public Map<String, String> getEpisodeList(String episodeListUrl) throws IOException {
        return null;
    }

    @Override
    public Map<String, String> extractEpisodeUrls(String episodeUrl) throws IOException {
        Map<String, String> result = new HashMap<>();
        String searchUrl = episodeUrl.replace("/f/", "/api/source/");
        Log.d(TAG, searchUrl);
        String jsonSearchResultString = postHttpContent(searchUrl);
        try {
            JSONObject jsonSearchResult = new JSONObject(jsonSearchResultString);
            JSONArray episodeList = jsonSearchResult.getJSONArray("data");
            for(int i = 0; i < episodeList.length(); i++) {
                JSONObject episode = episodeList.getJSONObject(i);
                //result.put(episode.getString("label"), episode.getString("file"));
                HttpURLConnection con = (HttpURLConnection) new URL(episode.getString("file")).openConnection();
                con.setInstanceFollowRedirects(false);
                con.connect();
                String realURL = con.getHeaderField("Location").toString();
                result.put(episode.getString("label"), realURL);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public Map<String, String> extractData(AnimeLinkData data) {
        return null;
    }
    @Override
    public String getDisplayName() {
        return "XStream";
    }
}
