package com.dhavalpateln.linkcast.animescrappers;

import android.util.Log;

import com.dhavalpateln.linkcast.database.AnimeLinkData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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

        this.animeUrl = baseUrl;
    }

    @Override
    public boolean isCorrectURL(String url) {
        if(url.startsWith("https://animepahe.com/anime/")) return true;
        return false;
    }

    @Override
    public Map<String, String> getEpisodeList(String episodeListUrl) {
        Map<String, String> map = new HashMap<>();
        try {
            //String basePageUrl = episodeListUrl.split("&sort=")[0] + "&sort=episode_asc";
            String jsonStringContent = getHttpContent(episodeListUrl);
            JSONObject jsonContent = new JSONObject(jsonStringContent);
            int totalEpisodes = jsonContent.getInt("total");
            int episodesPerPage = jsonContent.getInt("per_page");
            int startEpisodeNum = jsonContent.getJSONArray("data").getJSONObject(0).getInt("episode");
            for(int episodeNum = 0; episodeNum < totalEpisodes; episodeNum++) {
                int pageNum = (episodeNum / episodesPerPage) + 1;
                String episodePageUrl = episodeListUrl + "&page=" + pageNum;
                map.put(String.valueOf(episodeNum + 1), episodePageUrl + ":::" + (episodeNum + startEpisodeNum));
            }
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
        return map;
    }

    @Override
    public void extractEpisodeUrls(String episodeUrl, List<VideoURLData> result) {
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
                        String kwikUrl = episodeInfo.getJSONObject(res).getString("kwik_pahewin");
                        VideoURLData videoURLData = new VideoURLData(fansub + " - " + res, kwikUrl, "https://kwik.cx/");
                        result.add(videoURLData);
                        Log.d(TAG, fansub + " - " + res + " : " + kwikUrl);
                    }
                }
            }
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Map<String, String> extractData(AnimeLinkData animeLinkData) {
        try {
            boolean foundImage = getData("imageUrl") != null;
            boolean foundTitle = false;
            Map<String, String> data = animeLinkData.getData();

            if(animeLinkData.getTitle() != null && !animeLinkData.getTitle().equals("")) {
                animeLinkData.setTitle(animeLinkData.getTitle().replace("(" + getDisplayName() + ")", ""));
                setData(AnimeLinkData.DataContract.TITLE, animeLinkData.getTitle());
                setData("animeTitle", animeLinkData.getTitle());
                foundTitle = true;
            }

            if(data.containsKey(AnimeLinkData.DataContract.DATA_IMAGE_URL)) {
                setData(AnimeLinkData.DataContract.DATA_IMAGE_URL, data.get(AnimeLinkData.DataContract.DATA_IMAGE_URL));
                foundImage = true;
            }

            if(!foundImage || !foundTitle) {
                String[] lines = getHttpContent(data.get(AnimeLinkData.DataContract.URL)).split("\n");
                for (int i = 0; i < lines.length; i++) {
                    String line = lines[i].trim();
                    if (!foundImage && line.contains("<div class=\"anime-cover\"")) {
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
                    if (!foundTitle && line.contains("<h1>")) {
                        Pattern pattern = Pattern.compile("<h1>(.*?)</h1>");
                        Matcher matcher = pattern.matcher(line);
                        if (matcher.find()) {
                            setData("animeTitle", matcher.group(1));
                            foundTitle = true;
                        }
                    }
                }
            }

            String apiUrl = "https://animepahe.com/api?m=release&id=" +
                    data.get(AnimeLinkData.DataContract.DATA_ANIMEPAHE_SEARCH_ID) +
                    "&sort=episode_asc";

            return getEpisodeList(apiUrl);
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
