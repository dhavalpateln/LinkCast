package com.dhavalpateln.linkcast.database.kitsu;

public class KitsuEpisodeData {
    private String title;
    private String description;
    private String thumbnail;
    private String number;


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        description = description.replaceAll("\\(Source: .*\\)", "").trim();
        this.description = description;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }
}
