package com.dhavalpateln.linkcast.database.room.animelinkcache;

import android.util.Log;

import androidx.annotation.Nullable;
import androidx.room.Embedded;
import androidx.room.Relation;

import com.dhavalpateln.linkcast.database.AnimeLinkData;
import com.dhavalpateln.linkcast.database.FirebaseDBHelper;
import com.dhavalpateln.linkcast.database.room.linkmetadata.LinkMetaData;
import com.dhavalpateln.linkcast.database.room.maldata.MALMetaData;
import com.dhavalpateln.linkcast.utils.Utils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class LinkWithAllData implements Serializable {
    @Embedded
    public LinkData linkData;

    @Relation(parentColumn = "id", entityColumn = "id")
    public LinkMetaData linkMetaData;

    @Relation(parentColumn = "malId", entityColumn = "id")
    public MALMetaData malMetaData;

    public String getTitle() {
        if(this.malMetaData != null) {
            return this.malMetaData.getName();
        }
        return this.linkData.getTitle();
    }

    public String getId() {
        return this.linkData.getId();
    }

    public String getMetaData(String key) {
        if(linkData.getData().containsKey(key))  return linkData.getData().get(key);
        switch (key) {
            case AnimeLinkData.DataContract.DATA_FAVORITE:
                return "false";
            case AnimeLinkData.DataContract.DATA_STATUS:
                return "Planned";
            case AnimeLinkData.DataContract.DATA_SOURCE:
                return "";
            case AnimeLinkData.DataContract.DATA_EPISODE_NUM:
            case AnimeLinkData.DataContract.DATA_USER_SCORE:
            case AnimeLinkData.DataContract.DATA_VERSION:
                return "0";
            case AnimeLinkData.DataContract.DATA_LINK_TYPE:
                return "Anime";
            case AnimeLinkData.DataContract.DATA_LAST_FETCHED_EPISODES:
                return "-2";
            default:
                if(Utils.isNumeric(key)) {
                    return "0";
                }
                return null;
        }
    }

    public void updateData(String key, String value, boolean syncFirebase) {
        this.linkData.getData().put(key, value);
        if(syncFirebase) {

        }
    }

    public void updateFirebase() {
        if(getId() == null)  this.linkData.setId(Utils.getCurrentTime());

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
        linkWithAllData.malMetaData = null;

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

        if(this.malMetaData != null) {
            if(!this.malMetaData.equals(linkWithAllData.malMetaData))   return false;
        }

        return true;
    }
}
