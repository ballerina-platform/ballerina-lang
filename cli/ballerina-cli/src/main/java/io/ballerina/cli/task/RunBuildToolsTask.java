/*
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.cli.task;

import io.ballerina.cli.cmd.CommandUtil;
import io.ballerina.cli.launcher.LauncherUtils;
import io.ballerina.cli.utils.FileUtils;
import io.ballerina.projects.BuildTool;
import io.ballerina.projects.BuildToolResolution;
import io.ballerina.projects.Diagnostics;
import io.ballerina.projects.JvmTarget;
import io.ballerina.projects.PackageConfig;
import io.ballerina.projects.PackageManifest;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.Settings;
import io.ballerina.projects.buildtools.CodeGeneratorTool;
import io.ballerina.projects.buildtools.ToolContext;
import io.ballerina.projects.internal.PackageConfigCreator;
import io.ballerina.projects.internal.PackageDiagnostic;
import io.ballerina.projects.internal.ProjectDiagnosticErrorCode;
import io.ballerina.projects.util.BuildToolUtils;
import io.ballerina.toml.api.Toml;
import io.ballerina.toml.semantic.diagnostics.TomlDiagnostic;
import io.ballerina.toml.semantic.diagnostics.TomlNodeLocation;
import io.ballerina.toml.validator.schema.Schema;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticInfo;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import org.ballerinalang.central.client.CentralAPIClient;
import org.ballerinalang.central.client.CentralClientConstants;
import org.ballerinalang.central.client.exceptions.CentralClientException;
import org.ballerinalang.toml.exceptions.SettingsTomlException;
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.ballerina.cli.launcher.LauncherUtils.createLauncherException;
import static io.ballerina.cli.launcher.util.BalToolsUtil.findJarFiles;
import static io.ballerina.projects.JBallerinaBalaWriter.LIBS;
import static io.ballerina.projects.JBallerinaBalaWriter.TOOL;
import static io.ballerina.projects.PackageManifest.Tool;
import static io.ballerina.projects.util.ProjectConstants.TOOL_DIAGNOSTIC_CODE_PREFIX;
import static io.ballerina.projects.util.ProjectUtils.getAccessTokenOfCLI;
import static io.ballerina.projects.util.ProjectUtils.initializeProxy;

/**
 * Task for running tools integrated with the build.
 *
 * @since 2201.9.0
 */
public class RunBuildToolsTask implements Task {
    public static final String DEFAULT_VERSION = "0.0.0";
    private final PrintStream outStream;
    private final boolean exitWhenFinish;
    private ClassLoader toolClassLoader = this.getClass().getClassLoader();
    ServiceLoader<CodeGeneratorTool> toolServiceLoader = ServiceLoader.load(CodeGeneratorTool.class, toolClassLoader);
    private final Map<String, ToolContext> toolContextMap = new HashMap<>();

    public RunBuildToolsTask(PrintStream out) {
        this.outStream = out;
        this.exitWhenFinish = true;
    }

    @Override
    public void execute(Project project) {
        project.setToolContextMap(toolContextMap);

        // Print all build tool manifest diagnostics
        Collection<Diagnostic> toolManifestDiagnostics = project.currentPackage().manifest().diagnostics()
                .diagnostics().stream().filter(diagnostic -> diagnostic.diagnosticInfo().code()
                        .startsWith(TOOL_DIAGNOSTIC_CODE_PREFIX)).toList();
        toolManifestDiagnostics.forEach(outStream::println);

        // Read the build tool entries specified the Ballerina.toml
        List<Tool> toolEntries = project.currentPackage().manifest().tools();
        if (toolEntries.isEmpty()) {
            return;
        }
        this.outStream.println("\nExecuting Build Tools");

        // Populate the tool context map
        for (Tool toolEntry : toolEntries) {
            // Populate tool context
            ToolContext toolContext = ToolContext.from(toolEntry, project.currentPackage(), outStream);
            toolContextMap.put(toolEntry.id().value(), toolContext);
        }
        BuildToolResolution buildToolResolution;
        try {
            buildToolResolution = project.currentPackage().getBuildToolResolution();
        } catch (ProjectException e) {
            CommandUtil.printError(this.outStream, e.getMessage(), null, false);
            CommandUtil.exitError(this.exitWhenFinish);
            return;
        }
        buildToolResolution.getDiagnosticList().forEach(outStream::println);
        List<BuildTool> resolvedTools = buildToolResolution.getResolvedTools();
        List<BuildTool> centralDeliveredResolvedTools = resolvedTools.stream().filter(tool -> !DEFAULT_VERSION
                .equals(tool.version().toString())).toList();
        if (!centralDeliveredResolvedTools.isEmpty()) {
            if (!project.buildOptions().offlineBuild()) {
                pullLocallyUnavailableTools(centralDeliveredResolvedTools, buildToolResolution);
            }
            toolClassLoader = createToolClassLoader(centralDeliveredResolvedTools);
            toolServiceLoader = ServiceLoader.load(CodeGeneratorTool.class, toolClassLoader);
        }

        // We run only the entries of resolved tools.
        List<PackageManifest.Tool> resolvedToolEntries = toolEntries.stream()
                .filter(toolEntry -> resolvedTools.stream()
                        .anyMatch(tool -> tool.id().value().equals(toolEntry.type().value().split("\\.")[0])))
                .toList();
        for (Tool toolEntry : resolvedToolEntries) {
            String commandName = toolEntry.type().value();
            ToolContext toolContext = toolContextMap.get(toolEntry.id().value());
            Optional<CodeGeneratorTool> targetTool = BuildToolUtils.getTargetTool(commandName, toolServiceLoader);
            if (targetTool.isEmpty()) {
                // If the tool is not found, we skip the execution and report a diagnostic
                Diagnostic diagnostic = BuildToolUtils.getBuildToolCommandNotFoundDiagnostic(
                        commandName, toolEntry.type().location());
                toolContext.reportDiagnostic(diagnostic);
                this.outStream.println(diagnostic);
                printToolSkipWarning(toolEntry);
                continue;
            }
            // Here, we can safely pass the tool type value given in Ballerina.toml instead of
            // the aggregation of the ToolConfig annotation name fields of a command/ subcommand field
            // since we have verified that those two are identical in the ToolUtils.getTargetTool method
            Optional<TomlDiagnostic> cmdNameDiagnostic = BuildToolUtils
                    .getDiagnosticIfInvalidCommandName(commandName, toolEntry.id().location());
            if (cmdNameDiagnostic.isPresent()) {
                toolContext.reportDiagnostic(cmdNameDiagnostic.get());
                this.outStream.println(cmdNameDiagnostic.get());
                printToolSkipWarning(toolEntry);
                continue;
            }
            boolean hasOptionErrors = false;
            try {
                // validate the options toml and report diagnostics
                hasOptionErrors = validateOptionsToml(toolEntry.optionsToml(), toolEntry.id().value(),
                        toolEntry.type());
                if (hasOptionErrors) {
                    DiagnosticInfo diagnosticInfo = new DiagnosticInfo(
                            ProjectDiagnosticErrorCode.TOOL_OPTIONS_VALIDATION_FAILED.diagnosticId(),
                            ProjectDiagnosticErrorCode.TOOL_OPTIONS_VALIDATION_FAILED.messageKey(),
                            DiagnosticSeverity.ERROR);
                    TomlNodeLocation location = toolEntry.optionsToml() == null ? toolEntry.type().location() :
                            toolEntry.optionsTable().location();
                    TomlDiagnostic diagnostic = new TomlDiagnostic(location, diagnosticInfo, toolEntry.type().value());
                    toolContext.reportDiagnostic(diagnostic);
                }
            } catch (IOException e) {
                // if there is no options toml, we warn the user and continue
                outStream.printf("WARNING: Validation of tool options of '%s' for '%s' is skipped due to: %s%n",
                        commandName, toolEntry.id().value(), e.getMessage());
            }
            // if manifest errors or options table validation errors
            if (toolEntry.hasErrorDiagnostic() || hasOptionErrors) {
                printToolSkipWarning(toolEntry);
                continue;
            }

            // Execute the build tool
            try {
                this.outStream.printf("\t%s(%s)%n", toolEntry.type().value(), toolEntry.id().value());
                targetTool.get().execute(toolContext);
                for (Diagnostic d : toolContext.diagnostics()) {
                    if (d.toString().contains("(1:1,1:1)")) {
                        outStream.println(new PackageDiagnostic(d.diagnosticInfo(), toolContext.toolId()));
                    } else {
                        outStream.println(new PackageDiagnostic(d.diagnosticInfo(), d.location()));
                    }
                }
            } catch (Exception e) {
                throw createLauncherException(e.getMessage());
            }
        }
        // Reload the project to load the generated code
        reloadProject(project);
        this.outStream.println();
    }

    private boolean validateOptionsToml(Toml optionsToml, String toolId, Tool.Field toolType) throws IOException {
        if (optionsToml == null) {
            return validateEmptyOptionsToml(toolId, toolType);
        }
        FileUtils.validateToml(optionsToml, toolType.value(), toolClassLoader);
        optionsToml.diagnostics().forEach(outStream::println);
        return !Diagnostics.filterErrors(optionsToml.diagnostics()).isEmpty();
    }

    private boolean validateEmptyOptionsToml(String toolId, Tool.Field toolType) throws IOException {
        Schema schema = Schema.from(FileUtils.readSchema(toolType.value(), toolClassLoader));
        List<String> requiredFields = schema.required();
        if (!requiredFields.isEmpty()) {
            for (String field: requiredFields) {
                String message = "missing required optional field '" + field + "'";
                DiagnosticInfo diagnosticInfo = new DiagnosticInfo(
                        ProjectDiagnosticErrorCode.MISSING_TOOL_PROPERTIES_IN_BALLERINA_TOML.diagnosticId(),
                        ProjectDiagnosticErrorCode.MISSING_TOOL_PROPERTIES_IN_BALLERINA_TOML.messageKey(),
                        DiagnosticSeverity.ERROR);
                TomlDiagnostic diagnostic = new TomlDiagnostic(toolType.location(), diagnosticInfo, message);
                this.outStream.println(diagnostic);
            }
            return true;
        }
        this.outStream.printf("WARNING: Validation of tool options of '%s' for '%s' is skipped due to " +
                "no tool options found%n", toolType, toolId);
        return false;
    }

    private void pullLocallyUnavailableTools(List<BuildTool> tools, BuildToolResolution buildToolResolution) {
        for (BuildTool tool: tools) {
            String toolId = tool.id().value();
            String version = tool.version().toString();
            String org = tool.org().value();
            String name = tool.name().value();
            try {
                if (isToolLocallyAvailable(org, name, version)) {
                    continue;
                }
                pullToolFromCentral(toolId, version);
            } catch (CentralClientException e) {
                String message = "failed to pull build tool '" + toolId + ":" + version + "' from Ballerina Central: "
                        + e.getMessage();
                DiagnosticInfo diagnosticInfo = new DiagnosticInfo(
                        ProjectDiagnosticErrorCode.BUILD_TOOL_NOT_FOUND.diagnosticId(),
                        ProjectDiagnosticErrorCode.BUILD_TOOL_NOT_FOUND.messageKey(),
                        DiagnosticSeverity.ERROR);
                TomlDiagnostic diagnostic = new TomlDiagnostic(tool.location(), diagnosticInfo, message);
                outStream.println(diagnostic);
                buildToolResolution.getDiagnosticList().add(diagnostic);
                tools.remove(tool);
            } catch (ProjectException e) {
                String message = "failed to resolve build tool '" + toolId + ":" + version +
                        "' from the local cache: " + e.getMessage();
                DiagnosticInfo diagnosticInfo = new DiagnosticInfo(
                        ProjectDiagnosticErrorCode.BUILD_TOOL_NOT_FOUND.diagnosticId(),
                        ProjectDiagnosticErrorCode.BUILD_TOOL_NOT_FOUND.messageKey(),
                        DiagnosticSeverity.ERROR);
                TomlDiagnostic diagnostic = new TomlDiagnostic(tool.location(), diagnosticInfo, message);
                outStream.println(diagnostic);
                buildToolResolution.getDiagnosticList().add(diagnostic);
                tools.remove(tool);
            }
        }
    }

    private boolean isToolLocallyAvailable(String org, String name, String version) {
        Path toolCacheDir = BuildToolUtils.getCentralBalaDirPath().resolve(org).resolve(name);
        if (toolCacheDir.toFile().isDirectory()) {
            try (Stream<Path> versions = Files.list(toolCacheDir)) {
                return versions.anyMatch(path ->
                        path != null && path.getFileName() != null && version != null
                                && version.equals(path.getFileName().toString()));
            } catch (IOException e) {
                throw new ProjectException("Error while looking for locally available tools: " + e);
            }
        }
        return false;
    }

    private void pullToolFromCentral(String toolId, String version) throws CentralClientException {
        String supportedPlatform = Arrays.stream(JvmTarget.values())
                .map(JvmTarget::code)
                .collect(Collectors.joining(","));
        Path balaCacheDirPath = BuildToolUtils.getCentralBalaDirPath();
        Settings settings;
        try {
            settings = RepoUtils.readSettings();
            // Ignore Settings.toml diagnostics in the pull command
        } catch (SettingsTomlException e) {
            // Ignore 'Settings.toml' parsing errors and return empty Settings object
            settings = Settings.from();
        }
        System.setProperty(CentralClientConstants.ENABLE_OUTPUT_STREAM, Boolean.TRUE.toString());
        CentralAPIClient client = new CentralAPIClient(RepoUtils.getRemoteRepoURL(),
                initializeProxy(settings.getProxy()), settings.getProxy().username(),
                settings.getProxy().password(), getAccessTokenOfCLI(settings),
                settings.getCentral().getConnectTimeout(),
                settings.getCentral().getReadTimeout(), settings.getCentral().getWriteTimeout(),
                settings.getCentral().getCallTimeout());
        String[] toolInfo = client.pullTool(toolId, version, balaCacheDirPath, supportedPlatform,
                RepoUtils.getBallerinaVersion(), false);
        boolean isPulled = Boolean.parseBoolean(toolInfo[0]);
        if (isPulled) {
            outStream.println("tool '" + toolId + ":" + version + "' pulled successfully.");
        } else {
            outStream.println("tool '" + toolId + ":" + version + "' is already available locally.");
        }
    }

    private ClassLoader createToolClassLoader(List<BuildTool> resolvedTools) {
        List<File> toolJars = getToolCommandJarAndDependencyJars(resolvedTools);
        URL[] urls = toolJars.stream()
                .map(file -> {
                    try {
                        return file.toURI().toURL();
                    } catch (MalformedURLException e) {
                        throw LauncherUtils.createUsageExceptionWithHelp("invalid tool jar: " + file
                                .getAbsolutePath());
                    }
                })
                .toArray(URL[]::new);
        ClassLoader systemClassLoader = this.getClass().getClassLoader();
        return new URLClassLoader(urls, systemClassLoader);
    }

    private void printToolSkipWarning(PackageManifest.Tool toolEntry) {
        outStream.printf("WARNING: Execution of '%s:%s' is skipped due to errors%n", toolEntry
                .type().value(), toolEntry.id() != null ? toolEntry.id().value() : "");
    }

    private void reloadProject(Project project) {
        PackageConfig packageConfig = PackageConfigCreator.createBuildProjectConfig(project.sourceRoot(),
                project.buildOptions().disableSyntaxTree());
        project.addPackage(packageConfig);
    }

    private static List<File> getToolCommandJarAndDependencyJars(List<BuildTool> resolvedTools) {
        Path centralBalaDirPath = BuildToolUtils.getCentralBalaDirPath();
        return resolvedTools.stream()
                .map(tool -> findJarFiles(CommandUtil.getPlatformSpecificBalaPath(
                                tool.org().value(),
                                tool.name().value(),
                                tool.version().toString(),
                                centralBalaDirPath)
                        .resolve(TOOL).resolve(LIBS)
                        .toFile()))
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }
}
