package com.dhavalpateln.linkcast.adapters;

import android.content.Context;
import android.content.Intent;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.dhavalpateln.linkcast.adapters.viewholders.AnimeGridViewHolder;
import com.dhavalpateln.linkcast.myanimelist.MyAnimelistAnimeData;
import com.dhavalpateln.linkcast.myanimelist.MyAnimelistCharacterActivity;
import com.dhavalpateln.linkcast.myanimelist.MyAnimelistCharacterData;

import java.util.List;

import androidx.annotation.NonNull;

public class MyAnimeListGridCharacterAdapter extends GridRecyclerAdapter<MyAnimelistCharacterData> {

    private List<MyAnimelistCharacterData> dataList;

    public MyAnimeListGridCharacterAdapter(List<MyAnimelistCharacterData> recyclerDataArrayList, Context mcontext) {
        super(recyclerDataArrayList, mcontext);
        this.dataList = recyclerDataArrayList;
    }

    @Override
    public void onBindViewHolder(@NonNull AnimeGridViewHolder holder, int position) {
        MyAnimelistCharacterData data = dataList.get(position);
        holder.titleTextView.setText(data.getName());
        holder.subTextTextView.setText(data.getType());
        Glide.with(mcontext)
                .load(data.getImages().get(0))
                .centerCrop()
                .transition(new DrawableTransitionOptions().crossFade())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.imageView);
        holder.mainLayout.setOnClickListener(v -> {
            Intent intent = MyAnimelistCharacterActivity.prepareIntent(mcontext, data);
            mcontext.startActivity(intent);
        });
    }
}