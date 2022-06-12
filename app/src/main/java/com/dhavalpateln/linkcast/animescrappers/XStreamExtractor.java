package com.dhavalpateln.linkcast.animescrappers;

import android.util.Log;

import com.dhavalpateln.linkcast.database.AnimeLinkData;
import com.dhavalpateln.linkcast.utils.EpisodeNode;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XStreamExtractor extends AnimeScrapper {

    private String TAG = "XStream";

    public XStreamExtractor() {
        super();
    }

    @Override
    public boolean isCorrectURL(String url) {
        return false;
    }

    @Override
    public List<EpisodeNode> getEpisodeList(String episodeListUrl) {
        return null;
    }

    @Override
    public void extractEpisodeUrls(String episodeUrl, List<VideoURLData> result) {
        try {
            String searchUrl = episodeUrl.replace("/v/", "/api/source/");
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
                VideoURLData videoURLData = new VideoURLData("XStream", "XStream - " + episode.getString("label"), realURL, null);
                result.add(videoURLData);
            }
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<EpisodeNode> extractData(AnimeLinkData data) {
        return null;
    }
    @Override
    public String getDisplayName() {
        return "XStream";
    }
}
