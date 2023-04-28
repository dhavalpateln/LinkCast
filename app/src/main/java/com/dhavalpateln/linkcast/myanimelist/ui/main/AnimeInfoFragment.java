package com.dhavalpateln.linkcast.myanimelist.ui.main;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dhavalpateln.linkcast.AnimeSearchActivity;
import com.dhavalpateln.linkcast.R;
import com.dhavalpateln.linkcast.manga.MangaReaderActivity;
import com.dhavalpateln.linkcast.myanimelist.AdvSearchParams;
import com.dhavalpateln.linkcast.myanimelist.MyAnimeListSearchActivity;
import com.dhavalpateln.linkcast.myanimelist.MyAnimelistAnimeData;
import com.dhavalpateln.linkcast.myanimelist.MyAnimelistInfoActivity;
import com.dhavalpateln.linkcast.myanimelist.adapters.SliderAdapter;
import com.dhavalpateln.linkcast.utils.SimpleHttpClient;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.material.divider.MaterialDivider;
import com.google.android.material.textview.MaterialTextView;
import com.smarteist.autoimageslider.SliderView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.HttpURLConnection;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AnimeInfoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AnimeInfoFragment extends Fragment {

    private final String TAG = "AnimeInfo";
    private SliderView sliderView;
    private ArrayList<String> sliderImageURLs;
    private SliderAdapter imageSliderAdapter;
    private LinearLayout infosLinearLayout;
    private LinearLayout infosContentLinearLayout;
    private MaterialTextView titleTextView;
    private LinearLayout relatedLinearLayout;
    private LinearLayout relatedContentLinearLayout;
    private MyAnimelistDataViewModel viewModel;
    private TextView scoreTextView;
    private TextView rankTextView;
    private TextView popularityTextView;
    private TextView englishTitleTextView;
    private Button searchAnimeButton;
    private FlexboxLayout genreFlexBox;

    public AnimeInfoFragment() {
        // Required empty public constructor
    }

    public static AnimeInfoFragment newInstance() {
        AnimeInfoFragment fragment = new AnimeInfoFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_anime_info, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sliderImageURLs = new ArrayList<>();

        titleTextView = view.findViewById(R.id.mal_info_anime_title);
        scoreTextView = view.findViewById(R.id.mal_score_text_view);
        rankTextView = view.findViewById(R.id.mal_rank_text_view);
        popularityTextView = view.findViewById(R.id.mal_popularity_text_view);
        infosLinearLayout = view.findViewById(R.id.mal_info_stats_linear_layout);
        infosContentLinearLayout = view.findViewById(R.id.mal_info_stats_content_linear_layout);
        englishTitleTextView = view.findViewById(R.id.mal_info_anime_english_title);
        searchAnimeButton = view.findViewById(R.id.mal_anime_search_button);
        sliderView = view.findViewById(R.id.anime_info_img_slider);
        genreFlexBox = view.findViewById(R.id.genres_flex_box_layout);

        imageSliderAdapter = new SliderAdapter(getActivity(), sliderImageURLs, (imageList, position) -> {
            Intent intent = new Intent(getActivity(), MangaReaderActivity.class);
            intent.putExtra(MangaReaderActivity.INTENT_REVERSE, false);
            intent.putExtra(MangaReaderActivity.INTENT_START_POSITION, position);
            intent.putExtra(MangaReaderActivity.INTENT_IMAGE_ARRAY, imageList.toArray(new String[0]));
            startActivity(intent);
        });

        sliderView.setAutoCycleDirection(SliderView.LAYOUT_DIRECTION_LTR);
        sliderView.setSliderAdapter(imageSliderAdapter);
        sliderView.setScrollTimeInSec(5);
        sliderView.setAutoCycle(true);
        sliderView.startAutoCycle();

        viewModel = new ViewModelProvider(getActivity()).get(MyAnimelistDataViewModel.class);
        viewModel.getData().observe(getViewLifecycleOwner(), myAnimelistAnimeData -> {
            if(myAnimelistAnimeData.getId() > 0) {
                /*for (String imageURL : myAnimelistAnimeData.getImages()) {
                    if (!sliderImageURLs.contains(imageURL)) sliderImageURLs.add(imageURL);
                }

                imageSliderAdapter.notifyDataSetChanged();*/
                updateUI(myAnimelistAnimeData);
                ExtractMoreInfo extractMoreInfoTask = new ExtractMoreInfo();
                extractMoreInfoTask.execute(myAnimelistAnimeData);
            }
        });


    }

    private void updateUI(MyAnimelistAnimeData myAnimelistAnimeData) {
        titleTextView.setText(myAnimelistAnimeData.getTitle());

        searchAnimeButton.setOnClickListener(v -> {
            Intent searchIntent = new Intent(getContext(), AnimeSearchActivity.class);
            searchIntent.putExtra(AnimeSearchActivity.INTENT_SEARCH_TERM, myAnimelistAnimeData.getTitle());
            startActivity(searchIntent);
        });



        rankTextView.setText(myAnimelistAnimeData.getInfo("Ranked"));
        scoreTextView.setText(myAnimelistAnimeData.getInfo("Score"));
        popularityTextView.setText(myAnimelistAnimeData.getInfo("Popularity"));

        if(myAnimelistAnimeData.getInfo("English").equals("N/A")) {
            englishTitleTextView.setVisibility(View.GONE);
        }
        else {
            englishTitleTextView.setVisibility(View.VISIBLE);
            englishTitleTextView.setText(myAnimelistAnimeData.getInfo("English"));
        }

        infosContentLinearLayout.removeAllViews();

        for (String imageURL : myAnimelistAnimeData.getImages()) {
            if (!sliderImageURLs.contains(imageURL)) sliderImageURLs.add(imageURL);
        }
        imageSliderAdapter.notifyDataSetChanged();
        String[] infoKeys;
        if(myAnimelistAnimeData.getUrl().contains("/anime/")) {
            infoKeys = new String[] {
                    "Type",
                    "Episodes",
                    "Status",
                    "Aired",
                    "Premiered",
                    "Broadcast",
                    "Producers",
                    "Licensors",
                    "Studios",
                    "Source"
            };
        }
        else {
            infoKeys = new String[] {
                    "Type",
                    "Volumes",
                    "Chapters",
                    "Status",
                    "Published",
                    "Authors"
            };
        }

        genreFlexBox.removeAllViews();
        for(String genreName: myAnimelistAnimeData.getGenres()) {
            TextView genreTextView = getGenreTextView(genreName);
            genreFlexBox.addView(genreTextView);
        }

        boolean initialView = true;
        LinearLayout.LayoutParams dividerLayoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        dividerLayoutParams.setMargins(8,4,8,4);
        for(String key: infoKeys) {
            String value = myAnimelistAnimeData.getInfo(key);
            if(value != null) {
                if(!initialView) {
                    MaterialDivider divider = new MaterialDivider(getContext());
                    divider.setLayoutParams(dividerLayoutParams);
                    infosContentLinearLayout.addView(divider);
                }
                else {
                    initialView = false;
                }
                View keyValueTextView = getKeyValueView(key, value);
                infosContentLinearLayout.addView(keyValueTextView);
            }
        }

        for(MyAnimelistAnimeData prequel: myAnimelistAnimeData.getPrequels()) {
            MaterialDivider divider = new MaterialDivider(getContext());
            divider.setLayoutParams(dividerLayoutParams);
            infosContentLinearLayout.addView(divider);

            View keyValueView = getKeyValueView("Prequel", prequel.getTitle());
            TextView valueTV = keyValueView.findViewById(R.id.kv_value_text_view);
            valueTV.setTextColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
            valueTV.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), MyAnimelistInfoActivity.class);
                intent.putExtra(MyAnimelistInfoActivity.INTENT_ANIMELIST_DATA_KEY, prequel);
                startActivity(intent);
                getActivity().finish();
            });
            infosContentLinearLayout.addView(keyValueView);
        }
        for(MyAnimelistAnimeData sequel: myAnimelistAnimeData.getSequels()) {
            MaterialDivider divider = new MaterialDivider(getContext());
            divider.setLayoutParams(dividerLayoutParams);
            infosContentLinearLayout.addView(divider);

            View keyValueView = getKeyValueView("Sequel", sequel.getTitle());
            TextView valueTV = keyValueView.findViewById(R.id.kv_value_text_view);
            valueTV.setTextColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
            valueTV.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), MyAnimelistInfoActivity.class);
                intent.putExtra(MyAnimelistInfoActivity.INTENT_ANIMELIST_DATA_KEY, sequel);
                startActivity(intent);
                getActivity().finish();
            });
            infosContentLinearLayout.addView(keyValueView);
        }
    }

    private class ExtractMoreInfo extends AsyncTask<MyAnimelistAnimeData, Void, MyAnimelistAnimeData> {

        private void fetchPics(MyAnimelistAnimeData myAnimelistAnimeData) {
            try {
                HttpURLConnection picsURLConnection = SimpleHttpClient.getURLConnection(myAnimelistAnimeData.getUrl() + "/pics");
                SimpleHttpClient.setBrowserUserAgent(picsURLConnection);
                Document picsDoc = Jsoup.parse(SimpleHttpClient.getResponse(picsURLConnection));
                Elements picElements = picsDoc.select("div.picSurround");
                for(Element picElement: picElements) {
                    myAnimelistAnimeData.addImage(picElement.getElementsByTag("a").get(0).attr("href"));
                }
            } catch (Exception e) { e.printStackTrace(); }
        }

        @Override
        protected void onPostExecute(MyAnimelistAnimeData myAnimelistAnimeData) {
            super.onPostExecute(myAnimelistAnimeData);
            for (String imageURL : myAnimelistAnimeData.getImages()) {
                if (!sliderImageURLs.contains(imageURL)) sliderImageURLs.add(imageURL);
            }
            imageSliderAdapter.notifyDataSetChanged();
        }

        @Override
        protected MyAnimelistAnimeData doInBackground(MyAnimelistAnimeData... myAnimelistAnimeDatas) {
            MyAnimelistAnimeData myAnimelistAnimeData = myAnimelistAnimeDatas[0];
            try {
                fetchPics(myAnimelistAnimeData);
            } catch (Exception e) { e.printStackTrace(); }
            return myAnimelistAnimeData;
        }
    }

    private View getKeyValueView(String key, String value) {
        View view = getLayoutInflater().inflate(R.layout.layout_key_value, null, false);
        TextView keyTextView = view.findViewById(R.id.kv_layout_key_text_view);
        TextView valueTextView = view.findViewById(R.id.kv_value_text_view);
        keyTextView.setText(key);
        valueTextView.setText(value);
        return view;
    }

    private TextView getGenreTextView(String genreName) {
        TextView textView = new TextView(getContext());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(24, 4, 24, 4);
        textView.setLayoutParams(layoutParams);
        textView.setTextColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
        textView.setTextSize(16);
        textView.setText(genreName);
        textView.setOnClickListener(v -> {
            AdvSearchParams searchParams = new AdvSearchParams();
            searchParams.addGenre(genreName);
            Intent intent = MyAnimeListSearchActivity.prepareIntent(getContext(), searchParams);
            startActivity(intent);
        });
        return textView;
    }
}