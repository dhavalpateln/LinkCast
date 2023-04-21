package com.dhavalpateln.linkcast.explorer.tasks;

import com.dhavalpateln.linkcast.extractors.AnimeExtractor;
import com.dhavalpateln.linkcast.utils.EpisodeNode;

import java.util.List;

public class ExtractAnimeData implements Runnable {

    private AnimeExtractor extractor;

    public interface AnimeEpisodeListListener {
        void onEpisodesFetched(List<EpisodeNode> episodeList);
    }

    public ExtractAnimeData(AnimeExtractor extractor, AnimeEpisodeListListener listener) {
        this.extractor = extractor;
    }

    @Override
    public void run() {

    }
}
