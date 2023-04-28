package com.dhavalpateln.linkcast.extractors.bookmark;

import android.content.Context;

import com.dhavalpateln.linkcast.animesearch.AnimeSearch;
import com.dhavalpateln.linkcast.data.StoredAnimeLinkData;
import com.dhavalpateln.linkcast.database.AnimeLinkData;
import com.dhavalpateln.linkcast.database.room.LinkCastRoomRepository;
import com.dhavalpateln.linkcast.database.room.animelinkcache.LinkData;
import com.dhavalpateln.linkcast.database.room.animelinkcache.LinkWithAllData;
import com.dhavalpateln.linkcast.extractors.AnimeMangaSearch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BookmarkedSearch extends AnimeMangaSearch {

    private ArrayList<AnimeLinkData> data;
    private List<LinkWithAllData> linkData;
    private boolean mangaLoaded = false;
    private boolean animeLoaded = false;
    private LinkCastRoomRepository roomRepo;

    public BookmarkedSearch(Context context) {
        this.data = new ArrayList<>();
        this.roomRepo = new LinkCastRoomRepository(context);
    }

    @Override
    public ArrayList<AnimeLinkData> search(String term) {
        term = term.toLowerCase();
        this.data.clear();
        /*if(StoredAnimeLinkData.getInstance().getAnimeCache() != null) {
            loadData(StoredAnimeLinkData.getInstance().getAnimeCache());
        }
        if(StoredAnimeLinkData.getInstance().getMangaCache() != null) {
            loadData(StoredAnimeLinkData.getInstance().getMangaCache());
        }*/
        loadData(null);
        ArrayList<AnimeLinkData> result = new ArrayList<>();
        for (AnimeLinkData animeLinkData : this.data) {
            if ((animeLinkData.getTitle() + " (" + animeLinkData.getAnimeMetaData(AnimeLinkData.DataContract.DATA_SOURCE) + ")").toLowerCase().contains(term)) {
                result.add(animeLinkData);
            }
        }
        return result;
    }

    public void loadData(Map<String, AnimeLinkData> sourceCache) {
        List<LinkData> linkDataList = this.roomRepo.getAllAnimeLinks();
        for(LinkData linkData: linkDataList) {
            this.data.add(AnimeLinkData.from(linkData));
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

    @Override
    public List<LinkWithAllData> searchLink(String term) {
        term = term.toLowerCase();
        List<LinkWithAllData> linkWithData = this.roomRepo.getLinkWithData();
        List<LinkWithAllData> result = new ArrayList<>();
        for (LinkWithAllData linkWithAllData : linkWithData) {
            if(linkWithAllData.linkData.getTitle().toLowerCase().contains(term)) {
                result.add(linkWithAllData);
                continue;
            }
            if(linkWithAllData.malMetaData != null) {
                if(linkWithAllData.malMetaData.getEngName().toLowerCase().contains(term) || linkWithAllData.malMetaData.getName().toLowerCase().contains(term)) {
                    result.add(linkWithAllData);
                }
            }
        }
        return result;
    }
}
