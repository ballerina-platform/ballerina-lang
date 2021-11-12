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
import io.ballerina.projects.internal.model.Dependency;
import org.ballerinalang.toml.model.Platform;

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
    private final CompilerPluginDescriptor compilerPluginDesc;
    private final Map<String, Platform> platforms;
    private final List<Dependency> dependencies;
    private final DiagnosticResult diagnostics;
    private final List<String> license;
    private final List<String> authors;
    private final List<String> keywords;
    private final String repository;
    private final List<String> exportedModules;
    private final String ballerinaVersion;
    private final String visibility;
    private boolean template;

    // Other entries hold other key/value pairs available in the Ballerina.toml file.
    // These keys are not part of the Ballerina package specification.
    private final Map<String, Object> otherEntries;

    private PackageManifest(PackageDescriptor packageDesc,
                            CompilerPluginDescriptor compilerPluginDesc,
                            Map<String, Platform> platforms,
                            List<Dependency> dependencies,
                            Map<String, Object> otherEntries,
                            DiagnosticResult diagnostics) {
        this.packageDesc = packageDesc;
        this.compilerPluginDesc = compilerPluginDesc;
        this.platforms = Collections.unmodifiableMap(platforms);
        this.dependencies = Collections.unmodifiableList(dependencies);
        this.otherEntries = Collections.unmodifiableMap(otherEntries);
        this.diagnostics = diagnostics;
        this.license = Collections.emptyList();
        this.authors = Collections.emptyList();
        this.keywords = Collections.emptyList();
        this.exportedModules = Collections.emptyList();
        this.repository = "";
        this.ballerinaVersion = "";
        this.visibility = "";
    }

    private PackageManifest(PackageDescriptor packageDesc,
                            CompilerPluginDescriptor compilerPluginDesc,
                            Map<String, Platform> platforms,
                            List<Dependency> dependencies,
                            Map<String, Object> otherEntries,
                            DiagnosticResult diagnostics,
                            List<String> license,
                            List<String> authors,
                            List<String> keywords,
                            List<String> exportedModules,
                            String repository,
                            String ballerinaVersion,
                            String visibility,
                            boolean template) {
        this.packageDesc = packageDesc;
        this.compilerPluginDesc = compilerPluginDesc;
        this.platforms = Collections.unmodifiableMap(platforms);
        this.dependencies = Collections.unmodifiableList(dependencies);
        this.otherEntries = Collections.unmodifiableMap(otherEntries);
        this.diagnostics = diagnostics;
        this.license = license;
        this.authors = authors;
        this.keywords = keywords;
        this.exportedModules = getExport(packageDesc, exportedModules);
        this.repository = repository;
        this.ballerinaVersion = ballerinaVersion;
        this.visibility = visibility;
        this.template = template;
    }

    public static PackageManifest from(PackageDescriptor packageDesc) {
        return new PackageManifest(packageDesc, null, Collections.emptyMap(), Collections.emptyList(),
                                   Collections.emptyMap(), new DefaultDiagnosticResult(Collections.emptyList()));
    }

    public static PackageManifest from(PackageDescriptor packageDesc,
                                       CompilerPluginDescriptor compilerPluginDesc,
                                       Map<String, Platform> platforms,
                                       List<Dependency> dependencies) {
        return new PackageManifest(packageDesc, compilerPluginDesc, platforms, dependencies, Collections.emptyMap(),
                new DefaultDiagnosticResult(Collections.emptyList()));
    }

    public static PackageManifest from(PackageDescriptor packageDesc,
                                       CompilerPluginDescriptor compilerPluginDesc,
                                       Map<String, Platform> platforms,
                                       List<Dependency> dependencies,
                                       Map<String, Object> otherEntries,
                                       DiagnosticResult diagnostics,
                                       List<String> license,
                                       List<String> authors,
                                       List<String> keywords,
                                       List<String> export,
                                       String repository,
                                       String ballerinaVersion,
                                       String visibility,
                                       boolean template) {
        return new PackageManifest(packageDesc, compilerPluginDesc, platforms, dependencies, otherEntries, diagnostics,
                license, authors, keywords, export, repository, ballerinaVersion, visibility, template);
    }

    public static PackageManifest from(PackageDescriptor packageDesc,
                                       CompilerPluginDescriptor compilerPluginDesc,
                                       Map<String, Platform> platforms,
                                       List<Dependency> dependencies,
                                       List<String> license,
                                       List<String> authors,
                                       List<String> keywords,
                                       List<String> export,
                                       String repository,
                                       String ballerinaVersion,
                                       String visibility,
                                       boolean template) {
        return new PackageManifest(packageDesc, compilerPluginDesc, platforms, dependencies, Collections.emptyMap(),
                new DefaultDiagnosticResult(Collections.emptyList()), license, authors, keywords,
                export, repository, ballerinaVersion, visibility, template);
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
        return Optional.ofNullable(compilerPluginDesc);
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

    public List<Dependency> dependencies() {
        return dependencies;
    }

    public String ballerinaVersion() {
        return ballerinaVersion;
    }

    public String visibility() {
        return visibility;
    }

    public DiagnosticResult diagnostics() {
        return diagnostics;
    }

    public boolean template() {
        return template;
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
    public static class Dependency {
        private final PackageName packageName;
        private final PackageOrg packageOrg;
        private final PackageVersion version;
        private final String repository;

        public Dependency(PackageName packageName, PackageOrg packageOrg, PackageVersion version) {
            this.packageName = packageName;
            this.packageOrg = packageOrg;
            this.version = version;
            this.repository = null;
        }

        public Dependency(PackageName packageName, PackageOrg packageOrg, PackageVersion version,
                          String repository) {
            this.packageName = packageName;
            this.packageOrg = packageOrg;
            this.version = version;
            this.repository = repository;
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
