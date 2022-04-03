package com.dhavalpateln.linkcast.myanimelist;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.dhavalpateln.linkcast.databinding.ActivityMyAnimelistInfoBinding;
import com.dhavalpateln.linkcast.myanimelist.ui.main.MyAnimelistDataViewModel;
import com.dhavalpateln.linkcast.myanimelist.ui.main.MyAnimelistSectionsPagerAdapter;
import com.dhavalpateln.linkcast.utils.SimpleHttpClient;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.HttpURLConnection;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager.widget.ViewPager;

public class MyAnimelistInfoActivity extends AppCompatActivity {

    private ActivityMyAnimelistInfoBinding binding;
    private MyAnimelistDataViewModel myAnimelistDataViewModel;
    private final String TAG = "MyAnimelistInfoActivity";
    private ProgressDialog progressDialog;

    public static final String INTENT_ANIMELIST_DATA_KEY = "animelistdata";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMyAnimelistInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        MyAnimelistSectionsPagerAdapter sectionsPagerAdapter = new MyAnimelistSectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = binding.viewPager;
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = binding.tabs;
        tabs.setupWithViewPager(viewPager);
        FloatingActionButton fab = binding.fab;

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        MyAnimelistAnimeData myAnimelistAnimeData = (MyAnimelistAnimeData) getIntent().getSerializableExtra(INTENT_ANIMELIST_DATA_KEY);

        myAnimelistDataViewModel = new ViewModelProvider(this).get(MyAnimelistDataViewModel.class);
        ExtractInfo extractInfo = new ExtractInfo();
        extractInfo.execute(myAnimelistAnimeData);

    }

    private class ExtractInfo extends AsyncTask<MyAnimelistAnimeData, Void, MyAnimelistAnimeData> {

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(MyAnimelistInfoActivity.this);
            progressDialog.setMessage("Please Wait...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(MyAnimelistAnimeData myAnimelistAnimeData) {
            super.onPostExecute(myAnimelistAnimeData);
            myAnimelistDataViewModel.setData(myAnimelistAnimeData);
            progressDialog.dismiss();
        }

        @Override
        protected MyAnimelistAnimeData doInBackground(MyAnimelistAnimeData... myAnimelistAnimeDatas) {
            MyAnimelistAnimeData myAnimelistAnimeData = myAnimelistAnimeDatas[0];

            if(myAnimelistAnimeData.getUrl() != null) {
                try {
                    HttpURLConnection httpURLConnection = SimpleHttpClient.getURLConnection(myAnimelistAnimeData.getUrl());
                    SimpleHttpClient.setBrowserUserAgent(httpURLConnection);
                    Document html = Jsoup.parse(SimpleHttpClient.getResponse(httpURLConnection));
                    Elements infoElements = html.select("div.spaceit_pad");

                    for(Element infoElement: infoElements) {
                        try {
                            String infoKey = infoElement.getElementsByTag("span").get(0).text();
                            String infoValue = infoElement.text().replace(infoKey, "");
                            myAnimelistAnimeData.putInfo(infoKey.replace(":", ""), infoValue);
                        } catch(Exception e) {
                            e.printStackTrace();
                        }
                    }

                    try {
                        Elements sysnopsisElements = html.select("p[itemprop=description]");
                        myAnimelistAnimeData.setSynopsis(sysnopsisElements.get(0).text());
                    } catch (Exception e) {e.printStackTrace();}

                    try {
                        Elements imageElements = html.select("img[itemprop=image]");
                        myAnimelistAnimeData.addImage(imageElements.get(0).attr("data-src"));
                    } catch (Exception e) {e.printStackTrace();}

                    Log.d(TAG, "Extraction complete");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


            return myAnimelistAnimeData;
        }
    }
}