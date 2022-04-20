package com.dhavalpateln.linkcast.ui.animes;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.dhavalpateln.linkcast.AnimeAdvancedView;
import com.dhavalpateln.linkcast.AnimeWebExplorer;
import com.dhavalpateln.linkcast.LinkMaterialCardView;
import com.dhavalpateln.linkcast.R;
import com.dhavalpateln.linkcast.database.FirebaseDBHelper;
import com.dhavalpateln.linkcast.database.Link;
import com.dhavalpateln.linkcast.dialogs.BookmarkLinkDialog;
import com.dhavalpateln.linkcast.uihelpers.AbstractCatalogFragment;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.Map;

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