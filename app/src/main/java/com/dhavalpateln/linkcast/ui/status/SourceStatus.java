package com.dhavalpateln.linkcast.ui.status;

import java.util.Map;

public class SourceStatus {
    private String status;
    private Map<String, SourceStatus> sources;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Map<String, SourceStatus> getSources() {
        return sources;
    }

    public void setSources(Map<String, SourceStatus> sources) {
        this.sources = sources;
    }
}
