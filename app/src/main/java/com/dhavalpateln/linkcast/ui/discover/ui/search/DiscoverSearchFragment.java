package com.dhavalpateln.linkcast.ui.discover.ui.search;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.dhavalpateln.linkcast.R;
import com.dhavalpateln.linkcast.adapters.SettingsListAdapter;
import com.dhavalpateln.linkcast.interfaces.SettingsObject;
import com.dhavalpateln.linkcast.myanimelist.AdvSearchParams;
import com.dhavalpateln.linkcast.myanimelist.MyAnimeListSearchActivity;
import com.dhavalpateln.linkcast.myanimelist.MyAnimelistSearch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class DiscoverSearchFragment extends Fragment {

    List<SettingsObject> parameters;
    private RecyclerView recyclerView;
    private SettingsListAdapter adapter;

    public DiscoverSearchFragment() {
        // Required empty public constructor
    }
    public static DiscoverSearchFragment newInstance() {
        DiscoverSearchFragment fragment = new DiscoverSearchFragment();
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
        return inflater.inflate(R.layout.fragment_discover_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        parameters = new ArrayList<>();

        recyclerView = view.findViewById(R.id.discover_search_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new SettingsListAdapter(parameters, getContext());
        recyclerView.setAdapter(adapter);

        SettingsObject nameParameter = new SettingsObject("Name");
        SettingsObject typeParameter = new SettingsObject("Type", MyAnimelistSearch.animeTypes, false);
        SettingsObject statusParameter = new SettingsObject("Status", MyAnimelistSearch.statusTypes, false);
        SettingsObject scoreParameter = new SettingsObject("Score", new String[] {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"}, false);
        SettingsObject genresParameter = new SettingsObject("Genres", MyAnimelistSearch.genres, true);
        SettingsObject themesParameter = new SettingsObject("Themes", MyAnimelistSearch.themes, true);
        SettingsObject demographicsParameter = new SettingsObject("Demographics", MyAnimelistSearch.demographics, true);

        parameters.add(nameParameter);
        parameters.add(typeParameter);
        parameters.add(statusParameter);
        parameters.add(scoreParameter);
        parameters.add(genresParameter);
        parameters.add(themesParameter);
        parameters.add(demographicsParameter);

        adapter.notifyDataSetChanged();

        Button searchButton = view.findViewById(R.id.discover_search_button);
        searchButton.setOnClickListener(v -> {
            AdvSearchParams searchParams = new AdvSearchParams();
            String genres = genresParameter.getValue() + "," + themesParameter.getValue() + "," + demographicsParameter.getValue();
            for(String genre: genres.split(",")) {
                if(!genres.equals("Any"))   searchParams.addGenre(genre);
            }


            if(!statusParameter.getValue().equals("Any")) {
                searchParams.setStatus(statusParameter.getValue());
            }

            if(!scoreParameter.getValue().equals("Any")) {
                searchParams.setScore(Integer.valueOf(scoreParameter.getValue()));
            }
            if(!typeParameter.getValue().equals("Any")) {
                searchParams.setType(typeParameter.getValue());
            }

            searchParams.setQuery(nameParameter.getValue());

            Intent intent = MyAnimeListSearchActivity.prepareIntent(getContext(), searchParams);
            startActivity(intent);
        });
    }
}