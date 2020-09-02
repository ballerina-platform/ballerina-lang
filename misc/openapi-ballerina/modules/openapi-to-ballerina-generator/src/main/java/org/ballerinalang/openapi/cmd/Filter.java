package org.ballerinalang.openapi.cmd;

import java.util.ArrayList;
import java.util.List;

public class Filter {
    private List<String> tags = new ArrayList<>();
    private List<String> operations = new ArrayList<>();

    public Filter(List<String> tags, List<String> operations) {
        this.tags = tags;
        this.operations = operations;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public List<String> getOperations() {
        return operations;
    }

    public void setOperations(List<String> operations) {
        this.operations = operations;
    }
}
