package com.dhavalpateln.linkcast.animescrappers;

import android.util.Log;

import com.dhavalpateln.linkcast.ProvidersData;
import com.dhavalpateln.linkcast.database.AnimeLinkData;
import com.dhavalpateln.linkcast.database.VideoURLData;
import com.dhavalpateln.linkcast.database.EpisodeNode;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StreamTapeExtractor extends AnimeScrapper {

    private String TAG = "StreamTape";
    private String displayName;

    public StreamTapeExtractor() {
        super();
        displayName = ProvidersData.STREAMTAPE.NAME;
    }

    public StreamTapeExtractor(String name) {
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
        try {
            Pattern urlPattern = Pattern.compile("'robotlink'\\)\\.innerHTML = '(.+?)'\\+ \\('xcd(.+?)'\\)");
            String htmlContent = getHttpContent(episodeUrl);
            Matcher matcher = urlPattern.matcher(htmlContent);
            if(matcher.find()) {
                String redirectUrl = getRedirectUrl("https:" + matcher.group(1) + matcher.group(2));
                if(redirectUrl != null) {
                    VideoURLData videoURLData = new VideoURLData(ProvidersData.STREAMTAPE.NAME, this.displayName, redirectUrl, null);
                    result.add(videoURLData);
                }
                Log.d(TAG, "complete");
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
