package org.ballerinalang.central.client.model;

import java.util.ArrayList;
import java.util.List;

public class PackageResolutionRequest {

    List<Package> packages;

    static class Package {
        private String orgName;
        private String name;
        private String version;
        Mode mode;

        public Package(String orgName, String name, String version, Mode mode) {
            this.orgName = orgName;
            this.name = name;
            this.version = version;
            this.mode = mode;
        }

        public String getOrgName() {
            return orgName;
        }

        public void setOrgName(String orgName) {
            this.orgName = orgName;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public Mode getMode() {
            return mode;
        }

        public void setMode(Mode mode) {
            this.mode = mode;
        }
    }

    public static enum Mode {
        SOFT("soft"),
        MEDIUM("medium"),
        HARD("hard");

        private final String text;

        /**
         * @param text
         */
        Mode(final String text) {
            this.text = text;
        }

        /* (non-Javadoc)
         * @see java.lang.Enum#toString()
         */
        @Override
        public String toString() {
            return text;
        }
    }

    public PackageResolutionRequest() {
        this.packages = new ArrayList<>();
    }

    public void addPackage(String orgName, String name, String version, Mode mode) {
        packages.add(new Package(orgName, name, version, mode));
    }
}
