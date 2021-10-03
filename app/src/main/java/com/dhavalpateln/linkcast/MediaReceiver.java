package com.dhavalpateln.linkcast;

import android.content.Intent;
import android.os.Bundle;


import com.dhavalpateln.linkcast.database.FirebaseDBHelper;
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
        String url = linkIntent.getData().toString();
        String title = linkIntent.getExtras().getString("title");

        /** Old Logic - To be Deprecated **/
        /*database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("title");
        myRef.setValue(title);
        myRef = database.getReference("url");
        myRef.setValue(url);

        myRef = database.getReference();
        String id = getCurrentTime();
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/link/" + id + "/title", title);
        childUpdates.put("/link/" + id + "/url", url);
        myRef.updateChildren(childUpdates);*/
        /*myRef.child("link").child(id).child("title").setValue(title);
        myRef.child("link").child(id).child("url").setValue(url);*/


        /** New Logic **/
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

        finish();
    }

    public static String getCurrentTime() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        String formattedDate = df.format(c.getTime());
        return formattedDate;
    }
}
