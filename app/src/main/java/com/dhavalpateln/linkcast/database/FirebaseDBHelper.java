package com.dhavalpateln.linkcast.database;


import com.google.firebase.database.DatabaseReference;


public class FirebaseDBHelper {
    public static DatabaseReference getAppVersionRef() {
        return FirebaseDB.getInstance().getReference("app").child("version");
    }
}
