package com.dhavalpateln.linkcast.database;

import android.net.Uri;

import com.dhavalpateln.linkcast.data.JikanCache;
import com.dhavalpateln.linkcast.myanimelist.AdvSearchParams;
import com.dhavalpateln.linkcast.myanimelist.MyAnimelistAnimeData;
import com.dhavalpateln.linkcast.myanimelist.MyAnimelistCharacterData;
import com.dhavalpateln.linkcast.ui.discover.ui.popular.PopularViewModel;
import com.dhavalpateln.linkcast.ui.discover.ui.seasonal.SeasonalViewModel;
import com.dhavalpateln.linkcast.utils.SimpleHttpClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

public class JikanDatabase {
    private JikanCache cache;
    private static JikanDatabase jikanDatabase;

    private JikanDatabase() {
        cache = JikanCache.getInstance();
    }

    public static JikanDatabase getInstance() {
        if(jikanDatabase == null)   jikanDatabase = new JikanDatabase();
        return jikanDatabase;
    }

    private JSONObject getJikanResult(String url) {

        try {
            HttpURLConnection urlConnection = SimpleHttpClient.getURLConnection(url);
            int responseCode = SimpleHttpClient.getResponseCode(urlConnection);
            if(responseCode < 400) {
                return new JSONObject(SimpleHttpClient.getResponse(urlConnection));
            }
            if(responseCode == 429) {
                Thread.sleep(1000);
                HttpURLConnection urlConnection2 = SimpleHttpClient.getURLConnection(url);
                int responseCode2 = SimpleHttpClient.getResponseCode(urlConnection2);
                if(responseCode2 < 400) {
                    return new JSONObject(SimpleHttpClient.getResponse(urlConnection2));
                }
            }
        } catch (IOException | JSONException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<MyAnimelistAnimeData> getList(PopularViewModel.TYPE listType, int page) {
        String url = "https://api.jikan.moe/v4/top/anime?";

        switch (listType) {
            case TOP_AIRING:
                url += "filter=airing"; break;
            case TOP_UPCOMING:
                url += "filter=upcoming"; break;
            case POPULAR:
                url += "filter=bypopularity"; break;
            case FAVORITE:
                url += "filter=favorite"; break;
            case TOP_TV_SERIES:
                url += "type=tv"; break;
            case TOP_MOVIES:
                url += "type=movie"; break;
            default:
                return null;
        }
        url += "&page=" + page;

        if(cache.getQueryResult(url) != null) {
            return cache.getQueryResult(url);
        }
        List<MyAnimelistAnimeData> result = new ArrayList<>();

        try {
            JSONObject jikanResult = getJikanResult(url);
            if(jikanResult != null) {
                JSONArray jikanResultArray = jikanResult.getJSONArray("data");
                for (int i = 0; i < jikanResultArray.length(); i++) {
                    JSONObject animeData = jikanResultArray.getJSONObject(i);
                    MyAnimelistAnimeData myAnimelistAnimeData = new MyAnimelistAnimeData();
                    myAnimelistAnimeData.setUrl(animeData.getString("url"));
                    myAnimelistAnimeData.setTitle(animeData.getString("title"));
                    myAnimelistAnimeData.addImage(animeData.getJSONObject("images").getJSONObject("jpg").getString("image_url"));

                    String genreString = "";
                    JSONArray genreArray = animeData.getJSONArray("genres");
                    for (int genreIndex = 0; genreIndex < genreArray.length(); genreIndex++) {
                        genreString += genreArray.getJSONObject(genreIndex).getString("name") + ",";
                    }
                    if (genreArray.length() > 0) {
                        myAnimelistAnimeData.putInfo("Genres", genreString.substring(0, genreString.length() - 1));
                    }
                    myAnimelistAnimeData.setMalScoreString(animeData.getString("score"));
                    result.add(myAnimelistAnimeData);
                }
                cache.storeCache(url, result);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    public List<String> getSeasonList() {
        List<String> result = JikanCache.getInstance().getSeasonListCache();
        String[] seasonOrder = new String[] {
                SeasonalViewModel.Quater.WINTER,
                SeasonalViewModel.Quater.SPRING,
                SeasonalViewModel.Quater.SUMMER,
                SeasonalViewModel.Quater.FALL
        };
        if(result == null) {
            result = new ArrayList<>();
            try {
                result.add("Later");
                String url = "https://api.jikan.moe/v4/seasons";
                //HttpURLConnection urlConnection = SimpleHttpClient.getURLConnection(url);
                JSONObject jsonResult = getJikanResult(url);
                JSONArray seasonList = jsonResult.getJSONArray("data");
                for (int yearIndex = 0; yearIndex < seasonList.length(); yearIndex++) {
                    JSONObject yearJsonObj = seasonList.getJSONObject(yearIndex);
                    String year = yearJsonObj.getString("year");
                    JSONArray seasons = yearJsonObj.getJSONArray("seasons");
                    for (int seasonIndex = seasons.length() - 1; seasonIndex >= 0; seasonIndex--) {
                        result.add(seasonOrder[seasonIndex] + " " + year);
                    }
                }
                JikanCache.getInstance().setSeasonListCache(result);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public void getCharacterData(MyAnimelistCharacterData data) {
        String url = "https://api.jikan.moe/v4/characters/" + data.getId();
        JSONObject jikanResult = getJikanResult(url);
        try {
            data.setName(jikanResult.getJSONObject("data").getString("name"));
            data.setAbout(jikanResult.getJSONObject("data").getString("about"));
            data.addImage(jikanResult.getJSONObject("data").getJSONObject("images").getJSONObject("jpg").getString("image_url"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public List<MyAnimelistAnimeData> getSearchResult(AdvSearchParams searchParams, int page) {
        List<MyAnimelistAnimeData> result = new ArrayList<>();

        Uri.Builder builder = new Uri.Builder();
        builder.appendPath("v4");
        builder.appendPath("anime");

        builder.appendQueryParameter("page", String.valueOf(page));
        if(searchParams.getQuery() != null) builder.appendQueryParameter("q", searchParams.getQuery());
        if(searchParams.getType() != null) builder.appendQueryParameter("type", searchParams.getType());
        if(searchParams.getScore() != 0) builder.appendQueryParameter("score", String.valueOf(searchParams.getScore()));
        if(searchParams.getStatus() != null) builder.appendQueryParameter("status", searchParams.getStatus());
        if(searchParams.getOrderBy() != null) builder.appendQueryParameter("order_by", searchParams.getOrderBy());
        if(searchParams.getSort() != null) builder.appendQueryParameter("sort", searchParams.getSort());


        String genres = "";
        for(Integer genreId: searchParams.getGenres()) {
            genres += "," + genreId;
        }
        if(!genres.equals(""))   genres = genres.substring(1);
        builder.appendQueryParameter("genres", genres);

        String url = "https://api.jikan.moe" + builder.build().toString();

        try {
            HttpURLConnection urlConnection = SimpleHttpClient.getURLConnection(url);
            SimpleHttpClient.setBrowserUserAgent(urlConnection);
            JSONObject jsonResponse = new JSONObject(SimpleHttpClient.getResponse(urlConnection));
            JSONArray animeList = jsonResponse.getJSONArray("data");
            for(int i = 0; i < animeList.length(); i++) {
                JSONObject animeData = animeList.getJSONObject(i);
                MyAnimelistAnimeData animelistAnimeData = new MyAnimelistAnimeData();
                animelistAnimeData.setUrl(animeData.getString("url"));
                animelistAnimeData.setTitle(animeData.getString("title"));
                animelistAnimeData.addImage(animeData.getJSONObject("images").getJSONObject("jpg").getString("image_url"));
                animelistAnimeData.setMalScoreString(animeData.getString("score"));

                String genresString = "";
                JSONArray genresArray = animeData.getJSONArray("genres");
                for(int genreNum = 0; genreNum < genresArray.length(); genreNum++) {
                    genresString += "," + genresArray.getJSONObject(genreNum).getString("name");
                }
                if(!genresString.equals(""))    genresString = genresString.substring(1);

                animelistAnimeData.putInfo("Genres", genresString);
                result.add(animelistAnimeData);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
