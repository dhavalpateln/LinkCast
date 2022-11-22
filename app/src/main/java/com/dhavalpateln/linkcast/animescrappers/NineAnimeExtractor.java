package com.dhavalpateln.linkcast.animescrappers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import com.dhavalpateln.linkcast.ProvidersData;
import com.dhavalpateln.linkcast.database.AnimeLinkData;
import com.dhavalpateln.linkcast.database.VideoURLData;
import com.dhavalpateln.linkcast.utils.EpisodeNode;
import com.dhavalpateln.linkcast.utils.SimpleHttpClient;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.HttpURLConnection;
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
    private final static String BASE64_TABLE = "c/aUAorINHBLxWTy3uRiPt8J+vjsOheFG1E0q2X9CYwDZlnmd4Kb5M6gSVzfk7pQ";
    private final static String NORMAL_TABLE = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
    private Map<Character, Character> tableMap;
    private String nineAnimeBaseUrl;
    private SharedPreferences prefs;
    private final String DECRYPTION_CODE = "hlPeNwkncH0fq9so";

    public static final String SOURCE_PREF_KEY = "9animesource";

    public NineAnimeExtractor() {
        super();
        tableMap = new HashMap<>();
        for(int i = 0; i < BASE64_TABLE.length(); i++) {
            tableMap.put(BASE64_TABLE.charAt(i), NORMAL_TABLE.charAt(i));
        }
    }

    public NineAnimeExtractor(Context context) {
        this();
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static String getCipher(String key, String text) {
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
            //j = (j + f) % 256;
            j = (j + 1) % 256;
            i = (i + mapping.get(j)) % 256;
            int temp = mapping.get(j);
            mapping.put(j, mapping.get(i));
            mapping.put(i, temp);
            cipher += (char) (text.charAt(f) ^ mapping.get((mapping.get(i) + mapping.get(j)) % 256));

        }
        return cipher;
    }

    private static String justify(String data, char extra, int length, char mode) {
        String toAppend = "";
        for(int i = data.length(); i < length; i++) toAppend += extra;
        if(mode == 'l') {
            return data + toAppend;
        } else {
            return toAppend + data;
        }
    }

    private static List<String> wrap(String data, int length) {
        List<String> result = new ArrayList<>();
        for(int start = 0; start < data.length(); start = start + length) {
            result.add(data.substring(start, Math.min(start + length, data.length())));
        }
        return result;
    }

    @SuppressLint("NewApi")
    private String translate(String data, Map<Character, Character> tableMap) {
        String result = "";
        for(int i = 0; i < data.length(); i++) {
            result += tableMap.getOrDefault(data.charAt(i), data.charAt(i));
        }
        return result;
    }

    public static String encrypt(String data, String base64Table) {
        String result = "";
        String binData = "";
        for(int i = 0; i < data.length(); i++) {
            char c = data.charAt(i);
            binData += justify(Integer.toBinaryString(c), '0', 8, 'r');
        }
        List<String> wrappedData = wrap(binData, 6);
        for(String binNumString: wrappedData) {
            result += base64Table.charAt(Integer.valueOf(justify(binNumString, '0', 6, 'l'), 2));
        }
        return result;
    }

    private String decrypt(String data, String table) {
        Map<Character, Character> tableMap = new HashMap<>();
        for(int i = 0; i < table.length(); i++) {
            tableMap.put(table.charAt(i), NORMAL_TABLE.charAt(i));
        }
        String result = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            byte[] b64DecodedData = Base64.getDecoder().decode(translate(data, tableMap).getBytes(StandardCharsets.UTF_8));
            for(byte b: b64DecodedData) {
                result += (char) (b & 255);
            }
        }
        return result;
    }

    private String encryptURL(String url, String key) {
        return encrypt(getCipher(key, Uri.encode(url)), NORMAL_TABLE);
    }

    private String decryptURL(String url, String secret) {
        return Uri.decode(getCipher(secret, decrypt(url, NORMAL_TABLE)));
    }

    @Override
    public boolean isCorrectURL(String url) {
        return url.startsWith(ProvidersData.NINEANIME.URL);
    }

    @Override
    public List<EpisodeNode> getEpisodeList(String episodeListUrl) {
        updateBaseUrl();
        List<EpisodeNode> result = new ArrayList<>();
        try {
            HttpURLConnection urlConnection = SimpleHttpClient.getURLConnection(episodeListUrl);
            if(SimpleHttpClient.getResponseCode(urlConnection) == 301) {
                episodeListUrl = urlConnection.getHeaderField("Location").replace("http://", "https://");
                urlConnection = SimpleHttpClient.getURLConnection(episodeListUrl);
            }
            String urlContent = SimpleHttpClient.getResponse(urlConnection);
            Pattern contentIDPattern = Pattern.compile("data-id=\"(.+?)\"");
            Matcher contentIDMatcher = contentIDPattern.matcher(urlContent);
            if(contentIDMatcher.find()) {
                String contentID = contentIDMatcher.group(1);
                JSONObject responseContent = new JSONObject(getHttpContent(
                        nineAnimeBaseUrl + "/ajax/episode/list/" + contentID));

                Elements episodeElements = Jsoup.parse(responseContent.getString("result")).select("a[data-num]");
                for(Element episodeElement: episodeElements) {
                    try {
                        result.add(new EpisodeNode(episodeElement.text(), episodeElement.attr("data-ids")));
                    } catch (Exception e) {
                        result.add(new EpisodeNode(episodeElement.attr("data-num"), episodeElement.attr("data-ids")));
                    }
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
                nineAnimeBaseUrl = prefs.getString(SOURCE_PREF_KEY, ProvidersData.NINEANIME.URL);
                if(getHttpResponseCode(nineAnimeBaseUrl) != 200) {
                    for(String url: ProvidersData.NINEANIME.ALTERNATE_URLS) {
                        if(getHttpResponseCode(url) == 200) {
                            Log.d(TAG, "Using alternate url: " + url);
                            nineAnimeBaseUrl = url;
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString(SOURCE_PREF_KEY, nineAnimeBaseUrl);
                            editor.commit();
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
    public void extractEpisodeUrls(String data, List<VideoURLData> result) {
        updateBaseUrl();
        try {
            JSONObject response = new JSONObject(
                    getHttpContent(nineAnimeBaseUrl + "/ajax/server/list/" + data)
            );
            Document doc = Jsoup.parse(response.getString("result"));

            for(Element contentTypeContainer: doc.select("div[data-type]")) {
                for(Element server: contentTypeContainer.select("ul > li[data-sv-id]")) {
                    String url = decryptURL(new JSONObject(SimpleHttpClient.getResponse(
                            SimpleHttpClient.getURLConnection(nineAnimeBaseUrl + "/ajax/server/" + server.attr("data-link-id")))
                    ).getJSONObject("result").getString("url"), DECRYPTION_CODE);

                    try {
                        Log.d(TAG, url);
                        AnimeScrapper extractor;
                        switch (server.attr("data-sv-id")) {
                            case "41": // VIDSTREAM
                                extractor = new VidStreamExtractor();
                                extractor.extractEpisodeUrls(url, result);
                                break;
                            case "28": // MCLOUD
                                extractor = new MCloudExtractor();
                                extractor.extractEpisodeUrls(url, result);
                                break;
                            case "43":
                                break;
                            case "40": // STREAMTAPE
                                extractor = new StreamTapeExtractor();
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
            }
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<EpisodeNode> extractData(AnimeLinkData data) {
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
