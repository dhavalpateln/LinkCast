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
import android.os.AsyncTask;
import android.os.Bundle;
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
import com.dhavalpateln.linkcast.animescrappers.AnimeKisaCCExtractor;
import com.dhavalpateln.linkcast.animescrappers.AnimeKisaTVExtractor;
import com.dhavalpateln.linkcast.animescrappers.AnimePaheExtractor;
import com.dhavalpateln.linkcast.animescrappers.AnimeScrapper;
import com.dhavalpateln.linkcast.animescrappers.AnimixPlayTOExtractor;
import com.dhavalpateln.linkcast.animescrappers.GogoAnimeExtractor;
import com.dhavalpateln.linkcast.animescrappers.VideoURLData;
import com.dhavalpateln.linkcast.database.AnimeLinkData;
import com.dhavalpateln.linkcast.database.FirebaseDBHelper;
import com.dhavalpateln.linkcast.dialogs.AdvancedSourceSelector;
import com.dhavalpateln.linkcast.dialogs.CastDialog;
import com.dhavalpateln.linkcast.dialogs.LinkDownloadManagerDialog;
import com.dhavalpateln.linkcast.exoplayer.ExoPlayerActivity;
import com.dhavalpateln.linkcast.ui.catalog.CatalogFragment;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.ext.cast.CastPlayer;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastState;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class AnimeAdvancedView extends AppCompatActivity {

    private static final int WEB_VIEW_REQUEST_CODE = 1;
    private static final String TAG = "AdvanceView";

    private Map<String, AnimeScrapper> extractors;
    private ProgressDialog progressDialog;
    private ImageView animeImageView;
    private RecyclerView episodeRecyclerView;
    private ArrayList<EpisodeData> episodeListData;
    private RecyclerViewAdapter adapter;
    private TextView animeTitleTextView;
    private TextView animeEpisodeNumTextView;
    private AnimeScrapper sourceExtractor;
    private Button statusButton;
    private Button episodeProgressButton;
    private Button saveButton;
    private String id;
    private int currentEpisode = 0;
    private int totalEpisode = 0;
    private boolean saveProgress = true;
    private boolean episodeUpdateMode = false;
    private AnimeLinkData animeData;



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

    public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.RecyclerViewHolder> {

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
                    animeEpisodeNumTextView.setText("Episode - " + recyclerData.getNum());
                    if(!episodeUpdateMode) {
                        ExtractEpisodeTask task = new ExtractEpisodeTask();
                        task.execute(recyclerData.getUrl(), recyclerData.getNum());
                    }
                    else {
                        episodeUpdateMode = false;
                    }
                    if(saveProgress && id != null) {
                        FirebaseDBHelper.getUserAnimeWebExplorerLinkRef(id).child("data").child("episodenumtext")
                                .setValue(animeEpisodeNumTextView.getText().toString());
                    }
                    currentEpisode = Integer.valueOf(recyclerData.getNum());
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
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anime_advanced_view);

        animeImageView = findViewById(R.id.anime_advanced_view_anime_image);
        episodeRecyclerView = findViewById(R.id.anime_advanced_view_episode_recycler_view);
        animeTitleTextView = findViewById(R.id.advanced_view_anime_title_text_view);
        animeEpisodeNumTextView = findViewById(R.id.advanced_view_anime_episode_num_text_view);
        statusButton = findViewById(R.id.advanced_view_status_button);
        episodeProgressButton = findViewById(R.id.advanced_view_episode_progress_button);
        saveButton = findViewById(R.id.advanced_view_save_button);
        // TODO: implement update episode progress button

        CastButtonFactory.setUpMediaRouteButton(getApplicationContext(), findViewById(R.id.mediaRouteButton));

        Intent calledIntent = getIntent();

        Bundle intentBundle = getIntent().getExtras();
        animeData = new AnimeLinkData();
        animeData.setUrl(intentBundle.getString(AnimeLinkData.DataContract.URL));
        Map<String, String> data = new HashMap<>();
        for(String intentKey: intentBundle.keySet()) {
            if(intentKey.startsWith("data-")) {
                data.put(intentKey.replace("data-", ""), intentBundle.getString(intentKey));
            }
        }
        animeData.setData(data);

        if(calledIntent.hasExtra("data-episodenumtext") && !calledIntent.getStringExtra("data-episodenumtext").equals("")) {
            animeEpisodeNumTextView.setText(calledIntent.getStringExtra("data-episodenumtext"));
            currentEpisode = Integer.valueOf(calledIntent.getStringExtra("data-episodenumtext").split("-")[1].trim());
            updateEpisodeProgress();
        }
        if(calledIntent.hasExtra("id")) {
            id = calledIntent.getStringExtra("id");
        }

        if(calledIntent.hasExtra("data-status")) {
            statusButton.setText(calledIntent.getStringExtra("data-status"));
        }
        else {
            statusButton.setText("PLANNED");
        }

        if(calledIntent.hasExtra("title")) {
            animeTitleTextView.setText(calledIntent.getStringExtra("title"));
        }

        episodeListData = new ArrayList<>();
        adapter = new RecyclerViewAdapter(episodeListData,this);

        GridLayoutManager layoutManager=new GridLayoutManager(this,4);

        // at last set adapter to recycler view.
        episodeRecyclerView.setHasFixedSize(true);
        episodeRecyclerView.setLayoutManager(layoutManager);
        episodeRecyclerView.setAdapter(adapter);

        extractors = new HashMap<>();
        extractors.put("animekisa.tv", new AnimeKisaTVExtractor(calledIntent.getStringExtra("url")));
        extractors.put("animekisa.cc", new AnimeKisaCCExtractor(calledIntent.getStringExtra("url")));
        extractors.put("animixplay.to", new AnimixPlayTOExtractor(calledIntent.getStringExtra("url")));
        extractors.put("animepahe.com", new AnimePaheExtractor(calledIntent.getStringExtra("url")));
        extractors.put(ProvidersData.GOGOANIME.NAME, new GogoAnimeExtractor(calledIntent.getStringExtra("url")));

        Log.d("ADV_VIEW", "URL=" + calledIntent.getStringExtra("url"));

        for(String extractorName: extractors.keySet()) {
            if(extractors.get(extractorName).isCorrectURL(calledIntent.getStringExtra("url"))) {
                sourceExtractor = extractors.get(extractorName);
                break;
            }
        }

        ExtractDataTask extractDataTask = new ExtractDataTask();
        extractDataTask.execute(calledIntent.getStringExtra("url"));

        episodeProgressButton.setOnClickListener(v -> {
            episodeUpdateMode = true;
            Toast.makeText(getApplicationContext(), "Select episode to update", Toast.LENGTH_LONG).show();
        });

        statusButton.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Set Status");
            ArrayList<String> statusOptions = new ArrayList<>(Arrays.asList(CatalogFragment.CATALOG_TYPE));
            statusOptions.remove("All");
            builder.setItems(statusOptions.toArray(new String[0]), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String status = statusOptions.get(which);
                    if(id != null) {
                        FirebaseDBHelper.getUserAnimeWebExplorerLinkRef(id).child("data").child("status").setValue(status);
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
                update.put(id + "/title", animeTitleTextView.getText().toString() + " - " + animeEpisodeNumTextView.getText().toString());
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
                LinkDownloadManagerDialog linkDownloadManagerDialog = new LinkDownloadManagerDialog(url, animeTitleTextView.getText().toString() + " - " + animeEpisodeNumTextView.getText().toString() + ".mp4", new LinkDownloadManagerDialog.LinkDownloadListener() {
                    @Override
                    public void onDownloadComplete() {
                        Toast.makeText(getApplicationContext(), "Download Completed", Toast.LENGTH_SHORT).show();
                    }
                });
                linkDownloadManagerDialog.show(getSupportFragmentManager(), "Download");
                castDialog.close();
            }
        });
        CastDialog castDialog = new CastDialog("", url, map, null);
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

    public class ExtractEpisodeTask extends AsyncTask<String, Integer, Map<String, VideoURLData>> {

        private String episodeNum;

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(AnimeAdvancedView.this);
            progressDialog.setMessage("Please Wait...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(Map<String, VideoURLData> stringStringMap) {
            super.onPostExecute(stringStringMap);
            progressDialog.dismiss();
            if(stringStringMap != null && stringStringMap.size() > 0) {
                AdvancedSourceSelector dialog = new AdvancedSourceSelector(stringStringMap, new AdvancedSourceSelector.OnClickListener() {
                    @Override
                    public void onClick(AdvancedSourceSelector dialog, VideoURLData videoURLData) {

                        String source = videoURLData.getTitle();
                        String url = videoURLData.getUrl();

                        boolean openCastDialog = true;
                        boolean canCast = true;
                        HashMap<String, String> headers = new HashMap<>();
                        if(videoURLData.hasReferer()) {
                            headers.put("Referer", videoURLData.getReferer());
                        }
                        if(source.toLowerCase().startsWith("doodstream")) {
                            headers.put("Referer", "https://dood.la/");
                            //startPlayer(url, episodeNum, headers, true);
                        }
                        else if(source.toLowerCase().startsWith("vidstream")) {
                            headers.put("Referer", "https://gogoplay.io/");
                            //startPlayer(url, episodeNum, headers, true);
                        }
                        else if(url.contains(".m3u8") || url.replace(".mp4upload", "").contains(".mp4")) {
                            if(sourceExtractor.getDisplayName().equals("AnimePahe.com")) {
                                headers.put("Referer", "https://kwik.cx/");
                            }
                            //startPlayer(url, episodeNum, headers, true);
                        }
                        else if(source.toLowerCase().startsWith("xstreamcdn")) {
                            canCast = false;
                            //startPlayer(url, episodeNum, null, false);
                        }
                        else {
                            openCastDialog = false;
                            Intent intent = new Intent(getApplicationContext(), AnimeWebExplorer.class);
                            intent.putExtra("search", animeTitleTextView.getText().toString());
                            if(url.contains("sbplay")) {
                                intent.putExtra("source", "sbplay.org");
                                intent.putExtra("search", url);
                            }
                            else {
                                intent.putExtra("source", "generic");
                                intent.putExtra("generic_url", url);
                            }

                            intent.putExtra(AnimeWebExplorer.RESULT_EPISODE_NUM, episodeNum);
                            intent.putExtra(AnimeWebExplorer.RETURN_RESULT, true);
                            intent.putExtra("scrapper", sourceExtractor.getDisplayName());

                            startActivityForResult(intent, WEB_VIEW_REQUEST_CODE);
                            //startActivity(intent);
                        }

                        dialog.close();

                        if(openCastDialog) {
                            openCastDialog(url, episodeNum, headers, canCast);
                        }
                    }
                });
                dialog.show(getSupportFragmentManager(), "SourceSelector");
            }
        }

        @Override
        protected Map<String, VideoURLData> doInBackground(String... strings) {
            String url = strings[0];
            episodeNum = strings[1];
            AnimeScrapper extractor = sourceExtractor;
            Map<String, VideoURLData> extractedEpisodes = extractor.extractEpisodeUrls(url);
            return extractedEpisodes;
        }
    }

    public class ExtractDataTask extends AsyncTask<String, Integer, Map<String, String>> {

        String baseURL;

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(AnimeAdvancedView.this);
            progressDialog.setMessage("Please Wait...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(Map<String, String> episodeList) {
            AnimeScrapper extractor = sourceExtractor;
            if(extractor.getData("imageUrl") != null) {
                Glide.with(getApplicationContext())
                        .load(extractor.getData("imageUrl"))
                        .centerCrop()
                        .crossFade()
                        //.bitmapTransform(new CropCircleTransformation(getApplicationContext()))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(animeImageView);
                if(id != null) {
                    FirebaseDBHelper.getUserAnimeWebExplorerLinkRef(id).child("data").child("imageUrl").setValue(extractor.getData("imageUrl"));
                }
            }

            if(extractor.getData("animeTitle") != null) {
                animeTitleTextView.setText(extractor.getData("animeTitle"));
            }


            episodeListData.clear();
            if(episodeList != null) {
                for (int i = 1; i <= episodeList.size(); i++) {
                    episodeListData.add(new EpisodeData(episodeList.get(String.valueOf(i)), String.valueOf(i)));
                }
            }
            else {
                Log.e(TAG, "No episodes found");
            }
            adapter.notifyDataSetChanged();

            totalEpisode = episodeListData.size();
            updateEpisodeProgress();

            progressDialog.dismiss();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            if (values[0].equals(0)) {
                Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected Map<String, String> doInBackground(String... strings) {
            try {
                String urlString = strings[0];
                this.baseURL = strings[0];
                AnimeLinkData animeLinkData = new AnimeLinkData();
                Map<String, String> intentData = new HashMap<>();
                Bundle intentBundle = getIntent().getExtras();
                for(String key: intentBundle.keySet()) {
                    if(key.startsWith("data-")) {
                        intentData.put(key.replace("data-", ""), intentBundle.getString(key));
                    }
                }
                animeLinkData.setData(intentData);
                animeLinkData.setUrl(intentBundle.getString(AnimeLinkData.DataContract.URL));
                animeLinkData.setTitle(intentBundle.getString(AnimeLinkData.DataContract.TITLE));

                Map<String, String> episodeList = sourceExtractor.extractData(animeLinkData);
                return episodeList;
            } catch (Exception e) {
                return null;
            }
        }
    }


    private void saveProgress() {
        Intent calledIntent = getIntent();
        String id;
        if(calledIntent.hasExtra("id")) {
            id = calledIntent.getStringExtra("id");
        }
        else {
            id = getCurrentTime();
        }
        this.id = id;
        Map<String, Object> update = new HashMap<>();
        update.put(id + "/title", animeTitleTextView.getText().toString() + "(" + sourceExtractor.getDisplayName() + ")");
        update.put(id + "/url", calledIntent.getStringExtra("url"));

        for(String key: animeData.getData().keySet()) {
            update.put(id + "/data/" + key, animeData.getData().get(key));
        }

        update.put(id + "/data/mode", "advanced");
        update.put(id + "/data/episodenumtext", animeEpisodeNumTextView.getText().toString());
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
        popupMenu.getMenuInflater().inflate(R.menu.advanced_view_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
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
                }
            }
        });
        popupMenu.show();
    }
}