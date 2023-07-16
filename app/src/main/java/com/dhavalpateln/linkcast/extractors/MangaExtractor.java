package com.dhavalpateln.linkcast.extractors;

import com.dhavalpateln.linkcast.database.AnimeLinkData;
import com.dhavalpateln.linkcast.database.EpisodeNode;

import java.util.List;

public abstract class MangaExtractor extends Extractor {
    public MangaExtractor() {
        this.setSourceType(Source.SOURCE_TYPE.MANGA);
    }
    public abstract List<EpisodeNode> getChapters(String url);

    @Override
    public List<EpisodeNode> getEpisodeList(String episodeListUrl) {
        return getChapters(episodeListUrl);
    }

    public abstract List<String> getPages(String url);
}
