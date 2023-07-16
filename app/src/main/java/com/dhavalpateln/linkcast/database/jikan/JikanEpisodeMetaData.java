package com.dhavalpateln.linkcast.database.jikan;

public class JikanEpisodeMetaData {
    private String title;
    private boolean isFiller;

    public JikanEpisodeMetaData(String title, boolean isFiller) {
        this.title = title;
        this.isFiller = isFiller;
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
}
