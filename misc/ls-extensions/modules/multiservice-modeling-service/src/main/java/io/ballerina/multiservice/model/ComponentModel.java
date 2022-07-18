package io.ballerina.multiservice.model;

import java.util.List;
import io.ballerina.multiservice.model.Service;

public class ComponentModel {
    private final PackageId packageId;

    private final List<Service> services;

    public ComponentModel(PackageId packageId, List<Service> services) {
        this.packageId = packageId;
        this.services = services;
    }

    public static class PackageId {
        private final String name;
        private final String org;
        private final String version;

        public PackageId(String name, String org, String version) {
            this.name = name;
            this.org = org;
            this.version = version;
        }
    }
}
