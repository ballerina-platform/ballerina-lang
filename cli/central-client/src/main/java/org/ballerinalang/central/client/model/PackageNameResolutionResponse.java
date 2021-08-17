package org.ballerinalang.central.client.model;

import java.util.List;

public class PackageNameResolutionResponse {

    List<Module> resolvedModules;

    public static class Module {
        String organization;
        String moduleName;
        String version;
        String packageName;

        public String getOrganization() {
            return organization;
        }

        public void setOrganization(String organization) {
            this.organization = organization;
        }

        public String getModuleName() {
            return moduleName;
        }

        public void setModuleName(String moduleName) {
            this.moduleName = moduleName;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getPackageName() {
            return packageName;
        }

        public void setPackageName(String packageName) {
            this.packageName = packageName;
        }

        public Module(String organization, String moduleName) {
            this.organization = organization;
            this.moduleName = moduleName;

        }
    }

    public List<Module> modules() {
        return resolvedModules;
    }
}
