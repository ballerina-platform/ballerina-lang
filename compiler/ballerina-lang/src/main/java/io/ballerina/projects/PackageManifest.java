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
package io.ballerina.projects;

import io.ballerina.projects.internal.DefaultDiagnosticResult;
import io.ballerina.projects.internal.model.CompilerPluginDescriptor;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Represents a Ballerina.toml file.
 *
 * @since 2.0.0
 */
public class PackageManifest {
    private final PackageDescriptor packageDesc;
    private final Optional<CompilerPluginDescriptor> compilerPluginDesc;
    private final Map<String, Platform> platforms;
    private final List<LocalPackage> localPackages;
    private final DiagnosticResult diagnostics;
    private final List<String> license;
    private final List<String> authors;
    private final List<String> keywords;
    private final String repository;
    private final List<String> exportedModules;

    // Other entries hold other key/value pairs available in the Ballerina.toml file.
    // These keys are not part of the Ballerina package specification.
    private final Map<String, Object> otherEntries;

    private PackageManifest(PackageDescriptor packageDesc,
                            Optional<CompilerPluginDescriptor> compilerPluginDesc,
                            Map<String, Platform> platforms,
                            List<LocalPackage> localPackages,
                            Map<String, Object> otherEntries,
                            DiagnosticResult diagnostics) {
        this.packageDesc = packageDesc;
        this.compilerPluginDesc = compilerPluginDesc;
        this.platforms = Collections.unmodifiableMap(platforms);
        this.localPackages = Collections.unmodifiableList(localPackages);
        this.otherEntries = Collections.unmodifiableMap(otherEntries);
        this.diagnostics = diagnostics;
        this.license = Collections.emptyList();
        this.authors = Collections.emptyList();
        this.keywords = Collections.emptyList();
        this.exportedModules = Collections.emptyList();
        this.repository = "";
    }

    private PackageManifest(PackageDescriptor packageDesc,
                            Optional<CompilerPluginDescriptor> compilerPluginDesc,
                            Map<String, Platform> platforms,
                            List<LocalPackage> localPackages,
                            Map<String, Object> otherEntries,
                            DiagnosticResult diagnostics,
                            List<String> license,
                            List<String> authors,
                            List<String> keywords,
                            List<String> exportedModules,
                            String repository) {
        this.packageDesc = packageDesc;
        this.compilerPluginDesc = compilerPluginDesc;
        this.platforms = Collections.unmodifiableMap(platforms);
        this.localPackages = Collections.unmodifiableList(localPackages);
        this.otherEntries = Collections.unmodifiableMap(otherEntries);
        this.diagnostics = diagnostics;
        this.license = license;
        this.authors = authors;
        this.keywords = keywords;
        this.exportedModules = getExport(packageDesc, exportedModules);
        this.repository = repository;
    }

    public static PackageManifest from(PackageDescriptor packageDesc) {
        return new PackageManifest(packageDesc, Optional.empty(), Collections.emptyMap(), Collections.emptyList(),
                                   Collections.emptyMap(), new DefaultDiagnosticResult(Collections.emptyList()));
    }

    public static PackageManifest from(PackageDescriptor packageDesc,
                                       Optional<CompilerPluginDescriptor> compilerPluginDesc,
                                       Map<String, Platform> platforms,
                                       List<LocalPackage> localPackages) {
        return new PackageManifest(packageDesc, compilerPluginDesc, platforms, localPackages, Collections.emptyMap(),
                new DefaultDiagnosticResult(Collections.emptyList()));
    }

    public static PackageManifest from(PackageDescriptor packageDesc,
                                       Optional<CompilerPluginDescriptor> compilerPluginDesc,
                                       Map<String, Platform> platforms,
                                       List<LocalPackage> localPackages,
                                       Map<String, Object> otherEntries,
                                       DiagnosticResult diagnostics,
                                       List<String> license,
                                       List<String> authors,
                                       List<String> keywords,
                                       List<String> export,
                                       String repository) {
        return new PackageManifest(packageDesc, compilerPluginDesc, platforms, localPackages, otherEntries, diagnostics,
                                   license, authors, keywords, export, repository);
    }

    public static PackageManifest from(PackageDescriptor packageDesc,
                                       Optional<CompilerPluginDescriptor> compilerPluginDesc,
                                       Map<String, Platform> platforms,
                                       List<LocalPackage> localPackages,
                                       List<String> license,
                                       List<String> authors,
                                       List<String> keywords,
                                       List<String> export,
                                       String repository) {
        return new PackageManifest(packageDesc, compilerPluginDesc, platforms, localPackages, Collections.emptyMap(),
                                   new DefaultDiagnosticResult(Collections.emptyList()), license, authors, keywords,
                                   export, repository);
    }

    public PackageName name() {
        return packageDesc.name();
    }

    public PackageOrg org() {
        return packageDesc.org();
    }

    public PackageVersion version() {
        return packageDesc.version();
    }

    public PackageDescriptor descriptor() {
        return packageDesc;
    }

    public Optional<CompilerPluginDescriptor> compilerPluginDescriptor() {
        return compilerPluginDesc;
    }

    public Platform platform(String platformCode) {
        return platforms.get(platformCode);
    }

    // TODO Do we need to custom key/value par mapping here
    public Object getValue(String key) {
        return otherEntries.get(key);
    }

    public List<String> license() {
        return license;
    }

    public List<String> authors() {
        return authors;
    }

    public List<String> keywords() {
        return keywords;
    }

    public List<String> exportedModules() {
        return exportedModules;
    }

    public String repository() {
        return repository;
    }

    public List<LocalPackage> localPackages() {
        return localPackages;
    }

    public DiagnosticResult diagnostics() {
        return diagnostics;
    }

    /**
     * Represents the platform section in Ballerina.toml file.
     *
     * @since 2.0.0
     */
    public static class Platform {
        // We could eventually add more things to the platform
        private final List<Map<String, Object>> dependencies;
        private final List<Map<String, Object>> repositories;

        public Platform(List<Map<String, Object>> dependencies) {
            this(dependencies, Collections.emptyList());
        }

        public Platform(List<Map<String, Object>> dependencies, List<Map<String, Object>> repositories) {
            if (dependencies != null) {
                this.dependencies = Collections.unmodifiableList(dependencies);
            } else {
                this.dependencies = Collections.emptyList();
            }
            if (repositories != null) {
                this.repositories = Collections.unmodifiableList(repositories);
            } else {
                this.repositories = Collections.emptyList();
            }
        }

        public List<Map<String, Object>> dependencies() {
            return dependencies;
        }

        public List<Map<String, Object>> repositories() {
            return repositories;
        }
    }

    /**
     * Represents a local dependency package.
     *
     * @since 2.0.0
     */
    public static class LocalPackage {
        private final PackageName packageName;
        private final PackageOrg packageOrg;
        private final PackageVersion semanticVersion;
        private final String repository;

        public LocalPackage(PackageName packageName, PackageOrg packageOrg, PackageVersion semanticVersion) {
            this.packageName = packageName;
            this.packageOrg = packageOrg;
            this.semanticVersion = semanticVersion;
            this.repository = null;
        }

        public LocalPackage(PackageName packageName, PackageOrg packageOrg, PackageVersion semanticVersion,
                            String repository) {
            this.packageName = packageName;
            this.packageOrg = packageOrg;
            this.semanticVersion = semanticVersion;
            this.repository = repository;
        }

        public PackageName name() {
            return packageName;
        }

        public PackageOrg org() {
            return packageOrg;
        }

        public PackageVersion version() {
            return semanticVersion;
        }

        public String repository() {
            return repository;
        }
    }

    private List<String> getExport(PackageDescriptor packageDesc, List<String> export) {
        if (export == null || export.isEmpty()) {
            return Collections.singletonList(packageDesc.name().value());
        }
        return export;
    }
}
