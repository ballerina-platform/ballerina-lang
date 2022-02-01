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

import java.util.List;

/**
 * Package Resolution response.
 *
 * @since 2.0.0
 */
public class PackageResolutionResponse {

    List<Package> resolved;
    List<Package> unresolved;

    private PackageResolutionResponse(List<Package> resolved, List<Package> unresolved) {
        this.resolved = resolved;
        this.unresolved = unresolved;
    }

    public static PackageResolutionResponse from(List<Package> resolved, List<Package> unresolved) {
        return new PackageResolutionResponse(resolved, unresolved);
    }

    /**
     * Package resolution response package model.
     *
     */
    public static class Package {
        private String org;
        private String name;
        private String version;
        private List<Dependency> dependencyGraph;

        public Package(String orgName, String name, String version, List<Dependency> dependencies) {
            this.org = orgName;
            this.name = name;
            this.version = version;
            this.dependencyGraph = dependencies;
        }

        public String org() {
            return org;
        }

        public void setOrg(String org) {
            this.org = org;
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

        public List<Dependency> dependencyGraph() {
            return dependencyGraph;
        }

        public void setDependencies(List<Dependency> dependencies) {
            this.dependencyGraph = dependencies;
        }
    }

    /**
     * Package resolution response Dependency model.
     */
    public static class Dependency {
        private String org;
        private String name;
        private String version;
        private List<Dependency> dependencies;

        public <T> Dependency(String org, String name, String version, List<Dependency> dependencies) {
            this.org = org;
            this.name = name;
            this.version = version;
            this.dependencies = dependencies;
        }

        public String org() {
            return org;
        }

        public String name() {
            return name;
        }

        public String version() {
            return version;
        }

        public List<Dependency> dependencies() {
            return dependencies;
        }

        public void setOrg(String org) {
            this.org = org;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public void setDependencies(List<Dependency> dependencies) {
            this.dependencies = dependencies;
        }
    }

    public List<Package> resolved() {
        return resolved;
    }

    public List<Package> unresolved() {
        return unresolved;
    }

}
