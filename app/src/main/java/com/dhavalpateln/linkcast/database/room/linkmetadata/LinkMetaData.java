package com.dhavalpateln.linkcast.database.room.linkmetadata;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.dhavalpateln.linkcast.database.AnimeLinkData;

import java.util.Map;

@Entity
public class LinkMetaData {

    @PrimaryKey
    @NonNull
    private String id;
    private Map<String, String> miscData;

    @ColumnInfo(defaultValue = "-2")
    private int lastEpisodeNodesFetchCount;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Map<String, String> getMiscData() {
        return miscData;
    }

    public void setMiscData(Map<String, String> miscData) {
        this.miscData = miscData;
    }

    public int getLastEpisodeNodesFetchCount() {
        return lastEpisodeNodesFetchCount;
    }

    public void setLastEpisodeNodesFetchCount(int lastEpisodeNodesFetchCount) {
        this.lastEpisodeNodesFetchCount = lastEpisodeNodesFetchCount;
    }
}
