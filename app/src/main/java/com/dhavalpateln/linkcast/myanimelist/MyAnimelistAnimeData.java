package com.dhavalpateln.linkcast.myanimelist;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import androidx.annotation.Nullable;

public class MyAnimelistAnimeData implements Serializable {
    private int id;
    private String title;
    private String englishTitle;
    private String url;
    private String[] alternateTitles;
    private List<String> images;
    private Set<MyAnimelistAnimeData> prequels;
    private Set<MyAnimelistAnimeData> sequels;
    private Set<MyAnimelistAnimeData> sideStory;
    private List<MyAnimelistCharacterData> characters;
    private List<MyAnimelistAnimeData> recommendations;

    String synopsis;
    private Map<String, String> infos;

    public MyAnimelistAnimeData(int id) {
        this.id = id;
        infos = new HashMap<>();
        this.images = new ArrayList<>();
        prequels = new HashSet<>();
        sequels = new HashSet<>();
        sideStory = new HashSet<>();
        characters = new ArrayList<>();
        recommendations = new ArrayList<>();
    }

    public MyAnimelistAnimeData() {
        this(0);
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getEnglishTitle() {
        return englishTitle;
    }

    public void setEnglishTitle(String englishTitle) {
        this.englishTitle = englishTitle;
    }

    public String getUrl() {
        return url;
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

    public String[] getAlternateTitles() {
        return alternateTitles;
    }

    public void setAlternateTitles(String[] alternateTitles) {
        this.alternateTitles = alternateTitles;
    }

    public void putInfo(String key, String value) {
        infos.put(key, value);
    }

    public String getInfo(String key) {
        if(!infos.containsKey(key)) return "N/A";
        return infos.get(key);
    }

    public String getSynopsis() {
        if(synopsis == null)    return "N/A";
        return synopsis;
    }

    public void setSynopsis(String synopsis) {
        this.synopsis = synopsis;
    }

    public List<String> getImages() {
        return images;
    }

    public void addImage(String imageURL) {
        if(!this.images.contains(imageURL))  this.images.add(imageURL);
    }

    public void addPrequel(MyAnimelistAnimeData data) {
        prequels.add(data);
    }
    public void addSequel(MyAnimelistAnimeData data) {
        sequels.add(data);
    }
    public void addSideStory(MyAnimelistAnimeData data) {
        sideStory.add(data);
    }

    public Set<MyAnimelistAnimeData> getPrequels() {
        return prequels;
    }

    public Set<MyAnimelistAnimeData> getSequels() {
        return sequels;
    }

    public Set<MyAnimelistAnimeData> getSideStory() {
        return sideStory;
    }

    public List<MyAnimelistCharacterData> getCharacters() {
        return characters;
    }

    public void addCharacter(MyAnimelistCharacterData character) {
        if(!this.characters.contains(character)) {
            this.characters.add(character);
        }
    }

    public List<MyAnimelistAnimeData> getRecommendations() {
        return recommendations;
    }

    public void addRecommendation(MyAnimelistAnimeData data) {
        if(!this.recommendations.contains(data)) {
            this.recommendations.add(data);
        }
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
