package com.dhavalpateln.linkcast;

import static com.dhavalpateln.linkcast.utils.Utils.isInternetConnected;
import static com.dhavalpateln.linkcast.utils.Utils.isNetworkAvailable;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.app.UiModeManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.dhavalpateln.linkcast.data.AppInfo;
import com.dhavalpateln.linkcast.database.FirebaseDB;
import com.dhavalpateln.linkcast.database.FirebaseDBHelper;
import com.dhavalpateln.linkcast.database.ValueCallback;
import com.dhavalpateln.linkcast.exoplayer.ExoPlayerActivity;
import com.dhavalpateln.linkcast.migration.MigrationActivity;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Arrays;
import java.util.List;

public class LauncherActivity extends AppCompatActivity {

    private final String TAG = "LAUNCHER_ACTIVITY";
    private final String APP_VERSION = "v5.0.4";

    private static final int RC_SIGN_IN = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(TAG, "Started Activity");

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

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

        FirebaseDBHelper.getValue(FirebaseDBHelper.getAppDataRef(), dataSnapshot -> {
            AppInfo appinfo = AppInfo.getInstance();
            appinfo.updateData(dataSnapshot);

            if(!appinfo.getApkVersion().equals(APP_VERSION)) {
                Intent updateIntent = new Intent(LauncherActivity.this, UpdateActivity.class);
                startActivity(updateIntent);
            }
            else {
                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                if(firebaseUser == null) {
                    List<AuthUI.IdpConfig> providers = Arrays.asList(
                            new AuthUI.IdpConfig.GoogleBuilder().build());

                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setAvailableProviders(providers)
                                    .build(),
                            RC_SIGN_IN);
                }
                else {
                    updateUserMetaData();
                    startMainActivity();
                    finish();

                    /*Intent intent = new Intent(getApplicationContext(), ExoPlayerActivity.class);
                    intent.putExtra("url", "https://rnrjz.vizcloud.digital/simple/EqPFI_8QBAro1HhYl67rC8EurFwDvr2zCw57rqk+wYMnU94US2El/br/list.m3u8#.mp4");
                    intent.putExtra("Referer", "https://vizcloud.digital/embed/2EYDX1QZRJ1Q");
                    intent.putExtra("saveProgress", true);
                    intent.putExtra("id", "2022-04-25-11-30-06");
                    intent.putExtra(ExoPlayerActivity.EPISODE_NUM, "11");
                    startActivity(intent);*/
                }
            }
        });

        FirebaseDBHelper.getValue(FirebaseDBHelper.getAppVersionRef(), new ValueCallback() {
            @Override
            public void onValueObtained(DataSnapshot dataSnapshot) {


            }
        });

        if(!isNetworkAvailable(getApplicationContext())) {
            Toast.makeText(getApplicationContext(), "No connection", Toast.LENGTH_LONG).show();
            startMainActivity();
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                DatabaseReference userListRef = FirebaseDB.getInstance().getReference("userlist");
                userListRef.child(user.getUid()).setValue(user.getEmail());

                updateUserMetaData();

                //Intent start = new Intent(this, AnimeWebExplorer.class);
                startMainActivity();
                LauncherActivity.this.finish();
                // ...
            } else {
                LauncherActivity.this.finish();
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
            }
        }
    }

    private void startMainActivity() {
        Intent start;
        UiModeManager uiModeManager = (UiModeManager) getSystemService(UI_MODE_SERVICE);
        if (uiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_TELEVISION) {
            start = new Intent(LauncherActivity.this, TVActivity.class);
        } else {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            if(prefs.getInt(MigrationActivity.PREF_MIGRATION_VERSION_KEY, 0) < MigrationActivity.VERSION) {
                start = new Intent(LauncherActivity.this, MigrationActivity.class);
            }
            else {
                start = new Intent(LauncherActivity.this, MainActivity.class);
            }
        }
        startActivity(start);
    }

    public void updateUserMetaData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference ref = FirebaseDBHelper.getUserDataRef();
        ref.child("displayName").setValue(user.getDisplayName());
        ref.child("email").setValue(user.getEmail());
        ref.child("phone").setValue(user.getPhoneNumber());
        ref.child("photoURI").setValue(user.getPhotoUrl().toString());
    }
}
