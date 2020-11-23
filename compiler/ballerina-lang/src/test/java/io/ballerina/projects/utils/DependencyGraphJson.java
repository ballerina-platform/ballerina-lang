/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.projects.utils;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * A bean that represents a dependency graph.
 */
public class DependencyGraphJson {
    private static final String PACKAGE_DEPENDENCY_GRAPH = "packages";

    @SerializedName(PACKAGE_DEPENDENCY_GRAPH)
    private List<DependencyJson> directDependencies;

    public List<DependencyJson> directDependencies() {
        return directDependencies;
    }

    /**
     * A bean that represents a package dependency.
     */
    public static class DependencyJson {
        private final String org;
        private final String name;
        private final String version;
        private final List<DependencyJson> dependencies;

        public DependencyJson(String org, String name, String version, List<DependencyJson> dependencies) {
            this.org = org;
            this.name = name;
            this.version = version;
            this.dependencies = dependencies;
        }

        public String getOrg() {
            return org;
        }

        public String getName() {
            return name;
        }

        public String getVersion() {
            return version;
        }

        public List<DependencyJson> getDependencies() {
            return dependencies;
        }
    }
}
