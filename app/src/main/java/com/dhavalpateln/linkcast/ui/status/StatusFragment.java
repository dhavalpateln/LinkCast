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
import com.dhavalpateln.linkcast.animescrappers.AnimeScrapper;
import com.dhavalpateln.linkcast.animescrappers.GogoAnimeExtractor;
import com.dhavalpateln.linkcast.animescrappers.NineAnimeExtractor;
import com.dhavalpateln.linkcast.animescrappers.VideoURLData;
import com.dhavalpateln.linkcast.animescrappers.ZoroExtractor;
import com.dhavalpateln.linkcast.animesearch.AnimePaheSearch;
import com.dhavalpateln.linkcast.animesearch.AnimeSearch;
import com.dhavalpateln.linkcast.animesearch.GogoAnimeSearch;
import com.dhavalpateln.linkcast.animesearch.MangaFourLifeSearch;
import com.dhavalpateln.linkcast.animesearch.NineAnimeSearch;
import com.dhavalpateln.linkcast.animesearch.ZoroSearch;
import com.dhavalpateln.linkcast.database.AnimeLinkData;
import com.dhavalpateln.linkcast.database.FirebaseDBHelper;
import com.dhavalpateln.linkcast.mangascrappers.MangaFourLife;
import com.dhavalpateln.linkcast.mangascrappers.MangaScrapper;
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
        zoroTest(statusContainer);
        nineanimeTest(statusContainer);
        animepaheTest(statusContainer);
        manga4lifeTest(statusContainer);
    }

    private void gogoanimeTest(LinearLayout container) {
        LinearLayout divider = generateHeaderView(ProvidersData.GOGOANIME.NAME);
        ConstraintLayout browsingStatus = generateStatusView("Browsing");
        ConstraintLayout gogoplayStatus = generateStatusView("GogoPlay");
        ConstraintLayout sbStatus = generateStatusView("StreamSB");
        ConstraintLayout xStreamStatus = generateStatusView("XStream");
        container.addView(divider);
        container.addView(browsingStatus);
        container.addView(gogoplayStatus);
        container.addView(sbStatus);
        container.addView(xStreamStatus);

        executor.execute(() -> {
            boolean searchSuccess = false;
            boolean episodeListSuccess = false;
            try {
                GogoAnimeExtractor extractor = new GogoAnimeExtractor();
                GogoAnimeSearch searcher = new GogoAnimeSearch();

                List<EpisodeNode> episodeList = browse(searcher, extractor, "hero academia 5");
                episodeListSuccess = !episodeList.isEmpty();

                uiHandler.post(() -> markStatus(browsingStatus, !episodeList.isEmpty()));

                List<VideoURLData> episodeURLs = new ArrayList<>();
                extractor.extractEpisodeUrls(episodeList.get(0).getUrl(), episodeURLs);
                Set<String> extractedSources = new HashSet<>();
                for (VideoURLData videoURLData : episodeURLs) {
                    extractedSources.add(videoURLData.getSource().toLowerCase());
                }
                uiHandler.post(() -> {
                    markStatus(gogoplayStatus, extractedSources.contains("gogoplay"));
                    markStatus(sbStatus, extractedSources.contains("streamsb"));
                    markStatus(xStreamStatus, extractedSources.contains("xstream"));
                });
            } catch (Exception e) {
                boolean finalEpisodeListSuccess1 = episodeListSuccess;
                uiHandler.post(() -> {
                    markStatus(browsingStatus, finalEpisodeListSuccess1);
                    markStatus(gogoplayStatus, false);
                    markStatus(sbStatus, false);
                    markStatus(xStreamStatus, false);
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
            boolean browseSuccess = false;
            try {
                AnimePaheExtractor extractor = new AnimePaheExtractor();
                AnimePaheSearch searcher = new AnimePaheSearch();

                List<EpisodeNode> episodeList = browse(searcher, extractor, "hero academia");
                browseSuccess = !episodeList.isEmpty();

                uiHandler.post(() -> markStatus(browsingStatus, !episodeList.isEmpty()));

                List<VideoURLData> episodeURLs = new ArrayList<>();
                extractor.extractEpisodeUrls(episodeList.get(0).getUrl(), episodeURLs);
                Set<String> extractedSources = new HashSet<>();
                for (VideoURLData videoURLData : episodeURLs) {
                    extractedSources.add(videoURLData.getSource().toLowerCase());
                }
                uiHandler.post(() -> {
                    markStatus(kwik, !extractedSources.isEmpty());
                });
            } catch (Exception e) {
                boolean finalBrowseSuccess = browseSuccess;
                uiHandler.post(() -> {
                    markStatus(browsingStatus, finalBrowseSuccess);
                    markStatus(kwik, false);
                });
            }
        });
    }

    private void zoroTest(LinearLayout container) {
        LinearLayout divider = generateHeaderView(ProvidersData.ZORO.NAME);
        ConstraintLayout browsingStatus = generateStatusView("Browsing");
        ConstraintLayout rapidCloud = generateStatusView("RapidCloud");
        ConstraintLayout sbStatus = generateStatusView("StreamSB");
        ConstraintLayout streamTape = generateStatusView("StreamTape");
        container.addView(divider);
        container.addView(browsingStatus);
        container.addView(rapidCloud);
        container.addView(sbStatus);
        container.addView(streamTape);

        executor.execute(() -> {
            boolean searchSuccess = false;
            boolean episodeListSuccess = false;
            try {
                ZoroExtractor extractor = new ZoroExtractor();
                ZoroSearch searcher = new ZoroSearch();

                List<EpisodeNode> episodeList = browse(searcher, extractor, "hero academia 5");
                episodeListSuccess = !episodeList.isEmpty();

                uiHandler.post(() -> markStatus(browsingStatus, !episodeList.isEmpty()));

                List<VideoURLData> episodeURLs = new ArrayList<>();
                extractor.extractEpisodeUrls(episodeList.get(0).getUrl(), episodeURLs);
                Set<String> extractedSources = new HashSet<>();
                for (VideoURLData videoURLData : episodeURLs) {
                    extractedSources.add(videoURLData.getSource());
                }
                uiHandler.post(() -> {
                    markStatus(sbStatus, extractedSources.contains(ProvidersData.STREAMSB.NAME));
                    markStatus(streamTape, extractedSources.contains(ProvidersData.STREAMTAPE.NAME));
                    markStatus(rapidCloud, extractedSources.contains(ProvidersData.RAPIDCLOUD.NAME));
                });
            } catch (Exception e) {
                boolean finalEpisodeListSuccess1 = episodeListSuccess;
                uiHandler.post(() -> {
                    markStatus(browsingStatus, finalEpisodeListSuccess1);
                    markStatus(rapidCloud, false);
                    markStatus(sbStatus, false);
                    markStatus(streamTape, false);
                });
            }
        });
    }

    private void manga4lifeTest(LinearLayout container) {
        LinearLayout divider = generateHeaderView(ProvidersData.MANGAFOURLIFE.NAME);
        ConstraintLayout browsingStatus = generateStatusView("Browsing");
        ConstraintLayout mangas = generateStatusView("Manga");
        container.addView(divider);
        container.addView(browsingStatus);
        container.addView(mangas);

        Executors.newSingleThreadExecutor().execute(() -> {
            boolean episodeListSuccess = false;
            try {
                MangaFourLife extractor = new MangaFourLife();
                MangaFourLifeSearch searcher = new MangaFourLifeSearch();

                searcher.init();
                List<EpisodeNode> episodeList = browse(searcher, extractor, "my hero academia");
                episodeListSuccess = !episodeList.isEmpty();

                boolean finalEpisodeListSuccess = episodeListSuccess;
                uiHandler.post(() -> markStatus(browsingStatus, finalEpisodeListSuccess));

                List<String> imageURLs = extractor.getPages(episodeList.get(0).getUrl());

                uiHandler.post(() -> {
                    markStatus(mangas, !imageURLs.isEmpty());
                });
            } catch (Exception e) {
                e.printStackTrace();
                markStatus(browsingStatus, episodeListSuccess);
                markStatus(mangas, false);
            }
        });
    }


    private List<EpisodeNode> browse(AnimeSearch searcher, AnimeScrapper extractor, String searchTerm) {
        List<AnimeLinkData> searchResult = searcher.search(searchTerm);
        return extractor.extractData(searchResult.get(0));
    }
    private List<EpisodeNode> browse(AnimeSearch searcher, MangaScrapper extractor, String searchTerm) {
        List<AnimeLinkData> searchResult = searcher.search(searchTerm);
        return extractor.getChapters(searchResult.get(0).getUrl());
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