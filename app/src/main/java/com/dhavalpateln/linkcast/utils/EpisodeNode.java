package com.dhavalpateln.linkcast.utils;

public class EpisodeNode {
    private double episodeNum;
    private String episodeNumString;
    private String url;
    private EpisodeType type;

    public enum EpisodeType {
        ANIME,
        MANGA,
        MANHWA
    }

    public EpisodeNode(String episodeNumString, String url) {
        this.episodeNumString = episodeNumString;
        this.episodeNum = Double.valueOf(episodeNumString);
        this.url = url;
        this.type = EpisodeType.ANIME;
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

    public EpisodeType getType() {
        return type;
    }

    public void setType(EpisodeType type) {
        this.type = type;
    }

    public boolean isAnime() {  return type == EpisodeType.ANIME;  }
    public boolean isManga() {  return type == EpisodeType.MANGA;  }
    public boolean isManhwa() {  return type == EpisodeType.MANHWA;  }
}
