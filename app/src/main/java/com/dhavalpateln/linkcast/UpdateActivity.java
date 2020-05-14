package com.dhavalpateln.linkcast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.dhavalpateln.linkcast.database.FirebaseDBHelper;
import com.dhavalpateln.linkcast.database.ValueCallback;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class UpdateActivity extends AppCompatActivity {

    public final String TAG = "UPDATE_ACTIVITY";
    private TextView versionTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);

        versionTextView = findViewById(R.id.versionTextView);

        FirebaseDBHelper.getValue(FirebaseDBHelper.getAppVersionRef(), new ValueCallback() {
            @Override
            public void onValueObtained(Object o) {
                String version = o.toString();
                versionTextView.setText(version);
            }
        });

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
