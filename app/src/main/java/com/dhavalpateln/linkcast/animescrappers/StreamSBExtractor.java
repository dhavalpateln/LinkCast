package com.dhavalpateln.linkcast.animescrappers;

import android.util.Log;

import com.dhavalpateln.linkcast.database.AnimeLinkData;
import com.dhavalpateln.linkcast.database.FirebaseDBHelper;
import com.dhavalpateln.linkcast.utils.Utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StreamSBExtractor extends AnimeScrapper {
    private String TAG = "StreamSB";

    public StreamSBExtractor(String baseUrl) {
        super(baseUrl);
    }

    @Override
    public boolean isCorrectURL(String url) {
        return false;
    }

    @Override
    public Map<String, String> getEpisodeList(String episodeListUrl) {
        return null;
    }

    @Override
    public Map<String, VideoURLData> extractEpisodeUrls(String url) {
        Map<String, VideoURLData> result = new HashMap<>();
        try {
            Log.d(TAG, "StreamSB src");
            if (url.contains("/e/")) {
                url = url.replace("/e/", "/d/") + ".html";
            }
            for (int fullretry = 0; fullretry < 1; fullretry++) {
                Log.d(TAG, url);
                String streamsbContent = getHttpContent(url);
                for (String streamsbLine : streamsbContent.split("\n")) {
                    streamsbLine = streamsbLine.trim();
                    if (streamsbLine.startsWith("<tr><td><a href=\"#\"")) {
                        Log.d(TAG, "Found Table row");
                        Pattern sbpattern = Pattern.compile("<tr><td><a href=\"#\" onclick=\"download_video\\((.*?)\\).*</a></td><td>(.*x.*?),");
                        Matcher sbmatcher = sbpattern.matcher(streamsbLine);
                        if (sbmatcher.find()) {
                            String[] downloadVideoParams = sbmatcher.group(1).replace("'", "").split(",");
                            String res = sbmatcher.group(2);
                            String lastDownloadUrl = "https://sbplay.org/dl?op=download_orig&id=" + downloadVideoParams[0] + "&mode=" + downloadVideoParams[1] +
                                    "&hash=" + downloadVideoParams[2];
                            Log.d(TAG, lastDownloadUrl);
                            VideoURLData urlData = new VideoURLData("Stream SB - " + res, lastDownloadUrl, null);
                            result.put(urlData.getTitle(), urlData);

                        /*Log.d(TAG, "error: initial sleep");
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }*/
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
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
