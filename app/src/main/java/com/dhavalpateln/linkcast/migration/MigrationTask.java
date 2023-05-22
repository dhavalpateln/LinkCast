package com.dhavalpateln.linkcast.migration;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.dhavalpateln.linkcast.database.room.LinkCastRoomRepository;

public abstract class MigrationTask {

    private Handler uiHandler;
    private MigrationListener listener;
    private int version;

    public MigrationTask(int version, MigrationListener listener) {
        this.listener = listener;
        this.version = version;
        uiHandler = new Handler(Looper.getMainLooper());
    }

    public abstract void execute();

    protected void updateProgress(int progress) {
        this.uiHandler.post(() -> this.listener.onMigrationProgressChange(progress));
    }

    protected void notifySuccess() {
        this.uiHandler.post(() -> this.listener.onMigrationSuccess(version));
    }
}
