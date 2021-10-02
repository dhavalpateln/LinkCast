package com.dhavalpateln.linkcast.animesources;

import java.util.Map;

public abstract class AnimeSource {

    public String animeSource;
    private String lastTitle = "";

    public AnimeSource() {}

    public abstract String getAnimeTitle(String currentURL, String searchTerm, boolean includeEpisode);
    public abstract String getSearchURL(String searchTerm);
    public abstract boolean isCorrectSource(String term);
    public abstract void updateBookmarkPage(String url, String id, String title);
    public abstract boolean containsAds(String urlString, boolean notFoundMP4, String currentWebViewURI);
    public abstract boolean isPlayable(String url);

    public String getAnimeSourceName() {
        return animeSource;
    }
    public String getLastCheckedUrl() { return null; }

    public void updateLastTitle(String title) { this.lastTitle = title; }
    public String getLastTitle() { return this.lastTitle; }


}
