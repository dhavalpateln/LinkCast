package com.dhavalpateln.linkcast.utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class SimpleHttpClient {

    public static HttpURLConnection getURLConnection(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        return urlConnection;
    }

    public static void setBrowserUserAgent(HttpURLConnection urlConnection) {
        urlConnection.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/95.0.4638.69 Safari/537.36");
    }

    public static String getResponse(HttpURLConnection urlConnection) throws IOException {
        InputStream in = new BufferedInputStream(urlConnection.getInputStream());
        int bufferSize = 1024;
        char[] buffer = new char[bufferSize];
        StringBuilder out = new StringBuilder();
        Reader inr = new InputStreamReader(in, StandardCharsets.UTF_8);
        for (int numRead; (numRead = inr.read(buffer, 0, buffer.length)) > 0; ) {
            out.append(buffer, 0, numRead);
        }
        String result = out.toString();
        return result;
    }

    public static JSONObject getJSONResponse(HttpURLConnection urlConnection) throws IOException, JSONException {
        return new JSONObject(getResponse(urlConnection));
    }

    public static int getResponseCode(HttpURLConnection urlConnection){
        try {
            urlConnection.setConnectTimeout(3000);
            return urlConnection.getResponseCode();
        } catch (SocketTimeoutException e) {
            return 408;
        } catch (IOException e) {
            return 500;
        }
    }

}
