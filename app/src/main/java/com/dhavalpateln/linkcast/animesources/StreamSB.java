package com.dhavalpateln.linkcast.animesources;

import android.net.Uri;

public class StreamSB extends AnimeSource {

    public StreamSB() {

    }


    @Override
    public boolean isCorrectSource(String term) {
        return term.contains("sbplay.org") || term.contains("streamsss.net");
    }


    @Override
    public boolean containsAds(String urlString, boolean notFoundMP4, String currentWebViewURI) {
        if(urlString.contains(".mp4") && notFoundMP4) return false;
        if(urlString.contains(".m3u8") && notFoundMP4) return false;
        if(urlString.startsWith("https://streamani.net/")) return false;
        if(urlString.startsWith("https://streamsss.net/")) return false;
        if(urlString.startsWith("https://dood.la/")) return false;
        if(urlString.startsWith("https://hydrax.net/")) return false;
        if(urlString.startsWith("https://goload.one/")) return false;
        if(urlString.contains("https://sbplay.org/downloadembed/")) return true;
        if(urlString.contains("gogo-stream")) return false;
        if(urlString.contains(".css") || urlString.contains(".js")) return false;
        if(urlString.contains("google")) return false;
        if(urlString.contains("facebook")) return true;
        if(urlString.contains("opcharizardon.com")) return true;
        if(urlString.startsWith("https://sbplay.org/")) return false;
        return true;
    }

    @Override
    public boolean shouldOverrideURL(String urlString) {
        //if(urlString.contains("https://sbplay.org/downloadembed/")) return false;
        //if(urlString.contains("https://streamsss.net/")) return true;
        return false;
    }

    @Override
    public boolean isPlayable(String url) {
        return url.contains(".mp4") || url.contains(".m3u8");
    }
}
