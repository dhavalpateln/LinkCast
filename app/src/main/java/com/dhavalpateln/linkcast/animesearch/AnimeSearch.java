package com.dhavalpateln.linkcast.animesearch;

import com.dhavalpateln.linkcast.database.AnimeLinkData;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public abstract class AnimeSearch {

    public static final String IMAGE = "image";
    public static final String ID = "id";
    public static final String LINK = "link";
    public static final String TITLE = "title";


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

    public boolean isMangeSource() {return false;}

    public abstract ArrayList<AnimeLinkData> search(String term);
    public abstract String getName();
    public abstract boolean hasQuickSearch();
}
