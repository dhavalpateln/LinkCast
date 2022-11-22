package com.dhavalpateln.linkcast.animescrappers;

import android.net.Uri;
import android.os.Build;
import android.util.Log;

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
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VidStreamExtractor extends AnimeScrapper {

    private String TAG = "VidStream";
    private static String BASE64_TABLE = "51wJ0FDq/UVCefLopEcmK3ni4WIQztMjZdSYOsbHr9R2h7PvxBGAuglaN8+kXT6y";
    private final String CIPHER_DATA_ENDPOINT = "https://raw.githubusercontent.com/AnimeJeff/Overflow/main/syek";
    private static JSONObject CIPHER_ALGO = null;

    public VidStreamExtractor() {
        super();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private JSONObject getCipherAlgorithm() throws IOException, JSONException {
        if(CIPHER_ALGO == null) {
            Base64.Decoder decoder = Base64.getDecoder();
            byte[] content = Utils.stringToBytes(SimpleHttpClient.getResponse(SimpleHttpClient.getURLConnection(CIPHER_DATA_ENDPOINT)));
            CIPHER_ALGO = new JSONObject(
                    Utils.bytesToString(decoder.decode(decoder.decode(decoder.decode(content))))
            );
        }
        return CIPHER_ALGO;
    }

    private String stringEncrypt(String data) {
        StringBuilder builder = new StringBuilder("");
        for(int i = 0; i < data.length(); i++) {
            char c = data.charAt(i);
            if(Character.isAlphabetic(c)) {
                int value = c + 13;
                int a = c <= 90 ? 90 : 122;
                c = (char) (a >= value ? value : (value - 26));
            }
            builder.append(c);
        }
        return builder.toString();
    }

    private String conditionalEncrypt(String content, JSONArray steps, String table) throws JSONException {
        for(int i = 0; i < steps.length(); i++) {
            switch (steps.getString(i)) {
                case "s":
                    content = stringEncrypt(content);
                    break;
                case "o":
                    content = NineAnimeExtractor.encrypt(content, table).replace("/", "_");
                    break;
                case "a":
                    content = new StringBuilder(content).reverse().toString();
                    break;
            }
        }
        return content;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String dashify(String data) throws Exception {
        List<String> dashNums = new ArrayList<>();
        JSONObject operations = getCipherAlgorithm().getJSONObject("operations");
        for(int i = 0; i < data.length(); i++) {
            String[] operation = operations.getString(String.valueOf(i % operations.length())).split(" ");
            int operand1 = data.charAt(i);
            int operand2 = Integer.valueOf(operation[1]);
            int result;
            switch (operation[0]) {
                case "*":
                    result = operand1 * operand2;
                    break;
                case "-":
                    result = operand1 - operand2;
                    break;
                case "+":
                    result = operand1 + operand2;
                    break;
                case "^":
                    result = operand1 ^ operand2;
                    break;
                case "<<":
                    result = operand1 << operand2;
                    break;
                default:
                    throw new Exception("Unknown operator");
            }
            dashNums.add(String.valueOf(result));
        }
        return String.join("-", dashNums);
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
            if (embedMatcher.find() && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                String host = embedMatcher.group(1);
                String slug = embedMatcher.group(2);

                JSONObject algorithm = getCipherAlgorithm();
                String b64Table = algorithm.getString("encryptKey");
                String contentID = NineAnimeExtractor.encrypt(
                        NineAnimeExtractor.getCipher(algorithm.getString("cipherKey"), slug),
                        b64Table
                );



                String dashID = conditionalEncrypt(contentID, algorithm.getJSONArray("pre"), b64Table);
                String dash = conditionalEncrypt(dashify(dashID), algorithm.getJSONArray("post"), b64Table);

                String infoUrl = host + "/" + algorithm.getString("mainKey") + "/" + dash;
                JSONObject infoContent = new JSONObject(
                        getHttpContent(infoUrl, episodeUrl)
                );

                JSONArray sources = infoContent.getJSONObject("data").getJSONObject("media").getJSONArray("sources");
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
