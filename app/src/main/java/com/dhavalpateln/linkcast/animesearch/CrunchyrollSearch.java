package com.dhavalpateln.linkcast.animesearch;

import android.net.Uri;

import com.dhavalpateln.linkcast.ProvidersData;
import com.dhavalpateln.linkcast.database.AnimeLinkData;
import com.dhavalpateln.linkcast.utils.Searcher;
import com.dhavalpateln.linkcast.utils.SimpleHttpClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CrunchyrollSearch extends AnimeSearch {

    private boolean initComplete = false;
    private Searcher<JSONObject> searcher = null;

    @Override
    public ArrayList<AnimeLinkData> search(String term) {
        ArrayList<AnimeLinkData> result = new ArrayList<>();
        try {
            List<JSONObject> searchResult = searcher.search(term);
            for(JSONObject anime: searchResult) {
                AnimeLinkData animeLinkData = new AnimeLinkData();
                animeLinkData.setUrl(anime.getString("link") + ":::" + anime.getString("etp_guid"));
                animeLinkData.setTitle(anime.getString("name"));
                animeLinkData.updateData(AnimeLinkData.DataContract.DATA_SOURCE, ProvidersData.CRUNCHYROLL.NAME, true);
                animeLinkData.updateData(AnimeLinkData.DataContract.DATA_IMAGE_URL, anime.getString("img").replace("small", "full"), true);
                result.add(animeLinkData);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public void init() {
        try {
            HttpURLConnection animeCandidatesUrl = SimpleHttpClient.getURLConnection(ProvidersData.CRUNCHYROLL.URL + "/ajax/?req=RpcApiSearch_GetSearchCandidates");
            String candiateContent = SimpleHttpClient.getResponse(animeCandidatesUrl);

            JSONObject result = new JSONObject(candiateContent.split("\n")[1]);
            JSONArray animeList = result.getJSONArray("data");
            Map<String, JSONObject> animeMap = new HashMap<>();
            for(int i = 0; i < animeList.length(); i++) {
                animeMap.put(animeList.getJSONObject(i).getString("name"), animeList.getJSONObject(i));
            }
            searcher = new Searcher<>(animeMap);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean requiresInit() {
        return searcher == null;
    }

    @Override
    public String getName() {
        return ProvidersData.ZORO.NAME;
    }

    @Override
    public boolean hasQuickSearch() {
        return true;
    }
}
