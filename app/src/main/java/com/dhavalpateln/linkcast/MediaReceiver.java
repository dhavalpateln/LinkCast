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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.linkeceiver_layout);
        Intent linkIntent = getIntent();
        String url = linkIntent.getData().toString();
        String title = linkIntent.getExtras().getString("title");

        /** Old Logic - To be Deprecated **/
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


        /** New Logic **/
        Intent receivedIntent = getIntent();
        DatabaseReference userLinkRef = FirebaseDBHelper.getUserLinkRef();
        String time = getCurrentTime();
        Map<String, Object> update = new HashMap<>();
        update.put(time + "/type", "video");
        update.put(time + "/title", receivedIntent.getExtras().getString("title"));
        update.put(time + "/url", receivedIntent.getData().toString());
        userLinkRef.updateChildren(update);

        finish();
    }

    public String getCurrentTime() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        String formattedDate = df.format(c.getTime());
        return formattedDate;
    }
}
