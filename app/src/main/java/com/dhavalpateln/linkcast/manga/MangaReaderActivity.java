package com.dhavalpateln.linkcast.manga;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;

import com.dhavalpateln.linkcast.R;

public class MangaReaderActivity extends AppCompatActivity {

    private static final String TAG = "MangaReaderActivity";
    public static final String INTENT_REVERSE = "reverse";
    public static final String INTENT_IMAGE_ARRAY = "images";
    public static final String INTENT_START_POSITION = "startpos";

    ViewPager mViewPager;

    // Creating Object of ViewPagerAdapter
    MangaPagerAdapter mViewPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manga_reader);

        Intent intent = getIntent();
        String[] images = intent.getStringArrayExtra(INTENT_IMAGE_ARRAY);


        mViewPager = (ViewPager)findViewById(R.id.mangaViewPager);
        mViewPager.setOffscreenPageLimit(5);

        int initialPosition = intent.getIntExtra(INTENT_START_POSITION, -1);

        if(intent.getBooleanExtra(INTENT_REVERSE, true)) {
            String[] imagesReverse = new String[images.length];
            for (int i = imagesReverse.length - 1; i >= 0; i--) {
                Log.d(TAG, "onCreate: " + images[i]);
                imagesReverse[imagesReverse.length - 1 - i] = images[i];
            }
            images = imagesReverse;
            if(initialPosition == -1)   initialPosition = images.length - 1;
        }
        else if(initialPosition == -1) {
            initialPosition = 0;
        }

        // Initializing the ViewPagerAdapter
        mViewPagerAdapter = new MangaPagerAdapter(MangaReaderActivity.this, images);

        // Adding the Adapter to the ViewPager
        mViewPager.setAdapter(mViewPagerAdapter);
        mViewPager.setCurrentItem(initialPosition, false);
    }

    public static Intent prepareIntent(Context context, String[] images) {
        Intent intent = new Intent(context, MangaReaderActivity.class);
        intent.putExtra(INTENT_IMAGE_ARRAY, images);
        return intent;
    }

}