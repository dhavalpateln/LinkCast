package com.dhavalpateln.linkcast.animesources;

import com.dhavalpateln.linkcast.database.FirebaseDBHelper;

import java.net.URLEncoder;

public class AnimeKisaCC extends AnimeSource {

    public AnimeKisaCC() {
        this.animeSource = "animekisa.cc";
    }

    @Override
    public String getAnimeTitle(String currentURL, String searchTerm, boolean includeEpisode) {
        String[] elements = currentURL.split("/");
        if (includeEpisode) {

            return elements[elements.length - 1];
        } else {
            return elements[elements.length - 1].split("-episode")[0];
        }
    }

    @Override
    public String getSearchURL(String searchTerm) {
        return "https://www.animekisa.cc/search?name=" + URLEncoder.encode(searchTerm);
    }

    @Override
    public boolean isCorrectSource(String term) {
        return term.contains("animekisa.cc");
    }

    @Override
    public void updateBookmarkPage(String url, String id, String title) {
        if (url.contains("episode")) {
            String episodeNum = url.split("episode-")[1];
            FirebaseDBHelper.getUserAnimeWebExplorerLinkRef()
                    .child(id)
                    .child("title")
                    .setValue(title.split(" - EP")[0] + " - EP" + episodeNum);
        }
    }

    @Override
    public boolean containsAds(String urlString, boolean notFoundMP4, String currentWebViewURI) {
        if(urlString.contains("blank.mp4")) return true;
        if(urlString.contains(".mp4") && notFoundMP4) return false;
        if(urlString.contains("https://streamani.net/")) return false;
        if(urlString.contains("https://dood.la/")) return false;
        if(urlString.contains("https://hydrax.net/")) return false;
        if(urlString.contains("https://goload.one/")) return false;
        if(urlString.contains("https://sbplay.org/")) return false;
        if(urlString.contains(".css") || urlString.contains(".js")) return false;
        if(urlString.contains("play")) return false;
        if(urlString.contains("google")||urlString.contains("facebook")) return true;
        if(!urlString.contains("animekisa.cc")) return true;
        return false;
    }

    @Override
    public boolean isPlayable(String url) {
        return url.contains(".mp4") || url.contains(".m3u8");
    }

    @Override
    public boolean isAdvancedModeUrl(String url) {
        return !url.startsWith("https://www.animekisa.cc/search?name=") && url.startsWith("https://www.animekisa.cc/");
    }
}
