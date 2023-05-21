package com.dhavalpateln.linkcast.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.dhavalpateln.linkcast.R;
import com.dhavalpateln.linkcast.adapters.viewholders.LinkDataViewHolder;
import com.dhavalpateln.linkcast.database.AnimeLinkData;
import com.dhavalpateln.linkcast.database.room.animelinkcache.LinkWithAllData;
import com.dhavalpateln.linkcast.utils.Utils;

import java.util.List;

public class LinkDataListRecyclerAdapter extends RecyclerView.Adapter<LinkDataViewHolder> {

    private Context mcontext;
    private List<LinkWithAllData> dataArrayList;

    public LinkDataListRecyclerAdapter(List<LinkWithAllData> recyclerDataArrayList, Context mcontext) {
        this.mcontext = mcontext;
        this.dataArrayList = recyclerDataArrayList;
    }

    @NonNull
    @Override
    public LinkDataViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_linkdata_recycler_object, parent, false);
        return new LinkDataViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LinkDataViewHolder holder, int position) {
        Log.d("List", "binding data");
        LinkWithAllData linkWithAllData = dataArrayList.get(position);
        AnimeLinkData recyclerData = AnimeLinkData.from(linkWithAllData.linkData);
        //AnimeLinkData recyclerData = dataArrayList.get(position);
        if(recyclerData.getId() == null)    holder.scoreTextView.setVisibility(View.GONE);
        else {
            holder.scoreTextView.setText("\u2605" + recyclerData.getAnimeMetaData(AnimeLinkData.DataContract.DATA_USER_SCORE));
        }
        holder.titleTextView.setText(linkWithAllData.getTitle());
        /*if(recyclerData.getId() != null) {
            String sourceName = recyclerData.getAnimeMetaData(AnimeLinkData.DataContract.DATA_SOURCE);
            holder.titleTextView.setText(recyclerData.getTitle() + (sourceName.equals("") ? "" : " (" + sourceName + ")"));
        }*/
        /*if(recyclerData.getId() == null) {
            holder.deleteButton.setVisibility(View.GONE);
            holder.editButton.setVisibility(View.GONE);
        }*/

        String episodeNumText = linkWithAllData.getMetaData(AnimeLinkData.DataContract.DATA_EPISODE_NUM);
        if(episodeNumText.startsWith("Episode")) {
            episodeNumText = episodeNumText.split("-")[1].trim();
        }

        int watchedEpisodes = Integer.parseInt(episodeNumText);
        int fetchedEpisodes = watchedEpisodes;
        if(linkWithAllData.linkMetaData != null) {
            fetchedEpisodes = Math.max(watchedEpisodes, linkWithAllData.linkMetaData.getLastEpisodeNodesFetchCount());
        }
        int totalEpisode = fetchedEpisodes;
        if(linkWithAllData.alMalMetaData != null && Utils.isNumeric(linkWithAllData.alMalMetaData.getTotalEpisodes())) {
            totalEpisode = Math.max(Integer.parseInt(linkWithAllData.alMalMetaData.getTotalEpisodes()), totalEpisode);
        }

        holder.progressBar.setProgress((int) ((watchedEpisodes*100.0) / totalEpisode));
        holder.progressBar.setSecondaryProgress((int) ((fetchedEpisodes*100.0) / totalEpisode));

        if(totalEpisode == fetchedEpisodes) {
            holder.episodeProgressTextView.setText(watchedEpisodes + " | " + totalEpisode);
        }
        else {
            holder.episodeProgressTextView.setText(watchedEpisodes + " | " + fetchedEpisodes + " | " + totalEpisode);
        }


        String imageUrl = recyclerData.getAnimeMetaData(AnimeLinkData.DataContract.DATA_IMAGE_URL);
        if(imageUrl != null) {
            Glide.with(mcontext)
                    .load(imageUrl)
                    .centerCrop()
                    .transition(new DrawableTransitionOptions().crossFade())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.animeImageView);
            holder.animeImageView.setClipToOutline(true);
        }
        else {
            holder.animeImageView.setImageDrawable(mcontext.getResources().getDrawable(R.drawable.ic_stat_name));
        }
    }

    @Override
    public int getItemCount() {
        return dataArrayList.size();
    }
}
