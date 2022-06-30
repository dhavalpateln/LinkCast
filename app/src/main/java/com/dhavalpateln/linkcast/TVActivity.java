package com.dhavalpateln.linkcast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.dhavalpateln.linkcast.database.FirebaseDBHelper;
import com.dhavalpateln.linkcast.database.TvActionData;
import com.dhavalpateln.linkcast.database.VideoURLData;
import com.dhavalpateln.linkcast.dialogs.ConfirmationDialog;
import com.dhavalpateln.linkcast.exoplayer.ExoPlayerActivity;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class TVActivity extends AppCompatActivity {

    private boolean firstTime = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tv);

        FirebaseDBHelper.getUserTvPlay().addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(firstTime) {
                    firstTime = false;
                    return;
                }
                TvActionData data = snapshot.getValue(TvActionData.class);
                Intent intent = ExoPlayerActivity.prepareIntent(getApplicationContext(), data.getId(), data.getVideoData(), data.getEpisodeNum());
                startActivity(intent);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    public void logOut(View view) {

        ConfirmationDialog dialog = new ConfirmationDialog("Log out?", () -> AuthUI.getInstance()
                .signOut(getApplicationContext())
                .addOnCompleteListener(task -> {
                    Intent intent = new Intent(TVActivity.this, LauncherActivity.class);
                    startActivity(intent);
                    TVActivity.this.finish();
                }));
        dialog.show(getSupportFragmentManager(), "logout");


    }
}