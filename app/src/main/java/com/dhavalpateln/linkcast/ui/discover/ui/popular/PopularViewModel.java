package com.dhavalpateln.linkcast.ui.discover.ui.popular;

import android.os.AsyncTask;

import com.dhavalpateln.linkcast.data.MyAnimeListCache;
import com.dhavalpateln.linkcast.database.JikanDatabase;
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

public class PopularViewModel extends ViewModel {

    public enum TYPE {
        TOP_AIRING,
        TOP_UPCOMING,
        TOP_TV_SERIES,
        TOP_MOVIES,
        POPULAR,
        FAVORITE
    }

    Map<TYPE, MutableLiveData<List<MyAnimelistAnimeData>>> liveDataMap;

    public PopularViewModel() {
        liveDataMap = new HashMap<>();
    }

    public MutableLiveData<List<MyAnimelistAnimeData>> getData(TYPE key) {
        if(!liveDataMap.containsKey(key)) {
            MutableLiveData<List<MyAnimelistAnimeData>> data = new MutableLiveData<>();
            liveDataMap.put(key, data);
            loadData(key, 0);
        }
        return liveDataMap.get(key);
    }

    private String getMalUrl(TYPE type, int limit) {
        String malURL = "https://myanimelist.net/topanime.php?type=";
        switch (type) {
            case TOP_AIRING:
                malURL += "airing"; break;
            case TOP_UPCOMING:
                malURL += "upcoming"; break;
            case TOP_MOVIES:
                malURL += "movie"; break;
            case TOP_TV_SERIES:
                malURL += "tv"; break;
            case POPULAR:
                malURL += "bypopularity"; break;
            case FAVORITE:
                malURL += "favorite"; break;
        }
        malURL += "&limit=" + limit;
        return malURL;
    }

    public void loadData(TYPE type, int limit) {
        LoadDataTask loadDataTask = new LoadDataTask(type, limit);
        loadDataTask.execute();
    }

    private class LoadDataTask extends AsyncTask<Void, Void, List<MyAnimelistAnimeData>> {

        private TYPE type;
        private int limit;

        public LoadDataTask(TYPE type, int limit) {
            this.type = type;
            this.limit = limit;
        }

        @Override
        protected void onPostExecute(List<MyAnimelistAnimeData> myAnimelistAnimeData) {
            super.onPostExecute(myAnimelistAnimeData);
            getData(type).setValue(myAnimelistAnimeData);
        }

        @Override
        protected List<MyAnimelistAnimeData> doInBackground(Void... voids) {
            String malURL = getMalUrl(type, limit);
            List<MyAnimelistAnimeData> result = JikanDatabase.getInstance().getList(type, limit / 25 + 1);
            if(result.size() == 0) {
                result = MyAnimeListCache.getInstance().getQueryResult(malURL);
                if (result == null) {
                    result = new ArrayList<>();
                    try {
                        HttpURLConnection urlConnection = SimpleHttpClient.getURLConnection(malURL);
                        SimpleHttpClient.setBrowserUserAgent(urlConnection);
                        Document html = Jsoup.parse(SimpleHttpClient.getResponse(urlConnection));
                        Elements animeElements = html.select("tr.ranking-list");
                        for (Element animeElement : animeElements) {
                            try {
                                MyAnimelistAnimeData myAnimelistAnimeData = new MyAnimelistAnimeData();

                                Element imageElement = animeElement.getElementsByTag("img").get(0);
                                myAnimelistAnimeData.addImage(imageElement.attr("data-src").split("\\?")[0].replace("r/50x70/", ""));

                                Element titleElement = animeElement.getElementsByTag("h3").get(0).getElementsByTag("a").get(0);
                                myAnimelistAnimeData.setUrl(titleElement.attr("href"));
                                myAnimelistAnimeData.setTitle(titleElement.text());
                                myAnimelistAnimeData.setMalScoreString(animeElement.selectFirst("td.score").text().trim());

                                result.add(myAnimelistAnimeData);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        if (result.size() > 0)
                            MyAnimeListCache.getInstance().storeAnimeCache(malURL, result);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return result;
        }
    }


}
