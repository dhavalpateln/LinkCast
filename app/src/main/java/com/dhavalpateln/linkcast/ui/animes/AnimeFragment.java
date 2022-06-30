package com.dhavalpateln.linkcast.ui.animes;

import androidx.fragment.app.Fragment;

import com.dhavalpateln.linkcast.ui.AbstractCatalogFragment;

public class AnimeFragment extends AbstractCatalogFragment {

    public static class Catalogs {
        public static final String WATCHING = "Watching";
        public static final String PLANNED = "Planned";
        public static final String COMPLETED = "Completed";
        public static final String ONHOLD = "On Hold";
        public static final String DROPPED = "Dropped";
        public static final String FAVORITE = "Fav";
        public static final String ALL = "All";
        public static final String[] BASIC_TYPES = {WATCHING, PLANNED, COMPLETED, ONHOLD, DROPPED};
        public static final String[] ALL_TYPES = {WATCHING, PLANNED, FAVORITE, COMPLETED, ONHOLD, DROPPED, ALL};
    }

    @Override
    public Fragment createNewFragment(String tabName) {
        return AnimeFragmentObject.newInstance(tabName);
    }

    @Override
    public String[] getTabs() {
        return Catalogs.ALL_TYPES;
    }
}