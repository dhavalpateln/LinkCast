package com.dhavalpateln.linkcast.worker;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.concurrent.futures.CallbackToFutureAdapter;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;

import com.dhavalpateln.linkcast.database.room.LinkCastRoomRepository;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.Executors;

public abstract class LinkCastWorker extends ListenableWorker {

    private LinkCastWorkerCallback callback;
    private LinkCastRoomRepository roomRepo;

    /**
     * @param appContext   The application {@link Context}
     * @param workerParams Parameters to setup the internal state of this worker
     */
    public LinkCastWorker(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
    }

    @NonNull
    @Override
    public ListenableFuture<Result> startWork() {
        return CallbackToFutureAdapter.getFuture(completer -> {
            this.callback = new LinkCastWorkerCallback() {
                @Override
                public void onFailure() {}

                @Override
                public void onComplete() {
                    completer.set(Result.success());
                }
            };
            Executors.newSingleThreadExecutor().execute(this::run);
            return callback;
        });
    }

    protected LinkCastWorkerCallback getCallback() {
        return this.callback;
    }

    protected LinkCastRoomRepository getRoomRepo() {
        if(this.roomRepo == null) {
            this.roomRepo = new LinkCastRoomRepository(getApplicationContext());
        }
        return this.roomRepo;
    }

    public abstract void run();
    public abstract int getVersion();


}
