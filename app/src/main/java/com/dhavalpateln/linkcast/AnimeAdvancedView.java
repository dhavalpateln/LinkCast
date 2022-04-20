package com.dhavalpateln.linkcast;

import static com.dhavalpateln.linkcast.utils.Utils.getCurrentTime;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.dhavalpateln.linkcast.adapters.EpisodeGridRecyclerAdapter;
import com.dhavalpateln.linkcast.animescrappers.AnimeKisaTVExtractor;
import com.dhavalpateln.linkcast.animescrappers.AnimePaheExtractor;
import com.dhavalpateln.linkcast.animescrappers.AnimeScrapper;
import com.dhavalpateln.linkcast.animescrappers.GogoAnimeExtractor;
import com.dhavalpateln.linkcast.animescrappers.NineAnimeExtractor;
import com.dhavalpateln.linkcast.animescrappers.VideoURLData;
import com.dhavalpateln.linkcast.database.AnimeLinkData;
import com.dhavalpateln.linkcast.database.FirebaseDBHelper;
import com.dhavalpateln.linkcast.database.SharedPrefContract;
import com.dhavalpateln.linkcast.dialogs.AdvancedSourceSelector;
import com.dhavalpateln.linkcast.dialogs.CastDialog;
import com.dhavalpateln.linkcast.dialogs.LinkDownloadManagerDialog;
import com.dhavalpateln.linkcast.dialogs.MyAnimeListSearchDialog;
import com.dhavalpateln.linkcast.exoplayer.ExoPlayerActivity;
import com.dhavalpateln.linkcast.myanimelist.MyAnimelistAnimeData;
import com.dhavalpateln.linkcast.myanimelist.MyAnimelistInfoActivity;
import com.dhavalpateln.linkcast.myanimelist.MyAnimelistSearch;
import com.dhavalpateln.linkcast.ui.animes.AnimeFragment;
import com.dhavalpateln.linkcast.ui.settings.SettingsFragment;
import com.dhavalpateln.linkcast.utils.EpisodeNode;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.ext.cast.CastPlayer;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastState;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AnimeAdvancedView extends AppCompatActivity {

    private static final int WEB_VIEW_REQUEST_CODE = 1;
    private static final String TAG = "AdvanceView";

    public static final String INTENT_ANIME_LINK_DATA = "animedata";

    private Map<String, AnimeScrapper> extractors;
    private ProgressDialog progressDialog;
    private ImageView animeImageView;
    private RecyclerView episodeRecyclerView;
    private List<EpisodeNode> episodeListData;
    private RecyclerViewAdapter adapter;
    private TextView animeTitleTextView;
    private AnimeScrapper sourceExtractor;
    private Button statusButton;
    private Button episodeProgressButton;
    private Button scoreButton;
    private String id;
    private int currentEpisode = 0;
    private int totalEpisode = 0;
    private boolean saveProgress = true;
    private boolean episodeUpdateMode = false;
    private AnimeLinkData animeData;
    private List<MyAnimelistAnimeData> myAnimelistSearchResult;
    private MyAnimelistAnimeData selectedMyAnimelistAnimeData;
    private SharedPreferences prefs;
    private Executor mExecutor = Executors.newCachedThreadPool();
    private Handler uiHandler = new Handler(Looper.getMainLooper());


    private class EpisodeData {
        private String url;
        private String num;

        public EpisodeData(String url, String num) {
            this.url = url;
            this.num = num;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getNum() {
            return num;
        }

        public void setNum(String num) {
            this.num = num;
        }
    }

    public class RecyclerViewAdapter extends EpisodeGridRecyclerAdapter {

        public RecyclerViewAdapter(List recyclerDataArrayList, Context mcontext) {
            super(recyclerDataArrayList, mcontext);
        }

        @Override
        public void onBindViewHolder(EpisodeRecyclerViewHolder holder, int position, Object data) {
            EpisodeNode episodeNode = (EpisodeNode) data;
            holder.episodeNumTextView.setText(episodeNode.getEpisodeNumString());
            holder.mainLayout.setOnClickListener(v -> {
                if(!episodeUpdateMode) {
                    ExtractEpisodeTask task = new ExtractEpisodeTask();
                    task.execute(episodeNode.getUrl(), episodeNode.getEpisodeNumString());
                }
                else {
                    episodeUpdateMode = false;
                }
                int episodeUpdateMode = prefs.getInt(SharedPrefContract.EPISODE_TRACKING, SharedPrefContract.EPISODE_TRACKING_DEFAULT);
                if(episodeUpdateMode == SettingsFragment.EpisodeTracking.MAX_EPISODE) {
                    currentEpisode = Math.max(currentEpisode, Integer.valueOf(episodeNode.getEpisodeNumString()));
                }
                else {
                    currentEpisode = Integer.valueOf(episodeNode.getEpisodeNumString());
                }
                animeData.updateData(AnimeLinkData.DataContract.DATA_EPISODE_NUM, "Episode - " + currentEpisode);
                updateEpisodeProgress();
            });
        }
    }

    /*public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.RecyclerViewHolder> {

        private ArrayList<EpisodeData> episodeDataArrayList;
        private Context mcontext;

        public RecyclerViewAdapter(ArrayList<EpisodeData> recyclerDataArrayList, Context mcontext) {
            this.episodeDataArrayList = recyclerDataArrayList;
            this.mcontext = mcontext;
        }

        @NonNull
        @Override
        public RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // Inflate Layout
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.anime_advanced_view_episode, parent, false);
            return new RecyclerViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerViewHolder holder, int position) {
            // Set the data to textview and imageview.
            EpisodeData recyclerData = episodeDataArrayList.get(position);
            holder.episodeNumTextView.setText(recyclerData.getNum());
            holder.mainLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!episodeUpdateMode) {
                        ExtractEpisodeTask task = new ExtractEpisodeTask();
                        task.execute(recyclerData.getUrl(), recyclerData.getNum());
                    }
                    else {
                        episodeUpdateMode = false;
                    }
                    int episodeUpdateMode = prefs.getInt(SharedPrefContract.EPISODE_TRACKING, SharedPrefContract.EPISODE_TRACKING_DEFAULT);
                    if(episodeUpdateMode == SettingsFragment.EpisodeTracking.MAX_EPISODE) {
                        currentEpisode = Math.max(currentEpisode, Integer.valueOf(recyclerData.getNum()));
                    }
                    else {
                        currentEpisode = Integer.valueOf(recyclerData.getNum());
                    }
                    animeData.updateData(AnimeLinkData.DataContract.DATA_EPISODE_NUM, "Episode - " + currentEpisode);
                    updateEpisodeProgress();
                }
            });
        }

        @Override
        public int getItemCount() {
            // this method returns the size of recyclerview
            return episodeDataArrayList.size();
        }

        // View Holder Class to handle Recycler View.
        public class RecyclerViewHolder extends RecyclerView.ViewHolder {

            private TextView episodeNumTextView;
            private ConstraintLayout mainLayout;

            public RecyclerViewHolder(@NonNull View itemView) {
                super(itemView);
                this.mainLayout = (ConstraintLayout) itemView;
                this.episodeNumTextView = itemView.findViewById(R.id.advanced_view_episode_num);
            }
        }
    }*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anime_advanced_view);

        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        animeImageView = findViewById(R.id.anime_advanced_view_anime_image);
        episodeRecyclerView = findViewById(R.id.anime_advanced_view_episode_recycler_view);
        animeTitleTextView = findViewById(R.id.advanced_view_anime_title_text_view);
        statusButton = findViewById(R.id.advanced_view_status_button);
        episodeProgressButton = findViewById(R.id.advanced_view_episode_progress_button);
        scoreButton = findViewById(R.id.anime_user_score_button);

        CastButtonFactory.setUpMediaRouteButton(getApplicationContext(), findViewById(R.id.mediaRouteButton));

        Intent calledIntent = getIntent();

        Bundle intentBundle = getIntent().getExtras();
        if(getIntent().hasExtra(INTENT_ANIME_LINK_DATA)) {
            animeData = (AnimeLinkData) getIntent().getSerializableExtra(INTENT_ANIME_LINK_DATA);
        }
        else {
            animeData = new AnimeLinkData();
            animeData.setUrl(intentBundle.getString(AnimeLinkData.DataContract.URL));
            Map<String, String> data = new HashMap<>();
            for (String intentKey : intentBundle.keySet()) {
                if (intentKey.startsWith("data-")) {
                    data.put(intentKey.replace("data-", ""), intentBundle.getString(intentKey));
                }
            }
            animeData.setData(data);
        }

        currentEpisode = Integer.valueOf(animeData.getAnimeMetaData(AnimeLinkData.DataContract.DATA_EPISODE_NUM).split("-")[1].trim());
        updateEpisodeProgress();

        if(calledIntent.hasExtra("id")) {
            id = calledIntent.getStringExtra("id");
            animeData.setId(id);
        }

        statusButton.setText(animeData.getAnimeMetaData(AnimeLinkData.DataContract.DATA_STATUS));

        scoreButton.setText(animeData.getAnimeMetaData(AnimeLinkData.DataContract.DATA_USER_SCORE));

        if(calledIntent.hasExtra("title")) {
            animeTitleTextView.setText(calledIntent.getStringExtra("title"));
            animeData.setTitle(calledIntent.getStringExtra("title").split("\\(")[0]);
        }

        if(animeData.getAnimeMetaData(AnimeLinkData.DataContract.DATA_MYANIMELIST_URL) != null) {
            selectedMyAnimelistAnimeData = new MyAnimelistAnimeData();
            selectedMyAnimelistAnimeData.setUrl(animeData.getAnimeMetaData(
                    AnimeLinkData.DataContract.DATA_MYANIMELIST_URL
            ));
        }

        episodeListData = new ArrayList<>();
        adapter = new RecyclerViewAdapter(episodeListData,this);

        GridLayoutManager layoutManager=new GridLayoutManager(this,4);

        // at last set adapter to recycler view.
        episodeRecyclerView.setHasFixedSize(true);
        episodeRecyclerView.setLayoutManager(layoutManager);
        episodeRecyclerView.setAdapter(adapter);

        extractors = new HashMap<>();
        extractors.put("animekisa.tv", new AnimeKisaTVExtractor());
        extractors.put("animepahe.com", new AnimePaheExtractor());
        extractors.put(ProvidersData.GOGOANIME.NAME, new GogoAnimeExtractor());
        extractors.put(ProvidersData.NINEANIME.NAME, new NineAnimeExtractor(getApplicationContext()));

        Log.d("ADV_VIEW", "URL=" + animeData.getUrl());

        String animeDataSource = animeData.getAnimeMetaData(AnimeLinkData.DataContract.DATA_SOURCE);
        if(extractors.containsKey(animeDataSource)) {
            sourceExtractor = extractors.get(animeDataSource);
        }
        else {
            for (String extractorName : extractors.keySet()) {
                if (extractors.get(extractorName).isCorrectURL(animeData.getUrl())) {
                    sourceExtractor = extractors.get(extractorName);
                    break;
                }
            }
        }

        //ExtractDataTask extractDataTask = new ExtractDataTask();
        //extractDataTask.execute(animeData.getUrl());
        mExecutor.execute(new ExtractAnimeData());

        episodeProgressButton.setOnClickListener(v -> {
            episodeUpdateMode = true;
            Toast.makeText(getApplicationContext(), "Select episode to update", Toast.LENGTH_LONG).show();
        });

        statusButton.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Set Status");
            builder.setItems(AnimeFragment.Catalogs.BASIC_TYPES, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String status = AnimeFragment.Catalogs.BASIC_TYPES[which];
                    if(animeData.getId() != null) {
                        animeData.updateData(AnimeLinkData.DataContract.DATA_STATUS, status);
                        //FirebaseDBHelper.getUserAnimeWebExplorerLinkRef(id).child("data").child("status").setValue(status);
                    }
                    statusButton.setText(status);
                }
            });
            builder.show();
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == WEB_VIEW_REQUEST_CODE) {
            if(resultCode == Activity.RESULT_OK) {
                String url = data.getStringExtra(AnimeWebExplorer.RESULT_URL);
                String episodeNum = data.getStringExtra(AnimeWebExplorer.RESULT_EPISODE_NUM);
                HashMap<String, String> headers = new HashMap<>();
                if(data.hasExtra(AnimeWebExplorer.RESULT_REFERER)) {
                    headers.put("Referer", data.getStringExtra(AnimeWebExplorer.RESULT_REFERER));
                }
                openCastDialog(url, episodeNum, headers, true);
                //startPlayer(url, episodeNum, headers, true);
            }
        }
    }

    private void openCastDialog(String url, String episodeNum, HashMap<String, String> headers, boolean canCast) {
        Map<String, CastDialog.OnClickListener> map = new HashMap<>();

        map.put("PLAY", new CastDialog.OnClickListener() {
            @Override
            public void onClick(CastDialog castDialog, String title, String url, Map<String, String> data) {
                startPlayer(url, episodeNum, headers, canCast);
                castDialog.close();
            }
        });
        map.put("WEB CAST", new CastDialog.OnClickListener() {
            @Override
            public void onClick(CastDialog castDialog, String title, String url, Map<String, String> data) {
                String id = getCurrentTime();
                Map<String, Object> update = new HashMap<>();
                update.put(id + "/title", animeTitleTextView.getText().toString() + " - " + animeData.getAnimeMetaData(AnimeLinkData.DataContract.DATA_EPISODE_NUM));
                update.put(id + "/url", url);
                if(data != null) {
                    for (Map.Entry<String, String> entry : data.entrySet()) {
                        update.put(id + "/data/" + entry.getKey(), entry.getValue());
                    }
                }
                FirebaseDBHelper.getUserLinkRef().updateChildren(update);
                castDialog.close();
            }
        });
        map.put("DOWNLOAD", new CastDialog.OnClickListener() {
            @Override
            public void onClick(CastDialog castDialog, String title, String url, Map<String, String> data) {
                String referer = data.containsKey("Referer") ? data.get("Referer") : null;
                LinkDownloadManagerDialog linkDownloadManagerDialog = new LinkDownloadManagerDialog(url, animeTitleTextView.getText().toString() + " - " + animeData.getAnimeMetaData(AnimeLinkData.DataContract.DATA_EPISODE_NUM) + ".mp4", referer, new LinkDownloadManagerDialog.LinkDownloadListener() {
                    @Override
                    public void onDownloadComplete() {
                        Toast.makeText(getApplicationContext(), "Download Completed", Toast.LENGTH_SHORT).show();
                    }
                });
                linkDownloadManagerDialog.show(getSupportFragmentManager(), "Download");
                castDialog.close();
            }
        });
        CastDialog castDialog = new CastDialog("", url, map, headers);
        castDialog.show(getSupportFragmentManager(), "CastDialog");
    }

    public void startPlayer(String url, String episodeNum, HashMap<String, String> headers, boolean canCast) {

        CastContext castContext = CastContext.getSharedInstance();
        long playbackPosition = 0;
        if(getIntent().hasExtra("data-" + episodeNum)) {
            playbackPosition = Long.valueOf(getIntent().getStringExtra("data-" + episodeNum));
        }

        if(canCast && castContext.getCastState() == CastState.CONNECTED) {
            if(sourceExtractor.getDisplayName().equals("AnimePahe.com") && url.contains(".m3u8")) {
                Toast.makeText(getApplicationContext(), "Cast not supported yet with this source", Toast.LENGTH_LONG).show();
            }
            CastPlayer castPlayer = new CastPlayer(castContext);

            String mimeType = MimeTypes.VIDEO_MP4;


            MediaItem mediaItem = new MediaItem.Builder()
                    .setUri(url)
                    .setMimeType(mimeType)
                    .setClipStartPositionMs(playbackPosition)
                    .build();
            castPlayer.setMediaItem(mediaItem);
            castPlayer.setPlayWhenReady(true);
            castPlayer.prepare();
        }
        else {
            Intent intent = new Intent(getApplicationContext(), ExoPlayerActivity.class);
            intent.putExtra("url", url);
            intent.putExtra("saveProgress", saveProgress);
            if(id != null)  intent.putExtra("id", id);
            if(headers != null) {
                intent.putExtra("headers", headers);
                if(headers.containsKey("Referer")) {
                    intent.putExtra("Referer", headers.get("Referer"));
                }
            }

            if(episodeNum != null) intent.putExtra(ExoPlayerActivity.EPISODE_NUM, episodeNum);
            if(getIntent().hasExtra("data-" + episodeNum)) {
                intent.putExtra(ExoPlayerActivity.LAST_VIEW_POINT, getIntent().getStringExtra("data-" + episodeNum));
            }
            startActivity(intent);

        }

    }

    private void updateEpisodeProgress() {
        episodeProgressButton.setText(currentEpisode + "/" + totalEpisode);
    }

    public class ExtractEpisodeTask extends AsyncTask<String, Integer, List<VideoURLData>> {

        private String episodeNum;

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(AnimeAdvancedView.this);
            progressDialog.setMessage("Please Wait...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(List<VideoURLData> videoURLDataList) {
            super.onPostExecute(videoURLDataList);
            progressDialog.dismiss();
            if(videoURLDataList != null && videoURLDataList.size() > 0) {
                AdvancedSourceSelector dialog = new AdvancedSourceSelector(videoURLDataList, new AdvancedSourceSelector.OnClickListener() {
                    @Override
                    public void onClick(AdvancedSourceSelector dialog, VideoURLData videoURLData) {

                        if(videoURLData.getUrl().replace(".mp4upload", "").contains(".mp4") || videoURLData.getUrl().contains(".m3u8")) {
                            boolean canCast = true;
                            if(videoURLData.getTitle().toLowerCase().startsWith("xstreamcdn")) {
                                canCast = false;
                            }
                            openCastDialog(videoURLData.getUrl(), episodeNum, (HashMap<String, String>) videoURLData.getHeaders(), canCast);
                        }
                        else {
                            Intent intent = new Intent(getApplicationContext(), AnimeWebExplorer.class);
                            intent.putExtra("search", animeTitleTextView.getText().toString());
                            if(videoURLData.getUrl().contains("sbplay")) {
                                intent.putExtra("source", "sbplay.org");
                                intent.putExtra("search", videoURLData.getUrl());
                            }
                            else {
                                intent.putExtra("source", "generic");
                                intent.putExtra("generic_url", videoURLData.getUrl());
                            }

                            intent.putExtra(AnimeWebExplorer.RESULT_EPISODE_NUM, episodeNum);
                            intent.putExtra(AnimeWebExplorer.RETURN_RESULT, true);
                            intent.putExtra("scrapper", sourceExtractor.getDisplayName());

                            startActivityForResult(intent, WEB_VIEW_REQUEST_CODE);
                        }
                        dialog.close();
                    }
                });
                dialog.show(getSupportFragmentManager(), "SourceSelector");
            }
        }

        @Override
        protected List<VideoURLData> doInBackground(String... strings) {
            String url = strings[0];
            episodeNum = strings[1];
            AnimeScrapper extractor = sourceExtractor;
            List<VideoURLData> result = new ArrayList<>();
            extractor.extractEpisodeUrls(url, result);
            return result;
        }
    }

    private class ExtractAnimeData implements Runnable {

        @Override
        public void run() {
            uiHandler.post(() -> {
                progressDialog = new ProgressDialog(AnimeAdvancedView.this);
                progressDialog.setMessage("Please Wait...");
                progressDialog.setCancelable(false);
                progressDialog.show();
            });
            try {
                List<EpisodeNode> episodeList = sourceExtractor.extractData(animeData);

                if(selectedMyAnimelistAnimeData == null) {
                    myAnimelistSearchResult = MyAnimelistSearch.anime(animeData.getTitle());
                    for (MyAnimelistAnimeData myAnimelistAnimeData : myAnimelistSearchResult) {
                        if (myAnimelistAnimeData.getTitle().equals(animeData.getTitle())) {
                            selectedMyAnimelistAnimeData = myAnimelistAnimeData;
                            break;
                        }
                    }
                    if(selectedMyAnimelistAnimeData == null && myAnimelistSearchResult.size() > 1) {
                        if (myAnimelistSearchResult.get(0).getSearchScore() - myAnimelistSearchResult.get(1).getSearchScore() > 10) {
                            selectedMyAnimelistAnimeData = myAnimelistSearchResult.get(0);
                        }
                    }
                    if(selectedMyAnimelistAnimeData != null) {
                        animeData.updateData(
                                AnimeLinkData.DataContract.DATA_MYANIMELIST_ID,
                                String.valueOf(selectedMyAnimelistAnimeData.getId())
                        );
                        animeData.updateData(
                                AnimeLinkData.DataContract.DATA_MYANIMELIST_URL,
                                String.valueOf(selectedMyAnimelistAnimeData.getUrl())
                        );
                    }
                }

                uiHandler.post(() -> {
                    String imageUrl = animeData.getAnimeMetaData(AnimeLinkData.DataContract.DATA_IMAGE_URL);
                    if(imageUrl != null) {
                        Glide.with(getApplicationContext())
                                .load(imageUrl)
                                .centerCrop()
                                .crossFade()
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .into(animeImageView);
                    }
                    animeTitleTextView.setText(animeData.getTitle());
                    episodeListData.clear();
                    episodeListData.addAll(episodeList);

                    Collections.sort(episodeListData, (node1, node2) -> (int) (node1.getEpisodeNum() - node2.getEpisodeNum()));

                    adapter.notifyDataSetChanged();

                    totalEpisode = episodeListData.size();
                    updateEpisodeProgress();
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
            uiHandler.post(() -> progressDialog.dismiss());
        }
    }

    private void saveProgress() {
        String id;
        if(animeData.getId() != null) {
            id = animeData.getId();
        }
        else {
            id = getCurrentTime();
        }
        this.id = id;
        Map<String, Object> update = new HashMap<>();
        update.put(id + "/title", animeTitleTextView.getText().toString());// + "(" + sourceExtractor.getDisplayName() + ")");
        update.put(id + "/url", animeData.getUrl());

        for(String key: animeData.getData().keySet()) {
            update.put(id + "/data/" + key, animeData.getData().get(key));
        }

        update.put(id + "/data/mode", "advanced");
        update.put(id + "/data/" + AnimeLinkData.DataContract.DATA_SOURCE, sourceExtractor.getDisplayName());
        update.put(id + "/data/episodenumtext", animeData.getAnimeMetaData(AnimeLinkData.DataContract.DATA_EPISODE_NUM));
        update.put(id + "/data/status", statusButton.getText().toString());
        if(sourceExtractor.getData("imageUrl") != null) {
            update.put(id + "/data/imageUrl", sourceExtractor.getData("imageUrl"));
        }

        FirebaseDBHelper.getUserAnimeWebExplorerLinkRef().updateChildren(update);

        Toast.makeText(getApplicationContext(), "Progress saved", Toast.LENGTH_LONG).show();

    }

    public void save(View view) {
        saveProgress();
    }

    public void showOptions(View view) {
        PopupMenu popupMenu = new PopupMenu(getApplicationContext(), view);
        //popupMenu.getMenuInflater().inflate(R.menu.advanced_view_menu, popupMenu.getMenu());
        popupMenu.getMenu().add("Bookmark");
        if(animeData.getAnimeMetaData(AnimeLinkData.DataContract.DATA_FAVORITE).equals("true")) {
            popupMenu.getMenu().add("Unfavorite");
        }
        else {
            popupMenu.getMenu().add("Favorite");
        }
        popupMenu.getMenu().add("Reselect MAL Info");

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getTitle().toString()) {
                    case "Favorite":
                        item.setTitle("Unfavorite");
                        animeData.updateData(AnimeLinkData.DataContract.DATA_FAVORITE, "true");
                        return true;
                    case "Unfavorite":
                        item.setTitle("Favorite");
                        animeData.updateData(AnimeLinkData.DataContract.DATA_FAVORITE, "false");
                        return true;
                    case "Reselect MAL Info":
                        selectFromSearchDialog();
                        return true;
                    case "Bookmark":
                        saveProgress();
                        return true;
                    default:
                        return true;
                }
                /*switch (item.getItemId()) {
                    case R.id.av_bookmark:
                        saveProgress();
                        return true;
                    case R.id.av_basic_mode:
                        Intent intent = new Intent(AnimeAdvancedView.this, AnimeWebExplorer.class);
                        intent.putExtra("source", "saved");
                        intent.putExtra("search", getIntent().getStringExtra("url"));
                        intent.putExtra("title", animeTitleTextView.getText().toString());
                        intent.putExtra("advancedMode", false);
                        startActivity(intent);
                        finish();
                        return true;
                    default:
                        return true;
                }*/
            }
        });
        popupMenu.show();
    }

    private void startAnimeInfoActivity(MyAnimelistAnimeData myAnimelistAnimeData) {
        Intent intent = new Intent(this, MyAnimelistInfoActivity.class);
        intent.putExtra(MyAnimelistInfoActivity.INTENT_ANIMELIST_DATA_KEY, myAnimelistAnimeData);
        startActivity(intent);
    }

    private void selectFromSearchDialog() {
        MyAnimeListSearchDialog myAnimeListSearchDialog = new MyAnimeListSearchDialog(animeData.getTitle(), myAnimelistAnimeData -> {
            selectedMyAnimelistAnimeData = myAnimelistAnimeData;
            animeData.updateData(
                    AnimeLinkData.DataContract.DATA_MYANIMELIST_ID,
                    String.valueOf(selectedMyAnimelistAnimeData.getId())
            );
            animeData.updateData(
                    AnimeLinkData.DataContract.DATA_MYANIMELIST_URL,
                    String.valueOf(selectedMyAnimelistAnimeData.getUrl())
            );
            startAnimeInfoActivity(selectedMyAnimelistAnimeData);
        });
        myAnimeListSearchDialog.show(getSupportFragmentManager(), "MALSearch");
    }

    public void animeInfo(View view) {
        if(selectedMyAnimelistAnimeData == null) {
            selectFromSearchDialog();
            Toast.makeText(getApplicationContext(), "Match not found", Toast.LENGTH_LONG).show();
            return;
        }
        startAnimeInfoActivity(selectedMyAnimelistAnimeData);
    }

    public void updateScore(View view) {
        PopupMenu popupMenu = new PopupMenu(getApplicationContext(), view);
        String[] scores = new String[] {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};
        for(String score: scores)   popupMenu.getMenu().add(score);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                animeData.updateData(AnimeLinkData.DataContract.DATA_USER_SCORE, menuItem.getTitle().toString());
                ((Button) view).setText(menuItem.getTitle().toString());
                return false;
            }
        });
        popupMenu.show();
    }

    public static Intent prepareIntent(Context context, AnimeLinkData animeLinkData) {
        Intent intent = new Intent(context, AnimeAdvancedView.class);
        intent.putExtra(INTENT_ANIME_LINK_DATA, animeLinkData);
        return intent;
    }
}