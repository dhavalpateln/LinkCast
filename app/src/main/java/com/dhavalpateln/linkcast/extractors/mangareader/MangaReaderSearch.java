package com.dhavalpateln.linkcast.extractors.mangareader;

import android.net.Uri;

import com.dhavalpateln.linkcast.ProvidersData;
import com.dhavalpateln.linkcast.animesearch.AnimeSearch;
import com.dhavalpateln.linkcast.database.AnimeLinkData;
import com.dhavalpateln.linkcast.extractors.AnimeMangaSearch;
import com.dhavalpateln.linkcast.utils.SimpleHttpClient;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;

public class MangaReaderSearch extends AnimeMangaSearch {

    public MangaReaderSearch() {
        setSourceType(SOURCE_TYPE.MANGA);
    }

    @Override
    public ArrayList<AnimeLinkData> search(String term) {

        ArrayList<AnimeLinkData> result = new ArrayList<>();
        try {
            String url = ProvidersData.MANGAREADER.URL + "/search?keyword=" + Uri.encode(term);
            HttpURLConnection urlConnection = SimpleHttpClient.getURLConnection(url);
            Document searchDoc = Jsoup.parse(SimpleHttpClient.getResponse(urlConnection));
            Elements mangaElements = searchDoc.select("div.item.item-spc");
            for(Element mangaElement: mangaElements) {
                AnimeLinkData data = new AnimeLinkData();
                data.setTitle(mangaElement.selectFirst("h3.manga-name").text());
                data.setUrl(ProvidersData.MANGAREADER.URL + mangaElement.selectFirst("a.manga-poster").attr("href"));
                data.updateData(AnimeLinkData.DataContract.DATA_IMAGE_URL, mangaElement.selectFirst("a.manga-poster").selectFirst("img").attr("src"), false);
                data.updateData(AnimeLinkData.DataContract.DATA_LINK_TYPE, "manga", false);
                data.updateData(AnimeLinkData.DataContract.DATA_SOURCE, ProvidersData.MANGAREADER.NAME, false);
                result.add(data);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public void init() {

    }

    @Override
    public boolean requiresInit() {
        return false;
    }

    @Override
    public String getDisplayName() {
        return ProvidersData.MANGAREADER.NAME;
    }

    @Override
    public boolean hasQuickSearch() {
        return true;
    }

}
