package com.dhavalpateln.linkcast.data;

import com.dhavalpateln.linkcast.database.AnimeLinkData;

import java.util.Map;

public class StoredAnimeLinkData {
    private static StoredAnimeLinkData storedAnimeLinkData;
    private Map<String, AnimeLinkData> animeCache;
    private Map<String, AnimeLinkData> mangaCache;

    public static StoredAnimeLinkData getInstance() {
        if(storedAnimeLinkData == null) storedAnimeLinkData = new StoredAnimeLinkData();
        return storedAnimeLinkData;
    }

    public void updateAnimeCache(Map<String, AnimeLinkData> cache) {
        this.animeCache = cache;
    }

    public Map<String, AnimeLinkData> getAnimeCache() {
        return this.animeCache;
    }

    public AnimeLinkData getAnimeLinkData(String id) {
        if(this.animeCache == null || !this.animeCache.containsKey(id))   return null;
        return this.animeCache.get(id);
    }

    public void updateMangaCache(Map<String, AnimeLinkData> cache) {
        this.mangaCache = cache;
    }

    public Map<String, AnimeLinkData> getMangaCache() {
        return this.mangaCache;
    }
}
