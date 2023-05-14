package com.dhavalpateln.linkcast.explorer.adapters;

import android.content.Context;
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
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.dhavalpateln.linkcast.R;
import com.dhavalpateln.linkcast.database.EpisodeNode;
import com.dhavalpateln.linkcast.database.kitsu.KitsuEpisodeData;
import com.dhavalpateln.linkcast.utils.Utils;

import java.util.List;
import java.util.Map;

public class EpisodeNodeRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private List<EpisodeNode> data;
    private int type;
    private EpisodeNodeSelectionListener listener;
    private int currentIndex = -1;
    private Map<String, KitsuEpisodeData> kitsuData;
    private Context mContext;

    public static final int GRID = 0;
    public static final int LIST = 1;

    private boolean skipSingleAnimate = false;
    private int lastBoundView = 0;

    public EpisodeNodeRecyclerAdapter(Context context, List<EpisodeNode> data, EpisodeNodeSelectionListener listener) {
        this.data = data;
        this.type = GRID;
        this.mContext = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case LIST:
                return new EpisodeNodeListViewHolder(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.item_episode_node_list_object, parent, false)
                );
            case GRID:
            default:
                return new EpisodeRecyclerViewHolder(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.item_episode_node_grid_object, parent, false)
                );
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        // Set the data to textview and imageview.
        this.lastBoundView = holder.getAbsoluteAdapterPosition();
        EpisodeNode node = data.get(position);
        View clickListenerView = null;
        if(holder instanceof EpisodeRecyclerViewHolder) {
            EpisodeRecyclerViewHolder viewHolder = (EpisodeRecyclerViewHolder) holder;
            if(!skipSingleAnimate) Utils.startScaleAnimation(viewHolder.mainLayout);
            viewHolder.episodeNumTextView.setText(node.getEpisodeNumString());
            viewHolder.noteIndicator.setVisibility(node.getNote() != null ? View.VISIBLE : View.GONE);
            viewHolder.selectedIndicator.setVisibility(currentIndex == viewHolder.getBindingAdapterPosition() ? View.VISIBLE : View.GONE);

            clickListenerView = viewHolder.mainLayout;
        }
        else if(holder instanceof EpisodeNodeListViewHolder) {
            EpisodeNodeListViewHolder viewHolder = (EpisodeNodeListViewHolder) holder;
            clickListenerView = viewHolder.mainLayout;
            if(!skipSingleAnimate) Utils.startScaleAnimation(viewHolder.mainLayout);

            viewHolder.episodeNumTextView.setText(node.getEpisodeNumString());
            if(node.getTitle() == null) {
                viewHolder.episodeNodeTitle.setText("Episode - " + node.getEpisodeNumString());
            }
            else {
                viewHolder.episodeNodeTitle.setText(node.getTitle());
            }
            //viewHolder.episodeNodeTitle.setText(node.getTitle() == null ? "Episode - " + node.getEpisodeNumString() : node.getTitle());
            if(node.getDescription() == null || node.getDescription().equals("")) {
                viewHolder.episodeNodeDescription.setText("");
            }
            else {
                viewHolder.episodeNodeDescription.setText(node.getDescription());
            }
            if(node.getThumbnail() != null) {
                loadImage(viewHolder.episodeNodeImage, node.getThumbnail());
            }

            if(node.isFiller()) viewHolder.fillerTag.setVisibility(View.VISIBLE);
            else viewHolder.fillerTag.setVisibility(View.GONE);

        }

        int finalPosition = position;
        if(clickListenerView != null) {
            clickListenerView.setOnClickListener(v -> {
                if(holder instanceof EpisodeRecyclerViewHolder) {

                }
                currentIndex = finalPosition;
                this.listener.onEpisodeSelected(node, finalPosition);
            });
            clickListenerView.setOnLongClickListener(view -> {
                this.listener.onEpisodeLongPressed(node, finalPosition);
                return true;
            });
        }

        skipSingleAnimate = false;
    }



    private void loadImage(ImageView view, String url) {
        if (url != null) {
            Glide.with(this.mContext)
                    .load(url)
                    .centerCrop()
                    .transition(new DrawableTransitionOptions().crossFade())
                    .error(R.drawable.ic_stat_name)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(view);
            view.setClipToOutline(true);
        }
    }

    @Override
    public int getItemCount() {
        // this method returns the size of recyclerview
        return data.size();
    }

    @Override
    public int getItemViewType(int position) {
        return this.type;
    }

    public int getLastBoundView() {
        return this.lastBoundView;
    }

    public void updateType(int type) {
        this.type = type;
    }

    public void setSkipSingleAnimate(boolean value) { this.skipSingleAnimate = value; }

    public class EpisodeRecyclerViewHolder extends RecyclerView.ViewHolder {

        public TextView episodeNumTextView;
        public ConstraintLayout mainLayout;
        public View selectedIndicator;
        public View noteIndicator;

        public EpisodeRecyclerViewHolder(@NonNull View itemView) {
            super(itemView);
            this.mainLayout = (ConstraintLayout) itemView;
            this.episodeNumTextView = itemView.findViewById(R.id.advanced_view_episode_num);
            this.noteIndicator = itemView.findViewById(R.id.episode_note_indicator_image_view);
            this.selectedIndicator = itemView.findViewById(R.id.episode_note_current_indicator);
        }
    }

    public class EpisodeNodeListViewHolder extends RecyclerView.ViewHolder {

        public TextView episodeNodeTitle;
        public TextView episodeNodeDescription;
        public TextView episodeNumTextView;
        public ConstraintLayout mainLayout;
        public ImageView episodeNodeImage;
        public TextView fillerTag;

        public EpisodeNodeListViewHolder(@NonNull View itemView) {
            super(itemView);
            this.mainLayout = (ConstraintLayout) itemView;
            this.episodeNodeTitle = itemView.findViewById(R.id.episode_node_title_text_view);
            this.episodeNodeDescription = itemView.findViewById(R.id.episode_node_description_text_view);
            this.episodeNodeImage = itemView.findViewById(R.id.episode_node_image_view);
            this.episodeNumTextView = itemView.findViewById(R.id.episode_node_episode_num);
            this.fillerTag = itemView.findViewById(R.id.episode_node_filler_tag);
        }
    }



}
