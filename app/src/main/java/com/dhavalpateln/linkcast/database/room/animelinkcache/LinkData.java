package com.dhavalpateln.linkcast.database.room.animelinkcache;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.dhavalpateln.linkcast.database.AnimeLinkData;
import com.dhavalpateln.linkcast.database.AnimeMALMetaData;

import java.io.Serializable;
import java.util.Map;

@Entity
public class LinkData implements Serializable {

    @PrimaryKey
    @NonNull
    private String id;

    private String title;
    private String url;
    private String type;
    private String malId;
    private Map<String, String> data;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Map<String, String> getData() {
        return data;
    }

    public void setData(Map<String, String> data) {
        this.data = data;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMalId() {
        return malId;
    }

    public void setMalId(String malId) {
        this.malId = malId;
    }

    public static LinkData from(AnimeLinkData data) {
        LinkData linkData = new LinkData();
        linkData.setData(data.getData());
        linkData.setId(data.getId());
        linkData.setTitle(data.getTitle());
        linkData.setUrl(data.getUrl());
        if(data.isAnime()) {
            linkData.setType("anime");
        }
        else {
            linkData.setType("manga");
        }
        String malId = data.getAnimeMetaData(AnimeLinkData.DataContract.DATA_MYANIMELIST_ID);
        if(malId != null) {
            linkData.setMalId(malId);
        }
        return linkData;
    }
}
