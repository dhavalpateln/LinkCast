package com.dhavalpateln.linkcast.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dhavalpateln.linkcast.AnimeSearchActivity;
import com.dhavalpateln.linkcast.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

public abstract class AbstractCatalogFragment extends Fragment {

    CatalogCollectionAdapter catalogCollectionAdapter;
    private ViewPager2 viewPager;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_catalog, container, false);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        FloatingActionButton fab = view.findViewById(R.id.fab);

        fab.setOnClickListener(view1 -> {
            Intent searchIntent = new Intent(getContext(), AnimeSearchActivity.class);
            searchIntent.putExtra("search", "");
            searchIntent.putExtra("source", "SAVED");
            searchIntent.putExtra("advancedMode", true);
            startActivity(searchIntent);

        });

        catalogCollectionAdapter = new CatalogCollectionAdapter(this);
        viewPager = view.findViewById(R.id.pager);
        //viewPager.setOffscreenPageLimit(4);
        viewPager.setAdapter(catalogCollectionAdapter);

        TabLayout tabLayout = view.findViewById(R.id.tab_layout);
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(getTabs()[position])
        ).attach();
    }

    public abstract Fragment createNewFragment(String tabName);

    private class CatalogCollectionAdapter extends FragmentStateAdapter {

        public CatalogCollectionAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
           return createNewFragment(getTabs()[position]);
        }

        @Override
        public int getItemCount() {
            return getTabs().length;
        }
    }

    public abstract String[] getTabs();

}