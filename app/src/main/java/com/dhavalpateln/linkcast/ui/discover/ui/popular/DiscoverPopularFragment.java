package com.dhavalpateln.linkcast.ui.discover.ui.popular;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dhavalpateln.linkcast.R;
import com.dhavalpateln.linkcast.ui.catalog.CatalogFragment;
import com.dhavalpateln.linkcast.ui.catalog.CatalogObjectFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class DiscoverPopularFragment extends Fragment {

    private PopularViewModel.TYPE[] tabs = {
            PopularViewModel.TYPE.TOP_AIRING,
            PopularViewModel.TYPE.TOP_UPCOMING,
            PopularViewModel.TYPE.TOP_TV_SERIES,
            PopularViewModel.TYPE.TOP_MOVIES,
            PopularViewModel.TYPE.POPULAR,
            PopularViewModel.TYPE.FAVORITE
    };
    private String[] tabTitles = {
            "Airing",
            "Upcoming",
            "TV",
            "Movies",
            "Popular",
            "Favorite"
    };

    public DiscoverPopularFragment() {
        // Required empty public constructor
    }
    public static DiscoverPopularFragment newInstance() {
        DiscoverPopularFragment fragment = new DiscoverPopularFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_discover_popular, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        PopularFragmentStateAdapter popularFragmentStateAdapter = new PopularFragmentStateAdapter(getParentFragment());
        ViewPager2 viewPager = view.findViewById(R.id.discover_popular_pager);
        /*viewPager.setSaveEnabled(false);
        viewPager.setSaveFromParentEnabled(false);*/
        viewPager.setAdapter(popularFragmentStateAdapter);

        TabLayout tabLayout = view.findViewById(R.id.discover_popular_tab_layout);
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(tabTitles[position])
        ).attach();
    }

    private class PopularFragmentStateAdapter extends FragmentStateAdapter {

        public PopularFragmentStateAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            DiscoverPopularFragmentObject fragment = DiscoverPopularFragmentObject.newInstance(tabs[position]);
            return fragment;
        }

        @Override
        public int getItemCount() {
            return tabs.length;
        }
    }

}