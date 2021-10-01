package com.dhavalpateln.linkcast.animesources;

import com.dhavalpateln.linkcast.database.FirebaseDBHelper;

public class AnimePahe extends AnimeSource {

    public AnimePahe() {
        this.animeSource = "animepahe.com";
    }

    @Override
    public String getAnimeTitle(String currentURL, String searchTerm, boolean includeEpisode) {
        return searchTerm;
    }

    @Override
    public String getSearchURL(String searchTerm) {
        return "https://animepahe.com/";
    }

    @Override
    public boolean isCorrectSource(String term) {
        return term.contains("animepahe.com");
    }

    @Override
    public void updateBookmarkPage(String url, String id, String title) {
        if (url.contains("animepahe.com")) {
            //String episodeNum = currentWebViewURI.split("/ep")[1];
            FirebaseDBHelper.getUserAnimeWebExplorerLinkRef()
                    .child(id)
                    .child("title")
                    .setValue(title);
        }
    }

    @Override
    public boolean containsAds(String urlString, boolean notFoundMP4, String currentWebViewURI) {
        /*if(urlString.startsWith("https://matomo.kwik.cx")) return true;
        if(urlString.contains("kwik.cx")) return false;
        if(urlString.endsWith(".ts")) return false;
        if(urlString.endsWith(".m3u8")) return false;
        if(urlString.startsWith("https://na")) return false;
        if(urlString.contains(".mp4") && notFoundMP4) return false;
        if(urlString.startsWith("https://newassets.hcaptcha.com/captcha")) return false;
        if(urlString.startsWith("https://kwik.cx")) return false;
        if(urlString.endsWith(".png") || urlString.endsWith(".jpg")) return false;
        if(urlString.endsWith(".svg")) return false;
        if(urlString.contains(".css") || urlString.contains(".js")) return false;
        if(urlString.contains("google")||urlString.contains("facebook")) return true;
        if(!urlString.contains("https://animepahe.com/")) return true;*/
        return false;
    }

    @Override
    public boolean isPlayable(String url) {
        return url.contains(".mp4") || url.contains(".m3u8");
    }

}
