package org.ballerinalang.central.client.model;

import java.util.List;

public class PackageResolutionResponse {

    List<Package> resolved;
    List<Package> unresolved;

    public static class Package {
        private String orgName;
        private String name;
        private String version;
        private List<Package> dependencies;

        public Package(String orgName, String name, String version, List<Package> dependencies) {
            this.orgName = orgName;
            this.name = name;
            this.version = version;
            this.dependencies = dependencies;
        }

        public String orgName() {
            return orgName;
        }

        public void setOrgName(String orgName) {
            this.orgName = orgName;
        }

        public String name() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String version() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }
    }

    public List<Package> resolved() {
        return resolved;
    }

    public List<Package> unresolved() {
        return unresolved;
    }
}
