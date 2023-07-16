package com.dhavalpateln.linkcast.database;

import androidx.annotation.Nullable;

import com.dhavalpateln.linkcast.utils.Utils;

import java.io.Serializable;

public class AnimeMALMetaData implements Serializable {
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

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        AnimeMALMetaData metaDataObj = (AnimeMALMetaData) obj;
        return getId().equals(metaDataObj.getId()) &&
                getStatus().equals(metaDataObj.getStatus()) &&
                Utils.compareNullableStrings(getAirDate(), metaDataObj.getAirDate()) &&
                Utils.compareNullableStrings(getTotalEpisodes(), metaDataObj.getTotalEpisodes()) &&
                getName().equals(metaDataObj.getName()) &&
                Utils.compareNullableStrings(getImageURL(), metaDataObj.getImageURL()) &&
                getEngName().equals(metaDataObj.getEngName());
    }
}
