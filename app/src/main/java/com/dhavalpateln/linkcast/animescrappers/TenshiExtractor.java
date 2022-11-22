package com.dhavalpateln.linkcast.animescrappers;

import com.dhavalpateln.linkcast.ProvidersData;
import com.dhavalpateln.linkcast.database.AnimeLinkData;
import com.dhavalpateln.linkcast.database.VideoURLData;
import com.dhavalpateln.linkcast.utils.EpisodeNode;
import com.dhavalpateln.linkcast.utils.SimpleHttpClient;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

public class TenshiExtractor extends AnimeScrapper {
    @Override
    public boolean isCorrectURL(String url) {
        return url.startsWith(ProvidersData.TENSHI.URL);
    }

    @Override
    public List<EpisodeNode> getEpisodeList(String episodeListUrl) {
        List<EpisodeNode> result = new ArrayList<>();
        try {
            SimpleHttpClient.bypassDDOS(ProvidersData.TENSHI.URL);
            HttpURLConnection urlConnection = SimpleHttpClient.getURLConnection(episodeListUrl);
            Document document = Jsoup.parse(SimpleHttpClient.getResponse(urlConnection));
            Elements episodeElements = document.selectFirst("ul.loop.episode-loop").select("li");
            for(Element element: episodeElements) {
                EpisodeNode node = new EpisodeNode(
                        element.selectFirst("div.episode-number").text().split(" ")[1].trim(),
                        element.selectFirst("a").attr("href")
                );
                node.setTitle(element.selectFirst("div.episode-label").text());
                result.add(node);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public void extractEpisodeUrls(String episodeUrl, List<VideoURLData> result) {
        try {
            HttpURLConnection episodePageConnection = SimpleHttpClient.getURLConnection(episodeUrl);
            Document episodePageDoc = Jsoup.parse(SimpleHttpClient.getResponse(episodePageConnection));
            String videoSrcUrl = episodePageDoc.selectFirst("iframe").attr("src");
            HttpURLConnection sourceUrlConnection = SimpleHttpClient.getURLConnection(videoSrcUrl);
            Document sourceDoc = Jsoup.parse(SimpleHttpClient.getResponse(sourceUrlConnection));
            Elements sources = sourceDoc.selectFirst("video").select("source");
            for(Element source: sources) {
                VideoURLData videoURLData = new VideoURLData(source.attr("src"));
                videoURLData.setTitle(source.attr("size"));
                videoURLData.setPlayable(true);
                result.add(videoURLData);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<EpisodeNode> extractData(AnimeLinkData data) {
        return getEpisodeList(data.getUrl());
    }

    @Override
    public String getDisplayName() {
        return ProvidersData.TENSHI.NAME;
    }
}
