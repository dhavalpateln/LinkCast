package com.dhavalpateln.linkcast.database;

import com.dhavalpateln.linkcast.utils.Utils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class AnimeLinkData implements Serializable {

    private String id;
    private String title;
    private String url;
    private Map<String, String> data;

    public static class DataContract {
        public static final String TITLE = "title";
        public static final String URL = "url";
        public static final String DATA = "data";
        public static final String DATA_MODE = "mode";
        public static final String DATA_STATUS = "status";
        public static final String DATA_IMAGE_URL = "imageUrl";
        public static final String DATA_EPISODE_NUM = "episodenumtext";
        public static final String DATA_ANIMEPAHE_SEARCH_ID = "pahesearchid";
        public static final String DATA_ANIMEPAHE_SESSION = "pahesession";
        public static final String DATA_FAVORITE = "fav";
        public static final String DATA_SOURCE = "source";
        public static final String DATA_MYANIMELIST_ID = "malid";
        public static final String DATA_MYANIMELIST_URL = "malurl";
        public static final String DATA_USER_SCORE = "userscore";
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Map<String, String> getData() {
        if(this.data == null) {
            this.data = new HashMap<>();
            this.data.put("status", "Planned");
        }
        return data;
    }

    public String getAnimeMetaData(String key) {
        if(getData().containsKey(key))  return getData().get(key);
        switch (key) {
            case DataContract.DATA_FAVORITE:
                return "false";
            case DataContract.DATA_STATUS:
                return "Planned";
            case DataContract.DATA_SOURCE:
                return "";
            case DataContract.DATA_EPISODE_NUM:
                return "Episode - 0";
            case DataContract.DATA_USER_SCORE:
                return "0";
            default:
                return null;
        }
    }

    public void updateData(String key, String value) {
        updateData(key, value, true, true);
    }

    public void updateData(String key, String value, boolean updateFirebase) {
        updateData(key, value, updateFirebase, true);
    }

    public void updateData(String key, String value, boolean updateFirebase, boolean isAnime) {
        getData().put(key, value);
        if(updateFirebase && getId() != null) {
            if(isAnime) {
                FirebaseDBHelper.getUserAnimeWebExplorerLinkRef(getId()).child("data").child(key).setValue(value);
            }
            else {
                FirebaseDBHelper.getUserMangaWebExplorerLinkRef(getId()).child("data").child(key).setValue(value);
            }
        }
    }

    public void updateAll(boolean isAnime) {

        if(id == null)  id = Utils.getCurrentTime();

        Map<String, Object> update = new HashMap<>();
        update.put(id + "/title", getTitle());
        update.put(id + "/url", getUrl());

        for(String key: getData().keySet()) {
            update.put(id + "/data/" + key, getData().get(key));
        }
        if(isAnime)   FirebaseDBHelper.getUserAnimeWebExplorerLinkRef().updateChildren(update);
        else FirebaseDBHelper.getUserMangaWebExplorerLinkRef().updateChildren(update);
    }

    public void setData(Map<String, String> data) {

        this.data = data;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void copyFrom(AnimeLinkData animeLinkData) {
        setTitle(animeLinkData.getTitle());
        setUrl(animeLinkData.getUrl());
        Map<String, String> dataMap = getData();
        for(Map.Entry<String, String> dataEntry: animeLinkData.getData().entrySet()) {
            dataMap.put(dataEntry.getKey(), dataEntry.getValue());
        }
    }
}
