package com.dhavalpateln.linkcast.animesources;

import java.net.URLEncoder;

public class NineAnime extends AnimeSource {

    public NineAnime() {

    }



    @Override
    public boolean isCorrectSource(String term) {
        if(term.startsWith("http") && !term.startsWith("https://9anime.to/")) return false;
        return term.contains("9anime.to");
    }

    @Override
    public boolean containsAds(String urlString, boolean notFoundMP4, String currentWebViewURI) {
        //if(urlString.contains(".mp4") && notFoundMP4) return false;
        //if(urlString.startsWith("https://s2.bunnycdn.ru/")) return true;
        /*if(urlString.contains(".css") || urlString.contains(".js")) return false;
        //if(urlString.contains(".css")) return false;
                /*if(urlString.contains("google")||urlString.contains("facebook")) {
                    return true;
                }*/

        /*if(urlString.contains(".jpg") || urlString.contains("https://www.google.com/recaptcha")) {
            return false;
        }
        if(!urlString.contains("9anime.to")) {
            return true;
        }*/
        return false;
    }

    @Override
    public boolean isPlayable(String url) {
        return url.contains(".mp4") || url.contains(".m3u8");
    }
}
