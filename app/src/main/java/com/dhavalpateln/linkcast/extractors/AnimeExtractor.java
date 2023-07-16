package com.dhavalpateln.linkcast.extractors;

import com.dhavalpateln.linkcast.database.AnimeLinkData;
import com.dhavalpateln.linkcast.database.VideoURLData;
import com.dhavalpateln.linkcast.explorer.listeners.VideoServerListener;
import com.dhavalpateln.linkcast.database.EpisodeNode;

import java.util.ArrayList;
import java.util.List;

public abstract class AnimeExtractor extends Extractor {
    public AnimeExtractor() {
        this.setSourceType(SOURCE_TYPE.ANIME);
    }
    public abstract void extractEpisodeUrls(String episodeUrl, List<VideoURLData> result);

    public void extractEpisodeUrls(String episodeUrl, VideoServerListener listener) {
        List<VideoURLData> result = new ArrayList<>();
        extractEpisodeUrls(episodeUrl, result);
        for(VideoURLData videURL: result) {
            listener.onVideoExtracted(videURL);
        }
    }

    public void addVideoUrlData(VideoURLData data, List<VideoURLData> result, VideoServerListener listener) {
        if(result != null) {
            result.add(data);
        }
        if(listener != null) {
            listener.onVideoExtracted(data);
        }
    }
}
