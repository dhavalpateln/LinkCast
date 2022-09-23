package com.dhavalpateln.linkcast.animesearch;

import android.net.Uri;
import android.util.Log;

import com.dhavalpateln.linkcast.ProvidersData;
import com.dhavalpateln.linkcast.database.AnimeLinkData;
import com.dhavalpateln.linkcast.utils.SimpleHttpClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TenshiSearch extends AnimeSearch {

    private final String TAG = "TenshiSearch";
    private String csrfToken = "";

    public void init() {
        try {
            SimpleHttpClient.bypassDDOS(ProvidersData.TENSHI.URL);
            HttpURLConnection urlConnection = SimpleHttpClient.getURLConnection(ProvidersData.TENSHI.URL);
            String content = SimpleHttpClient.getResponse(urlConnection);
            Pattern pattern = Pattern.compile("<meta name=\"csrf-token\" content=\"(.*?)\">");
            Matcher matcher = pattern.matcher(content);
            if(matcher.find()) {
                csrfToken = matcher.group(1);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public boolean requiresInit() {return true;}

    private HttpURLConnection getSearchURLConnection(String term) throws IOException {
        String searchUrl = ProvidersData.TENSHI.URL + "/anime/search";
        HttpURLConnection urlConnection = SimpleHttpClient.getURLConnection(searchUrl);
        urlConnection.setRequestMethod("POST");
        SimpleHttpClient.setBrowserUserAgent(urlConnection);
        urlConnection.setRequestProperty("x-csrf-token", csrfToken);
        urlConnection.setRequestProperty("x-requested-with", "XMLHttpRequest");
        urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        urlConnection.setRequestProperty("Accept", "application/json, text/javascript, */*");
        byte[] input = ("q=" + Uri.encode(term)).getBytes("utf-8");
        urlConnection.setRequestProperty("Content-Length", Integer.toString(input.length));
        urlConnection.setDoOutput(true);
        try(OutputStream os = urlConnection.getOutputStream()) {
            os.write(input, 0, input.length);
        }
        return urlConnection;
    }

    @Override
    public ArrayList<AnimeLinkData> search(String term) {
        ArrayList<AnimeLinkData> result = new ArrayList<>();

        try {
            HttpURLConnection urlConnection = getSearchURLConnection(term);
            int responseCode = SimpleHttpClient.getResponseCode(urlConnection);
            Log.d(TAG, "Response Code " + responseCode);
            if(responseCode > 400 && responseCode < 500) {
                init();
                urlConnection = getSearchURLConnection(term);
                responseCode = SimpleHttpClient.getResponseCode(urlConnection);
                Log.d(TAG, "Response Code " + responseCode);
            }
            String content = SimpleHttpClient.getResponse(urlConnection);
            JSONArray resultArray = new JSONArray(content);
            for(int i = 0; i < resultArray.length(); i++) {
                JSONObject data = resultArray.getJSONObject(i);
                AnimeLinkData animeLinkData = new AnimeLinkData();
                animeLinkData.setUrl(data.getString("url"));
                animeLinkData.setTitle(data.getString("title"));
                animeLinkData.updateData(AnimeLinkData.DataContract.DATA_IMAGE_URL, data.getString("cover"), true);
                animeLinkData.updateData(AnimeLinkData.DataContract.DATA_SOURCE, ProvidersData.TENSHI.NAME, true);
                result.add(animeLinkData);
            }
            Log.d(TAG, "Found " + result.size() + " results");
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public String getName() {
        return ProvidersData.TENSHI.NAME;
    }

    @Override
    public boolean hasQuickSearch() {
        return true;
    }
}
