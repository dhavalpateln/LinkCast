package com.dhavalpateln.linkcast.ui.discover.ui.seasonal;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dhavalpateln.linkcast.R;

public class DiscoverSeasonalFragment extends Fragment {

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
}