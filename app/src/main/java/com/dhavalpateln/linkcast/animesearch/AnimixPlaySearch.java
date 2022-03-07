package com.dhavalpateln.linkcast.animesearch;

import com.dhavalpateln.linkcast.ProvidersData;
import com.dhavalpateln.linkcast.database.AnimeLinkData;

import java.util.ArrayList;

public class AnimixPlaySearch extends AnimeSearch {
    @Override
    public ArrayList<AnimeLinkData> search(String term) {
        ArrayList<AnimeLinkData> result = new ArrayList<>();
        return result;
    }

    @Override
    public String getName() {
        return ProvidersData.AnimixPlay.NAME;
    }

    @Override
    public boolean hasQuickSearch() {
        return false;
    }
}
