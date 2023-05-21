package com.dhavalpateln.linkcast.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.dhavalpateln.linkcast.R;
import com.dhavalpateln.linkcast.adapters.viewholders.LinkDataGridViewHolder;
import com.dhavalpateln.linkcast.adapters.viewholders.LinkDataViewHolder;
import com.dhavalpateln.linkcast.database.AnimeLinkData;
import com.dhavalpateln.linkcast.database.room.almaldata.AlMalMetaData;
import com.dhavalpateln.linkcast.database.room.animelinkcache.LinkData;
import com.dhavalpateln.linkcast.database.room.animelinkcache.LinkWithAllData;
import com.dhavalpateln.linkcast.database.room.linkmetadata.LinkMetaData;
import com.dhavalpateln.linkcast.utils.Utils;

import java.util.List;

public class LinkDataAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int GRID_VIEW = 0;
    public static final int LIST_VIEW = 1;
    private int type;
    private List<LinkWithAllData> linkDataList;
    private Context context;
    private LinkDataAdapterInterface listener;

    public LinkDataAdapter(Context context, List<LinkWithAllData> linkDataList, LinkDataAdapterInterface listener) {
        this(context, linkDataList, listener, GRID_VIEW);
    }

    public LinkDataAdapter(Context context, List<LinkWithAllData> linkDataList, LinkDataAdapterInterface listener, int type) {
        this.context = context;
        this.linkDataList = linkDataList;
        this.listener = listener;
        this.type = type;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case LIST_VIEW:
                return new LinkDataViewHolder(LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.list_linkdata_recycler_object,
                        parent,
                        false));
            case GRID_VIEW:
            default:
                return new LinkDataGridViewHolder(LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.grid_linkdata_recycler_object,
                        parent,
                        false));
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        LinkWithAllData linkData = this.linkDataList.get(position);
        LinkMetaData linkMetaData = linkData.linkMetaData;
        AlMalMetaData alMalMetaData = linkData.alMalMetaData;
        LinkData linkDataFB = linkData.linkData;
        ImageView animeImageView = null;

        String episodeProgressString = "";

        String episodeNumText = linkData.getMetaData(AnimeLinkData.DataContract.DATA_EPISODE_NUM);
        if (episodeNumText.startsWith("Episode")) {
            episodeNumText = episodeNumText.split("-")[1].trim();
        }

        int watchedEpisodes = Integer.parseInt(episodeNumText);
        int fetchedEpisodes = watchedEpisodes;
        if (linkMetaData != null) {
            fetchedEpisodes = Math.max(watchedEpisodes, linkMetaData.getLastEpisodeNodesFetchCount());
        }
        int totalEpisode = fetchedEpisodes;
        if (alMalMetaData != null && Utils.isNumeric(alMalMetaData.getTotalEpisodes())) {
            totalEpisode = Math.max(Integer.parseInt(alMalMetaData.getTotalEpisodes()), totalEpisode);
        }

        if (totalEpisode == fetchedEpisodes || (linkMetaData != null && linkMetaData.getLastEpisodeNodesFetchCount() == -2)) {
            episodeProgressString = watchedEpisodes + " | " + totalEpisode;
        } else {
            episodeProgressString = watchedEpisodes + " | " + fetchedEpisodes + " | " + totalEpisode;
        }


        if (holder instanceof LinkDataGridViewHolder) {
            LinkDataGridViewHolder viewHolder = (LinkDataGridViewHolder) holder;
            loadImage(viewHolder.animeImageView, linkData.getMetaData(AnimeLinkData.DataContract.DATA_IMAGE_URL));
            if (linkData.getId() == null) {
                viewHolder.scoreTextView.setVisibility(View.GONE);
                viewHolder.episodeProgressTextView.setVisibility(View.GONE);
            } else {
                viewHolder.scoreTextView.setText("\u2605" + linkData.getMetaData(AnimeLinkData.DataContract.DATA_USER_SCORE));
                viewHolder.episodeProgressTextView.setText(episodeProgressString);
            }
            viewHolder.titleTextView.setText(linkData.getTitle());
            animeImageView = viewHolder.animeImageView;
            viewHolder.mainLayout.setOnClickListener(v -> this.listener.onLinkDataClicked(linkData, viewHolder.animeImageView));
            viewHolder.mainLayout.setOnLongClickListener(v -> {
                this.listener.onLinkDataLongClick(linkData);
                return true;
            });
            Utils.startScaleAnimation(viewHolder.mainLayout);
        } else if (holder instanceof LinkDataViewHolder) {
            LinkDataViewHolder viewHolder = (LinkDataViewHolder) holder;
            loadImage(viewHolder.animeImageView, linkData.getMetaData(AnimeLinkData.DataContract.DATA_IMAGE_URL));
            if (linkData.getId() == null) {
                viewHolder.scoreTextView.setVisibility(View.GONE);
                viewHolder.episodeProgressTextView.setVisibility(View.GONE);
                viewHolder.progressBar.setVisibility(View.GONE);
            } else {
                viewHolder.scoreTextView.setText("\u2605" + linkData.getMetaData(AnimeLinkData.DataContract.DATA_USER_SCORE));
                viewHolder.episodeProgressTextView.setText(episodeProgressString);
                viewHolder.progressBar.setProgress((int) ((watchedEpisodes * 100.0) / totalEpisode));
                viewHolder.progressBar.setSecondaryProgress((int) ((fetchedEpisodes * 100.0) / totalEpisode));
            }
            viewHolder.titleTextView.setText(linkData.getTitle());
            animeImageView = viewHolder.animeImageView;
            viewHolder.mainLayout.setOnClickListener(v -> this.listener.onLinkDataClicked(linkData, viewHolder.animeImageView));
            viewHolder.mainLayout.setOnLongClickListener(v -> {
                this.listener.onLinkDataLongClick(linkData);
                return true;
            });
        }

        if (animeImageView != null) {

        }
    }

    private void loadImage(ImageView view, String url) {
        view.setImageDrawable(this.context.getResources().getDrawable(R.drawable.ic_stat_name));
        if (url != null) {
            Glide.with(this.context)
                    .load(url)
                    .transition(new DrawableTransitionOptions().crossFade())
                    .centerCrop()
                    .transform(new RoundedCorners(50))
                    //.placeholder(R.drawable.ic_stat_name)
                    .error(R.drawable.ic_stat_name)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(view);
            view.setClipToOutline(true);
        }
    }

    @Override
    public int getItemCount() {
        return this.linkDataList.size();
    }

    public void updateType(int type) {
        this.type = type;
    }
}
