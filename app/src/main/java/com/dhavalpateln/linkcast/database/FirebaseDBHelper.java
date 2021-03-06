package com.dhavalpateln.linkcast.database;


import android.provider.ContactsContract;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;


public class FirebaseDBHelper {
    public static DatabaseReference getAppVersionRef() {
        return FirebaseDB.getInstance().getReference("app").child("version");
    }
    public static DatabaseReference getAppAPKLinkRef() {
        return FirebaseDB.getInstance().getReference("app").child("link");
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



    public static void removeLink(String id) {
        getUserLinkRef().child(id).setValue(null);
    }
    public static void removeAnimeLink(String id) {
        getUserAnimeWebExplorerLinkRef().child(id).setValue(null);
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
