package com.dhavalpateln.linkcast.animesearch;

import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class AnimePaheSearch extends AnimeSearch {
    public AnimePaheSearch(){

    }

    @Override
    public JSONArray search(String term) {
        String searchUrl = "https://animepahe.com/api?m=search&l=8&q=" + Uri.encode(term);
        JSONArray result = new JSONArray();
        try {
            JSONObject resultElement = new JSONObject();
            String searchResult = getHttpContent(searchUrl);
            JSONArray searchJsonArray = new JSONObject(searchResult).getJSONArray("data");
            for(int i = 0; i < searchJsonArray.length(); i++) {
                JSONObject searchElement = searchJsonArray.getJSONObject(i);
                resultElement.put(ID, searchElement.getString("id"));
                resultElement.put(IMAGE, searchElement.getString("poster"));
                resultElement.put(TITLE, searchElement.getString("title"));
                resultElement.put(LINK, "https://animepahe.com/anime/" + searchElement.getString("session"));
                result.put(resultElement);
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
