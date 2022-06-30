package com.dhavalpateln.linkcast.myanimelist;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

import com.dhavalpateln.linkcast.R;
import com.dhavalpateln.linkcast.database.MyAnimeListDatabase;
import com.dhavalpateln.linkcast.manga.MangaReaderActivity;
import com.dhavalpateln.linkcast.myanimelist.adapters.SliderAdapter;
import com.smarteist.autoimageslider.SliderView;

import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MyAnimelistCharacterActivity extends AppCompatActivity {

    public static final String INTENT_CHARACTER_DATA = "chardata";
    private ProgressDialog progressDialog;
    private SliderView sliderView;
    private TextView titleTextView;
    private TextView aboutTextView;
    private ArrayList<String> sliderImageURLs;
    private SliderAdapter imageSliderAdapter;
    private Executor executor = Executors.newSingleThreadExecutor();
    private Handler uiHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_animelist_character);

        sliderImageURLs = new ArrayList<>();

        titleTextView = findViewById(R.id.character_title);
        aboutTextView = findViewById(R.id.character_about_text_view);
        sliderView = findViewById(R.id.character_img_slider);
        imageSliderAdapter = new SliderAdapter(getApplicationContext(), sliderImageURLs, (imageList, position) -> {
            Intent intent = new Intent(this, MangaReaderActivity.class);
            intent.putExtra(MangaReaderActivity.INTENT_REVERSE, false);
            intent.putExtra(MangaReaderActivity.INTENT_START_POSITION, position);
            intent.putExtra(MangaReaderActivity.INTENT_IMAGE_ARRAY, imageList.toArray(new String[0]));
            startActivity(intent);
        });

        sliderView.setAutoCycleDirection(SliderView.LAYOUT_DIRECTION_LTR);
        sliderView.setSliderAdapter(imageSliderAdapter);


        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please Wait...");
        progressDialog.setCancelable(false);


        MyAnimelistCharacterData intentData = (MyAnimelistCharacterData) getIntent().getSerializableExtra(INTENT_CHARACTER_DATA);
        executor.execute(() -> {
            uiHandler.post(() -> progressDialog.show());
            MyAnimelistCharacterData data = MyAnimeListDatabase.getInstance().getCharacterData(intentData);
            uiHandler.post(() -> {
                titleTextView.setText(data.getName());
                if(data.getAbout().equals("null")) {
                    data.setAbout("No Info");
                }
                aboutTextView.setText(data.getAbout());
                sliderImageURLs.clear();
                sliderImageURLs.addAll(data.getImages());
                imageSliderAdapter.notifyDataSetChanged();
                sliderView.setScrollTimeInSec(5);
                sliderView.setAutoCycle(true);
                sliderView.startAutoCycle();
                progressDialog.dismiss();
            });
        });
    }



    public static Intent prepareIntent(Context context, MyAnimelistCharacterData data) {
        Intent intent = new Intent(context, MyAnimelistCharacterActivity.class);
        intent.putExtra(INTENT_CHARACTER_DATA, data);
        return intent;
    }
}