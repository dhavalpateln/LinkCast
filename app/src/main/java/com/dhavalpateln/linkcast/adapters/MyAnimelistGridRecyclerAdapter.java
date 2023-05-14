package com.dhavalpateln.linkcast.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.dhavalpateln.linkcast.adapters.viewholders.AnimeGridViewHolder;
import com.dhavalpateln.linkcast.myanimelist.MyAnimelistAnimeData;
import com.dhavalpateln.linkcast.myanimelist.MyAnimelistInfoActivity;

import java.util.List;

import androidx.annotation.NonNull;

public class MyAnimelistGridRecyclerAdapter extends GridRecyclerAdapter<MyAnimelistAnimeData> {

    private List<MyAnimelistAnimeData> dataList;

    public MyAnimelistGridRecyclerAdapter(List<MyAnimelistAnimeData> recyclerDataArrayList, Context mcontext) {
        super(recyclerDataArrayList, mcontext);
        this.dataList = recyclerDataArrayList;
    }

    @Override
    public void onBindViewHolder(@NonNull AnimeGridViewHolder holder, int position) {
        MyAnimelistAnimeData data = dataList.get(position);
        holder.titleTextView.setText(data.getTitle());
        if(!data.getInfo("Genres").equals("N/A"))
            holder.subTextTextView.setText(data.getInfo("Genres"));
        try {
            Glide.with(mcontext)
                    .load(data.getImages().get(0))
                    .centerCrop()
                    .transition(new DrawableTransitionOptions().crossFade())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.imageView);
        } catch (Exception e) {e.printStackTrace();}

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(mcontext, MyAnimelistInfoActivity.class);
            intent.putExtra(MyAnimelistInfoActivity.INTENT_ANIMELIST_DATA_KEY, data);
            mcontext.startActivity(intent);
        });

        if(data.getMalScoreString() != null) {
            holder.scoreTextView.setVisibility(View.VISIBLE);
            holder.scoreTextView.setText(data.getMalScoreString());
        }
    }
}
