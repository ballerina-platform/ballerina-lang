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
import io.ballerina.projects.Diagnostics;
import io.ballerina.projects.PackageDescriptor;
import io.ballerina.projects.PackageManifest;
import io.ballerina.projects.PackageName;
import io.ballerina.projects.PackageOrg;
import io.ballerina.projects.PackageVersion;
import io.ballerina.projects.PlatformLibraryScope;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.SemanticVersion;
import io.ballerina.projects.TomlDocument;
import io.ballerina.projects.internal.model.BalToolDescriptor;
import io.ballerina.projects.internal.model.CompilerPluginDescriptor;
import io.ballerina.projects.util.FileUtils;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.projects.util.ProjectUtils;
import io.ballerina.toml.api.Toml;
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
import io.ballerina.toml.semantic.diagnostics.TomlNodeLocation;
import io.ballerina.toml.validator.TomlValidator;
import io.ballerina.toml.validator.schema.Schema;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticInfo;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import org.apache.commons.io.FilenameUtils;
import org.ballerinalang.compiler.CompilerOptionName;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.ballerina.projects.internal.ManifestUtils.ToolNodeValueType;
import static io.ballerina.projects.internal.ManifestUtils.convertDiagnosticToString;
import static io.ballerina.projects.internal.ManifestUtils.getBooleanFromTomlTableNode;
import static io.ballerina.projects.internal.ManifestUtils.getBuildToolTomlValueType;
import static io.ballerina.projects.internal.ManifestUtils.getStringFromTomlTableNode;
import static io.ballerina.projects.util.ProjectConstants.DOT;
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
    private final TomlDocument balToolToml;
    private DiagnosticResult diagnostics;
    private final List<Diagnostic> diagnosticList;
    private final PackageManifest packageManifest;
    private final BuildOptions buildOptions;
    private final Path projectPath;
    Set<String> toolIdsSet = new HashSet<>();
    Set<String> targetModuleSet = new HashSet<>();

    private static final String PACKAGE = "package";
    private static final String VERSION = "version";
    public static final String ORG = "org";
    public static final String NAME = "name";
    private static final String LICENSE = "license";
    private static final String AUTHORS = "authors";
    private static final String REPOSITORY = "repository";
    private static final String KEYWORDS = "keywords";
    private static final String EXPORT = "export";
    private static final String INCLUDE = "include";
    private static final String PLATFORM = "platform";
    private static final String SCOPE = "scope";
    private static final String ARTIFACT_ID = "artifactId";
    private static final String GROUP_ID = "groupId";
    private static final String PATH = "path";
    private static final String TEMPLATE = "template";
    public static final String ICON = "icon";
    public static final String GRAALVM_COMPATIBLE = "graalvmCompatible";
    public static final String DISTRIBUTION = "distribution";
    public static final String VISIBILITY = "visibility";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String URL = "url";
    private static final String DEPENDENCY = "dependency";
    private static final String ID = "id";
    private static final String TARGETMODULE = "targetModule";
    private static final String OPTIONS = "options";
    private static final String TOOL = "tool";
    private static final String DESCRIPTION = "description";
    private static final String README = "readme";

    private boolean isOldStructure;

    private ManifestBuilder(TomlDocument ballerinaToml,
                            TomlDocument compilerPluginToml,
                            TomlDocument balToolToml,
                            Path projectPath) {
        this.projectPath = projectPath;
        this.ballerinaToml = ballerinaToml;
        this.compilerPluginToml = compilerPluginToml;
        this.balToolToml = balToolToml;
        this.diagnosticList = new ArrayList<>();
        this.packageManifest = parseAsPackageManifest();
        this.buildOptions = parseBuildOptions();
    }

    public static ManifestBuilder from(TomlDocument ballerinaToml,
                                       TomlDocument compilerPluginToml,
                                       TomlDocument balToolToml,
                                       Path projectPath) {
        return new ManifestBuilder(ballerinaToml, compilerPluginToml, balToolToml, projectPath);
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
        String readme = null;
        String description = "";
        List<PackageManifest.Module> moduleEntries = new ArrayList<>();

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

                String customReadmeVal = getStringValueFromTomlTableNode(pkgNode, README, null);
                readme = validateAndGetReadmePath(pkgNode, customReadmeVal, packageDescriptor.name());
                description = getStringValueFromTomlTableNode(pkgNode, DESCRIPTION, "");
                moduleEntries = getModuleEntries(pkgNode, customReadmeVal, packageDescriptor.name());

                if (!exported.isEmpty()) {
                    reportDiagnostic(pkgNode.entries().get(EXPORT),
                            "'export' under [package] is deprecated. " +
                                    "Add the exports using the 'export' field under '[[package.modules]]'",
                            ProjectDiagnosticErrorCode.DEPRECATED_BALLERINA_TOML_ENTRY, DiagnosticSeverity.WARNING);
                }
                if (!isOldStructure) {
                    if (!exported.contains(packageDescriptor.name().toString())) {
                        exported.add(packageDescriptor.name().toString()); // default module is always exported
                    }
                    for (PackageManifest.Module moduleEntry : moduleEntries) {
                        if (!moduleEntry.export()) {
                            continue;
                        }
                        String name = moduleEntry.name();
                        if (!exported.contains(name)) {
                            exported.add(name);
                        }
                    }
                }
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

        // Process pre build generator tools
        List<PackageManifest.Tool> tools = getTools();

        // Compiler plugin descriptor
        CompilerPluginDescriptor pluginDescriptor = null;
        if (this.compilerPluginToml != null) {
            pluginDescriptor = CompilerPluginDescriptor.from(this.compilerPluginToml);
        }

        // BalTool descriptor
        BalToolDescriptor balToolDescriptor = null;
        if (this.balToolToml != null) {
            balToolDescriptor = BalToolDescriptor.from(this.balToolToml, this.projectPath);
        }
        return PackageManifest.from(packageDescriptor, pluginDescriptor, balToolDescriptor, platforms,
                localRepoDependencies, otherEntries, diagnostics(), license, authors, keywords, exported, includes,
                repository, ballerinaVersion, visibility, template, icon, tools, readme, description, moduleEntries);
    }

    private List<PackageManifest.Module> getModuleEntries(
            TomlTableNode pkgNode, String customReadmeVal, PackageName packageName) {

        TopLevelNode dependencyEntries = validateAndGetModuleNodes(pkgNode, packageName);

        List<PackageManifest.Module> moduleList = new ArrayList<>();
        Path modulesRoot = this.projectPath.resolve(ProjectConstants.MODULES_ROOT);
        if (!Files.exists(modulesRoot)) {
            return moduleList;
        }
        Map<String, Path> moduleDirs;
        try (Stream<Path> stream = Files.walk(modulesRoot, 1)) {
            moduleDirs = stream
                    .filter(Files::isDirectory)
                    .filter(path -> !path.equals(modulesRoot))
                    .collect(Collectors.toMap(path -> Optional.of(path.getFileName()).get().toString(), path -> path));
        } catch (IOException e) {
            throw new ProjectException("Failed to read the module README:", e);
        }

        if (customReadmeVal == null) {
            if (Files.exists(this.projectPath.resolve(ProjectConstants.PACKAGE_MD_FILE_NAME))) {
                // old structure. Module READMEs are captured by <module-root>/Module.md file.
                for (Map.Entry<String, Path> pathEntry : moduleDirs.entrySet()) {
                    Path modReadmePath = pathEntry.getValue().resolve(ProjectConstants.MODULE_MD_FILE_NAME);
                    String modReadme = null;
                    if (Files.exists(modReadmePath)) {
                        modReadme = Paths.get(ProjectConstants.MODULES_ROOT).resolve(pathEntry.getKey())
                                .resolve(ProjectConstants.MODULE_MD_FILE_NAME).toString();
                    }
                    PackageManifest.Module module = new PackageManifest.Module(
                            packageName + DOT + Optional.of(pathEntry.getValue().getFileName()).get(), false,
                            "", modReadme);
                    moduleList.add(module);
                }
                return moduleList;
            }
        }

        // new structure
        if (dependencyEntries == null || dependencyEntries.kind() == TomlType.NONE) {
            for (Map.Entry<String, Path> pathEntry : moduleDirs.entrySet()) {
                Path modReadmePath = pathEntry.getValue().resolve(ProjectConstants.README_MD_FILE_NAME);
                String modReadme = "";
                if (Files.exists(modReadmePath)) {
                    modReadme = Paths.get(ProjectConstants.MODULES_ROOT).resolve(pathEntry.getKey())
                            .resolve(ProjectConstants.README_MD_FILE_NAME).toString();;
                }
                PackageManifest.Module module = new PackageManifest.Module(
                        packageName + DOT + Optional.of(pathEntry.getValue().getFileName()).get(), false,
                        "", modReadme);
                moduleList.add(module);
            }
            return moduleList;
        }
        if (dependencyEntries.kind() == TomlType.TABLE_ARRAY) {
            TomlTableArrayNode dependencyTableArray = (TomlTableArrayNode) dependencyEntries;
            for (TomlTableNode modulesNode : dependencyTableArray.children()) {
                String moduleName = getStringValueFromTomlTableNode(modulesNode, NAME, null);
                if (moduleName == null
                        || !moduleName.contains(DOT)) { // The invalid module name is already handled
                    continue;
                }
                String moduleNamepart = moduleName.substring(packageName.toString().length() + 1);

                boolean export = Boolean.TRUE.equals(getBooleanValueFromTomlTableNode(modulesNode, EXPORT));
                String description = getStringValueFromTomlTableNode(modulesNode, DESCRIPTION, null);
                String modReadme = getStringValueFromTomlTableNode(modulesNode, README, null);
                if (modReadme == null) {
                    Path defaultReadme = modulesRoot.resolve(moduleNamepart)
                            .resolve(ProjectConstants.README_MD_FILE_NAME);
                    if (Files.exists(defaultReadme)) {
                        modReadme = defaultReadme.toString();
                    }
                } else {
                    if (!Paths.get(modReadme).isAbsolute()) {
                        modReadme = this.projectPath.resolve(modReadme).toString();
                    }
                }
                PackageManifest.Module module = new PackageManifest.Module(moduleName, export,
                        description, modReadme);
                moduleList.add(module);
                moduleDirs.remove(moduleNamepart);
            }
            // If there are README.mds in other modules, add them
            for (Map.Entry<String, Path> pathEntry : moduleDirs.entrySet()) {
                Path modReadmePath = pathEntry.getValue().resolve(ProjectConstants.README_MD_FILE_NAME);
                String modReadme = "";
                if (Files.exists(modReadmePath)) {
                    modReadme = modReadmePath.toString();
                }
                PackageManifest.Module module = new PackageManifest.Module(
                        packageName + DOT + Optional.of(pathEntry.getValue().getFileName()).get(), false,
                        "", modReadme);
                moduleList.add(module);
            }
        }
        return moduleList;
    }

    private TopLevelNode validateAndGetModuleNodes(TomlTableNode pkgNode, PackageName packageName) {
        TopLevelNode dependencyEntries = pkgNode.entries().get("modules");
        if (dependencyEntries != null && dependencyEntries.kind() == TomlType.TABLE_ARRAY) {
            TomlTableArrayNode dependencyTableArray = (TomlTableArrayNode) dependencyEntries;
            for (TomlTableNode modulesNode : dependencyTableArray.children()) {
                String moduleName = getStringValueFromTomlTableNode(modulesNode, NAME, null);
                if (moduleName == null) {
                    continue;
                }
                if (moduleName.equals(packageName.toString())) {
                    String warning = "module '" + moduleName + "' is not allowed\n";
                    reportDiagnostic(modulesNode.entries().get(NAME), warning,
                            ProjectDiagnosticErrorCode.INVALID_MODULE, DiagnosticSeverity.ERROR);
                    continue;
                }

                try {
                    if (Files.notExists(this.projectPath.resolve(ProjectConstants.MODULES_ROOT)
                            .resolve(moduleName.split(packageName + DOT)[1]))) {
                        String warning = "module '" + moduleName + "' not found";
                        reportDiagnostic(modulesNode.entries().get(NAME), warning,
                                ProjectDiagnosticErrorCode.INVALID_MODULE, DiagnosticSeverity.ERROR);
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    String warning = "module '" + moduleName + "' not found";
                    reportDiagnostic(modulesNode.entries().get(NAME), warning,
                            ProjectDiagnosticErrorCode.INVALID_MODULE, DiagnosticSeverity.ERROR);
                }
            }
        }
        return dependencyEntries;
    }

    private String validateAndGetReadmePath(TomlTableNode pkgNode, String readme, PackageName name) {
        Path readmeMdPath;
        if (readme == null) {
            readmeMdPath = this.projectPath.resolve(ProjectConstants.PACKAGE_MD_FILE_NAME);
            if (Files.exists(readmeMdPath)) {
                String warning = """
                        The default file for package documentation is changed to README.md. If you prefer to \
                        use the Package.md, add the following line under the '[package]' section in your \
                        Ballerina.toml file:
                        \treadme = "Package.md"
                        """;
                DiagnosticInfo diagnosticInfo = new DiagnosticInfo(ProjectDiagnosticErrorCode.
                        DEPRECATED_DOC_FILE.diagnosticId(), warning, DiagnosticSeverity.WARNING);
                PackageDiagnostic packageDiagnostic = new PackageDiagnostic(diagnosticInfo,
                        name.toString());
                ProjectUtils.addMiscellaneousProjectDiagnostics(packageDiagnostic);
                isOldStructure = true;
                return readmeMdPath.toString();
            } else {
                readmeMdPath = this.projectPath.resolve(ProjectConstants.README_MD_FILE_NAME);
                if (Files.exists(readmeMdPath)) {
                    return readmeMdPath.toString();
                } else {
                    return null;
                }
            }
        }

        readmeMdPath = Paths.get(readme);
        if (!readmeMdPath.isAbsolute()) {
            readmeMdPath = this.projectPath.resolve(readmeMdPath);
        }
        if (Files.notExists(readmeMdPath)) {
            reportDiagnostic(pkgNode.entries().get(README),
                    "could not locate the readme file '" + readmeMdPath + "'",
                    ProjectDiagnosticErrorCode.INVALID_PATH, DiagnosticSeverity.ERROR);
        }

        if (!FilenameUtils.getExtension(readme).equals(ProjectConstants.README_EXTENSION)) {
            reportDiagnostic(pkgNode.entries().get(README),
                    "invalid 'readme' under [package]: 'readme' can only have '.md' files",
                    ProjectDiagnosticErrorCode.INVALID_FILE_FORMAT, DiagnosticSeverity.ERROR);
        }
        return readmeMdPath.toString();
    }

    private List<PackageManifest.Tool> getTools() {
        TomlTableNode rootNode = ballerinaToml.toml().rootNode();
        if (rootNode.entries().isEmpty()) {
            return Collections.emptyList();
        }
        TopLevelNode toolEntries = rootNode.entries().get(TOOL);
        List<PackageManifest.Tool> tools = new ArrayList<>();
        if (toolEntries == null || toolEntries.kind() != TomlType.TABLE) {
            return Collections.emptyList();
        }
        TomlTableNode toolTable = (TomlTableNode) toolEntries;
        Set<String> toolCodes = toolTable.entries().keySet();

        // Gather tool configurations and add the tools to a list
        for (String toolCode : toolCodes) {
            addToolAndSubTools(tools, toolCode, toolTable.entries().get(toolCode), "");
        }
        return tools;
    }

    private void addToolAndSubTools(List<PackageManifest.Tool> tools, String toolCode, TopLevelNode toolCodeNode,
                                    String toolCodePrefix) {
        if (toolCodeNode.kind() == TomlType.TABLE) {
            TomlTableNode toolEntry = (TomlTableNode) toolCodeNode;
            addSubTools(tools, toolEntry, toolCode, toolCodePrefix);
        } else if (toolCodeNode.kind() == TomlType.TABLE_ARRAY) {
            TomlTableArrayNode toolEntriesArray = (TomlTableArrayNode) toolCodeNode;
            for (TomlTableNode toolEntry : toolEntriesArray.children()) {
                addSubTools(tools, toolEntry, toolCode, toolCodePrefix);
                addTool(tools, toolEntry, toolCode, toolCodePrefix);
            }
        }
    }

    private void addSubTools(List<PackageManifest.Tool> tools, TomlTableNode toolEntry, String toolCode,
                             String toolCodePrefix) {
        if (toolEntry.entries().isEmpty()) {
            return;
        }
        Set<String> toolFields = toolEntry.entries().keySet();
        for (String field : toolFields) {
            if (field.equals(OPTIONS) || field.equals(ID) || field.equals(VERSION) || field.equals(TARGETMODULE) ||
                    field.equals("filePath")) {
                continue;
            }
            addToolAndSubTools(tools, field, toolEntry.entries().get(field), toolCodePrefix + toolCode + ".");
        }
    }

    private void addTool(List<PackageManifest.Tool> tools, TomlTableNode toolEntry, String toolCode,
                         String toolCodePrefix) {
        PackageManifest.Tool.Field id = getValueFromPreBuildToolNode(toolEntry, "id", toolCode);
        PackageManifest.Tool.Field filePath = getValueFromPreBuildToolNode(toolEntry, "filePath",
                toolCode);
        PackageManifest.Tool.Field targetModule = getValueFromPreBuildToolNode(toolEntry,
                TARGETMODULE, toolCode);
        Toml optionsToml = getToml(toolEntry, OPTIONS);
        TopLevelNode topLevelNode = toolEntry.entries().get(OPTIONS);
        TomlTableNode optionsNode = null;
        if (topLevelNode != null && topLevelNode.kind() == TomlType.TABLE) {
            optionsNode = (TomlTableNode) topLevelNode;
        }

        // Validate recurring tool ids and target modules
        if (!toolIdsSet.add(id.value())) {
            reportDiagnostic(toolEntry, "recurring tool id '" + id.value() + "' found in Ballerina.toml. " +
                            "Tool id must be unique for each tool",
                    ProjectDiagnosticErrorCode.RECURRING_TOOL_PROPERTIES,
                    DiagnosticSeverity.ERROR);
        }
        if (!targetModuleSet.add(targetModule.value())) {
            reportDiagnostic(toolEntry, "recurring target module found in Ballerina.toml. Target " +
                            "module must be unique for each tool",
                    ProjectDiagnosticErrorCode.RECURRING_TOOL_PROPERTIES,
                    DiagnosticSeverity.ERROR);
        }

        // Add a flag for tools with error diagnostics

        boolean hasErrorDiagnostic = !Diagnostics.filterErrors(toolEntry.diagnostics()).isEmpty();
        String typeValue = toolCodePrefix + toolCode;
        PackageManifest.Tool.Field type = new PackageManifest.Tool.Field(typeValue, toolEntry.location());
        PackageManifest.Tool tool = new PackageManifest.Tool(type, id, filePath,
                targetModule, optionsToml, optionsNode, hasErrorDiagnostic);
        tools.add(tool);
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
                    ProjectDiagnosticErrorCode.MISSING_PKG_INFO_IN_BALLERINA_TOML,
                    DiagnosticSeverity.WARNING);
            return PackageDescriptor.from(defaultOrg(), defaultName(this.projectPath), defaultVersion());
        }

        TopLevelNode topLevelPkgNode = tomlTableNode.entries().get(PACKAGE);
        if (topLevelPkgNode == null || topLevelPkgNode.kind() != TomlType.TABLE) {
            reportDiagnostic(tomlTableNode, errorMessage,
                    ProjectDiagnosticErrorCode.MISSING_PKG_INFO_IN_BALLERINA_TOML,
                    DiagnosticSeverity.WARNING);
            return PackageDescriptor.from(defaultOrg(), defaultName(this.projectPath), defaultVersion());
        }

        TomlTableNode pkgNode = (TomlTableNode) topLevelPkgNode;

        org = getStringValueFromTomlTableNode(pkgNode, ORG, "");
        if (pkgNode.entries().get(ORG) == null) {
            org = defaultOrg().value();
            reportDiagnostic(pkgNode, "missing key 'org' in table '[package]' in 'Ballerina.toml'. " +
                            "Defaulting to 'org = \"" + org + "\"'",
                    ProjectDiagnosticErrorCode.MISSING_PKG_INFO_IN_BALLERINA_TOML,
                    DiagnosticSeverity.WARNING);
        }
        name = getStringValueFromTomlTableNode(pkgNode, NAME, "");
        if (pkgNode.entries().get(NAME) == null) {
            name = defaultName(this.projectPath).value();
            reportDiagnostic(pkgNode, "missing key 'name' in table '[package]' in 'Ballerina.toml'. " +
                            "Defaulting to 'name = \"" + name + "\"'",
                    ProjectDiagnosticErrorCode.MISSING_PKG_INFO_IN_BALLERINA_TOML,
                    DiagnosticSeverity.WARNING);
        }
        version = getStringValueFromTomlTableNode(pkgNode, VERSION, "");
        if (pkgNode.entries().get(VERSION) == null) {
            version = defaultVersion().value().toString();
            reportDiagnostic(pkgNode, "missing key 'version' in table '[package]' in 'Ballerina.toml'. " +
                            "Defaulting to 'version = \"" + version + "\"'",
                    ProjectDiagnosticErrorCode.MISSING_PKG_INFO_IN_BALLERINA_TOML,
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
            Path iconPath = Path.of(icon);
            if (!iconPath.isAbsolute()) {
                iconPath = this.projectPath.resolve(iconPath);
            }

            if (Files.notExists(iconPath)) {
                // validate icon path
                // if file path does not exist, throw this error
                reportDiagnostic(pkgNode.entries().get(ICON),
                        "could not locate icon path '" + icon + "'",
                        ProjectDiagnosticErrorCode.INVALID_PATH, DiagnosticSeverity.ERROR);
            } else {
                // validate file content
                // if other file types renamed as png, throw this error
                try {
                    if (!FileUtils.isValidPng(iconPath)) {
                        reportDiagnostic(pkgNode.entries().get("icon"),
                                "invalid 'icon' under [package]: 'icon' can only have 'png' images",
                                ProjectDiagnosticErrorCode.INVALID_FILE_FORMAT, DiagnosticSeverity.ERROR);
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
                for (String platformCode : platformCodes) {
                    TopLevelNode platformCodeNode = platformTable.entries().get(platformCode);

                    if (platformCodeNode.kind() == TomlType.TABLE) {
                        TomlTableNode platformCodeTable = (TomlTableNode) platformCodeNode;
                        // Get graalvmCompatible value
                        TopLevelNode graalvmCompatibleNode = platformCodeTable.entries().get(GRAALVM_COMPATIBLE);
                        if (graalvmCompatibleNode != null && graalvmCompatibleNode.kind() != TomlType.NONE) {
                            PackageManifest.Platform newPlatform =
                                    getGraalvmCompatibilityPlatform(graalvmCompatibleNode);
                            if (newPlatform != null) {
                                if (platforms.get(platformCode) != null) {
                                    newPlatform = new PackageManifest.Platform(platforms.
                                            get(platformCode).dependencies(),
                                            platforms.get(platformCode).repositories(),
                                            newPlatform.graalvmCompatible());
                                }
                                platforms.put(platformCode, newPlatform);
                            }
                        }

                        TopLevelNode topLevelNode = platformCodeTable.entries().get(DEPENDENCY);
                        if (topLevelNode != null && topLevelNode.kind() != TomlType.NONE) {
                            PackageManifest.Platform newPlatform = getDependencyPlatform(topLevelNode);
                            if (newPlatform != null) {
                                if (platforms.get(platformCode) != null) {
                                    newPlatform = new PackageManifest.Platform(newPlatform.dependencies(),
                                            platforms.get(platformCode).repositories(),
                                            platforms.get(platformCode).graalvmCompatible());
                                }
                                platforms.put(platformCode, newPlatform);
                            }
                        }
                        topLevelNode = platformCodeTable.entries().get(REPOSITORY);
                        if (topLevelNode != null && topLevelNode.kind() != TomlType.NONE) {
                            PackageManifest.Platform platform = getRepositoryPlatform(topLevelNode);
                            if (platform != null) {
                                if (platforms.get(platformCode) != null) {
                                    platform = new PackageManifest.Platform(platforms.get(platformCode)
                                            .dependencies(), platform.repositories(),
                                            platforms.get(platformCode).graalvmCompatible());
                                }
                                platforms.put(platformCode, platform);
                            }
                        }
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
                platform = new PackageManifest.Platform(Collections.emptyList(), platformEntry, null);
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
                        String pathValue = getStringValueFromPlatformEntry(platformEntryTable, PATH);
                        if (pathValue != null) {
                            Path path = Path.of(pathValue);
                            if (!path.isAbsolute()) {
                                path = this.projectPath.resolve(path);
                            }
                            if (Files.notExists(path)) {
                                reportDiagnostic(platformEntryTable.entries().get(PATH),
                                        "could not locate dependency path '" + pathValue + "'",
                                        ProjectDiagnosticErrorCode.INVALID_PATH, DiagnosticSeverity.ERROR);
                            }
                        }
                        String groupId = getStringValueFromPlatformEntry(platformEntryTable, GROUP_ID);
                        String artifactId = getStringValueFromPlatformEntry(platformEntryTable, ARTIFACT_ID);
                        String version = getStringValueFromPlatformEntry(platformEntryTable, VERSION);
                        String scope = getStringValueFromPlatformEntry(platformEntryTable, SCOPE);
                        Boolean graalvmCompatibility = getBooleanValueFromTomlTableNode(platformEntryTable,
                                GRAALVM_COMPATIBLE);
                        if (PlatformLibraryScope.PROVIDED.getStringValue().equals(scope)
                                && !providedPlatformDependencyIsValid(artifactId, groupId, version)) {
                            reportDiagnostic(platformEntryTable,
                                    "artifactId, groupId and version must be provided for platform " +
                                            "dependencies with provided scope",
                                    ProjectDiagnosticErrorCode.INVALID_PROVIDED_DEPENDENCY, DiagnosticSeverity.ERROR);
                        }
                        platformEntryMap.put(PATH,
                                pathValue);
                        platformEntryMap.put(GROUP_ID, groupId);
                        platformEntryMap.put(ARTIFACT_ID, artifactId);
                        platformEntryMap.put(VERSION, version);
                        platformEntryMap.put(SCOPE, scope);
                        platformEntryMap.put(GRAALVM_COMPATIBLE, graalvmCompatibility);
                        platformEntry.add(platformEntryMap);
                    }
                }
                platform = new PackageManifest.Platform(platformEntry);
            }
        }
        return platform;
    }

    private PackageManifest.Platform getGraalvmCompatibilityPlatform(TopLevelNode graalvmCompatibleNode) {
        if (graalvmCompatibleNode.kind() == TomlType.KEY_VALUE) {
            TomlKeyValueNode keyValueNode = ((TomlKeyValueNode) graalvmCompatibleNode);
            if (keyValueNode.value().kind() == TomlType.BOOLEAN) {
                Boolean graalvmCompatible = ((TomlBooleanValueNode) keyValueNode.value()).getValue();
                return new PackageManifest.Platform(Collections.emptyList(), Collections.emptyList(),
                        graalvmCompatible);
            }
        }
        return null;
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
                String name = getStringValueFromDependencyNode(dependencyNode, NAME);
                String org = getStringValueFromDependencyNode(dependencyNode, ORG);
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

    // TODO: Fix code and messageFormat parameters in usages.
    private void reportDiagnostic(TopLevelNode tomlTableNode,
                                  String message,
                                  ProjectDiagnosticErrorCode errorCode,
                                  DiagnosticSeverity severity) {
        DiagnosticInfo diagnosticInfo =
                new DiagnosticInfo(errorCode.diagnosticId(), errorCode.messageKey(), severity);
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
        Boolean experimental =
                getBooleanFromBuildOptionsTableNode(tableNode, CompilerOptionName.EXPERIMENTAL.toString());
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
        String graalVMBuildOptions = getStringFromBuildOptionsTableNode(tableNode,
                BuildOptions.OptionName.GRAAL_VM_BUILD_OPTIONS.toString());
        Boolean remoteManagement = getBooleanFromBuildOptionsTableNode(tableNode,
                CompilerOptionName.REMOTE_MANAGEMENT.toString());
        Boolean showDependencyDiagnostics = getBooleanFromBuildOptionsTableNode(tableNode,
                BuildOptions.OptionName.SHOW_DEPENDENCY_DIAGNOSTICS.toString());
        Boolean optimizeDependencyCompilation = getBooleanFromBuildOptionsTableNode(tableNode,
                BuildOptions.OptionName.OPTIMIZE_DEPENDENCY_COMPILATION.toString());

        buildOptionsBuilder
                .setOffline(offline)
                .setExperimental(experimental)
                .setObservabilityIncluded(observabilityIncluded)
                .setTestReport(testReport)
                .setCodeCoverage(codeCoverage)
                .setCloud(cloud)
                .setListConflictedClasses(listConflictedClasses)
                .setDumpBuildTime(dumpBuildTime)
                .setSticky(sticky)
                .setEnableCache(enableCache)
                .setNativeImage(nativeImage)
                .setExportComponentModel(exportComponentModel)
                .setGraalVMBuildOptions(graalVMBuildOptions)
                .setRemoteManagement(remoteManagement)
                .setShowDependencyDiagnostics(showDependencyDiagnostics)
                .setOptimizeDependencyCompilation(optimizeDependencyCompilation);

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

    private Boolean getBooleanValueFromTomlTableNode(TomlTableNode pkgNode, String key) {
        TopLevelNode topLevelNode = pkgNode.entries().get(key);
        if (topLevelNode == null || topLevelNode.kind() == TomlType.NONE) {
            return null;
        }
        return getBooleanFromTomlTableNode(topLevelNode);
    }

    private String getStringValueFromDependencyNode(TomlTableNode pkgNode, String key) {
        TopLevelNode topLevelNode = pkgNode.entries().get(key);
        if (topLevelNode == null) {
            return null;
        }
        return getStringFromTomlTableNode(topLevelNode);
    }

    private PackageManifest.Tool.Field getValueFromPreBuildToolNode(TomlTableNode toolNode, String key,
                                                                    String toolCode) {
        TopLevelNode topLevelNode = toolNode.entries().get(key);
        if (topLevelNode == null) {
            String errorMessage = "missing key '[" + key + "]' in table '[tool." + toolCode + "]'.";
            if (key.equals(ID)) {
                reportDiagnostic(toolNode, errorMessage,
                        ProjectDiagnosticErrorCode.MISSING_TOOL_PROPERTIES_IN_BALLERINA_TOML,
                        DiagnosticSeverity.ERROR);
            }
            return new PackageManifest.Tool.Field(null, toolNode.location());
        }
        ToolNodeValueType toolNodeValueType = getBuildToolTomlValueType(topLevelNode);
        if (ToolNodeValueType.STRING.equals(toolNodeValueType)) {
            String value = getStringFromTomlTableNode(topLevelNode);
            TomlNodeLocation location = topLevelNode.location();
            return new PackageManifest.Tool.Field(value, location);
        } else if (ToolNodeValueType.EMPTY.equals(toolNodeValueType)) {
            if (!key.equals(TARGETMODULE)) {
                reportDiagnostic(toolNode, "empty string found for key '[" + key + "]' in table '[tool."
                                + toolCode + "]'.",
                    ProjectDiagnosticErrorCode.EMPTY_TOOL_PROPERTY,
                    DiagnosticSeverity.ERROR);
            }
            return new PackageManifest.Tool.Field(null, toolNode.location());
        } else if (ToolNodeValueType.NON_STRING.equals(toolNodeValueType)) {
            reportDiagnostic(toolNode, "incompatible type found for key '[" + key + "]': expected 'STRING'",
                ProjectDiagnosticErrorCode.INCOMPATIBLE_TYPE_FOR_TOOL_PROPERTY,
                DiagnosticSeverity.ERROR);
            return new PackageManifest.Tool.Field(null, toolNode.location());
        }
        return new PackageManifest.Tool.Field(null, toolNode.location());
    }

    private Toml getToml(TomlTableNode toolNode, String key) {
        TopLevelNode topLevelNode = toolNode.entries().get(key);
        if (topLevelNode == null) {
            return null;
        }
        if (topLevelNode.kind() != TomlType.TABLE) {
            return null;
        }
        TomlTableNode optionsNode = (TomlTableNode) topLevelNode;
        return new Toml(optionsNode);
    }

    private boolean providedPlatformDependencyIsValid(String artifactId, String groupId, String version) {
        return artifactId != null && !artifactId.isEmpty() && groupId != null && !groupId.isEmpty()
                && version != null && !version.isEmpty();
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
