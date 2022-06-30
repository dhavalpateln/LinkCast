package com.dhavalpateln.linkcast.animescrappers;

import android.net.Uri;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.dhavalpateln.linkcast.utils.SimpleHttpClient;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CaptchaSolver {

    private static String RECAPTCHA_API_JS = "https://www.google.com/recaptcha";

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static Map<String, String> bypassCaptcha(String url, Map<String, String> headers) throws IOException {

        Map<String, String> result = new HashMap<>();

        Uri uri = Uri.parse(url);
        String domain = new String(Base64.getEncoder().encode(
                (uri.getScheme() + "://" + uri.getHost() + ":443").getBytes(StandardCharsets.UTF_8)
        ), StandardCharsets.UTF_8).replaceAll("[=]+$", "").replaceAll("^[=]+", "") + ".";

        HttpURLConnection urlConnection = SimpleHttpClient.getURLConnection(url);
        for(Map.Entry<String, String> entry: headers.entrySet()) {
            urlConnection.setRequestProperty(entry.getKey(), entry.getValue());
        }
        String initialPageContent = SimpleHttpClient.getResponse(urlConnection);

        Pattern captchaKeyPattern = Pattern.compile("recaptchaSiteKey = '(.+?)'");
        Matcher captchaMatcher = captchaKeyPattern.matcher(initialPageContent);

        if(captchaMatcher.find()) {
            String captchaKey = captchaMatcher.group(1);
        }

        result.put("token", getTokenRecaptcha(domain, captchaMatcher.group(1), uri.getScheme() + "://" + uri.getHost() + "/"));

        Matcher numberMatcher = Pattern.compile("recaptchaNumber = '(\\d+?)'").matcher(initialPageContent);

        if(numberMatcher.find()) {
            result.put("number", numberMatcher.group(1));
        }

        return result;
    }

    private static String getTokenRecaptcha(String domain, String siteKey, String url) throws IOException {
        Uri uri = new Uri.Builder()
                .appendQueryParameter("render", siteKey)
                .appendPath("api.js")
                .build();
        HttpURLConnection recaptchOutConnection = SimpleHttpClient.getURLConnection(RECAPTCHA_API_JS + uri);
        recaptchOutConnection.setRequestProperty("referer", url);

        Matcher vTokenMatcher = Pattern.compile("releases/([^/&?#]+)").matcher(SimpleHttpClient.getResponse(recaptchOutConnection));
        if(!vTokenMatcher.find()) {
            return null;
        }
        String vToken = vTokenMatcher.group(1);

        Uri anchorOutURI = new Uri.Builder()
                .appendQueryParameter("ar", "1")
                .appendQueryParameter("k", siteKey)
                .appendQueryParameter("co", domain)
                .appendQueryParameter("hl", "en")
                .appendQueryParameter("v", vToken)
                .appendQueryParameter("size", "invisible")
                .appendQueryParameter("cb", "kr42069kr")
                .appendPath("anchor")
                .build();

        Matcher recaptchaTokenMatcher = Pattern.compile("recaptcha-token.+?=\"(.+?)\"").matcher(SimpleHttpClient.getResponse(
                SimpleHttpClient.getURLConnection("https://www.google.com/recaptcha/api2" + anchorOutURI)
        ));
        if(!recaptchaTokenMatcher.find())   return null;

        String recaptchaToken = recaptchaTokenMatcher.group(1);

        Uri tokenURI = new Uri.Builder()
                .scheme("https")
                .authority("www.google.com")
                .appendPath("recaptcha")
                .appendPath("api2")
                .appendPath("reload")
                .appendQueryParameter("reason", "q")
                .appendQueryParameter("k", siteKey)
                .appendQueryParameter("co", domain)
                .appendQueryParameter("c", recaptchaToken)
                .appendQueryParameter("v", vToken)
                .appendQueryParameter("sa", "")
                .build();

        HttpURLConnection tokenURLConnection = SimpleHttpClient.getURLConnection(tokenURI.toString());
        tokenURLConnection.setRequestProperty("referer", "https://www.google.com/recaptcha/api2");
        tokenURLConnection.setRequestMethod("POST");

        Matcher tokenMatcher = Pattern.compile("rresp\",\"(.+?)\"").matcher(
            SimpleHttpClient.getResponse(tokenURLConnection)
        );
        if(!tokenMatcher.find())    return null;

        return tokenMatcher.group(1);
    }


}
