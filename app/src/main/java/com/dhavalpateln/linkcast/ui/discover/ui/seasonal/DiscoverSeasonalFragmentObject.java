package com.dhavalpateln.linkcast.ui.discover.ui.seasonal;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dhavalpateln.linkcast.R;
import com.dhavalpateln.linkcast.adapters.MyAnimelistGridRecyclerAdapter;
import com.dhavalpateln.linkcast.database.JikanDatabase;
import com.dhavalpateln.linkcast.myanimelist.MyAnimelistAnimeData;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DiscoverSeasonalFragmentObject#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DiscoverSeasonalFragmentObject extends Fragment {

    private SeasonalViewModel.SeasonType seasonType;
    private String TAG = "SeasonalFragmentObject";
    private MyAnimelistGridRecyclerAdapter gridRecyclerAdapter;
    private List<MyAnimelistAnimeData> dataList;
    private List<MyAnimelistAnimeData> fullList;
    private ProgressBar progressBar;
    private SeasonalViewModel model;
    private TextView seasonTextView;
    private String currentSeasonString = "Later";
    private Button animeTypeButton;

    private Observer<List<MyAnimelistAnimeData>> observer;
    private Executor executor = Executors.newSingleThreadExecutor();
    private Handler uiHandler = new Handler(Looper.getMainLooper());

    public DiscoverSeasonalFragmentObject() {
        // Required empty public constructor
    }

    public static DiscoverSeasonalFragmentObject newInstance(SeasonalViewModel.SeasonType seasonType) {
        DiscoverSeasonalFragmentObject fragment = new DiscoverSeasonalFragmentObject();
        Bundle args = new Bundle();
        args.putSerializable("type", seasonType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            seasonType = (SeasonalViewModel.SeasonType) getArguments().getSerializable("type");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_discover_seasonal_object, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        seasonTextView = view.findViewById(R.id.discover_seasonal_season_text_view);
        progressBar = view.findViewById(R.id.discover_seasonal_progress_bar);
        animeTypeButton = view.findViewById(R.id.discover_seasonal_type_button);
        dataList = new ArrayList<>();
        fullList = new ArrayList<>();
        gridRecyclerAdapter = new MyAnimelistGridRecyclerAdapter(dataList, getContext());

        RecyclerView recyclerView = view.findViewById(R.id.discover_seasonal_recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerView.setAdapter(gridRecyclerAdapter);

        model = new ViewModelProvider(getActivity()).get(SeasonalViewModel.class);
        observer = myAnimelistAnimeData -> updateRecyclerData(myAnimelistAnimeData);

        if(seasonType == SeasonalViewModel.SeasonType.ARCHIVE) {
            executor.execute(() -> {
                List<String> seasonList = JikanDatabase.getInstance().getSeasonList();
                uiHandler.post(() -> {
                    seasonTextView.setOnClickListener(v -> {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setTitle("Choose season");
                        builder.setItems(seasonList.toArray(new String[0]), (dialog, which) -> {
                            dataList.clear();
                            fullList.clear();
                            gridRecyclerAdapter.notifyDataSetChanged();
                            progressBar.setVisibility(View.VISIBLE);
                            String seasonString = seasonList.get(which);
                            model.getData(currentSeasonString).removeObserver(observer);

                            if(currentSeasonString.equals("Later")) {
                                if(!seasonString.equals("Later")) animeTypeButton.setText("TV (New)");
                            }
                            else if(seasonString.equals("Later")) {
                                if(!currentSeasonString.equals("Later")) animeTypeButton.setText("TV");
                            }

                            currentSeasonString = seasonString;

                            model.getData(currentSeasonString).observe(getViewLifecycleOwner(), observer);
                            seasonTextView.setText(seasonString);
                        });
                        builder.show();
                    });
                    seasonTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
                });
            });
            animeTypeButton.setText("TV");
            seasonTextView.setText("Later");
            model.getData("Later").observe(getViewLifecycleOwner(), observer);
        }
        else {
            animeTypeButton.setText("TV (New)");
            seasonTextView.setText(model.getSeasonString(seasonType));
            currentSeasonString = model.getSeasonString(seasonType);
            model.getData(seasonType).observe(getViewLifecycleOwner(), observer);
        }

        animeTypeButton.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(getContext(), v);
            String[] filterTypes;

            if(currentSeasonString.equals("Later")) {
                filterTypes = new String[] {"TV", "ONA", "OVA", "Movie", "Special"};
            }
            else {
                filterTypes = new String[] {"TV (New)", "TV (Continuing)", "ONA", "OVA", "Movie", "Special"};
            }
            for(String filter: filterTypes)   popupMenu.getMenu().add(filter);
            popupMenu.setOnMenuItemClickListener(menuItem -> {
                animeTypeButton.setText(menuItem.getTitle());
                filter();
                return false;
            });
            popupMenu.show();
        });
    }

    private void filter() {
        String type = animeTypeButton.getText().toString();
        dataList.clear();
        for(MyAnimelistAnimeData animeData: fullList) {
            if(animeData.getInfo("type").equals(type)) {
                dataList.add(animeData);
            }
        }
        gridRecyclerAdapter.notifyDataSetChanged();
    }

    private void updateRecyclerData(List<MyAnimelistAnimeData> myAnimelistAnimeData) {
        progressBar.setVisibility(View.GONE);
        fullList.clear();
        fullList.addAll(myAnimelistAnimeData);
        filter();
        //gridRecyclerAdapter.notifyDataSetChanged();
    }
}