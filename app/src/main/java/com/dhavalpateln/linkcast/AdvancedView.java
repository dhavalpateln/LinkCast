package com.dhavalpateln.linkcast;

import static com.dhavalpateln.linkcast.utils.Utils.getCurrentTime;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.dhavalpateln.linkcast.adapters.EpisodeGridRecyclerAdapter;
import com.dhavalpateln.linkcast.animescrappers.AnimePaheExtractor;
import com.dhavalpateln.linkcast.animescrappers.AnimeScrapper;
import com.dhavalpateln.linkcast.animescrappers.GogoAnimeExtractor;
import com.dhavalpateln.linkcast.animescrappers.NineAnimeExtractor;
import com.dhavalpateln.linkcast.database.TvActionData;
import com.dhavalpateln.linkcast.database.VideoURLData;
import com.dhavalpateln.linkcast.animescrappers.ZoroExtractor;
import com.dhavalpateln.linkcast.database.AnimeLinkData;
import com.dhavalpateln.linkcast.database.FirebaseDBHelper;
import com.dhavalpateln.linkcast.database.SharedPrefContract;
import com.dhavalpateln.linkcast.dialogs.AdvancedSourceSelector;
import com.dhavalpateln.linkcast.dialogs.CastDialog;
import com.dhavalpateln.linkcast.dialogs.EpisodeInfoDialog;
import com.dhavalpateln.linkcast.dialogs.EpisodeNoteDialog;
import com.dhavalpateln.linkcast.dialogs.LinkDownloadManagerDialog;
import com.dhavalpateln.linkcast.dialogs.MyAnimeListSearchDialog;
import com.dhavalpateln.linkcast.dialogs.ViewNotesDialog;
import com.dhavalpateln.linkcast.exoplayer.ExoPlayerActivity;
import com.dhavalpateln.linkcast.manga.MangaReaderActivity;
import com.dhavalpateln.linkcast.mangascrappers.MangaFourLife;
import com.dhavalpateln.linkcast.mangascrappers.MangaScrapper;
import com.dhavalpateln.linkcast.myanimelist.MyAnimelistAnimeData;
import com.dhavalpateln.linkcast.myanimelist.MyAnimelistInfoActivity;
import com.dhavalpateln.linkcast.myanimelist.MyAnimelistSearch;
import com.dhavalpateln.linkcast.ui.animes.AnimeFragment;
import com.dhavalpateln.linkcast.ui.mangas.MangaFragment;
import com.dhavalpateln.linkcast.ui.settings.SettingsFragment;
import com.dhavalpateln.linkcast.utils.EpisodeNode;
import com.dhavalpateln.linkcast.utils.Utils;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.ext.cast.CastPlayer;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AdvancedView extends AppCompatActivity {

    private static final int WEB_VIEW_REQUEST_CODE = 1;
    private static final int CHANGE_SOURCE_REQUEST_CODE = 2;
    private static final String TAG = "AdvanceView";

    public static final String INTENT_ANIME_LINK_DATA = "animedata";
    public static final String INTENT_MODE_ANIME = "isanime";

    private Map<String, AnimeScrapper> animeExtractors;
    private Map<String, MangaScrapper> mangaExtractors;
    private ProgressDialog progressDialog;
    private ImageView animeImageView;
    private RecyclerView episodeRecyclerView;
    private List<EpisodeNode> episodeListData;
    private RecyclerViewAdapter adapter;
    private TextView animeTitleTextView;
    private Button statusButton;
    private Button episodeProgressButton;
    private Button scoreButton;
    private Button optionsButton;
    private int currentEpisode = 0;
    private int totalEpisode = 0;
    private boolean saveProgress = true;
    private boolean episodeUpdateMode = false;
    private AnimeLinkData animeData;
    private List<MyAnimelistAnimeData> myAnimelistSearchResult;
    private MyAnimelistAnimeData selectedMyAnimelistAnimeData;
    private SharedPreferences prefs;
    private boolean isAnimeMode;
    private int currentIndex = -1;
    private Executor mExecutor = Executors.newCachedThreadPool();
    private Handler uiHandler = new Handler(Looper.getMainLooper());

    public class RecyclerViewAdapter extends EpisodeGridRecyclerAdapter {

        public RecyclerViewAdapter(List recyclerDataArrayList, Context mcontext) {
            super(recyclerDataArrayList, mcontext);
        }

        @Override
        public void onBindViewHolder(EpisodeRecyclerViewHolder holder, int position, Object data) {
            EpisodeNode episodeNode = (EpisodeNode) data;
            holder.episodeNumTextView.setText(episodeNode.getEpisodeNumString());
            if(episodeNode.getNote() != null) {
                holder.noteIndicator.setVisibility(View.VISIBLE);
            }
            else {
                holder.noteIndicator.setVisibility(View.GONE);
            }
            if(currentIndex == holder.getBindingAdapterPosition()) {
                holder.selectedIndicator.setVisibility(View.VISIBLE);
            }
            else {
                holder.selectedIndicator.setVisibility(View.GONE);
            }
            holder.mainLayout.setOnClickListener(v -> {
                if(!episodeUpdateMode) {
                    mExecutor.execute(new OpenEpisodeNodeTask(episodeNode));
                }
                int episodeUpdatePref = prefs.getInt(SharedPrefContract.EPISODE_TRACKING, SharedPrefContract.EPISODE_TRACKING_DEFAULT);
                if(episodeUpdatePref == SettingsFragment.EpisodeTracking.MAX_EPISODE && !episodeUpdateMode) {
                    currentEpisode = Math.max(currentEpisode, Integer.valueOf(episodeNode.getEpisodeNumString()));
                }
                else {
                    currentEpisode = (int) episodeNode.getEpisodeNum(); //Integer.valueOf(episodeNode.getEpisodeNumString());
                }
                animeData.updateData(AnimeLinkData.DataContract.DATA_EPISODE_NUM, "Episode - " + currentEpisode, true, isAnimeMode);
                updateEpisodeProgress();
                episodeUpdateMode = false;
                currentIndex = holder.getBindingAdapterPosition();
                adapter.notifyDataSetChanged();
            });
            holder.mainLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if(animeData.getId() != null) {
                        EpisodeNoteDialog episodeNoteDialog = new EpisodeNoteDialog(episodeNode.getNote(), new EpisodeNoteDialog.NoteChangeListener() {
                            @Override
                            public void onNoteUpdated(String note) {
                                FirebaseDBHelper.getNotesRef(animeData.getId()).child(episodeNode.getEpisodeNumString()).setValue(note);
                                episodeNode.setNote(note);
                                holder.noteIndicator.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onNoteRemoved() {
                                FirebaseDBHelper.getNotesRef(animeData.getId()).child(episodeNode.getEpisodeNumString()).setValue(null);
                                episodeNode.setNote(null);
                                holder.noteIndicator.setVisibility(View.GONE);
                            }
                        });
                        episodeNoteDialog.show(getSupportFragmentManager(), "NoteDialog");

                    }
                    return true;
                }
            });
        }
    }

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
        optionsButton = findViewById(R.id.advanced_options_button);

        CastButtonFactory.setUpMediaRouteButton(getApplicationContext(), findViewById(R.id.mediaRouteButton));

        Intent calledIntent = getIntent();

        isAnimeMode = calledIntent.getBooleanExtra(INTENT_MODE_ANIME, true);

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

        if(animeData.getId() == null) {
            optionsButton.setText("Bookmark");
        }

        String animeDataEpisodeNum = animeData.getAnimeMetaData(AnimeLinkData.DataContract.DATA_EPISODE_NUM);
        if(animeDataEpisodeNum.contains("-")) {
            currentEpisode = Integer.valueOf(animeData.getAnimeMetaData(AnimeLinkData.DataContract.DATA_EPISODE_NUM).split("-")[1].trim());
        }
        else {
            currentEpisode = Integer.valueOf(animeData.getAnimeMetaData(AnimeLinkData.DataContract.DATA_EPISODE_NUM));
        }

        updateEpisodeProgress();

        statusButton.setText(animeData.getAnimeMetaData(AnimeLinkData.DataContract.DATA_STATUS));
        scoreButton.setText(animeData.getAnimeMetaData(AnimeLinkData.DataContract.DATA_USER_SCORE));

        animeTitleTextView.setText(animeData.getTitle().replaceAll("\\(.*\\)", ""));

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

        animeExtractors = new HashMap<>();
        animeExtractors.put(ProvidersData.ANIMEPAHE.NAME, new AnimePaheExtractor());
        animeExtractors.put(ProvidersData.GOGOANIME.NAME, new GogoAnimeExtractor());
        animeExtractors.put(ProvidersData.NINEANIME.NAME, new NineAnimeExtractor(getApplicationContext()));
        animeExtractors.put(ProvidersData.ZORO.NAME, new ZoroExtractor());

        mangaExtractors = new HashMap<>();
        mangaExtractors.put(ProvidersData.MANGAFOURLIFE.NAME, new MangaFourLife());

        Log.d("ADV_VIEW", "URL=" + animeData.getUrl());

        mExecutor.execute(new ExtractAnimeData());

        episodeProgressButton.setOnClickListener(v -> {
            episodeUpdateMode = true;
            Toast.makeText(getApplicationContext(), "Select episode to update", Toast.LENGTH_LONG).show();
        });

        statusButton.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Set Status");
            String[] statusItems;
            if(isAnimeMode) statusItems = AnimeFragment.Catalogs.BASIC_TYPES;
            else statusItems = MangaFragment.Catalog.BASIC_TYPES;
            builder.setItems(statusItems, (dialog, which) -> {
                String status = statusItems[which];
                animeData.updateData(AnimeLinkData.DataContract.DATA_STATUS, status, true, isAnimeMode);
                statusButton.setText(status);
            });
            builder.show();
        });

    }

    private AnimeScrapper getAnimeExtractor() {
        String animeDataSource = animeData.getAnimeMetaData(AnimeLinkData.DataContract.DATA_SOURCE);
        if(animeExtractors.containsKey(animeDataSource)) {
            return animeExtractors.get(animeDataSource);
        }
        else {
            for (String extractorName : animeExtractors.keySet()) {
                if (animeExtractors.get(extractorName).isCorrectURL(animeData.getUrl())) {
                    return animeExtractors.get(extractorName);
                }
            }
        }
        return null;
    }

    private MangaScrapper getMangaExtractor() {
        String animeDataSource = animeData.getAnimeMetaData(AnimeLinkData.DataContract.DATA_SOURCE);
        if(mangaExtractors.containsKey(animeDataSource)) {
            return mangaExtractors.get(animeDataSource);
        }
        else {
            for (String extractorName : mangaExtractors.keySet()) {
                if (mangaExtractors.get(extractorName).isCorrectURL(animeData.getUrl())) {
                    return mangaExtractors.get(extractorName);
                }
            }
        }
        return null;
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
                VideoURLData videoURLData = new VideoURLData(url);
                videoURLData.setHeaders(headers);
                openCastDialog(videoURLData, episodeNum, true);
                //startPlayer(url, episodeNum, headers, true);
            }
        }
        else if(requestCode == CHANGE_SOURCE_REQUEST_CODE) {
            if(resultCode == Activity.RESULT_OK) {
                AnimeLinkData changedSourceData = (AnimeLinkData) data.getSerializableExtra(AnimeSearchActivity.RESULT_ANIMELINKDATA);

                mExecutor.execute(() -> {
                    String formattedTitle = changedSourceData.getTitle().replaceAll("\\(.*\\)", "").trim();
                    List<MyAnimelistAnimeData> checkSearchResult = MyAnimelistSearch.search(formattedTitle, isAnimeMode);
                    MyAnimelistAnimeData exactMatch = null;
                    for (MyAnimelistAnimeData myAnimelistAnimeData : checkSearchResult) {
                        if (myAnimelistAnimeData.getTitle().equalsIgnoreCase(formattedTitle)) {
                            exactMatch = myAnimelistAnimeData;
                            break;
                        }
                    }
                    if(exactMatch == null && checkSearchResult.size() > 0) {
                        exactMatch = checkSearchResult.get(0);
                    }
                    MyAnimelistAnimeData finalExactMatch = exactMatch;
                    uiHandler.post(() -> {
                        if(finalExactMatch != null && String.valueOf(finalExactMatch.getId()).equals(animeData.getAnimeMetaData(AnimeLinkData.DataContract.DATA_MYANIMELIST_ID))) {
                            animeData.copyFrom(changedSourceData);
                            animeData.updateAll(isAnimeMode);
                            mExecutor.execute(new ExtractAnimeData());
                        }
                        else {
                            Toast.makeText(getApplicationContext(), "Does not match", Toast.LENGTH_LONG).show();
                        }
                    });
                });
            }
        }
    }

    private void openCastDialog(VideoURLData videoURLData, String episodeNum, boolean canCast) {
        Map<String, CastDialog.OnClickListener> map = new HashMap<>();

        map.put("PLAY", new CastDialog.OnClickListener() {
            @Override
            public void onClick(CastDialog castDialog, String title, String url, Map<String, String> data) {
                startPlayer(videoURLData, episodeNum, canCast);
                castDialog.close();
            }
        });
        map.put("APP CAST", new CastDialog.OnClickListener() {
            @Override
            public void onClick(CastDialog castDialog, String title, String url, Map<String, String> data) {
               /* String id = getCurrentTime();
                Map<String, Object> update = new HashMap<>();
                update.put(id + "/title", animeTitleTextView.getText().toString() + " - " + animeData.getAnimeMetaData(AnimeLinkData.DataContract.DATA_EPISODE_NUM));
                update.put(id + "/url", url);
                if(data != null) {
                    for (Map.Entry<String, String> entry : data.entrySet()) {
                        update.put(id + "/data/" + entry.getKey(), entry.getValue());
                    }
                }
                FirebaseDBHelper.getUserLinkRef().updateChildren(update);*/
                TvActionData tvActionData = new TvActionData();
                tvActionData.setAction("Play");
                tvActionData.setVideoData(videoURLData);
                tvActionData.setEpisodeNum(episodeNum);
                tvActionData.setId(animeData.getId());
                FirebaseDBHelper.getUserTvPlay().setValue(tvActionData);
                castDialog.close();
            }
        });
        if(videoURLData.getUrl().replace("mp4upload", "").contains(".mp4")) {
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
        }
        CastDialog castDialog = new CastDialog("", videoURLData.getUrl(), map, videoURLData.getHeaders());
        castDialog.show(getSupportFragmentManager(), "CastDialog");
    }

    public void startPlayer(VideoURLData videoURLData, String episodeNum, boolean canCast) {

        String url = videoURLData.getUrl();
        CastContext castContext = CastContext.getSharedInstance();
        long playbackPosition = 0;
        if(animeData.getData().containsKey(episodeNum)) {
            playbackPosition = Long.valueOf(animeData.getData().get(episodeNum));
        }

        if(canCast && castContext.getCastState() == CastState.CONNECTED) {
            if(getAnimeExtractor().getDisplayName().equals("AnimePahe.com") && url.contains(".m3u8")) {
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
            Intent intent = ExoPlayerActivity.prepareIntent(getApplicationContext(), animeData, videoURLData, episodeNum);
            /*Intent intent = new Intent(getApplicationContext(), ExoPlayerActivity.class);
            intent.putExtra("url", url);
            intent.putExtra("saveProgress", saveProgress);
            if(animeData.getId() != null)  intent.putExtra("id", animeData.getId());
            if(headers != null) {
                intent.putExtra("headers", headers);
                if(headers.containsKey("Referer")) {
                    intent.putExtra("Referer", headers.get("Referer"));
                }
            }

            if(episodeNum != null) intent.putExtra(ExoPlayerActivity.EPISODE_NUM, episodeNum);
            if(animeData.getData().containsKey(episodeNum)) {
                intent.putExtra(ExoPlayerActivity.LAST_VIEW_POINT, animeData.getData().get(episodeNum));
            }*/
            startActivity(intent);

        }

    }

    private void updateEpisodeProgress() {
        episodeProgressButton.setText(currentEpisode + "/" + totalEpisode);
    }

    private class OpenEpisodeNodeTask implements Runnable {

        private EpisodeNode node;

        public OpenEpisodeNodeTask(EpisodeNode node) {
            this.node = node;
        }

        @Override
        public void run() {

            uiHandler.post(() -> {
                progressDialog = new ProgressDialog(AdvancedView.this);
                progressDialog.setMessage("Please Wait...");
                progressDialog.setCancelable(false);
                progressDialog.show();
            });

            if(isAnimeMode) {
                List<VideoURLData> videoURLDataList = new ArrayList<>();
                getAnimeExtractor().extractEpisodeUrls(this.node.getUrl(), videoURLDataList);
                uiHandler.post(() -> {
                    if(videoURLDataList != null && videoURLDataList.size() > 0) {
                        AdvancedSourceSelector dialog = new AdvancedSourceSelector(videoURLDataList, new AdvancedSourceSelector.OnClickListener() {
                            @Override
                            public void onClick(AdvancedSourceSelector dialog, VideoURLData videoURLData) {

                                if(videoURLData.getUrl().replace(".mp4upload", "").contains(".mp4") || videoURLData.getUrl().contains(".m3u8")) {
                                    boolean canCast = true;
                                    if(videoURLData.getTitle().toLowerCase().startsWith("xstreamcdn")) {
                                        canCast = false;
                                    }
                                    openCastDialog(videoURLData, node.getEpisodeNumString(), canCast);
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

                                    intent.putExtra(AnimeWebExplorer.RESULT_EPISODE_NUM, node.getEpisodeNumString());
                                    intent.putExtra(AnimeWebExplorer.RETURN_RESULT, true);
                                    intent.putExtra("scrapper", getAnimeExtractor().getDisplayName());

                                    startActivityForResult(intent, WEB_VIEW_REQUEST_CODE);
                                }
                                dialog.close();
                            }
                        });
                        dialog.show(getSupportFragmentManager(), "SourceSelector");
                    }
                });
            }
            else {
                List<String> pages = getMangaExtractor().getPages(node.getUrl());
                uiHandler.post(() -> {
                    Intent intent = MangaReaderActivity.prepareIntent(getApplicationContext(), pages.toArray(new String[0]));
                    if(!node.isManga()) {
                        intent.putExtra(MangaReaderActivity.INTENT_VERTICAL_MODE, true);
                        intent.putExtra(MangaReaderActivity.INTENT_REVERSE, false);
                    }
                    startActivity(intent);
                });
            }
            uiHandler.post(() -> progressDialog.dismiss());
        }
    }


    private class ExtractAnimeData implements Runnable {

        @Override
        public void run() {
            uiHandler.post(() -> {
                progressDialog = new ProgressDialog(AdvancedView.this);
                progressDialog.setMessage("Please Wait...");
                progressDialog.setCancelable(false);
                progressDialog.show();
            });
            try {
                List<EpisodeNode> episodeList;
                if(isAnimeMode) {
                    episodeList = getAnimeExtractor().extractData(animeData);
                }
                else {
                    episodeList = getMangaExtractor().getChapters(animeData.getUrl());
                }

                if(selectedMyAnimelistAnimeData == null) {
                    String formattedTitle = animeData.getTitle().replaceAll("\\(.*\\)", "").trim();
                    myAnimelistSearchResult = MyAnimelistSearch.search(formattedTitle, isAnimeMode);
                    for (MyAnimelistAnimeData myAnimelistAnimeData : myAnimelistSearchResult) {
                        if (myAnimelistAnimeData.getTitle().equalsIgnoreCase(formattedTitle)) {
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
                                String.valueOf(selectedMyAnimelistAnimeData.getId()),
                                true,
                                isAnimeMode
                        );
                        animeData.updateData(
                                AnimeLinkData.DataContract.DATA_MYANIMELIST_URL,
                                String.valueOf(selectedMyAnimelistAnimeData.getUrl()),
                                true,
                                isAnimeMode
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

                    Collections.sort(episodeListData, (node1, node2) -> (int) (node2.getEpisodeNum() - node1.getEpisodeNum()));

                    totalEpisode = (int) episodeListData.get(0).getEpisodeNum();
                    updateEpisodeProgress();
                    episodeRecyclerView.scrollToPosition(totalEpisode - currentEpisode);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
            uiHandler.post(() -> {
                if(animeData.getId() != null) {
                    FirebaseDBHelper.getValue(FirebaseDBHelper.getNotesRef(animeData.getId()), dataSnapshot -> {
                        Map<String, String> notesMap = (Map<String, String>) dataSnapshot.getValue();
                        if (notesMap != null) {
                            for (EpisodeNode episodeNode : episodeListData) {
                                if (notesMap.containsKey(episodeNode.getEpisodeNumString())) {
                                    episodeNode.setNote(notesMap.get(episodeNode.getEpisodeNumString()));
                                }
                            }
                        }
                        adapter.notifyDataSetChanged();
                        progressDialog.dismiss();
                    });
                }
                else {
                    adapter.notifyDataSetChanged();
                    progressDialog.dismiss();
                }
            });
        }
    }

    private void saveProgress() {
        if(animeData.getId() == null) {
            animeData.setId(Utils.getCurrentTime());
        }
        animeData.updateAll(isAnimeMode);
        Toast.makeText(getApplicationContext(), "Bookmarked", Toast.LENGTH_LONG).show();
        adapter.notifyDataSetChanged();
    }

    public void save(View view) {
        saveProgress();
    }

    public void showOptions(View view) {

        if(optionsButton.getText().toString().equalsIgnoreCase("Bookmark")) {
            saveProgress();
            optionsButton.setText("Options");
        }
        else {

            PopupMenu popupMenu = new PopupMenu(getApplicationContext(), view);
            //popupMenu.getMenuInflater().inflate(R.menu.advanced_view_menu, popupMenu.getMenu());
            //popupMenu.getMenu().add("Bookmark");
            if (animeData.getAnimeMetaData(AnimeLinkData.DataContract.DATA_FAVORITE).equals("true")) {
                popupMenu.getMenu().add("Unfavorite");
            } else {
                popupMenu.getMenu().add("Favorite");
            }
            popupMenu.getMenu().add("Change source");
            popupMenu.getMenu().add("View notes");
            if(isAnimeMode) {
                popupMenu.getMenu().add("Episodes Info");
            }
            popupMenu.getMenu().add("Select MAL Info");

            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getTitle().toString()) {
                        case "Favorite":
                            item.setTitle("Unfavorite");
                            animeData.updateData(AnimeLinkData.DataContract.DATA_FAVORITE, "true", true, isAnimeMode);
                            return true;
                        case "Unfavorite":
                            item.setTitle("Favorite");
                            animeData.updateData(AnimeLinkData.DataContract.DATA_FAVORITE, "false", true, isAnimeMode);
                            return true;
                        case "Select MAL Info":
                            selectFromSearchDialog();
                            return true;
                        case "Change source":
                            Intent intent = AnimeSearchActivity.prepareChangeSourceIntent(AdvancedView.this, animeData, isAnimeMode);
                            startActivityForResult(intent, CHANGE_SOURCE_REQUEST_CODE);
                            return true;
                        case "Episodes Info":
                            if(selectedMyAnimelistAnimeData == null) {
                                selectFromSearchDialog();
                                Toast.makeText(getApplicationContext(), "Match not found", Toast.LENGTH_LONG).show();
                            }
                            else {
                                EpisodeInfoDialog episodeInfoDialog = new EpisodeInfoDialog(animeData.getAnimeMetaData(AnimeLinkData.DataContract.DATA_MYANIMELIST_ID), episodeListData.size());
                                episodeInfoDialog.show(getSupportFragmentManager(), "InfoDialog");
                            }
                            return true;
                        case "Bookmark":
                            saveProgress();
                            return true;
                        case "View notes":
                            ViewNotesDialog viewNotesDialog = new ViewNotesDialog(episodeListData);
                            viewNotesDialog.show(getSupportFragmentManager(), "NotesDialog");
                            return true;
                        default:
                            return true;
                    }
                }
            });
            popupMenu.show();
        }
    }

    private void startAnimeInfoActivity(MyAnimelistAnimeData myAnimelistAnimeData) {
        Intent intent = new Intent(this, MyAnimelistInfoActivity.class);
        intent.putExtra(MyAnimelistInfoActivity.INTENT_ANIMELIST_DATA_KEY, myAnimelistAnimeData);
        startActivity(intent);
    }

    private void selectFromSearchDialog() {
        MyAnimeListSearchDialog myAnimeListSearchDialog = new MyAnimeListSearchDialog(animeData.getTitle(), isAnimeMode, myAnimelistAnimeData -> {
            selectedMyAnimelistAnimeData = myAnimelistAnimeData;
            animeData.updateData(
                    AnimeLinkData.DataContract.DATA_MYANIMELIST_ID,
                    String.valueOf(selectedMyAnimelistAnimeData.getId()),
                    true,
                    isAnimeMode
            );
            animeData.updateData(
                    AnimeLinkData.DataContract.DATA_MYANIMELIST_URL,
                    String.valueOf(selectedMyAnimelistAnimeData.getUrl()),
                    true,
                    isAnimeMode
            );
            Toast.makeText(getApplicationContext(), "MAL selected", Toast.LENGTH_LONG).show();
            //startAnimeInfoActivity(selectedMyAnimelistAnimeData);
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
                animeData.updateData(AnimeLinkData.DataContract.DATA_USER_SCORE, menuItem.getTitle().toString(), true, isAnimeMode);
                ((Button) view).setText(menuItem.getTitle().toString());
                return false;
            }
        });
        popupMenu.show();
    }

    public static Intent prepareIntent(Context context, AnimeLinkData animeLinkData) {
        Intent intent = new Intent(context, AdvancedView.class);
        intent.putExtra(INTENT_ANIME_LINK_DATA, animeLinkData);
        return intent;
    }
}