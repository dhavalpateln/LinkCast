package com.dhavalpateln.linkcast.animesources;

import com.dhavalpateln.linkcast.database.FirebaseDBHelper;

import java.net.URLEncoder;

public class Animixplay extends AnimeSource {

    public Animixplay() {
        this.animeSource = "animixplay.to";
    }

    @Override
    public String getAnimeTitle(String currentURL, String searchTerm, boolean includeEpisode) {
        if (includeEpisode) {
            return currentURL.split("animixplay.to/v1/")[1];
        }
        return currentURL.split("animixplay.to/v1/")[1].split("/ep")[0];
    }

    @Override
    public String getSearchURL(String searchTerm) {
        return "https://animixplay.to/?q=" + URLEncoder.encode(searchTerm) + "&sengine=all";
    }

    @Override
    public boolean isCorrectSource(String term) {
        return term.contains("animixplay.to");
    }

    @Override
    public void updateBookmarkPage(String url, String id, String title) {
        if (url.contains("/ep")) {
            String episodeNum = url.split("/ep")[1];
            FirebaseDBHelper.getUserAnimeWebExplorerLinkRef()
                    .child(id)
                    .child("title")
                    .setValue(title.split(" - EP")[0] + " - EP" + episodeNum);
        }
    }

    @Override
    public boolean containsAds(String urlString, boolean notFoundMP4, String currentWebViewURI) {
        if(urlString.equals("https://v1.mp4.sh/video.php")) return true;
        if(urlString.contains(".mp4") && notFoundMP4) return false;
        if(urlString.contains("https://streamani.net/")) return false;
        if(urlString.startsWith("https://cachecow.eu/api/search")) return false;
        if(urlString.startsWith("https://api.jikan.moe/v3/search")) return false;
        if(urlString.startsWith("https://mp4.sh/")) return false;
        if(urlString.contains("https://dood.la/")) return false;
        if(urlString.contains("https://hydrax.net/")) return false;
        if(urlString.endsWith(".png") || urlString.endsWith(".jpg")) return false;
        if(urlString.contains(".css") || urlString.contains(".js")) return false;
        if(urlString.contains("google")||urlString.contains("facebook")) return true;
        if(currentWebViewURI.startsWith("https://animixplay.to/?q=")) return false;
        if(!urlString.contains("https://animixplay.to/")) return true;
        return false;
    }

    @Override
    public boolean isPlayable(String url) {
        return url.contains(".mp4") || url.contains(".m3u8");
    }

    @Override
    public boolean isAdvancedModeUrl(String url) {
        return !url.startsWith("https://animixplay.to/?q=") && url.startsWith("https://animixplay.to/v1");
    }
}
