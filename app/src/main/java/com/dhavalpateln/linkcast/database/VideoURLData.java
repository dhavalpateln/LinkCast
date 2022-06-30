package com.dhavalpateln.linkcast.database;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VideoURLData implements Serializable {
    private String source;
    private String title;
    private String url;
    private String referer;
    private Map<String, String> headers;
    private List<String> subtitles;
    private String linkCastID;
    private String episodeNum;

    public VideoURLData() { }

    public VideoURLData(String source, String title, String url, String referer) {
        this.source = source;
        this.title = title;
        this.url = url;
        this.referer = referer;
        headers = new HashMap<>();
        subtitles = new ArrayList<>();
        linkCastID = null;
        if(hasReferer()) headers.put("Referer", this.referer);
    }

    public VideoURLData(String url) {
        this("", "", url, null);
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
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

    public void addSubtitle(String url) {
        if(!subtitles.contains(url)) {
            subtitles.add(url);
        }
    }

    public boolean hasSubtitles() {
        return subtitles != null && !subtitles.isEmpty();
    }

    public List<String> getSubtitles() {return subtitles;}

    public String getLinkCastID() {
        return linkCastID;
    }

    public void setLinkCastID(String linkCastID) {
        this.linkCastID = linkCastID;
    }

    public boolean hasLinkCastID() {
        return this.linkCastID != null;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public void setSubtitles(List<String> subtitles) {
        this.subtitles = subtitles;
    }

    public String getEpisodeNum() {
        return episodeNum;
    }

    public void setEpisodeNum(String episodeNum) {
        this.episodeNum = episodeNum;
    }
}
