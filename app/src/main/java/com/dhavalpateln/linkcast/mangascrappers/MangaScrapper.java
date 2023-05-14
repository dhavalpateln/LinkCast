package com.dhavalpateln.linkcast.mangascrappers;

import com.dhavalpateln.linkcast.database.EpisodeNode;

import java.util.List;

public abstract class MangaScrapper {

    public abstract List<EpisodeNode> getChapters(String url);
    public abstract List<String> getPages(String url);
    public abstract boolean isCorrectURL(String url);
    public abstract String getDisplayName();
}
