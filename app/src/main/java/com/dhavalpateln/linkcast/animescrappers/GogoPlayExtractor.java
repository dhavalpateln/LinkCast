package com.dhavalpateln.linkcast.animescrappers;

import android.net.Uri;
import android.os.Build;
import android.util.Log;

import com.dhavalpateln.linkcast.database.AnimeLinkData;
import com.dhavalpateln.linkcast.utils.Utils;
import com.google.android.gms.common.util.ArrayUtils;

import org.json.JSONArray;
import org.json.JSONObject;

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

import androidx.annotation.RequiresApi;

public class GogoPlayExtractor extends AnimeScrapper {
    private String TAG = "VidStream";
    private String GOGOANIME_ENCRYPT_KEY = "93106165734640459728346589106791";
    private String GOGOANIME_DECRYPT_KEY = "97952160493714852094564712118349";
    private String GOGOANIME_SECRET = "63976882873559819639988080820907";
    private String GOGOANIME_IV = "9728346589106791";
    private String CUSTOM_PADDER = "\u0008\u000e\u0003\u0008\t\u0003\u0004\t";

    public GogoPlayExtractor(String baseUrl) {
        super(baseUrl);
    }

    @Override
    public boolean isCorrectURL(String url) {
        return false;
    }

    @Override
    public Map<String, String> getEpisodeList(String episodeListUrl) {
        return null;
    }

    private String pad(String s) {
        /*int lastChars = CUSTOM_PADDER.length() - (s.length() % 16);
        if(lastChars < 0) lastChars *= -1;
        if(s.length() == 8) lastChars = CUSTOM_PADDER.length();
        return s + CUSTOM_PADDER.substring(CUSTOM_PADDER.length() - lastChars);*/
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
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
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
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
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

    @Override
    public void extractEpisodeUrls(String episodeUrl, List<VideoURLData> result) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            try {
                Uri uri = Uri.parse(episodeUrl);
                String hostName = "https://" + uri.getHost();
                String contentId = uri.getQueryParameter("id");
                byte[] GOGOANIME_KEY = getGogoAnimeKey(contentId, GOGOANIME_IV.getBytes(StandardCharsets.UTF_8));
                Uri vidStreamUri = new Uri.Builder()
                        .appendPath("encrypt-ajax.php")
                        .appendQueryParameter("id", encrypt(uri.getQueryParameter("id"), GOGOANIME_KEY, GOGOANIME_IV.getBytes(StandardCharsets.UTF_8)))
                        .appendQueryParameter("alias", uri.getQueryParameter("id"))
                        .build();

                Log.d(TAG, hostName + vidStreamUri.toString());

                Map<String, String> headerMap = new HashMap<>();
                headerMap.put("x-requested-with", "XMLHttpRequest");
                headerMap.put("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/95.0.4638.69 Safari/537.36");
                headerMap.put("referer", hostName);
                String response = getHttpContent(hostName + vidStreamUri.toString(), headerMap);
                JSONObject jsonObject = new JSONObject(response);

                String dataResponse = decrypt(jsonObject.getString("data"), GOGOANIME_KEY, GOGOANIME_IV.getBytes(StandardCharsets.UTF_8));
                //dataResponse = dataResponse.replace("o\"<P{#meme\":", "e\":[{\"file\":");
                dataResponse = dataResponse.replaceAll("^[\u0000\u0001\u0002\u0003\u0004\u0005\u0006\u0007\u0008\t\n\u000b\u000c\r\u000e\u000f\u0010]+", "");
                dataResponse = dataResponse.replaceAll("[\u0000\u0001\u0002\u0003\u0004\u0005\u0006\u0007\u0008\t\n\u000b\u000c\r\u000e\u000f\u0010]+$", "");

                JSONObject content = new JSONObject(dataResponse);

                JSONArray vidSources = content.getJSONArray("source");
                for (int i = 0; i < vidSources.length(); i++) {
                    JSONObject vidSource = vidSources.getJSONObject(i);
                    if (vidSources.length() == 1 || !vidSource.getString("label").equals("Auto")) {
                        VideoURLData videoURLData = new VideoURLData("GogoPlay - " + vidSource.getString("label"), vidSource.getString("file"), "https://" + Uri.parse(baseUrl).getHost() + "/");
                        videoURLData.addHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/95.0.4638.69 Safari/537.36");
                        result.add(videoURLData);
                        Log.d(TAG, videoURLData.getTitle() + " : " + videoURLData.getUrl());
                    }
                }
                /*String htmlContent = getHttpContent(episodeUrl);
                Pattern dataValuePattern = Pattern.compile("data-name=\"episode\" data-value=\"(.*)\"");

                for(String line: htmlContent.split("\n")) {
                    if(line.contains("data-name=\"episode\"")) {
                        Matcher matcher = dataValuePattern.matcher(line);
                        if (matcher.find()) {
                            String cryptoDataValue = "test"; //decrypt(matcher.group(1));
                            String id = cryptoDataValue.split("&")[0];
                            String hostName = "https://" + uri.getHost();
                            //hostName = "https://gogoplay4.com";

                            Uri vidStreamUri = new Uri.Builder()
                                    .appendPath("encrypt-ajax.php")
                                    .appendQueryParameter("id", encrypt(uri.getQueryParameter("id"), GOGOANIME_ENCRYPT_KEY, GOGOANIME_IV))
                                    .build();

                            Log.d(TAG, hostName + vidStreamUri.toString());

                            Map<String, String> headerMap = new HashMap<>();
                            headerMap.put("x-requested-with", "XMLHttpRequest");
                            JSONObject jsonObject = new JSONObject(getHttpContent(hostName + vidStreamUri.toString(), headerMap));

                            String dataResponse = decrypt(jsonObject.getString("data"), GOGOANIME_DECRYPT_KEY, GOGOANIME_IV);
                            dataResponse = dataResponse.replace("o\"<P{#meme\":", "e\":[{\"file\":");
                            dataResponse = dataResponse.replaceAll("^[\u0000\u0001\u0002\u0003\u0004\u0005\u0006\u0007\u0008\t\n\u000b\u000c\r\u000e\u000f\u0010]+", "");
                            dataResponse = dataResponse.replaceAll("[\u0000\u0001\u0002\u0003\u0004\u0005\u0006\u0007\u0008\t\n\u000b\u000c\r\u000e\u000f\u0010]+$", "");

                            JSONObject content = new JSONObject(dataResponse);

                            JSONArray vidSources = content.getJSONArray("source");
                            for (int i = 0; i < vidSources.length(); i++) {
                                JSONObject vidSource = vidSources.getJSONObject(i);
                                if (vidSources.length() == 1 || !vidSource.getString("label").equals("Auto")) {
                                    VideoURLData videoURLData = new VideoURLData("GogoPlay - " + vidSource.getString("label"), vidSource.getString("file"), "https://" + Uri.parse(baseUrl).getHost() + "/");
                                    videoURLData.addHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/95.0.4638.69 Safari/537.36");
                                    result.add(videoURLData);
                                    Log.d(TAG, videoURLData.getTitle() + " : " + videoURLData.getUrl());
                                }
                            }
                        }
                    }
                }*/
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public Map<String, String> extractData(AnimeLinkData data) {
        return null;
    }

    @Override
    public String getDisplayName() {
        return "GogoPlay";
    }
}
