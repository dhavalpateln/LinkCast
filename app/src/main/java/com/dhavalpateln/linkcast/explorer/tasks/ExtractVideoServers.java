package com.dhavalpateln.linkcast.explorer.tasks;

import com.dhavalpateln.linkcast.database.VideoURLData;
import com.dhavalpateln.linkcast.explorer.listeners.TaskCompleteListener;
import com.dhavalpateln.linkcast.explorer.listeners.VideoServerListener;
import com.dhavalpateln.linkcast.extractors.AnimeExtractor;

public class ExtractVideoServers extends RunnableTask {

    private VideoServerListener listener;
    private AnimeExtractor extractor;
    private String episodeURL;
    private TaskCompleteListener taskCompleteListener;

    public ExtractVideoServers(AnimeExtractor extractor, String episodeURL, VideoServerListener listener, TaskCompleteListener taskCompleteListener) {
        super();
        this.listener = listener;
        this.extractor = extractor;
        this.episodeURL = episodeURL;
        this.taskCompleteListener = taskCompleteListener;
    }

    @Override
    public void run() {
        extractor.extractEpisodeUrls(this.episodeURL, new VideoServerListener() {
            @Override
            public void onVideoExtracted(VideoURLData videoURLData) {
                getUIHandler().post(() -> listener.onVideoExtracted(videoURLData));
            }
        });
        getUIHandler().post(() ->taskCompleteListener.onTaskCompleted());

    }
}
