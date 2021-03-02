package com.dhavalpateln.linkcast;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

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

public class AnimeWebExplorer extends AppCompatActivity {

    WebView animeExplorerWebView;
    String TAG = "AnimeExplorer";
    String currentWebViewURI = null;
    boolean notFoundMP4 = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anime_web_explorer);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        animeExplorerWebView = findViewById(R.id.anime_explorer_web_view);
        WebSettings webSettings = animeExplorerWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        animeExplorerWebView.setWebViewClient(new MyWebViewClient());
        animeExplorerWebView.loadUrl("https://4anime.to/");

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.d(TAG, "shouldOverrideUrlLoading: " + url);
            if (Uri.parse(url).getHost().contains("4anime")) {
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
            if(urlString.contains(".mp4") && notFoundMP4) {
                return false;
            }
            if(urlString.contains("google")||urlString.contains("facebook")) {
                return true;
            }
            if(!urlString.contains("4anime.to")) {
                return true;
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
                InputStream textStream = new ByteArrayInputStream("".getBytes());
                return getTextWebResource(textStream);
            }
            Log.d(TAG, "shouldInterceptRequest: " + request.getUrl().toString());
            if(request.getUrl().toString().contains(".mp4")) {
                notFoundMP4 = false;
                Intent intent = new Intent(getApplicationContext(), MediaReceiver.class);
                intent.setData(request.getUrl());
                String animeTitle = currentWebViewURI.split("4anime.to/")[1].split("\\?")[0];
                intent.putExtra("title", animeTitle);
                intent.putExtra("intentSource", "anime_web_explorer");
                startActivity(intent);
                finish();
                Log.d(TAG, "shouldInterceptRequest: " + request.getUrl().toString());
            }
            return super.shouldInterceptRequest(view, request);
        }
    }
}