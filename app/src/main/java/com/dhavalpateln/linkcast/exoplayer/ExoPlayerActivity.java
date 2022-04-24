package com.dhavalpateln.linkcast.exoplayer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.dhavalpateln.linkcast.R;
import com.dhavalpateln.linkcast.data.StoredAnimeLinkData;
import com.dhavalpateln.linkcast.database.AnimeLinkData;
import com.dhavalpateln.linkcast.database.FirebaseDBHelper;
import com.dhavalpateln.linkcast.database.ValueCallback;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.util.Log;
import com.google.android.gms.cast.framework.CastContext;
import com.google.firebase.database.DataSnapshot;

import java.util.HashMap;
import java.util.Map;


public class ExoPlayerActivity extends AppCompatActivity {

    public static final String ID = "id";
    public static final String EPISODE_NUM = "num";
    public static final String LAST_VIEW_POINT = "last_point";
    public static final String MEDIA_URL = "url";
    public static final String FILE_TYPE = "file_type";

    private final String ARG_FIRST_LOAD = "firstload";
    private final String ARG_PLAYBACK_POSITION = "position";

    public enum FileTypes {
        LOCAL,
        URL
    }

    private PlayerView exoplayerView;
    private SimpleExoPlayer player;
    private boolean playWhenReady = true;
    private int currentWindow = 0;
    private long playbackPosition = 0L;
    private ImageView fullScreenIcon;
    private boolean saveProgress = true;
    private String id = null;
    private String episodeNum = null;
    private boolean firstLoad = false;
    private FileTypes fileType;
    private Handler handler = new Handler();

    @Override
    protected void onStart() {
        super.onStart();
        initPlayer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(player == null) {
            initPlayer();
        }
        else {
            player.play();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(player != null) {
            player.pause();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        release();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        release();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putBoolean(ARG_FIRST_LOAD, firstLoad);
        outState.putLong(ARG_PLAYBACK_POSITION, playbackPosition);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if(getIntent().hasExtra(FILE_TYPE)) {
            fileType = (FileTypes) getIntent().getSerializableExtra(FILE_TYPE);
        }
        else {
            fileType = FileTypes.URL;
        }


        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            //Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
            fullScreenIcon.setImageResource(R.drawable.exo_styled_controls_fullscreen_exit);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            //Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
            fullScreenIcon.setImageResource(R.drawable.exo_styled_controls_fullscreen_enter);
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState != null) {
            firstLoad = savedInstanceState.getBoolean(ARG_FIRST_LOAD, false);
            playbackPosition = savedInstanceState.getLong(ARG_PLAYBACK_POSITION, 0);
        }

        setContentView(R.layout.activity_exo_player);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        fullScreenIcon = findViewById(R.id.exo_fullscreen);
        exoplayerView = findViewById(R.id.exoplayer);
        exoplayerView.setKeepScreenOn(true);

        //exoplayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
    }

    private void release() {
        if(player != null) {
            this.playWhenReady = player.getPlayWhenReady();
            this.currentWindow = player.getCurrentWindowIndex();
            this.playbackPosition = player.getCurrentPosition();
            player.release();
            if(saveProgress && id != null && episodeNum != null) {
                FirebaseDBHelper.getPlayBackPositionRef(id).child(episodeNum).setValue(String.valueOf(playbackPosition));
            }
        }
        player = null;
    }

    private void initPlayer() {

        Intent intent = getIntent();
        String url = intent.getStringExtra("url");
        if(intent.hasExtra(ID)) id = intent.getStringExtra(ID);
        if(intent.hasExtra(EPISODE_NUM)) episodeNum = intent.getStringExtra(EPISODE_NUM);



        Map<String, String> headerMap = new HashMap<>();
        if(intent.hasExtra("Referer")) {
            headerMap.put("Referer", intent.getStringExtra("Referer"));
            headerMap.put("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/95.0.4638.69 Safari/537.36");
        }


        DataSource.Factory dataSourceFactory = new DefaultHttpDataSource.Factory().setDefaultRequestProperties(headerMap);

        player = new SimpleExoPlayer.Builder(getApplicationContext())
                .setMediaSourceFactory(new DefaultMediaSourceFactory(dataSourceFactory))
                .build();



        exoplayerView.setPlayer(player);

        // Build the media item.
        MediaItem mediaItem = MediaItem.fromUri(Uri.parse(url));
        // Set the media item to be played.
        player.setMediaItem(mediaItem);
        player.setPlayWhenReady(playWhenReady);
        player.seekTo(currentWindow, playbackPosition);
        // Prepare the player.
        player.prepare();
        if(!firstLoad) {
            firstLoad = true;
            if (id != null && episodeNum != null) {
                FirebaseDBHelper.getValue(FirebaseDBHelper.getPlayBackPositionRef(id).child(episodeNum), dataSnapshot -> {
                    if(dataSnapshot.getValue() != null) {
                        playbackPosition = Long.valueOf(dataSnapshot.getValue().toString());
                        if(player != null) {
                            player.seekTo(currentWindow, playbackPosition);
                        }
                    }
                });

                /*AnimeLinkData animeLinkData = StoredAnimeLinkData.getInstance().getAnimeLinkData(id);
                String lastViewPoint = animeLinkData.getAnimeMetaData(episodeNum);
                if (lastViewPoint != null) playbackPosition = Long.valueOf(lastViewPoint);*/
            }
        }
        handler.postDelayed(new ProgressChecker(), 15000);
    }

    public void toggleFullscreen(View view) {
        //if(player == null)  return;
        //if(!player.isPlaying()) return;
        Log.d("EXOPLAYER", "CRASH UPCOMING????????");
        if(getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
    }
    public void cast(View view) {
        CastContext castContext = CastContext.getSharedInstance(getApplicationContext());

    }

    public void skip90(View view) {
        player.seekTo(player.getCurrentPosition() + 85000L);
    }

    private class ProgressChecker implements Runnable {
        @Override
        public void run() {
            if(player != null && id != null) {
                playbackPosition = player.getCurrentPosition();
                FirebaseDBHelper.getPlayBackPositionRef(id).child(episodeNum).setValue(String.valueOf(playbackPosition));
                handler.postDelayed(this, 10000);
            }
        }
    }

}