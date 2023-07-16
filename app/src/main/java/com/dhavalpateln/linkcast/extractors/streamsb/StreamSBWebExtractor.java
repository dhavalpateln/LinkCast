package com.dhavalpateln.linkcast.extractors.streamsb;

import android.net.Uri;
import android.util.Log;

import com.dhavalpateln.linkcast.ProvidersData;
import com.dhavalpateln.linkcast.database.AnimeLinkData;
import com.dhavalpateln.linkcast.database.EpisodeNode;
import com.dhavalpateln.linkcast.database.VideoURLData;
import com.dhavalpateln.linkcast.explorer.listeners.VideoServerListener;
import com.dhavalpateln.linkcast.extractors.AnimeExtractor;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StreamSBWebExtractor extends AnimeExtractor {
    private String TAG = "StreamSB - Web";
    private String displayName;

    public StreamSBWebExtractor() {
        super();
        displayName = ProvidersData.STREAMSB.NAME;
    }

    public StreamSBWebExtractor(String name) {
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

    public void extractEpisodeUrls(String url, List<VideoURLData> result, VideoServerListener listener) {
        try {
            Log.d(TAG, "StreamSB src");
            if (url.contains("/e/")) {
                url = url.replace("/e/", "/d/") + ".html";
            }
            HttpURLConnection urlConnection = SimpleHttpClient.getURLConnection(url);
            Document doc = Jsoup.parse(SimpleHttpClient.getResponse(urlConnection));
            Elements divLinks = doc.select("div[onclick]");
            for (Element divLink : divLinks) {
                try {
                    String quality = divLink.text().split(" ")[0];
                    String downloadMethod = divLink.attr("onclick");
                    Pattern paramPattern = Pattern.compile("'(.*?)','(.?)','(.*?)'");
                    Matcher paramMatcher = paramPattern.matcher(downloadMethod);
                    if (paramMatcher.find()) {
                        String downloadURL = "https://streamsss.net/dl?op=download_orig" +
                                "&id=" + paramMatcher.group(1) +
                                "&mode=" + paramMatcher.group(2) +
                                "&hash=" + paramMatcher.group(3);
                        VideoURLData urlData = new VideoURLData(getDisplayName(), "Stream SB - " + quality, downloadURL, null);
                        addVideoUrlData(urlData, result, listener);
                    }
                } catch (Exception e) {
                    Log.d(TAG, "Error in fetching stream SB web view mode url");
                }
            }
            //}
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
