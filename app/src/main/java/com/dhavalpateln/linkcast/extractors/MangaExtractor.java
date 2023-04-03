package com.dhavalpateln.linkcast.extractors;

import com.dhavalpateln.linkcast.utils.EpisodeNode;

import java.util.List;

public abstract class MangaExtractor extends Source {
    public MangaExtractor() {
        this.setSourceType(Source.SOURCE_TYPE.MANGA);
    }
    public abstract List<EpisodeNode> getChapters(String url);
    public abstract List<String> getPages(String url);
    public abstract boolean isCorrectURL(String url);
}
