package com.dhavalpateln.linkcast.explorer.tasks;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public abstract class RunnableTask implements Runnable {
    private Handler uiHandler;
    public RunnableTask() {
        uiHandler = new Handler(Looper.getMainLooper());
    }

    protected Handler getUIHandler() {
        return this.uiHandler;
    }
}
