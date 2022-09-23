package com.dhavalpateln.linkcast;

import android.app.Activity;
import android.content.Context;
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
import com.dhavalpateln.linkcast.animesources.GenericSource;
import com.dhavalpateln.linkcast.animesources.GogoAnime;
import com.dhavalpateln.linkcast.animesources.KwikCX;
import com.dhavalpateln.linkcast.animesources.NineAnime;
import com.dhavalpateln.linkcast.animesources.StreamAni;
import com.dhavalpateln.linkcast.animesources.StreamSB;
import com.dhavalpateln.linkcast.database.AnimeLinkData;
import com.dhavalpateln.linkcast.database.FirebaseDBHelper;
import com.dhavalpateln.linkcast.dialogs.CastDialog;
import com.dhavalpateln.linkcast.exoplayer.ExoPlayerActivity;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.ext.cast.CastPlayer;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastState;
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
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AnimeWebExplorer extends AppCompatActivity {

    public static final String RETURN_RESULT = "returnresult";
    public static final String RESULT_URL = "url";
    public static final String RESULT_REFERER = "referer";
    public static final String RESULT_EPISODE_NUM = "episodenum";

    public static final String EXPLORE_URL = "explore_url";

    WebView animeExplorerWebView;
    String TAG = "AnimeExplorer";
    String currentWebViewURI = null;
    boolean stateSaved = false;
    boolean castDialogOpen = false;
    private AnimeSource animeSource;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anime_web_explorer);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ArrayList<AnimeSource> animeSources = new ArrayList<>();
        animeSources.add(new Animixplay());
        animeSources.add(new AnimeKisaCC());
        animeSources.add(new AnimeKisaTV());
        animeSources.add(new AnimePahe());
        animeSources.add(new FourAnime());
        animeSources.add(new NineAnime());
        animeSources.add(new AnimeUltima());
        animeSources.add(new StreamAni());
        animeSources.add(new KwikCX());
        animeSources.add(new StreamSB());
        animeSources.add(new GogoAnime());

        String exploreUrl = getIntent().getStringExtra(EXPLORE_URL);
        for(AnimeSource source : animeSources) {
            if(source.isCorrectSource(exploreUrl)) {
                animeSource = source;
            }
        }
        if(animeSource == null) {
            animeSource = new GenericSource(exploreUrl);
        }


        animeExplorerWebView = findViewById(R.id.anime_explorer_web_view);
        WebSettings webSettings = animeExplorerWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        animeExplorerWebView.setWebViewClient(new MyWebViewClient());

        animeExplorerWebView.loadUrl(exploreUrl);
        currentWebViewURI = animeExplorerWebView.getUrl();
    }

    private class MyWebViewClient extends WebViewClient {

        @Override
        public void onLoadResource(WebView view, String url) {
            super.onLoadResource(view, url);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {

            Log.d(TAG, "shouldOverrideUrlLoading: " + url);
            if(url.contains("opcharizardon.com")) {
                return true;
            }
            String hostName = Uri.parse(url).getHost();
            if(hostName == null) {
                return true;
            }

            if(url.startsWith("https://na") && url.contains(".mp4")) {
                openCastDialog(url);
                return true;
            }
            if(url.endsWith(".mp4")) {
                openCastDialog(url);
                return true;
            }

            if(url.startsWith("https://animepahe.com/play/") || url.startsWith("https://kwik.cx/")) {
                Toast.makeText(getApplicationContext(), "Use Download option here", Toast.LENGTH_LONG).show();
            }

            if(animeSource != null) {
                if(!animeSource.shouldOverrideURL(url)) return true;
                // This is my website, so do not override; let my WebView load the page
                currentWebViewURI = url;
                view.loadUrl(url);
                return false;

            }

            Log.d(TAG, "shouldOverrideUrlLoading: Blocked: " + url);
            return true;
        }

        private boolean containsAds(String urlString) {
            if(urlString.startsWith("https://prd.jwpltx.com")) {
                return true;
            }
            if(animeSource != null) {
                return animeSource.containsAds(urlString, true, currentWebViewURI);
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
            boolean isPlayable;
            if(animeSource != null) {
                isPlayable = animeSource.isPlayable(requestUrl);
            }
            else {
                isPlayable = request.getUrl().toString().endsWith(".mp4") || request.getUrl().toString().contains(".mp4") || request.getUrl().toString().contains(".m3u8");
            }

            if(isPlayable) {
                if(!castDialogOpen && !stateSaved){
                    openCastDialog(requestUrl);
                    InputStream textStream = new ByteArrayInputStream("".getBytes());
                    return getTextWebResource(textStream);
                }
                Log.d(TAG, "shouldInterceptRequest: " + request.getUrl().toString());
            }
            return super.shouldInterceptRequest(view, request);
        }

        public void openCastDialog(String requestUrl) {
            castDialogOpen = true;

            Map<String, String> data = new HashMap<>();
            if(currentWebViewURI.startsWith("https://animekisa.")) {
                data.put("Referer", "https://linkcast-fbdff.firebaseapp.com/");
            }
            if(currentWebViewURI.startsWith("https://kwik.cx/") || currentWebViewURI.startsWith("https://animepahe.com/")) {
                data.put("Referer", "https://kwik.cx/");
            }

            if(getIntent().getBooleanExtra(RETURN_RESULT, false)) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra(RESULT_URL, requestUrl);
                if(data.containsKey("Referer")) {
                    returnIntent.putExtra(RESULT_REFERER, data.get("Referer"));
                }
                returnIntent.putExtra(RESULT_EPISODE_NUM, getIntent().getStringExtra(RESULT_EPISODE_NUM));
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }
        }

    }

    public static Intent prepareIntent(Context context, AnimeLinkData animeLinkData) {
        Intent intent = new Intent(context, AnimeWebExplorer.class);
        if(animeLinkData.getData() != null) {
            intent.putExtra("mapdata", (HashMap<String, String>) animeLinkData.getData());
            for(Map.Entry<String, String> entry: animeLinkData.getData().entrySet()) {
                intent.putExtra("data-" + entry.getKey(), entry.getValue());
            }
        }
        intent.putExtra("search", animeLinkData.getUrl());
        intent.putExtra("source", "saved");
        intent.putExtra("id", animeLinkData.getId());
        intent.putExtra("title", animeLinkData.getTitle());
        return intent;
    }
}