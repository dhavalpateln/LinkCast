package com.dhavalpateln.linkcast.ui.discover.ui.seasonal;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;

import com.dhavalpateln.linkcast.data.MyAnimeListCache;
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
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import static com.dhavalpateln.linkcast.utils.Utils.getCurrentTime;

public class SeasonalViewModel  extends ViewModel {

    private Season currentSeason;
    private Executor executor = Executors.newSingleThreadExecutor();
    Handler uiHandler = new Handler(Looper.getMainLooper());

    public enum SeasonType {
        CURRENT,
        LAST,
        NEXT,
        LATER,
        ARCHIVE
    }

    public static class Quater {
        public static final String WINTER = "Winter";
        public static final String SPRING = "Spring";
        public static final String SUMMER = "Summer";
        public static final String FALL = "Fall";
    }

    public class Season {
        private String quater;
        private String year;

        public Season(String quater, String year) {
            this.quater = quater;
            this.year = year;
        }

        public Season(int month, String year) {
            this.quater = monthToQuater(month);
            this.year = year;
        }

        public Season(String seasonString) {
            String[] season = seasonString.split(" ");
            this.quater = season[0];
            this.year = season[1];
        }

        private String monthToQuater(int month) {
            switch ((int) month / 3) {
                case 0: return Quater.WINTER;
                case 1: return Quater.SPRING;
                case 2: return Quater.SUMMER;
                case 3: return Quater.FALL;
            }
            return null;
        }

        private String nextQuater(String quater) {
            switch (quater) {
                case Quater.WINTER:  return Quater.SPRING;
                case Quater.SPRING:  return Quater.SUMMER;
                case Quater.SUMMER:  return Quater.FALL;
                case Quater.FALL:  return Quater.WINTER;
            }
            return null;
        }
        private String prevQuater(String quater) {
            switch (quater) {
                case Quater.WINTER:  return Quater.FALL;
                case Quater.SPRING:  return Quater.WINTER;
                case Quater.SUMMER:  return Quater.SPRING;
                case Quater.FALL:  return Quater.SUMMER;
            }
            return null;
        }

        public Season nextSeason() {
            String quater = nextQuater(this.quater);
            String year = this.year;
            if(quater.equals(Quater.WINTER)) {
                year = String.valueOf(Integer.valueOf(year) + 1);
            }
            return new Season(quater, year);
        }

        public Season prevSeason() {
            String quater = prevQuater(this.quater);
            String year = this.year;
            if(quater.equals(Quater.FALL)) {
                year = String.valueOf(Integer.valueOf(year) - 1);
            }
            return new Season(quater, year);
        }

        public String getQuater() {
            return quater;
        }

        public String getYear() {
            return year;
        }

        @Override
        public String toString() {
            return quater + " " + year;
        }
    }



    Map<String, MutableLiveData<List<MyAnimelistAnimeData>>> seasonDataMap;

    public SeasonalViewModel() {
        seasonDataMap = new HashMap<>();
    }

    public MutableLiveData<List<MyAnimelistAnimeData>> getData(String key) {
        if(!seasonDataMap.containsKey(key)) {
            MutableLiveData<List<MyAnimelistAnimeData>> data = new MutableLiveData<>();
            seasonDataMap.put(key, data);
            loadData(key);
        }
        return seasonDataMap.get(key);
    }

    public Season getCurrentSeason() {
        if(currentSeason == null) {
            String year = getCurrentTime("YYYY");
            int month = Integer.valueOf(getCurrentTime("MM")) - 1;
            currentSeason = new Season(month, year);
        }
        return currentSeason;
    }

    public MutableLiveData<List<MyAnimelistAnimeData>> getData(SeasonType key) {
        switch (key) {
            case CURRENT:
            case NEXT:
            case LAST:
                return getData(getSeasonString(key));
        }
        return null;
    }

    public String getSeasonString(SeasonType key) {
        switch (key) {
            case CURRENT:   return getCurrentSeason().toString();
            case NEXT:  return getCurrentSeason().nextSeason().toString();
            case LAST:  return getCurrentSeason().prevSeason().toString();
        }
        return null;
    }

    public void loadData(String seasonString) {
        executor.execute(() -> {
            String url = "https://myanimelist.net/anime/season/";
            if(seasonString.equals("Later")) {
                url += "later";
            }
            else {
                Season season = new Season(seasonString);
                url += season.getYear() + "/" + season.getQuater().toLowerCase();
            }


            List<MyAnimelistAnimeData> result = MyAnimeListCache.getInstance().getQueryResult(url);
            if(result == null) {
                result = new ArrayList<>();
                try {
                    HttpURLConnection httpURLConnection = SimpleHttpClient.getURLConnection(url);
                    SimpleHttpClient.setBrowserUserAgent(httpURLConnection);
                    Document html = Jsoup.parse(SimpleHttpClient.getResponse(httpURLConnection));
                    Elements seasonalTypes = html.select("div.seasonal-anime-list");
                    for (Element seasonalType : seasonalTypes) {
                        String type = seasonalType.selectFirst("div.anime-header").text();
                        Elements animeElements = seasonalType.select("div.seasonal-anime");
                        for (Element animeElement : animeElements) {
                            MyAnimelistAnimeData myAnimelistAnimeData = new MyAnimelistAnimeData();
                            Element titleElement = animeElement.selectFirst("div.title");
                            Element imageElement = animeElement.selectFirst("div.image");
                            myAnimelistAnimeData.setTitle(titleElement.selectFirst("h2").text());
                            myAnimelistAnimeData.setUrl(imageElement.selectFirst("a").attr("href"));
                            String imageUrl = imageElement.selectFirst("img").attr("src");
                            if (imageUrl.equals("")) {
                                imageUrl = imageElement.selectFirst("img").attr("data-src");
                            }
                            myAnimelistAnimeData.addImage(imageUrl);
                            myAnimelistAnimeData.putInfo("type", type);

                            try {
                                String genreString = "";
                                Elements genreElements = animeElement.selectFirst("div.genres").select("a");
                                for (Element genreElement : genreElements)
                                    genreString += genreElement.text() + ",";
                                genreString = genreString.substring(0, genreString.length() - 1);
                                myAnimelistAnimeData.putInfo("Genres", genreString);
                            }catch (Exception e) {e.printStackTrace();}
                            myAnimelistAnimeData.setMalScoreString(animeElement.selectFirst("div.scormem-item.score").text().trim());
                            result.add(myAnimelistAnimeData);
                        }
                    }
                    if(result.size() > 0) {
                        MyAnimeListCache.getInstance().storeAnimeCache(url, result);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            List<MyAnimelistAnimeData> finalResult = result;
            uiHandler.post(() -> {
                getData(seasonString).setValue(finalResult);
            });
        });
    }
}
