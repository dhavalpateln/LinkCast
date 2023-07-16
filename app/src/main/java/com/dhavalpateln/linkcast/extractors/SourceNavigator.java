package com.dhavalpateln.linkcast.extractors;

public abstract class SourceNavigator {
    public abstract boolean isCorrectSource(String term);
    public abstract boolean containsAds(String urlString, boolean notFoundMP4, String currentWebViewURI);
    public abstract boolean isPlayable(String url);
    public boolean shouldOverrideURL(String url) {return true;}
}
