package com.dhavalpateln.linkcast.data;

import com.dhavalpateln.linkcast.myanimelist.MyAnimelistAnimeData;
import com.dhavalpateln.linkcast.myanimelist.MyAnimelistCharacterData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JikanCache {
    private static JikanCache jikanCache;
    private Map<String, List<MyAnimelistAnimeData>> queryCache;
    private Map<String, MyAnimelistAnimeData> animeInfoCache;
    private Map<String, MyAnimelistCharacterData> charactersCache;
    private List<String> seasonListCache;

    private JikanCache() {
        queryCache = new HashMap<>();
        animeInfoCache = new HashMap<>();
        charactersCache = new HashMap<>();
        seasonListCache = null;
    }

    public static JikanCache getInstance() {
        if(jikanCache == null) jikanCache = new JikanCache();
        return jikanCache;
    }

    public void storeCache(String url, List<MyAnimelistAnimeData> data) { queryCache.put(url, data); }
    public void storeCache(String url, MyAnimelistAnimeData data) {
        animeInfoCache.put(url, data);
    }
    public void storeCache(String url, MyAnimelistCharacterData data) { charactersCache.put(url, data); }

    public MyAnimelistAnimeData getInfo(String url) {
        if(animeInfoCache.containsKey(url)) {
            return animeInfoCache.get(url);
        }
        return null;
    }

    public List<MyAnimelistAnimeData> getQueryResult(String url) {
        if(queryCache.containsKey(url)) {
            return queryCache.get(url);
        }
        return null;
    }

    public MyAnimelistCharacterData getCharacterData(String url) {
        if(charactersCache.containsKey(url)) {
            return charactersCache.get(url);
        }
        return null;
    }

    public List<String> getSeasonListCache() {
        return seasonListCache;
    }

    public void setSeasonListCache(List<String> seasonListCache) {
        this.seasonListCache = seasonListCache;
    }
}
