package com.dhavalpateln.linkcast.database;

import com.google.firebase.database.DataSnapshot;

public interface ValueCallback {
    void onValueObtained(DataSnapshot dataSnapshot);
}
