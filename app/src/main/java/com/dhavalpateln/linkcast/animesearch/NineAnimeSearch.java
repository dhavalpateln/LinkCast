package com.dhavalpateln.linkcast.animesearch;

import android.net.Uri;
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

public class NineAnimeSearch extends AnimeSearch {

    private final String TAG = "9AnimeSearch";

    @Override
    public ArrayList<AnimeLinkData> search(String term) {
        ArrayList<AnimeLinkData> result = new ArrayList<>();
        try {
            String searchURL = "https://9anime.to/filter?sort=views:desc&keyword=" + Uri.encode(term);
            Document html = Jsoup.parse(getHttpContent(searchURL));
            Elements animeLinks = html.select(".anime-list li");
            for(Element element: animeLinks) {
                AnimeLinkData animeLinkData = new AnimeLinkData();
                Map<String, String> animeData = new HashMap<>();

                Element animeInfoElement = element.selectFirst(".name");

                animeData.put(AnimeLinkData.DataContract.DATA_IMAGE_URL, element.getElementsByTag("img").get(0).attr("src"));
                animeData.put(AnimeLinkData.DataContract.DATA_MODE, "advanced");

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
}
