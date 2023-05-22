package com.dhavalpateln.linkcast.migration;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.dhavalpateln.linkcast.database.AnimeLinkData;
import com.dhavalpateln.linkcast.database.FirebaseDBHelper;
import com.dhavalpateln.linkcast.database.room.LinkCastRoomRepository;
import com.dhavalpateln.linkcast.database.room.animelinkcache.LinkData;
import com.dhavalpateln.linkcast.database.room.animelinkcache.LinkWithAllData;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Migration_0_1 extends MigrationTask {
    private LinkCastRoomRepository roomRepo;
    private List<LinkWithAllData> linkDataList;
    private int fireDBRefFetched;
    private String TAG = "MIGRATION_0_1";
    public Migration_0_1(Context context, MigrationListener listener) {
        super(1, listener);
        this.roomRepo = new LinkCastRoomRepository(context);
        this.linkDataList = new ArrayList<>();
        this.fireDBRefFetched = 0;
    }

    @Override
    public void execute() {
        fetchRefData(FirebaseDBHelper.getUserAnimeWebExplorerLinkRef());
        fetchRefData(FirebaseDBHelper.getUserMangaWebExplorerLinkRef());
    }

    private void fetchRefData(DatabaseReference ref) {
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot childSnapshot: snapshot.getChildren()) {
                    AnimeLinkData animeLinkData = childSnapshot.getValue(AnimeLinkData.class);
                    animeLinkData.setId(childSnapshot.getKey());
                    LinkWithAllData linkWithAllData = new LinkWithAllData();
                    linkWithAllData.linkData = LinkData.from(animeLinkData);
                    linkDataList.add(linkWithAllData);
                }
                onRefFetched();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void onRefFetched() {
        fireDBRefFetched++;
        if(fireDBRefFetched == 2) {
            startMigration();
        }
    }

    private void startMigration() {
        updateProgress(0);
        for(int i = 0; i < this.linkDataList.size(); i++) {
            updateProgress((i + 1) * 100 / linkDataList.size());
            LinkWithAllData link = linkDataList.get(i);

            try {
                if (link.linkData.getTitle() == null) {
                    if (link.isAnime()) {
                        FirebaseDBHelper.getUserAnimeWebExplorerLinkRef(link.getId()).setValue(null);
                    } else {
                        FirebaseDBHelper.getUserMangaWebExplorerLinkRef(link.getId()).setValue(null);
                    }
                    continue;
                }

                roomRepo.insert(link.linkData);
            } catch (Exception e) {
                Log.d(TAG, "Error migrating " + link.getId());
            }
        }
        notifySuccess();
    }

}
