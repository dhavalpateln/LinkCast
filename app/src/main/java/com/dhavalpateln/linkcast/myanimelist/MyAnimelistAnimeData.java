package com.dhavalpateln.linkcast.myanimelist;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyAnimelistAnimeData implements Serializable {
    private int id;
    private String title;
    private String url;
    private String[] alternateTitles;
    private List<String> images;

    String synopsis;
    private Map<String, String> infos;

    public MyAnimelistAnimeData(int id) {
        this.id = id;
        infos = new HashMap<>();
        this.images = new ArrayList<>();
    }

    public int getId() {
        return id;
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

    public String[] getAlternateTitles() {
        return alternateTitles;
    }

    public void setAlternateTitles(String[] alternateTitles) {
        this.alternateTitles = alternateTitles;
    }

    public void putInfo(String key, String value) {
        infos.put(key, value);
    }

    public String getInfo(String key) {
        if(!infos.containsKey(key)) return "N/A";
        return infos.get(key);
    }

    public String getSynopsis() {
        if(synopsis == null)    return "N/A";
        return synopsis;
    }

    public void setSynopsis(String synopsis) {
        this.synopsis = synopsis;
    }

    public List<String> getImages() {
        return images;
    }

    public void addImage(String imageURL) {
        if(!this.images.contains(imageURL))  this.images.add(imageURL);
    }
}
