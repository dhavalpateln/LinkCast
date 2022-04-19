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
import com.dhavalpateln.linkcast.animescrappers.AnimePaheExtractor;
import com.dhavalpateln.linkcast.animescrappers.GogoAnimeExtractor;
import com.dhavalpateln.linkcast.animescrappers.NineAnimeExtractor;
import com.dhavalpateln.linkcast.animescrappers.VideoURLData;
import com.dhavalpateln.linkcast.animesearch.AnimePaheSearch;
import com.dhavalpateln.linkcast.animesearch.GogoAnimeSearch;
import com.dhavalpateln.linkcast.animesearch.NineAnimeSearch;
import com.dhavalpateln.linkcast.database.AnimeLinkData;
import com.dhavalpateln.linkcast.database.FirebaseDBHelper;
import com.dhavalpateln.linkcast.utils.EpisodeNode;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
        nineanimeTest(statusContainer);
        animepaheTest(statusContainer);
    }

    private void gogoanimeTest(LinearLayout container) {
        LinearLayout divider = generateHeaderView(ProvidersData.GOGOANIME.NAME);
        ConstraintLayout browsingStatus = generateStatusView("Browsing");
        ConstraintLayout gogoplayStatus = generateStatusView("GogoPlay");
        ConstraintLayout sbStatus = generateStatusView("StreamSB");
        container.addView(divider);
        container.addView(browsingStatus);
        container.addView(gogoplayStatus);
        container.addView(sbStatus);

        executor.execute(() -> {
            boolean searchSuccess = false;
            boolean episodeListSuccess = false;
            try {
                GogoAnimeExtractor extractor = new GogoAnimeExtractor();
                GogoAnimeSearch searcher = new GogoAnimeSearch();

                searchSuccess = !searcher.search("hero academia").isEmpty();
                episodeListSuccess = !extractor.getEpisodeList("https://gogoanime.fi/category/one-piece").isEmpty();

                boolean finalSearchSuccess = searchSuccess;
                boolean finalEpisodeListSuccess = episodeListSuccess;
                uiHandler.post(() -> markStatus(browsingStatus, finalSearchSuccess && finalEpisodeListSuccess));

                List<VideoURLData> episodeURLs = new ArrayList<>();
                extractor.extractEpisodeUrls("https://gogoanime.fi/boku-no-hero-academia-episode-13", episodeURLs);
                Set<String> extractedSources = new HashSet<>();
                for (VideoURLData videoURLData : episodeURLs) {
                    extractedSources.add(videoURLData.getSource().toLowerCase());
                }
                uiHandler.post(() -> {
                    markStatus(gogoplayStatus, extractedSources.contains("gogoplay"));
                    markStatus(sbStatus, extractedSources.contains("streamsb"));
                });
            } catch (Exception e) {
                boolean finalEpisodeListSuccess1 = episodeListSuccess;
                boolean finalSearchSuccess1 = searchSuccess;
                uiHandler.post(() -> {
                    markStatus(browsingStatus, finalSearchSuccess1 && finalEpisodeListSuccess1);
                    markStatus(gogoplayStatus, false);
                    markStatus(sbStatus, false);
                });
            }
        });
    }

    private void nineanimeTest(LinearLayout container) {
        LinearLayout divider = generateHeaderView(ProvidersData.NINEANIME.NAME);
        ConstraintLayout browsingStatus = generateStatusView("Browsing");
        ConstraintLayout vidstream = generateStatusView(ProvidersData.VIDSTREAM.NAME);
        ConstraintLayout mcloud = generateStatusView(ProvidersData.MCLOUD.NAME);
        ConstraintLayout streamtape = generateStatusView(ProvidersData.STREAMTAPE.NAME);
        container.addView(divider);
        container.addView(browsingStatus);
        container.addView(vidstream);
        container.addView(mcloud);
        container.addView(streamtape);

        Executors.newSingleThreadExecutor().execute(() -> {
            boolean searchSuccess = false;
            boolean episodeListSuccess = false;
            try {
                NineAnimeExtractor extractor = new NineAnimeExtractor(getActivity());
                NineAnimeSearch searcher = new NineAnimeSearch(getActivity());

                searcher.init();
                searchSuccess = !searcher.search("hero academia").isEmpty();
                List<EpisodeNode> episodeList = extractor.getEpisodeList("https://9anime.to/watch/my-hero-academia-season-5.653z");
                episodeListSuccess = !episodeList.isEmpty();

                boolean finalSearchSuccess = searchSuccess;
                boolean finalEpisodeListSuccess = episodeListSuccess;
                uiHandler.post(() -> markStatus(browsingStatus, finalSearchSuccess && finalEpisodeListSuccess));

                List<VideoURLData> episodeURLs = new ArrayList<>();
                extractor.extractEpisodeUrls(episodeList.get(0).getUrl(), episodeURLs);
                Set<String> extractedSources = new HashSet<>();
                for (VideoURLData videoURLData : episodeURLs) {
                    extractedSources.add(videoURLData.getSource());
                }
                uiHandler.post(() -> {
                    markStatus(vidstream, extractedSources.contains(ProvidersData.VIDSTREAM.NAME));
                    markStatus(mcloud, extractedSources.contains(ProvidersData.MCLOUD.NAME));
                    markStatus(streamtape, extractedSources.contains(ProvidersData.STREAMTAPE.NAME));
                });
            } catch (Exception e) {
                e.printStackTrace();
                markStatus(browsingStatus, searchSuccess && episodeListSuccess);
                markStatus(vidstream, false);
                markStatus(mcloud, false);
                markStatus(streamtape, false);
            }
        });
    }

    private void animepaheTest(LinearLayout container) {
        LinearLayout divider = generateHeaderView(ProvidersData.ANIMEPAHE.NAME);
        ConstraintLayout browsingStatus = generateStatusView("Browsing");
        ConstraintLayout kwik = generateStatusView("Kwik");
        container.addView(divider);
        container.addView(browsingStatus);
        container.addView(kwik);

        Executors.newSingleThreadExecutor().execute(() -> {
            boolean searchSuccess = false;
            boolean episodeListSuccess = false;
            try {
                AnimePaheExtractor extractor = new AnimePaheExtractor();
                AnimePaheSearch searcher = new AnimePaheSearch();

                searchSuccess = !searcher.search("hero academia").isEmpty();
                episodeListSuccess = !extractor.getEpisodeList("https://animepahe.com/api?m=release&id=82713178-9273-583d-0074-d47fa8d57a9b&sort=episode_asc").isEmpty();

                boolean finalSearchSuccess = searchSuccess;
                boolean finalEpisodeListSuccess = episodeListSuccess;
                uiHandler.post(() -> markStatus(browsingStatus, finalSearchSuccess && finalEpisodeListSuccess));

                List<VideoURLData> episodeURLs = new ArrayList<>();
                extractor.extractEpisodeUrls("https://animepahe.com/api?m=release&id=82713178-9273-583d-0074-d47fa8d57a9b&sort=episode_asc&page=1:::1", episodeURLs);
                Set<String> extractedSources = new HashSet<>();
                for (VideoURLData videoURLData : episodeURLs) {
                    extractedSources.add(videoURLData.getSource().toLowerCase());
                }
                uiHandler.post(() -> {
                    markStatus(kwik, !extractedSources.isEmpty());
                });
            } catch (Exception e) {
                boolean finalSearchSuccess1 = searchSuccess;
                boolean finalEpisodeListSuccess1 = episodeListSuccess;
                uiHandler.post(() -> {
                    markStatus(browsingStatus, finalSearchSuccess1 && finalEpisodeListSuccess1);
                    markStatus(kwik, false);
                });
            }
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