package com.dhavalpateln.linkcast.animescrappers;

import static java.net.URLDecoder.decode;

import com.dhavalpateln.linkcast.ProvidersData;
import com.dhavalpateln.linkcast.database.AnimeLinkData;
import com.dhavalpateln.linkcast.database.VideoURLData;
import com.dhavalpateln.linkcast.utils.EpisodeNode;
import com.dhavalpateln.linkcast.utils.SimpleHttpClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MarinExtractor extends AnimeScrapper {

    private String xsrf = null;
    private String cookie = null;
    private String ddosCookie = ";__ddg1_=;__ddg2_=;";

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
    public boolean isCorrectURL(String url) {
        return url.startsWith(ProvidersData.MARIN.URL);
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

    private JSONObject getAppData(String htmlContent) throws JSONException {
        Document document = Jsoup.parse(htmlContent);
        return new JSONObject(document.getElementById("app").attr("data-page"));
    }

    @Override
    public List<EpisodeNode> getEpisodeList(String episodeListUrl) {
        List<EpisodeNode> result = new ArrayList<>();
        try {
            HttpURLConnection urlConnection = SimpleHttpClient.getURLConnection(episodeListUrl);
            JSONObject appData = getAppData(SimpleHttpClient.getResponse(urlConnection));
            int episodeCount = appData.getJSONObject("props").getJSONObject("episode_list").getJSONObject("meta").getInt("total");
            for(int i = 0; i < episodeCount; i++) {
                String episodeNum = String.valueOf(i + 1);
                EpisodeNode node = new EpisodeNode(
                        episodeNum,
                        episodeListUrl + "/" + episodeNum
                );
                //node.setTitle(episodeData.getString("title"));
                result.add(node);
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public void extractEpisodeUrls(String episodeUrl, List<VideoURLData> result) {
        try {
            HttpURLConnection urlConnection = SimpleHttpClient.getURLConnection(episodeUrl);
            JSONObject appData = getAppData(SimpleHttpClient.getResponse(urlConnection));
            JSONArray videoList = appData.getJSONObject("props").getJSONObject("video_list").getJSONArray("data");
            for(int i = 0; i < videoList.length(); i++) {
                JSONObject videoJSONData = videoList.getJSONObject(i);
                HttpURLConnection videoUrlConnection = SimpleHttpClient.getURLConnection(episodeUrl);
                videoUrlConnection.setRequestMethod("POST");
                configConnection(videoUrlConnection);
                SimpleHttpClient.setBrowserUserAgent(videoUrlConnection);
                JSONObject payload = new JSONObject();
                payload.put("video", videoJSONData.getString("slug"));
                //videoUrlConnection.setRequestProperty("x-requested-with", "XMLHttpRequest");
                SimpleHttpClient.setPayload(videoUrlConnection, payload);

                JSONObject videoData = getAppData(SimpleHttpClient.getResponse(videoUrlConnection));
                videoData = videoData.getJSONObject("props").getJSONObject("video").getJSONObject("data");
                JSONArray mirrors = videoData.getJSONArray("mirror");
                for(int j = 0; j < mirrors.length(); j++) {
                    JSONObject mirror = mirrors.getJSONObject(j);
                    VideoURLData videoLinkData = new VideoURLData(mirror.getJSONObject("code").getString("file"));
                    videoLinkData.setPlayable(true);
                    videoLinkData.setTitle(videoData.getString("title") + " - " + (videoData.getJSONObject("audio").getString("code").equals("jp") ? "SUB" : "DUB") + " - " + mirror.getString("resolution"));
                    videoLinkData.setDownloadable(true);
                    result.add(videoLinkData);
                }

                //VideoURLData videoURLData = new VideoURLData(source.attr("src"));
                //videoURLData.setTitle(source.attr("size"));
                //videoURLData.setPlayable(true);
                //result.add(videoURLData);
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<EpisodeNode> extractData(AnimeLinkData data) {
        return getEpisodeList(data.getUrl());
    }

    @Override
    public String getDisplayName() {
        return ProvidersData.MARIN.NAME;
    }
}
