package com.dhavalpateln.linkcast.animescrappers;

import android.net.Uri;
import android.util.Log;

import com.dhavalpateln.linkcast.database.AnimeLinkData;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GogoAnimeExtractor extends AnimeScrapper {

    private String TAG = "Gogoanime - Extractor";

    public GogoAnimeExtractor(String baseUrl) {
        super(baseUrl);
        setData("domain", "https://www3.gogoanime.cm/");
    }

    @Override
    public boolean isCorrectURL(String url) {
        return false;
    }

    @Override
    public Map<String, String> getEpisodeList(String episodeListUrl) throws IOException {
        return null;
    }

    @Override
    public Map<String, String> extractEpisodeUrls(String episodeUrl) throws IOException {
        episodeUrl = episodeUrl.replace("https://animekisa.tv/", "https://www3.gogoanime.cm/");

        Map<String, String> result = new HashMap<>();
        String order = "dummy";

        Log.d(TAG, "Episode Link:" + episodeUrl);

        String streamSBUrl = null;
        String downloadEpisodeLink = null;

        String baseHtmlContent = getHttpContent(episodeUrl);
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
            downloadEpisodeLink = downloadEpisodeLink.replace("load.php", "download");
            downloadEpisodeLink = downloadEpisodeLink.replace("https://gogoplay1.com/", "https://gogoplay.io/");
            Log.d(TAG, "Download Link:" + downloadEpisodeLink);
            String downloadHtmlContent = getHttpContent(downloadEpisodeLink);
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
                            StreamSBExtractor extractor = new StreamSBExtractor(url);
                            Map<String, String> episodeUrls = extractor.extractEpisodeUrls(url);
                            for(String res: episodeUrls.keySet()) {
                                result.put("StreamSB - " + res, episodeUrls.get(res));
                                order += ",StreamSB - " + res;
                                Log.d(TAG, "StreamSB - " + res + " : " + episodeUrls.get(res));
                            }
                        }
                        else if(source.equalsIgnoreCase("xstreamcdn")) {
                            try {
                                XStreamExtractor extractor = new XStreamExtractor(url);
                                Map<String, String> episodeUrls = extractor.extractEpisodeUrls(url);
                                for(String res: episodeUrls.keySet()) {
                                    result.put("XstreamCDN - " + res, episodeUrls.get(res));
                                    order += ",XstreamCDN - " + res;
                                    Log.d(TAG, "XstreamCDN - " + res + " : " + episodeUrls.get(res));
                                }
                            } catch (Exception e) {
                                Log.d(TAG, "XstreamCDN - ERROR: " + url);
                            }

                        }
                        else if(source.toLowerCase().equals("doodstream")) {
                            Log.d(TAG, "DOODSTREAM base url: " + url);
                            String doodStreamHtmlContent = getHttpContent(url);
                            String downloadUrl = null;
                            for(String doodStreamline: doodStreamHtmlContent.split("\n")) {
                                if(doodStreamline.trim().startsWith("<a href=\"")) {
                                    Pattern doodDownloadLinkPattern = Pattern.compile("<a href=\"(/download/.*?)\"");
                                    Matcher doodmatcher = doodDownloadLinkPattern.matcher(doodStreamline);
                                    if (doodmatcher.find()) {
                                        String hostName = Uri.parse(url).getHost();
                                        downloadUrl = "https://" + hostName + doodmatcher.group(1);
                                        break;
                                    }
                                }
                            }
                            if(downloadUrl != null) {
                                Log.d(TAG, "DOODSTREAM download url: " + downloadUrl);
                                String mainContent = getHttpContent(downloadUrl);
                                for(String httpLine: mainContent.split("\n")) {
                                    if(httpLine.trim().startsWith("<a onclick=\"window.open")) {
                                        Pattern doodDownloadLinkPattern = Pattern.compile("<a onclick=\"window.open\\('(.*?)', '_self'\\)\"");
                                        Matcher doodmatcher = doodDownloadLinkPattern.matcher(httpLine);
                                        if (doodmatcher.find()) {
                                            downloadUrl = doodmatcher.group(1);
                                            result.put("DOODSTREAM", downloadUrl);
                                            order += ",DOODSTREAM";
                                            Log.d(TAG, "DOODSTREAM" + " : " + downloadUrl);
                                            break;
                                        }
                                    }
                                }
                            }
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
    public Map<String, String> extractData(AnimeLinkData data) {
        return null;
    }

    @Override
    public String getDisplayName() {
        return null;
    }
}
