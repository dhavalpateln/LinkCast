package com.dhavalpateln.linkcast;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import java.util.Set;

public class MALReceiverActivity extends AppCompatActivity {

    String TAG = "MAL_RECEIVER";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_m_a_l_receiver);
        Intent malIntent = getIntent();
        ClipData url = malIntent.getClipData();
        Set<String> keys = malIntent.getExtras().keySet();
        for(String key: malIntent.getExtras().keySet()) {
            Log.d(TAG, "onCreate: " + malIntent.getExtras().get(key));
        }
        int i = 0;
    }
}