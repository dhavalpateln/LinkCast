package com.dhavalpateln.linkcast.database;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.dhavalpateln.linkcast.database.room.LinkCastRoomRepository;
import com.dhavalpateln.linkcast.database.room.animelinkcache.LinkData;
import com.dhavalpateln.linkcast.database.room.animelinkcache.LinkWithAllData;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.List;

public class LinkDataViewModel extends AndroidViewModel {

    private LinkCastRoomRepository roomRepo;
    private boolean animeSyncStarted = false;
    private boolean mangaSyncStarted = false;
    private String TAG = "LinkDataViewModel";

    public LinkDataViewModel(@NonNull Application application) {
        super(application);
        roomRepo = new LinkCastRoomRepository(application);
    }

    public LiveData<List<LinkWithAllData>> getAnimeLinks() {
        if(!animeSyncStarted) {
            startSync(FirebaseDBHelper.getUserAnimeWebExplorerLinkRef());
            animeSyncStarted = true;
        }
        return this.roomRepo.getAnimeLinks();
    }

    public LiveData<List<LinkWithAllData>> getMangaLinks() {
        if(!mangaSyncStarted) {
            startSync(FirebaseDBHelper.getUserMangaWebExplorerLinkRef());
            mangaSyncStarted = true;
        }
        return this.roomRepo.getMangaLinks();
    }

    private void startSync(DatabaseReference dbRef) {
        dbRef.addChildEventListener(new FirebaseSyncListener());
    }

    private class FirebaseSyncListener implements ChildEventListener {

        @Override
        public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            AnimeLinkData animeLinkData = snapshot.getValue(AnimeLinkData.class);
            animeLinkData.setId(snapshot.getKey());
            roomRepo.insert(LinkData.from(animeLinkData));
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            AnimeLinkData animeLinkData = snapshot.getValue(AnimeLinkData.class);
            animeLinkData.setId(snapshot.getKey());
            roomRepo.insert(LinkData.from(animeLinkData));
        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot snapshot) {
            roomRepo.deleteLinkData(snapshot.getKey());
        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    }
}
