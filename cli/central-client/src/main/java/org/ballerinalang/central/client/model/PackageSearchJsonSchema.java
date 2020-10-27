package org.ballerinalang.central.client.model;

import java.util.List;

public class PackageSearchJsonSchema {

    private List<PackageJsonSchema> packages;
    private int count;

    public List<PackageJsonSchema> getPackages() {
        return packages;
    }

    public void setPackages(List<PackageJsonSchema> packages) {
        this.packages = packages;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
