package com.dhavalpateln.linkcast.animescrappers;

import android.net.Uri;
import android.util.Log;

import com.dhavalpateln.linkcast.ProvidersData;
import com.dhavalpateln.linkcast.database.AnimeLinkData;
import com.dhavalpateln.linkcast.utils.EpisodeNode;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class VidStreamExtractor extends AnimeScrapper {

    private String TAG = "VidStream";

    public VidStreamExtractor() {
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
            Pattern embedURLPattern = Pattern.compile("(.+?/)(?:e(?:mbed)?)/([a-zA-Z0-9]+)");
            Matcher embedMatcher = embedURLPattern.matcher(episodeUrl);
            if (embedMatcher.find()) {
                String host = embedMatcher.group(1);
                String slug = embedMatcher.group(2);
                String infoUrl;
                if(host.equals("https://mcloud.to/")) {
                    infoUrl = host + "info/" + slug;
                    Pattern sKeyPattern = Pattern.compile("window\\.skey = '(.+?)'");
                    Matcher sKeyMatcher = sKeyPattern.matcher(getHttpContent(episodeUrl, "https://9anime.to/"));
                    if(sKeyMatcher.find()) {
                        infoUrl += "?skey=" + Uri.encode(sKeyMatcher.group(1));
                    }
                }
                else {
                    infoUrl = host + "info/" + slug;
                }

                JSONObject infoContent = new JSONObject(
                        getHttpContent(infoUrl, episodeUrl)
                );

                JSONArray sources = infoContent.getJSONObject("media").getJSONArray("sources");
                for(int i = 0; i < sources.length(); i++) {
                    String videoURL = sources.getJSONObject(i).getString("file");
                    String title = Uri.parse(host).getHost().split("\\.")[0];
                    if(sources.length() > 1)    title += " - " + (i + 1);
                    VideoURLData videoURLData = new VideoURLData(getDisplayName(), title, videoURL, episodeUrl);
                    result.add(videoURLData);
                }
                Log.d(TAG, "done");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<EpisodeNode> extractData(AnimeLinkData data) {
        return null;
    }

    @Override
    public String getDisplayName() {
        return ProvidersData.VIDSTREAM.NAME;
    }
}
