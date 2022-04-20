package com.dhavalpateln.linkcast.ui.discover.ui.suggested;

import android.app.ProgressDialog;
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
import com.dhavalpateln.linkcast.AnimeAdvancedView;
import com.dhavalpateln.linkcast.R;
import com.dhavalpateln.linkcast.adapters.GridRecyclerAdapter;
import com.dhavalpateln.linkcast.data.StoredAnimeLinkData;
import com.dhavalpateln.linkcast.database.AnimeLinkData;
import com.dhavalpateln.linkcast.database.MyAnimeListDatabase;
import com.dhavalpateln.linkcast.myanimelist.MyAnimelistAnimeData;
import com.dhavalpateln.linkcast.myanimelist.MyAnimelistInfoActivity;
import com.dhavalpateln.linkcast.utils.Utils;

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
    private Set<Integer> userMalIDs;
    private RecyclerView recyclerView;
    private GridRecyclerAdapter<MyAnimelistAnimeData> recyclerAdapter;
    private List<MyAnimelistAnimeData> dataList;
    private ProgressDialog progressDialog;

    private Executor executor = Executors.newCachedThreadPool();
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
                    if(data.getMalScoreString() != null) {
                        holder.scoreTextView.setVisibility(View.VISIBLE);
                        holder.scoreTextView.setText(data.getMalScoreString());
                    }
                }
        );

        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Please Wait...");
        progressDialog.setCancelable(false);

        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerView.setAdapter(recyclerAdapter);

        generateRecommendations();
    }

    private void updateUserRankAnimeMap() {
        if(userRankAnimeMap == null) {
            Map<String, AnimeLinkData> userAnimeMap = StoredAnimeLinkData.getInstance().getCache();
            userMalIDs = new HashSet<>();
            userRankAnimeMap = new HashMap<>();
            for (int i = 0; i <= 10; i++) {
                userRankAnimeMap.put(String.valueOf(i), new ArrayList<>());
            }
            for (Map.Entry<String, AnimeLinkData> entry : userAnimeMap.entrySet()) {
                AnimeLinkData animeLinkData = entry.getValue();
                String malURL = animeLinkData.getAnimeMetaData(AnimeLinkData.DataContract.DATA_MYANIMELIST_URL);
                if (malURL != null) {
                    MyAnimelistAnimeData animelistAnimeData = new MyAnimelistAnimeData();
                    animelistAnimeData.setUrl(malURL);
                    animelistAnimeData.setSearchScore(Double.valueOf(animeLinkData.getAnimeMetaData(AnimeLinkData.DataContract.DATA_USER_SCORE)));
                    userRankAnimeMap.get(animeLinkData.getAnimeMetaData(AnimeLinkData.DataContract.DATA_USER_SCORE)).add(animelistAnimeData);
                    userMalIDs.add(animelistAnimeData.getId());
                }
            }
        }
    }

    private void selectCandidates(int count, String score, List<MyAnimelistAnimeData> result) {
        List<MyAnimelistAnimeData> pool = userRankAnimeMap.get(score);
        if(pool.size() > 0) {
            for(int i = 0; i < count; i++)
                result.add(pool.get(Utils.getRandomInt(0, pool.size())));
        }
    }

    private void generateRecommendations() {
        executor.execute(() -> {

            uiHandler.post(() -> progressDialog.show());
            List<MyAnimelistAnimeData> result = new ArrayList<>();
            try {
                updateUserRankAnimeMap();

                List<MyAnimelistAnimeData> baseCandidates = new ArrayList<>(100);

                selectCandidates(40, "10", baseCandidates);
                selectCandidates(30, "9", baseCandidates);
                selectCandidates(15, "8", baseCandidates);
                selectCandidates(15, "7", baseCandidates);

                if (baseCandidates.size() != 0) {

                    List<MyAnimelistAnimeData> recommedationPool = new ArrayList<>();

                    Collections.shuffle(baseCandidates);
                    Set<MyAnimelistAnimeData> finalCandidates = new HashSet<>();
                    for (int i = 0; i < baseCandidates.size() && finalCandidates.size() <= 10; i++) {
                        finalCandidates.add(baseCandidates.get(i));
                    }

                    for (MyAnimelistAnimeData candidate : finalCandidates) {
                        MyAnimeListDatabase.getInstance().getRecommendations(candidate);
                        List<MyAnimelistAnimeData> candidateRecommendations = candidate.getRecommendations();
                        List<MyAnimelistAnimeData> selectedRecommendations = new ArrayList<>(5);
                        for (int i = 0; i < candidateRecommendations.size() && selectedRecommendations.size() <= 5; i++) {
                            if (!userMalIDs.contains(candidateRecommendations.get(i).getId())) {
                                selectedRecommendations.add(candidateRecommendations.get(i));
                            }
                        }
                        for(MyAnimelistAnimeData recommendation: selectedRecommendations) {
                            if(!recommedationPool.contains(recommendation)) {
                                recommedationPool.add(recommendation);
                            }
                        }
                    }
                    addRandom(result, recommedationPool, 20);
                }
            } catch (Exception e) { e.printStackTrace(); }

            uiHandler.post(() -> {
                progressDialog.dismiss();
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