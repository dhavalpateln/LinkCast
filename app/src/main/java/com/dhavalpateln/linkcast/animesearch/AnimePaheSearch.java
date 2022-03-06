package com.dhavalpateln.linkcast.animesearch;

import android.net.Uri;

import com.dhavalpateln.linkcast.database.AnimeLinkData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AnimePaheSearch extends AnimeSearch {
    public AnimePaheSearch(){

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
                data.put("imageUrl", searchElement.getString("poster"));
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
}
