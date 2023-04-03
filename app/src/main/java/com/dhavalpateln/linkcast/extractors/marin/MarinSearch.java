package com.dhavalpateln.linkcast.extractors.marin;

import static java.net.URLDecoder.decode;

import android.util.Log;

import com.dhavalpateln.linkcast.ProvidersData;
import com.dhavalpateln.linkcast.animesearch.AnimeSearch;
import com.dhavalpateln.linkcast.database.AnimeLinkData;
import com.dhavalpateln.linkcast.extractors.AnimeMangaSearch;
import com.dhavalpateln.linkcast.utils.SimpleHttpClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MarinSearch extends AnimeMangaSearch {

    private final String TAG = "MarinSearch";

    public MarinSearch() {
        setRequiresInit(true);
    }

    public void init() {
        try {
            MarinUtils.getInstance().getCookies();
            setRequiresInit(false);
            Log.d(TAG, "Got cookies");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void configConnection(HttpURLConnection urlConnection) {
        try {
            Map<String, String> headers = MarinUtils.getInstance().getCookies();
            for(Map.Entry<String, String> entry: headers.entrySet()) {
                urlConnection.setRequestProperty(entry.getKey(), entry.getValue());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getDisplayName() {
        return ProvidersData.MARIN.NAME;
    }

    @Override
    public boolean isAdvanceModeSource() {
        return true;
    }

    @Override
    public ArrayList<AnimeLinkData> search(String term) {
        ArrayList<AnimeLinkData> result = new ArrayList<>();

        try {
            HttpURLConnection urlConnection = SimpleHttpClient.getURLConnection(ProvidersData.MARIN.URL + "/anime");
            urlConnection.setRequestMethod("POST");
            configConnection(urlConnection);
            SimpleHttpClient.setBrowserUserAgent(urlConnection);

            JSONObject payload = new JSONObject();
            payload.put("search", term);
            SimpleHttpClient.setPayload(urlConnection, payload);
            String response = SimpleHttpClient.getResponse(urlConnection);
            JSONObject appData = new JSONObject(Jsoup.parse(response).getElementById("app").attr("data-page"));
            JSONArray animeList = appData.getJSONObject("props").getJSONObject("anime_list").getJSONArray("data");

            for(int i = 0; i < animeList.length(); i++) {
                JSONObject animeData = animeList.getJSONObject(i);
                AnimeLinkData animeLinkData = new AnimeLinkData();
                animeLinkData.setUrl(ProvidersData.MARIN.URL + "/anime/" + animeData.getString("slug"));
                animeLinkData.setTitle(animeData.getString("title"));
                animeLinkData.updateData(AnimeLinkData.DataContract.DATA_IMAGE_URL, animeData.getString("cover"), true);
                animeLinkData.updateData(AnimeLinkData.DataContract.DATA_SOURCE, ProvidersData.MARIN.NAME, true);
                animeLinkData.updateData(AnimeLinkData.DataContract.DATA_MODE, "advanced", true);
                result.add(animeLinkData);
            }
            Log.d(TAG, "Found " + result.size() + " results");
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public boolean isAnimeSource() {
        return super.isAnimeSource();
    }

    @Override
    public boolean isMangaSource() {
        return super.isMangaSource();
    }

    @Override
    public boolean hasQuickSearch() {
        return true;
    }
}
