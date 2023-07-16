package com.dhavalpateln.linkcast.adapters.viewholders;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.dhavalpateln.linkcast.R;

public class LinkDataViewHolder extends RecyclerView.ViewHolder {

    public TextView titleTextView;
    public ImageView animeImageView;
    public TextView scoreTextView;
    public ConstraintLayout mainLayout;
    public ProgressBar progressBar;
    public TextView episodeProgressTextView;

    public LinkDataViewHolder(@NonNull View itemView) {
        super(itemView);
        this.mainLayout = (ConstraintLayout) itemView;
        this.titleTextView = itemView.findViewById(R.id.link_data_title);
        this.animeImageView = itemView.findViewById(R.id.list_object_image_view);
        this.scoreTextView = itemView.findViewById(R.id.anime_score_text_view);
        this.progressBar = itemView.findViewById(R.id.episodeProgressBar);
        this.episodeProgressTextView = itemView.findViewById(R.id.watchedEpisodeTextView);
    }
}
