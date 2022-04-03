package com.dhavalpateln.linkcast.ui.catalog;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.dhavalpateln.linkcast.AnimeSearchActivity;
import com.dhavalpateln.linkcast.AnimeWebExplorer;
import com.dhavalpateln.linkcast.MangaWebExplorer;
import com.dhavalpateln.linkcast.R;
import com.dhavalpateln.linkcast.dialogs.SearchDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.HashSet;
import java.util.Set;

public class CatalogFragment extends Fragment {

    CatalogCollectionAdapter catalogCollectionAdapter;
    private ViewPager2 viewPager;
    private SearchDialog searchDialog;
    private Set<String> mangaSourceList;

    public static class Catalogs {
        public static final String WATCHING = "Watching";
        public static final String PLANNED = "Planned";
        public static final String COMPLETED = "Completed";
        public static final String FAVORITE = "Fav";
        public static final String ALL = "All";
        public static final String[] BASIC_TYPES = {WATCHING, PLANNED, COMPLETED};
        public static final String[] ALL_TYPES = {WATCHING, PLANNED, FAVORITE, COMPLETED, ALL};
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_catalog, container, false);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        mangaSourceList = new HashSet<>();//;
        mangaSourceList.add("mangadex");
        mangaSourceList.add("manga4life");

        searchDialog = new SearchDialog();
        searchDialog.setSearchListener(new SearchDialog.SearchButtonClickListener() {
            @Override
            public void onSearchButtonClicked(String searchString, String source, boolean advancedMode) {
                Intent searchIntent;
                if(mangaSourceList.contains(source)) {
                    searchIntent = new Intent(getContext(), MangaWebExplorer.class);
                }
                else {
                    searchIntent = new Intent(getContext(), AnimeWebExplorer.class);
                }
                searchIntent.putExtra("search", searchString);
                searchIntent.putExtra("source", source);
                searchIntent.putExtra("advancedMode", advancedMode);
                startActivity(searchIntent);
            }
        });

        FloatingActionButton fab = view.findViewById(R.id.fab);

        fab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                //Toast.makeText(getApplicationContext(), "Long Click", Toast.LENGTH_LONG).show();
                searchDialog.show(getParentFragmentManager(), "Search");
                return false;
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent searchIntent = new Intent(getContext(), AnimeSearchActivity.class);
                searchIntent.putExtra("search", "");
                searchIntent.putExtra("source", "SAVED");
                searchIntent.putExtra("advancedMode", true);
                startActivity(searchIntent);

            }
        });

        catalogCollectionAdapter = new CatalogCollectionAdapter(this);
        viewPager = view.findViewById(R.id.pager);
        viewPager.setAdapter(catalogCollectionAdapter);

        TabLayout tabLayout = view.findViewById(R.id.tab_layout);
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(Catalogs.ALL_TYPES[position])
        ).attach();


    }

    private class CatalogCollectionAdapter extends FragmentStateAdapter {

        public CatalogCollectionAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            CatalogObjectFragment fragment = CatalogObjectFragment.newInstance(Catalogs.ALL_TYPES[position]);
            return fragment;
        }

        @Override
        public int getItemCount() {
            return Catalogs.ALL_TYPES.length;
        }
    }

}