package com.dhavalpateln.linkcast.animescrappers;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.dhavalpateln.linkcast.ProvidersData;
import com.dhavalpateln.linkcast.database.AnimeLinkData;
import com.dhavalpateln.linkcast.database.VideoURLData;
import com.dhavalpateln.linkcast.utils.EpisodeNode;
import com.dhavalpateln.linkcast.utils.SimpleHttpClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class RapidCloudExtractor extends AnimeScrapper {

    private String displayName;

    public RapidCloudExtractor(String name) {
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
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

                Uri uri = Uri.parse(episodeUrl);

                Map<String, String> headers = new HashMap<>();
                headers.put("referer", ProvidersData.ZORO.URL);
                String sid = wss();
                Map<String, String> captcha = CaptchaSolver.bypassCaptcha(episodeUrl, headers);
                String contentID = episodeUrl.split("embed-6/")[1].split("\\?z=")[0];

                Uri sourceURI = new Uri.Builder()
                        .scheme("https")
                        .authority(uri.getAuthority())
                        .appendPath("ajax")
                        .appendPath("embed-6")
                        .appendPath("getSources")
                        .appendQueryParameter("id", contentID)
                        .appendQueryParameter("_token", captcha.getOrDefault("token", ""))
                        .appendQueryParameter("_number", captcha.getOrDefault("number", ""))
                        .build();

                JSONObject sources = SimpleHttpClient.getJSONResponse(SimpleHttpClient.getURLConnection(sourceURI.toString()));

                List<String> subtitles = new ArrayList<>();

                JSONArray tracks = sources.getJSONArray("tracks");
                for(int i = 0; i < tracks.length(); i++) {
                    if(tracks.getJSONObject(i).getString("kind").equalsIgnoreCase("captions")) {
                        if(tracks.getJSONObject(i).getString("label").equalsIgnoreCase("english")) {
                            subtitles.add(tracks.getJSONObject(i).getString("file"));
                        }
                    }
                }

                JSONArray videoSources = sources.getJSONArray("sources");
                for(int i = 0; i < videoSources.length(); i++) {
                    JSONObject videoSource = videoSources.getJSONObject(i);
                    VideoURLData videoURLData = new VideoURLData(videoSource.getString("file"));
                    for(String subtitle: subtitles) videoURLData.addSubtitle(subtitle);
                    videoURLData.setSource(ProvidersData.RAPIDCLOUD.NAME);
                    videoURLData.setTitle(this.displayName);
                    videoURLData.addHeader("SID", sid);
                    result.add(videoURLData);
                }

                Log.d("Sid", sid);
            }
        } catch (InterruptedException | IOException | JSONException e) {
            e.printStackTrace();
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
        return ProvidersData.RAPIDCLOUD.NAME;
    }

    private String wss() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        final String[] sid = {null};
        WebSocketListener listener = new WebSocketListener() {
            @Override
            public void onMessage(@NonNull WebSocket webSocket, @NonNull String text) {
                if(text.startsWith("40")) {
                    Pattern sidPattern = Pattern.compile("sid\":\"(.*?)\"");
                    Matcher matcher = sidPattern.matcher(text);
                    if(matcher.find()) {
                        sid[0] = matcher.group(1);
                    }
                    latch.countDown();
                }
                else if(text.equals("2")) {
                    webSocket.send("3");
                }
            }

            @Override
            public void onOpen(@NonNull WebSocket webSocket, @NonNull Response response) {
                webSocket.send("40");
            }
        };

        WebSocket socket = new OkHttpClient().newWebSocket(
                new Request.Builder().url("wss://ws1.rapid-cloud.ru/socket.io/?EIO=4&transport=websocket").build(),
                listener
        );
        latch.await(30, TimeUnit.SECONDS);
        return sid[0];
    }
}
