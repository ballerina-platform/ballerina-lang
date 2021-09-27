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

package io.ballerina.projects.internal.model;

import io.ballerina.projects.PackageDependencyScope;

import java.util.Collections;
import java.util.List;

/**
 * {@code Dependency} Model for Dependency.
 *
 * @since 2.0.0
 */
public class Dependency {
    String org;
    String name;
    String version;
    PackageDependencyScope scope;
    boolean transitive;
    List<Dependency> dependencies;
    List<Module> modules;

    public Dependency(String org, String name, String version) {
        this.org = org;
        this.name = name;
        this.version = version;
        this.dependencies = Collections.emptyList();
        this.modules = Collections.emptyList();
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

    public List<Dependency> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<Dependency> dependencies) {
        this.dependencies = dependencies;
    }

    public PackageDependencyScope getScope() {
        return scope;
    }

    public void setScope(PackageDependencyScope scope) {
        this.scope = scope;
    }

    public boolean isTransitive() {
        return transitive;
    }

    public void setTransitive(boolean transitive) {
        this.transitive = transitive;
    }

    public List<Module> getModules() {
        return modules;
    }

    public void setModules(List<Module> modules) {
        this.modules = modules;
    }

    /**
     * Represents a module of a dependency package.
     *
     * @since 2.0.0
     */
    public static class Module {
        private final String org;
        private final String packageName;
        private final String moduleName;

        public Module(String org, String packageName, String moduleName) {
            this.org = org;
            this.packageName = packageName;
            this.moduleName = moduleName;
        }

        public String org() {
            return org;
        }

        public String packageName() {
            return packageName;
        }

        public String moduleName() {
            return moduleName;
        }
    }
}
