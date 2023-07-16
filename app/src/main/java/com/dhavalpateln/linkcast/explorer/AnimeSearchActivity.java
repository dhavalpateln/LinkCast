package com.dhavalpateln.linkcast.explorer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.dhavalpateln.linkcast.AnimeWebExplorer;
import com.dhavalpateln.linkcast.MangaWebExplorer;
import com.dhavalpateln.linkcast.R;
import com.dhavalpateln.linkcast.adapters.LinkDataAdapter;
import com.dhavalpateln.linkcast.adapters.LinkDataAdapterInterface;
import com.dhavalpateln.linkcast.adapters.LinkDataGridRecyclerAdapter;
import com.dhavalpateln.linkcast.adapters.ListRecyclerAdapter;

import com.dhavalpateln.linkcast.adapters.viewholders.LinkDataGridViewHolder;
import com.dhavalpateln.linkcast.database.AnimeLinkData;
import com.dhavalpateln.linkcast.database.SharedPrefContract;
import com.dhavalpateln.linkcast.database.room.animelinkcache.LinkWithAllData;
import com.dhavalpateln.linkcast.dialogs.LinkDataBottomSheet;
import com.dhavalpateln.linkcast.extractors.AnimeMangaSearch;
import com.dhavalpateln.linkcast.extractors.Providers;
import com.dhavalpateln.linkcast.extractors.bookmark.BookmarkedSearch;
import com.google.android.material.search.SearchBar;
import com.google.android.material.search.SearchView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AnimeSearchActivity extends AppCompatActivity implements LinkDataAdapterInterface {

    public static final String INTENT_SEARCH_TERM = "search";
    public static final String INTENT_CHANGE_SOURCE = "changesource";

    public static final String RESULT_ANIMELINKDATA = "resultanimedata";
    public static final String RESULT_FORCE = "resultforce";
    private static final String INTENT_SOURCE = "initsource";


    private ListRecyclerAdapter<AnimeLinkData> recyclerAdapter;
    private SearchListRecyclerAdapter adapter;
    private RecyclerView recyclerView;
    private AutoCompleteTextView searchEditText;
    private Spinner sourceSpinner;
    private ProgressDialog progressDialog;

    private Map<String, AnimeMangaSearch> searchers;
    private String TAG = "AnimeSearch";
    private BookmarkedSearch bookmarkedSearch;
    private List<LinkWithAllData> filteredData;
    private ExtractSearchResult extractSearchResult;
    private SharedPreferences prefs;

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

    @Override
    public void onLinkDataClicked(LinkWithAllData linkData, ImageView animeImage) {
        if(getIntent().hasExtra(INTENT_CHANGE_SOURCE) || getIntent().hasExtra(INTENT_SOURCE)) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra(RESULT_ANIMELINKDATA, linkData);
            resultIntent.putExtra(RESULT_FORCE, false);
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
            return;
        }
        Intent intent = AdvancedView.prepareIntent(getApplicationContext(), linkData);
        intent.putExtra(AdvancedView.INTENT_MODE_ANIME, linkData.isAnime());
        startActivity(intent);
    }

    @Override
    public void onLinkDataLongClick(LinkWithAllData linkData) {
        if(sourceSpinner.getSelectedItem().toString().equalsIgnoreCase(bookmarkedSearch.getDisplayName())) {
            LinkDataBottomSheet bottomSheet = new LinkDataBottomSheet(linkData, prefs.getString(SharedPrefContract.BOOKMARK_DELETE_CONFIRMATION, "ask"));
            bottomSheet.show(getSupportFragmentManager(), "LDBottomSheet");
        }
    }

    private class SearchListRecyclerAdapter extends LinkDataAdapter {

        public SearchListRecyclerAdapter(Context context, List<LinkWithAllData> linkDataList) {
            super(context, linkDataList, AnimeSearchActivity.this);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anime_search);

        sourceSpinner = findViewById(R.id.sourceSelectorSpinner);
        searchEditText = findViewById(R.id.searchBarText);

        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        progressDialog = new ProgressDialog(AnimeSearchActivity.this);
        progressDialog.setMessage("Please Wait...");
        progressDialog.setCancelable(false);

        if(getIntent().hasExtra(INTENT_SEARCH_TERM)) {
            searchEditText.setText(getIntent().getStringExtra(INTENT_SEARCH_TERM));
        }

        bookmarkedSearch = new BookmarkedSearch(getApplicationContext());
        filteredData = new ArrayList<>();

        ArrayAdapter<String> sourceSpinnerAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item);
        if(!getIntent().hasExtra(INTENT_CHANGE_SOURCE) && !getIntent().hasExtra(INTENT_SOURCE)) sourceSpinnerAdapter.add(bookmarkedSearch.getDisplayName());
        sourceSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        searchers = Providers.getSearchers();
        /*searchers.put(ProvidersData.GOGOANIME.NAME, new GogoAnimeSearch());
        searchers.put(ProvidersData.ANIMEPAHE.NAME, new AnimePaheSearch());
        searchers.put(ProvidersData.ZORO.NAME, new ZoroSearch());
        searchers.put(ProvidersData.MARIN.NAME, new MarinSearch());
        searchers.put(ProvidersData.CRUNCHYROLL.NAME, new CrunchyrollSearch());
        searchers.put("manga4life", new MangaFourLifeSearch());
        searchers.put(ProvidersData.MANGAREADER.NAME, new MangaReaderSearch());*/

        String[] order = Providers.getSearchOrder();

        for(String searchSourceName: order) {
            if(getIntent().hasExtra(INTENT_CHANGE_SOURCE)) {
                if(getIntent().getStringExtra(INTENT_CHANGE_SOURCE).equals("anime") && searchers.get(searchSourceName).isAnimeSource()) {
                    sourceSpinnerAdapter.add(searchSourceName);
                }
                else if(searchers.get(searchSourceName).isMangaSource()) {
                    sourceSpinnerAdapter.add(searchSourceName);
                }
            } else if (getIntent().hasExtra(INTENT_SOURCE)) {
                if(searchSourceName.equalsIgnoreCase(getIntent().getStringExtra(INTENT_SOURCE))) {
                    sourceSpinnerAdapter.add(searchSourceName);
                }
            } else {
                sourceSpinnerAdapter.add(searchSourceName);
            }
        }
        sourceSpinner.setAdapter(sourceSpinnerAdapter);
        searchers.put("Bookmarked", bookmarkedSearch);

        recyclerView = findViewById(R.id.search_recycler_view);
        adapter = new SearchListRecyclerAdapter(getApplicationContext(), filteredData);
        recyclerView.setLayoutManager(new GridLayoutManager(getApplicationContext(), 3));
        //recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setAdapter(adapter);

        /*SharedAnimeLinkDataViewModel viewModel = new ViewModelProvider(this).get(SharedAnimeLinkDataViewModel.class);
        viewModel.getData().observe(this, stringAnimeLinkDataMap -> {
            Log.d(TAG, "Data changed");
            ArrayList<AnimeLinkData> bookmarkedData = new ArrayList<>();
            for(Map.Entry<String, AnimeLinkData> entry: stringAnimeLinkDataMap.entrySet()) {
                AnimeLinkData animeLinkData = entry.getValue();
                animeLinkData.setId(entry.getKey());
                bookmarkedData.add(entry.getValue());
            }
            bookmarkedSearch.updateData(bookmarkedData);

        });*/


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
                AnimeMangaSearch searcher = searchers.get(sourceSpinner.getSelectedItem().toString());
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

    private void updateRecyclerData(List<LinkWithAllData> animeLinkDataList) {
        filteredData.clear();
        for(LinkWithAllData animeLinkData: animeLinkDataList) {
            filteredData.add(animeLinkData);
        }
        adapter.notifyDataSetChanged();
    }

    private class ExtractSearchResult extends AsyncTask<String, Integer, List<LinkWithAllData>> {

        @Override
        protected void onPostExecute(List<LinkWithAllData> animeLinkDataList) {
            super.onPostExecute(animeLinkDataList);
            updateRecyclerData(animeLinkDataList);
        }

        @Override
        protected List<LinkWithAllData> doInBackground(String... strings) {
            String searchTerm = strings[0].trim();
            String source = strings[1];
            List<LinkWithAllData> result;
            AnimeMangaSearch animeSearcher = searchers.get(source);
            result = animeSearcher.searchLink(searchTerm);
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
        AnimeMangaSearch searcher = searchers.get(sourceSpinner.getSelectedItem().toString());
        if(searcher.hasQuickSearch()) {
            if(extractSearchResult != null) {
                extractSearchResult.cancel(true);
            }
            extractSearchResult = new ExtractSearchResult();
            extractSearchResult.execute(searchEditText.getText().toString(), sourceSpinner.getSelectedItem().toString());
        }
        else if(!skipIfNoQuickSearch){
            Intent intent;
            if(searcher.isMangaSource()) {
                intent = new Intent(AnimeSearchActivity.this, MangaWebExplorer.class);
            }
            else {
                intent = new Intent(AnimeSearchActivity.this, AnimeWebExplorer.class);
            }
            intent.putExtra("source", searcher.getDisplayName());
            intent.putExtra("search", searchEditText.getText().toString());
            intent.putExtra("advancedMode", true);
            startActivity(intent);
            finish();
        }
    }

    public void search(View view) {
        search();
    }

    public static Intent prepareChangeSourceIntent(Context context, AnimeLinkData animeLinkData, boolean isAnime) {
        Intent intent = new Intent(context, AnimeSearchActivity.class);
        intent.putExtra(INTENT_CHANGE_SOURCE, isAnime ? "anime" : "manga");
        intent.putExtra(INTENT_SEARCH_TERM, animeLinkData.getTitle().replaceAll("\\(.*\\)", "").trim());
        return intent;
    }

    public static Intent prepareSearchIntent(Context context, String initSearch, String source) {
        Intent intent = new Intent(context, AnimeSearchActivity.class);
        intent.putExtra(INTENT_SOURCE, source);
        intent.putExtra(INTENT_SEARCH_TERM, initSearch);
        return intent;
    }

}