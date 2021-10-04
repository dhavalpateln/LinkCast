package com.dhavalpateln.linkcast.exoplayer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.dhavalpateln.linkcast.R;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;

import java.util.HashMap;
import java.util.Map;


public class ExoPlayerActivity extends AppCompatActivity {

    private PlayerView exoplayerView;
    private SimpleExoPlayer player;
    private boolean playWhenReady = true;
    private int currentWindow = 0;
    private long playbackPosition = 0L;
    private ImageView fullScreenIcon;


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
    }

    @Override
    protected void onPause() {
        super.onPause();
        release();
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
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

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
        }
        player = null;
    }

    private void initPlayer() {

        Intent intent = getIntent();
        String url = intent.getStringExtra("url");

        Map<String, String> headerMap = new HashMap<>();
        if(intent.hasExtra("Referer")) {
            headerMap.put("Referer", intent.getStringExtra("Referer"));
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

    }

    public void toggleFullscreen(View view) {
        if(getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
    }

    public void skip90(View view) {
        player.seekTo(player.getCurrentPosition() + 90000L);
    }
}