package com.dhavalpateln.linkcast.explorer.tasks;

import com.dhavalpateln.linkcast.database.VideoURLData;
import com.dhavalpateln.linkcast.database.room.animelinkcache.LinkWithAllData;
import com.dhavalpateln.linkcast.explorer.listeners.VideoServerListener;
import com.dhavalpateln.linkcast.extractors.AnimeExtractor;

import java.util.ArrayList;
import java.util.List;

public class ExtractVideoServers extends RunnableTask {

    private VideoServerListener listener;
    private AnimeExtractor extractor;
    private String episodeURL;

    public ExtractVideoServers(AnimeExtractor extractor, String episodeURL, VideoServerListener listener, TaskCompleteListener taskListener) {
        super("VideoExtractor", taskListener);
        this.listener = listener;
        this.extractor = extractor;
        this.episodeURL = episodeURL;
    }

    @Override
    public void runTask() {
        List<VideoURLData> videoServerList = new ArrayList<>();
        extractor.extractEpisodeUrls(this.episodeURL, videoServerList);
        getUIHandler().post(() -> this.listener.onVideoServerExtracted(videoServerList));
    }
}
