package com.dhavalpateln.linkcast.animescrappers;

import android.util.Log;
import android.widget.Toast;

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
    public Map<String, String> getEpisodeList(String episodeListUrl) throws IOException {
        return null;
    }

    @Override
    public Map<String, String> extractEpisodeUrls(String url) throws IOException {
        Map<String, String> result = new HashMap<>();
        Log.d(TAG, "StreamSB src");
        if(url.contains("/e/")) {
            url = url.replace("/e/", "/d/") + ".html";
        }
        for(int fullretry = 0; fullretry < 1; fullretry++) {
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
                        result.put(res, lastDownloadUrl);

                        /*Log.d(TAG, "error: initial sleep");
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }*/


                        /* NOT WORKING */
                        int retryCount = 0;
                        while (retryCount > 0 && !result.containsKey(res)) {
                            Log.d(TAG, "sleeping and trying");
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            String lastHTMLContent = getHttpContent(lastDownloadUrl);
                            for (String lastHTMLLine : lastHTMLContent.split("\n")) {
                                lastHTMLLine = lastHTMLLine.trim();
                                if(lastHTMLLine.startsWith("<br><b class=\"err\">You have to wait ")) {
                                    try {
                                        Log.d(TAG, "Sleeping" + lastHTMLLine.split(" ")[5]);
                                        Thread.sleep(Long.valueOf(lastHTMLLine.split(" ")[5]) * 1000);
                                    } catch (Exception e) {

                                    }
                                }
                                if (lastHTMLLine.startsWith("<a href=\"") && lastHTMLLine.contains("Direct Download Link")) {
                                    result.put(res, lastHTMLLine.split("\"")[1]);
                                    Log.d(TAG, "StreamSB - " + res + " : " + lastHTMLLine.split("\"")[1]);
                                }
                            }
                            if (!result.containsKey(res)) {
                                Log.d(TAG, "StreamSB-Error");
                                result.put(res, lastDownloadUrl);
                                FirebaseDBHelper.getUserDataRef().child("sberror").child(Utils.getCurrentTime()).child(res).setValue(lastHTMLContent);
                            }
                            retryCount--;
                        }
                    }
                }
            }
        }
        return result;
    }

    @Override
    public String extractData() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return null;
    }
}
