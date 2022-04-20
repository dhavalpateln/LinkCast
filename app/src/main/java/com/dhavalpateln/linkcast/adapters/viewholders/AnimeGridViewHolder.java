package com.dhavalpateln.linkcast.adapters.viewholders;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.dhavalpateln.linkcast.R;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class AnimeGridViewHolder extends RecyclerView.ViewHolder {
    public TextView subTextTextView;
    public TextView titleTextView;
    public ImageView imageView;
    public TextView scoreTextView;
    public ConstraintLayout mainLayout;

    public AnimeGridViewHolder(@NonNull View itemView) {
        super(itemView);
        this.mainLayout = (ConstraintLayout) itemView;
        this.subTextTextView = itemView.findViewById(R.id.grid_object_subtext_text_view);
        this.titleTextView = itemView.findViewById(R.id.grid_object_title_text_view);
        this.imageView = itemView.findViewById(R.id.grid_object_image_view);
        this.scoreTextView = itemView.findViewById(R.id.mal_score_text_view);
    }
}
