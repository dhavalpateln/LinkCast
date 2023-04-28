package com.dhavalpateln.linkcast.database;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.dhavalpateln.linkcast.database.room.LinkCastRoomRepository;
import com.dhavalpateln.linkcast.database.room.animelinkcache.LinkData;
import com.dhavalpateln.linkcast.database.room.animelinkcache.LinkWithAllData;
import com.dhavalpateln.linkcast.utils.Utils;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;

public class LinkDataViewModel extends AndroidViewModel {

    private LinkCastRoomRepository roomRepo;
    private boolean animeSyncStarted = false;
    private boolean mangaSyncStarted = false;
    private String TAG = "LinkDataViewModel";
    private Set<String> fetchedIDs;
    private int checkedFdb = 0;

    public LinkDataViewModel(@NonNull Application application) {
        super(application);
        roomRepo = new LinkCastRoomRepository(application);
        this.fetchedIDs = new HashSet<>();
    }

    public LiveData<List<LinkWithAllData>> getAnimeLinks() {
        if(!animeSyncStarted) {
            startSync(FirebaseDBHelper.getUserAnimeWebExplorerLinkRef());
            Executors.newSingleThreadExecutor().execute(() -> startDeleteSync(
                    FirebaseDBHelper.getUserAnimeWebExplorerLinkRef(),
                    this.roomRepo.getAllAnimeIDs()
            ));
            animeSyncStarted = true;
        }
        return this.roomRepo.getAnimeLinks();
    }

    public LiveData<List<LinkWithAllData>> getMangaLinks() {
        if(!mangaSyncStarted) {
            startSync(FirebaseDBHelper.getUserMangaWebExplorerLinkRef());
            Executors.newSingleThreadExecutor().execute(() -> startDeleteSync(
                    FirebaseDBHelper.getUserMangaWebExplorerLinkRef(),
                    this.roomRepo.getAllMangaIDs()
            ));
            mangaSyncStarted = true;
        }
        return this.roomRepo.getMangaLinks();
    }

    private void startDeleteSync(DatabaseReference dbRef, List<String> ids) {
        try {
            Thread.sleep(750);
        } catch (InterruptedException e) {

        }
        for(String linkID: ids) {
            if(fetchedIDs.contains(linkID)) continue;
            checkedFdb++;
            dbRef.child(linkID).child("title").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Log.d(TAG, "check count = " + checkedFdb);
                    if(!snapshot.exists()) {
                        roomRepo.deleteLinkData(linkID);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }
    }

    private void startSync(DatabaseReference dbRef) {
        dbRef.addChildEventListener(new FirebaseSyncListener());
    }

    private class FirebaseSyncListener implements ChildEventListener {


        public FirebaseSyncListener() {

        }

        @Override
        public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            AnimeLinkData animeLinkData = snapshot.getValue(AnimeLinkData.class);
            animeLinkData.setId(snapshot.getKey());
            roomRepo.insert(LinkData.from(animeLinkData));
            fetchedIDs.add(snapshot.getKey());
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            AnimeLinkData animeLinkData = snapshot.getValue(AnimeLinkData.class);
            animeLinkData.setId(snapshot.getKey());
            roomRepo.insert(LinkData.from(animeLinkData));
            fetchedIDs.add(snapshot.getKey());
        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot snapshot) {
            roomRepo.deleteLinkData(snapshot.getKey());
            fetchedIDs.add(snapshot.getKey());
        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    }
}
