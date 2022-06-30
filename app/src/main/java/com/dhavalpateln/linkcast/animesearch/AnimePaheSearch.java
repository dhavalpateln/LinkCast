package com.dhavalpateln.linkcast.animesearch;

import android.net.Uri;

import com.dhavalpateln.linkcast.ProvidersData;
import com.dhavalpateln.linkcast.database.AnimeLinkData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AnimePaheSearch extends AnimeSearch {

    public AnimePaheSearch(){

    }

    @Override
    public void configConnection(HttpURLConnection urlConnection) {
        super.configConnection(urlConnection);
        urlConnection.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/95.0.4638.69 Safari/537.36");
        //urlConnection.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/95.0.4638.69 Safari/537.36");
    }

    @Override
    public ArrayList<AnimeLinkData> search(String term) {
        String searchUrl = "https://animepahe.com/api?m=search&l=8&q=" + Uri.encode(term);
        //JSONArray result = new JSONArray();
        ArrayList<AnimeLinkData> result = new ArrayList<>();
        try {
            //JSONObject resultElement = new JSONObject();
            String searchResult = getHttpContent(searchUrl);
            JSONArray searchJsonArray = new JSONObject(searchResult).getJSONArray("data");
            for(int i = 0; i < searchJsonArray.length(); i++) {
                JSONObject searchElement = searchJsonArray.getJSONObject(i);
                Map<String, String> data = new HashMap<>();
                AnimeLinkData animeLinkData = new AnimeLinkData();
                animeLinkData.setUrl("https://animepahe.com/anime/" + searchElement.getString("session"));
                animeLinkData.setTitle(searchElement.getString("title"));
                data.put(AnimeLinkData.DataContract.DATA_IMAGE_URL, searchElement.getString("poster"));
                data.put(AnimeLinkData.DataContract.DATA_MODE, "advanced");
                data.put(AnimeLinkData.DataContract.DATA_ANIMEPAHE_SEARCH_ID, searchElement.getString("id"));
                data.put(AnimeLinkData.DataContract.DATA_ANIMEPAHE_SESSION, searchElement.getString("session"));
                data.put(AnimeLinkData.DataContract.DATA_SOURCE, ProvidersData.ANIMEPAHE.NAME);
                animeLinkData.setData(data);
                /*resultElement.put(ID, searchElement.getString("id"));
                resultElement.put(IMAGE, searchElement.getString("poster"));
                resultElement.put(TITLE, searchElement.getString("title"));
                resultElement.put(LINK, "https://animepahe.com/anime/" + searchElement.getString("session"));*/
                result.add(animeLinkData);
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public String getName() {
        return "AnimePahe.com";
    }

    @Override
    public boolean hasQuickSearch() {
        return true;
    }
}
