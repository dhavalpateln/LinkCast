package com.dhavalpateln.linkcast.extractors.streamsb;

import android.net.Uri;
import android.util.Log;

import com.dhavalpateln.linkcast.ProvidersData;
import com.dhavalpateln.linkcast.database.AnimeLinkData;
import com.dhavalpateln.linkcast.database.VideoURLData;
import com.dhavalpateln.linkcast.explorer.listeners.VideoServerListener;
import com.dhavalpateln.linkcast.extractors.AnimeExtractor;
import com.dhavalpateln.linkcast.database.EpisodeNode;
import com.dhavalpateln.linkcast.utils.SimpleHttpClient;
import com.dhavalpateln.linkcast.utils.Utils;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StreamSBExtractor extends AnimeExtractor {
    private String TAG = "StreamSB";
    private String displayName;

    private String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

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
    public void extractEpisodeUrls(String episodeUrl, List<VideoURLData> result) {
        extractEpisodeUrls(episodeUrl, result, null);
    }

    @Override
    public void extractEpisodeUrls(String episodeUrl, VideoServerListener listener) {
        extractEpisodeUrls(episodeUrl, null, listener);
    }

    private String encode(String id) {
        String code = makeID() + "||" + id + "||" + makeID() + "||" + "streamsb";
        StringBuilder sb = new StringBuilder();
        char[] codeArr = code.toCharArray();
        for (char c: codeArr) {
            sb.append(Integer.toHexString(c));
        }
        return sb.toString();
    }

    private String makeID() {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i <= 12; i++) {
            sb.append(alphabet.charAt((int) Math.floor(random.nextDouble() * alphabet.length())));
        }
        return sb.toString();
    }

    public void extractEpisodeUrls(String url, List<VideoURLData> result, VideoServerListener listener) {
        Log.d(TAG, "StreamSB src");
        Uri uri = Uri.parse(url);

        try {
            String contentID = url.split("/e/")[1].split("\\.html")[0];
            String contentURL = "https://" + uri.getHost();

            /*String sourceInfoURL = contentURL + "/sources41/616e696d646c616e696d646c7c7c"
                    + new String(hexlify(contentID.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8)
                    + "7c7c616e696d646c616e696d646c7c7c73747265616d7362/616e696d646c616e696d646c7c7c363136653639366436343663363136653639366436343663376337633631366536393664363436633631366536393664363436633763376336313665363936643634366336313665363936643634366337633763373337343732363536313664373336327c7c616e696d646c616e696d646c7c7c73747265616d7362";*/

            /*String sourceInfoURL = "https://streamsss.net/sources16/"
                    + Utils.bytesToString(Utils.hexlify(Utils.stringToBytes("||" + contentID + "||||streamsb")))+ "/";*/

            String sourceInfoURL = contentURL + "/375664356a494546326c4b797c7c6e756577776778623171737/" + encode(contentID);

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

        } catch (Exception e) {
            Log.d(TAG, "Error finding stream url");
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
