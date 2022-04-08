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
import java.util.List;

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


        MyAnimelistAnimeData myAnimelistAnimeData = (MyAnimelistAnimeData) getIntent().getSerializableExtra(INTENT_ANIMELIST_DATA_KEY);

        myAnimelistDataViewModel = new ViewModelProvider(this).get(MyAnimelistDataViewModel.class);
        myAnimelistDataViewModel.loadData(myAnimelistAnimeData);
        progressDialog = new ProgressDialog(MyAnimelistInfoActivity.this);
        progressDialog.setMessage("Please Wait...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        myAnimelistDataViewModel.getData().observe(this, d-> {
            progressDialog.dismiss();
        });
    }
}