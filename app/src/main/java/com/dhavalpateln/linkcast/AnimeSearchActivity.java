package com.dhavalpateln.linkcast;

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
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.dhavalpateln.linkcast.AnimeAdvancedView;
import com.dhavalpateln.linkcast.AnimeWebExplorer;
import com.dhavalpateln.linkcast.R;
import com.dhavalpateln.linkcast.animescrappers.AnimeScrapper;
import com.dhavalpateln.linkcast.animesearch.AnimeKisaSearch;
import com.dhavalpateln.linkcast.animesearch.AnimeKisaSiteSearch;
import com.dhavalpateln.linkcast.animesearch.AnimePaheSearch;
import com.dhavalpateln.linkcast.animesearch.AnimeSearch;
import com.dhavalpateln.linkcast.animesearch.AnimixPlaySearch;
import com.dhavalpateln.linkcast.animesearch.BookmarkedSearch;
import com.dhavalpateln.linkcast.animesearch.GogoAnimeSearch;
import com.dhavalpateln.linkcast.animesearch.MangaFourLife;
import com.dhavalpateln.linkcast.animesearch.NineAnimeSearch;
import com.dhavalpateln.linkcast.database.AnimeLinkData;
import com.dhavalpateln.linkcast.database.FirebaseDBHelper;
import com.dhavalpateln.linkcast.dialogs.BookmarkLinkDialog;
import com.dhavalpateln.linkcast.ui.catalog.CatalogObjectFragment;
import com.dhavalpateln.linkcast.ui.catalog.SharedAnimeLinkDataViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AnimeSearchActivity extends AppCompatActivity {

    private RecyclerViewAdapter adapter;
    private RecyclerView recyclerView;
    private EditText searchEditText;
    private Spinner sourceSpinner;

    private Map<String, AnimeSearch> searchers;
    private AnimeSearch animeSearch;
    private String TAG = "AnimeSearch";
    private BookmarkedSearch bookmarkedSearch;
    private ArrayList<AnimeLinkData> filteredData;
    private ExtractSearchResult extractSearchResult;

    private String currentSource = "SAVED";

    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            search(true);
        }
    };

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
            if(recyclerData.getId() != null) {
                String sourceName = recyclerData.getAnimeMetaData(AnimeLinkData.DataContract.DATA_SOURCE);
                holder.episodeNumTextView.setText(recyclerData.getTitle() + (sourceName.equals("") ? "" : " (" + sourceName + ")"));
            }
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
            holder.openButton.setOnLongClickListener(v -> {
                Intent intent = new Intent(getApplicationContext(), AnimeWebExplorer.class);
                for(Map.Entry<String, String> entry: recyclerData.getData().entrySet()) {
                    intent.putExtra("data-" + entry.getKey(), entry.getValue());
                }
                intent.putExtra("url", recyclerData.getUrl());
                intent.putExtra("search", recyclerData.getUrl());
                intent.putExtra("source", "saved");
                intent.putExtra("id", recyclerData.getId());
                intent.putExtra("title", recyclerData.getTitle());
                startActivity(intent);
                return false;
            });
            if(recyclerData.getId() != null) {
                holder.deleteButton.setOnClickListener(v -> {
                    FirebaseDBHelper.removeAnimeLink(recyclerData.getId());
                });
                holder.editButton.setOnClickListener(v -> {
                    // TODO: add more fields to edit
                    BookmarkLinkDialog dialog = new BookmarkLinkDialog(recyclerData.getId(), recyclerData.getTitle(), recyclerData.getUrl(), recyclerData.getData());
                    dialog.show(getSupportFragmentManager(), "bookmarkEdit");
                });
            }
            else {
                holder.deleteButton.setVisibility(View.GONE);
                holder.editButton.setVisibility(View.GONE);
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

        sourceSpinner = findViewById(R.id.sourceSelectorSpinner);
        searchEditText = findViewById(R.id.editTextSearchBar);

        bookmarkedSearch = new BookmarkedSearch();
        filteredData = new ArrayList<>();

        ArrayAdapter<String> sourceSpinnerAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item);
        sourceSpinnerAdapter.add(bookmarkedSearch.getName());
        sourceSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        searchers = new HashMap<>();
        searchers.put("animekisa.tv", new AnimeKisaSearch());
        searchers.put(ProvidersData.ANIMEKISASITE.NAME, new AnimeKisaSiteSearch());
        searchers.put(ProvidersData.GOGOANIME.NAME, new GogoAnimeSearch());
        searchers.put(ProvidersData.NINEANIME.NAME, new NineAnimeSearch());
        searchers.put("animepahe.com", new AnimePaheSearch());
        //searchers.put("animixplay.to", new AnimixPlaySearch());
        searchers.put("manga4life", new MangaFourLife());

        String[] order = new String[] {
                //ProvidersData.ANIMEKISASITE.NAME,
                ProvidersData.GOGOANIME.NAME,
                ProvidersData.NINEANIME.NAME,
                "animepahe.com",
                "animixplay.to",
                "animekisa.tv",
                "manga4life"
        };

        for(String searchSourceName: order) {
            sourceSpinnerAdapter.add(searchSourceName);
        }
        sourceSpinner.setAdapter(sourceSpinnerAdapter);
        searchers.put("Bookmarked", bookmarkedSearch);

        recyclerView = findViewById(R.id.search_recycler_view);
        adapter = new RecyclerViewAdapter(filteredData, getApplicationContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setAdapter(adapter);

        SharedAnimeLinkDataViewModel viewModel = new ViewModelProvider(this).get(SharedAnimeLinkDataViewModel.class);
        viewModel.getData().observe(this, new Observer<Map<String, AnimeLinkData>>() {
            @Override
            public void onChanged(Map<String, AnimeLinkData> stringAnimeLinkDataMap) {
                Log.d(TAG, "Data changed");
                ArrayList<AnimeLinkData> bookmarkedData = new ArrayList<>();
                for(Map.Entry<String, AnimeLinkData> entry: stringAnimeLinkDataMap.entrySet()) {
                    AnimeLinkData animeLinkData = entry.getValue();
                    animeLinkData.setId(entry.getKey());
                    bookmarkedData.add(entry.getValue());
                }
                bookmarkedSearch.updateData(bookmarkedData);
                if(sourceSpinner.getSelectedItem().toString().equals(bookmarkedSearch.getName())) {
                    updateRecyclerData(bookmarkedSearch.search(searchEditText.getText().toString()));
                }
            }
        });


        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                handler.removeCallbacks(runnable);
                /*if(sourceSpinner.getSelectedItem().toString().equals(bookmarkedSearch.getName())) {
                    updateRecyclerData(bookmarkedSearch.search(charSequence.toString()));
                }*/
            }

            @Override
            public void afterTextChanged(Editable editable) {
                handler.postDelayed(runnable, 300);

            }
        });

        sourceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                TextView selectedText = (TextView) adapterView.getChildAt(0);
                if (selectedText != null) {
                    selectedText.setTextColor(Color.WHITE);
                }
                search(true);
                if(!searchers.get(sourceSpinner.getSelectedItem().toString()).hasQuickSearch()) {
                    Toast.makeText(getApplicationContext(), "Use Search button for this source", Toast.LENGTH_LONG).show();
                    filteredData.clear();
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }

    private void updateRecyclerData(ArrayList<AnimeLinkData> animeLinkDataList) {
        filteredData.clear();
        for(AnimeLinkData animeLinkData: animeLinkDataList) {
            filteredData.add(animeLinkData);
        }
        adapter.notifyDataSetChanged();
    }

    private class ExtractSearchResult extends AsyncTask<String, Integer, ArrayList<AnimeLinkData>> {

        @Override
        protected void onPostExecute(ArrayList<AnimeLinkData> animeLinkDataList) {
            super.onPostExecute(animeLinkDataList);
            updateRecyclerData(animeLinkDataList);
        }

        @Override
        protected ArrayList<AnimeLinkData> doInBackground(String... strings) {
            String searchTerm = strings[0].trim();
            String source = strings[1];
            ArrayList<AnimeLinkData> result;
            AnimeSearch animeSearcher = searchers.get(source);
            result = animeSearcher.search(searchTerm);
            return result;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            updateRecyclerData(new ArrayList<>());
        }
    }

    public void search() {
        search(false);
    }

    public void search(boolean skipIfNoQuickSearch) {
        AnimeSearch searcher = searchers.get(sourceSpinner.getSelectedItem().toString());
        if(searcher.hasQuickSearch()) {
            if(extractSearchResult != null) {
                extractSearchResult.cancel(true);
            }
            extractSearchResult = new ExtractSearchResult();
            extractSearchResult.execute(searchEditText.getText().toString(), sourceSpinner.getSelectedItem().toString());
        }
        else if(!skipIfNoQuickSearch){
            Intent intent;
            if(searcher.isMangeSource()) {
                intent = new Intent(AnimeSearchActivity.this, MangaWebExplorer.class);
            }
            else {
                intent = new Intent(AnimeSearchActivity.this, AnimeWebExplorer.class);
            }
            intent.putExtra("source", searcher.getName());
            intent.putExtra("search", searchEditText.getText().toString());
            intent.putExtra("advancedMode", true);
            startActivity(intent);
            finish();
        }
    }

    public void search(View view) {
        search();
    }

}