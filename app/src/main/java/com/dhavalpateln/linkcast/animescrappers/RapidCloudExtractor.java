package com.dhavalpateln.linkcast.animescrappers;

import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.dhavalpateln.linkcast.ProvidersData;
import com.dhavalpateln.linkcast.database.AnimeLinkData;
import com.dhavalpateln.linkcast.database.VideoURLData;
import com.dhavalpateln.linkcast.utils.EpisodeNode;
import com.dhavalpateln.linkcast.utils.SimpleHttpClient;
import com.dhavalpateln.linkcast.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class RapidCloudExtractor extends AnimeScrapper {

    private String displayName;
    private final static String SID_ENDPOINT = "https://api.enime.moe/tool/rapid-cloud/server-id";
    private final static String SALT_SECRET_ENDPOINT = "https://raw.githubusercontent.com/consumet/rapidclown/main/key.txt";

    private static String SID_KEY = null;
    private static String SALT_KEY = null;
    private final String TAG = "RAPIDCLOUD";

    public RapidCloudExtractor(String name) {
        this.displayName = name;
    }

    @Override
    public boolean isCorrectURL(String url) {
        return false;
    }

    private String getAssociativeKey(String endpoint) {
        try {
            return SimpleHttpClient.getResponse(SimpleHttpClient.getURLConnection(endpoint));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getSIDKey() {
        if(SID_KEY == null) {
            SID_KEY = getAssociativeKey(SID_ENDPOINT);
        }
        return SID_KEY;
    }

    private byte[] getSALTKey() {
        if(SALT_KEY == null) {
            SALT_KEY = getAssociativeKey(SALT_SECRET_ENDPOINT);
        }
        return Utils.stringToBytes(SALT_KEY);
    }

    private byte[] getKeyFromSalt(byte[] salt, byte[] secret) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("MD5");
        digest.update(secret);
        digest.update(salt);
        byte[] key = digest.digest();
        List<Byte> currentKey = new ArrayList<>();
        for(byte b: key) currentKey.add(b);

        while(currentKey.size() < 48) {
            digest.update(key);
            digest.update(secret);
            digest.update(salt);
            key = digest.digest();
            for(byte b: key) currentKey.add(b);
        }
        byte[] result = new byte[48];
        for(int i = 0; i < 48; i++) result[i] = currentKey.get(i);
        return result;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private JSONArray decipherSaltedAES(String encodedMessage) throws JSONException {
        String result = "[]";
        try {
            byte[] data = Base64.getDecoder().decode(Utils.stringToBytes(encodedMessage));
            byte[] key = getKeyFromSalt(Utils.extractBytes(data, 8, 16), getSALTKey());

            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");

            SecretKeySpec secretKey = new SecretKeySpec(Utils.extractBytes(key, 0, 32), "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(Utils.extractBytes(key, 32, 48));
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);

            byte[] decryptedData = cipher.doFinal(Utils.extractBytes(data, 16, data.length));
            int padLength = decryptedData[decryptedData.length - 1];
            result = Utils.bytesToString(Utils.extractBytes(decryptedData, 0, decryptedData.length - padLength));

            Log.d("RCLOUD", Utils.bytesToString(decryptedData));
        } catch (Exception e) {
            Log.d(TAG, "Error deciphering key");
            e.printStackTrace();
        }
        return new JSONArray(result);
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
                //String sid = getSIDKey();
                //Map<String, String> captcha = CaptchaSolver.bypassCaptcha(episodeUrl, headers);
                String contentID = episodeUrl.split("embed-6/")[1].split("\\?")[0];

                Uri sourceURI = new Uri.Builder()
                        .scheme("https")
                        .authority(uri.getAuthority())
                        .appendPath("ajax")
                        .appendPath("embed-6")
                        .appendPath("getSources")
                        .appendQueryParameter("id", contentID)
                        //.appendQueryParameter("sId", sid)
                        //.appendQueryParameter("_token", captcha.getOrDefault("token", ""))
                        //.appendQueryParameter("_number", captcha.getOrDefault("number", ""))
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

                JSONArray videoSources = decipherSaltedAES(sources.getString("sources"));
                for(int i = 0; i < videoSources.length(); i++) {
                    JSONObject videoSource = videoSources.getJSONObject(i);
                    VideoURLData videoURLData = new VideoURLData(videoSource.getString("file"));
                    for(String subtitle: subtitles) videoURLData.addSubtitle(subtitle);
                    videoURLData.setSource(ProvidersData.RAPIDCLOUD.NAME);
                    videoURLData.setTitle(this.displayName);
                    //videoURLData.addHeader("SID", sid);
                    result.add(videoURLData);
                }

                //Log.d("Sid", sid);
            }
        } catch (IOException | JSONException e) {
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
                new Request.Builder().url("wss://ws1.rapid-cloud.co/socket.io/?EIO=4&transport=websocket").build(),
                listener
        );
        latch.await(30, TimeUnit.SECONDS);
        return sid[0];
    }
}
