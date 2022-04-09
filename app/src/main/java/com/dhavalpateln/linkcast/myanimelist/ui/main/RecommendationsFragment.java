package com.dhavalpateln.linkcast.myanimelist.ui.main;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.dhavalpateln.linkcast.R;
import com.dhavalpateln.linkcast.adapters.GridRecyclerAdapter;
import com.dhavalpateln.linkcast.database.MyAnimeListDatabase;
import com.dhavalpateln.linkcast.myanimelist.MyAnimelistAnimeData;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RecommendationsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RecommendationsFragment extends Fragment {

    private RecyclerView recyclerView;
    private List<MyAnimelistAnimeData> dataList;
    private ProgressBar progressBar;
    private Executor executor = Executors.newSingleThreadExecutor();
    private Handler uiHandler = new Handler(Looper.getMainLooper());

    public RecommendationsFragment() {
        // Required empty public constructor
    }

    public static RecommendationsFragment newInstance() {
        RecommendationsFragment fragment = new RecommendationsFragment();
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
        return inflater.inflate(R.layout.fragment_recommendations, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dataList = new ArrayList<>();
        recyclerView = view.findViewById(R.id.mal_info_recommendations_recycler_view);
        progressBar = view.findViewById(R.id.mal_recommendation_progressbar);

        GridRecyclerAdapter<MyAnimelistAnimeData> recyclerAdapter = new GridRecyclerAdapter<>(dataList, getContext(), (GridRecyclerAdapter.RecyclerInterface<MyAnimelistAnimeData>) (holder, position, data) -> {
            holder.titleTextView.setText(data.getTitle());
            Glide.with(getContext())
                    .load(data.getImages().get(0))
                    .centerCrop()
                    .crossFade()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.imageView);
        });

        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerView.setAdapter(recyclerAdapter);

        MyAnimelistDataViewModel viewModel = new ViewModelProvider(getActivity()).get(MyAnimelistDataViewModel.class);
        viewModel.getData().observe(getViewLifecycleOwner(), myAnimelistAnimeData -> {
            if(myAnimelistAnimeData.getId() > 0) {
                progressBar.setVisibility(View.VISIBLE);
                executor.execute(() -> {
                    MyAnimeListDatabase.getInstance().getRecommendations(myAnimelistAnimeData);
                    uiHandler.post(() -> {
                        progressBar.setVisibility(View.GONE);
                        dataList.clear();
                        dataList.addAll(myAnimelistAnimeData.getRecommendations());
                        recyclerAdapter.notifyDataSetChanged();
                        if(dataList.size() == 0) {
                            Toast.makeText(getContext(), "No Recommendations found", Toast.LENGTH_LONG).show();
                        }
                    });
                });
            }
        });
    }


}