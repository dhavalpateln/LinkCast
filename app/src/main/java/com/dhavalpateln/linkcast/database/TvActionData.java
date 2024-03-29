package com.dhavalpateln.linkcast.database;

public class TvActionData {
    private VideoURLData videoData;
    private String action;
    private String id;
    private String episodeNum;
    private String timestamp;
    private boolean resumeOption;
    public TvActionData() {}

    public VideoURLData getVideoData() {
        return videoData;
    }

    public void setVideoData(VideoURLData videoData) {
        this.videoData = videoData;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEpisodeNum() {
        return episodeNum;
    }

    public void setEpisodeNum(String episodeNum) {
        this.episodeNum = episodeNum;
    }

    public boolean getResumeOption() {
        return resumeOption;
    }

    public void setResumeOption(boolean resumeOption) {
        this.resumeOption = resumeOption;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
