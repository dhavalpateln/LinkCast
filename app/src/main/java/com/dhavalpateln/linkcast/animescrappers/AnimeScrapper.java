package com.dhavalpateln.linkcast.animescrappers;

import com.dhavalpateln.linkcast.database.AnimeLinkData;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public abstract class AnimeScrapper {

    String baseUrl;
    Map<String, String> dataMap;
    Map<String, Map<String, String>> episodeList;
    private int connectionTimeout = 6000;

    public AnimeScrapper(String baseUrl) {
        this.baseUrl = baseUrl;
        episodeList = new HashMap<>();
        dataMap = new HashMap<>();
    }

    public String getHttpContent(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        //urlConnection.setConnectTimeout(connectionTimeout);
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
    public String getHttpContent(String urlString, String referer) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        //urlConnection.setConnectTimeout(connectionTimeout);
        urlConnection.setRequestProperty("Referer", referer);
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

    public String getHttpContent(String urlString, Map<String, String> headers) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        //urlConnection.setConnectTimeout(connectionTimeout);
        for(String header: headers.keySet()) {
            urlConnection.setRequestProperty(header, headers.get(header));
        }

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

    public int getHttpResponseCode(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        //urlConnection.setConnectTimeout(connectionTimeout);
        return urlConnection.getResponseCode();
    }

    public String postHttpContent(String urlString) throws IOException {

        URL url = new URL(urlString);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        //urlConnection.setConnectTimeout(connectionTimeout);
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

    public void setData(String key, String value) {
        dataMap.put(key, value);
    }

    public String getData(String key) {
        if(dataMap.containsKey(key)) {
            return dataMap.get(key);
        }
        return null;
    }

    public abstract boolean isCorrectURL(String url);
    public abstract Map<String, String> getEpisodeList(String episodeListUrl) throws IOException;
    public abstract Map<String, String> extractEpisodeUrls(String episodeUrl) throws IOException;
    public abstract Map<String, String> extractData(AnimeLinkData data);
    public abstract String getDisplayName();
}
