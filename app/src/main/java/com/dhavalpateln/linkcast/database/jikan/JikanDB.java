package com.dhavalpateln.linkcast.database.jikan;


import com.dhavalpateln.linkcast.database.EpisodeNode;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class JikanDB {
    private Map<String, Map<String, JikanEpisodeMetaData>> episodeMetaData;
    private String apiUrl;
    private static JikanDB instance;

    private JikanDB() {
        this.episodeMetaData = new HashMap<>();
        this.apiUrl = "https://api.jikan.moe/v4";
    }

    public static JikanDB getInstance() {
        if(instance == null) instance = new JikanDB();
        return instance;
    }



    public void fetchEpisodeMetaData(String malID) {
        if(episodeMetaData.containsKey(malID))  return;
        Map<String, JikanEpisodeMetaData> dataMap = new HashMap<>();
        boolean hasNextPage = true;
        int page = 1;
        while (hasNextPage && page < 30) {

            page++;
        }
    }
}
