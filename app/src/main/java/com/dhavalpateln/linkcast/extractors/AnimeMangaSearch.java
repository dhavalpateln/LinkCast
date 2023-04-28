package com.dhavalpateln.linkcast.extractors;

import com.dhavalpateln.linkcast.database.AnimeLinkData;
import com.dhavalpateln.linkcast.database.room.animelinkcache.LinkData;
import com.dhavalpateln.linkcast.database.room.animelinkcache.LinkWithAllData;

import java.util.ArrayList;
import java.util.List;

public abstract class AnimeMangaSearch extends Source {
    public abstract ArrayList<AnimeLinkData> search(String term);
    public abstract boolean hasQuickSearch();
    public boolean isAdvanceModeSource() {
        return true;
    }

    public List<LinkWithAllData> searchLink(String term) {
        List<AnimeLinkData> searchResult = search(term);
        List<LinkWithAllData> result = new ArrayList<>();
        for(AnimeLinkData animeLinkData: searchResult) {
            LinkWithAllData link = new LinkWithAllData();
            link.linkData = LinkData.from(animeLinkData);
            result.add(link);
        }
        return result;
    }
}
