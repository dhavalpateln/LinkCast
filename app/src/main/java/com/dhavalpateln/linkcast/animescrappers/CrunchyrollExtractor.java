package com.dhavalpateln.linkcast.animescrappers;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.dhavalpateln.linkcast.ProvidersData;
import com.dhavalpateln.linkcast.database.AnimeLinkData;
import com.dhavalpateln.linkcast.database.VideoURLData;
import com.dhavalpateln.linkcast.database.EpisodeNode;
import com.dhavalpateln.linkcast.utils.SimpleHttpClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CrunchyrollExtractor extends AnimeScrapper {

    private String TAG = "Crunchyroll - Extractor";
    private Set<String> supportedFormats;

    public CrunchyrollExtractor() {
        supportedFormats = new HashSet<>();
        supportedFormats.add("adaptive_dash");
        supportedFormats.add("adaptive_hls");
        supportedFormats.add("multitrack_adaptive_hls_v2");
        supportedFormats.add("vo_adaptive_dash");
        supportedFormats.add("vo_adaptive_hls");
    }

    @Override
    public void configConnection(HttpURLConnection urlConnection) {

    }

    @Override
    public boolean isCorrectURL(String url) {
        return url.startsWith(ProvidersData.CRUNCHYROLL.URL);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public List<EpisodeNode> getEpisodeList(String episodeListUrl) {
        List<EpisodeNode> result = new ArrayList<>();
        try {
            String[] linkData = episodeListUrl.split(":::");
            episodeListUrl = linkData[0];
            String id = linkData[1];
            String garbage = getHttpContent("https://www.crunchyroll.com");
            String apiContent = getHttpContent("https://www.crunchyroll.com/content/v2/cms/seasons/GRK587V16/episodes?locale=en-US");
            HttpURLConnection pageUrlConnection = SimpleHttpClient.getURLConnection(episodeListUrl);
            Document html = Jsoup.parse(SimpleHttpClient.getResponse(pageUrlConnection));
            Elements episodeElements = html.select("a.episode");
            for(Element episodeElement: episodeElements) {
                String url = ProvidersData.CRUNCHYROLL.URL + episodeElement.attr("href");
                try {
                    String episodeNum = episodeElement.selectFirst("span").text().trim().split(" ", 2)[1];
                    EpisodeNode node = new EpisodeNode(episodeNum, url);
                    result.add(node);
                } catch(Exception e) {
                    Log.d(TAG, "Error fetching " + url);
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
            String content = getHttpContent(episodeUrl);
            Pattern pattern = Pattern.compile("vilos\\.config\\.media = (\\{.+\\})");
            Matcher matcher = pattern.matcher(content);
            if(matcher.find()) {
                JSONObject data = new JSONObject(matcher.group(1));

                JSONArray subtitles = data.getJSONArray("subtitles");
                String subtitle = null;
                for(int i = 0; i < subtitles.length(); i++) {
                    JSONObject subtitleObj = subtitles.getJSONObject(i);
                    if(subtitleObj.getString("language").equals("enUS")) {
                        subtitle = subtitleObj.getString("url");
                    }
                }

                JSONArray streams = data.getJSONArray("streams");
                for(int i = 0; i < streams.length(); i++) {
                    JSONObject streamObj = streams.getJSONObject(i);
                    if(supportedFormats.contains(streamObj.getString("format"))) {
                        if(!streamObj.has("hardsub_lang") || streamObj.getString("hardsub_lang").equals("enUS")) {
                            VideoURLData videoURLData = new VideoURLData();
                            videoURLData.setPlayable(true);
                            videoURLData.setDownloadable(false);
                            videoURLData.setUrl(streamObj.getString("url"));
                            videoURLData.setTitle(streamObj.getString("format"));
                            videoURLData.setSource(ProvidersData.CRUNCHYROLL.NAME);
                            if(!streamObj.has("hardsub_lang") && subtitle != null) {
                                videoURLData.addSubtitle(subtitle);
                            }
                            result.add(videoURLData);
                        }
                    }
                }
            }
            Log.d(TAG, "Episode extraction complete. Found " + result.size() + " urls");
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<EpisodeNode> extractData(AnimeLinkData data) {
        return getEpisodeList(data.getUrl());
    }

    @Override
    public String getDisplayName() {
        return ProvidersData.CRUNCHYROLL.NAME;
    }
}
