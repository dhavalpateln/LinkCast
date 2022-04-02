package com.dhavalpateln.linkcast.animesearch;

import android.util.Log;

import com.dhavalpateln.linkcast.ProvidersData;
import com.dhavalpateln.linkcast.database.AnimeLinkData;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AnimeKisaSiteSearch extends AnimeSearch {

    private String TAG = "AnimeKisaSiteSearch";

    public String searchResult(String urlString, String query) throws IOException {
        String formData = "query=" + query;
        URL url = new URL(urlString);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        configConnection(urlConnection);
        urlConnection.setRequestMethod("POST");
        urlConnection.setDoOutput( true );

        urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        urlConnection.setRequestProperty("Content-Length", String.valueOf(formData.length()));

        try( DataOutputStream wr = new DataOutputStream( urlConnection.getOutputStream())) {
            wr.write(formData.getBytes(StandardCharsets.UTF_8));
        }

        InputStream in = new BufferedInputStream(urlConnection.getInputStream());
        int bufferSize = 1024;
        char[] buffer = new char[bufferSize];
        StringBuilder out = new StringBuilder();
        Reader inr = new InputStreamReader(in, StandardCharsets.UTF_8);
        for (int numRead; (numRead = inr.read(buffer, 0, buffer.length)) > 0; ) {
            out.append(buffer, 0, numRead);
        }
        String result = out.toString();
        return result;
    }

    @Override
    public ArrayList<AnimeLinkData> search(String term) {
        ArrayList<AnimeLinkData> result = new ArrayList<>();
        try {
            String searchResultString = searchResult(ProvidersData.ANIMEKISASITE.SEARCH_URL, term);
            Document doc = Jsoup.parse(searchResultString);
            Elements links = doc.getElementsByTag("a");
            for(Element link: links) {
                try {
                    AnimeLinkData animeLinkData = new AnimeLinkData();
                    Map<String, String> data = new HashMap<>();
                    animeLinkData.setUrl(ProvidersData.ANIMEKISASITE.URL + link.attr("href"));

                    Element imageTag = link.getElementsByTag("img").get(0);
                    data.put(AnimeLinkData.DataContract.DATA_IMAGE_URL, imageTag.attr("src"));
                    animeLinkData.setTitle(imageTag.attr("alt"));
                    animeLinkData.setData(data);
                    result.add(animeLinkData);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            Log.d(TAG, "Search complete");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public String getName() {
        return ProvidersData.ANIMEKISASITE.NAME;
    }

    @Override
    public boolean hasQuickSearch() {
        return true;
    }
}
