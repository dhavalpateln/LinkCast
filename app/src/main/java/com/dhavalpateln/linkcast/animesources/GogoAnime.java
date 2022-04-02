package com.dhavalpateln.linkcast.animesources;

import android.net.Uri;

import com.dhavalpateln.linkcast.ProvidersData;

public class GogoAnime extends AnimeSource {
    @Override
    public String getAnimeTitle(String currentURL, String searchTerm, boolean includeEpisode) {
        return searchTerm;
    }

    @Override
    public String getSearchURL(String searchTerm) {
        return ProvidersData.GOGOANIME.URL + "/search.html?keyword=" + Uri.encode(searchTerm);
    }

    @Override
    public boolean isCorrectSource(String term) {
        return term.contains(ProvidersData.GOGOANIME.NAME);
    }

    @Override
    public void updateBookmarkPage(String url, String id, String title) {

    }

    @Override
    public boolean containsAds(String urlString, boolean notFoundMP4, String currentWebViewURI) {
        if(urlString.contains(".mp4") && notFoundMP4) return false;
        if(urlString.contains(".m3u8") && notFoundMP4) return false;
        if(urlString.startsWith("https://streamani.net/")) return false;
        if(urlString.startsWith("https://dood.la/")) return false;
        if(urlString.startsWith("https://hydrax.net/")) return false;
        if(urlString.startsWith("https://goload.one/")) return false;
        if(urlString.startsWith("https://sbplay.org/")) return false;
        if(urlString.contains("gogo-stream")) return false;
        if(urlString.contains(".css") || urlString.contains(".js")) return false;
        if(urlString.contains("google")||urlString.contains("facebook")) return true;
        //if(urlString.contains("https://gogo-stream.com/loadserver.php?id=MTY1Njkx")) return false;
        return false;
    }

    @Override
    public boolean isPlayable(String url) {
        return false;
    }

    public boolean isAdvancedModeUrl(String url) {
        if(url.startsWith(ProvidersData.GOGOANIME.URL + "/category")) return true;
        return false;
    }

    @Override
    public String getAnimeSourceName() {
        return ProvidersData.GOGOANIME.NAME;
    }
}
