package com.dhavalpateln.linkcast.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.dhavalpateln.linkcast.R;
import com.dhavalpateln.linkcast.adapters.viewholders.NotesViewHolder;
import com.dhavalpateln.linkcast.database.JikanDatabase;
import com.dhavalpateln.linkcast.utils.EpisodeNode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class EpisodeInfoDialog extends LinkCastDialog {

    private Spinner episodeRangeTextView;
    ArrayAdapter<String> sourceSpinnerAdapter;
    private RecyclerView recyclerView;
    private List<EpisodeNode> dataList;
    private List<String> episodeRanges;
    private String animeID;
    private EpisodeInfoAdapter adapter;
    private ProgressBar progressBar;
    private boolean firstExtract = true;
    private Handler uiHandler = new Handler(Looper.getMainLooper());
    private Executor executor = Executors.newCachedThreadPool();
    private int total_pages = 0;

    @Override
    public int getContentLayout() {
        return R.layout.dialog_episode_info;
    }

    public EpisodeInfoDialog(String id, int total_episodes) {
        episodeRanges = new ArrayList<>();
        dataList = new ArrayList<>();
        this.animeID = id;
        double ceilVal = Math.ceil((total_episodes + 1.0) / 100);
        total_pages = (int) ceilVal;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        View view = getContentView();
        recyclerView = view.findViewById(R.id.dialog_episode_info_recycler_view);
        episodeRangeTextView = view.findViewById(R.id.dialog_episode_info_episode_range_text_view);
        progressBar = view.findViewById(R.id.dialog_episode_info_progress_bar);

        adapter = new EpisodeInfoAdapter();
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        sourceSpinnerAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item);
        sourceSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        for(int pageNum = 0; pageNum < total_pages; pageNum++) {
            int startEpisode = (pageNum * 100) + 1;
            sourceSpinnerAdapter.add(startEpisode + " - " + (startEpisode + 99));
        }
        sourceSpinnerAdapter.notifyDataSetChanged();
        episodeRangeTextView.setAdapter(sourceSpinnerAdapter);



        episodeRangeTextView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                executor.execute(new ExtractEpisodeInfo(i + 1, animeID));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        return dialog;
    }

    private class EpisodeInfoAdapter extends RecyclerView.Adapter<NotesViewHolder> {

        @NonNull
        @Override
        public NotesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_note_oblect, parent, false);
            return new NotesViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull NotesViewHolder holder, int position) {
            holder.episodeNumTextView.setText("Episode - " + dataList.get(position).getEpisodeNumString());
            holder.noteTextView.setText(dataList.get(position).getTitle());
        }

        @Override
        public int getItemCount() {
            return dataList.size();
        }
    }

    private class ExtractEpisodeInfo implements Runnable {

        private int page;
        private String id;

        public ExtractEpisodeInfo(int page, String id) {
            this.page = page;
            this.id = id;
        }

        @Override
        public void run() {
            uiHandler.post(() -> {
                progressBar.setVisibility(View.VISIBLE);
                dataList.clear();
                adapter.notifyDataSetChanged();
            });

            List<EpisodeNode> tempDataList = new ArrayList<>();
            int total_pages = JikanDatabase.getInstance().getEpisodeInfo(tempDataList, page, id);

            uiHandler.post(() -> {
                dataList.addAll(tempDataList);
                progressBar.setVisibility(View.GONE);
                adapter.notifyDataSetChanged();
            });
        }
    }
}
