package com.dhavalpateln.linkcast.database;

import com.dhavalpateln.linkcast.data.JikanCache;
import com.dhavalpateln.linkcast.myanimelist.MyAnimelistAnimeData;
import com.dhavalpateln.linkcast.ui.discover.ui.popular.PopularViewModel;
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
            if(responseCode == 200) {
                return new JSONObject(SimpleHttpClient.getResponse(urlConnection));
            }
        } catch (IOException | JSONException e) {
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
            JSONArray jikanResultArray = jikanResult.getJSONArray("data");
            for(int i = 0; i < jikanResultArray.length(); i++) {
                JSONObject animeData = jikanResultArray.getJSONObject(i);
                MyAnimelistAnimeData myAnimelistAnimeData = new MyAnimelistAnimeData();
                myAnimelistAnimeData.setUrl(animeData.getString("url"));
                myAnimelistAnimeData.setTitle(animeData.getString("title"));
                myAnimelistAnimeData.addImage(animeData.getJSONObject("images").getJSONObject("jpg").getString("image_url"));

                String genreString= "";
                JSONArray genreArray = animeData.getJSONArray("genres");
                for(int genreIndex = 0; genreIndex < genreArray.length(); genreIndex++) {
                    genreString += genreArray.getJSONObject(genreIndex).getString("name") + ",";
                }
                if(genreArray.length() > 0) {
                    myAnimelistAnimeData.putInfo("Genres", genreString.substring(0, genreString.length() - 1));
                }
                result.add(myAnimelistAnimeData);
            }
            cache.storeCache(url, result);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }
}
