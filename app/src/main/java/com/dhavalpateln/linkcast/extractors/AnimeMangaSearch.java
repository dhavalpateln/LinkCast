package com.dhavalpateln.linkcast.extractors;

import com.dhavalpateln.linkcast.database.AnimeLinkData;
import java.util.ArrayList;

public abstract class AnimeMangaSearch extends Source {
    public abstract ArrayList<AnimeLinkData> search(String term);
    public abstract boolean hasQuickSearch();
    public boolean isAdvanceModeSource() {
        return true;
    }
}
