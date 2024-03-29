package com.dhavalpateln.linkcast.database;

import com.dhavalpateln.linkcast.ui.settings.SettingsFragment;
import com.dhavalpateln.linkcast.ui.AbstractCatalogObjectFragment;

public class SharedPrefContract {
    public static final String EPISODE_TRACKING = "episode_tracker";
    public static final int EPISODE_TRACKING_DEFAULT = SettingsFragment.EpisodeTracking.LAST_EPISODE;

    public static final String ANIME_LIST_SORT_ORDER = "anime_sort_order";
    public static final String ANIME_LIST_SORT_DEFAULT = AbstractCatalogObjectFragment.Sort.BY_SCORE.name();
    public static final String BOOKMARK_DELETE_CONFIRMATION = "bookmark_delete";
    public static final String RESUME_BEHAVIOUR = "resume_options";
    public static final int RESUME_BEHAVIOUR_DEFAULT = 2;
}
