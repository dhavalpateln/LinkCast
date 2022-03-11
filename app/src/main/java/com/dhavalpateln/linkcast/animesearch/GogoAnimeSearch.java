package com.dhavalpateln.linkcast.animesearch;

import android.net.Uri;
import android.util.Log;

import com.dhavalpateln.linkcast.ProvidersData;
import com.dhavalpateln.linkcast.database.AnimeLinkData;

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
import java.util.Map;

public class GogoAnimeSearch extends AnimeSearch {

    private final String TAG = "GogoAnimeSearch";

    /*@Override
    public void configConnection(HttpURLConnection urlConnection) {
        urlConnection.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/95.0.4638.69 Safari/537.36");
    }*/

    @Override
    public ArrayList<AnimeLinkData> search(String term) {
        ArrayList<AnimeLinkData> result = new ArrayList<>();
        String searchUrl = ProvidersData.GOGOANIME.URL + "/search.html?keyword=" + Uri.encode(term);
        try {
            //JSONObject searchResult = new JSONObject(getHttpContent(searchUrl));
            String htmlContent = getHttpContent(searchUrl);
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
    public String getName() {
        return ProvidersData.GOGOANIME.NAME;
    }

    @Override
    public boolean hasQuickSearch() {
        return true;
    }
}
