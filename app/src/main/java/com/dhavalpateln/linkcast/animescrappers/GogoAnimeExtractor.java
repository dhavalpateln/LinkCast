package com.dhavalpateln.linkcast.animescrappers;

import android.net.Uri;
import android.util.Log;

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
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GogoAnimeExtractor extends AnimeScrapper {

    private String TAG = "Gogoanime - Extractor";

    public GogoAnimeExtractor() { }

    @Override
    public void configConnection(HttpURLConnection urlConnection) {
        //SimpleHttpClient.setBrowserUserAgent(urlConnection);
        urlConnection.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/95.0.4638.69 Safari/537.36");
        urlConnection.setRequestProperty("Referer", ProvidersData.GOGOANIME.URL);
    }

    @Override
    public boolean isCorrectURL(String url) {
        return url.startsWith(ProvidersData.GOGOANIME.URL);
    }

    @Override
    public List<EpisodeNode> getEpisodeList(String episodeListUrl) {
        List<EpisodeNode> result = new ArrayList<>();
        try {
            HttpURLConnection urlConnection = SimpleHttpClient.getURLConnection(episodeListUrl);
            configConnection(urlConnection);
            if(SimpleHttpClient.getResponseCode(urlConnection) == 301) {
                episodeListUrl = urlConnection.getHeaderField("Location").replace("http://", "https://");
                urlConnection = SimpleHttpClient.getURLConnection(episodeListUrl);
                configConnection(urlConnection);
            }

            //Uri gogoURI = Uri.parse(episodeListUrl);

            Log.d(TAG, "Url = " + episodeListUrl);
            Uri sourceURI = Uri.parse(episodeListUrl);
            String htmlContent = SimpleHttpClient.getResponse(urlConnection);
            //Document html = Jsoup.parse(htmlContent);

            Pattern idPattern = Pattern.compile("value=\"(.*?)\".*class=\"movie_id\"");
            String id = null;
            for(String line: htmlContent.split("\n")) {
                line = line.trim();
                if(line.startsWith("<input") && line.contains("class=\"movie_id\"")) {
                    Matcher matcher = idPattern.matcher(line);
                    if(matcher.find()) {
                        id = matcher.group(1);
                        break;
                    }
                }
            }
            if(id != null) {
                String loadURL = "https://ajax.gogo-load.com/ajax";
                Uri uri = new Uri.Builder()
                        .appendPath("load-list-episode")
                        .appendQueryParameter("id", id)
                        .appendQueryParameter("ep_start", "0")
                        .appendQueryParameter("ep_end", "100000")
                        .build();
                String episodeListQueryUrl = loadURL + uri.toString();
                String listContent = getHttpContent(episodeListQueryUrl);
                String[] lines = listContent.split("\n");
                Pattern linkPattern = Pattern.compile("href=\"(.*?)\"");
                Pattern episodeNumPattern = Pattern.compile("</span>(.*?)</div>");
                for(int linenum = 0; linenum < lines.length; linenum++) {
                    String line = lines[linenum];
                    if(line.contains("href")) {
                        Matcher linkMatcher = linkPattern.matcher(line);
                        Matcher episodeNumMatcher = episodeNumPattern.matcher(lines[linenum + 1]);
                        if(linkMatcher.find() && episodeNumMatcher.find()) {
                            result.add(new EpisodeNode(episodeNumMatcher.group(1).trim(), sourceURI.getScheme() + "://" + sourceURI.getHost() + linkMatcher.group(1).trim()));
                        }
                    }
                }
                Log.d(TAG, "Extraction complete. Found " + result.size() + " episodes");
            } else {
                Log.e(TAG, "Unable to find anime ID");
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public void extractEpisodeUrls(String episodeUrl, List<VideoURLData> result) {
        try {
            Document html = Jsoup.parse(getHttpContent(episodeUrl));
            try {
                //Element iframeElement = html.getElementsByTag("iframe").get(0);
                //String videStreamUrl = "https:" + iframeElement.attr("src");
                //GogoPlayExtractor extractor = new GogoPlayExtractor(videStreamUrl);
                //extractor.extractEpisodeUrls(videStreamUrl, result);

                Elements sources = html.select("a[data-video]");
                for(Element sourceLink: sources) {
                    String sourceName = sourceLink.text()
                            .replace(sourceLink.getElementsByTag("span").get(0).text(), "")
                            .toLowerCase();
                    String link = sourceLink.attr("data-video");
                    AnimeScrapper extractor = null;
                    switch (sourceName) {
                        case "vidstreaming":
                            link = "https:" + link;
                            extractor = new GogoPlayExtractor();
                            break;
                        case "streamsb":
                            extractor = new StreamSBExtractor();
                            break;
                        case "xstreamcdn":
                            extractor = new XStreamExtractor();
                            break;
                    }
                    if(extractor != null) {
                        extractor.extractEpisodeUrls(link, result);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "error fetching vidstream urls");
            }

            Log.d(TAG, "Episode extraction complete. Found " + result.size() + " urls");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<EpisodeNode> extractData(AnimeLinkData data) {
        if(data.getTitle() == null) {
            String[] urlsplit = data.getUrl().split("/");
            data.setTitle(urlsplit[urlsplit.length - 1].replace("-", " "));
        }
        data.setTitle(data.getTitle().replace("(" + getDisplayName() + ")", ""));
        setData("animeTitle", data.getTitle());
        setData(AnimeLinkData.DataContract.DATA_IMAGE_URL, data.getData().get(AnimeLinkData.DataContract.DATA_IMAGE_URL));
        return getEpisodeList(data.getUrl());
    }

    @Override
    public String getDisplayName() {
        return ProvidersData.GOGOANIME.NAME;
    }
}
