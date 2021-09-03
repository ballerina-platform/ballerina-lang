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

import io.ballerina.projects.DependencyManifest;
import io.ballerina.projects.DiagnosticResult;
import io.ballerina.projects.PackageName;
import io.ballerina.projects.PackageOrg;
import io.ballerina.projects.PackageVersion;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.TomlDocument;
import io.ballerina.projects.util.FileUtils;
import io.ballerina.toml.semantic.TomlType;
import io.ballerina.toml.semantic.ast.TomlArrayValueNode;
import io.ballerina.toml.semantic.ast.TomlInlineTableValueNode;
import io.ballerina.toml.semantic.ast.TomlKeyValueNode;
import io.ballerina.toml.semantic.ast.TomlTableArrayNode;
import io.ballerina.toml.semantic.ast.TomlTableNode;
import io.ballerina.toml.semantic.ast.TomlValueNode;
import io.ballerina.toml.semantic.ast.TopLevelNode;
import io.ballerina.toml.validator.TomlValidator;
import io.ballerina.toml.validator.schema.Schema;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticFactory;
import io.ballerina.tools.diagnostics.DiagnosticInfo;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import io.ballerina.tools.diagnostics.Location;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static io.ballerina.projects.internal.ManifestUtils.convertDiagnosticToString;
import static io.ballerina.projects.internal.ManifestUtils.getBooleanFromTomlTableNode;
import static io.ballerina.projects.internal.ManifestUtils.getStringFromTomlTableNode;

/**
 * Build Dependency Manifest using Dependencies.toml file.
 *
 * @since 2.0.0
 */
public class DependencyManifestBuilder {

    private final Optional<TomlDocument> dependenciesToml;
    private DiagnosticResult diagnostics;
    private final List<Diagnostic> diagnosticList;
    private final DependencyManifest dependencyManifest;

    private static final String LATEST_DEPS_TOML_VERSION = "2";

    private DependencyManifestBuilder(TomlDocument dependenciesToml) {
        this.dependenciesToml = Optional.ofNullable(dependenciesToml);
        this.diagnosticList = new ArrayList<>();
        this.dependencyManifest = parseAsDependencyManifest();
    }

    public static DependencyManifestBuilder from(TomlDocument dependenciesToml) {
        return new DependencyManifestBuilder(dependenciesToml);
    }

    public static DependencyManifestBuilder from() {
        return new DependencyManifestBuilder(null);
    }

    public DiagnosticResult diagnostics() {
        if (diagnostics != null) {
            return diagnostics;
        }

        // Add toml syntax diagnostics
        dependenciesToml.ifPresent(tomlDocument -> this.diagnosticList.addAll(tomlDocument.toml().diagnostics()));
        diagnostics = new DefaultDiagnosticResult(this.diagnosticList);
        return diagnostics;
    }

    public String getErrorMessage() {
        StringBuilder message = new StringBuilder();
        message.append("Dependencies.toml contains errors\n");
        for (Diagnostic diagnostic : diagnostics().errors()) {
            message.append(convertDiagnosticToString(diagnostic));
            message.append("\n");
        }
        return message.toString();
    }

    public DependencyManifest dependencyManifest() {
        return dependencyManifest;
    }

    private DependencyManifest parseAsDependencyManifest() {
        if (dependenciesToml.isEmpty() || dependenciesToml.get().toml().rootNode().entries().isEmpty()) {
            return DependencyManifest.from(null, Collections.emptyList(), diagnostics());
        }

        // Check `dependencies-toml-version` exists
        String dependenciesTomlVersion = getDependenciesTomlVersion();

        // Latest `Dependencies.toml`
        if (dependenciesTomlVersion != null) {
            validateDependenciesTomlAgainstSchema("dependencies-toml-schema.json");
            List<DependencyManifest.Package> packages = getPackages();
            return DependencyManifest.from(dependenciesTomlVersion, packages, diagnostics());
        }

        // Old `Dependencies.toml`
        reportDiagnostic("Detected an old version of 'Dependencies.toml' file. This will be updated to v2 format.",
                         ProjectDiagnosticErrorCode.OLD_DEPENDENCIES_TOML.diagnosticId(), DiagnosticSeverity.WARNING,
                         dependenciesToml.get().toml().rootNode().location());
        validateDependenciesTomlAgainstSchema("old-dependencies-toml-schema.json");
        List<DependencyManifest.Package> packages = getPackagesFromOldBallerinaToml();
        return DependencyManifest.from(null, packages, diagnostics());
    }

    private void validateDependenciesTomlAgainstSchema(String schemaName) {
        if (dependenciesToml.isPresent()) {
            TomlValidator dependenciesTomlValidator;
            try {
                dependenciesTomlValidator = new TomlValidator(Schema.from(FileUtils.readFileAsString(schemaName)));
            } catch (IOException e) {
                throw new ProjectException("Failed to read the Dependencies.toml validator schema file:" + schemaName);
            }
            // Validate dependencies toml using dependencies toml schema
            dependenciesTomlValidator.validate(dependenciesToml.get().toml());
        }
    }

    private String getDependenciesTomlVersion() {
        if (dependenciesToml.isEmpty()) {
            // When Dependencies.toml does not exists, we consider it is in the latest toml version
            return LATEST_DEPS_TOML_VERSION;
        }

        TomlTableNode tomlTableNode = dependenciesToml.get().toml().rootNode();
        if (tomlTableNode.entries().isEmpty()) {
            return null;
        }

        TopLevelNode ballerinaEntries = tomlTableNode.entries().get("ballerina");
        if (ballerinaEntries == null || ballerinaEntries.kind() == TomlType.NONE) {
            return null;
        }

        if (ballerinaEntries.kind() == TomlType.TABLE) {
            TomlTableNode ballerinaTableNode = (TomlTableNode) ballerinaEntries;
            return getStringValueFromDependencyNode(ballerinaTableNode, "dependencies-toml-version");
        }
        return null;
    }

    private List<DependencyManifest.Package> getPackages() {
        if (dependenciesToml.isEmpty()) {
            return Collections.emptyList();
        }

        TomlTableNode tomlTableNode = dependenciesToml.get().toml().rootNode();
        if (tomlTableNode.entries().isEmpty()) {
            return Collections.emptyList();
        }

        TopLevelNode packageEntries = tomlTableNode.entries().get("package");
        if (packageEntries == null || packageEntries.kind() == TomlType.NONE) {
            return Collections.emptyList();
        }

        List<DependencyManifest.Package> dependencies = new ArrayList<>();
        if (packageEntries.kind() == TomlType.TABLE_ARRAY) {
            TomlTableArrayNode dependencyTableArray = (TomlTableArrayNode) packageEntries;

            for (TomlTableNode dependencyNode : dependencyTableArray.children()) {
                String name = getStringValueFromDependencyNode(dependencyNode, "name");
                String org = getStringValueFromDependencyNode(dependencyNode, "org");
                String version = getStringValueFromDependencyNode(dependencyNode, "version");
                String scope = getStringValueFromDependencyNode(dependencyNode, "scope");
                boolean transitive = getBooleanValueFromDependencyNode(dependencyNode, "transitive");
                List<DependencyManifest.Dependency> transDependencies = getTransDependenciesFromDependencyNode(
                        dependencyNode);
                List<DependencyManifest.Module> modules = getModulesFromDependencyNode(dependencyNode);

                // If name, org or version, one of the value is null, ignore dependency
                if (name == null || org == null || version == null) {
                    continue;
                }

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

                dependencies.add(new DependencyManifest.Package(depName, depOrg, depVersion, scope, transitive,
                                                                transDependencies, modules));
            }
        }
        return dependencies;
    }

    private List<DependencyManifest.Package> getPackagesFromOldBallerinaToml() {
        if (dependenciesToml.isEmpty()) {
            return Collections.emptyList();
        }

        List<DependencyManifest.Package> packages = new ArrayList<>();
        TomlTableNode tomlTableNode = dependenciesToml.get().toml().rootNode();

        if (tomlTableNode.entries().isEmpty()) {
            return Collections.emptyList();
        }

        TopLevelNode dependencyEntries = tomlTableNode.entries().get("dependency");
        if (dependencyEntries == null || dependencyEntries.kind() == TomlType.NONE) {
            return Collections.emptyList();
        }

        if (dependencyEntries.kind() == TomlType.TABLE_ARRAY) {
            TomlTableArrayNode dependencyTableArray = (TomlTableArrayNode) dependencyEntries;

            for (TomlTableNode dependencyNode : dependencyTableArray.children()) {
                String name = getStringValueFromDependencyNode(dependencyNode, "name");
                String org = getStringValueFromDependencyNode(dependencyNode, "org");
                String version = getStringValueFromDependencyNode(dependencyNode, "version");

                // If name, org or version, one of the value is null, ignore dependency
                if (name == null || org == null || version == null) {
                    continue;
                }

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

                // If local packages found in the `Dependencies.toml`
                if (dependencyNode.entries().containsKey("repository")) {
                    String repository = getStringValueFromDependencyNode(dependencyNode, "repository");
                    String message = "Detected local dependency declarations in 'Dependencies.toml' file. "
                            + "Add them to 'Ballerina.toml' using following syntax:\n"
                            + "[[dependency]]\n"
                            + "org = \"" + depOrg + "\"\n"
                            + "name = \"" + depName + "\"\n"
                            + "version = \"" + depVersion + "\"\n"
                            + "repository = \"" + repository + "\"\n";
                    reportDiagnostic(message,
                                     ProjectDiagnosticErrorCode.LOCAL_PACKAGES_IN_DEPENDENCIES_TOML.diagnosticId(),
                                     DiagnosticSeverity.WARNING, dependencyNode.location());
                    continue;
                }
                packages.add(new DependencyManifest.Package(depName, depOrg, depVersion));
            }
        }
        return packages;
    }

    private String getStringValueFromDependencyNode(TomlTableNode pkgNode, String key) {
        TopLevelNode topLevelNode = pkgNode.entries().get(key);
        if (topLevelNode == null) {
            return null;
        }
        return getStringFromTomlTableNode(topLevelNode);
    }

    private boolean getBooleanValueFromDependencyNode(TomlTableNode pkgNode, String key) {
        TopLevelNode topLevelNode = pkgNode.entries().get(key);
        if (topLevelNode == null) {
            return false;
        }
        return getBooleanFromTomlTableNode(topLevelNode);
    }

    private List<DependencyManifest.Dependency> getTransDependenciesFromDependencyNode(TomlTableNode pkgNode) {
        List<DependencyManifest.Dependency> transDependencies = new ArrayList<>();
        var topLevelNode = pkgNode.entries().get("dependencies");
        if (topLevelNode == null) {
            return transDependencies;
        }
        if (topLevelNode.kind() != null && topLevelNode.kind() == TomlType.KEY_VALUE) {
            TomlValueNode transDepsValueNode = ((TomlKeyValueNode) topLevelNode).value();
            if (transDepsValueNode != null && transDepsValueNode.kind() == TomlType.ARRAY) {
                TomlArrayValueNode transDepsArrayValueNode = (TomlArrayValueNode) transDepsValueNode;
                for (TomlValueNode transDepValueNode : transDepsArrayValueNode.elements()) {
                    if (transDepValueNode != null && transDepValueNode.kind() == TomlType.INLINE_TABLE) {
                        String orgValue = null;
                        String nameValue = null;
                        TomlInlineTableValueNode inlineTableNode = (TomlInlineTableValueNode) transDepValueNode;
                        for (TopLevelNode inlineTableEntry : inlineTableNode.elements()) {
                            if (inlineTableEntry != null && inlineTableEntry.kind() == TomlType.KEY_VALUE) {
                                TomlKeyValueNode inlineTableKeyValue = (TomlKeyValueNode) inlineTableEntry;
                                if (inlineTableKeyValue.key().name().equals("org")) {
                                    orgValue = inlineTableKeyValue.value().toString();
                                } else if (inlineTableKeyValue.key().name().equals("name")) {
                                    nameValue = inlineTableKeyValue.value().toString();
                                }
                            }
                        }
                        if (orgValue != null && nameValue != null) {
                            transDependencies.add(new DependencyManifest.Dependency(PackageName.from(nameValue),
                                                                                    PackageOrg.from(orgValue)));
                        }
                    }
                }
            }
        }
        return transDependencies;
    }

    private List<DependencyManifest.Module> getModulesFromDependencyNode(TomlTableNode pkgNode) {
        List<DependencyManifest.Module> modules = new ArrayList<>();
        var topLevelNode = pkgNode.entries().get("modules");
        if (topLevelNode == null) {
            return modules;
        }
        if (topLevelNode.kind() != null && topLevelNode.kind() == TomlType.KEY_VALUE) {
            TomlValueNode transDepsValueNode = ((TomlKeyValueNode) topLevelNode).value();
            if (transDepsValueNode != null && transDepsValueNode.kind() == TomlType.ARRAY) {
                TomlArrayValueNode transDepsArrayValueNode = (TomlArrayValueNode) transDepsValueNode;
                for (TomlValueNode transDepValueNode : transDepsArrayValueNode.elements()) {
                    if (transDepValueNode != null && transDepValueNode.kind() == TomlType.INLINE_TABLE) {
                        String orgValue = null;
                        String pkgNameValue = null;
                        String moduleNameValue = null;
                        TomlInlineTableValueNode inlineTableNode = (TomlInlineTableValueNode) transDepValueNode;
                        for (TopLevelNode inlineTableEntry : inlineTableNode.elements()) {
                            if (inlineTableEntry != null && inlineTableEntry.kind() == TomlType.KEY_VALUE) {
                                TomlKeyValueNode inlineTableKeyValue = (TomlKeyValueNode) inlineTableEntry;
                                if ("org".equals(inlineTableKeyValue.key().name())) {
                                    orgValue = inlineTableKeyValue.value().toString();
                                } else if ("packageName".equals(inlineTableKeyValue.key().name())) {
                                    pkgNameValue = inlineTableKeyValue.value().toString();
                                } else if ("moduleName".equals(inlineTableKeyValue.key().name())) {
                                    moduleNameValue = inlineTableKeyValue.value().toString();
                                }
                            }
                        }
                        if (orgValue != null && pkgNameValue != null && moduleNameValue != null) {
                            modules.add(new DependencyManifest.Module(orgValue, pkgNameValue, moduleNameValue));
                        }
                    }
                }
            }
        }
        return modules;
    }

    void reportDiagnostic(String message, String diagnosticErrorCode, DiagnosticSeverity severity, Location location) {
        var diagnosticInfo = new DiagnosticInfo(diagnosticErrorCode, message, severity);
        var diagnostic = DiagnosticFactory.createDiagnostic(diagnosticInfo, location);
        this.diagnosticList.add(diagnostic);
    }
}
