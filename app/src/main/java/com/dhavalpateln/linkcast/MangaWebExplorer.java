package com.dhavalpateln.linkcast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.dhavalpateln.linkcast.database.FirebaseDBHelper;
import com.dhavalpateln.linkcast.manga.MangaReaderActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MangaWebExplorer extends AppCompatActivity {

    WebView mangaExplorerWebView;
    String TAG = "MangaExplorer";
    String currentWebViewURI = null;
    ProgressDialog progressDialog;
    @Override
    protected void onPause() {
        super.onPause();
        if (mangaExplorerWebView != null) {
            mangaExplorerWebView.onPause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mangaExplorerWebView != null) {
            mangaExplorerWebView.onResume();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mangaExplorerWebView != null) {
            mangaExplorerWebView.destroy();
            mangaExplorerWebView = null;
        }
    }

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
        if(source.equals("mangadex")) {
            return "https://mangadex.tv/search?type=titles&title=" + URLEncoder.encode(searchTerm);
        }
        if(source.equals("manga4life")) {
            return "https://manga4life.com/search/?name=" + URLEncoder.encode(searchTerm);
        }
        return "https://manga4life.com/";
    }

    private String getMangaTitle(boolean includeChapter) {
        if(getIntent().hasExtra("mangaTitle")) {
            return getIntent().getStringExtra("mangaTitle");
        }
        String searchTerm = getIntent().getStringExtra("search");


        return searchTerm;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manga_web_explorer);

        final Intent calledIntent = getIntent();
        mangaExplorerWebView = findViewById(R.id.manga_explorer_web_view);
        WebSettings webSettings = mangaExplorerWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);

        mangaExplorerWebView.setWebViewClient(new MyWebViewClient());
        mangaExplorerWebView.loadUrl(getSearchURL(
                calledIntent.getStringExtra("search"),
                calledIntent.getStringExtra("source")
        ));
        currentWebViewURI = mangaExplorerWebView.getUrl();

        FloatingActionButton fab = findViewById(R.id.mangafab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (calledIntent.getStringExtra("source").equals("saved")) {
                        FirebaseDBHelper.getUserAnimeWebExplorerLinkRef()
                                .child(calledIntent.getStringExtra("id"))
                                .child("url")
                                .setValue(currentWebViewURI);
                        if (currentWebViewURI.contains("mangadex") && currentWebViewURI.contains("something to save")) {
                            String episodeNum = currentWebViewURI.split("\\?")[0].split("episode-")[1];
                            FirebaseDBHelper.getUserAnimeWebExplorerLinkRef()
                                    .child(calledIntent.getStringExtra("id"))
                                    .child("title")
                                    .setValue(calledIntent.getStringExtra("title").split(" - EP")[0] + " - EP" + episodeNum);
                        }

                        Snackbar.make(view, "Bookmark Updated", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    } else {
                        DatabaseReference mangaLinkDBRef = FirebaseDBHelper.getUserMangaWebExplorerLinkRef();
                        String time = getCurrentTime();
                        Map<String, Object> update = new HashMap<>();
                        update.put(time + "/title", getMangaTitle(false));
                        update.put(time + "/url", currentWebViewURI);
                        mangaLinkDBRef.updateChildren(update);
                        Snackbar.make(view, "Added the link to Bookmarks", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                }
                catch(Exception e) {
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    e.printStackTrace(pw);

                }
            }

        });

    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.d(TAG, "shouldOverrideUrlLoading: " + url);
            String hostName = Uri.parse(url).getHost();
            if (hostName.contains("mangadex")) {
                // This is my website, so do not override; let my WebView load the page
                if(url.contains("/chapter")) {
                    ExtractManga extractMangaTask = new ExtractManga();
                    extractMangaTask.execute(url);
                    return true;
                }

                currentWebViewURI = url;
                view.loadUrl(url);
                return false;
            }
            if (hostName.contains("manga4life")) {
                // This is my website, so do not override; let my WebView load the page
                if(url.contains("-chapter-")) {
                    if(url.contains("-page-1")) {
                        url = url.replace("-page-1", "");
                    }
                    ExtractManga extractMangaTask = new ExtractManga();
                    extractMangaTask.execute(url);
                    return true;
                }

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

            if(currentWebViewURI.contains("mangadex")) {
                if(urlString.contains(".css") || urlString.contains(".js")) return false;
                if(urlString.contains("google")||urlString.contains("facebook")) return true;
                if(urlString.contains("cm.blazefast.co")) return false;
                if(!urlString.contains("mangadex")) return true;
            }
            if(currentWebViewURI.contains("manga4life")) {
                if(urlString.contains("ads"))   return true;
                if(urlString.contains(".css") || urlString.contains(".js")) return false;
                //if(urlString.contains("google")||urlString.contains("facebook")) return true;
                if(urlString.contains("revcontent")) return true;
                if(urlString.contains(".png") || urlString.contains(".jpg")) return false;
                if(!urlString.contains("manga4life")) return true;
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

            return super.shouldInterceptRequest(view, request);
        }



    }

    public class ExtractManga extends AsyncTask<String, Integer, String> {

        String mangaURL;

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(MangaWebExplorer.this);
            progressDialog.setMessage("Please Wait...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(String s) {
            progressDialog.dismiss();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            if(values[0].equals(0)) {
                Toast.makeText(getApplicationContext(), "Error getting manga", Toast.LENGTH_LONG).show();
                currentWebViewURI = mangaURL;
                mangaExplorerWebView.loadUrl(mangaURL);
            }
        }

        private boolean isImageURL(String url) {
            try {
                HttpURLConnection imageCheckConnection = (HttpURLConnection) (new URL(url)).openConnection();
                if(imageCheckConnection.getContentType().contains("image/")) {
                    return true;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                String urlString = strings[0];
                this.mangaURL = urlString;
                URL url = new URL(urlString);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    int bufferSize = 1024;
                    char[] buffer = new char[bufferSize];
                    StringBuilder out = new StringBuilder();
                    Reader inr = new InputStreamReader(in, StandardCharsets.UTF_8);
                    for (int numRead; (numRead = inr.read(buffer, 0, buffer.length)) > 0; ) {
                        out.append(buffer, 0, numRead);
                    }
                    String result = out.toString();
                    String lines[] = result.split("\n");
                    ArrayList<String> imageLines = new ArrayList<>();
                    if (urlString.contains("mangadex.tv")) {
                        for (String line : lines) {
                            if (line.contains("reader-image-wrapper")) {
                                Pattern pattern = Pattern.compile("data-src=\"(.*?)\"");
                                Matcher matcher = pattern.matcher(line);
                                if (matcher.find()) {
                                    imageLines.add(matcher.group(1));
                                }
                            }
                        }
                    }
                    if (urlString.contains("manga4life")) {
                        JSONObject curChapter = null;
                        String curPathName = null;
                        String indexName = null;
                        for (String line : lines) {
                            if (line.contains("<img class=\"img-fluid")) {
                                Pattern pattern = Pattern.compile(" src=\"(.*?)\"");
                                Matcher matcher = pattern.matcher(line);
                                if (matcher.find()) {
                                    imageLines.add(matcher.group(1));
                                }
                            }
                            if (curChapter == null && line.contains("vm.CurChapter = {")) {
                                Pattern pattern = Pattern.compile("vm.CurChapter = (.*?);");
                                Matcher matcher = pattern.matcher(line);
                                if (matcher.find()) {
                                    try {
                                        curChapter = new JSONObject(matcher.group(1));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                            if (curPathName == null && line.contains("vm.CurPathName = ")) {
                                Pattern pattern = Pattern.compile("vm.CurPathName = \"(.*?)\";");
                                Matcher matcher = pattern.matcher(line);
                                if (matcher.find()) {
                                    curPathName = matcher.group(1);
                                }
                            }
                            if (indexName == null && line.contains("vm.IndexName = ")) {
                                Pattern pattern = Pattern.compile("vm.IndexName = \"(.*?)\";");
                                Matcher matcher = pattern.matcher(line);
                                if (matcher.find()) {
                                    indexName = matcher.group(1);
                                }
                            }
                        }
                        try {
                            String chapter = curChapter.getString("Chapter").substring(1, curChapter.getString("Chapter").length() - 1);
                            chapter += curChapter.getString("Chapter").charAt(curChapter.getString("Chapter").length() - 1) == '0' ? "" : ("." + curChapter.getString("Chapter").charAt(curChapter.getString("Chapter").length() - 1));
                            int pages = Integer.valueOf(curChapter.getString("Page"));
                            String directory = curChapter.getString("Directory");
                            String imgLinkPrefix = "https://" + curPathName + "/manga/" + indexName + "/" + (directory.equals("") ? "" : (directory+'/')) + chapter + "-";//{{vm.PageImage(Page)}}.png";
                            if(!isImageURL(imgLinkPrefix + "001.png")) {
                                imgLinkPrefix = "https://" + "scans-hot.leanbox.us" + "/manga/" + indexName + "/" + (directory.equals("") ? "" : (directory+'/')) + chapter + "-";
                            }
                            if(isImageURL(imgLinkPrefix + "001.png")) {
                                for (int pageNum = 1; pageNum <= pages; pageNum++) {
                                    String pageString = "000" + pageNum;
                                    imageLines.add(imgLinkPrefix + pageString.substring(pageString.length() - 3) + ".png");
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Log.d(TAG, "doInBackground: read manga4life");
                    }

                    String[] images = imageLines.toArray(new String[0]);
                    if (images.length > 0) {
                        Intent intent = new Intent(MangaWebExplorer.this, MangaReaderActivity.class);
                        intent.putExtra("images", images);
                        startActivity(intent);
                    } else {
                        publishProgress(0);
                    }


                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } finally {
                    urlConnection.disconnect();
                }
                return null;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}