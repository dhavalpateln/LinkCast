package com.dhavalpateln.linkcast.database;


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

    public static DatabaseReference getUserDataRef() {
        return FirebaseDB.getInstance().getReference("userdata").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
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
