package com.dhavalpateln.linkcast.animescrappers;

import android.net.Uri;
import android.util.Log;

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
    private String GOGOANIME_SECRET = "25716538522938396164662278833288";
    private String GOGOANIME_IV = "1285672985238393";
    private String CUSTOM_PADDER = "\u0008\u000e\u0003\u0008\t\u0003\u0004\t";

    public VidStreamExtractor(String baseUrl) {
        super(baseUrl);
    }

    @Override
    public boolean isCorrectURL(String url) {
        return false;
    }

    @Override
    public Map<String, String> getEpisodeList(String episodeListUrl) throws IOException {
        return null;
    }

    private String pad(String s) {
        int lastChars = CUSTOM_PADDER.length() - (s.length() % 16);
        if(lastChars < 0) lastChars *= -1;
        if(s.length() == 8) lastChars = CUSTOM_PADDER.length();
        return s + CUSTOM_PADDER.substring(CUSTOM_PADDER.length() - lastChars);
    }

    private String encrypt(String data) throws NoSuchPaddingException, InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        return encrypt(pad(data).getBytes(Charset.forName("UTF-8")));
    }

    private String encrypt(byte[] data) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");

        SecretKeySpec secretKey = new SecretKeySpec(GOGOANIME_SECRET.getBytes(Charset.forName("UTF-8")), "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(GOGOANIME_IV.getBytes(Charset.forName("UTF-8")));
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
        byte[] encryptedData = cipher.doFinal(data);
        byte[] bencoded = new byte[0];
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            bencoded = Base64.getEncoder().encode(encryptedData);
        }
        return new String(bencoded, StandardCharsets.UTF_8);
    }

    private String decrypt(String data) throws NoSuchPaddingException, InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        return decrypt(data.getBytes(Charset.forName("UTF-8")));
    }

    private String decrypt(byte[] data) throws InvalidAlgorithmParameterException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");

        SecretKeySpec secretKey = new SecretKeySpec(GOGOANIME_SECRET.getBytes(Charset.forName("UTF-8")), "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(GOGOANIME_IV.getBytes(Charset.forName("UTF-8")));
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);

        byte[] bdecoded = new byte[0];
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            bdecoded = Base64.getDecoder().decode(data);
        }
        byte[] decryptedData = cipher.doFinal(bdecoded);

        return new String(decryptedData, StandardCharsets.UTF_8);
    }

    @Override
    public Map<String, String> extractEpisodeUrls(String episodeUrl) throws IOException {
        Map<String, String> result = new HashMap<>();

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            try {
                Uri uri = Uri.parse(episodeUrl);
                String htmlContent = getHttpContent(episodeUrl);
                Pattern dataValuePattern = Pattern.compile("data-name=\"crypto\" data-value=\"(.*)\"");

                for(String line: htmlContent.split("\n")) {
                    if(line.contains("data-name=\"crypto\"")) {
                        Matcher matcher = dataValuePattern.matcher(line);
                        if (matcher.find()) {
                            String cryptoDataValue = decrypt(matcher.group(1));
                            String id = cryptoDataValue.split("&")[0];
                            String hostName = "https://" + uri.getHost();
                            //hostName = "https://gogoplay.io";

                            Uri vidStreamUri = new Uri.Builder()
                                    .appendPath("encrypt-ajax.php")
                                    .appendQueryParameter("id", encrypt(id))
                                    .build();

                            Log.d(TAG, hostName + vidStreamUri.toString());

                            Map<String, String> headerMap = new HashMap<>();
                            headerMap.put("x-requested-with", "XMLHttpRequest");
                            JSONObject jsonObject = new JSONObject(getHttpContent(hostName + vidStreamUri.toString(), headerMap));

                            String dataResponse = decrypt(jsonObject.getString("data"));
                            dataResponse = dataResponse.replace("o\"<P{#meme\":", "e\":[{\"file\":");
                            dataResponse = dataResponse.replaceAll("^[\u0000\u0001\u0002\u0003\u0004\u0005\u0006\u0007\u0008\t\n\u000b\u000c\r\u000e\u000f\u0010]+", "");
                            dataResponse = dataResponse.replaceAll("[\u0000\u0001\u0002\u0003\u0004\u0005\u0006\u0007\u0008\t\n\u000b\u000c\r\u000e\u000f\u0010]+$", "");

                            JSONObject content = new JSONObject(dataResponse);

                            JSONArray vidSources = content.getJSONArray("source");
                            for (int i = 0; i < vidSources.length(); i++) {
                                JSONObject vidSource = vidSources.getJSONObject(i);
                                if (vidSources.length() == 1 || !vidSource.getString("label").equals("Auto")) {
                                    result.put(vidSource.getString("label"), vidSource.getString("file"));
                                    Log.d(TAG, "VidStream - " + vidSource.getString("label") + " : " + vidSource.getString("file"));
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    @Override
    public String extractData() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return null;
    }
}
