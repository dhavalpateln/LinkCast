package com.dhavalpateln.linkcast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.dhavalpateln.linkcast.adapters.AnimeDataListRecyclerAdapter;
import com.dhavalpateln.linkcast.adapters.ListRecyclerAdapter;
import com.dhavalpateln.linkcast.adapters.viewholders.AnimeListViewHolder;
import com.dhavalpateln.linkcast.animesearch.AnimeKisaSearch;
import com.dhavalpateln.linkcast.animesearch.AnimeKisaSiteSearch;
import com.dhavalpateln.linkcast.animesearch.AnimePaheSearch;
import com.dhavalpateln.linkcast.animesearch.AnimeSearch;
import com.dhavalpateln.linkcast.animesearch.BookmarkedSearch;
import com.dhavalpateln.linkcast.animesearch.GogoAnimeSearch;
import com.dhavalpateln.linkcast.animesearch.MangaFourLifeSearch;
import com.dhavalpateln.linkcast.animesearch.NineAnimeSearch;
import com.dhavalpateln.linkcast.database.AnimeLinkData;
import com.dhavalpateln.linkcast.database.FirebaseDBHelper;
import com.dhavalpateln.linkcast.dialogs.BookmarkLinkDialog;
import com.dhavalpateln.linkcast.ui.animes.SharedAnimeLinkDataViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AnimeSearchActivity extends AppCompatActivity {

    public static String INTENT_SEARCH_TERM = "search";

    private ListRecyclerAdapter<AnimeLinkData> recyclerAdapter;
    private SearchListRecyclerAdapter adapter;
    private RecyclerView recyclerView;
    private EditText searchEditText;
    private Spinner sourceSpinner;
    private ProgressDialog progressDialog;

    private Map<String, AnimeSearch> searchers;
    private AnimeSearch animeSearch;
    private String TAG = "AnimeSearch";
    private BookmarkedSearch bookmarkedSearch;
    private ArrayList<AnimeLinkData> filteredData;
    private ExtractSearchResult extractSearchResult;

    private String currentSource = "SAVED";

    private Executor executor = Executors.newSingleThreadExecutor();
    private Handler uiHandler = new Handler(Looper.getMainLooper());

    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            search(true);
        }
    };

    private class SearchListRecyclerAdapter extends AnimeDataListRecyclerAdapter {

        public SearchListRecyclerAdapter(List<AnimeLinkData> recyclerDataArrayList, Context mcontext) {
            super(recyclerDataArrayList, mcontext);
        }

        @Override
        public void onBindViewHolder(@NonNull AnimeListViewHolder holder, int position) {
            super.onBindViewHolder(holder, position);
            AnimeLinkData recyclerData = this.dataArrayList.get(position);
            holder.openButton.setOnClickListener(v -> {
                AnimeSearch animeSearch = searchers.get(sourceSpinner.getSelectedItem().toString());
                Intent intent;
                if(animeSearch.isAdvanceModeSource()) {
                    intent = AdvancedView.prepareIntent(getApplicationContext(), recyclerData);
                    intent.putExtra(AdvancedView.INTENT_MODE_ANIME, !animeSearch.isMangeSource());
                }
                else if(animeSearch.isMangeSource()){
                    intent = new Intent(getApplicationContext(), MangaWebExplorer.class);
                }
                else {
                    intent = AnimeWebExplorer.prepareIntent(getApplicationContext(), recyclerData);
                }
                startActivity(intent);
            });
            if(recyclerData.getId() != null) {
                holder.deleteButton.setVisibility(View.VISIBLE);
                holder.editButton.setVisibility(View.VISIBLE);
                holder.deleteButton.setOnClickListener(v -> {
                    FirebaseDBHelper.removeAnimeLink(recyclerData.getId());
                });
                holder.editButton.setOnClickListener(v -> {
                    // TODO: add more fields to edit
                    BookmarkLinkDialog dialog = new BookmarkLinkDialog(recyclerData.getId(), recyclerData.getTitle(), recyclerData.getUrl(), recyclerData.getData());
                    dialog.show(getSupportFragmentManager(), "bookmarkEdit");
                });
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anime_search);

        sourceSpinner = findViewById(R.id.sourceSelectorSpinner);
        searchEditText = findViewById(R.id.editTextSearchBar);

        progressDialog = new ProgressDialog(AnimeSearchActivity.this);
        progressDialog.setMessage("Please Wait...");
        progressDialog.setCancelable(false);

        if(getIntent().hasExtra(INTENT_SEARCH_TERM)) {
            searchEditText.setText(getIntent().getStringExtra(INTENT_SEARCH_TERM));
        }

        bookmarkedSearch = new BookmarkedSearch();
        filteredData = new ArrayList<>();

        ArrayAdapter<String> sourceSpinnerAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item);
        sourceSpinnerAdapter.add(bookmarkedSearch.getName());
        sourceSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        searchers = new HashMap<>();
        searchers.put("animekisa.tv", new AnimeKisaSearch());
        searchers.put(ProvidersData.ANIMEKISASITE.NAME, new AnimeKisaSiteSearch());
        searchers.put(ProvidersData.GOGOANIME.NAME, new GogoAnimeSearch());
        searchers.put(ProvidersData.NINEANIME.NAME, new NineAnimeSearch(getApplicationContext()));
        searchers.put("animepahe.com", new AnimePaheSearch());
        //searchers.put("animixplay.to", new AnimixPlaySearch());
        searchers.put("manga4life", new MangaFourLifeSearch());

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
        adapter = new SearchListRecyclerAdapter(filteredData, getApplicationContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setAdapter(adapter);

        SharedAnimeLinkDataViewModel viewModel = new ViewModelProvider(this).get(SharedAnimeLinkDataViewModel.class);
        viewModel.getData().observe(this, stringAnimeLinkDataMap -> {
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
                AnimeSearch searcher = searchers.get(sourceSpinner.getSelectedItem().toString());
                if(searcher.requiresInit()) {
                    executor.execute(() -> {
                        uiHandler.post(() -> progressDialog.show());
                        searcher.init();
                        uiHandler.post(() -> {
                            progressDialog.dismiss();
                            search(true);
                        });
                    });
                }
                else {
                    search(true);
                }
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