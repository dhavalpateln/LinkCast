package com.dhavalpateln.linkcast.database.room.animelinkcache;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.dhavalpateln.linkcast.database.AnimeLinkData;
import com.dhavalpateln.linkcast.database.room.linkmetadata.LinkMetaData;
import com.dhavalpateln.linkcast.database.room.maldata.MALMetaData;
import com.dhavalpateln.linkcast.utils.Utils;

import java.io.Serializable;

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
}
