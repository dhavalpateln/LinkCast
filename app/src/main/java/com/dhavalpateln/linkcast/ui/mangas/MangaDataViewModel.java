package com.dhavalpateln.linkcast.ui.mangas;

import android.app.Application;

import com.dhavalpateln.linkcast.data.StoredAnimeLinkData;
import com.dhavalpateln.linkcast.database.AnimeLinkData;
import com.dhavalpateln.linkcast.database.FirebaseDBHelper;
import com.dhavalpateln.linkcast.database.room.LinkCastRoomRepository;
import com.dhavalpateln.linkcast.database.room.animelinkcache.LinkData;
import com.dhavalpateln.linkcast.database.room.animelinkcache.LinkWithAllData;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MangaDataViewModel extends AndroidViewModel {

    private MutableLiveData<Map<String, AnimeLinkData>> data;
    private LinkCastRoomRepository roomRepo;

    public MangaDataViewModel(@NonNull Application application) {
        super(application);
        roomRepo = new LinkCastRoomRepository(application);
    }

    public LiveData<Map<String, AnimeLinkData>> getData() {
        if(data == null) {
            data = new MutableLiveData<>();
            loadData();
        }
        return data;
    }

    public LiveData<List<LinkWithAllData>> getLinkData() {
        if(data == null) {
            data = new MutableLiveData<>();
            loadData();
        }
        return this.roomRepo.getMangaLinks();
    }

    private void loadData() {
        data.setValue(new HashMap<>());
        FirebaseDBHelper.getUserMangaWebExplorerLinkRef().addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Map<String, AnimeLinkData> map = data.getValue();
                AnimeLinkData animeLinkData = snapshot.getValue(AnimeLinkData.class);
                animeLinkData.setId(snapshot.getKey());
                map.put(snapshot.getKey(), animeLinkData);
                data.setValue(map);
                StoredAnimeLinkData.getInstance().updateMangaCache(map);
                roomRepo.insert(LinkData.from(animeLinkData));
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Map<String, AnimeLinkData> map = data.getValue();
                AnimeLinkData animeLinkData = snapshot.getValue(AnimeLinkData.class);
                animeLinkData.setId(snapshot.getKey());
                map.put(snapshot.getKey(), animeLinkData);
                data.setValue(map);
                StoredAnimeLinkData.getInstance().updateMangaCache(map);
                roomRepo.insert(LinkData.from(animeLinkData));
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                Map<String, AnimeLinkData> map = data.getValue();
                map.remove(snapshot.getKey());
                data.setValue(map);
                StoredAnimeLinkData.getInstance().updateMangaCache(map);
                roomRepo.deleteLinkData(snapshot.getKey());
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
