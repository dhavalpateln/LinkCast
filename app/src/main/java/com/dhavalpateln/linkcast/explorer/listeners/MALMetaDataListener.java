package com.dhavalpateln.linkcast.explorer.listeners;

import com.dhavalpateln.linkcast.database.AnimeMALMetaData;

public interface MALMetaDataListener {
    void onMALMetaData(AnimeMALMetaData metaData);
    void onBannerImageFetched(String url);
}
