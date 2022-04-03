package com.dhavalpateln.linkcast.myanimelist.ui.main;

import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dhavalpateln.linkcast.R;
import com.dhavalpateln.linkcast.myanimelist.MyAnimelistAnimeData;
import com.dhavalpateln.linkcast.myanimelist.MyAnimelistInfoActivity;
import com.dhavalpateln.linkcast.myanimelist.adapters.SliderAdapter;
import com.dhavalpateln.linkcast.utils.SimpleHttpClient;
import com.smarteist.autoimageslider.SliderView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AnimeInfoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AnimeInfoFragment extends Fragment {

    private final String TAG = "AnimeInfo";
    private SliderView sliderView;
    private ArrayList<String> sliderImageURLs;
    private SliderAdapter adapter;

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

        sliderView = view.findViewById(R.id.anime_info_img_slider);
        adapter = new SliderAdapter(getContext(), sliderImageURLs);

        sliderView.setAutoCycleDirection(SliderView.LAYOUT_DIRECTION_LTR);
        sliderView.setSliderAdapter(adapter);
        sliderView.setScrollTimeInSec(5);
        sliderView.setAutoCycle(true);
        sliderView.startAutoCycle();

        MyAnimelistDataViewModel viewModel = new ViewModelProvider(getActivity()).get(MyAnimelistDataViewModel.class);
        viewModel.getData().observe(getViewLifecycleOwner(), new Observer<MyAnimelistAnimeData>() {
            @Override
            public void onChanged(MyAnimelistAnimeData myAnimelistAnimeData) {
                if(myAnimelistAnimeData.getId() > 0) {
                    for (String imageURL : myAnimelistAnimeData.getImages()) {
                        if (!sliderImageURLs.contains(imageURL)) sliderImageURLs.add(imageURL);
                    }
                    adapter.notifyDataSetChanged();
                    ExtractMoreInfo extractMoreInfoTask = new ExtractMoreInfo();
                    extractMoreInfoTask.execute(myAnimelistAnimeData);
                }
            }
        });


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
            adapter.notifyDataSetChanged();
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
}