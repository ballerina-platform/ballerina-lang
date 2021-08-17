package org.ballerinalang.central.client.model;

import java.util.ArrayList;
import java.util.List;

public class PackageNameResolutionRequest {

    List<Module> modules;

    static class Module {
        String organization;
        String moduleName;

        public Module(String organization, String moduleName) {
            this.organization = organization;
            this.moduleName = moduleName;
        }
    }

    public PackageNameResolutionRequest() {
        this.modules = new ArrayList<>();
    }

    public void addModule(String orgName, String moduleName) {
        modules.add(new Module(orgName, moduleName));
    }
}
