package com.dhavalpateln.linkcast.animescrappers;

import android.net.Uri;
import android.util.Log;

import com.dhavalpateln.linkcast.database.AnimeLinkData;
import com.dhavalpateln.linkcast.utils.EpisodeNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AnimeKisaTVExtractor extends AnimeScrapper {

    private String TAG = "AnimeKisaTV - Extractor";

    public AnimeKisaTVExtractor() {
        super();
        setData("domain", "https://animekisa.tv/");
    }

    @Override
    public boolean isCorrectURL(String url) {
        return url.startsWith(getData("domain"));
    }

    @Override
    public List<EpisodeNode> getEpisodeList(String episodeListUrl) {
        List<EpisodeNode> result = new ArrayList<>();
        try {
            String baseHtmlContent = getHttpContent(episodeListUrl);
            String lines[] = baseHtmlContent.split("\n");
            for(int linenum = 0; linenum < lines.length; linenum++) {
                String line = lines[linenum].trim();
                if(line.startsWith("<a class=\"infovan\"")) {
                    Pattern pattern = Pattern.compile("<a class=\"infovan\" href=\"(.*?)\">");
                    Matcher matcher = pattern.matcher(line);
                    if (matcher.find()) {
                        String episodeUrl = getData("domain") + matcher.group(1);
                        String episodeNum = episodeUrl.split("-episode-")[1];
                        result.add(new EpisodeNode(episodeNum, episodeUrl));
                    }

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public void extractEpisodeUrls(String episodeUrl, List<VideoURLData> result) {
        try {
            Log.d(TAG, "Episode Link:" + episodeUrl);

            String baseHtmlContent = getHttpContent(episodeUrl);
            String downloadEpisodeLink = null;
            for (String line : baseHtmlContent.split("\n")) {
                if (line.contains("var VidStreaming =")) {
                    Pattern pattern = Pattern.compile("var VidStreaming = \"(.*?)\";");
                    Matcher matcher = pattern.matcher(baseHtmlContent);
                    if (matcher.find()) {
                        downloadEpisodeLink = matcher.group(1);
                        break;
                    }
                }
            }

            if (downloadEpisodeLink != null) {
                downloadEpisodeLink = downloadEpisodeLink.replace("load.php", "download");
                downloadEpisodeLink = downloadEpisodeLink.replace("download", "streaming.php");
                downloadEpisodeLink = downloadEpisodeLink.replace("https://gogoplay1.com/", "https://gogoplay4.com/");
                downloadEpisodeLink = downloadEpisodeLink.replace(Uri.parse(downloadEpisodeLink).getHost(), "gogoplay4.com");
                Log.d(TAG, "Download Link:" + downloadEpisodeLink);


                try {
                    GogoPlayExtractor extractor = new GogoPlayExtractor();
                    extractor.extractEpisodeUrls(downloadEpisodeLink, result);
                } catch (Exception e) {
                    Log.e(TAG, "Error extracting vidstream");
                }


                String downloadHtmlContent = getHttpContent(downloadEpisodeLink);
                String[] lines = downloadHtmlContent.split("\n");
                for (int linenum = 0; linenum < lines.length; linenum++) {
                    String line = lines[linenum].trim();
                    if (line.startsWith("<li class=\"")) {
                        Pattern pattern = Pattern.compile("data-video=\"(.*?)\">(.*?)</li>");
                        Matcher matcher = pattern.matcher(line);
                        if (matcher.find()) {
                            String url = matcher.group(1);
                            String source = matcher.group(2);
                            Log.d(TAG, "Url: " + url + ", Source: " + source);
                            //
                            if (source.toLowerCase().equals("streamsb")) {
                                StreamSBExtractor extractor = new StreamSBExtractor();
                                extractor.extractEpisodeUrls(url, result);
                            } else if (source.equalsIgnoreCase("xstreamcdn")) {
                                try {
                                    XStreamExtractor extractor = new XStreamExtractor();
                                    extractor.extractEpisodeUrls(url, result);
                                } catch (Exception e) {
                                    Log.d(TAG, "XstreamCDN - ERROR: " + url);
                                }

                            } else if (source.toLowerCase().equals("doodstream")) {
                                Log.d(TAG, "DOODSTREAM base url: " + url);
                                String doodStreamHtmlContent = getHttpContent(url);
                                String downloadUrl = null;
                                for (String doodStreamline : doodStreamHtmlContent.split("\n")) {
                                    if (doodStreamline.trim().startsWith("<a href=\"")) {
                                        Pattern doodDownloadLinkPattern = Pattern.compile("<a href=\"(/download/.*?)\"");
                                        Matcher doodmatcher = doodDownloadLinkPattern.matcher(doodStreamline);
                                        if (doodmatcher.find()) {
                                            String hostName = Uri.parse(url).getHost();
                                            downloadUrl = "https://" + hostName + doodmatcher.group(1);
                                            break;
                                        }
                                    }
                                }
                                if (downloadUrl != null) {
                                    Log.d(TAG, "DOODSTREAM download url: " + downloadUrl);
                                    String mainContent = getHttpContent(downloadUrl);
                                    for (String httpLine : mainContent.split("\n")) {
                                        if (httpLine.trim().startsWith("<a onclick=\"window.open")) {
                                            Pattern doodDownloadLinkPattern = Pattern.compile("<a onclick=\"window.open\\('(.*?)', '_self'\\)\"");
                                            Matcher doodmatcher = doodDownloadLinkPattern.matcher(httpLine);
                                            if (doodmatcher.find()) {
                                                downloadUrl = doodmatcher.group(1);
                                                VideoURLData videoURLData = new VideoURLData("DOODSTREAM", "DOODSTREAM", downloadUrl, null);
                                                result.add(videoURLData);
                                                Log.d(TAG, "DOODSTREAM" + " : " + downloadUrl);
                                                break;
                                            }
                                        }
                                    }
                                }
                                continue;
                            } else if (source.equalsIgnoreCase("mp4upload")) {
                                continue;
                            } else {

                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Error: " + e.toString());
        }

    }

    @Override
    public List<EpisodeNode> extractData(AnimeLinkData data) {
        try {
            boolean foundImage = getData("imageUrl") != null;
            boolean foundTitle = false;
            String baseHtmlContent = getHttpContent(data.getUrl());
            String lines[] = baseHtmlContent.split("\n");
            for(int linenum = 0; linenum < lines.length; linenum++) {
                String line = lines[linenum].trim();
                if(!foundImage) {
                    if(line.contains("<img class=\"posteri\"")) {
                        Pattern pattern = Pattern.compile("<img class=\"posteri\".*src=\"(.*?)\"");
                        Matcher matcher = pattern.matcher(line);
                        if (matcher.find()) {
                            setData("imageUrl", getData("domain") + matcher.group(1));
                            data.setTitle(getData("imageUrl"));
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
                            data.setTitle(getData("animeTitle"));
                            foundTitle = true;
                        }
                    }
                }
            }
            return getEpisodeList(data.getUrl());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String getDisplayName() {
        return "AnimeKisa.tv";
    }
}
