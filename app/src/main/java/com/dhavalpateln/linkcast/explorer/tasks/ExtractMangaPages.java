package com.dhavalpateln.linkcast.explorer.tasks;

import com.dhavalpateln.linkcast.database.EpisodeNode;
import com.dhavalpateln.linkcast.database.room.animelinkcache.LinkWithAllData;
import com.dhavalpateln.linkcast.explorer.listeners.MangaPageListener;
import com.dhavalpateln.linkcast.extractors.Extractor;
import com.dhavalpateln.linkcast.extractors.MangaExtractor;

import java.util.List;

public class ExtractMangaPages extends RunnableTask {

    private MangaExtractor extractor;
    private MangaPageListener listener;
    private LinkWithAllData linkData;
    private EpisodeNode node;
    private String TAG = "ExtractMangaPages";

    public ExtractMangaPages(Extractor extractor, LinkWithAllData linkData, EpisodeNode node, MangaPageListener listener) {
        super();
        this.extractor = (MangaExtractor) extractor;
        this.listener = listener;
        this.linkData = linkData;
        this.node = node;
    }

    @Override
    public void run() {
        List<String> pages = this.extractor.getPages(node.getUrl());
        getUIHandler().post(() -> this.listener.onMangaPagesExtracted(node, pages));
    }
}
