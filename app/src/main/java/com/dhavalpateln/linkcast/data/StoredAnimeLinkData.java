package com.dhavalpateln.linkcast.data;

import com.dhavalpateln.linkcast.database.AnimeLinkData;

import java.util.Map;

public class StoredAnimeLinkData {
    private static StoredAnimeLinkData storedAnimeLinkData;
    private Map<String, AnimeLinkData> cache;
    private Map<String, AnimeLinkData> mangaCache;

    public static StoredAnimeLinkData getInstance() {
        if(storedAnimeLinkData == null) storedAnimeLinkData = new StoredAnimeLinkData();
        return storedAnimeLinkData;
    }

    public void updateCache(Map<String, AnimeLinkData> cache) {
        this.cache = cache;
    }

    public Map<String, AnimeLinkData> getCache() {
        return this.cache;
    }

    public AnimeLinkData getAnimeLinkData(String id) {
        if(this.cache == null || !this.cache.containsKey(id))   return null;
        return this.cache.get(id);
    }

    public void updateMangaCache(Map<String, AnimeLinkData> cache) {
        this.mangaCache = cache;
    }

    public Map<String, AnimeLinkData> getMangaCache() {
        return this.mangaCache;
    }
}
