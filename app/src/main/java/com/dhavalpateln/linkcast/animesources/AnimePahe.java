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
        if(term.startsWith("http") && !term.startsWith("https://animepahe.com")) return false;
        if(term.contains("animepahe.com")) return true;
        /*if(term.contains("fainbory.com")) return true;
        if(term.contains("hurirk.net")) return true;
        if(term.contains("kwik.cx")) return true;
        if(term.startsWith("https://na") && term.contains(".mp4")) return true;*/
        return false;
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
        if(urlString.startsWith("https://matomo.kwik.cx")) return true;
        if(urlString.contains("kwik.cx")) return false;
        //if(urlString.endsWith(".ts")) return false;
        if(urlString.endsWith(".m3u8")) return false;
        //if(urlString.startsWith("https://na")) return false;
        if(urlString.contains(".mp4") && notFoundMP4) return false;
        //if(urlString.startsWith("https://newassets.hcaptcha.com/captcha")) return false;
        //if(urlString.startsWith("https://kwik.cx")) return false;
        if(urlString.endsWith(".png") || urlString.endsWith(".jpg")) return false;
        if(urlString.endsWith(".svg")) return false;
        if(urlString.contains(".css") || urlString.contains(".js")) return false;
        if(urlString.contains("google")||urlString.contains("facebook")) return true;
        //if(urlString.startsWith("https://anal.pahe.win")) return false;
        //if(urlString.startsWith("https://fainbory.com/")) return false;
        //if(urlString.startsWith("http://hurirk.net/")) return false;
        if(!urlString.contains("https://animepahe.com/")) return true;
        return false;
    }

    @Override
    public boolean isPlayable(String url) {
        //return false;
        return url.endsWith(".mp4") || url.contains(".m3u8");
    }

}
