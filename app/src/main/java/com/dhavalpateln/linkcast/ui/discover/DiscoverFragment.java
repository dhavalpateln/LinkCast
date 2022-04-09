package com.dhavalpateln.linkcast.ui.discover;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.dhavalpateln.linkcast.R;
import com.dhavalpateln.linkcast.ui.discover.ui.genre.DiscoverGenreFragment;
import com.dhavalpateln.linkcast.ui.discover.ui.popular.DiscoverPopularFragment;
import com.dhavalpateln.linkcast.ui.discover.ui.seasonal.DiscoverSeasonalFragment;
import com.dhavalpateln.linkcast.ui.discover.ui.suggested.SuggestedFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class DiscoverFragment extends Fragment {

    private NavigationBarView bottomNavigationView;
    private DiscoverSeasonalFragment discoverSeasonalFragment = DiscoverSeasonalFragment.newInstance();
    private DiscoverPopularFragment discoverPopularFragment = DiscoverPopularFragment.newInstance();
    private DiscoverGenreFragment discoverGenreFragment = DiscoverGenreFragment.newInstance();
    private SuggestedFragment discoverSuggestedFragment = SuggestedFragment.newInstance();

    public DiscoverFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_discover, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bottomNavigationView = view.findViewById(R.id.discover_bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.discover_seasonal:
                        getParentFragmentManager().beginTransaction().replace(R.id.discover_fill_fragment, discoverSeasonalFragment).commit();
                        return true;
                    case R.id.discover_popular:
                        getParentFragmentManager().beginTransaction().replace(R.id.discover_fill_fragment, discoverPopularFragment).commit();
                        return true;
                    case R.id.discover_genre:
                        getParentFragmentManager().beginTransaction().replace(R.id.discover_fill_fragment, discoverGenreFragment).commit();
                        return true;
                    case R.id.discover_suggested:
                        getParentFragmentManager().beginTransaction().replace(R.id.discover_fill_fragment, discoverSuggestedFragment).commit();
                        return true;
                }
                return false;
            }
        });
        bottomNavigationView.setSelectedItemId(R.id.discover_seasonal);
    }
}