package com.dhavalpateln.linkcast.animescrappers;

public class VideoURLData {
    private String title;
    private String url;
    private String referer;

    public VideoURLData(String title, String url, String referer) {
        this.title = title;
        this.url = url;
        this.referer = referer;
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

    public String getReferer() {
        return referer;
    }

    public void setReferer(String referer) {
        this.referer = referer;
    }

    public boolean hasReferer() {
        return this.referer != null;
    }
}
