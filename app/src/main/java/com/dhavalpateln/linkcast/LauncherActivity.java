package com.dhavalpateln.linkcast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.dhavalpateln.linkcast.database.FirebaseDB;
import com.dhavalpateln.linkcast.database.FirebaseDBHelper;
import com.dhavalpateln.linkcast.database.ValueCallback;
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
    private final String APP_VERSION = "v3.1.1";

    private static final int RC_SIGN_IN = 9001;

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
            public void onValueObtained(DataSnapshot dataSnapshot) {
                String version = dataSnapshot.getValue().toString();
                if(!version.equals(APP_VERSION)) {
                    Intent updateIntent = new Intent(LauncherActivity.this, UpdateActivity.class);
                    startActivity(updateIntent);
                }
                else {


                    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                    if(firebaseUser == null) {
                        List<AuthUI.IdpConfig> providers = Arrays.asList(
                                new AuthUI.IdpConfig.EmailBuilder().build(),
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
                        //Intent mainActivity = new Intent(LauncherActivity.this, AnimeWebExplorer.class);
                        Intent mainActivity = new Intent(LauncherActivity.this, MainActivity.class);
                        startActivity(mainActivity);
                        finish();
                    }
                }

            }
        });
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
                Intent start = new Intent(this, MainActivity.class);
                startActivity(start);
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

    public void updateUserMetaData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference ref = FirebaseDBHelper.getUserDataRef();
        ref.child("displayName").setValue(user.getDisplayName());
        ref.child("email").setValue(user.getEmail());
        ref.child("phone").setValue(user.getPhoneNumber());
        ref.child("photoURI").setValue(user.getPhotoUrl().toString());
    }
}
