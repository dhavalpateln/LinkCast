package com.dhavalpateln.linkcast.ui.discover.ui.popular;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.dhavalpateln.linkcast.R;
import com.dhavalpateln.linkcast.adapters.GridRecyclerAdapter;
import com.dhavalpateln.linkcast.adapters.ListRecyclerAdapter;
import com.dhavalpateln.linkcast.myanimelist.MyAnimelistAnimeData;
import com.dhavalpateln.linkcast.myanimelist.MyAnimelistInfoActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DiscoverPopularFragmentObject#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DiscoverPopularFragmentObject extends Fragment {

    private PopularViewModel.TYPE type;
    private String TAG = "PopularFragmentObject";
    private GridRecyclerAdapter<MyAnimelistAnimeData> gridRecyclerAdapter;
    private List<MyAnimelistAnimeData> dataList;
    private Button prevButton;
    private Button nextButton;
    private TextView currentPageTextView;
    private ProgressBar progressBar;
    private int currentLimit = 0;
    private Map<Integer, List<MyAnimelistAnimeData>> cache;
    private PopularViewModel model;

    public DiscoverPopularFragmentObject() {
        // Required empty public constructor
    }

    public static DiscoverPopularFragmentObject newInstance(PopularViewModel.TYPE type) {
        DiscoverPopularFragmentObject fragment = new DiscoverPopularFragmentObject();
        Bundle args = new Bundle();
        args.putSerializable("type", type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            type = (PopularViewModel.TYPE) getArguments().getSerializable("type");
            currentLimit = 0;
            cache = new HashMap<>();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_discover_popular_object, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressBar = view.findViewById(R.id.discover_popular_load_progress_bar);
        nextButton = view.findViewById(R.id.discover_popular_next_button);
        prevButton = view.findViewById(R.id.discover_popular_prev_button);
        currentPageTextView = view.findViewById(R.id.discover_popular_current_list_text_view);

        nextButton.setOnClickListener(v -> next(v));
        prevButton.setOnClickListener(v -> prev(v));

        dataList = new ArrayList<>();
        gridRecyclerAdapter = new GridRecyclerAdapter<>(dataList, getContext(), (GridRecyclerAdapter.RecyclerInterface<MyAnimelistAnimeData>) (holder, position, data) -> {
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

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), MyAnimelistInfoActivity.class);
                intent.putExtra(MyAnimelistInfoActivity.INTENT_ANIMELIST_DATA_KEY, data);
                startActivity(intent);
            });

            if(data.getMalScoreString() != null) {
                holder.scoreTextView.setVisibility(View.VISIBLE);
                holder.scoreTextView.setText(data.getMalScoreString());
            }
        });

        RecyclerView recyclerView = view.findViewById(R.id.discover_popular_recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerView.setAdapter(gridRecyclerAdapter);

        model = new ViewModelProvider(this).get(PopularViewModel.class);
        model.getData(type).observe(getViewLifecycleOwner(), new Observer<List<MyAnimelistAnimeData>>() {
            @Override
            public void onChanged(List<MyAnimelistAnimeData> myAnimelistAnimeData) {
                updateRecyclerData(myAnimelistAnimeData);
            }
        });
    }

    private void updateRecyclerData(List<MyAnimelistAnimeData> myAnimelistAnimeData) {
        progressBar.setVisibility(View.GONE);
        if(!cache.containsKey(currentLimit)) {
            cache.put(currentLimit, new ArrayList<>(myAnimelistAnimeData));
        }
        dataList.clear();
        dataList.addAll(myAnimelistAnimeData);
        gridRecyclerAdapter.notifyDataSetChanged();
        currentPageTextView.setVisibility(View.VISIBLE);
        currentPageTextView.setText((currentLimit + 1) + " - " + (currentLimit + myAnimelistAnimeData.size()));
    }

    private void updateData() {
        progressBar.setVisibility(View.VISIBLE);
        currentPageTextView.setVisibility(View.GONE);
        if(cache.containsKey(currentLimit)) {
            updateRecyclerData(cache.get(currentLimit));
        }
        else {
            model.loadData(type, currentLimit);
        }
    }

    public void next(View view) {
        if(dataList.size() == 0)    return;
        currentLimit += dataList.size();
        updateData();
    }

    public void prev(View view) {
        if(currentLimit == 0)   return;
        currentLimit -= dataList.size();
        updateData();
    }
}