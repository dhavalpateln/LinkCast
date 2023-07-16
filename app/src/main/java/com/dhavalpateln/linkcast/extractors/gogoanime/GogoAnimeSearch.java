package com.dhavalpateln.linkcast.extractors.gogoanime;

import android.net.Uri;
import android.util.Log;

import com.dhavalpateln.linkcast.ProvidersData;
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
import java.util.HashMap;
import java.util.Map;

public class GogoAnimeSearch extends AnimeMangaSearch {

    private final String TAG = "GogoAnimeSearch";

    public GogoAnimeSearch() {
    }

    @Override
    public ArrayList<AnimeLinkData> search(String term) {
        ArrayList<AnimeLinkData> result = new ArrayList<>();
        String searchUrl = ProvidersData.GOGOANIME.URL + "/search.html?keyword=" + Uri.encode(term);
        try {
            //JSONObject searchResult = new JSONObject(getHttpContent(searchUrl));
            HttpURLConnection urlConnection = SimpleHttpClient.getURLConnection(searchUrl);
            //urlConnection.setInstanceFollowRedirects(true);
            if(SimpleHttpClient.getResponseCode(urlConnection) == 301) {
                String movedLocation = urlConnection.getHeaderField("Location").replace("http://", "https://");
                urlConnection = SimpleHttpClient.getURLConnection(movedLocation);
                //configConnection(urlConnection);
            }

            String htmlContent = SimpleHttpClient.getResponse(urlConnection);
            Document doc = Jsoup.parse(htmlContent);
            Elements elements = doc.select("div.img");
            for(Element element: elements) {
                Element linkTag = element.getElementsByTag("a").get(0);
                Element imgTag = linkTag.getElementsByTag("img").get(0);
                AnimeLinkData animeLinkData = new AnimeLinkData();
                animeLinkData.setTitle(linkTag.attr("title"));
                animeLinkData.setUrl(ProvidersData.GOGOANIME.URL + linkTag.attr("href"));
                Map<String, String> data = new HashMap<>();
                data.put(AnimeLinkData.DataContract.DATA_IMAGE_URL, imgTag.attr("src"));
                data.put(AnimeLinkData.DataContract.DATA_MODE, "advanced");
                data.put(AnimeLinkData.DataContract.DATA_SOURCE, ProvidersData.GOGOANIME.NAME);
                animeLinkData.setData(data);
                result.add(animeLinkData);
            }
            Log.d(TAG, "Found " + result.size() + " results");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public boolean hasQuickSearch() {
        return true;
    }

    @Override
    public String getDisplayName() {
        return ProvidersData.GOGOANIME.NAME;
    }
}
