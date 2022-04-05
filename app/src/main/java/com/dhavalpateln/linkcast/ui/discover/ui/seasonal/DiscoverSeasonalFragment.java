package com.dhavalpateln.linkcast.ui.discover.ui.seasonal;

import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.dhavalpateln.linkcast.R;
import com.dhavalpateln.linkcast.utils.SimpleHttpClient;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.HttpURLConnection;

public class DiscoverSeasonalFragment extends Fragment {

    private SeasonalViewModel seasonalViewModel;
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private String[] tabTitles = { "Current", "Last", "Next" };

    public DiscoverSeasonalFragment() {
        // Required empty public constructor
    }

    public static DiscoverSeasonalFragment newInstance() {
        DiscoverSeasonalFragment fragment = new DiscoverSeasonalFragment();
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
        return inflater.inflate(R.layout.fragment_discover_seasonal, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        seasonalViewModel = new ViewModelProvider(getActivity()).get(SeasonalViewModel.class);


        viewPager = view.findViewById(R.id.discover_seasonal_pager);

        tabLayout = view.findViewById(R.id.discover_seasonal_tab_layout);


        SeasonalFragmentStateAdapter discoverFragmentStateAdapter = new SeasonalFragmentStateAdapter(getParentFragment());
        viewPager.setAdapter(discoverFragmentStateAdapter);
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(tabTitles[position])
        ).attach();


        //tabLayout.getTabAt(1).select();
        //viewPager.setCurrentItem(1);
        //tabLayout.setScrollPosition(1, 0f, true);

    }

    private class SeasonalFragmentStateAdapter extends FragmentStateAdapter {

        public SeasonalFragmentStateAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 1: return DiscoverSeasonalFragmentObject.newInstance(SeasonalViewModel.SeasonType.LAST);
                case 0: return DiscoverSeasonalFragmentObject.newInstance(SeasonalViewModel.SeasonType.CURRENT);
                case 2: return DiscoverSeasonalFragmentObject.newInstance(SeasonalViewModel.SeasonType.NEXT);
                default:    return DiscoverSeasonalFragmentObject.newInstance(SeasonalViewModel.SeasonType.CUSTOM);
            }
        }



        @Override
        public int getItemCount() {
            return tabTitles.length;
        }
    }

}