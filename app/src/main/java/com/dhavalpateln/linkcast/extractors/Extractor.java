package com.dhavalpateln.linkcast.extractors;

import com.dhavalpateln.linkcast.database.AnimeLinkData;
import com.dhavalpateln.linkcast.database.EpisodeNode;

import java.util.List;

public abstract class Extractor extends Source {
    public abstract boolean isCorrectURL(String url);
    public abstract List<EpisodeNode> getEpisodeList(String episodeListUrl);
    public abstract List<EpisodeNode> extractData(AnimeLinkData data);
}
