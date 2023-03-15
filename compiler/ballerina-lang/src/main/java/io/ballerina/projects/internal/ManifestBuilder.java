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

package io.ballerina.projects.internal;

import io.ballerina.projects.BuildOptions;
import io.ballerina.projects.DiagnosticResult;
import io.ballerina.projects.PackageDescriptor;
import io.ballerina.projects.PackageManifest;
import io.ballerina.projects.PackageName;
import io.ballerina.projects.PackageOrg;
import io.ballerina.projects.PackageVersion;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.SemanticVersion;
import io.ballerina.projects.TomlDocument;
import io.ballerina.projects.internal.model.CompilerPluginDescriptor;
import io.ballerina.projects.util.FileUtils;
import io.ballerina.projects.util.ProjectUtils;
import io.ballerina.toml.semantic.TomlType;
import io.ballerina.toml.semantic.ast.TomlArrayValueNode;
import io.ballerina.toml.semantic.ast.TomlBooleanValueNode;
import io.ballerina.toml.semantic.ast.TomlKeyValueNode;
import io.ballerina.toml.semantic.ast.TomlStringValueNode;
import io.ballerina.toml.semantic.ast.TomlTableArrayNode;
import io.ballerina.toml.semantic.ast.TomlTableNode;
import io.ballerina.toml.semantic.ast.TomlValueNode;
import io.ballerina.toml.semantic.ast.TopLevelNode;
import io.ballerina.toml.semantic.diagnostics.TomlDiagnostic;
import io.ballerina.toml.validator.TomlValidator;
import io.ballerina.toml.validator.schema.Schema;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticInfo;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import org.ballerinalang.compiler.CompilerOptionName;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.ballerina.projects.internal.ManifestUtils.convertDiagnosticToString;
import static io.ballerina.projects.internal.ManifestUtils.getStringFromTomlTableNode;
import static io.ballerina.projects.util.ProjectUtils.defaultName;
import static io.ballerina.projects.util.ProjectUtils.defaultOrg;
import static io.ballerina.projects.util.ProjectUtils.defaultVersion;

/**
 * Build Manifest using toml files.
 *
 * @since 2.0.0
 */
public class ManifestBuilder {

    private final TomlDocument ballerinaToml;
    private final TomlDocument compilerPluginToml;
    private DiagnosticResult diagnostics;
    private final List<Diagnostic> diagnosticList;
    private final PackageManifest packageManifest;
    private final BuildOptions buildOptions;
    private final Path projectPath;

    private static final String PACKAGE = "package";
    private static final String VERSION = "version";
    private static final String LICENSE = "license";
    private static final String AUTHORS = "authors";
    private static final String REPOSITORY = "repository";
    private static final String KEYWORDS = "keywords";
    private static final String EXPORT = "export";
    private static final String INCLUDE = "include";
    private static final String PLATFORM = "platform";
    private static final String SCOPE = "scope";
    private static final String TEMPLATE = "template";
    public static final String ICON = "icon";
    public static final String DISTRIBUTION = "distribution";
    public static final String VISIBILITY = "visibility";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String URL = "url";
    private static final String DEPENDENCY = "dependency";
    private static final String ID = "id";

    private ManifestBuilder(TomlDocument ballerinaToml,
                            TomlDocument compilerPluginToml,
                            Path projectPath) {
        this.projectPath = projectPath;
        this.ballerinaToml = ballerinaToml;
        this.compilerPluginToml = compilerPluginToml;
        this.diagnosticList = new ArrayList<>();
        this.packageManifest = parseAsPackageManifest();
        this.buildOptions = parseBuildOptions();
    }

    public static ManifestBuilder from(TomlDocument ballerinaToml,
                                       TomlDocument compilerPluginToml,
                                       Path projectPath) {
        return new ManifestBuilder(ballerinaToml, compilerPluginToml, projectPath);
    }

    public DiagnosticResult diagnostics() {
        if (diagnostics != null) {
            return diagnostics;
        }

        // Add toml syntax diagnostics
        this.diagnosticList.addAll(ballerinaToml.toml().diagnostics());
        diagnostics = new DefaultDiagnosticResult(this.diagnosticList);
        return diagnostics;
    }

    public String getErrorMessage() {
        StringBuilder message = new StringBuilder();
        message.append("Ballerina.toml contains errors\n");
        for (Diagnostic diagnostic : diagnostics().errors()) {
            message.append(convertDiagnosticToString(diagnostic));
            message.append("\n");
        }
        return message.toString();
    }

    public PackageManifest packageManifest() {
        return this.packageManifest;
    }

    public BuildOptions buildOptions() {
        return this.buildOptions;
    }

    private PackageManifest parseAsPackageManifest() {
        TomlValidator ballerinaTomlValidator;
        try {
            ballerinaTomlValidator = new TomlValidator(
                    Schema.from(FileUtils.readFileAsString("ballerina-toml-schema.json")));
        } catch (IOException e) {
            throw new ProjectException("Failed to read the Ballerina.toml validator schema file.");
        }

        // Validate ballerinaToml using ballerina toml schema
        ballerinaTomlValidator.validate(ballerinaToml.toml());

        TomlTableNode tomlAstNode = ballerinaToml.toml().rootNode();
        PackageDescriptor packageDescriptor = getPackageDescriptor(tomlAstNode);

        List<String> license = Collections.emptyList();
        List<String> authors = Collections.emptyList();
        List<String> keywords = Collections.emptyList();
        List<String> exported = Collections.emptyList();
        List<String> includes = Collections.emptyList();
        String repository = "";
        String ballerinaVersion = "";
        String visibility = "";
        boolean template = false;
        String icon = "";

        if (!tomlAstNode.entries().isEmpty()) {
            TopLevelNode topLevelPkgNode = tomlAstNode.entries().get(PACKAGE);
            if (topLevelPkgNode != null && topLevelPkgNode.kind() == TomlType.TABLE) {
                TomlTableNode pkgNode = (TomlTableNode) topLevelPkgNode;
                license = getStringArrayFromPackageNode(pkgNode, LICENSE);
                authors = getStringArrayFromPackageNode(pkgNode, AUTHORS);
                keywords = getStringArrayFromPackageNode(pkgNode, KEYWORDS);
                exported = getStringArrayFromPackageNode(pkgNode, EXPORT);
                includes = getStringArrayFromPackageNode(pkgNode, INCLUDE);
                repository = getStringValueFromTomlTableNode(pkgNode, REPOSITORY, "");
                ballerinaVersion = getStringValueFromTomlTableNode(pkgNode, DISTRIBUTION, "");
                visibility = getStringValueFromTomlTableNode(pkgNode, VISIBILITY, "");
                template = getBooleanFromTemplateNode(pkgNode, TEMPLATE);
                icon = getStringValueFromTomlTableNode(pkgNode, ICON, "");

                // we ignore file types except png here, since file type error will be shown
                validateIconPathForPng(icon, pkgNode);
            }
        }

        // Do not mutate toml tree
        Map<String, Object> otherEntries = new HashMap<>();
        if (!tomlAstNode.entries().isEmpty()) {
            Map<String, Object> tomlMap = ballerinaToml.toml().toMap();
            for (Map.Entry<String, Object> entry : tomlMap.entrySet()) {
                if (entry.getKey().equals(PACKAGE) || entry.getKey().equals(PLATFORM)) {
                    continue;
                }
                otherEntries.put(entry.getKey(), entry.getValue());
            }
        }

        // Process platforms
        TopLevelNode platformNode = tomlAstNode.entries().get(PLATFORM);
        Map<String, PackageManifest.Platform> platforms = getPlatforms(platformNode);

        // Process local repo dependencies
        List<PackageManifest.Dependency> localRepoDependencies = getLocalRepoDependencies();

        // Compiler plugin descriptor
        CompilerPluginDescriptor pluginDescriptor = null;
        if (this.compilerPluginToml != null) {
            pluginDescriptor = CompilerPluginDescriptor.from(this.compilerPluginToml);
        }

        return PackageManifest.from(packageDescriptor, pluginDescriptor, platforms, localRepoDependencies, otherEntries,
                diagnostics(), license, authors, keywords, exported, includes, repository, ballerinaVersion, visibility,
                template, icon);
    }

    private PackageDescriptor getPackageDescriptor(TomlTableNode tomlTableNode) {
        // set defaults
        String org;
        String name;
        String version;

        String errorMessage = "missing table '[package]' in 'Ballerina.toml'. Defaulting to:\n" +
                "[package]\n" +
                "org = \"" + defaultOrg().value() + "\"\n" +
                "name = \"" + defaultName(this.projectPath).value() + "\"\n" +
                "version = \"" + defaultVersion().value().toString() + "\"";

        if (tomlTableNode.entries().isEmpty()) {
            reportDiagnostic(tomlTableNode, errorMessage,
                    ProjectDiagnosticErrorCode.MISSING_PKG_INFO_IN_BALLERINA_TOML.diagnosticId(),
                    DiagnosticSeverity.WARNING);
            return PackageDescriptor.from(defaultOrg(), defaultName(this.projectPath), defaultVersion());
        }

        TopLevelNode topLevelPkgNode = tomlTableNode.entries().get(PACKAGE);
        if (topLevelPkgNode == null || topLevelPkgNode.kind() != TomlType.TABLE) {
            reportDiagnostic(tomlTableNode, errorMessage,
                    ProjectDiagnosticErrorCode.MISSING_PKG_INFO_IN_BALLERINA_TOML.diagnosticId(),
                    DiagnosticSeverity.WARNING);
            return PackageDescriptor.from(defaultOrg(), defaultName(this.projectPath), defaultVersion());
        }

        TomlTableNode pkgNode = (TomlTableNode) topLevelPkgNode;

        org = getStringValueFromTomlTableNode(pkgNode, "org");
        if (org == null) {
            org = defaultOrg().value();
            reportDiagnostic(pkgNode, "missing key 'org' in table '[package]' in 'Ballerina.toml'. " +
                            "Defaulting to 'org = \"" + org + "\"'",
                    ProjectDiagnosticErrorCode.MISSING_PKG_INFO_IN_BALLERINA_TOML.diagnosticId(),
                    DiagnosticSeverity.WARNING);
        }
        name = getStringValueFromTomlTableNode(pkgNode, "name");
        if (name == null) {
            name = defaultName(this.projectPath).value();
            reportDiagnostic(pkgNode, "missing key 'name' in table '[package]' in 'Ballerina.toml'. " +
                            "Defaulting to 'name = \"" + name + "\"'",
                    ProjectDiagnosticErrorCode.MISSING_PKG_INFO_IN_BALLERINA_TOML.diagnosticId(),
                    DiagnosticSeverity.WARNING);
        }
        version = getStringValueFromTomlTableNode(pkgNode, VERSION);
        if (version == null) {
            version = defaultVersion().value().toString();
            reportDiagnostic(pkgNode, "missing key 'version' in table '[package]' in 'Ballerina.toml'. " +
                            "Defaulting to 'version = \"" + version + "\"'",
                    ProjectDiagnosticErrorCode.MISSING_PKG_INFO_IN_BALLERINA_TOML.diagnosticId(),
                    DiagnosticSeverity.WARNING);
        }

        // check org is valid identifier
        boolean isValidOrg = ProjectUtils.validateOrgName(org);
        if (!isValidOrg) {
            org = defaultOrg().value();
        }

        // check that the package name is valid
        boolean isValidPkg = ProjectUtils.validatePackageName(org, name);
        if (!isValidPkg) {
            name = defaultName(this.projectPath).value();
        }

        // check version is compatible with semver
        try {
            SemanticVersion.from(version);
        } catch (ProjectException e) {
            version = defaultVersion().value().toString();
        }

        return PackageDescriptor.from(PackageOrg.from(org), PackageName.from(name), PackageVersion.from(version));
    }

    private void validateIconPathForPng(String icon, TomlTableNode pkgNode) {
        if (icon != null && hasPngExtension(icon)) {
            Path iconPath = Paths.get(icon);
            if (!iconPath.isAbsolute()) {
                iconPath = this.projectPath.resolve(iconPath);
            }

            if (Files.notExists(iconPath)) {
                // validate icon path
                // if file path does not exist, throw this error
                reportDiagnostic(pkgNode.entries().get(ICON),
                        "could not locate icon path '" + icon + "'",
                        "error.invalid.path", DiagnosticSeverity.ERROR);
            } else {
                // validate file content
                // if other file types renamed as png, throw this error
                try {
                    if (!FileUtils.isValidPng(iconPath)) {
                        reportDiagnostic(pkgNode.entries().get("icon"),
                                "invalid 'icon' under [package]: 'icon' can only have 'png' images",
                                "error.invalid.icon", DiagnosticSeverity.ERROR);
                    }
                } catch (IOException e) {
                    // should not reach to this line
                    throw new ProjectException("failed to read icon: '" + icon + "'");
                }
            }
        }
    }

    private BuildOptions parseBuildOptions() {
        TomlTableNode tomlTableNode = ballerinaToml.toml().rootNode();
        return setBuildOptions(tomlTableNode);
    }

    private Map<String, PackageManifest.Platform> getPlatforms(TopLevelNode platformNode) {
        Map<String, PackageManifest.Platform> platforms = new HashMap<>();
        if (platformNode == null || platformNode.kind() == TomlType.NONE) {
            platforms = Collections.emptyMap();
        } else {
            if (platformNode.kind() == TomlType.TABLE) {
                TomlTableNode platformTable = (TomlTableNode) platformNode;
                Set<String> platformCodes = platformTable.entries().keySet();
                if (platformCodes.size() != 1) {
                    return platforms;
                }

                String platformCode = platformCodes.stream().findFirst().get();
                TopLevelNode platformCodeNode = platformTable.entries().get(platformCode);

                if (platformCodeNode.kind() == TomlType.TABLE) {
                    TomlTableNode platformCodeTable = (TomlTableNode) platformCodeNode;
                    TopLevelNode topLevelNode = platformCodeTable.entries().get(DEPENDENCY);
                    if (topLevelNode != null) {
                        if (topLevelNode.kind() == TomlType.NONE) {
                            return platforms;
                        }
                        PackageManifest.Platform newPlatform = getDependencyPlatform(topLevelNode);
                        if (platforms.get(platformCode) != null) {
                            newPlatform = new PackageManifest.Platform(newPlatform.dependencies(),
                                    platforms.get(platformCode).repositories());
                        }
                        platforms.put(platformCode, newPlatform);
                    }
                    topLevelNode = platformCodeTable.entries().get(REPOSITORY);
                    if (topLevelNode != null) {
                        if (topLevelNode.kind() == TomlType.NONE) {
                            return platforms;
                        }
                        PackageManifest.Platform platform = getRepositoryPlatform(topLevelNode);
                        if (platforms.get(platformCode) != null) {
                            platform = new PackageManifest.Platform(platforms.get(platformCode)
                                    .dependencies(), platform.repositories());
                        }
                        platforms.put(platformCode, platform);
                    }
                }
            }
        }
        return platforms;
    }

    private PackageManifest.Platform getRepositoryPlatform(TopLevelNode dependencyNode) {
        PackageManifest.Platform platform = null;
        if (dependencyNode.kind() == TomlType.TABLE_ARRAY) {
            TomlTableArrayNode dependencyTableArray = (TomlTableArrayNode) dependencyNode;
            List<TomlTableNode> children = dependencyTableArray.children();
            if (!children.isEmpty()) {
                List<Map<String, Object>> platformEntry = new ArrayList<>();
                for (TomlTableNode platformEntryTable : children) {
                    Map<String, Object> platformEntryMap = new HashMap<>();
                    String username = getStringValueFromPlatformEntry(platformEntryTable, USERNAME);
                    String password = getStringValueFromPlatformEntry(platformEntryTable, PASSWORD);
                    platformEntryMap.put(ID, getStringValueFromPlatformEntry(platformEntryTable, ID));
                    platformEntryMap.put(URL, getStringValueFromPlatformEntry(platformEntryTable, URL));
                    if (username != null) {
                        platformEntryMap.put(USERNAME, username);
                    }
                    if (password != null) {
                        platformEntryMap.put(PASSWORD, password);
                    }
                    platformEntry.add(platformEntryMap);
                }
                platform = new PackageManifest.Platform(Collections.emptyList(), platformEntry);
            }
        }
        return platform;
    }

    private PackageManifest.Platform getDependencyPlatform(TopLevelNode dependencyNode) {
        PackageManifest.Platform platform = null;
        if (dependencyNode.kind() == TomlType.TABLE_ARRAY) {
            TomlTableArrayNode dependencyTableArray = (TomlTableArrayNode) dependencyNode;
            List<TomlTableNode> children = dependencyTableArray.children();
            if (!children.isEmpty()) {
                List<Map<String, Object>> platformEntry = new ArrayList<>();
                for (TomlTableNode platformEntryTable : children) {
                    if (!platformEntryTable.entries().isEmpty()) {
                        Map<String, Object> platformEntryMap = new HashMap<>();
                        String pathValue = getStringValueFromPlatformEntry(platformEntryTable, "path");
                        if (pathValue != null) {
                            Path path = Paths.get(pathValue);
                            if (!path.isAbsolute()) {
                                path = this.projectPath.resolve(path);
                            }
                            if (Files.notExists(path)) {
                                reportDiagnostic(platformEntryTable.entries().get("path"),
                                        "could not locate dependency path '" + pathValue + "'",
                                        "error.invalid.path", DiagnosticSeverity.ERROR);
                            }
                        }
                        platformEntryMap.put("path",
                                pathValue);
                        platformEntryMap.put("groupId",
                                getStringValueFromPlatformEntry(platformEntryTable, "groupId"));
                        platformEntryMap.put("artifactId",
                                getStringValueFromPlatformEntry(platformEntryTable, "artifactId"));
                        platformEntryMap.put(VERSION,
                                getStringValueFromPlatformEntry(platformEntryTable, VERSION));
                        platformEntryMap.put(SCOPE,
                                getStringValueFromPlatformEntry(platformEntryTable, SCOPE));
                        platformEntry.add(platformEntryMap);
                    }
                }
                platform = new PackageManifest.Platform(platformEntry);
            }
        }
        return platform;
    }

    private List<PackageManifest.Dependency> getLocalRepoDependencies() {
        TomlTableNode rootNode = ballerinaToml.toml().rootNode();
        if (rootNode.entries().isEmpty()) {
            return Collections.emptyList();
        }

        TopLevelNode dependencyEntries = rootNode.entries().get("dependency");
        if (dependencyEntries == null || dependencyEntries.kind() == TomlType.NONE) {
            return Collections.emptyList();
        }

        List<PackageManifest.Dependency> dependencies = new ArrayList<>();
        if (dependencyEntries.kind() == TomlType.TABLE_ARRAY) {
            TomlTableArrayNode dependencyTableArray = (TomlTableArrayNode) dependencyEntries;

            for (TomlTableNode dependencyNode : dependencyTableArray.children()) {
                String name = getStringValueFromDependencyNode(dependencyNode, "name");
                String org = getStringValueFromDependencyNode(dependencyNode, "org");
                String version = getStringValueFromDependencyNode(dependencyNode, VERSION);
                String repository = getStringValueFromDependencyNode(dependencyNode, REPOSITORY);

                PackageName depName = PackageName.from(name);
                PackageOrg depOrg = PackageOrg.from(org);
                PackageVersion depVersion;
                try {
                    depVersion = PackageVersion.from(version);
                } catch (ProjectException e) {
                    // Ignore exception and dependency
                    // Diagnostic will be added by toml schema validator for the semver version error
                    continue;
                }

                dependencies.add(new PackageManifest.Dependency(
                        depName, depOrg, depVersion, repository, dependencyNode.location()));
            }
        }
        return dependencies;
    }

    private void reportDiagnostic(TopLevelNode tomlTableNode,
                                  String message,
                                  String messageFormat,
                                  DiagnosticSeverity severity) {
        DiagnosticInfo diagnosticInfo =
                new DiagnosticInfo(null, messageFormat, severity);
        TomlDiagnostic tomlDiagnostic = new TomlDiagnostic(
                tomlTableNode.location(),
                diagnosticInfo,
                message);
        tomlTableNode.addDiagnostic(tomlDiagnostic);
    }

    private BuildOptions setBuildOptions(TomlTableNode tomlTableNode) {
        TopLevelNode topLevelBuildOptionsNode = tomlTableNode.entries().get("build-options");
        if (topLevelBuildOptionsNode == null || topLevelBuildOptionsNode.kind() != TomlType.TABLE) {
            return null;
        }
        TomlTableNode tableNode = (TomlTableNode) topLevelBuildOptionsNode;

        BuildOptions.BuildOptionsBuilder buildOptionsBuilder = BuildOptions.builder();

        Boolean offline = getBooleanFromBuildOptionsTableNode(tableNode, CompilerOptionName.OFFLINE.toString());
        Boolean observabilityIncluded =
                getBooleanFromBuildOptionsTableNode(tableNode, CompilerOptionName.OBSERVABILITY_INCLUDED.toString());
        Boolean testReport =
                getBooleanFromBuildOptionsTableNode(tableNode, BuildOptions.OptionName.TEST_REPORT.toString());
        Boolean codeCoverage =
                getBooleanFromBuildOptionsTableNode(tableNode, BuildOptions.OptionName.CODE_COVERAGE.toString());
        final TopLevelNode topLevelNode = tableNode.entries().get(CompilerOptionName.CLOUD.toString());
        Boolean dumpBuildTime =
                getBooleanFromBuildOptionsTableNode(tableNode, BuildOptions.OptionName.DUMP_BUILD_TIME.toString());
        Boolean sticky =
                getTrueFromBuildOptionsTableNode(tableNode, CompilerOptionName.STICKY.toString());
        String cloud = "";
        if (topLevelNode != null) {
            cloud = getStringFromTomlTableNode(topLevelNode);
        }
        Boolean listConflictedClasses = getBooleanFromBuildOptionsTableNode(tableNode,
                CompilerOptionName.LIST_CONFLICTED_CLASSES.toString());
        String targetDir = getStringFromBuildOptionsTableNode(tableNode,
                BuildOptions.OptionName.TARGET_DIR.toString());
        Boolean enableCache = getBooleanFromBuildOptionsTableNode(tableNode,
                CompilerOptionName.ENABLE_CACHE.toString());
        Boolean nativeImage = getBooleanFromBuildOptionsTableNode(tableNode,
                BuildOptions.OptionName.NATIVE_IMAGE.toString());
        Boolean exportComponentModel = getBooleanFromBuildOptionsTableNode(tableNode,
                BuildOptions.OptionName.EXPORT_COMPONENT_MODEL.toString());

        buildOptionsBuilder
                .setOffline(offline)
                .setObservabilityIncluded(observabilityIncluded)
                .setTestReport(testReport)
                .setCodeCoverage(codeCoverage)
                .setCloud(cloud)
                .setListConflictedClasses(listConflictedClasses)
                .setDumpBuildTime(dumpBuildTime)
                .setSticky(sticky)
                .setEnableCache(enableCache)
                .setNativeImage(nativeImage)
                .setExportComponentModel(exportComponentModel);

        if (targetDir != null) {
            buildOptionsBuilder.targetDir(targetDir);
        }

        return buildOptionsBuilder.build();
    }

    private Boolean getBooleanFromBuildOptionsTableNode(TomlTableNode tableNode, String key) {
        TopLevelNode topLevelNode = tableNode.entries().get(key);
        if (topLevelNode == null || topLevelNode.kind() == TomlType.NONE) {
            return null;
        }

        if (topLevelNode.kind() == TomlType.KEY_VALUE) {
            TomlKeyValueNode keyValueNode = (TomlKeyValueNode) topLevelNode;
            TomlValueNode value = keyValueNode.value();
            if (value.kind() == TomlType.BOOLEAN) {
                TomlBooleanValueNode tomlBooleanValueNode = (TomlBooleanValueNode) value;
                return tomlBooleanValueNode.getValue();
            }
        }
        return null;
    }

    private String getStringFromBuildOptionsTableNode(TomlTableNode tableNode, String key) {
        TopLevelNode topLevelNode = tableNode.entries().get(key);
        if (topLevelNode == null || topLevelNode.kind() == TomlType.NONE) {
            return null;
        }

        if (topLevelNode.kind() == TomlType.KEY_VALUE) {
            TomlKeyValueNode keyValueNode = (TomlKeyValueNode) topLevelNode;
            TomlValueNode value = keyValueNode.value();
            if (value.kind() == TomlType.STRING) {
                TomlStringValueNode tomlStringValueNode = (TomlStringValueNode) value;
                return tomlStringValueNode.getValue();
            }
        }
        return null;
    }

    private boolean getBooleanFromTemplateNode(TomlTableNode tableNode, String key) {
        TopLevelNode topLevelNode = tableNode.entries().get(key);
        if (topLevelNode == null || topLevelNode.kind() == TomlType.NONE) {
            return false;
        }

        if (topLevelNode.kind() == TomlType.KEY_VALUE) {
            TomlKeyValueNode keyValueNode = (TomlKeyValueNode) topLevelNode;
            TomlValueNode value = keyValueNode.value();
            if (value.kind() == TomlType.BOOLEAN) {
                TomlBooleanValueNode tomlBooleanValueNode = (TomlBooleanValueNode) value;
                return tomlBooleanValueNode.getValue();
            }
        }
        return false;
    }

    private boolean getTrueFromBuildOptionsTableNode(TomlTableNode tableNode, String key) {
        TopLevelNode topLevelNode = tableNode.entries().get(key);
        if (topLevelNode == null || topLevelNode.kind() == TomlType.NONE) {
            return true;
        }

        if (topLevelNode.kind() == TomlType.KEY_VALUE) {
            TomlKeyValueNode keyValueNode = (TomlKeyValueNode) topLevelNode;
            TomlValueNode value = keyValueNode.value();
            if (value.kind() == TomlType.BOOLEAN) {
                TomlBooleanValueNode tomlBooleanValueNode = (TomlBooleanValueNode) value;
                return tomlBooleanValueNode.getValue();
            }
        }
        return true;
    }

    public static String getStringValueFromTomlTableNode(TomlTableNode tomlTableNode, String key) {
        TopLevelNode topLevelNode = tomlTableNode.entries().get(key);
        if (topLevelNode == null || topLevelNode.kind() == TomlType.NONE) {
            // return default value
            return null;
        }
        return getStringFromTomlTableNode(topLevelNode);
    }

    public static String getStringValueFromTomlTableNode(TomlTableNode tomlTableNode, String key, String defaultValue) {
        TopLevelNode topLevelNode = tomlTableNode.entries().get(key);
        if (topLevelNode == null || topLevelNode.kind() == TomlType.NONE) {
            // return default value
            return defaultValue;
        }
        String value = getStringFromTomlTableNode(topLevelNode);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

    private List<String> getStringArrayFromPackageNode(TomlTableNode pkgNode, String key) {
        List<String> elements = new ArrayList<>();
        TopLevelNode topLevelNode = pkgNode.entries().get(key);
        if (topLevelNode == null || topLevelNode.kind() == TomlType.NONE) {
            return elements;
        }
        TomlValueNode valueNode = ((TomlKeyValueNode) topLevelNode).value();
        if (valueNode.kind() == TomlType.NONE) {
            return elements;
        }
        if (valueNode.kind() == TomlType.ARRAY) {
            TomlArrayValueNode arrayValueNode = (TomlArrayValueNode) valueNode;
            for (TomlValueNode value : arrayValueNode.elements()) {
                if (value.kind() == TomlType.STRING) {
                    elements.add(((TomlStringValueNode) value).getValue());
                }
            }
        }
        return elements;
    }

    private String getStringValueFromPlatformEntry(TomlTableNode pkgNode, String key) {
        TopLevelNode topLevelNode = pkgNode.entries().get(key);
        if (topLevelNode == null || topLevelNode.kind() == TomlType.NONE) {
            return null;
        }
        return getStringFromTomlTableNode(topLevelNode);
    }

    private String getStringValueFromDependencyNode(TomlTableNode pkgNode, String key) {
        TopLevelNode topLevelNode = pkgNode.entries().get(key);
        if (topLevelNode == null) {
            return null;
        }
        return getStringFromTomlTableNode(topLevelNode);
    }

    /**
     * Check file name has {@code .png} extension.
     *
     * @param fileName file name
     * @return has {@code .png} extension
     */
    private boolean hasPngExtension(String fileName) {
        String pngExtensionPattern = ".*.png$";
        Pattern pattern = Pattern.compile(pngExtensionPattern);
        Matcher matcher = pattern.matcher(fileName);
        return matcher.find();
    }
}
