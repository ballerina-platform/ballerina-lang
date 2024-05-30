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
import io.ballerina.projects.internal.model.BalToolDescriptor;
import io.ballerina.projects.internal.model.CompilerPluginDescriptor;
import io.ballerina.toml.api.Toml;
import io.ballerina.toml.semantic.ast.TomlTableNode;
import io.ballerina.toml.semantic.diagnostics.TomlNodeLocation;
import io.ballerina.tools.diagnostics.Location;

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
    private final BalToolDescriptor balToolDesc;
    private final Map<String, Platform> platforms;
    private final List<Tool> tools;
    private final List<Dependency> dependencies;
    private final DiagnosticResult diagnostics;
    private final List<String> license;
    private final List<String> authors;
    private final List<String> keywords;
    private final String repository;
    private final List<String> exportedModules;
    private final List<String> includes;
    private final String ballerinaVersion;
    private final String visibility;
    private boolean template;
    private final String icon;

    // Other entries hold other key/value pairs available in the Ballerina.toml file.
    // These keys are not part of the Ballerina package specification.
    private final Map<String, Object> otherEntries;

    private PackageManifest(PackageDescriptor packageDesc,
                            CompilerPluginDescriptor compilerPluginDesc,
                            BalToolDescriptor balToolDesc,
                            Map<String, Platform> platforms,
                            List<Dependency> dependencies,
                            Map<String, Object> otherEntries,
                            DiagnosticResult diagnostics) {
        this.packageDesc = packageDesc;
        this.compilerPluginDesc = compilerPluginDesc;
        this.balToolDesc = balToolDesc;
        this.platforms = Collections.unmodifiableMap(platforms);
        this.dependencies = Collections.unmodifiableList(dependencies);
        this.otherEntries = Collections.unmodifiableMap(otherEntries);
        this.diagnostics = diagnostics;
        this.license = Collections.emptyList();
        this.authors = Collections.emptyList();
        this.keywords = Collections.emptyList();
        this.exportedModules = Collections.emptyList();
        this.includes = Collections.emptyList();
        this.repository = "";
        this.ballerinaVersion = "";
        this.visibility = "";
        this.icon = "";
        this.tools = Collections.emptyList();
    }

    private PackageManifest(PackageDescriptor packageDesc,
                            CompilerPluginDescriptor compilerPluginDesc,
                            BalToolDescriptor balToolDesc,
                            Map<String, Platform> platforms,
                            List<Dependency> dependencies,
                            Map<String, Object> otherEntries,
                            List<Tool> tools,
                            DiagnosticResult diagnostics) {
        this.packageDesc = packageDesc;
        this.compilerPluginDesc = compilerPluginDesc;
        this.balToolDesc = balToolDesc;
        this.platforms = Collections.unmodifiableMap(platforms);
        this.dependencies = Collections.unmodifiableList(dependencies);
        this.otherEntries = Collections.unmodifiableMap(otherEntries);
        this.diagnostics = diagnostics;
        this.license = Collections.emptyList();
        this.authors = Collections.emptyList();
        this.keywords = Collections.emptyList();
        this.exportedModules = Collections.emptyList();
        this.includes = Collections.emptyList();
        this.repository = "";
        this.ballerinaVersion = "";
        this.visibility = "";
        this.icon = "";
        this.tools = Collections.unmodifiableList(tools);
    }

    private PackageManifest(PackageDescriptor packageDesc,
                            CompilerPluginDescriptor compilerPluginDesc,
                            BalToolDescriptor balToolDesc,
                            Map<String, Platform> platforms,
                            List<Dependency> dependencies,
                            Map<String, Object> otherEntries,
                            DiagnosticResult diagnostics,
                            List<String> license,
                            List<String> authors,
                            List<String> keywords,
                            List<String> exportedModules,
                            List<String> includes,
                            String repository,
                            String ballerinaVersion,
                            String visibility,
                            boolean template,
                            String icon) {
        this.packageDesc = packageDesc;
        this.compilerPluginDesc = compilerPluginDesc;
        this.balToolDesc = balToolDesc;
        this.platforms = Collections.unmodifiableMap(platforms);
        this.dependencies = Collections.unmodifiableList(dependencies);
        this.otherEntries = Collections.unmodifiableMap(otherEntries);
        this.diagnostics = diagnostics;
        this.license = license;
        this.authors = authors;
        this.keywords = keywords;
        this.exportedModules = getExport(packageDesc, exportedModules);
        this.includes = includes;
        this.repository = repository;
        this.ballerinaVersion = ballerinaVersion;
        this.visibility = visibility;
        this.template = template;
        this.icon = icon;
        this.tools = Collections.emptyList();
    }

    private PackageManifest(PackageDescriptor packageDesc,
                            CompilerPluginDescriptor compilerPluginDesc,
                            BalToolDescriptor balToolDesc,
                            Map<String, Platform> platforms,
                            List<Dependency> dependencies,
                            Map<String, Object> otherEntries,
                            DiagnosticResult diagnostics,
                            List<String> license,
                            List<String> authors,
                            List<String> keywords,
                            List<String> exportedModules,
                            List<String> includes,
                            String repository,
                            String ballerinaVersion,
                            String visibility,
                            boolean template,
                            String icon,
                            List<Tool> tools) {
        this.packageDesc = packageDesc;
        this.compilerPluginDesc = compilerPluginDesc;
        this.balToolDesc = balToolDesc;
        this.platforms = Collections.unmodifiableMap(platforms);
        this.dependencies = Collections.unmodifiableList(dependencies);
        this.otherEntries = Collections.unmodifiableMap(otherEntries);
        this.diagnostics = diagnostics;
        this.license = license;
        this.authors = authors;
        this.keywords = keywords;
        this.exportedModules = getExport(packageDesc, exportedModules);
        this.includes = includes;
        this.repository = repository;
        this.ballerinaVersion = ballerinaVersion;
        this.visibility = visibility;
        this.template = template;
        this.icon = icon;
        this.tools = tools;
    }
    public static PackageManifest from(PackageDescriptor packageDesc) {
        return new PackageManifest(packageDesc, null, null, Collections.emptyMap(), Collections.emptyList(),
                                   Collections.emptyMap(), Collections.emptyList(),
                                    new DefaultDiagnosticResult(Collections.emptyList()));
    }

    public static PackageManifest from(PackageDescriptor packageDesc,
                                       CompilerPluginDescriptor compilerPluginDesc,
                                       BalToolDescriptor balToolDesc,
                                       Map<String, Platform> platforms,
                                       List<Dependency> dependencies) {
        return new PackageManifest(packageDesc, compilerPluginDesc, balToolDesc, platforms, dependencies,
                Collections.emptyMap(), Collections.emptyList(), new DefaultDiagnosticResult(Collections.emptyList()));
    }

    public static PackageManifest from(PackageDescriptor packageDesc,
                                       CompilerPluginDescriptor compilerPluginDesc,
                                       BalToolDescriptor balToolDesc,
                                       Map<String, Platform> platforms,
                                       List<Dependency> dependencies,
                                       Map<String, Object> otherEntries,
                                       DiagnosticResult diagnostics,
                                       List<String> license,
                                       List<String> authors,
                                       List<String> keywords,
                                       List<String> export,
                                       List<String> include,
                                       String repository,
                                       String ballerinaVersion,
                                       String visibility,
                                       boolean template,
                                       String icon,
                                       List<Tool> tools) {
        return new PackageManifest(packageDesc, compilerPluginDesc, balToolDesc, platforms, dependencies, otherEntries,
                diagnostics, license, authors, keywords, export, include, repository, ballerinaVersion, visibility,
                template, icon, tools);
    }

    public static PackageManifest from(PackageDescriptor packageDesc,
                                       CompilerPluginDescriptor compilerPluginDesc,
                                       BalToolDescriptor balToolDescriptor,
                                       Map<String, Platform> platforms,
                                       List<Dependency> dependencies,
                                       List<String> license,
                                       List<String> authors,
                                       List<String> keywords,
                                       List<String> export,
                                       List<String> include,
                                       String repository,
                                       String ballerinaVersion,
                                       String visibility,
                                       boolean template) {
        return new PackageManifest(packageDesc, compilerPluginDesc, balToolDescriptor, platforms, dependencies,
                Collections.emptyMap(), new DefaultDiagnosticResult(Collections.emptyList()), license, authors,
                keywords, export, include, repository, ballerinaVersion, visibility, template, "");
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

    public Optional<BalToolDescriptor> balToolDescriptor() {
        return Optional.ofNullable(balToolDesc);
    }

    public Platform platform(String platformCode) {
        return platforms.get(platformCode);
    }

    public Map<String, Platform> platforms() {
        return platforms;
    }
    public List<Tool> tools() {
        return tools;
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

    public List<String> includes() {
        return includes;
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

    public String icon() {
        return icon;
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
        private final Boolean graalvmCompatible;

        public Platform(List<Map<String, Object>> dependencies) {
            this(dependencies, Collections.emptyList(), null);
        }

        public Platform(List<Map<String, Object>> dependencies, List<Map<String, Object>> repositories,
                        Boolean graalvmCompatible) {
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
            this.graalvmCompatible = graalvmCompatible;
        }

        public List<Map<String, Object>> dependencies() {
            return dependencies;
        }

        public List<Map<String, Object>> repositories() {
            return repositories;
        }

        public Boolean graalvmCompatible() {
            return graalvmCompatible;
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
        private final Location location;

        public Dependency(PackageName packageName, PackageOrg packageOrg, PackageVersion version) {
            this.packageName = packageName;
            this.packageOrg = packageOrg;
            this.version = version;
            this.location = null;
            this.repository = null;
        }

        public Dependency(PackageName packageName, PackageOrg packageOrg, PackageVersion version,
                          String repository) {
            this.packageName = packageName;
            this.packageOrg = packageOrg;
            this.version = version;
            this.repository = repository;
            this.location = null;
        }

        public Dependency(PackageName packageName, PackageOrg packageOrg, PackageVersion version,
                          String repository, Location location) {
            this.packageName = packageName;
            this.packageOrg = packageOrg;
            this.version = version;
            this.repository = repository;
            this.location = location;
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

        public Optional<Location> location() {
            return Optional.ofNullable(location);
        }
    }

    /**
     * Represents the build tool configurations in Ballerina.toml file.
     *
     * @since 2201.9.0
     */
    public static class Tool {
        private final Field id;
        private final TomlTableNode optionsTable;
        private final Field filePath;
        private final Field targetModule;
        private final Field type;
        private final Toml optionsToml;
        private final boolean hasErrorDiagnostic;

        public Tool(Field type, Field id, Field filePath, Field targetModule, Toml optionsToml,
                    TomlTableNode optionsTable, boolean hasErrorDiagnostic) {
            this.type = type;
            this.id = id;
            this.filePath = filePath;
            this.targetModule = targetModule;
            this.optionsTable = optionsTable;
            this.optionsToml = optionsToml;
            this.hasErrorDiagnostic = hasErrorDiagnostic;
        }

        /**
         * Returns the tool id.
         *
         * @return the tool id.
         */
        public Field id() {
            return id;
        }

        /**
         * Returns the filepath.
         *
         * @return the filepath.
         */
        public Field filePath() {
            return this.filePath;
        }

        /**
         * Returns the target module.
         *
         * @return the tool's target module.
         */
        public Field targetModule() {
            return this.targetModule;
        }

        /**
         * Returns the tool-specific options as a TomlTableNode.
         *
         * @return the tool options table.
         */
        public TomlTableNode optionsTable() {
            return this.optionsTable;
        }

        /**
         * Returns the type of the tool.
         *
         * @return the tool type.
         */
        public Field type() {
            return type;
        }

        /**
         * Returns the tool-specific options as a Toml.
         *
         * @return the options toml.
         */
        public Toml optionsToml() {
            return optionsToml;
        }

        /**
         * Returns a flag indicating whether the tool has error diagnostics.
         *
         * @return whether the tool has error diagnostics.
         */
        public boolean hasErrorDiagnostic() {
            return hasErrorDiagnostic;
        }

        public record Field(String value, TomlNodeLocation location) {
        }
    }

    private List<String> getExport(PackageDescriptor packageDesc, List<String> export) {
        if (export == null || export.isEmpty()) {
            return Collections.singletonList(packageDesc.name().value());
        }
        return export;
    }
}
