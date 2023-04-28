package com.dhavalpateln.linkcast.migration;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

public abstract class MigrationTask {

    private Handler uiHandler;
    private MigrationListener listener;

    public MigrationTask(MigrationListener listener) {
        this.listener = listener;
        uiHandler = new Handler(Looper.getMainLooper());
    }

    public abstract void execute();

    protected void updateProgress(int progress) {
        this.uiHandler.post(() -> this.listener.onMigrationProgressChange(progress));
    }
}
