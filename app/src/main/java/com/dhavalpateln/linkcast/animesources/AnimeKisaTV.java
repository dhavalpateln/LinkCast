package com.dhavalpateln.linkcast.animesources;

import com.dhavalpateln.linkcast.database.FirebaseDBHelper;

import java.net.URLEncoder;

public class AnimeKisaTV extends AnimeSource{

    public AnimeKisaTV() {
        this.animeSource = "animekisa.tv";
    }

    @Override
    public String getAnimeTitle(String currentURL, String searchTerm, boolean includeEpisode) {
        if (includeEpisode) {
            return currentURL.split("animekisa.tv/")[1];
        } else {
            return currentURL.split("animekisa.tv/")[1].split("-episode")[0];
        }
    }

    @Override
    public String getSearchURL(String searchTerm) {
        return "https://animekisa.tv/search?q=" + URLEncoder.encode(searchTerm);
    }

    @Override
    public boolean isCorrectSource(String term) {
        return term.contains("animekisa.tv");
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
        else {
            FirebaseDBHelper.getUserAnimeWebExplorerLinkRef()
                    .child(id)
                    .child("title")
                    .setValue(title);
        }
    }

    @Override
    public boolean containsAds(String urlString, boolean notFoundMP4, String currentWebViewURI) {
        if(urlString.contains(".mp4") && notFoundMP4) return false;
        if(urlString.contains(".m3u8") && notFoundMP4) return false;
        if(urlString.contains("https://streamani.net/")) return false;
        if(urlString.contains("https://dood.la/")) return false;
        if(urlString.contains("https://hydrax.net/")) return false;
        if(urlString.contains("https://goload.one/")) return false;
        if(urlString.contains("https://sbplay.org/")) return false;
        if(urlString.contains("gogo-stream")) return false;
        if(urlString.contains(".css") || urlString.contains(".js")) return false;
        if(urlString.contains("google")||urlString.contains("facebook")) return true;
        if(urlString.contains("https://gogo-stream.com/loadserver.php?id=MTY1Njkx")) return false;
        if(!urlString.contains("animekisa.tv")) return true;
        return false;
    }

    @Override
    public boolean isAdvancedModeUrl(String url) {
        return !url.startsWith("https://animekisa.tv/search?q=") && url.startsWith("https://animekisa.tv/");
    }

    @Override
    public boolean isPlayable(String url) {
        return url.contains(".mp4") || url.contains(".m3u8");
    }
}
