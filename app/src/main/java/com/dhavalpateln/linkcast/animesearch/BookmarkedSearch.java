package com.dhavalpateln.linkcast.animesearch;

import com.dhavalpateln.linkcast.database.AnimeLinkData;

import java.util.ArrayList;

public class BookmarkedSearch extends AnimeSearch {

    private ArrayList<AnimeLinkData> data;

    @Override
    public ArrayList<AnimeLinkData> search(String term) {
        ArrayList<AnimeLinkData> result = new ArrayList<>();
        if(this.data != null) {
            for (AnimeLinkData animeLinkData : this.data) {
                if ((animeLinkData.getTitle() + " (" + animeLinkData.getAnimeMetaData(AnimeLinkData.DataContract.DATA_SOURCE) + ")").toLowerCase().contains(term)) {
                    result.add(animeLinkData);
                }
            }
        }
        return result;
    }

    public void updateData(ArrayList<AnimeLinkData> data) {
        this.data = data;
    }

    @Override
    public String getName() {
        return "Bookmarked";
    }

    @Override
    public boolean hasQuickSearch() {
        return true;
    }
}
