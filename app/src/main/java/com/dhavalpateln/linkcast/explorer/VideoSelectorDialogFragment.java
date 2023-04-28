package com.dhavalpateln.linkcast.explorer;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dhavalpateln.linkcast.R;
import com.dhavalpateln.linkcast.adapters.viewholders.AnimeListViewHolder;
import com.dhavalpateln.linkcast.database.VideoURLData;
import com.dhavalpateln.linkcast.database.room.animelinkcache.LinkWithAllData;
import com.dhavalpateln.linkcast.exoplayer.ExoPlayerActivity;
import com.dhavalpateln.linkcast.explorer.listeners.VideoSelectedListener;
import com.dhavalpateln.linkcast.explorer.listeners.VideoServerListener;
import com.dhavalpateln.linkcast.explorer.tasks.ExtractVideoServers;
import com.dhavalpateln.linkcast.explorer.tasks.TaskCompleteListener;
import com.dhavalpateln.linkcast.extractors.AnimeExtractor;
import com.dhavalpateln.linkcast.utils.EpisodeNode;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class VideoSelectorDialogFragment extends BottomSheetDialogFragment implements VideoServerListener, TaskCompleteListener {

    private AnimeExtractor extractor;
    private EpisodeNode node;
    private LinkWithAllData linkWithAllData;
    private String episodeNum;
    boolean askResume;
    private List<VideoURLData> videoList;

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private RecyclerView.Adapter adapter;
    private VideoSelectedListener listener;

    public VideoSelectorDialogFragment(AnimeExtractor extractor, LinkWithAllData linkWithAllData, EpisodeNode node, VideoSelectedListener videoSelectedListener) {
        this.extractor = extractor;
        this.node = node;
        this.linkWithAllData = linkWithAllData;
        this.listener = videoSelectedListener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_video_selector, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.recyclerView = view.findViewById(R.id.video_list_recycler_view);
        this.progressBar = view.findViewById(R.id.video_progress_bar);
        Executors.newCachedThreadPool().execute(new ExtractVideoServers(this.extractor, this.node.getUrl(), this, this));
    }

    @Override
    public void onVideoServerExtracted(List<VideoURLData> videoServerList) {
        this.videoList = new ArrayList<>();
        videoList.addAll(videoServerList);
        adapter = new VideoListAdapter(videoServerList);
        this.recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        this.recyclerView.setHasFixedSize(true);
        this.recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    private void play(VideoURLData videoURLData) {
        videoURLData.setEpisodeNum(node.getEpisodeNumString());
        this.listener.onVideoSelected(videoURLData);
        dismiss();
    }

    @Override
    public void onTaskCompleted(String taskName) {
        this.progressBar.setVisibility(View.GONE);
    }

    private class VideoListAdapter extends RecyclerView.Adapter<VideoListViewHolder> {

        private List<VideoURLData> videoList;

        public VideoListAdapter(List<VideoURLData> videoList) {
            this.videoList = videoList;
        }

        @NonNull
        @Override
        public VideoListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_video_server, parent, false);
            return new VideoListViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull VideoListViewHolder holder, int position) {
            holder.mainLayout.setOnClickListener(v -> play(videoList.get(position)));
            holder.videoServerTitleTextView.setText(this.videoList.get(position).getTitle());
        }

        @Override
        public int getItemCount() {
            return this.videoList.size();
        }
    }

    private class VideoListViewHolder extends RecyclerView.ViewHolder {

        TextView videoServerTitleTextView;
        MaterialButton videoPlayButton;
        ConstraintLayout mainLayout;

        public VideoListViewHolder(@NonNull View itemView) {
            super(itemView);
            this.mainLayout = (ConstraintLayout) itemView;
            this.videoServerTitleTextView = itemView.findViewById(R.id.video_server_title);
            this.videoPlayButton = itemView.findViewById(R.id.video_play_button);
        }
    }
}
