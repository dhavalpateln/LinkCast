package com.dhavalpateln.linkcast.ui.catalog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.dhavalpateln.linkcast.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class CatalogFragment extends Fragment {

    CatalogCollectionAdapter catalogCollectionAdapter;
    ViewPager2 viewPager;

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