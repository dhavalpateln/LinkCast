package com.dhavalpateln.linkcast.database;


import android.provider.ContactsContract;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;


public class FirebaseDBHelper {

    public static DatabaseReference getAppDataRef() {
        return FirebaseDB.getInstance().getReference("app");
    }
    public static DatabaseReference getAppVersionRef() {
        return getAppDataRef().child("version");
    }
    public static DatabaseReference getAppAPKLinkRef() {
        return getAppDataRef().child("link");
    }
    public static DatabaseReference getAppWebLinkRef() {
        return getAppDataRef().child("web");
    }

    private static DatabaseReference getAppMetricsLinkRef() {
        return FirebaseDB.getInstance().getReference("metrics");
    }
    public static DatabaseReference getAppMetricsLastAccessedLinkRef() {
        return getAppMetricsLinkRef().child("last_accessed").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
    }

    public static DatabaseReference getFeedbackRef() {
        return FirebaseDB.getInstance().getReference("feedbacks");
    }

    public static DatabaseReference getUserDataRef() {
        return FirebaseDB.getInstance().getReference("userdata").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
    }
    public static DatabaseReference getUserLinkRef() {
        return getUserDataRef().child("links");
    }
    public static DatabaseReference getUserAnimeWebExplorerLinkRef() {
        return getUserDataRef().child("animewebexplorerlinks");
    }
    public static DatabaseReference getUserAnimeWebExplorerLinkRef(String id) {
        return getUserDataRef().child("animewebexplorerlinks").child(id);
    }

    public static DatabaseReference getUserMangaWebExplorerLinkRef() {
        return getUserDataRef().child("mangawebexplorerlinks");
    }
    public static DatabaseReference getUserMangaWebExplorerLinkRef(String id) {
        return getUserDataRef().child("mangawebexplorerlinks").child(id);
    }
    public static DatabaseReference getUserDownloadQueue() {
        return getUserDataRef().child("downloadqueue");
    }
    public static DatabaseReference getUserWebDownloadQueueTypes() {
        return getUserDownloadQueue().child("types");
    }
    public static DatabaseReference getUserRemoteDownloadQueue() {
        return getUserDownloadQueue().child("remote");
    }
    public static DatabaseReference getRemoteDownloadQueue() {
        return FirebaseDB.getInstance().getReference("downloadqueue").child("remote");
    }
    public static DatabaseReference getUserRemoteDownloadCode() {
        return getUserDataRef().child("remotecode");
    }
    public static DatabaseReference getUserPiMoteDownloadQueue() {
        return getUserDownloadQueue().child("pimote");
    }
    public static DatabaseReference getAnimeStatusRef() {
        return FirebaseDB.getInstance().getReference("anime_status");
    }


    public static void removeLink(String id) {
        getUserLinkRef().child(id).setValue(null);
    }
    public static void removeAnimeLink(String id) {
        getUserAnimeWebExplorerLinkRef().child(id).setValue(null);
    }

    public static void removeMangaLink(String id) {
        getUserMangaWebExplorerLinkRef().child(id).setValue(null);
    }


    public static void getValue(DatabaseReference databaseReference, final ValueCallback valueCallback) {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                valueCallback.onValueObtained(dataSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
