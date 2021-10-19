package com.dhavalpateln.linkcast.exoplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.dhavalpateln.linkcast.R;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ext.cast.CastPlayer;
import com.google.android.exoplayer2.ext.cast.SessionAvailabilityListener;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.dynamite.DynamiteModule;

public class ExoPlayerCastActivity extends AppCompatActivity implements Player.Listener, SessionAvailabilityListener {

    private PlayerControlView castControlView;
    private CastContext castContext;
    private CastPlayer castPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exo_player_cast);

        try {
            castContext = CastContext.getSharedInstance(this);
        } catch (RuntimeException e) {
            Throwable cause = e.getCause();
            while (cause != null) {
                if (cause instanceof DynamiteModule.LoadingException) {
                    setContentView(R.layout.cast_context_error);
                    return;
                }
                cause = cause.getCause();
            }
            // Unknown error. We propagate it.
            throw e;
        }

        CastButtonFactory.setUpMediaRouteButton(getApplicationContext(), findViewById(R.id.mediaRouteButton));
        castControlView = findViewById(R.id.cast_control_view);

        castPlayer = new CastPlayer(castContext);
        castPlayer.addListener(this);
        castPlayer.setSessionAvailabilityListener(this);
        castControlView.setPlayer(castPlayer);


    }

    @Override
    public void onCastSessionAvailable() {
        setCurrentPlayer(castPlayer);
    }

    @Override
    public void onCastSessionUnavailable() {

    }

    private void setCurrentPlayer(Player currentPlayer) {
        currentPlayer = castPlayer;

        // View management.
        castControlView.show();


        // Player state management.
        long playbackPositionMs = C.TIME_UNSET;
        int windowIndex = C.INDEX_UNSET;
        boolean playWhenReady = true;

        // Media queue management.
        MediaItem mediaItem = MediaItem.fromUri(Uri.parse("https://www151.sbcdnvideo.com/tysxexgfrg66j6cdadmbvrszf2km7mk43e76xkfc76474thf7o4ro32qlcza/boku-no-hero-academia-5th-season-episode-1.mp4"));
        mediaItem = new MediaItem.Builder()
                .setUri("https://www151.sbcdnvideo.com/tysxexgfrg66j6cdadmbvrszf2km7mk43e76xkfc76474thf7o4ro32qlcza/boku-no-hero-academia-5th-season-episode-1.mp4")
                .setMimeType(MimeTypes.VIDEO_MP4)
                .build();
        currentPlayer.setMediaItem(mediaItem);
        currentPlayer.setPlayWhenReady(playWhenReady);

        currentPlayer.prepare();
    }

}