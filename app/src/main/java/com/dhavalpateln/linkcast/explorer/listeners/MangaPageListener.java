package com.dhavalpateln.linkcast.explorer.listeners;

import com.dhavalpateln.linkcast.database.EpisodeNode;

import java.util.List;

public interface MangaPageListener {
    void onMangaPagesExtracted(EpisodeNode node, List<String> pages);
}
