package com.csula.hw3.indexer;

public class ScoreFactor {
    private String term;

    private int count = 0;

    private double  rank = 0d;

    public ScoreFactor(String term, int count) {
        this.term = term;
        this.count = count;
    }

    public ScoreFactor(String term, int count, double rank) {
        this.term = term;
        this.count = count;
        this.rank = rank;
    }

    public double getRank() {
        return rank;
    }

    public void setRank(double rank) {
        this.rank = rank;
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
