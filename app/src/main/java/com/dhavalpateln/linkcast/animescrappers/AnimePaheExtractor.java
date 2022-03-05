package com.dhavalpateln.linkcast.animescrappers;

import android.net.Uri;
import android.util.Log;

import com.dhavalpateln.linkcast.animesearch.AnimePaheSearch;
import com.dhavalpateln.linkcast.animesearch.AnimeSearch;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AnimePaheExtractor extends AnimeScrapper{

    private String TAG = "AnimePahe - Extractor";
    private String animeUrl;
    private String apiUrl;
    private String animeTitle;

    public AnimePaheExtractor(String baseUrl) {
        super(baseUrl);
        if(baseUrl.contains(":::")) {
            String[] links = baseUrl.split(":::");
            this.apiUrl = links[0];
            this.animeUrl = links[1];
            this.animeTitle = links[2];
        }
        else {
            this.apiUrl = baseUrl;
            this.animeUrl = baseUrl;
            this.animeTitle = null;
        }
    }

    @Override
    public boolean isCorrectURL(String url) {
        if(url.startsWith("https://animepahe.com/api?m=release")) return true;
        return false;
    }

    @Override
    public Map<String, String> getEpisodeList(String episodeListUrl) throws IOException {
        if(!episodeList.containsKey("fixed")) {
            Map<String, String> map = new HashMap<>();
            try {
                String basePageUrl = episodeListUrl.split("&sort=")[0] + "&sort=episode_asc";
                String jsonStringContent = getHttpContent(basePageUrl);
                JSONObject jsonContent = new JSONObject(jsonStringContent);
                int totalEpisodes = jsonContent.getInt("total");
                int episodesPerPage = jsonContent.getInt("per_page");
                int startEpisodeNum = jsonContent.getJSONArray("data").getJSONObject(0).getInt("episode");
                for(int episodeNum = 0; episodeNum < totalEpisodes; episodeNum++) {
                    int pageNum = (episodeNum / episodesPerPage) + 1;
                    String episodePageUrl = basePageUrl + "&page=" + pageNum;
                    map.put(String.valueOf(episodeNum + 1), episodePageUrl + ":::" + (episodeNum + startEpisodeNum));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            episodeList.put("fixed", map);
            //episodeList.put(baseUrl, map);
        }
        return episodeList.get("fixed");
    }

    @Override
    public Map<String, String> extractEpisodeUrls(String episodeUrl) throws IOException {
        Map<String, String> result = new HashMap<>();
        String order = "dummy";
        try {
            String episodeInfoUrl = null;
            String[] episdoeUrlSplit = episodeUrl.split(":::");
            String pageUrl = episdoeUrlSplit[0];
            int episodeNum = Integer.valueOf(episdoeUrlSplit[1]);
            JSONArray sessionData = new JSONObject(getHttpContent(pageUrl)).getJSONArray("data");
            for(int dataNum = 0; dataNum < sessionData.length(); dataNum++) {
                JSONObject episodeSession = sessionData.getJSONObject(dataNum);
                if(episodeSession.getInt("episode") == episodeNum) {
                    episodeInfoUrl = "https://animepahe.com/api?m=links&id=" + episodeSession.getInt("anime_id") + "&session=" + episodeSession.getString("session");
                }
            }

            if(episodeInfoUrl != null) {
                JSONArray episodeUrlList = new JSONObject(getHttpContent(episodeInfoUrl)).getJSONArray("data");
                for(int i = 0; i < episodeUrlList.length(); i++) {
                    JSONObject episodeInfo = episodeUrlList.getJSONObject(i);
                    for (Iterator<String> it = episodeInfo.keys(); it.hasNext(); ) {
                        String res = it.next();
                        String fansub = episodeInfo.getJSONObject(res).getString("fansub");
                        String kwikUrl = episodeInfo.getJSONObject(res).getString("kwik_adfly");
                        result.put(fansub + " - " + res, kwikUrl);
                        order += "," + fansub + " - " + res;
                        Log.d(TAG, fansub + " - " + res + " : " + kwikUrl);
                        //String kwikContent = getHttpContent(kwikUrl, "https://kwik.cx/");
                        /*for(String kwikContentLine: kwikContent.split("\n")) {
                            if(kwikContentLine.startsWith("<script>eval(function")) {
                                String response = getHttpContent("https://dnpatel.pythonanywhere.com/unpack?data=" + Uri.encode(kwikContentLine));
                                String url = new JSONObject(response).getString("url");
                                result.put(fansub + " - " + res, url);
                                order += "," + fansub + " - " + res;
                                Log.d(TAG, fansub + " - " + res + " : " + url);
                            }
                        }*/
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(!order.equals("dummpy")) {
            result.put("order", order);
        }
        return result;
    }

    @Override
    public String extractData() {
        try {
            boolean foundImage = getData("imageUrl") != null;
            boolean foundTitle = false;

            if(animeTitle != null) {
                AnimePaheSearch animePaheSearch = new AnimePaheSearch();
                JSONArray searchArray = animePaheSearch.search(animeTitle.replace("(AnimePahe.com)", ""));
                JSONObject result = searchArray.getJSONObject(0);
                this.animeUrl = result.getString(AnimeSearch.LINK);
                setData("imageUrl", result.getString(AnimeSearch.IMAGE));
                foundImage = true;
                setData("animeTitle", result.getString(AnimeSearch.TITLE));
                foundTitle = true;
            }

            String[] lines = getHttpContent(animeUrl).split("\n");
            for(int i = 0; i < lines.length; i++) {
                String line = lines[i].trim();
                if(!foundImage && line.contains("<div class=\"anime-cover\"")) {
                    Pattern pattern = Pattern.compile("<img src=\"(.*?)\" ");
                    Matcher matcher = pattern.matcher(lines[i + 1]);
                    if (matcher.find()) {
                        setData("imageUrl", matcher.group(1));
                        foundImage = true;
                    }
                    pattern = Pattern.compile("\"Cover image of (.*?)\" ");
                    matcher = pattern.matcher(lines[i + 1]);
                    if (matcher.find()) {
                        setData("animeTitle", matcher.group(1));
                        foundTitle = true;
                    }
                }
                if(!foundTitle && line.contains("<h1>")) {
                    Pattern pattern = Pattern.compile("<h1>(.*?)</h1>");
                    Matcher matcher = pattern.matcher(line);
                    if (matcher.find()) {
                        setData("animeTitle", matcher.group(1));
                        foundTitle = true;
                    }
                }
            }
            getEpisodeList(apiUrl);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String getDisplayName() {
        return "AnimePahe.com";
    }
}
