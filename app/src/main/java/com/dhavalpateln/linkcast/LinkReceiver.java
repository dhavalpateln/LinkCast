package com.dhavalpateln.linkcast;

import android.content.Intent;
import android.os.Bundle;


import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;

public class LinkReceiver extends AppCompatActivity {

    FirebaseMessaging fm;
    FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.linkeceiver_layout);
        Intent linkIntent = getIntent();
        String url = linkIntent.getData().toString();
        String title = linkIntent.getExtras().getString("title");
        database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("title");
        myRef.setValue(title);
        myRef = database.getReference("url");
        myRef.setValue(url);

        myRef = database.getReference();
        String id = getCurrentTime();
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/link/" + id + "/title", title);
        childUpdates.put("/link/" + id + "/url", url);
        myRef.updateChildren(childUpdates);
        /*myRef.child("link").child(id).child("title").setValue(title);
        myRef.child("link").child(id).child("url").setValue(url);*/

        finish();
    }

    public String getCurrentTime() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        String formattedDate = df.format(c.getTime());
        return formattedDate;
    }
}
