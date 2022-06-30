package com.dhavalpateln.linkcast.myanimelist;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AdvSearchParams implements Serializable {
    private String type;
    private Set<Integer> genres;
    private int score;
    private String status;
    private String orderBy;
    private String sort;
    private String query;

    public AdvSearchParams() {
        this.orderBy = "members";
        this.sort = "desc";
        this.genres = new HashSet<>();
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type.toLowerCase();
    }

    public Set<Integer> getGenres() {
        return genres;
    }

    public void addGenre(String genre) {
        genre = genre.toLowerCase();
        if(MyAnimelistSearch.genreMap.containsKey(genre)) {
            this.genres.add(MyAnimelistSearch.genreMap.get(genre));
        }
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status.toLowerCase();
    }

    public String getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }
}