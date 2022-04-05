package com.dhavalpateln.linkcast.database;

import java.util.HashMap;
import java.util.Map;

public class AnimeLinkData {

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
            default:
                return null;
        }
    }

    public void updateData(String key, String value) {
        updateData(key, value, true);
    }

    public void updateData(String key, String value, boolean updateFirebase) {
        getData().put(key, value);
        if(updateFirebase && getId() != null) {
            FirebaseDBHelper.getUserAnimeWebExplorerLinkRef(getId()).child("data").child(key).setValue(value);
        }
    }

    public void updateAll() {
        Map<String, Object> update = new HashMap<>();
        update.put(id + "/title", getTitle());
        update.put(id + "/url", getUrl());

        for(String key: getData().keySet()) {
            update.put(id + "/data/" + key, getData().get(key));
        }
        FirebaseDBHelper.getUserAnimeWebExplorerLinkRef().updateChildren(update);
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
}
