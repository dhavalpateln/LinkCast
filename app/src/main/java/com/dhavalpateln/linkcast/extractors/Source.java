package com.dhavalpateln.linkcast.extractors;

import com.dhavalpateln.linkcast.utils.SimpleHttpClient;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public abstract class Source {

    public static enum SOURCE_TYPE {
        ANIME,
        MANGA
    }

    private boolean initialized = true;
    private SOURCE_TYPE sourceType = SOURCE_TYPE.ANIME;

    public void init() {}
    public boolean requiresInit() {
        return !initialized;
    }
    public void setRequiresInit(boolean value) {this.initialized = !value;}
    public void configConnection(HttpURLConnection urlConnection) {
        return;
    }
    public String getHttpContent(String urlString) throws IOException {
        HttpURLConnection urlConnection = SimpleHttpClient.getURLConnection(urlString);
        configConnection(urlConnection);
        return SimpleHttpClient.getResponse(urlConnection);
    }
    public String getHttpContent(String urlString, String referer) throws IOException {
        HttpURLConnection urlConnection = SimpleHttpClient.getURLConnection(urlString);
        urlConnection.setRequestProperty("Referer", referer);
        configConnection(urlConnection);
        return SimpleHttpClient.getResponse(urlConnection);
    }
    public String getHttpContent(String urlString, Map<String, String> headers) throws IOException {
        HttpURLConnection urlConnection = SimpleHttpClient.getURLConnection(urlString);
        for(String header: headers.keySet()) {
            urlConnection.setRequestProperty(header, headers.get(header));
        }
        configConnection(urlConnection);
        return SimpleHttpClient.getResponse(urlConnection);
    }
    public String getRedirectUrl(String urlString) throws IOException {
        HttpURLConnection urlConnection = SimpleHttpClient.getURLConnection(urlString);
        urlConnection.setInstanceFollowRedirects(false);
        configConnection(urlConnection);
        return urlConnection.getHeaderField("Location");
    }
    public int getHttpResponseCode(String urlString) throws IOException {
        try {
            HttpURLConnection urlConnection = SimpleHttpClient.getURLConnection(urlString);
            urlConnection.setConnectTimeout(3000);
            configConnection(urlConnection);
            return urlConnection.getResponseCode();
        } catch (SocketTimeoutException e) {
            return 408;
        }
    }
    public String postHttpContent(String urlString) throws IOException {

        URL url = new URL(urlString);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        configConnection(urlConnection);
        urlConnection.setRequestMethod("POST");

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

    public boolean isAnimeSource() {
        return sourceType == SOURCE_TYPE.ANIME;
    }
    public boolean isMangaSource() {
        return sourceType == SOURCE_TYPE.MANGA;
    }

    protected void setSourceType(SOURCE_TYPE type) {
        this.sourceType = type;
    }
    public abstract String getDisplayName();


}
