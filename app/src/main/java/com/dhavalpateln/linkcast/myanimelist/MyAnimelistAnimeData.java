package com.dhavalpateln.linkcast.myanimelist;

public class MyAnimelistAnimeData {
    private int id;
    private String title;
    private String url;
    private String[] alternateTitles;

    public MyAnimelistAnimeData(int id) {
        this.id = id;
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
}
