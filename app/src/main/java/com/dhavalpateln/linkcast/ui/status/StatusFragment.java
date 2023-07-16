package com.dhavalpateln.linkcast.ui.status;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.content.Context;
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

import com.dhavalpateln.linkcast.R;
import com.dhavalpateln.linkcast.database.VideoURLData;
import com.dhavalpateln.linkcast.database.AnimeLinkData;
import com.dhavalpateln.linkcast.extractors.AnimeExtractor;
import com.dhavalpateln.linkcast.extractors.AnimeMangaSearch;
import com.dhavalpateln.linkcast.extractors.MangaExtractor;
import com.dhavalpateln.linkcast.extractors.animepahe.AnimePaheExtractor;
import com.dhavalpateln.linkcast.extractors.animepahe.AnimePaheSearch;
import com.dhavalpateln.linkcast.extractors.gogoanime.GogoAnimeExtractor;
import com.dhavalpateln.linkcast.extractors.gogoanime.GogoAnimeSearch;
import com.dhavalpateln.linkcast.extractors.mangafourlife.MangaFourLifeExtractor;
import com.dhavalpateln.linkcast.extractors.mangafourlife.MangaFourLifeSearch;
import com.dhavalpateln.linkcast.extractors.mangareader.MangaReaderExtractor;
import com.dhavalpateln.linkcast.extractors.mangareader.MangaReaderSearch;
import com.dhavalpateln.linkcast.extractors.marin.MarinExtractor;
import com.dhavalpateln.linkcast.extractors.marin.MarinSearch;
import com.dhavalpateln.linkcast.extractors.zoro.ZoroExtractor;
import com.dhavalpateln.linkcast.extractors.zoro.ZoroSearch;
import com.dhavalpateln.linkcast.database.EpisodeNode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class StatusFragment extends Fragment {

    private Executor executor = Executors.newCachedThreadPool();
    private Handler uiHandler = new Handler(Looper.getMainLooper());
    private Context context;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_status, container, false);
        return root;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        LinearLayout statusContainer = view.findViewById(R.id.status_container_linear_layout);

        //gogoanimeTest(statusContainer);
        //zoroTest(statusContainer);
        //nineanimeTest(statusContainer);
        //animepaheTest(statusContainer);
        //manga4lifeTest(statusContainer);

        genericTest(statusContainer, new GogoAnimeSearch(), new GogoAnimeExtractor());
        genericTest(statusContainer, new ZoroSearch(), new ZoroExtractor());
        //genericTest(statusContainer, new NineAnimeSearch(getContext()), new NineAnimeExtractor(getContext()));
        //genericTest(statusContainer, new TenshiSearch(), new TenshiExtractor());
        genericTest(statusContainer, new AnimePaheSearch(), new AnimePaheExtractor());
        genericTest(statusContainer, new MarinSearch(), new MarinExtractor());
        //genericTest(statusContainer, new CrunchyrollSearch(), new CrunchyrollExtractor());

        genericTest(statusContainer, new MangaReaderSearch(), new MangaReaderExtractor());
        genericTest(statusContainer, new MangaFourLifeSearch(), new MangaFourLifeExtractor());
    }

    private void genericTest(LinearLayout container, AnimeMangaSearch searcher, AnimeExtractor extractor) {
        ConstraintLayout status = generateStatusView(extractor.getDisplayName());
        container.addView(status);
        executor.execute(() -> {
            try {
                boolean successTest = false;
                if (searcher.requiresInit()) searcher.init();
                List<AnimeLinkData> searchResult = searcher.search("hero academia");
                if (!searchResult.isEmpty()) {
                    List<EpisodeNode> episodes = extractor.extractData(searchResult.get(0));
                    if (!episodes.isEmpty()) {
                        List<VideoURLData> links = new ArrayList<>();
                        extractor.extractEpisodeUrls(episodes.get(episodes.size() - 1).getUrl(), links);
                        successTest = !links.isEmpty();
                    }
                }
                boolean finalSuccessTest = successTest;
                uiHandler.post(() -> markStatus(status, finalSuccessTest));
            } catch (Exception e) {
                e.printStackTrace();
                uiHandler.post(() -> markStatus(status, false));
            }
        });
    }

    private void genericTest(LinearLayout container, AnimeMangaSearch searcher, MangaExtractor extractor) {
        ConstraintLayout status = generateStatusView(extractor.getDisplayName());
        container.addView(status);
        executor.execute(() -> {
            boolean successTest = false;
            if(searcher.requiresInit()) searcher.init();
            List<AnimeLinkData> searchResult = searcher.search("hero academia");
            if(!searchResult.isEmpty()) {
                List<EpisodeNode> chapters = extractor.getChapters(searchResult.get(0).getUrl());
                if(!chapters.isEmpty()) {
                    List<String> pages = extractor.getPages(chapters.get(0).getUrl());
                    successTest = !pages.isEmpty();
                }
            }
            boolean finalSuccessTest = successTest;
            uiHandler.post(() -> markStatus(status, finalSuccessTest));
        });
    }



    private List<EpisodeNode> browse(AnimeMangaSearch searcher, AnimeExtractor extractor, String searchTerm) {
        List<AnimeLinkData> searchResult = searcher.search(searchTerm);
        return extractor.extractData(searchResult.get(0));
    }
    private List<EpisodeNode> browse(AnimeMangaSearch searcher, MangaExtractor extractor, String searchTerm) {
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