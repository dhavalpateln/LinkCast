package com.dhavalpateln.linkcast;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class LauncherActivity extends AppCompatActivity {

    private final String TAG = "LAUNCHER_ACTIVITY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(TAG, "Started Activity");

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
