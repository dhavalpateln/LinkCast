package com.dhavalpateln.linkcast.ui.status;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dhavalpateln.linkcast.ProvidersData;
import com.dhavalpateln.linkcast.R;
import com.dhavalpateln.linkcast.animescrappers.GogoAnimeExtractor;
import com.dhavalpateln.linkcast.animescrappers.VideoURLData;
import com.dhavalpateln.linkcast.database.FirebaseDBHelper;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class StatusFragment extends Fragment {

    private Executor executor = Executors.newSingleThreadExecutor();
    private Handler uiHandler = new Handler(Looper.getMainLooper());

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_status, container, false);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        LinearLayout statusContainer = view.findViewById(R.id.status_container_linear_layout);

        gogoanimeTest(statusContainer);
    }

    private void gogoanimeTest(LinearLayout container) {
        LinearLayout divider = generateHeaderView(ProvidersData.GOGOANIME.NAME);
        ConstraintLayout gogoplayStatus = generateStatusView("GogoPlay");
        ConstraintLayout sbStatus = generateStatusView("StreamSB");
        container.addView(divider);
        container.addView(gogoplayStatus);
        container.addView(sbStatus);

        executor.execute(() -> {
            GogoAnimeExtractor extractor = new GogoAnimeExtractor();
            List<VideoURLData> episodeURLs = new ArrayList<>();
            extractor.extractEpisodeUrls("https://gogoanime.fi/boku-no-hero-academia-episode-13", episodeURLs);
            Set<String> extractedSources = new HashSet<>();
            for(VideoURLData videoURLData: episodeURLs) {
                extractedSources.add(videoURLData.getSource().toLowerCase());
            }
            uiHandler.post(() -> {
                markStatus(gogoplayStatus, extractedSources.contains("gogoplay"));
                markStatus(sbStatus, extractedSources.contains("streamsb"));
            });
        });
    }

    private void markStatus(ConstraintLayout statusConstraintLayout, boolean success) {
        ImageView failImageView = statusConstraintLayout.findViewById(R.id.status_fail_image_view);
        ImageView successImageView = statusConstraintLayout.findViewById(R.id.status_success_image_view);
        ProgressBar progressBar = statusConstraintLayout.findViewById(R.id.status_progress_bar);
        progressBar.setVisibility(View.GONE);
        if(success) {
            failImageView.setVisibility(View.GONE);
            successImageView.setVisibility(View.VISIBLE);
        }
        else {
            failImageView.setVisibility(View.VISIBLE);
            successImageView.setVisibility(View.GONE);
        }
    }

    private LinearLayout generateHeaderView(String header) {
        LinearLayout linearLayout = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.fragment_status_header, null, false);
        TextView headerTextView = linearLayout.findViewById(R.id.status_header_text_view);
        headerTextView.setText(header);
        return linearLayout;
    }

    private ConstraintLayout generateStatusView(String title) {
        ConstraintLayout constraintLayout = (ConstraintLayout) getActivity().getLayoutInflater().inflate(R.layout.fragment_status_object, null, false);
        TextView titleTextView = constraintLayout.findViewById(R.id.status_title_text_view);
        titleTextView.setText(title);
        return constraintLayout;
    }

}