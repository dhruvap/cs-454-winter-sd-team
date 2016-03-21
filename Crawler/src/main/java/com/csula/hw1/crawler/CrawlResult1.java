package com.csula.hw1.crawler;

import java.io.Serializable;
import java.util.Set;

public class CrawlResult implements Serializable {

    private String id;
    private String parentId;
    private int depth;
    private String resourceUrl;
    private String raw;
    private Set<String> parsedResource;
    private String fileLocation;

    public String getFileLocation() {
        return fileLocation;
    }

    public void setFileLocation(String fileLocation) {
        this.fileLocation = fileLocation;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public String getResourceUrl() {
        return resourceUrl;
    }

    public void setResourceUrl(String resourceUrl) {
        this.resourceUrl = resourceUrl;
    }

    public String getRaw() {
        return raw;
    }

    public void setRaw(String raw) {
        this.raw = raw;
    }

    public Set<String> getParsedResource() {
        return parsedResource;
    }

    public void setParsedResource(Set<String> parsedResource) {
        this.parsedResource = parsedResource;
    }
}
