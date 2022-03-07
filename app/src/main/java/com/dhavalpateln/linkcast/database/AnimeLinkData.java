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
