package com.dhavalpateln.linkcast.extractors.embedsito;

import com.dhavalpateln.linkcast.extractors.SourceNavigator;

public class EmbedSitoNavigator extends SourceNavigator {

    @Override
    public boolean isCorrectSource(String term) {
        return term.startsWith("https://embedsito.com/");
    }

    @Override
    public boolean containsAds(String urlString, boolean notFoundMP4, String currentWebViewURI) {
        return false;
    }

    @Override
    public boolean isPlayable(String url) {
        return url.contains(".mp4") || url.contains(".m3u8");
    }
}
