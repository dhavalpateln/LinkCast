package com.dhavalpateln.linkcast.extractors.gogoanime;

import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.dhavalpateln.linkcast.ProvidersData;
import com.dhavalpateln.linkcast.database.AnimeLinkData;
import com.dhavalpateln.linkcast.database.VideoURLData;
import com.dhavalpateln.linkcast.extractors.AnimeExtractor;
import com.dhavalpateln.linkcast.database.EpisodeNode;
import com.dhavalpateln.linkcast.utils.SimpleHttpClient;
import com.dhavalpateln.linkcast.utils.Utils;
import com.google.android.gms.common.util.ArrayUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
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

public class GogoPlayExtractor extends AnimeExtractor {
    private String TAG = "GogoPlay";

    private class GogoAnimeKeys {
        byte[] key1;
        byte[] key2;
        byte[] iv;
        String encrytedDataValue;
    }

    public GogoPlayExtractor() {
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

    private String pad(String s) {
        char padChar = (char) (s.length() % 16);
        int padLength = 16 - (s.length() % 16);
        for(int i = 0; i < padLength; i++) s += padChar;
        return s;
    }

    private String encrypt(String data, byte[] key, byte[] iv) throws NoSuchPaddingException, InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        return encrypt(pad(data).getBytes(Charset.forName("UTF-8")), key, iv);
    }

    private String encrypt(byte[] data, byte[] key, byte[] iv) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");

        SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
        byte[] encryptedData = cipher.doFinal(data);
        byte[] bencoded = new byte[0];
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            bencoded = Base64.getEncoder().encode(encryptedData);
        }
        return new String(bencoded, StandardCharsets.UTF_8);
    }

    private String decrypt(String data, byte[] key, byte[] iv) throws NoSuchPaddingException, InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        return decrypt(data.getBytes(Charset.forName("UTF-8")), key, iv);
    }

    private String decrypt(byte[] data, byte[] key, byte[] iv) throws InvalidAlgorithmParameterException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");

        SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);

        byte[] bdecoded = new byte[0];
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            bdecoded = Base64.getDecoder().decode(data);
        }
        byte[] decryptedData = cipher.doFinal(bdecoded);

        return new String(decryptedData, StandardCharsets.UTF_8);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private byte[] getGogoAnimeKey(String contentId, byte[] iv) {
        byte[] result = new byte[32];
        byte[] combinedContent = ArrayUtils.concatByteArrays(
                Base64.getDecoder().decode(contentId),
                iv
        );
        byte[] hexResult = Utils.hexlify(combinedContent);
        for(int i = 0; i < result.length; i++)  result[i] = hexResult[i];
        return result;
    }

    private GogoAnimeKeys fetchKeys(String url) {
        GogoAnimeKeys keys = new GogoAnimeKeys();
        try {
            HttpURLConnection urlConnection = SimpleHttpClient.getURLConnection(url);
            SimpleHttpClient.setBrowserUserAgent(urlConnection);
            String response = SimpleHttpClient.getResponse(urlConnection);
            Pattern keyPattern = Pattern.compile("(container|videocontent)-(\\d+?)\"");
            Matcher matcher = keyPattern.matcher(response);
            List<String> keyList = new ArrayList<>();
            while(matcher.find()) {
                keyList.add(matcher.group(2));
            }

            Pattern encrytedDataPattern = Pattern.compile("data-value=\"(.+?)\"");
            Matcher encrytedDataMatcher = encrytedDataPattern.matcher(response);
            if(encrytedDataMatcher.find()) {
                keys.encrytedDataValue = encrytedDataMatcher.group(1);
            }

            if(keyList.get(1).length() > 16) {
                keys.key1 = Utils.unhexlify(keyList.get(0));
                keys.key2 = Utils.unhexlify(keyList.get(2));
                keys.iv = Utils.unhexlify(keyList.get(1));
            }
            else {
                keys.key1 = keyList.get(0).getBytes(StandardCharsets.UTF_8);
                keys.key2 = keyList.get(2).getBytes(StandardCharsets.UTF_8);
                keys.iv = keyList.get(1).getBytes(StandardCharsets.UTF_8);
            }

            /*keys.key1 = Utils.unhexlify(keyList.get(0));
            keys.key2 = Utils.unhexlify(keyList.get(2));
            keys.iv = Utils.unhexlify(keyList.get(1));*/
        } catch (IOException e) {
            e.printStackTrace();
        }
        return keys;
    }

    @Override
    public void extractEpisodeUrls(String episodeUrl, List<VideoURLData> result) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                Uri uri = Uri.parse(episodeUrl);
                String hostName = "https://" + uri.getHost();
                String contentId = uri.getQueryParameter("id");
                if(contentId == null) {
                    throw new Exception("Error fetching content ID");
                }
                GogoAnimeKeys gogoAnimeKeys = fetchKeys(episodeUrl);

                String decrytedData = decrypt(gogoAnimeKeys.encrytedDataValue, gogoAnimeKeys.key1, gogoAnimeKeys.iv)
                        .replaceAll("^[\u0000\u0001\u0002\u0003\u0004\u0005\u0006\u0007\u0008\t\n\u000b\u000c\r\u000e\u000f\u0010]+", "")
                        .replaceAll("[\u0000\u0001\u0002\u0003\u0004\u0005\u0006\u0007\u0008\t\n\u000b\u000c\r\u000e\u000f\u0010]+$", "");

                //byte[] GOGOANIME_KEY = getGogoAnimeKey(contentId, GOGOANIME_IV.getBytes(StandardCharsets.UTF_8));
                Uri.Builder vidStreamUriBuilder = new Uri.Builder()
                        .appendPath("encrypt-ajax.php");


                for(String params: decrytedData.split("&")) {
                    String[] queryKeyValue = params.split("=", 2);
                    if(queryKeyValue.length == 2)   {
                        if(queryKeyValue[1].equals("")) continue;
                        if(queryKeyValue[0].equals("title"))    continue;
                        vidStreamUriBuilder = vidStreamUriBuilder.appendQueryParameter(queryKeyValue[0], queryKeyValue[1]);
                    }
                }

                vidStreamUriBuilder = vidStreamUriBuilder
                        .appendQueryParameter("id", encrypt(contentId, gogoAnimeKeys.key1, gogoAnimeKeys.iv));
                        //.appendQueryParameter("alias", uri.getQueryParameter("id"));

                Uri vidStreamUri = vidStreamUriBuilder.build();



                Map<String, String> headerMap = new HashMap<>();
                headerMap.put("x-requested-with", "XMLHttpRequest");
                headerMap.put("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/95.0.4638.69 Safari/537.36");
                headerMap.put("referer", hostName);

                String url = hostName + vidStreamUri.toString() + "&alias=" + contentId;
                Log.d(TAG, url);
                String response = getHttpContent(url, headerMap);
                JSONObject jsonObject = new JSONObject(response);

                String dataResponse = decrypt(jsonObject.getString("data"), gogoAnimeKeys.key2, gogoAnimeKeys.iv);
                //dataResponse = dataResponse.replace("o\"<P{#meme\":", "e\":[{\"file\":");
                dataResponse = dataResponse.replaceAll("^[\u0000\u0001\u0002\u0003\u0004\u0005\u0006\u0007\u0008\t\n\u000b\u000c\r\u000e\u000f\u0010]+", "");
                dataResponse = dataResponse.replaceAll("[\u0000\u0001\u0002\u0003\u0004\u0005\u0006\u0007\u0008\t\n\u000b\u000c\r\u000e\u000f\u0010]+$", "");

                JSONObject content = new JSONObject(dataResponse);

                JSONArray vidSources = content.getJSONArray("source");
                for (int i = 0; i < vidSources.length(); i++) {
                    JSONObject vidSource = vidSources.getJSONObject(i);
                    if (vidSources.length() == 1 || !vidSource.getString("label").equals("Auto")) {
                        VideoURLData videoURLData = new VideoURLData(
                                getDisplayName(),
                                "GogoPlay - " + vidSource.getString("label"),
                                vidSource.getString("file"),
                                "https://" + Uri.parse(episodeUrl).getHost() + "/"
                        );
                        videoURLData.addHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/95.0.4638.69 Safari/537.36");
                        result.add(videoURLData);
                        Log.d(TAG, videoURLData.getTitle() + " : " + videoURLData.getUrl());
                    }
                }
                JSONArray vidBackupSources = content.getJSONArray("source_bk");
                for (int i = 0; i < vidBackupSources.length(); i++) {
                    JSONObject vidSource = vidBackupSources.getJSONObject(i);
                    if (vidSources.length() == 1 || !vidSource.getString("label").equals("Auto")) {
                        VideoURLData videoURLData = new VideoURLData(
                                getDisplayName(),
                                "GogoPlay Bkup - " + vidSource.getString("label"),
                                vidSource.getString("file"),
                                "https://" + Uri.parse(episodeUrl).getHost() + "/"
                        );
                        videoURLData.addHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/95.0.4638.69 Safari/537.36");
                        result.add(videoURLData);
                        Log.d(TAG, videoURLData.getTitle() + " : " + videoURLData.getUrl());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public List<EpisodeNode> extractData(AnimeLinkData data) {
        return null;
    }

    @Override
    public String getDisplayName() {
        return ProvidersData.GOGOPLAY.NAME;
    }
}
