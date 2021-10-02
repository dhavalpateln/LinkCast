package com.dhavalpateln.linkcast.animesources;

public class AnimeOwl extends AnimeSource {
    @Override
    public String getAnimeTitle(String currentURL, String searchTerm, boolean includeEpisode) {
        return null;
    }

    @Override
    public String getSearchURL(String searchTerm) {
        return null;
    }

    @Override
    public boolean isCorrectSource(String term) {
        return false;
    }

    @Override
    public void updateBookmarkPage(String url, String id, String title) {

    }

    @Override
    public boolean containsAds(String urlString, boolean notFoundMP4, String currentWebViewURI) {
        return false;
    }

    @Override
    public boolean isPlayable(String url) {
        return false;
    }
}
