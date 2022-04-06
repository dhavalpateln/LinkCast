package com.dhavalpateln.linkcast.myanimelist;

import android.net.Uri;

import com.dhavalpateln.linkcast.utils.SimpleHttpClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

public class MyAnimelistSearch {
    public static List<MyAnimelistAnimeData> anime(String term) {
        String searchUrl = "https://myanimelist.net/search/prefix.json?type=anime&keyword=" + Uri.encode(term) + "&v=1";
        List<MyAnimelistAnimeData> result = new ArrayList<>();
        try {
            HttpURLConnection urlConnection = SimpleHttpClient.getURLConnection(searchUrl);
            JSONObject httpContent = new JSONObject(SimpleHttpClient.getResponse(urlConnection));
            JSONArray searchResult = httpContent.getJSONArray("categories").getJSONObject(0).getJSONArray("items");
            for(int i = 0; i < searchResult.length(); i++) {
                JSONObject animeData = searchResult.getJSONObject(i);
                MyAnimelistAnimeData myAnimelistAnimeData = new MyAnimelistAnimeData(animeData.getInt("id"));
                myAnimelistAnimeData.setTitle(animeData.getString("name"));
                myAnimelistAnimeData.setUrl(animeData.getString("url"));
                myAnimelistAnimeData.addImage(animeData.getString("image_url"));
                result.add(myAnimelistAnimeData);
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return result;
    }
}
