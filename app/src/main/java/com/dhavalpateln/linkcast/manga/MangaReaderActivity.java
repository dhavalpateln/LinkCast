package com.dhavalpateln.linkcast.manga;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.dhavalpateln.linkcast.R;
import com.dhavalpateln.linkcast.extractors.mangareader.MangaReaderTransform;

import java.util.ArrayList;
import java.util.List;

public class MangaReaderActivity extends AppCompatActivity {

    private static final String TAG = "MangaReaderActivity";
    public static final String INTENT_REVERSE = "reverse";
    public static final String INTENT_IMAGE_ARRAY = "images";
    public static final String INTENT_START_POSITION = "startpos";
    public static final String INTENT_VERTICAL_MODE = "vertical";
    private boolean showVertical = false;



    private ViewPager mViewPager;

    // Creating Object of ViewPagerAdapter
    private MangaPagerAdapter mViewPagerAdapter;
    private VerticalRecyclerAdapter verticalRecyclerAdapter;
    private RecyclerView verticalRecyclerView;

    private class VerticalRecyclerAdapter extends MangaRecyclerAdapter {

        //private BitmapTransformation trans = new MangaReaderTransform();

        public VerticalRecyclerAdapter(String[] recyclerDataArrayList, Context mcontext) {
            super(recyclerDataArrayList, mcontext);
        }

        @Override
        public void onBindViewHolder(RecyclerViewHolder holder, int position, String imageUrl) {
            Glide.with(getApplicationContext())
                    .load(imageUrl)
                    .transition(new DrawableTransitionOptions().crossFade())
                    //.transform(trans)
                    //.bitmapTransform(new CropCircleTransformation(getApplicationContext()))
                    //.diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.imageView);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manga_reader);

        Intent intent = getIntent();
        String[] images = intent.getStringArrayExtra(INTENT_IMAGE_ARRAY);

        mViewPager = findViewById(R.id.mangaViewPager);
        verticalRecyclerView = findViewById(R.id.manga_vertical_recycler_view);
        mViewPager.setOffscreenPageLimit(5);

        showVertical = intent.getBooleanExtra(INTENT_VERTICAL_MODE, false);

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
        if(showVertical) {
            verticalRecyclerAdapter = new VerticalRecyclerAdapter(images, MangaReaderActivity.this);
            LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
            verticalRecyclerView.setLayoutManager(layoutManager);
            verticalRecyclerView.setAdapter(verticalRecyclerAdapter);
            mViewPager.setVisibility(View.GONE);
            verticalRecyclerView.setVisibility(View.VISIBLE);
        }
        else {
            mViewPagerAdapter = new MangaPagerAdapter(MangaReaderActivity.this, images);
            mViewPager.setAdapter(mViewPagerAdapter);
            mViewPager.setCurrentItem(initialPosition, false);
            mViewPager.setVisibility(View.VISIBLE);
            verticalRecyclerView.setVisibility(View.GONE);
        }
    }

    public static Intent prepareIntent(Context context, String[] images) {
        Intent intent = new Intent(context, MangaReaderActivity.class);
        intent.putExtra(INTENT_IMAGE_ARRAY, images);
        return intent;
    }

}