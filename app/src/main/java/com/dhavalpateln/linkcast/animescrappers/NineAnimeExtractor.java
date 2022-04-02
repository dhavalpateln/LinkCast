package com.dhavalpateln.linkcast.animescrappers;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import com.dhavalpateln.linkcast.ProvidersData;
import com.dhavalpateln.linkcast.database.AnimeLinkData;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NineAnimeExtractor extends AnimeScrapper {

    private String TAG = "NINEANIME";
    private final String BASE64_TABLE = "0wMrYU+ixjJ4QdzgfN2HlyIVAt3sBOZnCT9Lm7uFDovkb/EaKpRWhqXS5168ePcG";
    private final String NORMAL_TABLE = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
    private Map<Character, Character> tableMap;
    private String nineAnimeBaseUrl;

    public NineAnimeExtractor(String baseUrl) {
        super(baseUrl);
        tableMap = new HashMap<>();
        for(int i = 0; i < BASE64_TABLE.length(); i++) {
            tableMap.put(BASE64_TABLE.charAt(i), NORMAL_TABLE.charAt(i));
        }
    }

    private String getCipher(String key, String text) {
        String cipher = "";
        int xcrypto = 0;

        Map<Integer, Integer> mapping = new HashMap<>();
        for(int i = 0; i < 256; i++) mapping.put(i, i);

        for(int c = 0; c < 256; c++) {
            xcrypto = (xcrypto + mapping.get(c) + key.charAt(c % key.length())) % 256;
            int temp = mapping.get(c);
            mapping.put(c, mapping.get(xcrypto));
            mapping.put(xcrypto, temp);
        }

        int i = 0;
        int j = 0;
        for(int f = 0; f < text.length(); f++) {
            j = (j + f) % 256;
            i = (i + mapping.get(j)) % 256;
            int temp = mapping.get(j);
            mapping.put(j, mapping.get(i));
            mapping.put(i, temp);
            cipher += (char) (text.charAt(f) ^ mapping.get((mapping.get(i) + mapping.get(j)) % 256));

        }
        return cipher;
    }

    private String justify(String data, char extra, int length, char mode) {
        String toAppend = "";
        for(int i = data.length(); i < length; i++) toAppend += extra;
        if(mode == 'l') {
            return data + toAppend;
        } else {
            return toAppend + data;
        }
    }

    private List<String> wrap(String data, int length) {
        List<String> result = new ArrayList<>();
        for(int start = 0; start < data.length(); start = start + length) {
            result.add(data.substring(start, Math.min(start + length, data.length())));
        }
        return result;
    }

    @SuppressLint("NewApi")
    private String translate(String data) {
        String result = "";
        for(int i = 0; i < data.length(); i++) {
            result += tableMap.getOrDefault(data.charAt(i), data.charAt(i));
        }
        return result;
    }

    private String encrypt(String data) {
        String result = "";
        String binData = "";
        for(int i = 0; i < data.length(); i++) {
            char c = data.charAt(i);
            binData += justify(Integer.toBinaryString(c), '0', 8, 'r');
        }
        List<String> wrappedData = wrap(binData, 6);
        for(String binNumString: wrappedData) {
            result += BASE64_TABLE.charAt(Integer.valueOf(justify(binNumString, '0', 6, 'l'), 2));
        }
        return result;
    }

    private String decrypt(String data) {
        String result = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            byte[] b64DecodedData = Base64.getDecoder().decode(translate(data).getBytes(StandardCharsets.UTF_8));
            for(byte b: b64DecodedData) {
                result += (char) (b & 255);
            }
        }
        return result;
    }

    private String encryptURL(String url) {
        return "kr1337" + encrypt(getCipher("kr1337", Uri.encode(url)));
    }

    private String decryptURL(String url, int length) {
        return Uri.decode(getCipher(url.substring(0, length), decrypt(url.substring(length))));
    }

    @Override
    public boolean isCorrectURL(String url) {
        return url.startsWith(ProvidersData.NINEANIME.URL);
    }

    @Override
    public Map<String, String> getEpisodeList(String episodeListUrl) {
        updateBaseUrl();
        Map<String, String> result = new HashMap<>();
        try {
            Pattern slugPattern = Pattern.compile("/watch/[^&?/]+\\.(.*?)$");
            Matcher slugMatcher = slugPattern.matcher(episodeListUrl);
            if(slugMatcher.find()) {
                String slug = slugMatcher.group(1);
                Uri uri = new Uri.Builder()
                        .appendQueryParameter("id", slug)
                        .appendQueryParameter("vrf", encryptURL(slug))
                        .build();

                JSONObject responseContent = new JSONObject(getHttpContent(
                        nineAnimeBaseUrl + "/ajax/anime/servers" + uri.toString()));

                Document html = Jsoup.parse(responseContent.getString("html"));
                Elements episodes = html.select("a[title][data-sources][data-base]");
                for(Element element: episodes) {
                    result.put(element.attr("data-base"), element.toString());
                }

                Log.d(TAG, "Found " + result.size() + " episodes");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public void updateBaseUrl() {
        try {
            if(nineAnimeBaseUrl == null) {
                nineAnimeBaseUrl = ProvidersData.NINEANIME.URL;
                if(getHttpResponseCode(nineAnimeBaseUrl) != 200) {
                    for(String url: ProvidersData.NINEANIME.ALTERNATE_URLS) {
                        if(getHttpResponseCode(url) == 200) {
                            Log.d(TAG, "Using alternate url: " + url);
                            nineAnimeBaseUrl = url;
                            break;
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void extractEpisodeUrls(String episodeUrl, List<VideoURLData> result) {
        updateBaseUrl();
        try {
            Document doc = Jsoup.parse(episodeUrl);
            JSONObject dataSources = new JSONObject(doc.getElementsByTag("a").get(0).attr("data-sources"));
            Iterator<String> iter = dataSources.keys();
            while(iter.hasNext()) {
                String dataSource = iter.next();
                String dataHash = dataSources.getString(dataSource);

                JSONObject response = new JSONObject(
                        getHttpContent(nineAnimeBaseUrl + "/ajax/anime/episode?id=" + Uri.encode(dataHash))
                );

                String url = decryptURL(response.getString("url"), 6);
                AnimeScrapper extractor;
                try {
                    switch (dataSource) {
                        case "41": // VIDSTREAM
                        case "28": // MCLOUD
                            extractor = new VidStreamExtractor(url);
                            extractor.extractEpisodeUrls(url, result);
                            break;
                        case "43":
                            break;
                        case "40": // STREAMTAPE
                            extractor = new StreamTapeExtractor(url);
                            extractor.extractEpisodeUrls(url, result);
                            break;
                        case "35":
                            break;
                        default:
                            break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Map<String, String> extractData(AnimeLinkData data) {
        data.setTitle(data.getTitle().replace("(" + getDisplayName() + ")", ""));
        setData("animeTitle", data.getTitle());
        setData(AnimeLinkData.DataContract.DATA_IMAGE_URL, data.getData().get(AnimeLinkData.DataContract.DATA_IMAGE_URL));
        return getEpisodeList(data.getUrl());
    }

    @Override
    public String getDisplayName() {
        return ProvidersData.NINEANIME.NAME;
    }
}
