package com.dhavalpateln.linkcast.animesources;

public class KwikCX extends AnimeSource {

    @Override
    public boolean isCorrectSource(String term) {
        if(term.startsWith("https://na") && (term.contains(".mp4") || term.contains(".m3u8"))) return true;
        return term.startsWith("https://kwik.cx");
    }

    @Override
    public boolean containsAds(String urlString, boolean notFoundMP4, String currentWebViewURI) {
        if(urlString.contains(".mp4") && notFoundMP4) return false;
        if(urlString.contains("uwu.m3u8") && notFoundMP4) return false;
        if(!urlString.contains("https://kwik.cx/")) return true;
        return false;
    }

    @Override
    public boolean isPlayable(String url) {
        return url.endsWith(".mp4") || url.contains(".m3u8");
    }
}
