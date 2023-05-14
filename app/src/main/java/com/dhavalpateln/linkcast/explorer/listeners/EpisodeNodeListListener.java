package com.dhavalpateln.linkcast.explorer.listeners;

import com.dhavalpateln.linkcast.database.EpisodeNode;

import java.util.List;

public interface EpisodeNodeListListener {
    void onEpisodeNodesFetched(List<EpisodeNode> episodeList);
}
