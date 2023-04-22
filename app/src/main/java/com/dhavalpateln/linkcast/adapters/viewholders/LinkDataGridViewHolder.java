package com.dhavalpateln.linkcast.adapters.viewholders;

import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.dhavalpateln.linkcast.R;

public class LinkDataGridViewHolder extends RecyclerView.ViewHolder {

    public TextView titleTextView;
    public ImageView animeImageView;
    public TextView scoreTextView;
    public ConstraintLayout mainLayout;
    public TextView episodeProgressTextView;

    public LinkDataGridViewHolder(@NonNull View itemView) {
        super(itemView);
        this.mainLayout = (ConstraintLayout) itemView;
        this.titleTextView = itemView.findViewById(R.id.link_data_title);
        this.animeImageView = itemView.findViewById(R.id.grid_object_image_view);
        this.scoreTextView = itemView.findViewById(R.id.anime_score_text_view);
        this.episodeProgressTextView = itemView.findViewById(R.id.watchedEpisodeTextView);
    }
}
