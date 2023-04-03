package com.dhavalpateln.linkcast.extractors;

import com.dhavalpateln.linkcast.database.AnimeLinkData;
import com.dhavalpateln.linkcast.database.VideoURLData;
import com.dhavalpateln.linkcast.utils.EpisodeNode;
import com.dhavalpateln.linkcast.utils.SimpleHttpClient;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AnimeExtractor extends Source {
    public AnimeExtractor() {
        this.setSourceType(SOURCE_TYPE.ANIME);
    }
    public abstract boolean isCorrectURL(String url);
    public abstract List<EpisodeNode> getEpisodeList(String episodeListUrl);
    public abstract void extractEpisodeUrls(String episodeUrl, List<VideoURLData> result);
    public abstract List<EpisodeNode> extractData(AnimeLinkData data);
}
