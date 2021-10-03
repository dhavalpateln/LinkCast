package com.dhavalpateln.linkcast.animesources;

public class EmbedSito extends AnimeSource {
    @Override
    public String getAnimeTitle(String currentURL, String searchTerm, boolean includeEpisode) {
        return searchTerm;
    }

    @Override
    public String getSearchURL(String searchTerm) {
        return searchTerm;
    }

    @Override
    public boolean isCorrectSource(String term) {
        return term.startsWith("https://embedsito.com/");
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
        return url.contains(".mp4") || url.contains(".m3u8");
    }
}
