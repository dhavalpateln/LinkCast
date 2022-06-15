package com.dhavalpateln.linkcast.animescrappers;

import static com.dhavalpateln.linkcast.utils.Utils.hexlify;

import android.net.Uri;
import android.util.Log;

import com.dhavalpateln.linkcast.ProvidersData;
import com.dhavalpateln.linkcast.database.AnimeLinkData;
import com.dhavalpateln.linkcast.database.VideoURLData;
import com.dhavalpateln.linkcast.utils.EpisodeNode;
import com.dhavalpateln.linkcast.utils.SimpleHttpClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StreamSBExtractor extends AnimeScrapper {
    private String TAG = "StreamSB";
    private String displayName;

    public StreamSBExtractor() {
        super();
        displayName = ProvidersData.STREAMSB.NAME;
    }

    public StreamSBExtractor(String name) {
        this.displayName = name;
    }

    @Override
    public boolean isCorrectURL(String url) {
        return false;
    }

    @Override
    public List<EpisodeNode> getEpisodeList(String episodeListUrl) {
        return null;
    }

    @Override
    public void extractEpisodeUrls(String url, List<VideoURLData> result) {
        try {
            Log.d(TAG, "StreamSB src");
            Uri uri = Uri.parse(url);

            Pattern contentIDPattern = Pattern.compile("/e/([^?#&/.]+)");
            Matcher matcher = contentIDPattern.matcher(url);

            if(matcher.find()) {
                String contentID = matcher.group(1);
                String contentURL = "https://" + uri.getHost();
                /*String sourceInfoURL = contentURL + "/sources41/616e696d646c616e696d646c7c7c"
                        + new String(hexlify(contentID.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8)
                        + "7c7c616e696d646c616e696d646c7c7c73747265616d7362/616e696d646c616e696d646c7c7c363136653639366436343663363136653639366436343663376337633631366536393664363436633631366536393664363436633763376336313665363936643634366336313665363936643634366337633763373337343732363536313664373336327c7c616e696d646c616e696d646c7c7c73747265616d7362";*/
                String sourceInfoURL = "https://sbplay2.com/sources43/7361696b6f757c7c"
                        + new String(hexlify(contentID.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8)
                        + "7c7c7361696b6f757c7c73747265616d7362/7361696b6f757c7c363136653639366436343663363136653639366436343663376337633631366536393664363436633631366536393664363436633763376336313665363936643634366336313665363936643634366337633763373337343732363536313664373336327c7c7361696b6f757c7c73747265616d7362";

                HttpURLConnection urlConnection = SimpleHttpClient.getURLConnection(sourceInfoURL);
                urlConnection.setRequestProperty("watchsb", "streamsb");
                SimpleHttpClient.setBrowserUserAgent(urlConnection);

                String responseStr = SimpleHttpClient.getResponse(urlConnection);
                JSONObject response = new JSONObject(responseStr);

                result.add(new VideoURLData(
                        ProvidersData.STREAMSB.NAME,
                        getDisplayName(),
                        response.getJSONObject("stream_data").getString("file"),
                        contentURL
                ));
                result.add(new VideoURLData(
                        ProvidersData.STREAMSB.NAME,
                        getDisplayName() + " - Backup",
                        response.getJSONObject("stream_data").getString("backup"),
                        contentURL
                ));


            }

            /*if (url.contains("/e/")) {
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
                            VideoURLData urlData = new VideoURLData(getDisplayName(), "Stream SB - " + res, lastDownloadUrl, null);
                            result.add(urlData);
                        }
                    }
                }
            }*/
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<EpisodeNode> extractData(AnimeLinkData data) {
        return null;
    }

    @Override
    public String getDisplayName() {
        return this.displayName;
    }
}
