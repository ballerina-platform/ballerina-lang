package org.ballerinalang.central.client.model;

import java.util.List;

/**
 * {@code PackageJsonSchema} represents package search result from central.
 */
public class PackageSearchResult {

    private List<Package> packages;
    private int count;

    public List<Package> getPackages() {
        return packages;
    }

    public void setPackages(List<Package> packages) {
        this.packages = packages;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
