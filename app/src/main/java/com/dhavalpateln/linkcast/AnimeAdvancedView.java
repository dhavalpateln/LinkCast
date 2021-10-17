package com.dhavalpateln.linkcast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
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
import com.dhavalpateln.linkcast.database.FirebaseDBHelper;
import com.dhavalpateln.linkcast.database.ValueCallback;
import com.dhavalpateln.linkcast.dialogs.AdvancedSourceSelector;
import com.dhavalpateln.linkcast.dialogs.CastDialog;
import com.dhavalpateln.linkcast.dialogs.LinkDownloadManagerDialog;
import com.dhavalpateln.linkcast.exoplayer.ExoPlayerActivity;
import com.dhavalpateln.linkcast.ui.RemoteCodeActivity;
import com.dhavalpateln.linkcast.ui.catalog.CatalogFragment;
import com.firebase.ui.auth.AuthUI;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.ext.cast.CastPlayer;
import com.google.android.exoplayer2.ext.cast.SessionAvailabilityListener;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastState;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnimeAdvancedView extends AppCompatActivity {

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

    public String getCurrentTime() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        String formattedDate = df.format(c.getTime());
        return formattedDate;
    }



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
                    ExtractEpisodeTask task = new ExtractEpisodeTask();
                    task.execute(recyclerData.getUrl(), recyclerData.getNum());
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
        //extractors.put("animepahe.com", new AnimePaheExtractor(calledIntent.getStringExtra("url")));

        for(String extractorName: extractors.keySet()) {
            if(extractors.get(extractorName).isCorrectURL(calledIntent.getStringExtra("url"))) {
                sourceExtractor = extractors.get(extractorName);
                break;
            }
        }

        ExtractDataTask extractDataTask = new ExtractDataTask();
        extractDataTask.execute(calledIntent.getStringExtra("url"));

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

    public void startPlayer(String url, String episodeNum, HashMap<String, String> headers, boolean canCast) {

        CastContext castContext = CastContext.getSharedInstance();

        if(canCast && castContext.getCastState() == CastState.CONNECTED) {
            if(sourceExtractor.getDisplayName().equals("AnimePahe.com") && url.contains(".m3u8")) {
                Toast.makeText(getApplicationContext(), "Cast not supported yet with this source", Toast.LENGTH_LONG).show();
            }
            CastPlayer castPlayer = new CastPlayer(castContext);



            String mimeType = MimeTypes.VIDEO_MP4;


            MediaItem mediaItem = new MediaItem.Builder()
                    .setUri(url)
                    .setMimeType(mimeType)
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
                FirebaseDBHelper.getValue(FirebaseDBHelper.getUserAnimeWebExplorerLinkRef(id).child("data").child(episodeNum), dataSnapshot -> {
                    intent.putExtra(ExoPlayerActivity.LAST_VIEW_POINT, (String) dataSnapshot.getValue());
                    startActivity(intent);
                });
            }
            else {
                startActivity(intent);
            }
        }

    }

    private void updateEpisodeProgress() {
        episodeProgressButton.setText(currentEpisode + "/" + totalEpisode);
    }

    public class ExtractEpisodeTask extends AsyncTask<String, Integer, Map<String, String>> {

        private String episodeNum;

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(AnimeAdvancedView.this);
            progressDialog.setMessage("Please Wait...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(Map<String, String> stringStringMap) {
            super.onPostExecute(stringStringMap);
            progressDialog.dismiss();
            if(stringStringMap != null && stringStringMap.size() > 1) {
                AdvancedSourceSelector dialog = new AdvancedSourceSelector(stringStringMap, new AdvancedSourceSelector.OnClickListener() {
                    @Override
                    public void onClick(AdvancedSourceSelector dialog, String source, String url) {
                        Map<String, CastDialog.OnClickListener> map = new HashMap<>();

                        map.put("PLAY", new CastDialog.OnClickListener() {
                            @Override
                            public void onClick(CastDialog castDialog, String title, String url, Map<String, String> data) {

                                if(source.toLowerCase().startsWith("doodstream")) {
                                    HashMap<String, String> headers = new HashMap<>();
                                    headers.put("Referer", "https://dood.la/");
                                    startPlayer(url, episodeNum, headers, true);
                                }
                                else if(url.contains(".m3u8") || url.replace(".mp4upload", "").contains(".mp4")) {
                                    HashMap<String, String> headers = new HashMap<>();
                                    if(sourceExtractor.getDisplayName().equals("AnimePahe.com")) {
                                        headers.put("Referer", "https://kwik.cx/");
                                    }
                                    startPlayer(url, episodeNum, headers, true);
                                }
                                else if(source.toLowerCase().startsWith("xstreamcdn")) {
                                    startPlayer(url, episodeNum, null, false);
                                }
                                else {
                                    Intent intent = new Intent(getApplicationContext(), AnimeWebExplorer.class);
                                    intent.putExtra("search", animeTitleTextView.getText().toString());
                                    intent.putExtra("source", "generic");
                                    intent.putExtra("generic_url", url);
                                    startActivity(intent);
                                }
                                castDialog.close();
                            }
                        });
                        map.put("JUST CAST", new CastDialog.OnClickListener() {
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
                        dialog.close();
                        CastDialog castDialog = new CastDialog(source, url, map, null);
                        castDialog.show(getSupportFragmentManager(), "CastDialog");
                    }
                });
                dialog.show(getSupportFragmentManager(), "SourceSelector");
            }
        }

        @Override
        protected Map<String, String> doInBackground(String... strings) {
            String url = strings[0];
            episodeNum = strings[1];
            AnimeScrapper extractor = sourceExtractor;
            Map<String, String> extractedEpisodes = null;
            try {
                extractedEpisodes = extractor.extractEpisodeUrls(url);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return extractedEpisodes;
        }
    }

    public class ExtractDataTask extends AsyncTask<String, Integer, String> {

        String baseURL;

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(AnimeAdvancedView.this);
            progressDialog.setMessage("Please Wait...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(String s) {
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
            try {
                Map<String, String> episodeList = extractor.getEpisodeList(baseURL);
                episodeListData.clear();
                for(int i = 1; i <= episodeList.size(); i++) {
                    episodeListData.add(new EpisodeData(episodeList.get(String.valueOf(i)), String.valueOf(i)));
                }
                adapter.notifyDataSetChanged();
            } catch (IOException e) {
                e.printStackTrace();
            }

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
        protected String doInBackground(String... strings) {
            try {
                String urlString = strings[0];
                this.baseURL = strings[0];
                sourceExtractor.extractData();
                return null;
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
        update.put(id + "/data/mode", "advanced");
        update.put(id + "/data/episodenumtext", animeEpisodeNumTextView.getText().toString());
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