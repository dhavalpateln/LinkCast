package com.dhavalpateln.linkcast.explorer.tasks;

import android.util.Log;

import com.dhavalpateln.linkcast.database.AnimeLinkData;
import com.dhavalpateln.linkcast.extractors.AnimeExtractor;
import com.dhavalpateln.linkcast.utils.EpisodeNode;

import java.util.List;

public class ExtractAnimeEpisodes extends RunnableTask {

    private AnimeExtractor extractor;
    private EpisodeNodeListListener listener;
    private AnimeLinkData animeData;

    public ExtractAnimeEpisodes(AnimeExtractor extractor, AnimeLinkData animeData, EpisodeNodeListListener listener, TaskCompleteListener taskListener) {
        super("ExtractAnimeEpisodes", taskListener);
        this.extractor = extractor;
        this.listener = listener;
        this.animeData = animeData;
    }

    @Override
    public void runTask() {
        try {
            if(this.extractor.requiresInit()) {
                this.extractor.init();
            }
            List<EpisodeNode> episodes = this.extractor.extractData(this.animeData);
            this.animeData.updateData(
                    AnimeLinkData.DataContract.DATA_LAST_FETCHED_EPISODES,
                    String.valueOf(episodes.size()),
                    true,
                    true
            );
            this.getUIHandler().post(() -> this.listener.onEpisodeNodesFetched(episodes));
        } catch (Exception e) {
            Log.d(getTaskName(), "Exception while running extractor: " + this.extractor.getDisplayName());
        }
    }
}
