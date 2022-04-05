package com.dhavalpateln.linkcast.ui.catalog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.dhavalpateln.linkcast.data.StoredAnimeLinkData;
import com.dhavalpateln.linkcast.database.AnimeLinkData;
import com.dhavalpateln.linkcast.database.FirebaseDBHelper;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SharedAnimeLinkDataViewModel extends ViewModel {

    private Map<String, MutableLiveData<Map<String, AnimeLinkData>>> livaDataMap;

    private MutableLiveData<Map<String, AnimeLinkData>> data;

    public LiveData<Map<String, AnimeLinkData>> getData() {
        if(data == null) {
            data = new MutableLiveData<>();
            loadData();
        }
        return data;
    }

    public LiveData<Map<String, AnimeLinkData>> getData(String catalogType) {
        if(livaDataMap == null) {
            loadData();
        }
        if(livaDataMap.containsKey(catalogType))    return livaDataMap.get(catalogType);
        return null;
    }

    private void loadData() {
        /*for(String catalogType: CatalogFragment.CATALOG_TYPE) {
            MutableLiveData<Map<String, AnimeLinkData>> liveData = new MutableLiveData<>();
            livaDataMap.put(catalogType, liveData);
        }*/
        data.setValue(new HashMap<>());
        FirebaseDBHelper.getUserAnimeWebExplorerLinkRef().addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Map<String, AnimeLinkData> map = data.getValue();
                AnimeLinkData animeLinkData = snapshot.getValue(AnimeLinkData.class);

                map.put(snapshot.getKey(), animeLinkData);
                data.setValue(map);
                StoredAnimeLinkData.getInstance().updateCache(map);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Map<String, AnimeLinkData> map = data.getValue();
                map.put(snapshot.getKey(), snapshot.getValue(AnimeLinkData.class));
                data.setValue(map);
                StoredAnimeLinkData.getInstance().updateCache(map);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                Map<String, AnimeLinkData> map = data.getValue();
                map.remove(snapshot.getKey());
                data.setValue(map);
                StoredAnimeLinkData.getInstance().updateCache(map);
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
