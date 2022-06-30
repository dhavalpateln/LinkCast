package com.dhavalpateln.linkcast.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dhavalpateln.linkcast.R;
import com.dhavalpateln.linkcast.adapters.viewholders.AnimeGridViewHolder;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public abstract class GridRecyclerAdapter<T> extends RecyclerView.Adapter<AnimeGridViewHolder> {

    private List<T> dataArrayList;
    protected Context mcontext;

    public GridRecyclerAdapter(List<T> recyclerDataArrayList, Context mcontext) {
        this.dataArrayList = recyclerDataArrayList;
        this.mcontext = mcontext;
    }

    @NonNull
    @Override
    public AnimeGridViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate Layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_recycler_object, parent, false);
        return new AnimeGridViewHolder(view);
    }

    @Override
    public int getItemCount() {
        // this method returns the size of recyclerview
        return dataArrayList.size();
    }
}