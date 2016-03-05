package com.csula.hw1.crawler;

import java.net.URL;

public class InputBean {

    private int depth = -1;

    private boolean termIndex = false;

    private String url;

    private URL urlObj;

    private String domain;

    private boolean extract;




    public boolean isExtract() {
        return extract;
    }

    public void setExtract(boolean extract) {
        this.extract = extract;
    }

    private boolean insideDomain = false;

    public URL getUrlObj() {
        return urlObj;
    }

    public void setUrlObj(URL urlObj) {
        this.urlObj = urlObj;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public boolean isInsideDomain() {
        return insideDomain;
    }

    public void setInsideDomain(boolean insideDomain) {
        this.insideDomain = insideDomain;
    }

    public InputBean() {
    }

    public InputBean(int depth, String url) {
        this.depth = depth;
        this.url = url;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isTermIndex() {
        return termIndex;
    }

    public void setTermIndex(boolean termIndex) {
        this.termIndex = termIndex;
    }
}
