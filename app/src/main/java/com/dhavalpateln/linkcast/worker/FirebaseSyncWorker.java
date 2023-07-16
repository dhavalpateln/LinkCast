package com.dhavalpateln.linkcast.worker;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.WorkerParameters;

import com.dhavalpateln.linkcast.database.AnimeLinkData;
import com.dhavalpateln.linkcast.database.FirebaseDBHelper;
import com.dhavalpateln.linkcast.database.room.animelinkcache.LinkData;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;

public class FirebaseSyncWorker extends LinkCastWorker {
    /**
     * @param appContext   The application {@link Context}
     * @param workerParams Parameters to setup the internal state of this worker
     */
    public FirebaseSyncWorker(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
    }

    private void sync(List<AnimeLinkData> animeLinkDataList) {
        Set<String> fbIDs = new HashSet<>();

        for(AnimeLinkData animeLinkData: animeLinkDataList) {
            fbIDs.add(animeLinkData.getId());
        }

        List<LinkData> roomLinkData = getRoomRepo().getAllAnimeLinks();
    }

    @Override
    public void run() {
        FirebaseDBHelper.getUserAnimeWebExplorerLinkRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                    List<AnimeLinkData> links = new ArrayList<>();
                    for(DataSnapshot childSnapshot: snapshot.getChildren()) {
                        AnimeLinkData animeLinkData = childSnapshot.getValue(AnimeLinkData.class);
                        animeLinkData.setId(childSnapshot.getKey());
                        links.add(animeLinkData);
                    }
                    Executors.newSingleThreadExecutor().execute(() -> sync(links));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    @Override
    public int getVersion() {
        return 0;
    }
}
