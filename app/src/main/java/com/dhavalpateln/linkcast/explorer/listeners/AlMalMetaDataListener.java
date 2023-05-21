package com.dhavalpateln.linkcast.explorer.listeners;

import com.dhavalpateln.linkcast.database.AnimeMALMetaData;
import com.dhavalpateln.linkcast.database.room.almaldata.AlMalMetaData;

public interface AlMalMetaDataListener {
    void onMALMetaData(AlMalMetaData metaData);
    void onBannerImageFetched(String url);
}
