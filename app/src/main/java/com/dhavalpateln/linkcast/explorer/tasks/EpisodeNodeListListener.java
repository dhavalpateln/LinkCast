package com.dhavalpateln.linkcast.explorer.tasks;

import com.dhavalpateln.linkcast.utils.EpisodeNode;

import java.util.List;

public interface EpisodeNodeListListener {
    void onEpisodeNodesFetched(List<EpisodeNode> episodeList);
}
