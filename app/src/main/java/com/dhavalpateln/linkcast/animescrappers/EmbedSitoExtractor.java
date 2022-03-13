package com.dhavalpateln.linkcast.animescrappers;

import com.dhavalpateln.linkcast.database.AnimeLinkData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmbedSitoExtractor extends AnimeScrapper {

    public EmbedSitoExtractor(String baseUrl) {
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
    public void extractEpisodeUrls(String episodeUrl, List<VideoURLData> result) {
        try {
            String searchUrl = episodeUrl.replace("https://embedsito.com/f/", "https://embedsito.com/api/source/");
            String jsonSearchResultString = postHttpContent(searchUrl);
            JSONObject jsonSearchResult = new JSONObject(jsonSearchResultString);
            JSONArray episodeList = jsonSearchResult.getJSONArray("data");
            for(int i = 0; i < episodeList.length(); i++) {
                JSONObject episode = episodeList.getJSONObject(i);
                VideoURLData urlData = new VideoURLData("Embed - " + episode.getString("label"), episode.getString("file"), null);
                result.add(urlData);
            }
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Map<String, String> extractData(AnimeLinkData data) {
        return null;
    }
    @Override
    public String getDisplayName() {
        return "EmbedSito";
    }
}
