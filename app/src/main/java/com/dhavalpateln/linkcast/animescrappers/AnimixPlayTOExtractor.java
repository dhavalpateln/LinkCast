package com.dhavalpateln.linkcast.animescrappers;

import android.util.Log;

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
    public Map<String, String> getEpisodeList(String episodeListUrl) throws IOException {
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
    public Map<String, String> extractEpisodeUrls(String episodeUrl) throws IOException {

        Map<String, String> result = new HashMap<>();
        String order = "dummy";

        String downloadEpisodeLink = episodeUrl;
        if(downloadEpisodeLink != null) {
            String downloadHtmlContent = getHttpContent(downloadEpisodeLink.replace("streaming.php", "download").replace("load.php", "download"));
            String[] lines = downloadHtmlContent.split("\n");
            for(int linenum = 0; linenum < lines.length; linenum++) {
                String line = lines[linenum].trim();
                if(line.startsWith("<div class=\"dowload\">")) {
                    Pattern pattern = Pattern.compile("href=\"(.*?)\".*Download (.*?)</a></div>");
                    Matcher matcher = pattern.matcher(lines[linenum + 1]);
                    if (matcher.find()) {
                        String url = matcher.group(1);
                        String source = matcher.group(2);
                        //
                        if(source.toLowerCase().equals("streamsb")) {
                            Log.d(TAG, "StreamSB src");
                            String streamsbContent = getHttpContent(url);
                            for(String streamsbLine: streamsbContent.split("\n")) {
                                streamsbLine = streamsbLine.trim();
                                if(streamsbLine.startsWith("<tr><td><a href=\"#\"")) {
                                    Log.d(TAG, "Found Table row");
                                    Pattern sbpattern = Pattern.compile("<tr><td><a href=\"#\" onclick=\"download_video\\((.*?)\\).*</a></td><td>(.*x.*?),");
                                    Matcher sbmatcher = sbpattern.matcher(streamsbLine);
                                    if (sbmatcher.find()) {
                                        String[] downloadVideoParams = sbmatcher.group(1).replace("'", "").split(",");
                                        String res = sbmatcher.group(2);
                                        String lastDownloadUrl = "https://sbplay.org/dl?op=download_orig&id=" + downloadVideoParams[0] + "&mode=" + downloadVideoParams[1] +
                                                "&hash=" + downloadVideoParams[2];
                                        Log.d(TAG, lastDownloadUrl);

                                        String lastHTMLContent = getHttpContent(lastDownloadUrl);
                                        for(String lastHTMLLine: lastHTMLContent.split("\n")) {
                                            lastHTMLLine = lastHTMLLine.trim();
                                            if(lastHTMLLine.startsWith("<a href=\"") && lastHTMLLine.contains("Direct Download Link")) {
                                                result.put("StreamSB - " + res, lastHTMLLine.split("\"")[1]);
                                                order += ",StreamSB - " + res;
                                                Log.d(TAG, "StreamSB - " + res + " : " + lastHTMLLine.split("\"")[1]);
                                            }
                                        }
                                        if(!result.containsKey("StreamSB - " + res)) {
                                            Log.d(TAG, "error: sleeping and retrying");
                                            try {
                                                Thread.sleep(1000);
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                            lastHTMLContent = getHttpContent(lastDownloadUrl);
                                            for(String lastHTMLLine: lastHTMLContent.split("\n")) {
                                                lastHTMLLine = lastHTMLLine.trim();
                                                if(lastHTMLLine.startsWith("<a href=\"") && lastHTMLLine.contains("Direct Download Link")) {
                                                    result.put("StreamSB - " + res, lastHTMLLine.split("\"")[1]);
                                                    order += ",StreamSB - " + res;
                                                    Log.d(TAG, "StreamSB - " + res + " : " + lastHTMLLine.split("\"")[1]);
                                                }
                                            }
                                            Log.d(TAG, "retry complete");
                                        }
                                    }
                                }
                            }
                        }
                        else if(source.toLowerCase().equals("xstreamcdn")) {
                            try {
                                EmbedSitoExtractor extractor = new EmbedSitoExtractor(url);
                                Map<String, String> episodeUrls = extractor.extractEpisodeUrls(url);
                                for(String res: episodeUrls.keySet()) {
                                    result.put("XstreamCDN - " + res, episodeUrls.get(res));
                                    order += ",XstreamCDN - " + res;
                                }
                            } catch (Exception e) {

                            }

                        }
                        else if(source.toLowerCase().equals("doodstream")) {
                            continue;
                        }
                        else {
                            if(getHttpResponseCode(url) != 200) { continue; }
                            while(result.containsKey(source)) {
                                source = source + "+";
                            }
                            result.put(source, url);
                            order += "," + source;
                        }
                    }
                }
            }
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
