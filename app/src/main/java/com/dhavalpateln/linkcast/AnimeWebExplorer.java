package com.dhavalpateln.linkcast;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.dhavalpateln.linkcast.animesources.AnimeKisaCC;
import com.dhavalpateln.linkcast.animesources.AnimeKisaTV;
import com.dhavalpateln.linkcast.animesources.AnimePahe;
import com.dhavalpateln.linkcast.animesources.AnimeSource;
import com.dhavalpateln.linkcast.animesources.AnimeUltima;
import com.dhavalpateln.linkcast.animesources.Animixplay;
import com.dhavalpateln.linkcast.animesources.FourAnime;
import com.dhavalpateln.linkcast.animesources.NineAnime;
import com.dhavalpateln.linkcast.animesources.StreamAni;
import com.dhavalpateln.linkcast.database.FirebaseDBHelper;
import com.dhavalpateln.linkcast.dialogs.CastDialog;
import com.dhavalpateln.linkcast.ui.feedback.CrashReportActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AnimeWebExplorer extends AppCompatActivity {

    WebView animeExplorerWebView;
    String TAG = "AnimeExplorer";
    String currentWebViewURI = null;
    DatabaseReference animeLinkDBRef;
    boolean notFoundMP4 = true;
    boolean stateSaved = false;
    private boolean castDialogOpen = false;
    private Map<String, AnimeSource> animeSourceMap;
    Set<String> mp4sFound;

    @Override
    protected void onPause() {
        super.onPause();
        if (animeExplorerWebView != null) {
            animeExplorerWebView.onPause();
        }
        stateSaved = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (animeExplorerWebView != null) {
            animeExplorerWebView.onResume();
        }
        stateSaved = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (animeExplorerWebView != null) {
            animeExplorerWebView.destroy();
            animeExplorerWebView = null;
        }
    }

    public String getCurrentTime() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        String formattedDate = df.format(c.getTime());
        return formattedDate;
    }

    private AnimeSource getAnimeSource(String term) {
        for(Map.Entry<String, AnimeSource> entry: animeSourceMap.entrySet()) {
            if(entry.getValue().isCorrectSource(term)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private String getSearchURL(String searchTerm, String source) {
        if(source.equals("saved")) {
            return searchTerm;
        }
        AnimeSource animeSource = getAnimeSource(source);
        if(animeSource != null) {
            return animeSource.getSearchURL(searchTerm);
        }
        return animeSourceMap.get("animekisa.tv").getSearchURL(searchTerm);
    }

    private String getAnimeTitle(boolean includeEpisode) {
        if(getIntent().hasExtra("animeTitle")) {
            return getIntent().getStringExtra("animeTitle");
        }
        String searchTerm = getIntent().getStringExtra("search");
        try {
            AnimeSource animeSource = getAnimeSource(currentWebViewURI);
            if(animeSource != null) {
                return animeSource.getAnimeTitle(currentWebViewURI, searchTerm, includeEpisode);
            }
        } catch (Exception e) {
            return searchTerm;
        }
        return searchTerm;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anime_web_explorer);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        animeSourceMap = new HashMap<>();
        animeSourceMap.put("animixplay.to", new Animixplay());
        animeSourceMap.put("animekisa.cc", new AnimeKisaCC());
        animeSourceMap.put("animekisa.tv", new AnimeKisaTV());
        animeSourceMap.put("animepahe.com", new AnimePahe());
        animeSourceMap.put("4anime.org", new FourAnime());
        animeSourceMap.put("9anime.to", new NineAnime());
        animeSourceMap.put("animeultima", new AnimeUltima());
        animeSourceMap.put("streamani.net", new StreamAni());


        mp4sFound = new HashSet<>();
        final Intent calledIntent = getIntent();
        animeExplorerWebView = findViewById(R.id.anime_explorer_web_view);
        WebSettings webSettings = animeExplorerWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        animeExplorerWebView.setWebViewClient(new MyWebViewClient());
        animeExplorerWebView.loadUrl(getSearchURL(
                calledIntent.getStringExtra("search"),
                calledIntent.getStringExtra("source")
        ));
        currentWebViewURI = animeExplorerWebView.getUrl();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                    if (calledIntent.getStringExtra("source").equals("saved")) {
                        FirebaseDBHelper.getUserAnimeWebExplorerLinkRef()
                                .child(calledIntent.getStringExtra("id"))
                                .child("url")
                                .setValue(currentWebViewURI);

                        AnimeSource animeSource = getAnimeSource(currentWebViewURI);
                        if(animeSource != null) {
                            animeSource.updateBookmarkPage(currentWebViewURI, calledIntent.getStringExtra("id"), calledIntent.getStringExtra("title"));
                        }
                        Snackbar.make(view, "Bookmark Updated", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    } else {
                        animeLinkDBRef = FirebaseDBHelper.getUserAnimeWebExplorerLinkRef();
                        String time = getCurrentTime();
                        Map<String, Object> update = new HashMap<>();
                        update.put(time + "/title", getAnimeTitle(false));
                        update.put(time + "/url", currentWebViewURI);
                        animeLinkDBRef.updateChildren(update);
                        Snackbar.make(view, "Added the link to Bookmarks", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }


            }

        });
    }

    private class MyWebViewClient extends WebViewClient {

        @Override
        public void onLoadResource(WebView view, String url) {
            super.onLoadResource(view, url);


        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {

            Log.d(TAG, "shouldOverrideUrlLoading: " + url);
            String hostName = Uri.parse(url).getHost();
            if(hostName == null) {
                return true;
            }

            AnimeSource animeSource = getAnimeSource(hostName);
            if(animeSource != null) {
                // This is my website, so do not override; let my WebView load the page
                currentWebViewURI = url;
                view.loadUrl(url);
                mp4sFound = new HashSet<>();
                return false;
            }

            // Otherwise, the link is not for a page on my site, so launch another Activity that handles URLs
            /*Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);*/
            return true;
        }

        private boolean containsAds(String urlString) {

            if(urlString.startsWith("https://prd.jwpltx.com")) {
                return true;
            }

            AnimeSource animeSource = getAnimeSource(currentWebViewURI);
            if(animeSource != null) {
                return animeSource.containsAds(urlString, notFoundMP4, currentWebViewURI);
            }
            if(currentWebViewURI.contains("mp4upload.com")) {
                //return false;
                if(urlString.endsWith(".mp4") && notFoundMP4) return false;
                if(urlString.contains("https://streamani.net/")) return false;
                if(urlString.contains("https://dood.la/")) return false;
                if(urlString.contains("https://hydrax.net/")) return false;
                if(urlString.contains("https://sbplay.org/")) return false;
                if(urlString.contains("gogo-stream")) return false;
                if(urlString.contains(".css") || urlString.contains(".js")) return false;
                if(urlString.contains("google")||urlString.contains("facebook")) return true;
                if(urlString.contains("https://gogo-stream.com/loadserver.php?id=MTY1Njkx")) return false;
                if(!urlString.contains("mp4upload.com")) return true;
            }
            return false;
        }



        private WebResourceResponse getTextWebResource(InputStream data) {
            return new WebResourceResponse("text/plain", "UTF-8", data);
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Nullable
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            String urlString = request.getUrl().toString();
            if(containsAds(urlString)){
                Log.d(TAG, "Blocked: " + urlString);
                InputStream textStream = new ByteArrayInputStream("".getBytes());
                return getTextWebResource(textStream);
            }
            Log.d(TAG, "shouldInterceptRequest: " + request.getUrl().toString());
            String requestUrl = request.getUrl().toString();

            AnimeSource animeSource = getAnimeSource(currentWebViewURI);
            boolean isPlayable = false;
            if(animeSource != null) {
                isPlayable = animeSource.isPlayable(requestUrl);
            }
            else {
                isPlayable = request.getUrl().toString().endsWith(".mp4") || request.getUrl().toString().contains(".mp4") || request.getUrl().toString().contains(".m3u8");
            }

            if(isPlayable) {

                if(mp4sFound.contains(requestUrl)) {
                    InputStream textStream = new ByteArrayInputStream("".getBytes());
                    return getTextWebResource(textStream);
                }
                else {
                    //notFoundMP4 = false;
                    mp4sFound.add(requestUrl);
                    Log.d(TAG, "shouldInterceptRequest: found mp4: " + request.getUrl().toString());
                }
                if(currentWebViewURI.contains("animeultima")) {
                }
                /*else if(currentWebViewURI.contains("animixplay") && !requestUrl.startsWith("https://v1.mp4.sh/")) {

                }*/
                else if(!castDialogOpen && !stateSaved){
                    castDialogOpen = true;


                    String animeTitle = getAnimeTitle(true);

                    MediaReceiver.insertData("video", animeTitle, request.getUrl().toString());

                    Map<String, CastDialog.OnClickListener> map = new HashMap<>();

                    map.put("PLAY", new CastDialog.OnClickListener() {
                        @Override
                        public void onClick(CastDialog castDialog, String title, String url) {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                            startActivity(intent);
                            castDialogOpen = false;
                            mp4sFound = new HashSet<>();
                            mp4sFound.add(url);
                            castDialog.close();
                        }
                    });
                    map.put("CAST MORE", new CastDialog.OnClickListener() {
                        @Override
                        public void onClick(CastDialog castDialog, String title, String url) {
                            castDialogOpen = false;
                            mp4sFound = new HashSet<>();
                            mp4sFound.add(url);
                            castDialog.close();
                        }
                    });
                    map.put("MAIN MENU", new CastDialog.OnClickListener() {
                        @Override
                        public void onClick(CastDialog castDialog, String title, String url) {
                            castDialogOpen = false;
                            mp4sFound = new HashSet<>();
                            mp4sFound.add(url);
                            castDialog.close();
                            finish();
                        }
                    });

                    CastDialog castDialog = new CastDialog(animeTitle, request.getUrl().toString(), map);
                    castDialog.show(getSupportFragmentManager(), "CastDialog");
                    notFoundMP4 = true;
                    /*Intent intent = new Intent(getApplicationContext(), MediaReceiver.class);
                    intent.setData(request.getUrl());
                    intent.putExtra("title", animeTitle);
                    intent.putExtra("intentSource", "anime_web_explorer");
                    startActivity(intent);
                    finish();*/
                    InputStream textStream = new ByteArrayInputStream("".getBytes());
                    return getTextWebResource(textStream);
                }
                Log.d(TAG, "shouldInterceptRequest: " + request.getUrl().toString());
            }
            return super.shouldInterceptRequest(view, request);
        }



    }
}