package com.dhavalpateln.linkcast.manga;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;

import com.dhavalpateln.linkcast.R;

public class MangaReaderActivity extends AppCompatActivity {

    private static final String TAG = "MangaReaderActivity";
    ViewPager mViewPager;

    // Creating Object of ViewPagerAdapter
    MangaPagerAdapter mViewPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manga_reader);

        Intent intent = getIntent();
        String[] images = intent.getStringArrayExtra("images");

        mViewPager = (ViewPager)findViewById(R.id.mangaViewPager);
        mViewPager.setOffscreenPageLimit(5);

        String[] imagesReverse = new String[images.length];
        for(int i = imagesReverse.length - 1; i >= 0; i--) {
            Log.d(TAG, "onCreate: " + images[i]);
            imagesReverse[imagesReverse.length - 1 - i] = images[i];
        }

        // Initializing the ViewPagerAdapter
        mViewPagerAdapter = new MangaPagerAdapter(MangaReaderActivity.this, imagesReverse);

        // Adding the Adapter to the ViewPager
        mViewPager.setAdapter(mViewPagerAdapter);
        mViewPager.setCurrentItem(imagesReverse.length - 1, false);
    }
}