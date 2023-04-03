package com.dhavalpateln.linkcast.extractors.bookmark;

import com.dhavalpateln.linkcast.animesearch.AnimeSearch;
import com.dhavalpateln.linkcast.data.StoredAnimeLinkData;
import com.dhavalpateln.linkcast.database.AnimeLinkData;
import com.dhavalpateln.linkcast.extractors.AnimeMangaSearch;

import java.util.ArrayList;
import java.util.Map;

public class BookmarkedSearch extends AnimeMangaSearch {

    private ArrayList<AnimeLinkData> data;
    private boolean mangaLoaded = false;
    private boolean animeLoaded = false;

    public BookmarkedSearch() {
        this.data = new ArrayList<>();
    }

    @Override
    public ArrayList<AnimeLinkData> search(String term) {
        term = term.toLowerCase();
        this.data.clear();
        if(StoredAnimeLinkData.getInstance().getAnimeCache() != null) {
            loadData(StoredAnimeLinkData.getInstance().getAnimeCache());
        }
        if(StoredAnimeLinkData.getInstance().getMangaCache() != null) {
            loadData(StoredAnimeLinkData.getInstance().getMangaCache());
        }
        ArrayList<AnimeLinkData> result = new ArrayList<>();
        for (AnimeLinkData animeLinkData : this.data) {
            if ((animeLinkData.getTitle() + " (" + animeLinkData.getAnimeMetaData(AnimeLinkData.DataContract.DATA_SOURCE) + ")").toLowerCase().contains(term)) {
                result.add(animeLinkData);
            }
        }
        return result;
    }

    public void loadData(Map<String, AnimeLinkData> sourceCache) {
        for(Map.Entry<String, AnimeLinkData> entry: sourceCache.entrySet()) {
            this.data.add(entry.getValue());
        }
    }

    public void updateData(ArrayList<AnimeLinkData> data) {
        this.data = data;
    }

    @Override
    public boolean hasQuickSearch() {
        return true;
    }

    @Override
    public String getDisplayName() {
        return "Bookmarked";
    }
}
