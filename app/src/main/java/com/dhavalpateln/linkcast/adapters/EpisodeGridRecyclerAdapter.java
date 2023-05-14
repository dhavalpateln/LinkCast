package com.dhavalpateln.linkcast.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dhavalpateln.linkcast.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public abstract class EpisodeGridRecyclerAdapter<T> extends RecyclerView.Adapter<EpisodeGridRecyclerAdapter.EpisodeRecyclerViewHolder>{

    private List<T> dataArrayList;
    private Context mcontext;

    public EpisodeGridRecyclerAdapter(List<T> recyclerDataArrayList, Context mcontext) {
        this.dataArrayList = recyclerDataArrayList;
        this.mcontext = mcontext;
    }

    @NonNull
    @Override
    public EpisodeGridRecyclerAdapter.EpisodeRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate Layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_episode_node_grid_object, parent, false);
        return new EpisodeGridRecyclerAdapter.EpisodeRecyclerViewHolder(view);
    }

    public abstract void onBindViewHolder(EpisodeGridRecyclerAdapter.EpisodeRecyclerViewHolder holder, int position, T data);

    @Override
    public void onBindViewHolder(@NonNull EpisodeGridRecyclerAdapter.EpisodeRecyclerViewHolder holder, int position) {
        // Set the data to textview and imageview.
        this.onBindViewHolder(holder, position, dataArrayList.get(position));
    }

    @Override
    public int getItemCount() {
        // this method returns the size of recyclerview
        return dataArrayList.size();
    }

    public class EpisodeRecyclerViewHolder extends RecyclerView.ViewHolder {

        public TextView episodeNumTextView;
        public ConstraintLayout mainLayout;
        public View selectedIndicator;
        public View noteIndicator;

        public EpisodeRecyclerViewHolder(@NonNull View itemView) {
            super(itemView);
            this.mainLayout = (ConstraintLayout) itemView;
            this.episodeNumTextView = itemView.findViewById(R.id.advanced_view_episode_num);
            this.noteIndicator = itemView.findViewById(R.id.episode_note_indicator_image_view);
            this.selectedIndicator = itemView.findViewById(R.id.episode_note_current_indicator);
        }
    }

}
