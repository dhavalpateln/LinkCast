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
    public Map<String, String> getEpisodeList(String episodeListUrl) {
        return null;
    }

    @Override
    public Map<String, VideoURLData> extractEpisodeUrls(String episodeUrl) {
        Map<String, VideoURLData> result = new HashMap<>();
        try {
            String searchUrl = episodeUrl.replace("/f/", "/api/source/");
            Log.d(TAG, searchUrl);
            String jsonSearchResultString = postHttpContent(searchUrl);
            JSONObject jsonSearchResult = new JSONObject(jsonSearchResultString);
            JSONArray episodeList = jsonSearchResult.getJSONArray("data");
            for(int i = 0; i < episodeList.length(); i++) {
                JSONObject episode = episodeList.getJSONObject(i);
                //result.put(episode.getString("label"), episode.getString("file"));
                HttpURLConnection con = (HttpURLConnection) new URL(episode.getString("file")).openConnection();
                con.setInstanceFollowRedirects(false);
                con.connect();
                String realURL = con.getHeaderField("Location").toString();
                VideoURLData videoURLData = new VideoURLData("XStream - " + episode.getString("label"), realURL, null);
                result.put(videoURLData.getTitle(), videoURLData);
            }
        } catch (JSONException | IOException e) {
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
