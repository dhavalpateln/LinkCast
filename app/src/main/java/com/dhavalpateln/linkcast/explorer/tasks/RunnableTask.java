package com.dhavalpateln.linkcast.explorer.tasks;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public abstract class RunnableTask implements Runnable {
    private String taskName;
    private TaskCompleteListener listener;
    private Handler uiHandler;

    public RunnableTask(String name, TaskCompleteListener listener) {
        this(name, listener, true);
    }

    public RunnableTask(String name, TaskCompleteListener listener, boolean initiateUIHandler) {
        this.taskName = name;
        this.listener = listener;
        if(initiateUIHandler) {
            uiHandler = new Handler(Looper.getMainLooper());
        }
    }

    public RunnableTask(String name) {
        this.taskName = name;
        uiHandler = new Handler(Looper.getMainLooper());
    }

    public String getTaskName() {
        return this.taskName;
    }

    public abstract void runTask();

    @Override
    public void run() {
        this.runTask();
        this.notifyComplete();
    }

    protected Handler getUIHandler() {
        return this.uiHandler;
    }

    protected void notifyComplete() {
        if(getUIHandler() != null) {
            getUIHandler().post(() -> listener.onTaskCompleted(getTaskName()));
        }
        else {
            listener.onTaskCompleted(getTaskName());
        }
    }
}
