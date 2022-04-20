package com.dhavalpateln.linkcast.myanimelist.ui.main;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.dhavalpateln.linkcast.R;
import com.dhavalpateln.linkcast.adapters.VideoRecyclerAdapter;
import com.dhavalpateln.linkcast.database.MyAnimeListDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AnimeVideosFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AnimeVideosFragment extends Fragment {

    private List<MyAnimeListDatabase.VideoData> dataList;
    private VideoRecyclerAdapter<MyAnimeListDatabase.VideoData> recyclerAdapter;
    private RecyclerView videoRecyclerView;

    private Executor executor = Executors.newSingleThreadExecutor();
    private Handler uiHandler = new Handler(Looper.getMainLooper());

    public AnimeVideosFragment() {
        // Required empty public constructor
    }

    public static AnimeVideosFragment newInstance() {
        AnimeVideosFragment fragment = new AnimeVideosFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataList = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_character_info, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        videoRecyclerView = view.findViewById(R.id.mal_characters_recycler_view);

        recyclerAdapter = new VideoRecyclerAdapter<>(dataList, getContext(), new VideoRecyclerAdapter.RecyclerInterface<MyAnimeListDatabase.VideoData>() {
            @Override
            public void onBindView(VideoRecyclerAdapter.ListRecyclerViewHolder holder, int position, MyAnimeListDatabase.VideoData data) {
                holder.titleTextView.setText(data.getTitle());
                Glide.with(getContext())
                        .load(data.getImageURL())
                        .centerCrop()
                        .crossFade()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(holder.imageView);
                holder.mainLayout.setOnClickListener(v -> {
                    //WebViewDialog dialog = new WebViewDialog(data.getUrl());
                    //dialog.show(getChildFragmentManager(), "PV");
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(data.getUrl()));
                    startActivity(intent);
                });
            }
        });
        videoRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        videoRecyclerView.setAdapter(recyclerAdapter);


        MyAnimelistDataViewModel viewModel = new ViewModelProvider(getActivity()).get(MyAnimelistDataViewModel.class);
        viewModel.getData().observe(getViewLifecycleOwner(), myAnimelistAnimeData -> {
            if(myAnimelistAnimeData.getId() > 0) {
                executor.execute(() -> {
                    List<MyAnimeListDatabase.VideoData> result = MyAnimeListDatabase.getInstance().fetchVideos(myAnimelistAnimeData);
                    uiHandler.post(() -> updateRecyclerView(result));
                });
            }
            Log.d("MyAnimeVideoFrag", "Changed");
        });
    }

    private void updateRecyclerView(List<MyAnimeListDatabase.VideoData> videoDataList) {
        dataList.clear();
        dataList.addAll(videoDataList);
        recyclerAdapter.notifyDataSetChanged();
    }

}