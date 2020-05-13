package com.dhavalpateln.linkcast.database;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FirebaseDB {
    private static FirebaseDB firebaseDB = null;

    public static FirebaseDB getInstance() {
        if(firebaseDB == null) firebaseDB = new FirebaseDB();
        return firebaseDB;
    }

    public DatabaseReference getReference(String path) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference(path);
        return myRef;
    }

    public void addValueEventListener(String path, ValueEventListener listener) {
        DatabaseReference ref = getReference(path);
        ref.addValueEventListener(listener);
    }

    public void addChildEventListener(String path, ChildEventListener listener) {
        DatabaseReference ref = getReference(path);
        ref.addChildEventListener(listener);
    }

    public void removeValueEventListener(String path, ValueEventListener listener) {
        DatabaseReference ref = getReference(path);
        ref.removeEventListener(listener);
    }

    public void removeChildEventListener(String path, ChildEventListener listener) {
        DatabaseReference ref = getReference(path);
        ref.removeEventListener(listener);
    }

    public void getValue(String path, ValueEventListener listener) {
        DatabaseReference ref = getReference(path);
        ref.addListenerForSingleValueEvent(listener);
    }

}
