package com.dhavalpateln.linkcast.adapters.viewholders;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dhavalpateln.linkcast.R;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class AnimeListViewHolder extends RecyclerView.ViewHolder {
    public TextView titleTextView;
    public TextView subTextView;
    public ImageView animeImageView;
    public Button openButton;
    public Button deleteButton;
    public Button editButton;
    public TextView scoreTextView;
    public LinearLayout buttonHolder;
    public ConstraintLayout mainLayout;

    public AnimeListViewHolder(@NonNull View itemView) {
        super(itemView);
        this.mainLayout = (ConstraintLayout) itemView;
        this.subTextView = itemView.findViewById(R.id.list_object_subtext_text_view);
        this.titleTextView = itemView.findViewById(R.id.list_object_title_text_view);
        this.scoreTextView = itemView.findViewById(R.id.anime_score_text_view);
        this.animeImageView = itemView.findViewById(R.id.list_object_image_view);
        this.openButton = itemView.findViewById(R.id.open_button_catalog_recycler);
        this.deleteButton = itemView.findViewById(R.id.delete_button_catalog_recycler);
        this.editButton = itemView.findViewById(R.id.edit_button_catalog_recycler);
        this.buttonHolder = itemView.findViewById(R.id.list_object_buttons_holder_linearlayout);
    }
}
