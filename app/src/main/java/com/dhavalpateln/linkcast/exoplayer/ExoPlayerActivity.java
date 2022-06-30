package com.dhavalpateln.linkcast.exoplayer;

import static com.google.android.exoplayer2.ui.CaptionStyleCompat.EDGE_TYPE_OUTLINE;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.UiModeManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.dhavalpateln.linkcast.R;
import com.dhavalpateln.linkcast.database.VideoURLData;
import com.dhavalpateln.linkcast.database.AnimeLinkData;
import com.dhavalpateln.linkcast.database.FirebaseDBHelper;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.TracksInfo;
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.ui.CaptionStyleCompat;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.exoplayer2.ui.TrackSelectionDialogBuilder;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.util.Log;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.gms.cast.framework.CastContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ExoPlayerActivity extends AppCompatActivity implements Player.Listener {

    public static final String ID = "id";
    public static final String EPISODE_NUM = "num";
    public static final String LAST_VIEW_POINT = "last_point";
    public static final String MEDIA_URL = "url";
    public static final String FILE_TYPE = "file_type";
    private static final String VIDEO_DATA = "videodata";

    private final String ARG_FIRST_LOAD = "firstload";
    private final String ARG_PLAYBACK_POSITION = "position";
    private final String ARG_LOCKED = "locked";
    private final String ARG_IS_PLAYING = "playing";

    public enum FileTypes {
        LOCAL,
        URL
    }

    private StyledPlayerView exoplayerView;
    private ExoPlayer player;
    private ImageButton screenRotateButton;
    private ImageButton exoPlayButton;
    private ImageButton exoQualityButton;
    private ExtendedTimeBar timebar;
    private ImageButton lockButton;
    private ImageButton unLockButton;
    private View container;
    private View screen;

    private VideoURLData videoURLData;

    private DefaultTrackSelector trackSelector;
    private boolean playWhenReady = true;
    private boolean isInitialized = false;
    private boolean isPlaying = true;
    private boolean isLocked = false;
    private int currentWindow = 0;
    private long playbackPosition = 0L;
    private ImageView fullScreenIcon;
    private boolean saveProgress = true;
    private String id = null;
    private String episodeNum = null;
    private boolean firstLoad = false;
    private boolean isTvApp = false;
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
        outState.putBoolean(ARG_LOCKED, isLocked);
        outState.putBoolean(ARG_IS_PLAYING, isPlaying);
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
            screenRotateButton.setImageResource(R.drawable.exo_styled_controls_fullscreen_exit);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            //Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
            screenRotateButton.setImageResource(R.drawable.exo_styled_controls_fullscreen_enter);
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState != null) {
            firstLoad = savedInstanceState.getBoolean(ARG_FIRST_LOAD, false);
            playbackPosition = savedInstanceState.getLong(ARG_PLAYBACK_POSITION, 0);
            isLocked = savedInstanceState.getBoolean(ARG_LOCKED);
            isPlaying = savedInstanceState.getBoolean(ARG_IS_PLAYING);
        }

        setContentView(R.layout.activity_exo_player);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        fullScreenIcon = findViewById(R.id.exo_fullscreen);
        exoplayerView = findViewById(R.id.exoplayer);
        exoPlayButton = findViewById(R.id.exo_play);
        exoQualityButton = findViewById(R.id.exo_quality);
        container = findViewById(R.id.exo_controller_cont);
        screen = findViewById(R.id.exo_black_screen);
        lockButton = findViewById(R.id.exo_lock);
        unLockButton = findViewById(R.id.exo_unlock);
        timebar = findViewById(R.id.exo_progress);


        videoURLData = (VideoURLData) getIntent().getSerializableExtra(VIDEO_DATA);

        screenRotateButton = findViewById(R.id.exo_screen);
        findViewById(R.id.exo_skip).setOnClickListener(v -> {
            skip90(v);
        });

        exoplayerView.setKeepScreenOn(true);
        exoplayerView.getSubtitleView().setStyle(new CaptionStyleCompat(
                Color.WHITE,
                Color.TRANSPARENT,
                Color.TRANSPARENT,
                EDGE_TYPE_OUTLINE,
                Color.BLACK,
                null
        ));
        exoplayerView.getSubtitleView().setFixedTextSize(TypedValue.COMPLEX_UNIT_SP, 24f);
        //exoplayerView.getSubtitleView().offsetTopAndBottom(10);


        screenRotateButton.setOnClickListener(v -> {
            toggleFullscreen(v);
        });

        exoPlayButton.setOnClickListener(v -> {
            if(isInitialized) {
                isPlaying = player.isPlaying();
                //((AnimatedVectorDrawable) exoPlayButton.getDrawable()).start();
                if(isPlaying) {
                    exoPlayButton.setImageDrawable(getResources().getDrawable(R.drawable.exo_icon_play));
                    player.pause();
                } else {
                    exoPlayButton.setImageDrawable(getResources().getDrawable(R.drawable.exo_icon_pause));
                    player.play();
                }
            }
        });

        findViewById(R.id.exo_fast_forward_button).setOnClickListener(v -> {
            if(isInitialized) {
                player.seekTo(player.getCurrentPosition() + 10000L);
            }
        });

        findViewById(R.id.exo_fast_rewind_button).setOnClickListener(v -> {
            if(isInitialized) {
                player.seekTo(player.getCurrentPosition() - 15000L);
            }
        });

        findViewById(R.id.exo_back).setOnClickListener(v -> {
            onBackPressed();
        });

        lockButton.setOnClickListener(v -> {
            isLocked = true;
            screen.setVisibility(View.GONE);
            container.setVisibility(View.GONE);
            timebar.setForceDisabled(true);
            unLockButton.setVisibility(View.VISIBLE);
        });

        unLockButton.setOnClickListener(v -> {
            isLocked = false;
            unLockButton.setVisibility(View.GONE);
            screen.setVisibility(View.VISIBLE);
            container.setVisibility(View.VISIBLE);
            timebar.setForceDisabled(false);
        });

        handler.postDelayed(new ProgressChecker(), 15000);

        UiModeManager uiModeManager = (UiModeManager) getSystemService(UI_MODE_SERVICE);
        if (uiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_TELEVISION) {
            isTvApp = true;
            lockButton.setVisibility(View.GONE);
            findViewById(R.id.exo_playback_speed).setVisibility(View.GONE);
            screenRotateButton.setVisibility(View.GONE);
            findViewById(R.id.exo_back).setVisibility(View.GONE);
        }

        //exoplayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
    }

    private void release() {
        if(player != null) {
            this.playWhenReady = player.getPlayWhenReady();
            this.currentWindow = player.getCurrentWindowIndex();
            this.playbackPosition = player.getCurrentPosition();
            player.release();
            isInitialized = false;
            if(saveProgress && id != null && episodeNum != null) {
                FirebaseDBHelper.getPlayBackPositionRef(id).child(episodeNum).setValue(String.valueOf(playbackPosition));
            }
        }
        player = null;
    }

    private void initPlayer() {

        Intent intent = getIntent();
        String url = videoURLData.getUrl();
        if(intent.hasExtra(ID)) id = intent.getStringExtra(ID);
        if(intent.hasExtra(EPISODE_NUM)) episodeNum = intent.getStringExtra(EPISODE_NUM);



        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/95.0.4638.69 Safari/537.36");
        if(intent.hasExtra("Referer")) {
            headerMap.put("Referer", intent.getStringExtra("Referer"));

        }

        if(videoURLData.getHeaders() != null) {
            for (Map.Entry<String, String> entry : videoURLData.getHeaders().entrySet()) {
                headerMap.put(entry.getKey(), entry.getValue());
            }
        }




        DataSource.Factory dataSourceFactory = new DefaultHttpDataSource.Factory().setDefaultRequestProperties(headerMap);

        trackSelector = new DefaultTrackSelector(this);

        player = new ExoPlayer.Builder(getApplicationContext())
                .setTrackSelector(trackSelector)
                .setMediaSourceFactory(new DefaultMediaSourceFactory(dataSourceFactory))
                .build();



        exoplayerView.setPlayer(player);

        // Build the media item.
        //MediaItem mediaItem = MediaItem.fromUri(Uri.parse(url));

        MediaItem.Builder mediaItemBuilder = new MediaItem.Builder();
        mediaItemBuilder.setUri(Uri.parse(url));

        if(videoURLData.hasSubtitles()) {

            String mimeType = MimeTypes.TEXT_UNKNOWN;
            String subtitleURL = videoURLData.getSubtitles().get(0);

            if(subtitleURL.contains(".vtt"))    mimeType = MimeTypes.TEXT_VTT;
            else if(subtitleURL.contains(".ass") || subtitleURL.contains(".ssa"))   mimeType = MimeTypes.TEXT_SSA;

            MediaItem.SubtitleConfiguration subtitle = new MediaItem.SubtitleConfiguration.Builder(Uri.parse(videoURLData.getSubtitles().get(0)))
                    .setSelectionFlags(C.SELECTION_FLAG_FORCED)
                    .setMimeType(mimeType)
                    .build();

            List<MediaItem.SubtitleConfiguration> subtitleConfigurationList = new ArrayList<>();
            subtitleConfigurationList.add(subtitle);
            mediaItemBuilder.setSubtitleConfigurations(subtitleConfigurationList);

        }

        // Set the media item to be played.
        player.setMediaItem(mediaItemBuilder.build());
        player.setPlayWhenReady(playWhenReady);
        player.seekTo(currentWindow, playbackPosition);

        // Prepare the player.
        player.prepare();
        player.addListener(this);

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
        isInitialized = true;
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
            if(player != null && id != null && isInitialized) {
                playbackPosition = player.getCurrentPosition();
                FirebaseDBHelper.getPlayBackPositionRef(id).child(episodeNum).setValue(String.valueOf(playbackPosition));
                handler.postDelayed(this, 10000);
            }
        }
    }

    private boolean isVideoRenderer(MappingTrackSelector.MappedTrackInfo mappedTrackInfo, int rendererIndex) {
        if (mappedTrackInfo.getTrackGroups(rendererIndex).length == 0) return false;
        return C.TRACK_TYPE_VIDEO == mappedTrackInfo.getRendererType(rendererIndex);
    }

    @Override
    public void onTracksInfoChanged(TracksInfo tracksInfo) {
        if(tracksInfo.getTrackGroupInfos().size() > 2) {
            exoQualityButton.setVisibility(View.VISIBLE);
            exoQualityButton.setOnClickListener(v -> {
                MappingTrackSelector.MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();
                int rendererIndex = 0;
                for(int i = 0; i < mappedTrackInfo.getRendererCount(); i++) {
                    if(isVideoRenderer(mappedTrackInfo, i)) {
                        rendererIndex = i;
                    }
                }
                TrackSelectionDialogBuilder trackSelectionDialogBuilder = new TrackSelectionDialogBuilder(this, "Select", trackSelector, rendererIndex);
                trackSelectionDialogBuilder.build().show();
            });
        }
    }

    @Override
    public void onPlayerError(PlaybackException error) {
        Toast.makeText(this, error.getMessage(), Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if(isTvApp) {
            exoplayerView.showController();
            switch (event.getAction()) {
                /*case KeyEvent.KEYCODE_MEDIA_PAUSE:
                    player.pause();
                    return true;
                case KeyEvent.KEYCODE_MEDIA_PLAY:
                    player.play();
                    return true;*/
                default:
                    return super.dispatchKeyEvent(event);
            }
        }
        return super.dispatchKeyEvent(event);
    }

    public static Intent prepareIntent(Context context, AnimeLinkData animeData, VideoURLData videoURLData, String episodeNum) {

        //videoURLData = new VideoURLData("animepahe", "title", "https://v.vrv.co/evs3/d6eaa21935ca301f291cddc347bf287b/assets/2fitb7a8qgywrac_,1890521.mp4,1890515.mp4,1890509.mp4,.urlset/master.m3u8?Policy=eyJTdGF0ZW1lbnQiOlt7IlJlc291cmNlIjoiaHR0cCo6Ly92LnZydi5jby9ldnMzL2Q2ZWFhMjE5MzVjYTMwMWYyOTFjZGRjMzQ3YmYyODdiL2Fzc2V0cy8yZml0YjdhOHFneXdyYWNfLDE4OTA1MjEubXA0LDE4OTA1MTUubXA0LDE4OTA1MDkubXA0LC51cmxzZXQvbWFzdGVyLm0zdTgiLCJDb25kaXRpb24iOnsiRGF0ZUxlc3NUaGFuIjp7IkFXUzpFcG9jaFRpbWUiOjE2NTQ5MjkwODF9fX1dfQ__&Signature=At5k4Q8j6ULTzlJ8MKzLtSpewh~tr5FOv~mXuf7fLkmuAMv~iYX6HwYqImq3khEjGrYjCcdqqESBb8NxchXn1P7ePQORMk2sKHGowbh0UQHzS9RvwcSLL91lF3kmX98MgufRbV6glz1GbKD-yHPayzGtkdqk9bKfb~wN2hBivrmOg6ovpgfPQA1LrelvPYODnv0qBrX-RycnEwt2-iBNqBJVs9ykW-C3GEaHJlS93QvJdbph0ZVD8mw5jR0N4XMe0NEmWsCcgzYixxSIxWNzqxWAXeK2mJnjB~KG9PqJMzE0w7AFrNbmG4dwamMe9h2g~~o6xgvd6uhMMSpPao0NLw__&Key-Pair-Id=APKAJMWSQ5S7ZB3MF5VA", null);

        Intent intent = new Intent(context, ExoPlayerActivity.class);
        if(episodeNum != null) {
            episodeNum = episodeNum.replace(".", "dot");
            intent.putExtra(EPISODE_NUM, episodeNum);
        }
        intent.putExtra(VIDEO_DATA, videoURLData);
        intent.putExtra(MEDIA_URL, videoURLData.getUrl());
        intent.putExtra("saveProgress", true);
        if(animeData.getId() != null)  intent.putExtra("id", animeData.getId());
        return intent;
    }

    public static Intent prepareIntent(Context context, String id, VideoURLData videoURLData, String episodeNum) {

        //videoURLData = new VideoURLData("animepahe", "title", "https://v.vrv.co/evs3/d6eaa21935ca301f291cddc347bf287b/assets/2fitb7a8qgywrac_,1890521.mp4,1890515.mp4,1890509.mp4,.urlset/master.m3u8?Policy=eyJTdGF0ZW1lbnQiOlt7IlJlc291cmNlIjoiaHR0cCo6Ly92LnZydi5jby9ldnMzL2Q2ZWFhMjE5MzVjYTMwMWYyOTFjZGRjMzQ3YmYyODdiL2Fzc2V0cy8yZml0YjdhOHFneXdyYWNfLDE4OTA1MjEubXA0LDE4OTA1MTUubXA0LDE4OTA1MDkubXA0LC51cmxzZXQvbWFzdGVyLm0zdTgiLCJDb25kaXRpb24iOnsiRGF0ZUxlc3NUaGFuIjp7IkFXUzpFcG9jaFRpbWUiOjE2NTQ5MjkwODF9fX1dfQ__&Signature=At5k4Q8j6ULTzlJ8MKzLtSpewh~tr5FOv~mXuf7fLkmuAMv~iYX6HwYqImq3khEjGrYjCcdqqESBb8NxchXn1P7ePQORMk2sKHGowbh0UQHzS9RvwcSLL91lF3kmX98MgufRbV6glz1GbKD-yHPayzGtkdqk9bKfb~wN2hBivrmOg6ovpgfPQA1LrelvPYODnv0qBrX-RycnEwt2-iBNqBJVs9ykW-C3GEaHJlS93QvJdbph0ZVD8mw5jR0N4XMe0NEmWsCcgzYixxSIxWNzqxWAXeK2mJnjB~KG9PqJMzE0w7AFrNbmG4dwamMe9h2g~~o6xgvd6uhMMSpPao0NLw__&Key-Pair-Id=APKAJMWSQ5S7ZB3MF5VA", null);

        Intent intent = new Intent(context, ExoPlayerActivity.class);
        if(episodeNum != null) {
            episodeNum = episodeNum.replace(".", "dot");
            intent.putExtra(EPISODE_NUM, episodeNum);
        }
        intent.putExtra(VIDEO_DATA, videoURLData);
        intent.putExtra(MEDIA_URL, videoURLData.getUrl());
        intent.putExtra("saveProgress", true);
        if(id != null)  intent.putExtra("id", id);
        return intent;
    }
}