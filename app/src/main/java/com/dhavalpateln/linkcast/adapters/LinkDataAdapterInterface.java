package com.dhavalpateln.linkcast.adapters;

import android.widget.ImageView;

import com.dhavalpateln.linkcast.database.room.animelinkcache.LinkWithAllData;

public interface LinkDataAdapterInterface {
    void onLinkDataClicked(LinkWithAllData linkData, ImageView animeImage);
    void onLinkDataLongClick(LinkWithAllData linkData);
}
