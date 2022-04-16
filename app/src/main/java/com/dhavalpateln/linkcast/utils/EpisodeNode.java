package com.dhavalpateln.linkcast.utils;

public class EpisodeNode {
    private double episodeNum;
    private String episodeNumString;
    private String url;

    public EpisodeNode(String episodeNumString, String url) {
        this.episodeNumString = episodeNumString;
        this.episodeNum = Double.valueOf(episodeNumString);
        this.url = url;
    }

    public double getEpisodeNum() {
        return episodeNum;
    }

    public String getEpisodeNumString() {
        return episodeNumString;
    }

    public String getUrl() {
        return url;
    }
}
