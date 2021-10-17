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

package io.ballerina.projects;

import io.ballerina.projects.internal.DefaultDiagnosticResult;
import io.ballerina.projects.internal.PackageContainer;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Represents a Dependencies.toml file.
 *
 * @since 2.0.0
 */
public class DependencyManifest {

    private final String dependenciesTomlVersion;
    private final List<Package> packages;
    private final DiagnosticResult diagnostics;
    private final PackageContainer<Package> pkgContainer;

    private DependencyManifest(String dependenciesTomlVersion,
                               List<Package> packages,
                               DiagnosticResult diagnostics) {
        this.dependenciesTomlVersion = dependenciesTomlVersion;
        this.packages = Collections.unmodifiableList(packages);
        this.diagnostics = diagnostics;

        // Populate a container for efficient access to packages
        this.pkgContainer = new PackageContainer<>();
        for (Package dependency : packages) {
            this.pkgContainer.add(dependency.org(), dependency.name(), dependency);
        }
    }

    public static DependencyManifest from(String dependenciesTomlVersion,
                                          List<Package> dependencies,
                                          DiagnosticResult diagnostics) {
        return new DependencyManifest(dependenciesTomlVersion, dependencies, diagnostics);
    }

    public static DependencyManifest from(String dependenciesTomlVersion,
                                          List<Package> dependencies) {
        return new DependencyManifest(dependenciesTomlVersion, dependencies,
                                      new DefaultDiagnosticResult(Collections.emptyList()));
    }

    public String dependenciesTomlVersion() {
        return dependenciesTomlVersion;
    }

    public Collection<Package> packages() {
        return packages;
    }

    public Optional<Package> dependency(PackageOrg org, PackageName name) {
        return pkgContainer.get(org, name);
    }

    public Package dependencyOrThrow(PackageOrg org, PackageName name) {
        return pkgContainer.get(org, name)
                .orElseThrow(() -> new IllegalStateException("Dependency with org `" +
                        org + "` and name `" + name + "` must exists."));
    }

    public DiagnosticResult diagnostics() {
        return diagnostics;
    }

    /**
     * Represents a dependency package.
     *
     * @since 2.0.0
     */
    public static class Package {
        private final PackageName packageName;
        private final PackageOrg packageOrg;
        private final PackageVersion version;
        private final String scope;
        private final boolean transitive;
        private final List<Dependency> dependencies;
        private final List<Module> modules;

        public Package(PackageName packageName, PackageOrg packageOrg, PackageVersion version) {
            this.packageName = packageName;
            this.packageOrg = packageOrg;
            this.version = version;
            this.scope = null;
            this.transitive = false;
            this.dependencies = Collections.emptyList();
            this.modules = Collections.emptyList();
        }

        public Package(PackageName packageName, PackageOrg packageOrg, PackageVersion version, String scope,
                       boolean transitive, List<Dependency> dependencies, List<Module> modules) {
            this.packageName = packageName;
            this.packageOrg = packageOrg;
            this.version = version;
            this.scope = scope;
            this.transitive = transitive;
            this.dependencies = dependencies;
            this.modules = modules;
        }

        public PackageName name() {
            return packageName;
        }

        public PackageOrg org() {
            return packageOrg;
        }

        public PackageVersion version() {
            return version;
        }

        public String scope() {
            return scope;
        }

        public boolean isTransitive() {
            return transitive;
        }

        public Collection<Dependency> dependencies() {
            return dependencies;
        }

        public List<Module> modules() {
            return modules;
        }
    }

    /**
     * Represents a dependency of a package.
     *
     * @since 2.0.0
     */
    public static class Dependency {
        private final PackageName packageName;
        private final PackageOrg packageOrg;

        public Dependency(PackageName packageName, PackageOrg packageOrg) {
            this.packageName = packageName;
            this.packageOrg = packageOrg;
        }

        public PackageName name() {
            return packageName;
        }

        public PackageOrg org() {
            return packageOrg;
        }
    }

    /**
     * Represents a module of a dependency.
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
