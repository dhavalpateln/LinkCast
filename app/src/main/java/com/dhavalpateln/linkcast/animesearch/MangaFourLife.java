package com.dhavalpateln.linkcast.animesearch;

import android.content.Intent;

import com.dhavalpateln.linkcast.database.AnimeLinkData;

import java.util.ArrayList;

public class MangaFourLife extends AnimeSearch {
    @Override
    public ArrayList<AnimeLinkData> search(String term) {
        return null;
    }

    @Override
    public String getName() {
        return "manga4life";
    }

    @Override
    public boolean hasQuickSearch() {
        return false;
    }

    @Override
    public boolean isMangeSource() {
        return true;
    }
}
