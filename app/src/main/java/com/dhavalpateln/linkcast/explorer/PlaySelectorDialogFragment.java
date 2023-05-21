package com.dhavalpateln.linkcast.explorer;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;
import androidx.mediarouter.app.MediaRouteButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dhavalpateln.linkcast.R;
import com.dhavalpateln.linkcast.database.AnimeLinkData;
import com.dhavalpateln.linkcast.database.EpisodeNode;
import com.dhavalpateln.linkcast.database.FirebaseDBHelper;
import com.dhavalpateln.linkcast.database.TvActionData;
import com.dhavalpateln.linkcast.database.VideoURLData;
import com.dhavalpateln.linkcast.database.room.animelinkcache.LinkWithAllData;
import com.dhavalpateln.linkcast.dialogs.LinkCastDialog;
import com.dhavalpateln.linkcast.exoplayer.ExoPlayerActivity;
import com.dhavalpateln.linkcast.explorer.listeners.TaskCompleteListener;
import com.dhavalpateln.linkcast.explorer.listeners.VideoSelectedListener;
import com.dhavalpateln.linkcast.explorer.listeners.VideoServerListener;
import com.dhavalpateln.linkcast.explorer.tasks.ExtractVideoServers;
import com.dhavalpateln.linkcast.extractors.AnimeExtractor;
import com.dhavalpateln.linkcast.utils.Utils;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.ext.cast.CastPlayer;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastState;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class PlaySelectorDialogFragment extends DialogFragment {

    private VideoURLData videoURLData;
    private boolean shouldAskResume;
    private LinkWithAllData animeData;
    private MediaRouteButton mediaRouteButton;

    public PlaySelectorDialogFragment(VideoURLData videoURLData, LinkWithAllData animeData, boolean shouldAskResume) {
        this.videoURLData = videoURLData;
        this.animeData = animeData;
        this.shouldAskResume = shouldAskResume;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.small_dialog_70);
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        ConstraintLayout dialogView = (ConstraintLayout) inflater.inflate(R.layout.dialog_play_selector, null);

        mediaRouteButton = dialogView.findViewById(R.id.mediaRouteButton);
        dialogView.findViewById(R.id.video_play_button).setOnClickListener(v -> play());
        dialogView.findViewById(R.id.video_cast_button).setOnClickListener(v -> chromeCast());
        dialogView.findViewById(R.id.video_app_cast_button).setOnClickListener(v -> appCast());

        CastButtonFactory.setUpMediaRouteButton(getContext(), dialogView.findViewById(R.id.mediaRouteButton));

        builder.setView(dialogView);
        Dialog dialog = builder.create();

        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        return dialog;
    }

    private void play() {
        Intent intent = ExoPlayerActivity.prepareIntent(
                getActivity().getApplicationContext(),
                animeData,
                videoURLData,
                this.videoURLData.getEpisodeNum(),
                this.shouldAskResume
        );
        startActivity(intent);
        dismiss();
    }

    private void chromeCast() {
        CastContext castContext = CastContext.getSharedInstance();
        if(castContext.getCastState() == CastState.CONNECTED) {
            CastPlayer castPlayer = new CastPlayer(castContext);

            String mimeType = MimeTypes.VIDEO_MP4;

            MediaItem mediaItem = new MediaItem.Builder()
                    .setUri(this.videoURLData.getUrl())
                    .setMimeType(mimeType)
                    .build();
            castPlayer.setMediaItem(mediaItem);

            castPlayer.setPlayWhenReady(true);
            castPlayer.prepare();
            dismiss();
        }
        else {
            mediaRouteButton.performClick();
            Toast.makeText(getContext(), "Connect to chromecast first", Toast.LENGTH_LONG).show();
        }
        //Log.d("CHROMECASTTTTTTTTTTT", "button click event");
    }

    private void appCast() {
        TvActionData tvActionData = new TvActionData();
        tvActionData.setTimestamp(Utils.getCurrentTime());
        tvActionData.setAction("Play");
        tvActionData.setVideoData(videoURLData);
        tvActionData.setEpisodeNum(videoURLData.getEpisodeNum());
        tvActionData.setId(animeData.getId());
        tvActionData.setResumeOption(this.shouldAskResume);
        FirebaseDBHelper.getUserTvPlay().setValue(tvActionData);
        dismiss();
    }
}
