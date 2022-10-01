package com.dhavalpateln.linkcast.animesearch;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

import com.dhavalpateln.linkcast.ProvidersData;
import com.dhavalpateln.linkcast.database.AnimeLinkData;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.dhavalpateln.linkcast.animescrappers.NineAnimeExtractor.SOURCE_PREF_KEY;

public class NineAnimeSearch extends AnimeSearch {

    private final String TAG = "9AnimeSearch";
    private String baseURL;
    private SharedPreferences prefs;

    public NineAnimeSearch() {super();}

    public NineAnimeSearch(Context context) {
        this();
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Override
    public ArrayList<AnimeLinkData> search(String term) {
        ArrayList<AnimeLinkData> result = new ArrayList<>();
        try {
            String searchURL = baseURL + "/filter?sort=most_relevance&keyword=" + Uri.encode(term);
            Log.d(TAG, "Search URL: " + searchURL);
            Document html = Jsoup.parse(getHttpContent(searchURL));
            Elements animeLinks = html.select("div.item");
            for(Element element: animeLinks) {
                AnimeLinkData animeLinkData = new AnimeLinkData();
                Map<String, String> animeData = new HashMap<>();

                Element animeInfoElement = element.selectFirst("a.name");

                animeData.put(AnimeLinkData.DataContract.DATA_IMAGE_URL, element.getElementsByTag("img").get(0).attr("src"));
                animeData.put(AnimeLinkData.DataContract.DATA_MODE, "advanced");
                animeData.put(AnimeLinkData.DataContract.DATA_SOURCE, ProvidersData.NINEANIME.NAME);

                animeLinkData.setUrl(ProvidersData.NINEANIME.URL + animeInfoElement.attr("href"));
                animeLinkData.setTitle(animeInfoElement.text());
                animeLinkData.setData(animeData);
                result.add(animeLinkData);
            }
            Log.d(TAG, "Found " + result.size() + "results");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public String getName() {
        return ProvidersData.NINEANIME.NAME;
    }

    @Override
    public boolean hasQuickSearch() {
        return true;
    }

    @Override
    public void init() {
        try {
            if(baseURL == null) {
                baseURL = prefs.getString(SOURCE_PREF_KEY, ProvidersData.NINEANIME.URL);
                if(getHttpResponseCode(baseURL) != 200) {
                    for(String url: ProvidersData.NINEANIME.ALTERNATE_URLS) {
                        if(getHttpResponseCode(url) == 200) {
                            Log.d(TAG, "Using alternate url: " + url);
                            baseURL = url;
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString(SOURCE_PREF_KEY, baseURL);
                            editor.commit();
                            break;
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean requiresInit() {
        return baseURL == null;
    }
}
