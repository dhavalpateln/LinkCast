package com.dhavalpateln.linkcast.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.dhavalpateln.linkcast.R;
import com.dhavalpateln.linkcast.adapters.viewholders.AnimeGridViewHolder;
import com.dhavalpateln.linkcast.myanimelist.MyAnimelistAnimeData;
import com.dhavalpateln.linkcast.myanimelist.MyAnimelistInfoActivity;
import com.dhavalpateln.linkcast.utils.Utils;

import java.util.List;

public abstract class CatalogGridRecyclerAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<T> dataArrayList;
    protected Context mcontext;

    public CatalogGridRecyclerAdapter(List<T> recyclerDataArrayList, Context mcontext) {
        this.dataArrayList = recyclerDataArrayList;
        this.mcontext = mcontext;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate Layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_catalog_grid_recycler_object, parent, false);
        return new CatalogObjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        CatalogObjectViewHolder viewHolder = (CatalogObjectViewHolder) holder;

        T item = dataArrayList.get(position);
        String title = getTitle(item);
        String subTitle = getSubTitle(item);
        String imageUrl = getImageUrl(item);
        String score = getScoreText(item);

        if(title != null) {
            viewHolder.titleTextView.setText(title);
            viewHolder.titleTextView.setSelected(true);
        }
        else viewHolder.titleTextView.setVisibility(View.GONE);

        if(subTitle != null) viewHolder.subTextTextView.setText(subTitle);
        else viewHolder.subTextTextView.setVisibility(View.GONE);

        if(score != null) viewHolder.scoreTextView.setText(score);
        else viewHolder.scoreTextView.setVisibility(View.GONE);

        if(imageUrl != null) {
            Glide.with(mcontext)
                    .load(imageUrl)
                    .transition(new DrawableTransitionOptions().crossFade())
                    .centerCrop()
                    .transform(new RoundedCorners(50))
                    .error(R.drawable.ic_stat_name)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(viewHolder.imageView);
        }

        viewHolder.itemView.setOnClickListener(v -> {
            onClick(item);
        });
        Utils.startScaleAnimation(viewHolder.mainLayout);
    }

    public abstract String getTitle(T item);
    public abstract String getSubTitle(T item);
    public abstract String getImageUrl(T item);
    public abstract String getScoreText(T item);
    public abstract void onClick(T item);

    @Override
    public int getItemCount() {
        // this method returns the size of recyclerview
        return dataArrayList.size();
    }

    private class CatalogObjectViewHolder extends RecyclerView.ViewHolder {

        public TextView subTextTextView;
        public TextView titleTextView;
        public ImageView imageView;
        public TextView scoreTextView;
        public ConstraintLayout mainLayout;

        public CatalogObjectViewHolder(@NonNull View itemView) {
            super(itemView);
            this.mainLayout = (ConstraintLayout) itemView;
            this.subTextTextView = itemView.findViewById(R.id.catalog_object_sub_title_text_view);
            this.titleTextView = itemView.findViewById(R.id.catalog_object_title_text_view);
            this.imageView = itemView.findViewById(R.id.catalog_object_image_view);
            this.scoreTextView = itemView.findViewById(R.id.catalog_object_score_text_view);
        }
    }
}