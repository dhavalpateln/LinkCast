package com.dhavalpateln.linkcast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.dhavalpateln.linkcast.database.FirebaseDBHelper;
import com.dhavalpateln.linkcast.database.ValueCallback;
import com.dhavalpateln.linkcast.dialogs.LinkDownloadManagerDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.io.File;

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
            public void onValueObtained(DataSnapshot dataSnapshot) {
                String version = dataSnapshot.getValue().toString();
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

                LinkDownloadManagerDialog downloadManager = new LinkDownloadManagerDialog(link, "LinkCast." + versionTextView.getText().toString() + ".apk", new LinkDownloadManagerDialog.LinkDownloadListener() {
                    @Override
                    public void onDownloadComplete() {
                        Toast.makeText(getApplicationContext(), "Download Completed", Toast.LENGTH_SHORT).show();
                    }
                });
                downloadManager.show(getSupportFragmentManager(), "Download");

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void copyLink(View view) {
        DatabaseReference linkRef = FirebaseDBHelper.getAppAPKLinkRef();
        linkRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String link = dataSnapshot.getValue().toString();
                Log.i(TAG, "Link obtained : " + link);
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("apk link", link);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(getApplicationContext(), "Copied", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}
