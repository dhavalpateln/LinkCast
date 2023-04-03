package com.dhavalpateln.linkcast.extractors.animepahe;

import com.dhavalpateln.linkcast.ProvidersData;
import com.dhavalpateln.linkcast.animesearch.AnimePaheSearch;
import com.dhavalpateln.linkcast.database.AnimeLinkData;
import com.dhavalpateln.linkcast.database.VideoURLData;
import com.dhavalpateln.linkcast.extractors.AnimeExtractor;
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
import java.util.ArrayList;
import java.util.List;

public class AnimePaheExtractor extends AnimeExtractor {

    private String TAG = "AnimePahe";

    public AnimePaheExtractor() {
        super();
    }

    @Override
    public boolean isCorrectURL(String url) {
        if(url.startsWith("https://animepahe.com/")) return true;
        return false;
    }

    @Override
    public List<EpisodeNode> getEpisodeList(String episodeListUrl) {
        List<EpisodeNode> result = new ArrayList<>();
        try {
            //String basePageUrl = episodeListUrl.split("&sort=")[0] + "&sort=episode_asc";
            String jsonStringContent = getHttpContent(episodeListUrl);
            JSONObject jsonContent = new JSONObject(jsonStringContent);
            int totalEpisodes = jsonContent.getInt("total");
            int episodesPerPage = jsonContent.getInt("per_page");
            int startEpisodeNum = jsonContent.getJSONArray("data").getJSONObject(0).getInt("episode");
            for(int episodeNum = 0; episodeNum < totalEpisodes; episodeNum++) {
                int pageNum = (episodeNum / episodesPerPage) + 1;
                String episodePageUrl = episodeListUrl + "&page=" + pageNum;
                result.add(new EpisodeNode(String.valueOf(episodeNum + 1), episodePageUrl + ":::" + (episodeNum + startEpisodeNum)));
            }
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public void extractEpisodeUrls(String episodeUrl, List<VideoURLData> result) {
        try {
            //String episodeInfoUrl = null;
            String[] episdoeUrlSplit = episodeUrl.split(":::");
            String pageUrl = episdoeUrlSplit[0];
            int episodeNum = Integer.valueOf(episdoeUrlSplit[1]);
            JSONArray sessionData = new JSONObject(getHttpContent(pageUrl)).getJSONArray("data");
            String pageContent = null;
            for(int dataNum = 0; dataNum < sessionData.length(); dataNum++) {
                JSONObject episodeSession = sessionData.getJSONObject(dataNum);
                if(episodeSession.getInt("episode") == episodeNum) {
                    //episodeInfoUrl = "https://animepahe.com/api?m=links&id=" + episodeSession.getInt("anime_id") + "&session=" + episodeSession.getString("session");
                    pageContent = getHttpContent("https://animepahe.ru/play/" + pageUrl.split("&")[1].split("=")[1] + "/" + episodeSession.getString("session"));
                }
            }

            if(pageContent != null) {
                Document doc = Jsoup.parse(pageContent);
                Elements downloadLinks = doc.selectFirst("div#pickDownload").select("a");
                for(Element downloadLink: downloadLinks) {
                    VideoURLData videoURLData = new VideoURLData(
                            downloadLink.text(),
                            downloadLink.text(),
                            downloadLink.attr("href"),
                            "https://kwik.cx/"
                    );
                    result.add(videoURLData);
                }
            }

            /*if(episodeInfoUrl != null) {
                JSONArray episodeUrlList = new JSONObject(getHttpContent(episodeInfoUrl)).getJSONArray("data");
                for(int i = 0; i < episodeUrlList.length(); i++) {
                    JSONObject episodeInfo = episodeUrlList.getJSONObject(i);
                    for (Iterator<String> it = episodeInfo.keys(); it.hasNext(); ) {
                        String res = it.next();
                        String fansub = episodeInfo.getJSONObject(res).getString("fansub");
                        String kwikUrl = episodeInfo.getJSONObject(res).getString("kwik_pahewin");
                        VideoURLData videoURLData = new VideoURLData(fansub, fansub + " - " + res, kwikUrl, "https://kwik.cx/");
                        result.add(videoURLData);
                        Log.d(TAG, fansub + " - " + res + " : " + kwikUrl);
                    }
                }
            }*/
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<EpisodeNode> extractData(AnimeLinkData animeLinkData) {
        try {

            animeLinkData.setTitle(animeLinkData.getTitle().replaceAll("\\(.*\\)$", ""));

            String apiUrl = "https://animepahe.com/api?m=release&id=" +
                    animeLinkData.getAnimeMetaData(AnimeLinkData.DataContract.DATA_ANIMEPAHE_SESSION) +
                    "&sort=episode_asc";

            HttpURLConnection urlConnection = SimpleHttpClient.getURLConnection(apiUrl);
            int responseCode = SimpleHttpClient.getResponseCode(urlConnection);
            if(responseCode == 404) {
                AnimePaheSearch searcher = new AnimePaheSearch();
                List<AnimeLinkData> searchResult = searcher.search(animeLinkData.getTitle());
                for (AnimeLinkData animeData: searchResult) {
                    if(animeData.getTitle().equals(animeLinkData.getTitle())) {
                        animeLinkData.copyFrom(animeData);
                        apiUrl = "https://animepahe.com/api?m=release&id=" +
                                animeLinkData.getAnimeMetaData(AnimeLinkData.DataContract.DATA_ANIMEPAHE_SESSION) +
                                "&sort=episode_asc";
                        animeLinkData.updateAll(true);
                        break;
                    }
                }
            }

            return getEpisodeList(apiUrl);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String getDisplayName() {
        return ProvidersData.ANIMEPAHE.NAME;
    }
}
