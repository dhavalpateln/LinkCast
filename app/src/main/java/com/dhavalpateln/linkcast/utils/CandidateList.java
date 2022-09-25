package com.dhavalpateln.linkcast.utils;

import java.util.ArrayList;
import java.util.List;

public class CandidateList<K> {

    private class CandidateItem<T> {
        private T candidate;
        private double score;
        public CandidateItem(T name, double score) {
            this.candidate = name;
            this.score = score;
        }
        public T getCandidate() {return this.candidate;}
        public double getScore() {return this.score;}
    }

    private List<CandidateItem<K>> candidates;
    private int size;

    public CandidateList() {
        this(10);
    }

    public CandidateList(int size) {
        candidates = new ArrayList<>();
        this.size = size;
    }
    public void add(K candidate, double score) {
        double minScore = candidates.isEmpty() ? 0 : candidates.get(candidates.size() - 1).getScore();
        if(minScore < score) {
            int insertIndex;
            for(insertIndex = 0; insertIndex < candidates.size(); insertIndex++) {
                if(score >= candidates.get(insertIndex).getScore()) {
                    break;
                }
            }
            candidates.add(insertIndex, new CandidateItem(candidate, score));
        }
        if(candidates.size() > this.size) {
            candidates.remove(candidates.size() - 1);
        }
    }

    public List<K> getCandidates() {
        List<K> result = new ArrayList<>();
        for(CandidateItem<K> candidate: candidates) {
            result.add(candidate.getCandidate());
        }
        return result;
    }
}
