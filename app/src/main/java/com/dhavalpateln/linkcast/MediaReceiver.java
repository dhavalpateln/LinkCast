package com.dhavalpateln.linkcast;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;


import com.dhavalpateln.linkcast.database.FirebaseDBHelper;
import com.dhavalpateln.linkcast.exoplayer.ExoPlayerActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;

public class MediaReceiver extends AppCompatActivity {

    FirebaseMessaging fm;
    FirebaseDatabase database;

    public static void insertData(String type, String title, String url) {
        insertData(type, title, url, null);
    }

    public static void insertData(String type, String title, String url, Map<String, String> data) {
        DatabaseReference userLinkRef = FirebaseDBHelper.getUserLinkRef();
        String time = getCurrentTime();
        Map<String, Object> update = new HashMap<>();
        update.put(time + "/type", type);
        update.put(time + "/title", title);
        update.put(time + "/url", url);

        if(data != null) {
            for(String key: data.keySet()) {
                update.put(time + "/data/" + key, data.get(key));
            }
        }

        userLinkRef.updateChildren(update);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.linkeceiver_layout);
        Intent linkIntent = getIntent();

        if(!linkIntent.hasExtra("title")) {
            Toast.makeText(getApplicationContext(), "Not Supported Yet!", Toast.LENGTH_LONG);
            /*Intent playerIntent = new Intent(MediaReceiver.this, ExoPlayerActivity.class);
            playerIntent.putExtra(ExoPlayerActivity.MEDIA_URL, Uri.parse(linkIntent.getData().toString()));
            playerIntent.putExtra(ExoPlayerActivity.FILE_TYPE, ExoPlayerActivity.FileTypes.LOCAL);
            startActivity(playerIntent);*/
        }
        else {
            Intent receivedIntent = getIntent();
            insertData(
                    "video",
                    receivedIntent.getExtras().getString("title"),
                    receivedIntent.getData().toString()
            );


            if(linkIntent.hasExtra("intentSource")) {
                if(linkIntent.getStringExtra("intentSource").equals("anime_web_explorer")) {
                    Intent mainactivity = new Intent(this, MainActivity.class);
                    startActivity(mainactivity);
                }
            }
        }






        finish();
    }

    public static String getCurrentTime() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        String formattedDate = df.format(c.getTime());
        return formattedDate;
    }
}
