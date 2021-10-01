package com.dhavalpateln.linkcast.animesources;

import java.net.URLEncoder;

public class AnimeUltima extends AnimeSource {

    public AnimeUltima() {
        this.animeSource = "animeultima.to";
    }

    @Override
    public String getAnimeTitle(String currentURL, String searchTerm, boolean includeEpisode) {
        return searchTerm;
    }

    @Override
    public String getSearchURL(String searchTerm) {
        return "https://www1.animeultima.to/search?search=" + URLEncoder.encode(searchTerm);
    }

    @Override
    public boolean isCorrectSource(String term) {
        return term.contains("animeultima.to");
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
