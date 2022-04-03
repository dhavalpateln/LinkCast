package com.dhavalpateln.linkcast.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.dhavalpateln.linkcast.AnimeAdvancedView;
import com.dhavalpateln.linkcast.AnimeWebExplorer;
import com.dhavalpateln.linkcast.R;
import com.dhavalpateln.linkcast.database.AnimeLinkData;
import com.dhavalpateln.linkcast.database.FirebaseDBHelper;
import com.dhavalpateln.linkcast.dialogs.BookmarkLinkDialog;
import com.dhavalpateln.linkcast.ui.catalog.CatalogObjectFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class ListRecyclerAdapter<T> extends RecyclerView.Adapter<ListRecyclerAdapter.ListRecyclerViewHolder> {

    public interface RecyclerInterface {
        void onBindView(ListRecyclerAdapter.ListRecyclerViewHolder holder, int position);
    }

    private ArrayList<T> episodeDataArrayList;
    private Context mcontext;
    private RecyclerInterface recyclerInterface;

    public ListRecyclerAdapter(ArrayList<T> recyclerDataArrayList, Context mcontext, RecyclerInterface recyclerInterface) {
        this.episodeDataArrayList = recyclerDataArrayList;
        this.mcontext = mcontext;
        this.recyclerInterface = recyclerInterface;
    }

    @NonNull
    @Override
    public ListRecyclerAdapter.ListRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate Layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.catalog_recycler_object, parent, false);
        return new ListRecyclerAdapter.ListRecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListRecyclerAdapter.ListRecyclerViewHolder holder, int position) {
        // Set the data to textview and imageview.
        this.recyclerInterface.onBindView(holder, position);
    }

    @Override
    public int getItemCount() {
        // this method returns the size of recyclerview
        return episodeDataArrayList.size();
    }

    // View Holder Class to handle Recycler View.
    public class ListRecyclerViewHolder extends RecyclerView.ViewHolder {

        private TextView episodeNumTextView;
        private TextView sourceTextView;
        private ImageView animeImageView;
        private Button openButton;
        private Button deleteButton;
        private Button editButton;
        private ConstraintLayout mainLayout;

        public ListRecyclerViewHolder(@NonNull View itemView) {
            super(itemView);
            this.mainLayout = (ConstraintLayout) itemView;
            this.episodeNumTextView = itemView.findViewById(R.id.catalog_recycler_object_text_view);
            this.sourceTextView = itemView.findViewById(R.id.catalog_recycler_object_source_text_view);
            this.animeImageView = itemView.findViewById(R.id.anime_image_view);
            this.openButton = itemView.findViewById(R.id.open_button_catalog_recycler);
            this.deleteButton = itemView.findViewById(R.id.delete_button_catalog_recycler);
            this.editButton = itemView.findViewById(R.id.edit_button_catalog_recycler);
        }
    }
}