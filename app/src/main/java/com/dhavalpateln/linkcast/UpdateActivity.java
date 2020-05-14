package com.dhavalpateln.linkcast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.dhavalpateln.linkcast.database.FirebaseDBHelper;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class UpdateActivity extends AppCompatActivity {

    public final String TAG = "UPDATE_ACTIVITY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);


    }

    public void download(View view) {
        DatabaseReference linkRef = FirebaseDBHelper.getAppAPKLinkRef();
        linkRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String link = dataSnapshot.getValue().toString();
                Log.i(TAG, "Link obtained : " + link);
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                startActivity(intent);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
