package io.ballerina.multiservice.model;

import java.util.List;

/**
 * Represents intermediate model to represent multi-service projects.
 */
public class ComponentModel {
    private final PackageId packageId;

    private final List<Service> services;

    public ComponentModel(PackageId packageId, List<Service> services) {
        this.packageId = packageId;
        this.services = services;
    }

    public PackageId getPackageId() {
        return packageId;
    }

    public List<Service> getServices() {
        return services;
    }

    /**
     * Represent current package information.
     */
    public static class PackageId {
        private final String name;
        private final String org;
        private final String version;

        public PackageId(String name, String org, String version) {
            this.name = name;
            this.org = org;
            this.version = version;
        }

        public String getName() {
            return name;
        }

        public String getOrg() {
            return org;
        }

        public String getVersion() {
            return version;
        }
    }
}
