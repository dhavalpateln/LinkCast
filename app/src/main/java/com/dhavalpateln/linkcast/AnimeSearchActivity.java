package com.dhavalpateln.linkcast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
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
import com.dhavalpateln.linkcast.animesearch.ZoroSearch;
import com.dhavalpateln.linkcast.data.StoredAnimeLinkData;
import com.dhavalpateln.linkcast.database.AnimeLinkData;
import com.dhavalpateln.linkcast.database.FirebaseDBHelper;
import com.dhavalpateln.linkcast.database.SharedPrefContract;
import com.dhavalpateln.linkcast.dialogs.BookmarkLinkDialog;
import com.dhavalpateln.linkcast.dialogs.ConfirmationDialog;
import com.dhavalpateln.linkcast.myanimelist.MyAnimelistAnimeData;
import com.dhavalpateln.linkcast.myanimelist.MyAnimelistSearch;
import com.dhavalpateln.linkcast.ui.animes.SharedAnimeLinkDataViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AnimeSearchActivity extends AppCompatActivity {

    public static final String INTENT_SEARCH_TERM = "search";
    public static final String INTENT_CHANGE_SOURCE = "changesource";

    public static final String RESULT_ANIMELINKDATA = "resultanimedata";


    private ListRecyclerAdapter<AnimeLinkData> recyclerAdapter;
    private SearchListRecyclerAdapter adapter;
    private RecyclerView recyclerView;
    private EditText searchEditText;
    private Spinner sourceSpinner;
    private ProgressDialog progressDialog;

    private Map<String, AnimeSearch> searchers;
    private String TAG = "AnimeSearch";
    private BookmarkedSearch bookmarkedSearch;
    private ArrayList<AnimeLinkData> filteredData;
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

    private class SearchListRecyclerAdapter extends AnimeDataListRecyclerAdapter {

        public SearchListRecyclerAdapter(List<AnimeLinkData> recyclerDataArrayList, Context mcontext) {
            super(recyclerDataArrayList, mcontext);
        }

        @Override
        public void onBindViewHolder(@NonNull AnimeListViewHolder holder, int position) {
            super.onBindViewHolder(holder, position);
            AnimeLinkData recyclerData = this.dataArrayList.get(position);
            AnimeSearch animeSearch = searchers.get(sourceSpinner.getSelectedItem().toString());
            holder.openButton.setOnClickListener(v -> {
                AnimeLinkData correctData = recyclerData;
                if(animeSearch instanceof BookmarkedSearch) {
                    boolean isMangaSource = correctData.getAnimeMetaData(AnimeLinkData.DataContract.DATA_SOURCE).equals(ProvidersData.MANGAFOURLIFE.NAME) ||
                            !correctData.getAnimeMetaData(AnimeLinkData.DataContract.DATA_LINK_TYPE).equals("Anime") ||
                            animeSearch.isMangeSource();
                    if(isMangaSource) correctData = StoredAnimeLinkData.getInstance().getMangaCache().get(recyclerData.getId());
                    else correctData = StoredAnimeLinkData.getInstance().getAnimeCache().get(recyclerData.getId());
                }
                Intent intent;
                if(getIntent().hasExtra(INTENT_CHANGE_SOURCE)) {
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra(RESULT_ANIMELINKDATA, correctData);
                    setResult(Activity.RESULT_OK, resultIntent);
                    finish();
                    return;
                }
                else if(animeSearch.isAdvanceModeSource()) {
                    intent = AdvancedView.prepareIntent(getApplicationContext(), correctData);
                    boolean isMangaSource = correctData.getAnimeMetaData(AnimeLinkData.DataContract.DATA_SOURCE).equals(ProvidersData.MANGAFOURLIFE.NAME) ||
                            !correctData.getAnimeMetaData(AnimeLinkData.DataContract.DATA_LINK_TYPE).equals("Anime") ||
                            animeSearch.isMangeSource();
                    intent.putExtra(AdvancedView.INTENT_MODE_ANIME, !isMangaSource);
                }
                else if(animeSearch.isMangeSource()){
                    intent = new Intent(getApplicationContext(), MangaWebExplorer.class);
                }
                else {
                    intent = AnimeWebExplorer.prepareIntent(getApplicationContext(), correctData);
                }
                startActivity(intent);
            });
            if(recyclerData.getId() != null) {
                holder.deleteButton.setVisibility(View.VISIBLE);
                holder.editButton.setVisibility(View.GONE);
                holder.deleteButton.setOnClickListener(v -> {

                    boolean isMangaSource = recyclerData.getAnimeMetaData(AnimeLinkData.DataContract.DATA_SOURCE).equals(ProvidersData.MANGAFOURLIFE.NAME) ||
                            !recyclerData.getAnimeMetaData(AnimeLinkData.DataContract.DATA_LINK_TYPE).equals("Anime") ||
                            animeSearch.isMangeSource();

                    if(prefs.getString(SharedPrefContract.BOOKMARK_DELETE_CONFIRMATION, "ask").equalsIgnoreCase("ask")) {
                        ConfirmationDialog confirmationDialog = new ConfirmationDialog("Are you sure you want to delete this?", () -> {
                            if(isMangaSource) {
                                FirebaseDBHelper.removeMangaLink(recyclerData.getId());
                                StoredAnimeLinkData.getInstance().getMangaCache().remove(recyclerData.getId());
                            }
                            else {
                                FirebaseDBHelper.removeAnimeLink(recyclerData.getId());
                                StoredAnimeLinkData.getInstance().getAnimeCache().remove(recyclerData.getId());
                            }
                        });
                        confirmationDialog.show(getSupportFragmentManager(), "Confirm");
                    }
                    else {
                        if(isMangaSource) {
                            FirebaseDBHelper.removeMangaLink(recyclerData.getId());
                            StoredAnimeLinkData.getInstance().getMangaCache().remove(recyclerData.getId());
                        }
                        else {
                            FirebaseDBHelper.removeAnimeLink(recyclerData.getId());
                            StoredAnimeLinkData.getInstance().getAnimeCache().remove(recyclerData.getId());
                        }
                    }


                    this.dataArrayList.remove(position);
                    adapter.notifyDataSetChanged();
                });
                /*holder.editButton.setOnClickListener(v -> {
                    // TODO: add more fields to edit
                    BookmarkLinkDialog dialog = new BookmarkLinkDialog(recyclerData.getId(), recyclerData.getTitle(), recyclerData.getUrl(), recyclerData.getData());
                    dialog.show(getSupportFragmentManager(), "bookmarkEdit");
                });*/
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anime_search);

        sourceSpinner = findViewById(R.id.sourceSelectorSpinner);
        searchEditText = findViewById(R.id.editTextSearchBar);

        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        progressDialog = new ProgressDialog(AnimeSearchActivity.this);
        progressDialog.setMessage("Please Wait...");
        progressDialog.setCancelable(false);

        if(getIntent().hasExtra(INTENT_SEARCH_TERM)) {
            searchEditText.setText(getIntent().getStringExtra(INTENT_SEARCH_TERM));
        }

        bookmarkedSearch = new BookmarkedSearch();
        filteredData = new ArrayList<>();

        ArrayAdapter<String> sourceSpinnerAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item);
        if(!getIntent().hasExtra(INTENT_CHANGE_SOURCE)) sourceSpinnerAdapter.add(bookmarkedSearch.getName());
        sourceSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        searchers = new HashMap<>();
        //searchers.put("animekisa.tv", new AnimeKisaSearch());
        //searchers.put(ProvidersData.ANIMEKISASITE.NAME, new AnimeKisaSiteSearch());
        searchers.put(ProvidersData.GOGOANIME.NAME, new GogoAnimeSearch());
        searchers.put(ProvidersData.NINEANIME.NAME, new NineAnimeSearch(getApplicationContext()));
        searchers.put(ProvidersData.ANIMEPAHE.NAME, new AnimePaheSearch());
        searchers.put(ProvidersData.ZORO.NAME, new ZoroSearch());
        //searchers.put("animixplay.to", new AnimixPlaySearch());
        searchers.put("manga4life", new MangaFourLifeSearch());

        String[] order = new String[] {
                //ProvidersData.ANIMEKISASITE.NAME,
                ProvidersData.GOGOANIME.NAME,
                ProvidersData.ZORO.NAME,
                ProvidersData.NINEANIME.NAME,
                ProvidersData.ANIMEPAHE.NAME,
                ProvidersData.MANGAFOURLIFE.NAME
        };
        if(getIntent().hasExtra(INTENT_CHANGE_SOURCE)) {
            if(getIntent().getStringExtra(INTENT_CHANGE_SOURCE).equals("anime")) {
                order = new String[] {
                        ProvidersData.GOGOANIME.NAME,
                        ProvidersData.ZORO.NAME,
                        ProvidersData.NINEANIME.NAME,
                        ProvidersData.ANIMEPAHE.NAME
                };
            }
            else {
                order = new String[] {ProvidersData.MANGAFOURLIFE.NAME};
            }
        }

        for(String searchSourceName: order) {
            sourceSpinnerAdapter.add(searchSourceName);
        }
        sourceSpinner.setAdapter(sourceSpinnerAdapter);
        searchers.put("Bookmarked", bookmarkedSearch);

        recyclerView = findViewById(R.id.search_recycler_view);
        adapter = new SearchListRecyclerAdapter(filteredData, getApplicationContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
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

    public static Intent prepareChangeSourceIntent(Context context, AnimeLinkData animeLinkData, boolean isAnime) {
        Intent intent = new Intent(context, AnimeSearchActivity.class);
        intent.putExtra(INTENT_CHANGE_SOURCE, isAnime ? "anime" : "manga");
        intent.putExtra(INTENT_SEARCH_TERM, animeLinkData.getTitle().replaceAll("\\(.*\\)", "").trim());
        return intent;
    }

}