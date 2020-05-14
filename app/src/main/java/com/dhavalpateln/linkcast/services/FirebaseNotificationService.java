package com.dhavalpateln.linkcast.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class FirebaseNotificationService extends FirebaseMessagingService {

    private final String TAG = "FIREBASE_NOTIFICATION";

    public FirebaseNotificationService() {
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.i(TAG, "New message received");
    }
}
