package com.dhavalpateln.linkcast.database.room.animelinkcache;

import android.util.Log;

import androidx.annotation.Nullable;
import androidx.room.Embedded;
import androidx.room.Relation;

import com.dhavalpateln.linkcast.database.AnimeLinkData;
import com.dhavalpateln.linkcast.database.FirebaseDBHelper;
import com.dhavalpateln.linkcast.database.room.linkmetadata.LinkMetaData;
import com.dhavalpateln.linkcast.database.room.almaldata.AlMalMetaData;
import com.dhavalpateln.linkcast.utils.Utils;
import com.google.firebase.database.DatabaseReference;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class LinkWithAllData implements Serializable {
    @Embedded
    public LinkData linkData;

    @Relation(parentColumn = "id", entityColumn = "id")
    public LinkMetaData linkMetaData;

    @Relation(parentColumn = "malId", entityColumn = "id")
    public AlMalMetaData alMalMetaData;

    public String getTitle() {
        if(this.alMalMetaData != null && this.alMalMetaData.getName() != null && isAnime()) {
            return this.alMalMetaData.getName();
        }
        return this.linkData.getTitle();
    }

    public String getId() {
        return this.linkData.getId();
    }

    public String getMetaData(String key) {
        if(linkData.getData().containsKey(key))  return linkData.getData().get(key);
        if(linkMetaData != null && linkMetaData.getMiscData().containsKey(key)) return linkMetaData.getMiscData().get(key);
        switch (key) {
            case LinkDataContract.DATA_FAVORITE:
                return "false";
            case LinkDataContract.DATA_STATUS:
                return "Planned";
            case LinkDataContract.DATA_SOURCE:
                return "";
            case LinkDataContract.NOTIFICATION:
                return "1";
            case LinkDataContract.DATA_EPISODE_NUM:
            case LinkDataContract.DATA_USER_SCORE:
            case LinkDataContract.DATA_VERSION:
                return "0";
            case LinkDataContract.DATA_LINK_TYPE:
                return "Anime";
            case LinkDataContract.DATA_LAST_FETCHED_EPISODES:
                return "-2";
            default:
                if(Utils.isNumeric(key)) {
                    return "0";
                }
                return null;
        }
    }

    public String getUrl() {
        return this.linkData.getUrl();
    }

    public void updateLocalData(String key, String value) {
        if(this.linkMetaData == null)   this.linkMetaData = new LinkMetaData();
        this.linkMetaData.getMiscData().put(key, value);
    }

    public void updateData(String key, String value) {
        this.linkData.getData().put(key, value);
        if(getId() != null) {
            DatabaseReference ref;
            if(isAnime())   ref = FirebaseDBHelper.getUserAnimeWebExplorerLinkRef(getId());
            else ref = FirebaseDBHelper.getUserMangaWebExplorerLinkRef(getId());
            ref.child("data").child(key).setValue(value);
        }
    }

    public void updateData(LinkData linkData) {
        Map<String, String> data = linkData.getData();
        this.linkData.setTitle(linkData.getTitle());
        this.linkData.setUrl(linkData.getUrl());

        for(Map.Entry<String, String> entry: data.entrySet()) {
            this.linkData.getData().put(entry.getKey(), entry.getValue());
        }
        updateFirebase();
    }

    public void updateFirebase() {
        if(getId() == null) {
            if(this.linkMetaData == null)   this.linkMetaData = new LinkMetaData();
            this.linkData.setId(Utils.getCurrentTime());
            this.linkMetaData.setId(this.linkData.getId());
        }

        if(this.linkData.getTitle() == null || this.linkData.getUrl() == null) {
            Log.d("LinkWithAllData", "Something bad happened. Bad copy");
            FirebaseDBHelper.getUserDataRef().child("badcopy").child(Utils.getCurrentTime()).setValue(getId());
            return;
        }

        Map<String, Object> update = new HashMap<>();
        update.put(getId() + "/title", this.linkData.getTitle());
        update.put(getId() + "/url", this.linkData.getUrl());

        for(String key: this.linkData.getData().keySet()) {
            update.put(getId() + "/data/" + key, this.linkData.getData().get(key));
        }
        if(this.linkData.getType().equalsIgnoreCase("anime"))   FirebaseDBHelper.getUserAnimeWebExplorerLinkRef().updateChildren(update);
        else FirebaseDBHelper.getUserMangaWebExplorerLinkRef().updateChildren(update);
    }

    public boolean isAnime() {
        return this.linkData.getType().equalsIgnoreCase("anime");
    }

    public LinkWithAllData from(AnimeLinkData animeLinkData) {
        LinkWithAllData linkWithAllData = new LinkWithAllData();
        linkWithAllData.linkData = LinkData.from(animeLinkData);
        linkWithAllData.linkMetaData = new LinkMetaData();
        linkWithAllData.alMalMetaData = null;

        linkWithAllData.linkMetaData.setId(linkWithAllData.linkData.getId());
        linkWithAllData.linkMetaData.setLastEpisodeNodesFetchCount(-2);
        linkWithAllData.linkMetaData.setMiscData(new HashMap<>());
        return linkWithAllData;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if(obj == null) return false;
        if(!(obj instanceof LinkWithAllData)) return false;

        LinkWithAllData linkWithAllData = (LinkWithAllData) obj;

        if(!getId().equals(linkWithAllData.getId()) ||
                !this.linkData.getTitle().equals(linkWithAllData.linkData.getTitle()))  return false;

        for(Map.Entry<String, String> entry: this.linkData.getData().entrySet()) {
            if(!entry.getValue().equals(linkWithAllData.linkData.getData().get(entry.getKey())))    return false;
        }

        if(linkMetaData != null) {
            if(linkWithAllData.linkMetaData == null)    return false;
            if(linkMetaData.getLastEpisodeNodesFetchCount() != linkWithAllData.linkMetaData.getLastEpisodeNodesFetchCount())    return false;
            for(Map.Entry<String, String> entry: this.linkMetaData.getMiscData().entrySet()) {
                if(!entry.getValue().equals(linkWithAllData.linkMetaData.getMiscData().get(entry.getKey())))    return false;
            }
        }
        else if(linkWithAllData.linkMetaData != null)   return false;

        if(this.alMalMetaData != null) {
            if(!this.alMalMetaData.equals(linkWithAllData.alMalMetaData))   return false;
        }

        return true;
    }
}
