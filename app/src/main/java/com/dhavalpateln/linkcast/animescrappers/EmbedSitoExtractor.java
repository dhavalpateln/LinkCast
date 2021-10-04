package com.dhavalpateln.linkcast.animescrappers;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
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
    public Map<String, String> getEpisodeList(String episodeListUrl) throws IOException {
        return null;
    }

    @Override
    public Map<String, String> extractEpisodeUrls(String episodeUrl) throws IOException {
        Map<String, String> result = new HashMap<>();
        String searchUrl = episodeUrl.replace("https://embedsito.com/f/", "https://embedsito.com/api/source/");
        String jsonSearchResultString = postHttpContent(searchUrl);
        try {
            JSONObject jsonSearchResult = new JSONObject(jsonSearchResultString);
            JSONArray episodeList = jsonSearchResult.getJSONArray("data");
            for(int i = 0; i < episodeList.length(); i++) {
                JSONObject episode = episodeList.getJSONObject(i);
                result.put(episode.getString("label"), episode.getString("file"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public String extractData() {
        return null;
    }
    @Override
    public String getDisplayName() {
        return "EmbedSito";
    }
}
