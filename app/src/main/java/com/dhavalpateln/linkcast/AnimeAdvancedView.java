package com.dhavalpateln.linkcast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
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
import com.dhavalpateln.linkcast.animescrappers.AnimeKisaTVExtractor;
import com.dhavalpateln.linkcast.animescrappers.AnimeScrapper;
import com.dhavalpateln.linkcast.database.FirebaseDBHelper;
import com.dhavalpateln.linkcast.database.ValueCallback;
import com.dhavalpateln.linkcast.dialogs.AdvancedSourceSelector;
import com.dhavalpateln.linkcast.dialogs.CastDialog;
import com.dhavalpateln.linkcast.dialogs.LinkDownloadManagerDialog;
import com.dhavalpateln.linkcast.exoplayer.ExoPlayerActivity;
import com.dhavalpateln.linkcast.ui.RemoteCodeActivity;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
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
                    task.execute(recyclerData.getUrl());
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

        Intent calledIntent = getIntent();
        if(calledIntent.hasExtra("data-episodenumtext")) {
            animeEpisodeNumTextView.setText(calledIntent.getStringExtra("data-episodenumtext"));
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

        ExtractDataTask extractDataTask = new ExtractDataTask();
        extractDataTask.execute(calledIntent.getStringExtra("url"));
    }

    public class ExtractEpisodeTask extends AsyncTask<String, Integer, Map<String, String>> {

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
                                if(url.contains(".m3u8") || url.replace(".mp4upload", "").contains(".mp4")) {
                                    Intent intent = new Intent(getApplicationContext(), ExoPlayerActivity.class);
                                    intent.putExtra("url", url);
                                    startActivity(intent);
                                }
                                else if(source.toLowerCase().startsWith("xstreamcdn")) {
                                    Intent intent = new Intent(getApplicationContext(), ExoPlayerActivity.class);
                                    intent.putExtra("url", url);
                                    startActivity(intent);
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
            AnimeScrapper extractor = extractors.get("animekisa.tv");
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
            AnimeScrapper extractor = extractors.get("animekisa.tv");
            if(extractor.getData("imageUrl") != null) {
                Glide.with(getApplicationContext())
                        .load(extractor.getData("imageUrl"))
                        .centerCrop()
                        .crossFade()
                        //.bitmapTransform(new CropCircleTransformation(getApplicationContext()))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(animeImageView);
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
                extractors.get("animekisa.tv").extractData();
                return null;
            } catch (Exception e) {
                return null;
            }
        }
    }


    public void showOptions(View view) {
        PopupMenu popupMenu = new PopupMenu(getApplicationContext(), view);
        popupMenu.getMenuInflater().inflate(R.menu.advanced_view_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.av_bookmark:
                        Intent calledIntent = getIntent();
                        String id;
                        if(calledIntent.hasExtra("id")) {
                            id = calledIntent.getStringExtra("id");
                        }
                        else {
                            id = getCurrentTime();
                        }
                        Map<String, Object> update = new HashMap<>();
                        update.put(id + "/title", animeTitleTextView.getText().toString() + "(A)");
                        update.put(id + "/url", calledIntent.getStringExtra("url"));
                        update.put(id + "/data/mode", "advanced");
                        update.put(id + "/data/episodenumtext", animeEpisodeNumTextView.getText().toString());

                        FirebaseDBHelper.getUserAnimeWebExplorerLinkRef().updateChildren(update);

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