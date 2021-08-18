/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.central.client.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Package resolution request model used by package resolver in central client.
 *
 * @since 2.0.0
 */
public class PackageResolutionRequest {

    List<Package> packages;

    /**
     * Package resolution request package model.
     *
     */
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

    /**
     * Package resolution response mode.
     *
     */
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
