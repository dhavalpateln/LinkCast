package com.dhavalpateln.linkcast;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.dhavalpateln.linkcast.database.FirebaseDBHelper;
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
import java.util.Map;

public class AnimeWebExplorer extends AppCompatActivity {

    WebView animeExplorerWebView;
    String TAG = "AnimeExplorer";
    String currentWebViewURI = null;
    DatabaseReference animeLinkDBRef;
    boolean notFoundMP4 = true;

    public String getCurrentTime() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        String formattedDate = df.format(c.getTime());
        return formattedDate;
    }

    private String getSearchURL(String searchTerm, String source) {
        if(source.equals("saved")) {
            return searchTerm;
        }
        if(source.equals("4anime")) {
            return "https://4anime.to/?s=" + URLEncoder.encode(searchTerm);
        }
        if(source.equals("9anime")) {
            return "https://www13.9anime.to/search?keyword=" + URLEncoder.encode(searchTerm);
        }
        if(source.equals("animeultima")) {
            return "https://www1.animeultima.to/search?search=" + URLEncoder.encode(searchTerm);
        }
        if(source.equals("animekisa")) {
            return "https://animekisa.tv/search?q=" + URLEncoder.encode(searchTerm);
        }
        return "https://4anime.to/";
    }

    private String getAnimeTitle(boolean includeEpisode) {
        if(getIntent().hasExtra("animeTitle")) {
            return getIntent().getStringExtra("animeTitle");
        }
        String searchTerm = getIntent().getStringExtra("search");
        if(currentWebViewURI.contains("4anime")) {

            String title = currentWebViewURI.split("4anime.to/")[1];
            if(includeEpisode) {
                title = title.split("\\?")[0];
            }
            else {
                if (title.contains("anime/")) {
                    title = title.split("/")[1];
                } else if (title.contains("episode") && !includeEpisode) {
                    title = title.split("-episode")[0];
                } else {
                    title = searchTerm;
                }
            }
            return title;
        }
        if(currentWebViewURI.contains("animekisa")) {
            if(includeEpisode) {
                return currentWebViewURI.split("animekisa.tv/")[1];
            }
            else {
                return currentWebViewURI.split("animekisa.tv/")[1].split("-episode")[0];
            }
        }
        return searchTerm;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anime_web_explorer);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


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
                try {
                    if (calledIntent.getStringExtra("source").equals("saved")) {
                        FirebaseDBHelper.getUserAnimeWebExplorerLinkRef()
                                .child(calledIntent.getStringExtra("id"))
                                .child("url")
                                .setValue(currentWebViewURI);
                        if (currentWebViewURI.contains("4anime") && currentWebViewURI.contains("episode")) {
                            String episodeNum = currentWebViewURI.split("\\?")[0].split("episode-")[1];
                            FirebaseDBHelper.getUserAnimeWebExplorerLinkRef()
                                    .child(calledIntent.getStringExtra("id"))
                                    .child("title")
                                    .setValue(calledIntent.getStringExtra("title").split(" - EP")[0] + " - EP" + episodeNum);
                        }
                        if (currentWebViewURI.contains("animekisa") && currentWebViewURI.contains("episode")) {
                            String episodeNum = currentWebViewURI.split("episode-")[1];
                            FirebaseDBHelper.getUserAnimeWebExplorerLinkRef()
                                    .child(calledIntent.getStringExtra("id"))
                                    .child("title")
                                    .setValue(calledIntent.getStringExtra("title").split(" - EP")[0] + " - EP" + episodeNum);
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
                catch(Exception e) {
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    e.printStackTrace(pw);
                    Intent crashIntent = new Intent(getApplicationContext(), CrashReportActivity.class);
                    crashIntent.putExtra("subject", "Crash");
                    crashIntent.putExtra("message", sw.toString());
                    startActivity(crashIntent);
                }
            }

        });
    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.d(TAG, "shouldOverrideUrlLoading: " + url);
            String hostName = Uri.parse(url).getHost();
            if (hostName.contains("4anime")) {
                // This is my website, so do not override; let my WebView load the page
                currentWebViewURI = url;
                view.loadUrl(url);
                return false;
            }
            if (hostName.contains("animeultima")) {
                // This is my website, so do not override; let my WebView load the page
                currentWebViewURI = url;
                view.loadUrl(url);
                return false;
            }
            if (hostName.contains("9anime")) {
                // This is my website, so do not override; let my WebView load the page
                currentWebViewURI = url;
                view.loadUrl(url);
                return false;
            }
            if (hostName.contains("animekisa")) {
                // This is my website, so do not override; let my WebView load the page
                currentWebViewURI = url;
                view.loadUrl(url);
                return false;
            }
            // Otherwise, the link is not for a page on my site, so launch another Activity that handles URLs
            /*Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);*/
            return true;
        }

        private boolean containsAds(String urlString) {
            if(urlString.contains(".mp4") && notFoundMP4) return false;

            if(urlString.contains(".css") || urlString.contains(".js")) return false;

            if(currentWebViewURI.contains("4anime")) {
                if(urlString.contains("google")||urlString.contains("facebook")) return true;
                if(!urlString.contains("4anime.to")) return true;
            }

            if(currentWebViewURI.contains("animekisa")) {
                if(urlString.contains("google")||urlString.contains("facebook")) return true;
                if(!urlString.contains("animekisa.tv")) return true;
            }

            if(currentWebViewURI.contains("animeultima")) {
                return false;
                /*if(urlString.contains("https://www.googletagmanager.com")) {
                    return false;
                }
                if(urlString.contains("google")||urlString.contains("facebook")) {
                    return true;
                }

                if(!urlString.contains("animeultima")) {
                    return true;
                }*/
            }

            if(currentWebViewURI.contains("9anime")) {

                //if(urlString.contains(".css")) return false;
                /*if(urlString.contains("google")||urlString.contains("facebook")) {
                    return true;
                }*/
                /*if(urlString.contains(".jpg") || urlString.contains("https://www.google.com/recaptcha")) {
                    return false;
                }*/
                if(!urlString.contains("9anime.to")) {
                    return true;
                }
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
            if(request.getUrl().toString().endsWith(".mp4")) {
                notFoundMP4 = false;
                if(currentWebViewURI.contains("animeultima")) {
                }
                else {
                    Intent intent = new Intent(getApplicationContext(), MediaReceiver.class);
                    intent.setData(request.getUrl());

                    String animeTitle = getAnimeTitle(true);
                    intent.putExtra("title", animeTitle);
                    intent.putExtra("intentSource", "anime_web_explorer");
                    startActivity(intent);
                    finish();
                }
                Log.d(TAG, "shouldInterceptRequest: " + request.getUrl().toString());
            }
            return super.shouldInterceptRequest(view, request);
        }
    }
}