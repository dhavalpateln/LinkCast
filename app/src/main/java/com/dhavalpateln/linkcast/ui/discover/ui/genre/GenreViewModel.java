package com.dhavalpateln.linkcast.ui.discover.ui.genre;

import android.os.AsyncTask;

import com.dhavalpateln.linkcast.myanimelist.MyAnimelistAnimeData;
import com.dhavalpateln.linkcast.utils.SimpleHttpClient;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class GenreViewModel extends ViewModel {

    Map<String, List<MyAnimelistAnimeData>> cache;
    MutableLiveData<List<MyAnimelistAnimeData>> liveData;

    public GenreViewModel() {
        cache = new HashMap<>();
    }

    public MutableLiveData<List<MyAnimelistAnimeData>> getData() {
        if(liveData == null) {
            liveData = new MutableLiveData<>();
        }
        return liveData;
    }

    public void loadData(String key) {
        new LoadDataTask(key).execute();
    }

    private class LoadDataTask extends AsyncTask<Void, Void, List<MyAnimelistAnimeData>> {

        public LoadDataTask(String key) {

        }

        @Override
        protected void onPostExecute(List<MyAnimelistAnimeData> myAnimelistAnimeData) {
            super.onPostExecute(myAnimelistAnimeData);

        }

        @Override
        protected List<MyAnimelistAnimeData> doInBackground(Void... voids) {
            List<MyAnimelistAnimeData> result = new ArrayList<>();

            return result;
        }
    }


}
