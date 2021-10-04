package com.dhavalpateln.linkcast.animescrappers;

import android.util.Log;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VidStreamingExtractor extends AnimeScrapper {

    private String TAG = "AnimeKisaTV - Extractor";

    public VidStreamingExtractor(String baseUrl) {
        super(baseUrl);
        setData("domain", "https://animekisa.tv/");
    }

    @Override
    public boolean isCorrectURL(String url) {
        return false;
    }

    @Override
    public Map<String, String> getEpisodeList(String episodeListUrl) throws IOException {
        if(!episodeList.containsKey(episodeListUrl)) {
            String baseHtmlContent = getHttpContent(episodeListUrl);
            String lines[] = baseHtmlContent.split("\n");
            getEpisodeList(episodeListUrl, lines, 0);
        }
        return episodeList.get(episodeListUrl);
    }

    private void getEpisodeList(String episodeListUrl, String[] lines, int startLineNum) {
        episodeList.put(episodeListUrl, new HashMap<>());
        for(int linenum = startLineNum; linenum < lines.length; linenum++) {
            String line = lines[linenum].trim();
            if(line.startsWith("<a class=\"infovan\"")) {
                Pattern pattern = Pattern.compile("<a class=\"infovan\" href=\"(.*?)\">");
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    String episodeUrl = getData("domain") + matcher.group(1);
                    String episodeNum = episodeUrl.split("-episode-")[1];
                    episodeList.get(episodeListUrl).put(episodeNum, episodeUrl);
                }

            }
        }
    }

    @Override
    public Map<String, String> extractEpisodeUrls(String episodeUrl) throws IOException {

        Map<String, String> result = new HashMap<>();
        String order = "dummy";

        String baseHtmlContent = getHttpContent(episodeUrl);
        String downloadEpisodeLink = null;
        for(String line: baseHtmlContent.split("\n")) {
            if(line.contains("var VidStreaming =")) {
                Pattern pattern = Pattern.compile("var VidStreaming = \"(.*?)\";");
                Matcher matcher = pattern.matcher(baseHtmlContent);
                if (matcher.find()) {
                    downloadEpisodeLink = matcher.group(1);
                    break;
                }
            }
        }
        if(downloadEpisodeLink != null) {
            String downloadHtmlContent = getHttpContent(downloadEpisodeLink.replace("load.php", "download"));
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
                    if(line.startsWith("<div class=\"infopicbox\">")) {
                        Pattern pattern = Pattern.compile("<img class=\"posteri\" .* src=\"(.*?)\"");
                        Matcher matcher = pattern.matcher(line);
                        if (matcher.find()) {
                            setData("imageUrl", getData("domain") + matcher.group(1));
                            foundImage = true;
                        }
                    }
                }
                if(!foundTitle) {
                    if(line.startsWith("<h1 class=\"infodes\"")) {
                        Pattern pattern = Pattern.compile("<h1 .*>(.*?)</h1>");
                        Matcher matcher = pattern.matcher(line);
                        if (matcher.find()) {
                            setData("animeTitle", matcher.group(1));
                            foundTitle = true;
                        }
                    }
                }
                if(!foundEpisodeListStartLineNum && line.startsWith("<a class=\"infovan\"")) {
                    episodeListStartLineNum = linenum;
                    foundEpisodeListStartLineNum = true;
                }
            }
            getEpisodeList(this.baseUrl, lines, episodeListStartLineNum);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    @Override
    public String getDisplayName() {
        return "VidStreaming";
    }
}
