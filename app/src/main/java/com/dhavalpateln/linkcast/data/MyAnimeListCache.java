package com.dhavalpateln.linkcast.data;

import com.dhavalpateln.linkcast.database.AnimeLinkData;
import com.dhavalpateln.linkcast.myanimelist.MyAnimelistAnimeData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyAnimeListCache {
    private static MyAnimeListCache storedAnimeLinkData;
    private Map<String, List<MyAnimelistAnimeData>> queryCache;
    private Map<String, MyAnimelistAnimeData> infoCache;

    private MyAnimeListCache() {
        queryCache = new HashMap<>();
        infoCache = new HashMap<>();
    }

    public static MyAnimeListCache getInstance() {
        if(storedAnimeLinkData == null) storedAnimeLinkData = new MyAnimeListCache();
        return storedAnimeLinkData;
    }

    public void storeCache(String url, List<MyAnimelistAnimeData> data) {
        queryCache.put(url, data);
    }
    public void storeCache(String url, MyAnimelistAnimeData data) {
        infoCache.put(url, data);
    }

    public MyAnimelistAnimeData getInfo(String url) {
        if(infoCache.containsKey(url)) {
            return infoCache.get(url);
        }
        return null;
    }

    public List<MyAnimelistAnimeData> getQueryResult(String url) {
        if(queryCache.containsKey(url)) {
            return queryCache.get(url);
        }
        return null;
    }
}
