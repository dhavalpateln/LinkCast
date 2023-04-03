package com.dhavalpateln.linkcast.mangascrappers;

import com.dhavalpateln.linkcast.ProvidersData;
import com.dhavalpateln.linkcast.utils.EpisodeNode;
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
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MangaReader extends MangaScrapper {

    private boolean isImageURL(String url) {
        try {
            HttpURLConnection imageCheckConnection = (HttpURLConnection) (new URL(url)).openConnection();
            if(imageCheckConnection.getContentType().contains("image/")) {
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public List<EpisodeNode> getChapters(String url) {
        List<EpisodeNode> result = new ArrayList<>();
        try {
            HttpURLConnection urlConnection = SimpleHttpClient.getURLConnection(url);
            Document html = Jsoup.parse(SimpleHttpClient.getResponse(urlConnection));

            EpisodeNode.EpisodeType type = EpisodeNode.EpisodeType.MANHWA;
            Element infoElement = html.selectFirst("div.anisc-info");
            for(Element info: infoElement.select("a.name")) {
                if(info.text().toLowerCase().contains("manga")) {
                    type = EpisodeNode.EpisodeType.MANGA;
                }
            }

            Elements chapterElements = html.selectFirst("div.chapters-list-ul").getElementById("en-chapters").select("li.item.reading-item.chapter-item");
            for(Element chapterElement: chapterElements) {
                EpisodeNode node = new EpisodeNode(
                        chapterElement.attr("data-number"),
                        ProvidersData.MANGAREADER.URL + chapterElement.selectFirst("a").attr("href")
                );
                node.setType(type);
                result.add(node);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public List<String> getPages(String url) {
        List<String> result = new ArrayList<>();
        try {
            HttpURLConnection urlConnection = SimpleHttpClient.getURLConnection(url);
            Document html = Jsoup.parse(SimpleHttpClient.getResponse(urlConnection));
            String id = html.getElementById("wrapper").attr("data-reading-id");
            HttpURLConnection dataURL = SimpleHttpClient.getURLConnection(
                    ProvidersData.MANGAREADER.URL + "/ajax/image/list/chap/" + id + "?quality=high"
            );
            Document imageHTML = Jsoup.parse(
                    new JSONObject(SimpleHttpClient.getResponse(dataURL)).getString("html")
            );
            Elements imageElements = imageHTML.select("div.iv-card");
            for(Element imageElement: imageElements) {
                result.add(imageElement.attr("data-url"));
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        return result;
    }

    @Override
    public boolean isCorrectURL(String url) {
        return url.startsWith(ProvidersData.MANGAREADER.URL);
    }

    @Override
    public String getDisplayName() {
        return ProvidersData.MANGAREADER.NAME;
    }
}
