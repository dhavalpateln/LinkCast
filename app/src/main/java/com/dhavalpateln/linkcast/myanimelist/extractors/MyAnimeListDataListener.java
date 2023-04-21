package com.dhavalpateln.linkcast.myanimelist.extractors;

import com.dhavalpateln.linkcast.myanimelist.MyAnimelistAnimeData;

public interface MyAnimeListDataListener {
    void onMALDataReceived(MyAnimelistAnimeData myAnimelistAnimeData);
}
