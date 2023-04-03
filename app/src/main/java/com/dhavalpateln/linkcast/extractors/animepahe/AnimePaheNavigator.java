package com.dhavalpateln.linkcast.extractors.animepahe;

import android.net.Uri;

import com.dhavalpateln.linkcast.extractors.SourceNavigator;

public class AnimePaheNavigator extends SourceNavigator {
    @Override
    public boolean isCorrectSource(String term) {


        if(term.contains("https://fainbory.com")) return true;
        if(term.contains("http://hurirk.net")) return true;
        if(term.startsWith("http") && !term.startsWith("https://animepahe.com")) return false;
        if(term.contains("animepahe.com")) {
            return true;
        }
        if(term.startsWith("https://pahe.win")) {
            return true;
        }
        return false;
    }

    @Override
    public boolean containsAds(String urlString, boolean notFoundMP4, String currentWebViewURI) {

        if(Uri.parse(currentWebViewURI).getHost().equalsIgnoreCase(Uri.parse(urlString).getHost())) {
            return false;
        }

        if(urlString.startsWith("https://matomo.kwik.cx")) return true;
        if(urlString.contains("kwik.cx")) return false;
        //if(urlString.endsWith(".ts")) return false;
        if(urlString.contains(".m3u8")) return false;
        //if(urlString.startsWith("https://na")) return false;
        if(urlString.contains(".mp4") && notFoundMP4) return false;
        //if(urlString.startsWith("https://newassets.hcaptcha.com/captcha")) return false;
        //if(urlString.startsWith("https://kwik.cx")) return false;
        if(urlString.endsWith(".png") || urlString.endsWith(".jpg")) return false;
        if(urlString.endsWith(".svg")) return false;
        if(urlString.contains(".css") || urlString.contains(".js")) return false;
        if(urlString.contains("google")||urlString.contains("facebook")) return true;
        if(urlString.startsWith("https://anal.pahe.win")) return false;
        if(urlString.startsWith("https://fainbory.com/")) return false;
        if(urlString.startsWith("http://hurirk.net/")) return false;
        if(!urlString.contains("https://animepahe.com/")) return true;
        return false;
    }

    @Override
    public boolean isPlayable(String url) {
        //return false;
        return url.endsWith(".mp4") || url.contains(".m3u8");
    }

    @Override
    public boolean shouldOverrideURL(String url) {
        if(Uri.parse(url).getHost().contains("kwik")) {
            return true;
        }
        return false;
    }
}
