package com.dhavalpateln.linkcast.ui.discover.ui.suggested;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.dhavalpateln.linkcast.R;
import com.dhavalpateln.linkcast.adapters.GridRecyclerAdapter;
import com.dhavalpateln.linkcast.data.StoredAnimeLinkData;
import com.dhavalpateln.linkcast.database.AnimeLinkData;
import com.dhavalpateln.linkcast.database.MyAnimeListDatabase;
import com.dhavalpateln.linkcast.myanimelist.MyAnimelistAnimeData;
import com.dhavalpateln.linkcast.myanimelist.MyAnimelistInfoActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SuggestedFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SuggestedFragment extends Fragment {

    private Map<String, List<MyAnimelistAnimeData>> userRankAnimeMap;
    private RecyclerView recyclerView;
    private GridRecyclerAdapter<MyAnimelistAnimeData> recyclerAdapter;
    private List<MyAnimelistAnimeData> dataList;

    private Executor executor = Executors.newSingleThreadExecutor();
    private Handler uiHandler = new Handler(Looper.getMainLooper());

    public SuggestedFragment() {
        // Required empty public constructor
    }

    public static SuggestedFragment newInstance() {
        SuggestedFragment fragment = new SuggestedFragment();
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
        return inflater.inflate(R.layout.fragment_suggested, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dataList = new ArrayList<>();
        recyclerView = view.findViewById(R.id.mal_suggested_recycler_view);
        recyclerAdapter = new GridRecyclerAdapter<>(
                dataList,
                getContext(),
                (GridRecyclerAdapter.RecyclerInterface<MyAnimelistAnimeData>) (holder, position, data) -> {
                    holder.titleTextView.setText(data.getTitle());
                    try {
                        Glide.with(getContext())
                                .load(data.getImages().get(0))
                                .centerCrop()
                                .crossFade()
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .into(holder.imageView);
                    } catch (Exception e) {e.printStackTrace();}
                    holder.mainLayout.setOnClickListener(v -> {
                        Intent intent = MyAnimelistInfoActivity.prepareIntent(getContext(), data);
                        startActivity(intent);
                    });
                }
        );

        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerView.setAdapter(recyclerAdapter);

        executor.execute(() -> {
            Map<String, AnimeLinkData> userAnimeMap = StoredAnimeLinkData.getInstance().getCache();
            userRankAnimeMap = new HashMap<>();
            for(int i = 0; i <= 10; i++) { userRankAnimeMap.put(String.valueOf(i), new ArrayList<>()); }
            for(Map.Entry<String, AnimeLinkData> entry: userAnimeMap.entrySet()) {
                AnimeLinkData animeLinkData = entry.getValue();
                String malURL = animeLinkData.getAnimeMetaData(AnimeLinkData.DataContract.DATA_MYANIMELIST_URL);
                if(malURL != null) {
                    MyAnimelistAnimeData animelistAnimeData = new MyAnimelistAnimeData();
                    animelistAnimeData.setUrl(malURL);
                    userRankAnimeMap.get(animeLinkData.getAnimeMetaData(AnimeLinkData.DataContract.DATA_USER_SCORE)).add(animelistAnimeData);
                }

            }
            generateRecommendations();
        });
    }

    private void generateRecommendations() {
        executor.execute(() -> {
            List<MyAnimelistAnimeData> result = new ArrayList<>();
            Set<MyAnimelistAnimeData> toFetchPool = new HashSet<>();
            List<MyAnimelistAnimeData> recommendationPool = new ArrayList<>();
            for(int score = 1; score <= 10; score++) {
                List<MyAnimelistAnimeData> animeList = userRankAnimeMap.get(String.valueOf(score));
                List<MyAnimelistAnimeData> randomSelected = new ArrayList<>();
                if(score < 6) {
                    addRandom(randomSelected, animeList, 1);
                }
                else {
                    addRandom(randomSelected, animeList, score);
                }
                for(MyAnimelistAnimeData anime: randomSelected) {
                    for (int i = 0; i <= score; i++)
                        recommendationPool.add(anime);
                }
            }
            Collections.shuffle(recommendationPool);
            if(recommendationPool.size() < 10) {
                addRandom(recommendationPool, userRankAnimeMap.get("0"), 10 - recommendationPool.size());
            }
            for(int i = 0; i < recommendationPool.size(); i++) {
                toFetchPool.add(recommendationPool.get(i));
                if(toFetchPool.size() == 10)    break;
            }


            for(MyAnimelistAnimeData selectedAnime: toFetchPool) {
                MyAnimeListDatabase.getInstance().getRecommendations(selectedAnime);
            }

            MyAnimelistAnimeData recommendationDummy = new MyAnimelistAnimeData();
            for(MyAnimelistAnimeData animelistAnimeData: toFetchPool) {
                MyAnimeListDatabase.getInstance().getRecommendations(animelistAnimeData);
                for(MyAnimelistAnimeData recommendation: animelistAnimeData.getRecommendations()) {
                    recommendationDummy.addRecommendation(recommendation);
                }
            }

            List<MyAnimelistAnimeData> resultRecommendationPool = recommendationDummy.getRecommendations();
            addRandom(result, resultRecommendationPool, 20);

            uiHandler.post(() -> {
                dataList.clear();
                dataList.addAll(result);
                recyclerAdapter.notifyDataSetChanged();
            });
        });
    }

    private void addRandom(List<MyAnimelistAnimeData> destination, List<MyAnimelistAnimeData> source, int num) {
        Collections.shuffle(source);
        for(int i = 0; i < Math.min(num, source.size()); i++) {
            destination.add(source.get(i));
        }
    }
}