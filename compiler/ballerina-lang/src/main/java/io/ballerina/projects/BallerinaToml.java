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
import io.ballerina.projects.util.ProjectUtils;
import io.ballerina.toml.semantic.TomlType;
import io.ballerina.toml.semantic.ast.Node;
import io.ballerina.toml.semantic.ast.TomlArrayValueNode;
import io.ballerina.toml.semantic.ast.TomlBooleanValueNode;
import io.ballerina.toml.semantic.ast.TomlKeyValueNode;
import io.ballerina.toml.semantic.ast.TomlStringValueNode;
import io.ballerina.toml.semantic.ast.TomlTableArrayNode;
import io.ballerina.toml.semantic.ast.TomlTableNode;
import io.ballerina.toml.semantic.ast.TomlValueNode;
import io.ballerina.toml.semantic.ast.TopLevelNode;
import io.ballerina.toml.semantic.diagnostics.TomlDiagnostic;
import io.ballerina.toml.semantic.diagnostics.TomlNodeLocation;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticCode;
import io.ballerina.tools.diagnostics.DiagnosticInfo;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import io.ballerina.tools.text.TextRange;
import org.ballerinalang.compiler.CompilerOptionName;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents the 'Ballerina.toml' file in a package.
 *
 * @since 2.0.0
 */
public class BallerinaToml extends TomlDocument {

    private DiagnosticResult diagnostics;
    private List<Diagnostic> diagnosticList;
    private PackageManifest packageManifest;
    private BuildOptions buildOptions;
    private Path filePath;

    private static final String VERSION = "version";
    private static final String LICENSE = "license";
    private static final String AUTHORS = "authors";
    private static final String REPOSITORY = "repository";
    private static final String KEYWORDS = "keywords";

    private BallerinaToml(Path filePath) {
        super(filePath);
        this.filePath = filePath;
        this.diagnosticList = new ArrayList<>();
        this.packageManifest = parseAsPackageManifest();
        this.buildOptions = parseBuildOptions();
    }

    public static BallerinaToml from(Path filePath) {
        return new BallerinaToml(filePath);
    }

    public DiagnosticResult diagnostics() {
        if (diagnostics != null) {
            return diagnostics;
        }

        // Add toml syntax diagnostics
        syntaxTree().diagnostics().forEach(this.diagnosticList::add);
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

    private String convertDiagnosticToString(Diagnostic diagnostic) {
        LineRange lineRange = diagnostic.location().lineRange();

        LineRange oneBasedLineRange = LineRange.from(
                lineRange.filePath(),
                LinePosition.from(lineRange.startLine().line(), lineRange.startLine().offset() + 1),
                LinePosition.from(lineRange.endLine().line(), lineRange.endLine().offset() + 1));

        return diagnostic.diagnosticInfo().severity().toString() + " [" +
                oneBasedLineRange.filePath() + ":" + oneBasedLineRange + "] " + diagnostic.message();
    }

    public PackageManifest packageManifest() {
        return this.packageManifest;
    }

    public BuildOptions buildOptions() {
        return this.buildOptions;
    }

    @SuppressWarnings("unchecked")
    private PackageManifest parseAsPackageManifest() {
        TomlTableNode tomlTableNode = tomlAstNode();

        if (tomlTableNode.entries().isEmpty()) {
            addDiagnostic(null, DiagnosticSeverity.ERROR, tomlTableNode,
                          "invalid Ballerina.toml file: cannot find [package]");
            return null;
        }

        TomlTableNode pkgNode = (TomlTableNode) tomlTableNode.entries().get("package");
        if (pkgNode == null || pkgNode.kind() == TomlType.NONE) {
            addDiagnostic(null, DiagnosticSeverity.ERROR, tomlTableNode,
                          "invalid Ballerina.toml file: cannot find [package]");
            return null;
        }
        if (pkgNode.entries().isEmpty()) {
            addDiagnostic(null, DiagnosticSeverity.ERROR, pkgNode,
                          "invalid Ballerina.toml file: organization, name and the version of the "
                                  + "package is missing. example: \n" + "[package]\n" + "org=\"my_org\"\n"
                                  + "name=\"my_package\"\n" + "version=\"1.0.0\"\n");
            return null;
        }

        String org = getStringValueFromPackageNode(pkgNode, "org");
        String name = getStringValueFromPackageNode(pkgNode, "name");
        String version = getStringValueFromPackageNode(pkgNode, VERSION);

        if (org == null || name == null || version == null) {
            return null;
        }
        if (!validatePackage(pkgNode, org, name, version)) {
            return null;
        }

        PackageDescriptor descriptor = PackageDescriptor.from(PackageOrg.from(org),
                                                              PackageName.from(name),
                                                              PackageVersion.from(version));

        Map<String, TopLevelNode> otherEntries = tomlTableNode.entries();
        otherEntries.remove("package");

        // TODO add package properties which is not available in the `PackageDescriptor` to `otherEntries`
        // TODO we need to fix this properly later
        otherEntries.put(LICENSE, pkgNode.entries().get(LICENSE));
        otherEntries.put(AUTHORS, pkgNode.entries().get(AUTHORS));
        otherEntries.put(REPOSITORY, pkgNode.entries().get(REPOSITORY));
        otherEntries.put(KEYWORDS, pkgNode.entries().get(KEYWORDS));
        validateOtherEntries(otherEntries);

        // Process dependencies
        TopLevelNode dependencyEntries = otherEntries.remove("dependency");
        var dependencies = getDependencies(dependencyEntries);

        // Process platforms
        TopLevelNode platformNode = otherEntries.remove("platform");
        Map<String, PackageManifest.Platform> platforms = getPlatforms(platformNode);

        return PackageManifest.from(descriptor, dependencies, platforms, otherEntries);
    }

    private BuildOptions parseBuildOptions() {
        TomlTableNode tomlTableNode = tomlAstNode();
        return setBuildOptions(tomlTableNode);
    }

    private boolean validatePackage(TomlTableNode pkgNode, String org, String name, String version) {
        // check org is valid identifier
        boolean isValidOrg = ProjectUtils.validateOrgName(org);
        if (!isValidOrg) {
            addDiagnostic(null, DiagnosticSeverity.ERROR, pkgNode,
                          "invalid Ballerina.toml file: Invalid 'org' under [package]: '" + org + "' :\n"
                                  + "'org' can only contain alphanumerics, underscores and periods "
                                  + "and the maximum length is 256 characters");
        }

        // check that the package name is valid
        boolean isValidPkg = ProjectUtils.validatePkgName(name);
        if (!isValidPkg) {
            addDiagnostic(null, DiagnosticSeverity.ERROR, pkgNode,
                          "invalid Ballerina.toml file: Invalid 'name' under [package]: '" + name + "' :\n"
                                  + "'name' can only contain alphanumerics, underscores "
                                  + "and the maximum length is 256 characters");
        }

        // check version is compatible with semver
        SemanticVersion semanticVersion = null;
        try {
            semanticVersion = SemanticVersion.from(version);
        } catch (ProjectException e) {
            addDiagnostic(null, DiagnosticSeverity.ERROR, pkgNode,
                          "invalid package version in Ballerina.toml. " + e.getMessage());
        }

        return isValidOrg && isValidPkg && semanticVersion != null;
    }

    private List<PackageManifest.Dependency> getDependencies(TopLevelNode dependencyEntries) {
        List<PackageManifest.Dependency> dependencies = new ArrayList<>();
        if (dependencyEntries == null || dependencyEntries.kind() == TomlType.NONE) {
            return Collections.emptyList();
        }

        if (dependencyEntries.kind() == TomlType.TABLE_ARRAY) {
            TomlTableArrayNode dependencyTableArray = (TomlTableArrayNode) dependencyEntries;

            for (TomlTableNode dependencyNode : dependencyTableArray.children()) {
                PackageName depName = PackageName.from(getStringValueFromDependencyNode(dependencyNode, "name"));
                PackageOrg depOrg = PackageOrg.from(getStringValueFromDependencyNode(dependencyNode, "org"));
                PackageVersion depVersion = PackageVersion
                        .from(getStringValueFromDependencyNode(dependencyNode, VERSION));

                dependencies.add(new PackageManifest.Dependency(depName, depOrg, depVersion));
            }

        } else {
            addDiagnostic(null, DiagnosticSeverity.ERROR, dependencyEntries,
                          "invalid Ballerina.toml file: 'dependency' should be a table array");
        }
        return dependencies;
    }

    @SuppressWarnings("unchecked")
    private Map<String, PackageManifest.Platform> getPlatforms(TopLevelNode platformNode) {
        Map<String, PackageManifest.Platform> platforms = new HashMap<>();
        if (platformNode == null || platformNode.kind() == TomlType.NONE) {
            platforms = Collections.emptyMap();
        } else {
            if (platformNode.kind() == TomlType.TABLE) {
                TomlTableNode platformTable = (TomlTableNode) platformNode;
                Set<String> platformCodes = platformTable.entries().keySet();
                if (platformCodes.size() != 1) {
                    addDiagnostic(null, DiagnosticSeverity.ERROR, platformTable,
                                  "invalid Ballerina.toml file: cannot find platform code under 'platform'");
                }

                String platformCode = platformCodes.stream().findFirst().get();
                TopLevelNode platformCodeNode = platformTable.entries().get(platformCode);

                if (platformCodeNode.kind() == TomlType.TABLE) {
                    TomlTableNode platformCodeTable = (TomlTableNode) platformCodeNode;
                    TopLevelNode dependencyNode = platformCodeTable.entries().get("dependency");

                    if (dependencyNode.kind() == TomlType.NONE) {
                        addDiagnostic(null, DiagnosticSeverity.ERROR, dependencyNode,
                                      "invalid Ballerina.toml file: cannot find 'dependency' under 'platform'");
                    }

                    if (dependencyNode.kind() == TomlType.TABLE_ARRAY) {
                        TomlTableArrayNode dependencyTableArray = (TomlTableArrayNode) dependencyNode;
                        List<TomlTableNode> children = dependencyTableArray.children();

                        if (!children.isEmpty()) {
                            List<Map<String, Object>> platformEntry = new ArrayList<>();

                            for (TomlTableNode platformEntryTable : children) {
                                if (!platformEntryTable.entries().isEmpty()) {
                                    Map<String, Object> platformEntryMap = new HashMap<>();
                                    platformEntryMap.put("path",
                                                         getStringValueFromPlatformEntry(platformEntryTable,
                                                                                         "path"));
                                    platformEntryMap.put("groupId",
                                                         getStringValueFromPlatformEntry(platformEntryTable,
                                                                                         "groupId"));
                                    platformEntryMap.put("artifactId",
                                                         getStringValueFromPlatformEntry(platformEntryTable,
                                                                                         "artifactId"));
                                    platformEntryMap.put(VERSION,
                                                         getStringValueFromPlatformEntry(platformEntryTable,
                                                                                         VERSION));
                                    platformEntryMap.put("scope",
                                                         getStringValueFromPlatformEntry(platformEntryTable,
                                                                                         "scope"));
                                    platformEntry.add(platformEntryMap);
                                }
                            }

                            PackageManifest.Platform platform = new PackageManifest.Platform(platformEntry);
                            platforms.put(platformCode, platform);
                        }
                    } else {
                        addDiagnostic(null, DiagnosticSeverity.ERROR, dependencyNode, "invalid Ballerina.toml file: "
                                + "'dependency' under 'platform' should be a table array");
                    }
                } else {
                    addDiagnostic(null, DiagnosticSeverity.ERROR, platformCodeNode,
                                  "invalid Ballerina.toml file: platform code under 'platform' should be a table");
                }
            } else {
                addDiagnostic(null, DiagnosticSeverity.ERROR, platformNode,
                              "invalid Ballerina.toml file: 'platform' should be a table");
            }
        }

        return platforms;
    }

    private BuildOptions setBuildOptions(TomlTableNode tomlTableNode) {
        TomlTableNode tableNode = (TomlTableNode) tomlTableNode.entries().get("build-options");
        if (tableNode == null || tableNode.kind() == TomlType.NONE) {
            return null;
        }

        BuildOptionsBuilder buildOptionsBuilder = new BuildOptionsBuilder();

        boolean skipTests = getBooleanFromBuildOptionsTableNode(tableNode, CompilerOptionName.SKIP_TESTS.toString());
        boolean offline = getBooleanFromBuildOptionsTableNode(tableNode, CompilerOptionName.OFFLINE.toString());
        boolean observabilityIncluded =
                getBooleanFromBuildOptionsTableNode(tableNode, CompilerOptionName.OBSERVABILITY_INCLUDED.toString());
        boolean testReport =
                getBooleanFromBuildOptionsTableNode(tableNode, BuildOptions.OptionName.TEST_REPORT.toString());
        boolean codeCoverage =
                getBooleanFromBuildOptionsTableNode(tableNode, BuildOptions.OptionName.CODE_COVERAGE.toString());
        return buildOptionsBuilder
                .skipTests(skipTests)
                .offline(offline)
                .observabilityIncluded(observabilityIncluded)
                .testReport(testReport)
                .codeCoverage(codeCoverage)
                .build();
    }

    private boolean getBooleanFromBuildOptionsTableNode(TomlTableNode tableNode, String key) {
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
        addDiagnostic(null, DiagnosticSeverity.ERROR, topLevelNode,
                      "invalid Ballerina.toml file: at '" + key + "' under [build-options]");
        return false;
    }

    private String getStringValueFromPackageNode(TomlTableNode pkgNode, String key) {
        TopLevelNode topLevelNode = pkgNode.entries().get(key);
        if (topLevelNode == null || topLevelNode.kind() == TomlType.NONE) {
            addDiagnostic(null, DiagnosticSeverity.ERROR, pkgNode,
                          "invalid Ballerina.toml file: cannot find '" + key + "' under [package]");
            return null;
        }
        return getStringFromTomlTableNode(topLevelNode, key, "[[package]]");
    }

    private String getStringValueFromDependencyNode(TomlTableNode pkgNode, String key) {
        TopLevelNode topLevelNode = pkgNode.entries().get(key);
        if (topLevelNode == null || topLevelNode.kind() == TomlType.NONE) {
            addDiagnostic(null, DiagnosticSeverity.ERROR, pkgNode,
                          "invalid Ballerina.toml file: cannot find '" + key + "' under [[dependency]]");
            return null;
        }
        return getStringFromTomlTableNode(topLevelNode, key, "[[dependency]]");
    }

    private String getStringValueFromPlatformEntry(TomlTableNode pkgNode, String key) {
        TopLevelNode topLevelNode = pkgNode.entries().get(key);
        if (topLevelNode == null || topLevelNode.kind() == TomlType.NONE) {
            return null;
        }
        return getStringFromTomlTableNode(topLevelNode, key, "[[platform]]");
    }

    private String getStringFromTomlTableNode(TopLevelNode topLevelNode, String key, String tableNodeName) {
        if (topLevelNode.kind() == TomlType.KEY_VALUE) {
            TomlKeyValueNode keyValueNode = (TomlKeyValueNode) topLevelNode;
            TomlValueNode value = keyValueNode.value();
            if (value.kind() == TomlType.STRING) {
                TomlStringValueNode stringValueNode = (TomlStringValueNode) value;
                return stringValueNode.getValue();
            }
        }
        addDiagnostic(null, DiagnosticSeverity.ERROR, topLevelNode,
                      "invalid Ballerina.toml file: cannot find '" + key + "' under " + tableNodeName);
        return null;
    }

    private void validateOtherEntries(Map<String, TopLevelNode> otherEntries) {
        validateOtherEntriesArrayNode(otherEntries.get(LICENSE), LICENSE);
        validateOtherEntriesArrayNode(otherEntries.get(AUTHORS), AUTHORS);
        validateOtherEntriesArrayNode(otherEntries.get(KEYWORDS), KEYWORDS);
        validateOtherEntriesStringNode(otherEntries.get(REPOSITORY), REPOSITORY);
    }

    private void validateOtherEntriesArrayNode(TopLevelNode topLevelArrayNode, String key) {
        if (topLevelArrayNode != null && topLevelArrayNode.kind() != TomlType.NONE) {
            if (topLevelArrayNode.kind() == TomlType.KEY_VALUE) {
                TomlKeyValueNode licenseKeyValueNode = (TomlKeyValueNode) topLevelArrayNode;
                TomlValueNode valueNode = licenseKeyValueNode.value();
                if (valueNode.kind() == TomlType.NONE) {
                    return;
                } else if (valueNode.kind() == TomlType.ARRAY) {
                    TomlArrayValueNode array = (TomlArrayValueNode) valueNode;
                    for (TomlValueNode value : array.elements()) {
                        if (value.kind() != TomlType.STRING) {
                            addDiagnostic(null, DiagnosticSeverity.ERROR, topLevelArrayNode,
                                          "invalid Ballerina.toml file: '" + key + "' should have string elements");
                        }
                    }
                    return;
                }
            }
            addDiagnostic(null, DiagnosticSeverity.ERROR, topLevelArrayNode,
                          "invalid Ballerina.toml file: at '" + key + "'");
        }
    }

    private void validateOtherEntriesStringNode(TopLevelNode topLevelStringNode, String key) {
        if (topLevelStringNode != null && topLevelStringNode.kind() != TomlType.NONE) {
            if (topLevelStringNode.kind() == TomlType.KEY_VALUE) {
                TomlKeyValueNode licenseKeyValueNode = (TomlKeyValueNode) topLevelStringNode;
                TomlValueNode valueNode = licenseKeyValueNode.value();
                if (valueNode.kind() != TomlType.NONE && valueNode.kind() == TomlType.STRING) {
                    return;
                }
            }
            addDiagnostic(null, DiagnosticSeverity.ERROR, topLevelStringNode,
                          "invalid Ballerina.toml file: at '" + key + "'");
        }
    }

    private void addDiagnostic(DiagnosticCode diagnosticCode, DiagnosticSeverity severity, Node node, String message) {
        DiagnosticInfo diagInfo;
        if (diagnosticCode != null) {
            diagInfo = new DiagnosticInfo(diagnosticCode.diagnosticId(), diagnosticCode.messageKey(),
                                          diagnosticCode.severity());
        } else {
            diagInfo = new DiagnosticInfo(null, message, severity);
        }

        TomlNodeLocation tomlNodeLocation;
        if (node.location() != null) {
            tomlNodeLocation = new TomlNodeLocation(node.location().lineRange(), node.location().textRange());
        } else {
            LineRange lineRange = LineRange.from(String.valueOf(this.filePath),
                                                 LinePosition.from(0, 0),
                                                 LinePosition.from(0, 0));
            TextRange textRange = TextRange.from(0, 0);
            tomlNodeLocation = new TomlNodeLocation(lineRange, textRange);
        }
        TomlDiagnostic tomlDiagnostic = new TomlDiagnostic(tomlNodeLocation, diagInfo, message);

        this.diagnosticList.add(tomlDiagnostic);
    }
}
