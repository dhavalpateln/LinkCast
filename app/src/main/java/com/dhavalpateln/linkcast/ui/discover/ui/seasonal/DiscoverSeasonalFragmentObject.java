package com.dhavalpateln.linkcast.ui.discover.ui.seasonal;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.dhavalpateln.linkcast.R;
import com.dhavalpateln.linkcast.adapters.GridRecyclerAdapter;
import com.dhavalpateln.linkcast.database.AnimeLinkData;
import com.dhavalpateln.linkcast.database.JikanDatabase;
import com.dhavalpateln.linkcast.myanimelist.MyAnimelistAnimeData;
import com.dhavalpateln.linkcast.myanimelist.MyAnimelistInfoActivity;
import com.dhavalpateln.linkcast.ui.catalog.CatalogFragment;

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
    private GridRecyclerAdapter<MyAnimelistAnimeData> gridRecyclerAdapter;
    private List<MyAnimelistAnimeData> dataList;
    private ProgressBar progressBar;
    private SeasonalViewModel model;
    private TextView seasonTextView;
    private String currentSeasonString = "Later";

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
        dataList = new ArrayList<>();
        gridRecyclerAdapter = new GridRecyclerAdapter<>(dataList, getContext(), new GridRecyclerAdapter.RecyclerInterface<MyAnimelistAnimeData>() {

            @Override
            public void onBindView(GridRecyclerAdapter.GridRecyclerViewHolder holder, int position, MyAnimelistAnimeData data) {
                holder.titleTextView.setText(data.getTitle());
                holder.subTextTextView.setText(data.getInfo("Genres"));
                try {
                    Glide.with(getContext())
                            .load(data.getImages().get(0))
                            .centerCrop()
                            .crossFade()
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(holder.imageView);
                } catch (Exception e) {e.printStackTrace();}
                if(data.getMalScoreString() != null) {
                    holder.scoreTextView.setVisibility(View.VISIBLE);
                    holder.scoreTextView.setText(data.getMalScoreString());
                }
                holder.itemView.setOnClickListener(v -> {
                    Intent intent = new Intent(getContext(), MyAnimelistInfoActivity.class);
                    intent.putExtra(MyAnimelistInfoActivity.INTENT_ANIMELIST_DATA_KEY, data);
                    startActivity(intent);
                });
            }
        });

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
                            gridRecyclerAdapter.notifyDataSetChanged();
                            progressBar.setVisibility(View.VISIBLE);
                            String seasonString = seasonList.get(which);
                            model.getData(currentSeasonString).removeObserver(observer);
                            currentSeasonString = seasonString;
                            model.getData(currentSeasonString).observe(getViewLifecycleOwner(), observer);
                            seasonTextView.setText(seasonString);
                        });
                        builder.show();
                    });
                    seasonTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
                });
            });
            seasonTextView.setText("Later");
            model.getData("Later").observe(getViewLifecycleOwner(), observer);
        }
        else {
            seasonTextView.setText(model.getSeasonString(seasonType));
            model.getData(seasonType).observe(getViewLifecycleOwner(), observer);
        }
    }

    private void updateRecyclerData(List<MyAnimelistAnimeData> myAnimelistAnimeData) {
        progressBar.setVisibility(View.GONE);
        dataList.clear();
        dataList.addAll(myAnimelistAnimeData);
        gridRecyclerAdapter.notifyDataSetChanged();
    }
}