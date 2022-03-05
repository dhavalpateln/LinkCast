package com.dhavalpateln.linkcast.animesearch;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.dhavalpateln.linkcast.AnimeAdvancedView;
import com.dhavalpateln.linkcast.AnimeWebExplorer;
import com.dhavalpateln.linkcast.R;
import com.dhavalpateln.linkcast.animescrappers.AnimeScrapper;
import com.dhavalpateln.linkcast.database.AnimeLinkData;
import com.dhavalpateln.linkcast.database.FirebaseDBHelper;
import com.dhavalpateln.linkcast.dialogs.BookmarkLinkDialog;
import com.dhavalpateln.linkcast.ui.catalog.CatalogObjectFragment;
import com.dhavalpateln.linkcast.ui.catalog.SharedAnimeLinkDataViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AnimeSearchActivity extends AppCompatActivity {

    private Map<String, AnimeSearch> searchers;
    private AnimeSearch animeSearch;
    private String TAG = "AnimeSearch";
    private ArrayList<AnimeLinkData> data;
    private ArrayList<AnimeLinkData> filteredData;
    private RecyclerViewAdapter adapter;
    private RecyclerView recyclerView;
    private EditText searchEditText;

    public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.RecyclerViewHolder> {

        private ArrayList<AnimeLinkData> episodeDataArrayList;
        private Context mcontext;

        public RecyclerViewAdapter(ArrayList<AnimeLinkData> recyclerDataArrayList, Context mcontext) {
            this.episodeDataArrayList = recyclerDataArrayList;
            this.mcontext = mcontext;
        }

        @NonNull
        @Override
        public RecyclerViewAdapter.RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // Inflate Layout
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.catalog_recycler_object, parent, false);
            return new RecyclerViewAdapter.RecyclerViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerViewAdapter.RecyclerViewHolder holder, int position) {
            // Set the data to textview and imageview.
            AnimeLinkData recyclerData = episodeDataArrayList.get(position);
            holder.episodeNumTextView.setText(recyclerData.getTitle());
            holder.openButton.setOnClickListener(v -> {
                Intent intent = new Intent(getApplicationContext(), AnimeWebExplorer.class);
                if(recyclerData.getData() != null) {
                    if(recyclerData.getData().containsKey("mode") && recyclerData.getData().get("mode").equals("advanced")) {
                        intent = new Intent(getApplicationContext(), AnimeAdvancedView.class);
                        intent.putExtra("url", recyclerData.getUrl());
                    }
                    intent.putExtra("mapdata", (HashMap<String, String>) recyclerData.getData());
                    for(Map.Entry<String, String> entry: recyclerData.getData().entrySet()) {
                        intent.putExtra("data-" + entry.getKey(), entry.getValue());
                    }
                }
                intent.putExtra("search", recyclerData.getUrl());
                intent.putExtra("source", "saved");
                intent.putExtra("id", recyclerData.getId());
                intent.putExtra("title", recyclerData.getTitle());
                startActivity(intent);
            });
            holder.deleteButton.setOnClickListener(v -> {
                FirebaseDBHelper.removeAnimeLink(recyclerData.getId());
            });
            holder.editButton.setOnClickListener(v -> {
                // TODO: add more fields to edit
                BookmarkLinkDialog dialog = new BookmarkLinkDialog(recyclerData.getId(), recyclerData.getTitle(), recyclerData.getUrl(), recyclerData.getData());
                dialog.show(getSupportFragmentManager(), "bookmarkEdit");
            });
            if(recyclerData.getTitle().contains("Chuuni") || recyclerData.getTitle().contains("Boku no Hero Academia 3")) {
                Log.d(TAG, "fk");

            }
            if(recyclerData.getData().containsKey("imageUrl")) {
                Glide.with(getApplicationContext())
                        .load(recyclerData.getData().get("imageUrl"))
                        .centerCrop()
                        .crossFade()
                        //.bitmapTransform(new CropCircleTransformation(getApplicationContext()))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(holder.animeImageView);
                holder.animeImageView.setClipToOutline(true);
            }
            else {
                holder.animeImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_stat_name));
            }
        }

        @Override
        public int getItemCount() {
            // this method returns the size of recyclerview
            return episodeDataArrayList.size();
        }

        // View Holder Class to handle Recycler View.
        public class RecyclerViewHolder extends RecyclerView.ViewHolder {

            private TextView episodeNumTextView;
            private ImageView animeImageView;
            private Button openButton;
            private Button deleteButton;
            private Button editButton;
            private ConstraintLayout mainLayout;

            public RecyclerViewHolder(@NonNull View itemView) {
                super(itemView);
                this.mainLayout = (ConstraintLayout) itemView;
                this.episodeNumTextView = itemView.findViewById(R.id.catalog_recycler_object_text_view);
                this.animeImageView = itemView.findViewById(R.id.anime_image_view);
                this.openButton = itemView.findViewById(R.id.open_button_catalog_recycler);
                this.deleteButton = itemView.findViewById(R.id.delete_button_catalog_recycler);
                this.editButton = itemView.findViewById(R.id.edit_button_catalog_recycler);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anime_search);

        data = new ArrayList<>();
        filteredData = new ArrayList<>();

        searchers = new HashMap<>();
        searchers.put("animepahe.com", new AnimePaheSearch());

        String source = getIntent().getStringExtra("source");

        recyclerView = findViewById(R.id.search_recycler_view);
        adapter = new RecyclerViewAdapter(filteredData, getApplicationContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setAdapter(adapter);

        searchEditText = findViewById(R.id.editTextSearchBar);

        if(searchers.containsKey(source)) {
            animeSearch = searchers.get(source);
        }
        else if(source.equals("SAVED")) {
            SharedAnimeLinkDataViewModel viewModel = new ViewModelProvider(this).get(SharedAnimeLinkDataViewModel.class);
            viewModel.getData().observe(this, new Observer<Map<String, AnimeLinkData>>() {
                @Override
                public void onChanged(Map<String, AnimeLinkData> stringAnimeLinkDataMap) {
                    Log.d(TAG, "Data changed");
                    data.clear();
                    filteredData.clear();
                    for(Map.Entry<String, AnimeLinkData> entry: stringAnimeLinkDataMap.entrySet()) {
                        AnimeLinkData animeLinkData = entry.getValue();
                        animeLinkData.setId(entry.getKey());
                        data.add(entry.getValue());
                        filteredData.add(entry.getValue());
                    }
                    adapter.notifyDataSetChanged();
                }
            });



        }
        else {
            finish();
        }

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                filteredData.clear();
                for(AnimeLinkData animeLinkData: data) {
                    if(animeLinkData.getTitle().toLowerCase().contains(charSequence)) {
                        filteredData.add(animeLinkData);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

    }

    private class ExtractSearchResult extends AsyncTask<String, Integer, ArrayList<AnimeLinkData>> {

        @Override
        protected void onPostExecute(ArrayList<AnimeLinkData> animeLinkDataList) {
            super.onPostExecute(animeLinkDataList);
            filteredData.clear();
            for(AnimeLinkData animeLinkData: animeLinkDataList) {
                filteredData.add(animeLinkData);
            }
            adapter.notifyDataSetChanged();
        }

        @Override
        protected ArrayList<AnimeLinkData> doInBackground(String... strings) {
            ArrayList<AnimeLinkData> filteredData = new ArrayList<>();
            String searchTerm = strings[0];
            String source = strings[1];



            return null;
        }
    }

}