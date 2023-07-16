package com.dhavalpateln.linkcast.explorer.listeners;

import com.dhavalpateln.linkcast.database.VideoURLData;

import java.util.List;

public interface VideoServerListener {
    void onVideoExtracted(VideoURLData videoURLData);
}
