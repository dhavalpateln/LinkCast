package com.dhavalpateln.linkcast.animesources;

import android.net.Uri;

public class GenericSource extends AnimeSource {

    private String baseUrl;
    private String hostname;

    public GenericSource(String baseUrl) {
        this.baseUrl = baseUrl;
        this.hostname = Uri.parse(baseUrl).getHost();
    }

    @Override
    public boolean isCorrectSource(String term) {
        if(term.startsWith("http")) {
            try {
                if(!Uri.parse(term).getHost().equals(this.hostname)) return false;
            } catch (Exception e) {

            }
        }
        return term.contains(this.hostname);
    }


    @Override
    public boolean containsAds(String urlString, boolean notFoundMP4, String currentWebViewURI) {
        if(urlString.contains(".mp4") && notFoundMP4) return false;
        if(urlString.contains(".m3u8") && notFoundMP4) return false;
        if(urlString.startsWith("https://streamani.net/")) return false;
        if(urlString.startsWith("https://dood.la/")) return false;
        if(urlString.startsWith("https://hydrax.net/")) return false;
        if(urlString.startsWith("https://goload.one/")) return false;
        if(urlString.startsWith("https://sbplay.org/")) return false;
        if(urlString.contains("gogo-stream")) return false;
        if(urlString.contains(".css") || urlString.contains(".js")) return false;
        if(urlString.contains("google")||urlString.contains("facebook")) return true;
        //if(urlString.contains("https://gogo-stream.com/loadserver.php?id=MTY1Njkx")) return false;
        if(!urlString.contains(this.hostname)) return true;
        return false;
    }

    @Override
    public boolean isPlayable(String url) {
        return url.contains(".mp4") || url.contains(".m3u8");
    }
}
