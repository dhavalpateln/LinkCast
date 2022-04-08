package com.dhavalpateln.linkcast.myanimelist;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import androidx.annotation.Nullable;

public class MyAnimelistCharacterData {
    private int id;
    private String url;
    private List<String> images;
    private String type;
    private String about;
    private Set<MyAnimelistAnimeData> animeography;

    public MyAnimelistCharacterData() {
        images = new ArrayList<>();
        animeography = new HashSet<>();
    }

    public String getUrl() {
        return url;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setUrl(String url) {
        if(url.startsWith("/")) url = "https://myanimelist.net" + url;
        this.url = url;
        if(getId() <= 0) {
            try {
                String[] urlSplit = url.split("/");
                setId(Integer.valueOf(urlSplit[urlSplit.length - 2]));
            } catch (Exception e) {}
        }
    }

    public List<String> getImages() {
        return images;
    }

    public void addImage(String imageUrl) {
        if(!this.images.contains(imageUrl)) {
            this.images.add(imageUrl);
        }
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public Set<MyAnimelistAnimeData> getAnimeography() {
        return animeography;
    }

    public void addAnimeography(MyAnimelistAnimeData data) {
        this.animeography.add(data);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if(obj != null && obj instanceof MyAnimelistAnimeData) {
            return ((MyAnimelistAnimeData) obj).getId() == getId();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return getId();
    }
}
