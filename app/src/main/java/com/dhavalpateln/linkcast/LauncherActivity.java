package com.dhavalpateln.linkcast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.dhavalpateln.linkcast.database.FirebaseDBHelper;
import com.dhavalpateln.linkcast.database.ValueCallback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

public class LauncherActivity extends AppCompatActivity {

    private final String TAG = "LAUNCHER_ACTIVITY";
    private final String APP_VERSION = "v1.0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(TAG, "Started Activity");

        FirebaseMessaging.getInstance().subscribeToTopic("update").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    Log.i(TAG, "Subscribed to Update Notification");
                }
                else {
                    Log.i(TAG, "Subscribing to Update Notification Failed");
                }
            }
        });

        FirebaseDBHelper.getValue(FirebaseDBHelper.getAppVersionRef(), new ValueCallback() {
            @Override
            public void onValueObtained(Object o) {
                String version = o.toString();
                if(!version.equals(APP_VERSION)) {
                    Intent updateIntent = new Intent(LauncherActivity.this, UpdateActivity.class);
                    startActivity(updateIntent);
                }
                else {
                    Intent mainActivity = new Intent(LauncherActivity.this, MainActivity.class);
                    startActivity(mainActivity);
                }
                finish();
            }
        });



    }
}
