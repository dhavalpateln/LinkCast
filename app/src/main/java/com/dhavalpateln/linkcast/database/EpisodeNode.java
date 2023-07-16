package com.dhavalpateln.linkcast.database;

public class EpisodeNode {
    private double episodeNum;
    private String episodeNumString;
    private String url;

    private String title;
    private EpisodeType type;
    private int season;
    private String note;
    private String description;
    private String thumbnail;
    private boolean isFiller;

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
        this.season = 0;
        this.isFiller = false;
    }

    public double getEpisodeNum() {
        return episodeNum;
    }

    public String getEpisodeNumString() {
        return episodeNumString;
    }

    public void setEpisodeNumString(String episodeNumString) {
        this.episodeNumString = episodeNumString;
        this.episodeNum = Double.valueOf(episodeNumString);
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

    public int getSeason() {
        return season;
    }

    public void setSeason(int season) {
        this.season = season;
    }

    public boolean isAnime() {  return type == EpisodeType.ANIME;  }
    public boolean isManga() {  return type == EpisodeType.MANGA;  }
    public boolean isManhwa() {  return type == EpisodeType.MANHWA;  }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isFiller() {
        return isFiller;
    }

    public void setFiller(boolean filler) {
        isFiller = filler;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }
}
