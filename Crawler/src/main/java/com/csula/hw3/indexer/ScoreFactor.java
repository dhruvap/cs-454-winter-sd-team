package com.csula.hw3.indexer;

public class ScoreFactor {
    private String term;

    private int count = 0;

    public ScoreFactor(String term, int count) {
        this.term = term;
        this.count = count;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
