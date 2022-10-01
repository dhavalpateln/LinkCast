package com.dhavalpateln.linkcast.animescrappers;

import static com.dhavalpateln.linkcast.utils.Utils.hexlify;

import android.net.Uri;
import android.util.Log;

import com.dhavalpateln.linkcast.ProvidersData;
import com.dhavalpateln.linkcast.database.AnimeLinkData;
import com.dhavalpateln.linkcast.database.VideoURLData;
import com.dhavalpateln.linkcast.utils.EpisodeNode;
import com.dhavalpateln.linkcast.utils.SimpleHttpClient;
import com.dhavalpateln.linkcast.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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

            //Pattern contentIDPattern = Pattern.compile("/e/([^?#&/.]+)");
            //Matcher matcher = contentIDPattern.matcher(url);
            boolean foundDirect = false;

            try {
                String contentID = url.split("/e/")[1].split("\\.html")[0];
                String contentURL = "https://" + uri.getHost();
                /*String sourceInfoURL = contentURL + "/sources41/616e696d646c616e696d646c7c7c"
                        + new String(hexlify(contentID.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8)
                        + "7c7c616e696d646c616e696d646c7c7c73747265616d7362/616e696d646c616e696d646c7c7c363136653639366436343663363136653639366436343663376337633631366536393664363436633631366536393664363436633763376336313665363936643634366336313665363936643634366337633763373337343732363536313664373336327c7c616e696d646c616e696d646c7c7c73747265616d7362";*/
                String sourceInfoURL = "https://streamsss.net/sources48/"
                        + Utils.bytesToString(Utils.hexlify(Utils.stringToBytes("||" + contentID + "||||streamsb")))+ "/";

                HttpURLConnection urlConnection = SimpleHttpClient.getURLConnection(sourceInfoURL);
                urlConnection.setRequestProperty("watchsb", "sbstream");
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
                foundDirect = true;

            } catch (Exception e) {
                Log.d(TAG, "Error finding stream url");
            }
            if(!foundDirect) {
                if (url.contains("/e/")) {
                    url = url.replace("/e/", "/d/") + ".html";
                }
                HttpURLConnection urlConnection = SimpleHttpClient.getURLConnection(url);
                Document doc = Jsoup.parse(SimpleHttpClient.getResponse(urlConnection));
                Elements tableLinks = doc.selectFirst("table").select("a");
                for (Element tableLink : tableLinks) {
                    String quality = tableLink.text().split(" ")[0];
                    String downloadMethod = tableLink.attr("onclick");
                    Pattern paramPattern = Pattern.compile("'(.*?)','(.?)','(.*?)'");
                    Matcher paramMatcher = paramPattern.matcher(downloadMethod);
                    if (paramMatcher.find()) {
                        String downloadURL = "https://streamsss.net/dl?op=download_orig" +
                                "&id=" + paramMatcher.group(1) +
                                "&mode=" + paramMatcher.group(2) +
                                "&hash=" + paramMatcher.group(3);
                        VideoURLData urlData = new VideoURLData(getDisplayName(), "Stream SB - " + quality, downloadURL, null);
                        result.add(urlData);
                    }
                }
            }
        } catch (IOException e) {
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
