package com.dhavalpateln.linkcast.extractors.zoro;

import android.net.Uri;
import android.util.Log;

import com.dhavalpateln.linkcast.ProvidersData;
import com.dhavalpateln.linkcast.database.AnimeLinkData;
import com.dhavalpateln.linkcast.database.VideoURLData;
import com.dhavalpateln.linkcast.extractors.AnimeExtractor;
import com.dhavalpateln.linkcast.extractors.rapid.RapidCloudExtractor;
import com.dhavalpateln.linkcast.extractors.streamsb.StreamSBExtractor;
import com.dhavalpateln.linkcast.extractors.streamtape.StreamTapeExtractor;
import com.dhavalpateln.linkcast.database.EpisodeNode;
import com.dhavalpateln.linkcast.utils.SimpleHttpClient;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

public class ZoroExtractor extends AnimeExtractor {
    @Override
    public boolean isCorrectURL(String url) {
        return url.startsWith(ProvidersData.ZORO.URL);
    }

    @Override
    public void configConnection(HttpURLConnection urlConnection) {
        urlConnection.setRequestProperty("X-Requested-With", "XMLHttpRequest");
        urlConnection.setRequestProperty("Referer", ProvidersData.ZORO.URL);
        SimpleHttpClient.setBrowserUserAgent(urlConnection);
    }

    @Override
    public List<EpisodeNode> getEpisodeList(String episodeListUrl) {
        List<EpisodeNode> result = new ArrayList<>();
        try {
            String[] urlSplit = episodeListUrl.split("-");
            String slug = urlSplit[urlSplit.length - 1];
            Elements episodeElements = Jsoup.parse(new JSONObject(
                    getHttpContent(ProvidersData.ZORO.URL + "/ajax/v2/episode/list/" + slug)
            ).getString("html")).select("a[title][data-number][data-id]");
            for(Element episodeElement: episodeElements) {
                String episodeNum = episodeElement.attr("data-number");
                if(episodeNum.equals(""))   episodeNum = "0";
                EpisodeNode episodeNode = new EpisodeNode(episodeNum, episodeElement.attr("data-id"));
                episodeNode.setTitle(episodeElement.attr("title"));
                result.add(episodeNode);
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        return result;
    }

    @Override
    public void extractEpisodeUrls(String dataId, List<VideoURLData> result) {
        try {
            Elements serverElements = Jsoup.parse(new JSONObject(
                    getHttpContent(ProvidersData.ZORO.URL + "/ajax/v2/episode/servers?episodeId=" + Uri.encode(dataId))
            ).getString("html")).select("div.server-item");
            for(Element serverElement: serverElements) {

                JSONObject sourceData = new JSONObject(
                        getHttpContent(ProvidersData.ZORO.URL + "/ajax/v2/episode/sources?id=" + Uri.encode(serverElement.attr("data-id")))
                );
                if(sourceData.getString("link").contains("rapid")) {
                    RapidCloudExtractor extractor = new RapidCloudExtractor(serverElement.text() + " - " + serverElement.attr("data-type").toUpperCase());
                    extractor.extractEpisodeUrls(sourceData.getString("link"), result);
                }
                else if(sourceData.getString("link").contains("watchsb")) {
                    StreamSBExtractor extractor = new StreamSBExtractor(serverElement.text() + " - " + serverElement.attr("data-type").toUpperCase());
                    extractor.extractEpisodeUrls(sourceData.getString("link"), result);
                }
                else if(sourceData.getString("link").contains("streamta")) {
                    StreamTapeExtractor extractor = new StreamTapeExtractor(serverElement.text() + " - " + serverElement.attr("data-type").toUpperCase());
                    extractor.extractEpisodeUrls(sourceData.getString("link"), result);
                }
                Log.d("type", "source");
            }
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<EpisodeNode> extractData(AnimeLinkData data) {
        return getEpisodeList(data.getUrl());
    }

    @Override
    public String getDisplayName() {
        return ProvidersData.ZORO.NAME;
    }


}
