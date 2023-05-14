package com.dhavalpateln.linkcast.explorer.adapters;

import com.dhavalpateln.linkcast.database.EpisodeNode;

public interface EpisodeNodeSelectionListener {
    void onEpisodeSelected(EpisodeNode node, int position);
    void onEpisodeLongPressed(EpisodeNode node, int position);
}
