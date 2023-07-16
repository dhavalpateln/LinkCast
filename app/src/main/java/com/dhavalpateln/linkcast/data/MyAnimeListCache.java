package com.dhavalpateln.linkcast.data;

import com.dhavalpateln.linkcast.myanimelist.MyAnimelistAnimeData;
import com.dhavalpateln.linkcast.myanimelist.MyAnimelistCharacterData;
import com.dhavalpateln.linkcast.database.EpisodeNode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyAnimeListCache {
    private static MyAnimeListCache storedAnimeLinkData;
    private Map<String, List<MyAnimelistAnimeData>> queryCache;
    private Map<String, MyAnimelistAnimeData> infoCache;
    private Map<String, MyAnimelistCharacterData> characterCache;
    private Map<String, List<MyAnimelistCharacterData>> animeCharactersCache;
    private Map<String, List<EpisodeNode>> episodeNodes;

    private MyAnimeListCache() {
        queryCache = new HashMap<>();
        infoCache = new HashMap<>();
        characterCache = new HashMap<>();
        animeCharactersCache = new HashMap<>();
        episodeNodes = new HashMap<>();
    }

    public static MyAnimeListCache getInstance() {
        if(storedAnimeLinkData == null) storedAnimeLinkData = new MyAnimeListCache();
        return storedAnimeLinkData;
    }

    public void storeAnimeCache(String url, List<MyAnimelistAnimeData> data) {
        queryCache.put(url, data);
    }
    public void storeAnimeCache(String url, MyAnimelistAnimeData data) {
        infoCache.put(url, data);
    }
    public void storeCharacterCache(String url, MyAnimelistCharacterData data) {
        characterCache.put(url, data);
    }
    public void storeCharacterCache(String url, List<MyAnimelistCharacterData> data) {
        animeCharactersCache.put(url, data);
    }

    public void storeEpisodeNodesCache(String url, List<EpisodeNode> data) {
        episodeNodes.put(url, data);
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

    public List<MyAnimelistCharacterData> getAnimeCharacterList(String url) {
        if(animeCharactersCache.containsKey(url)) {
            return animeCharactersCache.get(url);
        }
        return null;
    }

    public MyAnimelistCharacterData getCharacterData(String url) {
        if(characterCache.containsKey(url)) {
            return characterCache.get(url);
        }
        return null;
    }

    public List<EpisodeNode> getEpisodeNodes(String url) {
        if(episodeNodes.containsKey(url)) {
            return episodeNodes.get(url);
        }
        return null;
    }
}
