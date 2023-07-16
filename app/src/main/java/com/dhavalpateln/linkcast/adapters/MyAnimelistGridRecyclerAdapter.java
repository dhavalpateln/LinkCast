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

public class MyAnimelistGridRecyclerAdapter extends CatalogGridRecyclerAdapter<MyAnimelistAnimeData> {

    public MyAnimelistGridRecyclerAdapter(List<MyAnimelistAnimeData> recyclerDataArrayList, Context mcontext) {
        super(recyclerDataArrayList, mcontext);
    }

    @Override
    public String getTitle(MyAnimelistAnimeData item) {
        return item.getTitle();
    }

    @Override
    public String getSubTitle(MyAnimelistAnimeData item) {
        return !item.getInfo("Genres").equals("N/A") ? item.getInfo("Genres") : null;
    }

    @Override
    public String getImageUrl(MyAnimelistAnimeData item) {
        return item.getImages().get(0);
    }

    @Override
    public String getScoreText(MyAnimelistAnimeData item) {
        return item.getMalScoreString();
    }

    @Override
    public void onClick(MyAnimelistAnimeData item) {
        Intent intent = new Intent(mcontext, MyAnimelistInfoActivity.class);
        intent.putExtra(MyAnimelistInfoActivity.INTENT_ANIMELIST_DATA_KEY, item);
        mcontext.startActivity(intent);
    }
}
