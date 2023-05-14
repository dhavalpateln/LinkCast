package com.dhavalpateln.linkcast.database.room.maldata;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.dhavalpateln.linkcast.database.AnimeMALMetaData;
import com.dhavalpateln.linkcast.utils.Utils;

import java.io.Serializable;

@Entity
public class MALMetaData implements Serializable {

    @PrimaryKey
    @NonNull
    private String id;

    private String url;
    private String totalEpisodes;
    private String status;
    private String imageURL;
    private String name;
    private String engName;
    private String airDate;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTotalEpisodes() {
        return totalEpisodes;
    }

    public void setTotalEpisodes(String totalEpisodes) {
        this.totalEpisodes = totalEpisodes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEngName() {
        return engName;
    }

    public void setEngName(String engName) {
        this.engName = engName;
    }

    public String getAirDate() {
        return airDate;
    }

    public void setAirDate(String airDate) {
        this.airDate = airDate;
    }

    public static MALMetaData from(AnimeMALMetaData animeMALMetaData) {
        MALMetaData malMetaData = new MALMetaData();
        malMetaData.setAirDate(animeMALMetaData.getAirDate());
        malMetaData.setEngName(animeMALMetaData.getEngName());
        malMetaData.setId(animeMALMetaData.getId());
        malMetaData.setImageURL(animeMALMetaData.getImageURL());
        malMetaData.setName(animeMALMetaData.getName());
        malMetaData.setStatus(animeMALMetaData.getStatus());
        malMetaData.setTotalEpisodes(animeMALMetaData.getTotalEpisodes());
        malMetaData.setUrl(animeMALMetaData.getUrl());
        return malMetaData;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        MALMetaData metaDataObj = (MALMetaData) obj;
        return getId().equals(metaDataObj.getId()) &&
                Utils.compareNullableStrings(getStatus(), metaDataObj.getStatus()) &&
                Utils.compareNullableStrings(getAirDate(), metaDataObj.getAirDate()) &&
                Utils.compareNullableStrings(getTotalEpisodes(), metaDataObj.getTotalEpisodes()) &&
                Utils.compareNullableStrings(getName(), metaDataObj.getName()) &&
                Utils.compareNullableStrings(getImageURL(), metaDataObj.getImageURL()) &&
                Utils.compareNullableStrings(getEngName(), metaDataObj.getEngName());
    }
}
