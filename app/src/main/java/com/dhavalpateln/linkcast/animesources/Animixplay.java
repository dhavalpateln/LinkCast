package com.dhavalpateln.linkcast.animesources;

import com.dhavalpateln.linkcast.database.FirebaseDBHelper;

import java.net.URLEncoder;

public class Animixplay extends AnimeSource {

    public Animixplay() {
    }

    @Override
    public boolean isCorrectSource(String term) {
        return term.contains("animixplay.to");
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

}
