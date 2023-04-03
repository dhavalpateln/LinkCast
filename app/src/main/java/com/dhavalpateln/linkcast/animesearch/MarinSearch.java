package com.dhavalpateln.linkcast.animesearch;

import static java.net.URLDecoder.decode;

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

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;


public class MarinSearch extends AnimeSearch {

    private final String TAG = "MarinSearch";
    private boolean initComplete = false;
    private String xsrf = null;
    private String cookie = null;
    private String ddosCookie = ";__ddg1_=;__ddg2_=;";

    public void init() {
        try {
            getCookies();
            initComplete = true;
            Log.d(TAG, "Got cookies");

        } catch (IOException e) {
            e.printStackTrace();
        }
        //HttpURLConnection urlConnection = SimpleHttpClient.getURLConnection(ProvidersData.MARIN.URL);
        //String content = SimpleHttpClient.getResponse(urlConnection);
        //Pattern pattern = Pattern.compile("<meta name=\"csrf-token\" content=\"(.*?)\">");
        //Matcher matcher = pattern.matcher(content);
        //if(matcher.find()) {
        //    csrfToken = matcher.group(1);
        //}


    }

    private Map<String, String> getCookies() throws IOException {
        Map<String, String> result = new HashMap<>();
        if(cookie == null) {
            SimpleHttpClient.bypassDDOS(ProvidersData.MARIN.URL);
            HttpURLConnection urlConnection = SimpleHttpClient.getURLConnection(ProvidersData.MARIN.URL);
            SimpleHttpClient.getResponse(urlConnection);
            List<String> cookieList = urlConnection.getHeaderFields().get("Set-Cookie");
            cookie = String.join(";", cookieList);
            cookie += ddosCookie;
            for(String cookieValue: cookieList) {
                if(cookieValue.startsWith("XSRF-TOKEN")) {
                    xsrf = decode(cookieValue.substring(cookieValue.indexOf("=") + 1, cookieValue.indexOf(";")));
                    cookie += "cutemarinmoe_session=" + xsrf;
                }
            }
        }
        result.put("cookie", cookie);
        result.put("x-xsrf-token", xsrf);
        return result;
    }

    public boolean requiresInit() {return !initComplete;}

    @Override
    public void configConnection(HttpURLConnection urlConnection) {
        try {
            Map<String, String> headers = getCookies();
            for(Map.Entry<String, String> entry: headers.entrySet()) {
                urlConnection.setRequestProperty(entry.getKey(), entry.getValue());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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
    public String getName() {
        return ProvidersData.MARIN.NAME;
    }

    @Override
    public boolean hasQuickSearch() {
        return true;
    }
}
