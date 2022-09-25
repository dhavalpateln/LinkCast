package com.dhavalpateln.linkcast.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Searcher<T> {

    private Map<String, T> searchMap;

    public Searcher(Map<String, T> searchMap) {
        this.searchMap = searchMap;
    }

    public List<T> search(String term) {
        CandidateList<T> candidateList = new CandidateList<>(10);
        term = term.toLowerCase();
        for(Map.Entry<String, T> entry: searchMap.entrySet()) {
            String name = entry.getKey().toLowerCase();
            double score = name.contains(term) ? ((1.0 * term.length()) / name.length()) : 0;
            candidateList.add(entry.getValue(), score);
        }
        return candidateList.getCandidates();
    }
}
