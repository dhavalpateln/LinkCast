package com.dhavalpateln.linkcast.extractors.embedsito;

import com.dhavalpateln.linkcast.database.AnimeLinkData;
import com.dhavalpateln.linkcast.database.VideoURLData;
import com.dhavalpateln.linkcast.extractors.AnimeExtractor;
import com.dhavalpateln.linkcast.database.EpisodeNode;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

public class EmbedSitoExtractor extends AnimeExtractor {

    public EmbedSitoExtractor(String baseUrl) {
        super();
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
        try {
            String searchUrl = episodeUrl.replace("https://embedsito.com/f/", "https://embedsito.com/api/source/");
            String jsonSearchResultString = postHttpContent(searchUrl);
            JSONObject jsonSearchResult = new JSONObject(jsonSearchResultString);
            JSONArray episodeList = jsonSearchResult.getJSONArray("data");
            for(int i = 0; i < episodeList.length(); i++) {
                JSONObject episode = episodeList.getJSONObject(i);
                VideoURLData urlData = new VideoURLData("Embed", "Embed - " + episode.getString("label"), episode.getString("file"), null);
                result.add(urlData);
            }
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<EpisodeNode> extractData(AnimeLinkData data) {
        return null;
    }
    @Override
    public String getDisplayName() {
        return "EmbedSito";
    }
}
