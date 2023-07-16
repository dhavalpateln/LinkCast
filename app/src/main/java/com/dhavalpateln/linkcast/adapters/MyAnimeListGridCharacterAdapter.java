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

public class MyAnimeListGridCharacterAdapter extends CatalogGridRecyclerAdapter<MyAnimelistCharacterData> {


    public MyAnimeListGridCharacterAdapter(List<MyAnimelistCharacterData> recyclerDataArrayList, Context mcontext) {
        super(recyclerDataArrayList, mcontext);
    }

    @Override
    public String getTitle(MyAnimelistCharacterData item) {
        return item.getName();
    }

    @Override
    public String getSubTitle(MyAnimelistCharacterData item) {
        return null;
    }

    @Override
    public String getImageUrl(MyAnimelistCharacterData item) {
        return item.getImages().get(0);
    }

    @Override
    public String getScoreText(MyAnimelistCharacterData item) {
        return null;
    }

    @Override
    public void onClick(MyAnimelistCharacterData item) {
        Intent intent = MyAnimelistCharacterActivity.prepareIntent(mcontext, item);
        mcontext.startActivity(intent);
    }

}