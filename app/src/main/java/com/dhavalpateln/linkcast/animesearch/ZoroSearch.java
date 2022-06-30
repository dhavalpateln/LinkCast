package com.dhavalpateln.linkcast.animesearch;

import android.net.Uri;

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

public class ZoroSearch extends AnimeSearch {
    @Override
    public ArrayList<AnimeLinkData> search(String term) {
        ArrayList<AnimeLinkData> result = new ArrayList<>();
        try {
            String searchUrl = ProvidersData.ZORO.URL + "/search?keyword=" + Uri.encode(term);
            Document html = Jsoup.parse(getHttpContent(searchUrl));
            Elements searchList = html.select("div.flw-item");
            for(Element searchItem: searchList) {
                Element dataElement = searchItem.selectFirst("div.film-poster");
                Element animeInfo = dataElement.selectFirst("a.item-qtip[title][data-id]");

                AnimeLinkData animeLinkData = new AnimeLinkData();
                Map<String, String> animeData = new HashMap<>();

                animeData.put(AnimeLinkData.DataContract.DATA_IMAGE_URL, dataElement.selectFirst("img.film-poster-img").attr("data-src"));
                animeData.put(AnimeLinkData.DataContract.DATA_MODE, "advanced");
                animeData.put(AnimeLinkData.DataContract.DATA_SOURCE, ProvidersData.ZORO.NAME);

                animeLinkData.setUrl(ProvidersData.ZORO.URL + animeInfo.attr("href").replace("?ref=search", ""));
                animeLinkData.setTitle(animeInfo.attr("title"));
                animeLinkData.setData(animeData);
                result.add(animeLinkData);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
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
