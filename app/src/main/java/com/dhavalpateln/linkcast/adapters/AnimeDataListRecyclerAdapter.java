package com.dhavalpateln.linkcast.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.dhavalpateln.linkcast.AnimeAdvancedView;
import com.dhavalpateln.linkcast.AnimeWebExplorer;
import com.dhavalpateln.linkcast.MangaAdvancedView;
import com.dhavalpateln.linkcast.MangaWebExplorer;
import com.dhavalpateln.linkcast.R;
import com.dhavalpateln.linkcast.adapters.viewholders.AnimeListViewHolder;
import com.dhavalpateln.linkcast.animesearch.AnimeSearch;
import com.dhavalpateln.linkcast.database.AnimeLinkData;
import com.dhavalpateln.linkcast.database.FirebaseDBHelper;
import com.dhavalpateln.linkcast.dialogs.BookmarkLinkDialog;

import java.util.List;

import androidx.annotation.NonNull;

public class AnimeDataListRecyclerAdapter extends ListRecyclerAdapter<AnimeLinkData> {

    public AnimeDataListRecyclerAdapter(List<AnimeLinkData> recyclerDataArrayList, Context mcontext) {
        super(recyclerDataArrayList, mcontext);
    }

    @Override
    public void onBindViewHolder(@NonNull AnimeListViewHolder holder, int position) {
        AnimeLinkData recyclerData = dataArrayList.get(position);
        if(recyclerData.getId() == null)    holder.scoreTextView.setVisibility(View.GONE);
        else {
            holder.scoreTextView.setText("\u2605" + recyclerData.getAnimeMetaData(AnimeLinkData.DataContract.DATA_USER_SCORE));
        }
        holder.titleTextView.setText(recyclerData.getTitle());
        if(recyclerData.getId() != null) {
            String sourceName = recyclerData.getAnimeMetaData(AnimeLinkData.DataContract.DATA_SOURCE);
            holder.titleTextView.setText(recyclerData.getTitle() + (sourceName.equals("") ? "" : " (" + sourceName + ")"));
        }
        if(recyclerData.getId() == null) {
            holder.deleteButton.setVisibility(View.GONE);
            holder.editButton.setVisibility(View.GONE);
        }

        String imageUrl = recyclerData.getAnimeMetaData(AnimeLinkData.DataContract.DATA_IMAGE_URL);
        if(imageUrl != null) {
            Glide.with(mcontext)
                    .load(imageUrl)
                    .centerCrop()
                    .crossFade()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.animeImageView);
            holder.animeImageView.setClipToOutline(true);
        }
        else {
            holder.animeImageView.setImageDrawable(mcontext.getResources().getDrawable(R.drawable.ic_stat_name));
        }
    }
}
