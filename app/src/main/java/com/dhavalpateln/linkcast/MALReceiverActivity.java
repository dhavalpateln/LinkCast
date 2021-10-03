package com.dhavalpateln.linkcast;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.dhavalpateln.linkcast.dialogs.SearchDialog;

import java.util.Set;

public class MALReceiverActivity extends AppCompatActivity {

    String TAG = "MAL_RECEIVER";
    private boolean firstCall = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_m_a_l_receiver);
        Intent malIntent = getIntent();
        ClipData url = malIntent.getClipData();
        Set<String> keys = malIntent.getExtras().keySet();

        if(keys.contains("android.intent.extra.SUBJECT")) {
            SearchDialog dialog = new SearchDialog(malIntent.getStringExtra("android.intent.extra.SUBJECT"));
            dialog.setSearchListener(new SearchDialog.SearchButtonClickListener() {
                @Override
                public void onSearchButtonClicked(String searchString, String source, boolean advancedMode) {
                    Intent animeSearchIntent = new Intent(getApplicationContext(), AnimeWebExplorer.class);
                    animeSearchIntent.putExtra("search", searchString);
                    animeSearchIntent.putExtra("source", source);
                    animeSearchIntent.putExtra("animeTitle", searchString);
                    animeSearchIntent.putExtra("advancedMode", advancedMode);
                    startActivity(animeSearchIntent);
                }
            });
            dialog.show(getSupportFragmentManager(), "search");
        }
        for(String key: malIntent.getExtras().keySet()) {
            Log.d(TAG, "onCreate: " + key + " = " + malIntent.getExtras().get(key));
        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!firstCall) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            this.finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        firstCall = false;
    }
}