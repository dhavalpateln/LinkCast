package com.dhavalpateln.linkcast.manga;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.dhavalpateln.linkcast.R;
import com.dhavalpateln.linkcast.adapters.EpisodeGridRecyclerAdapter;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public abstract class MangaRecyclerAdapter extends RecyclerView.Adapter<MangaRecyclerAdapter.RecyclerViewHolder>{
    private String[] dataArrayList;
    private Context mcontext;

    public MangaRecyclerAdapter(String[] recyclerDataArrayList, Context mcontext) {
        this.dataArrayList = recyclerDataArrayList;
        this.mcontext = mcontext;
    }

    @NonNull
    @Override
    public RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate Layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.manga_vertical_page, parent, false);
        return new RecyclerViewHolder(view);
    }

    public abstract void onBindViewHolder(RecyclerViewHolder holder, int position, String imageUrl);

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewHolder holder, int position) {
        // Set the data to textview and imageview.
        this.onBindViewHolder(holder, position, dataArrayList[position]);
    }

    @Override
    public int getItemCount() {
        // this method returns the size of recyclerview
        return dataArrayList.length;
    }

    public class RecyclerViewHolder extends RecyclerView.ViewHolder {

        public ImageView imageView;
        public ConstraintLayout mainLayout;

        public RecyclerViewHolder(@NonNull View itemView) {
            super(itemView);
            this.mainLayout = (ConstraintLayout) itemView;
            this.imageView = itemView.findViewById(R.id.mangaRecyclerImageView);
        }
    }
}
