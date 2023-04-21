package com.dhavalpateln.linkcast.worker;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.concurrent.futures.CallbackToFutureAdapter;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;

import com.dhavalpateln.linkcast.database.AnimeLinkData;
import com.dhavalpateln.linkcast.database.FirebaseDBHelper;
import com.dhavalpateln.linkcast.database.room.LinkCastRoomRepository;
import com.dhavalpateln.linkcast.database.room.animelinkcache.LinkData;
import com.dhavalpateln.linkcast.worker.tasks.LinkUpdaterRoomTask;
import com.dhavalpateln.linkcast.worker.tasks.LinkUpdaterTask;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class LinkUpdaterWorker extends ListenableWorker {

    public LinkUpdaterWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public ListenableFuture<Result> startWork() {
        return CallbackToFutureAdapter.getFuture(completer -> {
            LinkCastWorkerCallback callback = new LinkCastWorkerCallback() {
                @Override
                public void onFailure() {

                }

                @Override
                public void onComplete() {
                    completer.set(Result.success());
                }
            };

            Executors.newSingleThreadExecutor().execute(() -> {
                new LinkUpdaterRoomTask(getApplicationContext(), callback).run();
            });

            /*FirebaseDBHelper.getUserAnimeWebExplorerLinkRef().addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    List<AnimeLinkData> links = new ArrayList<>();
                    for(DataSnapshot childSnapshot: snapshot.getChildren()) {
                        AnimeLinkData animeLinkData = childSnapshot.getValue(AnimeLinkData.class);
                        animeLinkData.setId(childSnapshot.getKey());
                        links.add(animeLinkData);
                    }
                    Executors.newSingleThreadExecutor().execute(new LinkUpdaterTask(getApplicationContext(), links, callback));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });*/
            return callback;
        });
    }
}
