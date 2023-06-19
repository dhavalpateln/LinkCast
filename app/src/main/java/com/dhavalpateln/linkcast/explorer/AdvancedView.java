package com.dhavalpateln.linkcast.explorer;

import static com.dhavalpateln.linkcast.utils.Utils.getCurrentTime;
import static com.dhavalpateln.linkcast.utils.Utils.isNumeric;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.dhavalpateln.linkcast.AnimeWebExplorer;
import com.dhavalpateln.linkcast.R;
import com.dhavalpateln.linkcast.database.AnimeMALMetaData;
import com.dhavalpateln.linkcast.database.VideoURLData;
import com.dhavalpateln.linkcast.database.AnimeLinkData;
import com.dhavalpateln.linkcast.database.FirebaseDBHelper;
import com.dhavalpateln.linkcast.database.SharedPrefContract;
import com.dhavalpateln.linkcast.database.room.LinkCastRoomRepository;
import com.dhavalpateln.linkcast.database.room.almaldata.AlMalMetaData;
import com.dhavalpateln.linkcast.database.room.animelinkcache.LinkData;
import com.dhavalpateln.linkcast.database.room.animelinkcache.LinkWithAllData;
import com.dhavalpateln.linkcast.dialogs.ConfirmationDialog;
import com.dhavalpateln.linkcast.dialogs.EpisodeInfoDialog;
import com.dhavalpateln.linkcast.dialogs.EpisodeNoteDialog;
import com.dhavalpateln.linkcast.dialogs.LinkCastProgressDialog;
import com.dhavalpateln.linkcast.dialogs.MyAnimeListSearchDialog;
import com.dhavalpateln.linkcast.dialogs.ViewNotesDialog;
import com.dhavalpateln.linkcast.explorer.adapters.EpisodeNodeRecyclerAdapter;
import com.dhavalpateln.linkcast.explorer.adapters.EpisodeNodeSelectionListener;
import com.dhavalpateln.linkcast.explorer.listeners.AppBarStateChangeListener;
import com.dhavalpateln.linkcast.explorer.listeners.MangaPageListener;
import com.dhavalpateln.linkcast.explorer.listeners.SourceChangeListener;
import com.dhavalpateln.linkcast.explorer.listeners.VideoSelectedListener;
import com.dhavalpateln.linkcast.explorer.listeners.EpisodeNodeListListener;
import com.dhavalpateln.linkcast.explorer.tasks.ExtractEpisodeNodes;
import com.dhavalpateln.linkcast.explorer.tasks.ExtractMALMetaData;
import com.dhavalpateln.linkcast.explorer.tasks.ExtractMangaPages;
import com.dhavalpateln.linkcast.explorer.listeners.AlMalMetaDataListener;
import com.dhavalpateln.linkcast.extractors.AnimeExtractor;
import com.dhavalpateln.linkcast.extractors.Extractor;
import com.dhavalpateln.linkcast.extractors.MangaExtractor;
import com.dhavalpateln.linkcast.extractors.Providers;
import com.dhavalpateln.linkcast.manga.MangaReaderActivity;
import com.dhavalpateln.linkcast.myanimelist.MyAnimelistAnimeData;
import com.dhavalpateln.linkcast.myanimelist.MyAnimelistInfoActivity;
import com.dhavalpateln.linkcast.myanimelist.MyAnimelistSearch;
import com.dhavalpateln.linkcast.ui.animes.AnimeFragment;
import com.dhavalpateln.linkcast.ui.mangas.MangaFragment;
import com.dhavalpateln.linkcast.ui.settings.SettingsFragment;
import com.dhavalpateln.linkcast.database.EpisodeNode;
import com.dhavalpateln.linkcast.utils.Utils;
import com.flaviofaria.kenburnsview.KenBurnsView;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AdvancedView extends AppCompatActivity implements EpisodeNodeListListener, AlMalMetaDataListener, VideoSelectedListener, MangaPageListener, EpisodeNodeSelectionListener {

    private static final int WEB_VIEW_REQUEST_CODE = 1;
    private static final int CHANGE_SOURCE_REQUEST_CODE = 2;
    private static final String TAG = "AdvanceView";

    public static final String INTENT_ANIME_LINK_DATA = "animedata";
    public static final String INTENT_MODE_ANIME = "isanime";
    private static final String INTENT_LINK_WITH_DATA = "linkwithalldata";

    private Map<String, Extractor> extractors;
    private LinkCastProgressDialog progressDialog;
    private ImageView animeImageView;
    private RecyclerView episodeRecyclerView;
    private List<EpisodeNode> episodeListData;
    private EpisodeNodeRecyclerAdapter adapter;
    private TextView animeTitleTextView;
    private Button statusButton;
    private TextView episodeProgressTextView;
    private Button scoreButton;
    private CheckBox favoriteButton;
    private Button bookmarkButton;
    private int currentEpisode = 0;
    private int totalFetchedEpisodes = 0;
    private boolean saveProgress = true;
    private boolean episodeUpdateMode = false;
    private AnimeLinkData animeData;
    private LinkWithAllData linkWithAllData;
    private List<MyAnimelistAnimeData> myAnimelistSearchResult;
    private SharedPreferences prefs;
    private boolean isAnimeMode;
    private int currentIndex = -1;
    private Executor mExecutor = Executors.newCachedThreadPool();
    private Handler uiHandler = new Handler(Looper.getMainLooper());
    private AlMalMetaData malMetaData;
    private LinkCastRoomRepository roomRepo;
    private boolean dismissProgressOnResume = false;
    private ChipGroup episodeChipGroup;
    private KenBurnsView kensBannerImage;
    private AppBarLayout appBarLayout;
    private ImageView expandMoreAppBarButton;
    private ImageView expandLessAppBarButton;
    private Button advViewSettingButton;
    private MaterialButtonToggleGroup episodeViewToggleGroup;

    @Override
    public void onEpisodeSelected(EpisodeNode node, int position) {
        if(isAnimeMode) {
            VideoSelectorDialogFragment dialog = new VideoSelectorDialogFragment(getAnimeExtractor(), linkWithAllData, node, AdvancedView.this);
            dialog.show(getSupportFragmentManager(), "VideoSelector");
        }
        else {
            showProgressDialog();
            mExecutor.execute(new ExtractMangaPages(getExtractor(), linkWithAllData, node, AdvancedView.this));
        }
        int episodeUpdatePref = prefs.getInt(SharedPrefContract.EPISODE_TRACKING, SharedPrefContract.EPISODE_TRACKING_DEFAULT);
        if(episodeUpdatePref == SettingsFragment.EpisodeTracking.MAX_EPISODE && !episodeUpdateMode) {
            currentEpisode = Math.max(currentEpisode, Integer.valueOf(node.getEpisodeNumString()));
        }
        else {
            currentEpisode = (int) node.getEpisodeNum(); //Integer.valueOf(episodeNode.getEpisodeNumString());
        }
        this.linkWithAllData.updateData(AnimeLinkData.DataContract.DATA_EPISODE_NUM, String.valueOf(currentEpisode));
        //animeData.updateData(AnimeLinkData.DataContract.DATA_EPISODE_NUM, String.valueOf(currentEpisode), true, isAnimeMode);
        updateEpisodeProgress();

        adapter.setSkipSingleAnimate(true);
        adapter.notifyItemChanged(position);
    }

    @Override
    public void onEpisodeLongPressed(EpisodeNode node, int position) {
        if(this.linkWithAllData.getId() != null) {
            EpisodeNoteDialog episodeNoteDialog = new EpisodeNoteDialog(node.getNote(), new EpisodeNoteDialog.NoteChangeListener() {
                @Override
                public void onNoteUpdated(String note) {
                    FirebaseDBHelper.getNotesRef(linkWithAllData.getId()).child(node.getEpisodeNumString()).setValue(note);
                    node.setNote(note);
                }

                @Override
                public void onNoteRemoved() {
                    FirebaseDBHelper.getNotesRef(linkWithAllData.getId()).child(node.getEpisodeNumString()).setValue(null);
                    node.setNote(null);
                    //holder.noteIndicator.setVisibility(View.GONE);
                }
            });
            episodeNoteDialog.show(getSupportFragmentManager(), "NoteDialog");

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
        setContentView(R.layout.activity_anime_advanced_view2);
        supportPostponeEnterTransition();

        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        animeImageView = findViewById(R.id.anime_advanced_view_anime_image);
        episodeRecyclerView = findViewById(R.id.anime_advanced_view_episode_recycler_view);
        animeTitleTextView = findViewById(R.id.advanced_view_anime_title_text_view);
        statusButton = findViewById(R.id.advanced_view_status_button);
        episodeProgressTextView = findViewById(R.id.advanced_view_episode_progress_button);
        scoreButton = findViewById(R.id.anime_user_score_button);
        episodeChipGroup = findViewById(R.id.episodeChipGroup);
        progressDialog = new LinkCastProgressDialog();
        kensBannerImage = findViewById(R.id.mediaBanner);
        appBarLayout = findViewById(R.id.mediaAppBar);
        expandMoreAppBarButton = findViewById(R.id.app_bar_expand_more_button);
        expandLessAppBarButton = findViewById(R.id.app_bar_expand_less_button);
        episodeViewToggleGroup = findViewById(R.id.episode_view_toggle_group);
        bookmarkButton = findViewById(R.id.bookmark_button);
        favoriteButton = findViewById(R.id.linkdata_fav_button);
        advViewSettingButton = findViewById(R.id.anime_info_button2);


        appBarLayout.addOnOffsetChangedListener(new AppBarStateChangeListener() {
            @Override
            public void onAppBarStateChanged(AppBarLayout appBarLayout, State state) {
                if(state == State.COLLAPSED) {
                    expandLessAppBarButton.setVisibility(View.GONE);
                    expandMoreAppBarButton.setVisibility(View.VISIBLE);
                }
                else if(state == State.EXPANDED) {
                    expandLessAppBarButton.setVisibility(View.VISIBLE);
                    expandMoreAppBarButton.setVisibility(View.GONE);
                }
                Log.d(TAG, "Appbar state changed: " + state.toString());
            }
        });

        expandMoreAppBarButton.setOnClickListener(v -> appBarLayout.setExpanded(true));
        expandLessAppBarButton.setOnClickListener(v -> appBarLayout.setExpanded(false));

        episodeViewToggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if(isChecked) {
                switch (checkedId) {
                    case R.id.episode_grid_view_button:
                        updateLayout(EpisodeNodeRecyclerAdapter.GRID);
                        break;
                    default:
                        updateLayout(EpisodeNodeRecyclerAdapter.LIST);
                }
            }
        });

        roomRepo = new LinkCastRoomRepository(getApplication());

        CastButtonFactory.setUpMediaRouteButton(getApplicationContext(), findViewById(R.id.mediaRouteButton));
        Intent calledIntent = getIntent();

        isAnimeMode = calledIntent.getBooleanExtra(INTENT_MODE_ANIME, true);

        if(getIntent().hasExtra(INTENT_LINK_WITH_DATA)) {
            linkWithAllData = (LinkWithAllData) getIntent().getSerializableExtra(INTENT_LINK_WITH_DATA);
            this.animeData = AnimeLinkData.from(linkWithAllData.linkData);
        }
        else if(getIntent().hasExtra(INTENT_ANIME_LINK_DATA)) {
            animeData = (AnimeLinkData) getIntent().getSerializableExtra(INTENT_ANIME_LINK_DATA);
            linkWithAllData = new LinkWithAllData();
            linkWithAllData.linkData = LinkData.from(animeData);
        }
        else {
            Toast.makeText(getApplicationContext(), "BAD Data", Toast.LENGTH_LONG).show();
            finish();
        }

        if(this.linkWithAllData.getId() == null) {
            bookmarkButton.setOnClickListener(v -> {
                saveProgress();
                bookmarkButton.setVisibility(View.GONE);
            });
        }
        else {
            bookmarkButton.setVisibility(View.GONE);
        }

        favoriteButton.setChecked(Boolean.parseBoolean(this.linkWithAllData.getMetaData(AnimeLinkData.DataContract.DATA_FAVORITE)));

        favoriteButton.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            this.linkWithAllData.updateData(AnimeLinkData.DataContract.DATA_FAVORITE, String.valueOf(isChecked));
        });

        updateAnimeBannerImage();

        String animeDataEpisodeNum = this.linkWithAllData.getMetaData(AnimeLinkData.DataContract.DATA_EPISODE_NUM);
        if(animeDataEpisodeNum.contains("-")) {
            currentEpisode = Integer.valueOf(this.linkWithAllData.getMetaData(AnimeLinkData.DataContract.DATA_EPISODE_NUM).split("-")[1].trim());
        }
        else {
            currentEpisode = Integer.valueOf(this.linkWithAllData.getMetaData(AnimeLinkData.DataContract.DATA_EPISODE_NUM));
        }

        updateEpisodeProgress();

        statusButton.setText(this.linkWithAllData.getMetaData(AnimeLinkData.DataContract.DATA_STATUS));
        scoreButton.setText(this.linkWithAllData.getMetaData(AnimeLinkData.DataContract.DATA_USER_SCORE));

        animeTitleTextView.setText(this.linkWithAllData.getTitle());

        episodeListData = new ArrayList<>();

        adapter = new EpisodeNodeRecyclerAdapter(getApplicationContext(), episodeListData,this);
        episodeRecyclerView.setAdapter(adapter);
        if(this.linkWithAllData.isAnime()) {
            updateLayout(EpisodeNodeRecyclerAdapter.LIST);
        }
        else {
            updateLayout(EpisodeNodeRecyclerAdapter.GRID);
        }


        Log.d("ADV_VIEW", "URL=" + this.linkWithAllData.getUrl());
        extractors = Providers.getExtractors();

        //mExecutor.execute(new ExtractAnimeData());
        //extractEpisodeNodes();

        episodeProgressTextView.setOnClickListener(v -> {
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
                this.linkWithAllData.updateData(AnimeLinkData.DataContract.DATA_STATUS, status);
                statusButton.setText(status);
            });
            builder.show();
        });

        advViewSettingButton.setOnClickListener(v -> openSettingsBottomSheet());

        loadData();
    }

    private void openSettingsBottomSheet() {
        AdvViewSettingsDialogFragment sheet = new AdvViewSettingsDialogFragment(this.linkWithAllData, () -> {
            loadData();
        });
        sheet.show(getSupportFragmentManager(), "AdvViewSettingsSheet");
    }

    private void updateLayout(int viewType) {
        RecyclerView.LayoutManager layoutManager;
        switch (viewType) {
            case EpisodeNodeRecyclerAdapter.LIST:
                layoutManager = new LinearLayoutManager(this);
                break;
            case EpisodeNodeRecyclerAdapter.GRID:
            default:
                layoutManager = new GridLayoutManager(this,4);
                break;
        }
        adapter.updateType(viewType);
        episodeRecyclerView.setLayoutManager(layoutManager);
        adapter.notifyDataSetChanged();
        episodeRecyclerView.scrollToPosition(adapter.getLastBoundView());
    }

    private void loadData() {
        mExecutor.execute(() -> {

            uiHandler.post(() -> progressDialog.show(getSupportFragmentManager(), "PROGESSDIAG"));

            if(this.linkWithAllData.linkData.getUrl() == null) {
                // No source selected
            }
            mExecutor.execute(new ExtractMALMetaData(this.linkWithAllData, this));
            /*if(this.linkWithAllData.getMetaData(AnimeLinkData.DataContract.DATA_MYANIMELIST_ID) != null) {
                mExecutor.execute(new ExtractMALMetaData(this.linkWithAllData, this));
            }*/

            Extractor extractor = getExtractor();
            if(extractor == null) {
                uiHandler.post(() -> Toast.makeText(getApplicationContext(), "Source is not supported", Toast.LENGTH_LONG).show());
            }
            else {
                new ExtractEpisodeNodes(extractor, this.linkWithAllData, this).run();
            }


            uiHandler.post(() -> progressDialog.dismissAllowingStateLoss());
        });
    }

    private void showProgressDialog() {
        progressDialog.show(getSupportFragmentManager(), "PROGESSDIAG");
    }

    private Extractor getExtractor() {
        String source = this.linkWithAllData.getMetaData(AnimeLinkData.DataContract.DATA_SOURCE);
        if(extractors.containsKey(source)) {
            return extractors.get(source);
        }
        else {
            for (String extractorName : extractors.keySet()) {
                if (extractors.get(extractorName).isCorrectURL(this.linkWithAllData.linkData.getUrl())) {
                    return extractors.get(extractorName);
                }
            }
        }
        return null;
    }

    private AnimeExtractor getAnimeExtractor() {
        return (AnimeExtractor) getExtractor();
    }

    private MangaExtractor getMangaExtractor() {
        return (MangaExtractor) getExtractor();
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
                videoURLData.setEpisodeNum(episodeNum);
                openPlaySelectorDialog(videoURLData);
            }
        }
        else if(requestCode == CHANGE_SOURCE_REQUEST_CODE) {
            if(resultCode == Activity.RESULT_OK) {
                LinkWithAllData changedSourceDataLWAD = (LinkWithAllData) data.getSerializableExtra(AnimeSearchActivity.RESULT_ANIMELINKDATA);
                AnimeLinkData changedSourceData = AnimeLinkData.from(changedSourceDataLWAD.linkData);
                boolean forceResult = data.getBooleanExtra(AnimeSearchActivity.RESULT_FORCE, false);

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
                        if(forceResult || (finalExactMatch != null && String.valueOf(finalExactMatch.getId()).equals(animeData.getAnimeMetaData(AnimeLinkData.DataContract.DATA_MYANIMELIST_ID)))) {
                            animeData.copyFrom(changedSourceData);
                            animeData.updateAll(isAnimeMode);
                            loadData();
                        }
                        else {
                            ConfirmationDialog confirmationDialog = new ConfirmationDialog("Title does not match. Proceed with change?", new ConfirmationDialog.ConfirmationListener() {
                                @Override
                                public void onConfirm() {
                                    animeData.copyFrom(changedSourceData);
                                    animeData.updateAll(isAnimeMode);
                                    loadData();
                                }
                            });
                            confirmationDialog.show(getSupportFragmentManager(), "CONFIRM_CHANGE");
                        }
                    });
                });
            }
        }
    }

    private boolean askResume() {
        int resumeOption = prefs.getInt(SharedPrefContract.RESUME_BEHAVIOUR, SharedPrefContract.RESUME_BEHAVIOUR_DEFAULT);
        boolean askResumeConfirmation = (resumeOption == SettingsFragment.RESUME.ASK) ||
                (resumeOption == SettingsFragment.RESUME.ASK_FOR_COMPLETED
                        && linkWithAllData.getMetaData(AnimeLinkData.DataContract.DATA_STATUS).equalsIgnoreCase(AnimeFragment.Catalogs.COMPLETED));
        return  askResumeConfirmation;
    }

    private void updateEpisodeProgress() {
        String totalEpisodes = "0";
        if(this.malMetaData != null && isNumeric(this.malMetaData.getTotalEpisodes())) {
            totalEpisodes = this.malMetaData.getTotalEpisodes();
        }
        else if(episodeListData != null){
            totalEpisodes = String.valueOf(episodeListData.size());
        }
        episodeProgressTextView.setText(currentEpisode + "/" + totalEpisodes);
    }

    @Override
    public void onMALMetaData(AlMalMetaData metaData) {
        this.malMetaData = metaData;
        roomRepo.insert(metaData);
        updateEpisodeProgress();
        updateAnimeTitle();
    }

    @Override
    public void onBannerImageFetched(String url) {
        Utils.loadImage(getApplicationContext(), kensBannerImage, url);
    }

    @Override
    public void onEpisodeNodesFetched(List<EpisodeNode> episodeList) {
        episodeListData.clear();
        episodeListData.addAll(episodeList);
        Collections.sort(episodeListData, (node1, node2) -> (int) (node2.getEpisodeNum() - node1.getEpisodeNum()));
        totalFetchedEpisodes = episodeList.isEmpty() ? 0 : (int) episodeListData.get(0).getEpisodeNum();
        this.roomRepo.updateLastFetchedEpisode(this.linkWithAllData, totalFetchedEpisodes);
        updateEpisodeProgress();
        episodeRecyclerView.scrollToPosition(totalFetchedEpisodes - currentEpisode);
        fetchNotes();
        updateAnimeBannerImage();
        updateAnimeTitle();
        adapter.notifyDataSetChanged();
        //loadEpisodeChips(totalFetchedEpisodes);
    }

    public void loadEpisodeChips(int episodeCount) {
        if(episodeCount <= 100) {
            episodeChipGroup.setVisibility(View.GONE);
        }
        else {
            episodeChipGroup.setVisibility(View.VISIBLE);
            int chipStartEpisode = 1;
            do {
                Chip chip = (Chip) getLayoutInflater().inflate(R.layout.episode_single_chip, episodeChipGroup, false);
                chip.setText(chipStartEpisode + " - " + Math.min(episodeCount, chipStartEpisode + 99));
                if(currentEpisode >= chipStartEpisode && currentEpisode <= (chipStartEpisode + 99)) {
                    chip.setChecked(true);
                }
                chip.setOnClickListener(view -> {

                });
                episodeChipGroup.addView(chip);
                chipStartEpisode += 100;
            }
            while(chipStartEpisode < episodeCount);
        }
    }

    public void openPlaySelectorDialog(VideoURLData videoURLData) {
        PlaySelectorDialogFragment playSelectorDialogFragment = new PlaySelectorDialogFragment(videoURLData, this.linkWithAllData, askResume());
        playSelectorDialogFragment.show(getSupportFragmentManager(), "PlayDialog");
    }

    @Override
    public void onVideoSelected(VideoURLData videoURLData) {
        if(videoURLData.isPlayable()) {
            openPlaySelectorDialog(videoURLData);
        }
        else {
            Intent intent = new Intent(getApplicationContext(), AnimeWebExplorer.class);
            intent.putExtra(AnimeWebExplorer.EXPLORE_URL, videoURLData.getUrl());
            intent.putExtra(AnimeWebExplorer.EXPLORE_SOURCE, linkWithAllData.getMetaData(AnimeLinkData.DataContract.DATA_SOURCE));
            intent.putExtra(AnimeWebExplorer.RESULT_EPISODE_NUM, videoURLData.getEpisodeNum());
            intent.putExtra(AnimeWebExplorer.RETURN_RESULT, true);
            intent.putExtra("scrapper", getAnimeExtractor().getDisplayName());
            startActivityForResult(intent, WEB_VIEW_REQUEST_CODE);
        }
    }

    @Override
    public void onMangaPagesExtracted(EpisodeNode node, List<String> pages) {
        Intent intent = MangaReaderActivity.prepareIntent(getApplicationContext(), pages.toArray(new String[0]));
        if(!node.isManga()) {
            intent.putExtra(MangaReaderActivity.INTENT_VERTICAL_MODE, true);
            intent.putExtra(MangaReaderActivity.INTENT_REVERSE, false);
        }
        startActivity(intent);
        progressDialog.dismissAllowingStateLoss();
    }

    private void updateAnimeBannerImage() {
        String imageUrl = linkWithAllData.getMetaData(AnimeLinkData.DataContract.DATA_IMAGE_URL);
        if(imageUrl != null) {

            Glide.with(getApplicationContext())
                    .load(imageUrl)
                    .dontAnimate()
                    .transform(new RoundedCorners(50))
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            supportStartPostponedEnterTransition();
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            supportStartPostponedEnterTransition();
                            return false;
                        }
                    })
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(animeImageView);
            animeImageView.setClipToOutline(true);
        }
    }

    private void updateAnimeTitle() {
        animeTitleTextView.setText(this.linkWithAllData.getTitle());
    }

    private void fetchNotes() {
        if(linkWithAllData.getId() != null) {
            FirebaseDBHelper.getValue(FirebaseDBHelper.getNotesRef(linkWithAllData.getId()), dataSnapshot -> {
                if(dataSnapshot.exists()) {
                    Map<String, String> notesMap = new HashMap<>();
                    Iterator<DataSnapshot> iterator = dataSnapshot.getChildren().iterator();
                    while (iterator.hasNext()) {
                        DataSnapshot childSnapShot = iterator.next();
                        notesMap.put(childSnapShot.getKey(), (String) childSnapShot.getValue());
                    }
                    for (EpisodeNode episodeNode : episodeListData) {
                        if (notesMap.containsKey(episodeNode.getEpisodeNumString())) {
                            episodeNode.setNote(notesMap.get(episodeNode.getEpisodeNumString()));
                        }
                    }
                    adapter.notifyDataSetChanged();
                }


            });
        }
    }

    private void saveProgress() {
        linkWithAllData.updateFirebase();
        Toast.makeText(getApplicationContext(), "Bookmarked", Toast.LENGTH_LONG).show();
        //adapter.notifyDataSetChanged();
    }

    public void save(View view) {
        saveProgress();
    }

    public void showOptions(View view) {

        if(false/*optionsButton.getText().toString().equalsIgnoreCase("Bookmark")*/) {
            saveProgress();
            //optionsButton.setText("Options");
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
                            if(malMetaData == null) {
                                selectFromSearchDialog();
                                Toast.makeText(getApplicationContext(), "Match not found", Toast.LENGTH_LONG).show();
                            }
                            else {
                                EpisodeInfoDialog episodeInfoDialog = new EpisodeInfoDialog(animeData, episodeListData.size());
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

    private void startAnimeInfoActivity() {
        MyAnimelistAnimeData myAnimelistAnimeData = new MyAnimelistAnimeData();
        myAnimelistAnimeData.setUrl(this.malMetaData.getUrl());
        myAnimelistAnimeData.setId(Integer.valueOf(this.malMetaData.getId()));
        Intent intent = new Intent(this, MyAnimelistInfoActivity.class);
        intent.putExtra(MyAnimelistInfoActivity.INTENT_ANIMELIST_DATA_KEY, myAnimelistAnimeData);
        startActivity(intent);
    }

    private void selectFromSearchDialog() {
        MyAnimeListSearchDialog myAnimeListSearchDialog = new MyAnimeListSearchDialog(this.linkWithAllData.getTitle(), isAnimeMode, myAnimelistAnimeData -> {
            //this.animeData.getMalMetaData().setUrl(myAnimelistAnimeData.getUrl());
            //this.animeData.getMalMetaData().setId(String.valueOf(myAnimelistAnimeData.getId()));
            this.linkWithAllData.updateData(AnimeLinkData.DataContract.DATA_MYANIMELIST_ID, String.valueOf(myAnimelistAnimeData.getId()));
            mExecutor.execute(new ExtractMALMetaData(this.linkWithAllData, this));
            Toast.makeText(getApplicationContext(), "MAL selected", Toast.LENGTH_LONG).show();

        });
        myAnimeListSearchDialog.show(getSupportFragmentManager(), "MALSearch");
    }

    public void animeInfo(View view) {
        if(this.malMetaData == null) {
            selectFromSearchDialog();
            Toast.makeText(getApplicationContext(), "Match not found", Toast.LENGTH_LONG).show();
            return;
        }
        startAnimeInfoActivity();
    }

    public void updateScore(View view) {
        PopupMenu popupMenu = new PopupMenu(getApplicationContext(), view);
        String[] scores = new String[] {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};
        for(String score: scores)   popupMenu.getMenu().add(score);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                linkWithAllData.updateData(AnimeLinkData.DataContract.DATA_USER_SCORE, menuItem.getTitle().toString());
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

    public static Intent prepareIntent(Context context, LinkWithAllData linkWithAllData) {
        Intent intent = new Intent(context, AdvancedView.class);
        intent.putExtra(INTENT_LINK_WITH_DATA, linkWithAllData);
        return intent;
    }
}