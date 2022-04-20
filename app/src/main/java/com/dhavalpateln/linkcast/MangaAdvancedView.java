package com.dhavalpateln.linkcast;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.dhavalpateln.linkcast.database.AnimeLinkData;
import com.dhavalpateln.linkcast.manga.MangaReaderActivity;
import com.dhavalpateln.linkcast.mangascrappers.MangaFourLife;
import com.dhavalpateln.linkcast.mangascrappers.MangaScrapper;
import com.dhavalpateln.linkcast.utils.EpisodeNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MangaAdvancedView extends AppCompatActivity {

    private static final int WEB_VIEW_REQUEST_CODE = 1;
    private static final String TAG = "MangaAdvanceView";

    public static final String INTENT_ANIME_LINK_DATA = "animedata";

    private Map<String, MangaScrapper> extractors;
    private ProgressDialog progressDialog;
    private ImageView animeImageView;
    private RecyclerView episodeRecyclerView;
    private ArrayList<EpisodeNode> episodeListData;
    private RecyclerViewAdapter adapter;
    private TextView animeTitleTextView;
    private MangaScrapper sourceExtractor;
    private Button episodeProgressButton;
    private int currentEpisode = 0;
    private int totalEpisode = 0;
    private AnimeLinkData animeData;
    private boolean episodeUpdateMode = false;
    private List<Integer> seasonList;
    private Executor executor = Executors.newSingleThreadExecutor();
    private Handler uiHandler = new Handler(Looper.getMainLooper());


    public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.RecyclerViewHolder> {

        private ArrayList<EpisodeNode> episodeDataArrayList;
        private Context mcontext;

        public RecyclerViewAdapter(ArrayList<EpisodeNode> recyclerDataArrayList, Context mcontext) {
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
            EpisodeNode recyclerData = episodeDataArrayList.get(position);
            holder.episodeNumTextView.setText(recyclerData.getEpisodeNumString());
            holder.mainLayout.setOnClickListener(v -> {
                animeData.updateData(AnimeLinkData.DataContract.DATA_EPISODE_NUM, recyclerData.getEpisodeNumString(), true, false);
                currentEpisode = Integer.valueOf(recyclerData.getEpisodeNumString());
                updateEpisodeProgress();
                if(!episodeUpdateMode) {
                    executor.execute(() -> {
                        uiHandler.post(() -> progressDialog.show());
                        List<String> pages = sourceExtractor.getPages(recyclerData.getUrl());
                        uiHandler.post(() -> {
                            progressDialog.dismiss();
                            Intent intent = MangaReaderActivity.prepareIntent(getApplicationContext(), pages.toArray(new String[0]));
                            if(recyclerData.isManhwa()) {
                                intent.putExtra(MangaReaderActivity.INTENT_VERTICAL_MODE, true);
                                intent.putExtra(MangaReaderActivity.INTENT_REVERSE, false);
                            }
                            startActivity(intent);
                        });
                    });
                }
                else {
                    episodeUpdateMode = false;
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
        setContentView(R.layout.activity_manga_advanced_view);

        animeImageView = findViewById(R.id.manga_advanced_view_manga_image);
        episodeRecyclerView = findViewById(R.id.manga_advanced_view_episode_recycler_view);
        animeTitleTextView = findViewById(R.id.advanced_view_manga_title_text_view);
        episodeProgressButton = findViewById(R.id.advanced_view_chapter_progress_button);

        progressDialog = new ProgressDialog(MangaAdvancedView.this);
        progressDialog.setMessage("Please Wait...");
        progressDialog.setCancelable(false);

        animeData = (AnimeLinkData) getIntent().getSerializableExtra(INTENT_ANIME_LINK_DATA);
        animeTitleTextView.setText(animeData.getTitle());
        Glide.with(getApplicationContext())
                .load(animeData.getAnimeMetaData(AnimeLinkData.DataContract.DATA_IMAGE_URL))
                .centerCrop()
                .crossFade()
                //.bitmapTransform(new CropCircleTransformation(getApplicationContext()))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(animeImageView);

        episodeListData = new ArrayList<>();
        adapter = new RecyclerViewAdapter(episodeListData,this);

        GridLayoutManager layoutManager=new GridLayoutManager(this,4);

        // at last set adapter to recycler view.
        episodeRecyclerView.setHasFixedSize(true);
        episodeRecyclerView.setLayoutManager(layoutManager);
        episodeRecyclerView.setAdapter(adapter);

        extractors = new HashMap<>();
        extractors.put(ProvidersData.MANGAFOURLIFE.NAME, new MangaFourLife());

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

        episodeProgressButton.setOnClickListener(v -> {
            episodeUpdateMode = true;
            Toast.makeText(getApplicationContext(), "Select episode to update", Toast.LENGTH_LONG).show();
        });

        executor.execute(() -> {
            uiHandler.post(() -> progressDialog.show());
            List<EpisodeNode> chapterList = sourceExtractor.getChapters(animeData.getUrl());
            totalEpisode = (int) chapterList.get(0).getEpisodeNum();
            uiHandler.post(() -> {
                updateEpisodeProgress();
                progressDialog.dismiss();
                episodeListData.addAll(chapterList);
                adapter.notifyDataSetChanged();
            });
        });
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == WEB_VIEW_REQUEST_CODE) {
            if(resultCode == Activity.RESULT_OK) {

            }
        }
    }

    private void updateEpisodeProgress() {
        episodeProgressButton.setText(currentEpisode + "/" + totalEpisode);
    }

    public void save(View view) {
        animeData.updateAll(false);
        Toast.makeText(getApplicationContext(), "Progress Saved", Toast.LENGTH_LONG).show();
    }

    public void updateScore(View view) {
        PopupMenu popupMenu = new PopupMenu(getApplicationContext(), view);
        String[] scores = new String[] {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};
        for(String score: scores)   popupMenu.getMenu().add(score);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                animeData.updateData(AnimeLinkData.DataContract.DATA_USER_SCORE, menuItem.getTitle().toString(), true, false);
                ((Button) view).setText(menuItem.getTitle().toString());
                return false;
            }
        });
        popupMenu.show();
    }

    public static Intent prepareIntent(Context context, AnimeLinkData animeLinkData) {
        Intent intent = new Intent(context, MangaAdvancedView.class);
        intent.putExtra(INTENT_ANIME_LINK_DATA, animeLinkData);
        return intent;
    }
}