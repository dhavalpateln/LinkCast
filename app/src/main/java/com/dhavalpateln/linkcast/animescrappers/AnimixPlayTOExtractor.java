package com.dhavalpateln.linkcast.animescrappers;

import android.util.Log;

import com.dhavalpateln.linkcast.database.AnimeLinkData;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AnimixPlayTOExtractor extends AnimeScrapper {

    private String TAG = "AnimeKisaCC - Extractor";

    public AnimixPlayTOExtractor(String baseUrl) {
        super(baseUrl);
        setData("domain", "https://animixplay.to/");
    }

    @Override
    public boolean isCorrectURL(String url) {
        return url.startsWith(getData("domain"));
    }

    @Override
    public Map<String, String> getEpisodeList(String episodeListUrl) {
        if(!episodeList.containsKey(episodeListUrl)) {
            return null;
        }
        return episodeList.get(episodeListUrl);
    }

    private void getEpisodeList(String episodeListUrl, String htmlContent) {
        episodeList.put(episodeListUrl, new HashMap<>());
        String[] lines = htmlContent.split("\n");
        for(int linenum = 0; linenum < lines.length; linenum++) {
            String line = lines[linenum].trim();
            if(line.startsWith("<div id=\"epslistplace\">")) {
                String episodeListJSONContent = lines[linenum + 1];
                try {
                    JSONObject episodeListData = new JSONObject(episodeListJSONContent);
                    for (Iterator<String> it = episodeListData.keys(); it.hasNext(); ) {
                        String key = it.next();
                        episodeList.get(episodeListUrl).put(key, "https:" + episodeListData.getString(key));
                    }
                    break;
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    @Override
    public Map<String, VideoURLData> extractEpisodeUrls(String episodeUrl) {

        Map<String, VideoURLData> result = new HashMap<>();
        return result;
    }

    @Override
    public Map<String, String> extractData(AnimeLinkData data) {
        try {
            boolean foundImage = getData("imageUrl") != null;
            boolean foundTitle = false;
            boolean foundEpisodeListStartLineNum = false;
            int episodeListStartLineNum = 0;
            String baseHtmlContent = getHttpContent(this.baseUrl);
            String lines[] = baseHtmlContent.split("\n");
            for(int linenum = 0; linenum < lines.length; linenum++) {
                String line = lines[linenum].trim();
                if(!foundImage) {
                    if(line.contains("var malid = '")) {
                        Pattern pattern = Pattern.compile("var malid = '(.*?)';");
                        Matcher matcher = pattern.matcher(line);
                        if (matcher.find()) {
                            String animeInfoUrl = getData("domain") + "assets/mal/" + matcher.group(1) + ".json";
                            String infoHTMLContent = getHttpContent(animeInfoUrl);
                            try {
                                JSONObject jsonObject = new JSONObject(infoHTMLContent);
                                setData("imageUrl", jsonObject.getString("image_url"));
                                setData("animeTitle", jsonObject.getString("title"));
                                foundImage = true;
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            //setData("imageUrl", matcher.group(1));
                            //foundImage = true;
                        }
                    }
                }
            }
            getEpisodeList(this.baseUrl, baseHtmlContent);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String getDisplayName() {
        return "AnimixPlay";
    }
}
