package com.dhavalpateln.linkcast.animescrappers;

import java.util.HashMap;
import java.util.Map;

public class VideoURLData {
    private String title;
    private String url;
    private String referer;
    private Map<String, String> headers;

    public VideoURLData(String title, String url, String referer) {
        this.title = title;
        this.url = url;
        this.referer = referer;
        headers = new HashMap<>();
        if(hasReferer()) headers.put("Referer", this.referer);
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

    public void addHeader(String key, String value) { this.headers.put(key, value); }

    public Map<String, String> getHeaders() { return this.headers; }
}
