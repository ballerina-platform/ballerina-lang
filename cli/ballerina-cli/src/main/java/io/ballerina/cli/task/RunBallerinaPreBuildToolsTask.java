/*
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
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

import com.google.gson.JsonSyntaxException;
import io.ballerina.cli.cmd.CommandUtil;
import io.ballerina.cli.launcher.LauncherUtils;
import io.ballerina.cli.utils.FileUtils;
import io.ballerina.projects.BuildTool;
import io.ballerina.projects.BuildToolId;
import io.ballerina.projects.DependencyManifest;
import io.ballerina.projects.Diagnostics;
import io.ballerina.projects.JvmTarget;
import io.ballerina.projects.PackageName;
import io.ballerina.projects.PackageOrg;
import io.ballerina.projects.PackageVersion;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.SemanticVersion;
import io.ballerina.projects.Settings;
import io.ballerina.projects.buildtools.CodeGeneratorTool;
import io.ballerina.projects.buildtools.ToolContext;
import io.ballerina.projects.environment.PackageLockingMode;
import io.ballerina.projects.environment.ToolResolutionRequest;
import io.ballerina.projects.internal.PackageDiagnostic;
import io.ballerina.projects.internal.ProjectDiagnosticErrorCode;
import io.ballerina.projects.internal.model.BuildJson;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.toml.api.Toml;
import io.ballerina.toml.validator.schema.Schema;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticInfo;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import org.ballerinalang.central.client.CentralAPIClient;
import org.ballerinalang.central.client.CentralClientConstants;
import org.ballerinalang.central.client.exceptions.CentralClientException;
import org.ballerinalang.central.client.model.ToolResolutionCentralRequest;
import org.ballerinalang.central.client.model.ToolResolutionCentralResponse;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.ballerina.cli.launcher.LauncherUtils.createLauncherException;
import static io.ballerina.cli.launcher.util.BalToolsUtil.findJarFiles;
import static io.ballerina.projects.JBallerinaBalaWriter.LIBS;
import static io.ballerina.projects.JBallerinaBalaWriter.TOOL;
import static io.ballerina.projects.PackageManifest.Tool;
import static io.ballerina.projects.util.ProjectConstants.BALA_DIR_NAME;
import static io.ballerina.projects.util.ProjectConstants.BUILD_FILE;
import static io.ballerina.projects.util.ProjectConstants.CENTRAL_REPOSITORY_CACHE_NAME;
import static io.ballerina.projects.util.ProjectConstants.REPOSITORIES_DIR;
import static io.ballerina.projects.util.ProjectConstants.TOOL_DIAGNOSTIC_CODE_PREFIX;
import static io.ballerina.projects.util.ProjectUtils.getAccessTokenOfCLI;
import static io.ballerina.projects.util.ProjectUtils.initializeProxy;
import static io.ballerina.projects.util.ProjectUtils.readBuildJson;

/**
 * Task for running tools integrated with the build.
 *
 * @since 2201.9.0
 */
public class RunBallerinaPreBuildToolsTask implements Task {
    public static final String DEFAULT_VERSION = "0.0.0";
    private final PrintStream outStream;
    private ClassLoader toolClassLoader = this.getClass().getClassLoader();
    private final Map<String, ToolContext> toolContextMap = new HashMap<>();

    public RunBallerinaPreBuildToolsTask(PrintStream out) {
        this.outStream = out;
    }

    @Override
    public void execute(Project project) {
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
        List<BuildTool> buildTools = new ArrayList<>();
        Set<String> toolIds = new HashSet<>();
        for (Tool toolEntry: toolEntries) {
            // Populate tool context
            ToolContext toolContext = ToolContext.from(toolEntry, project.currentPackage());
            toolContextMap.put(toolEntry.id(), toolContext);

            // Populate the tools needed to resolve
            BuildToolId toolId = BuildToolId.from(toolEntry.type().split("\\.")[0]);
            if (toolIds.add(toolId.value())) {
                buildTools.add(BuildTool.from(toolId, null, null, null));
            }
        }

        ServiceLoader<CodeGeneratorTool> toolServiceLoader = ServiceLoader.load(
                CodeGeneratorTool.class, toolClassLoader);

        // Load platform provided tools
        List<BuildTool> defaultVersionedTools = new ArrayList<>();
        List<BuildTool> resolutionRequiredTools = new ArrayList<>();
        for (BuildTool tool : buildTools) {
            Optional<CodeGeneratorTool> targetTool = getTargetTool(tool.id().value(), toolServiceLoader);
            if (targetTool.isEmpty()) {
                resolutionRequiredTools.add(tool);
            } else {
                tool.setVersion(PackageVersion.from(DEFAULT_VERSION));
                defaultVersionedTools.add(tool);
            }
        }

        // Resolve the versions of the tools
        if (!resolutionRequiredTools.isEmpty()) {
            List<BuildTool> resolvedTools = resolveToolVersions(project, resolutionRequiredTools);
            if (!resolvedTools.isEmpty()) {
                pullLocallyUnavailableTools(resolvedTools);
                toolClassLoader = createToolClassLoader(resolvedTools);
                toolServiceLoader = ServiceLoader.load(CodeGeneratorTool.class, toolClassLoader);
            }
        }

        for (Tool toolEntry : toolEntries) {
            String commandName = toolEntry.type();
            ToolContext toolContext = toolContextMap.get(toolEntry.id());
            Optional<CodeGeneratorTool> targetTool = getTargetTool(commandName, toolServiceLoader);
            if (targetTool.isEmpty()) {
                reportBuildToolNotFoundDiagnostic(toolEntry.id(), toolContextMap.get(toolEntry.id()));
                continue;
            }
            boolean hasOptionErrors = false;
            try {
                // validate the options toml and report diagnostics
                hasOptionErrors = validateOptionsToml(toolEntry.optionsToml(), commandName);
                if (hasOptionErrors) {
                    DiagnosticInfo diagnosticInfo = new DiagnosticInfo(
                            ProjectDiagnosticErrorCode.TOOL_OPTIONS_VALIDATION_FAILED.diagnosticId(),
                            ProjectDiagnosticErrorCode.TOOL_OPTIONS_VALIDATION_FAILED.messageKey(),
                            DiagnosticSeverity.ERROR);
                    PackageDiagnostic diagnostic = new PackageDiagnostic(diagnosticInfo, toolEntry.type());
                    toolContext.reportDiagnostic(diagnostic);
                }
            } catch (IOException e) {
                // if there is no options toml, we warn the user and continue
                outStream.printf("WARNING: Skipping validation of tool options for tool %s(%s) " +
                        "due to: %s%n", toolEntry.type(), toolEntry.id(), e.getMessage());
            }
            // if manifest errors or options table validation errors
            if (toolEntry.hasErrorDiagnostic() || hasOptionErrors) {
                outStream.printf("WARNING: Skipping execution of build tool %s(%s) as Ballerina.toml " +
                        "contains errors%n%n", toolEntry.type(), toolEntry.id() != null ? toolEntry.id() : "");
                continue;
            }

            // Execute the build tools
            try {
                this.outStream.printf("\t%s(%s)%n%n", toolEntry.type(), toolEntry.id());
                targetTool.get().execute(toolContext);
                for (Diagnostic d : toolContext.diagnostics()) {
                    if (d.toString().contains("(1:1,1:1)")) {
                        outStream.println(new PackageDiagnostic(d.diagnosticInfo(), toolContext.toolId()));
                    } else {
                        outStream.println(new PackageDiagnostic(d.diagnosticInfo(), d.location()));
                    }
                }
                toolContextMap.put(toolEntry.id(), toolContext);
            } catch (Exception e) {
                throw createLauncherException(e.getMessage());
            }
        }
        project.setToolContextMap(toolContextMap);
    }

    private Optional<CodeGeneratorTool> getTargetTool(
            String commandName, ServiceLoader<CodeGeneratorTool> buildRunners) {
        for (CodeGeneratorTool buildRunner : buildRunners) {
            if (buildRunner.toolName().equals(commandName)) {
                return Optional.of(buildRunner);
            }
        }
        return Optional.empty();
    }

    private boolean validateOptionsToml(Toml optionsToml, String toolName) throws IOException {
        if (optionsToml == null) {
            return validateEmptyOptionsToml(toolName);
        }
        FileUtils.validateToml(optionsToml, toolName, toolClassLoader);
        optionsToml.diagnostics().forEach(outStream::println);
        return !Diagnostics.filterErrors(optionsToml.diagnostics()).isEmpty();
    }

    private boolean validateEmptyOptionsToml(String toolName) throws IOException {
        Schema schema = Schema.from(FileUtils.readSchema(toolName, toolClassLoader));
        List<String> requiredFields = schema.required();
        if (!requiredFields.isEmpty()) {
            for (String field: requiredFields) {
                DiagnosticInfo diagnosticInfo = new DiagnosticInfo(
                        ProjectDiagnosticErrorCode.MISSING_TOOL_PROPERTIES_IN_BALLERINA_TOML.diagnosticId(),
                        String.format("missing required optional field '%s'", field),
                        DiagnosticSeverity.ERROR);
                PackageDiagnostic diagnostic = new PackageDiagnostic(diagnosticInfo,
                        toolName);
                this.outStream.println(diagnostic);
            }
            return true;
        }
        this.outStream.printf("WARNING: Skipping validation of tool options for tool %s due to: " +
                "No tool options found for%n", toolName);
        return false;
    }

    private List<BuildTool> resolveToolVersions(Project project, List<BuildTool> unresolvedTools) {
        PackageLockingMode packageLockingMode = getPackageLockingMode(project);
        Set<ToolResolutionRequest> resolutionRequests = getToolResolutionRequests(unresolvedTools, project,
                packageLockingMode);
        ToolResolutionCentralRequest toolResolutionRequest = createToolResolutionRequests(resolutionRequests);
        // TODO: Remove mock
//        return getToolResolutionResponse(toolResolutionRequest);
        return getMockToolResolutionResponse(toolResolutionRequest);
    }

    private void pullLocallyUnavailableTools(List<BuildTool> tools) {
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
                throw createLauncherException(
                        "failed to pull build tool '" + toolId + ":" + version + "' from Ballerina Central: "
                                + e.getMessage());
            } catch (ProjectException e) {
                throw createLauncherException(
                        "failed to resolve build tool '" + toolId + ":" + version + "' from the local cache: "
                                + e.getMessage());
            }
        }
    }

    private Set<ToolResolutionRequest> getToolResolutionRequests(List<BuildTool> unresolvedTools, Project project,
                                                                 PackageLockingMode packageLockingMode) {
        Set<ToolResolutionRequest> resolutionRequests = new HashSet<>();
        updateLockedToolDependencyVersions(unresolvedTools, project);
        for (BuildTool tool : unresolvedTools) {
            resolutionRequests.add(ToolResolutionRequest.from(tool, packageLockingMode));
        }
        return resolutionRequests;
    }

    private void updateLockedToolDependencyVersions(List<BuildTool> unresolvedTools, Project project) {
        DependencyManifest dependencyManifest = project.currentPackage().dependencyManifest();
        if (dependencyManifest == null || dependencyManifest.tools() == null) {
            return;
        }
        for (BuildTool tool : unresolvedTools) {
            for (DependencyManifest.Tool toolDependency : dependencyManifest.tools()) {
                if (toolDependency.id().equals(tool.id().value())) {
                    tool.setVersion(toolDependency.version());
                    break;
                }
            }
        }
    }

    private PackageLockingMode getPackageLockingMode(Project project) {
        boolean sticky = getSticky(project);

        // new project
        if (project.currentPackage().dependenciesToml().isEmpty()) {
            if (sticky) {
                return PackageLockingMode.MEDIUM;
            }
            return PackageLockingMode.SOFT;
        }

        // pre-built project
        if (sticky) {
            return PackageLockingMode.HARD;
        }
        SemanticVersion prevDistributionVersion = project.currentPackage().dependencyManifest()
                .distributionVersion();
        SemanticVersion currentDistributionVersion = SemanticVersion.from(RepoUtils.getBallerinaShortVersion());

        if (prevDistributionVersion == null || currentDistributionVersion.greaterThan(prevDistributionVersion)) {
            return PackageLockingMode.SOFT;
        }
        return PackageLockingMode.MEDIUM;
    }

    private boolean getSticky(Project project) {
        boolean sticky = project.buildOptions().sticky();
        if (sticky) {
            return true;
        }

        // set sticky if `build` file exists and `last_update_time` not passed 24 hours
        Path buildFilePath = project.targetDir().resolve(BUILD_FILE);
        if (Files.exists(buildFilePath) && buildFilePath.toFile().length() > 0) {
            try {
                BuildJson buildJson = readBuildJson(buildFilePath);
                // if distribution is not same, we anyway return sticky as false
                if (buildJson != null && buildJson.distributionVersion() != null &&
                        buildJson.distributionVersion().equals(RepoUtils.getBallerinaShortVersion()) &&
                        !buildJson.isExpiredLastUpdateTime()) {
                    return true;
                }
            } catch (IOException | JsonSyntaxException e) {
                // ignore
            }
        }
        return false;
    }

    private ToolResolutionCentralRequest createToolResolutionRequests(Set<ToolResolutionRequest> resolutionRequests) {
        ToolResolutionCentralRequest toolResolutionRequest = new ToolResolutionCentralRequest();
        for (ToolResolutionRequest resolutionRequest : resolutionRequests) {
            ToolResolutionCentralRequest.Mode mode = switch (resolutionRequest.packageLockingMode()) {
                case HARD -> ToolResolutionCentralRequest.Mode.HARD;
                case MEDIUM -> ToolResolutionCentralRequest.Mode.MEDIUM;
                case SOFT -> ToolResolutionCentralRequest.Mode.SOFT;
            };
            String version = resolutionRequest.version().map(v -> v.value().toString()).orElse("");
            toolResolutionRequest.addTool(resolutionRequest.id().toString(), version, mode);
        }
        return toolResolutionRequest;
    }


    private List<BuildTool> getToolResolutionResponse(ToolResolutionCentralRequest toolResolutionRequest) {
        Settings settings;
        try {
            settings = RepoUtils.readSettings();
        } catch (SettingsTomlException e) {
            settings = Settings.from();
        }
        CentralAPIClient client = new CentralAPIClient(RepoUtils.getRemoteRepoURL(),
                initializeProxy(settings.getProxy()), settings.getProxy().username(),
                settings.getProxy().password(), getAccessTokenOfCLI(settings),
                settings.getCentral().getConnectTimeout(),
                settings.getCentral().getReadTimeout(), settings.getCentral().getWriteTimeout(),
                settings.getCentral().getCallTimeout());
        String supportedPlatform = Arrays.stream(JvmTarget.values())
                .map(JvmTarget::code)
                .collect(Collectors.joining(","));
        ToolResolutionCentralResponse packageResolutionResponse;
        try {
            packageResolutionResponse = client.resolveToolDependencies(
                    toolResolutionRequest, supportedPlatform, RepoUtils.getBallerinaVersion());
        } catch (CentralClientException e) {
            throw createLauncherException("failed to resolve tool dependencies from Ballerina Central: "
                    + e.getMessage());
        }
        List<ToolResolutionCentralResponse.ResolvedTool> resolved = packageResolutionResponse.resolved();
        List<ToolResolutionCentralResponse.UnresolvedTool> unresolved = packageResolutionResponse.unresolved();
        for (ToolResolutionCentralResponse.UnresolvedTool tool : unresolved) {
            reportBuildToolNotFoundDiagnostic(tool.id(), toolContextMap.get(tool.id()));
        }
        List<BuildTool> resolvedTools = new ArrayList<>();
        for (ToolResolutionCentralResponse.ResolvedTool tool : resolved) {
            if (tool.id() == null || tool.version() == null || tool.name() == null || tool.org() == null) {
                reportBuildToolNotFoundDiagnostic(tool.id(), toolContextMap.get(tool.id()));
                continue;
            }
            try {
                PackageVersion.from(tool.version());
            } catch (ProjectException ignore) {
                reportBuildToolNotFoundDiagnostic(tool.id(), toolContextMap.get(tool.id()));
                continue;
            }
            resolvedTools.add(BuildTool.from(
                    BuildToolId.from(tool.id()),
                    PackageVersion.from(tool.version()),
                    PackageName.from(tool.name()),
                    PackageOrg.from(tool.org())
            ));
        }
        return resolvedTools;
    }

    private List<BuildTool> getMockToolResolutionResponse(ToolResolutionCentralRequest toolResolutionRequest) {
        return toolResolutionRequest.tools().stream().map(tool1 -> {
            switch (tool1.getId()) {
                case "dummy-tool.read" -> {
                    return BuildTool.from(BuildToolId.from("dummy_tool.read"), PackageVersion.from("0.1.0"),
                                PackageName.from("dummypkg"), PackageOrg.from("gayaldassanayake"));
                }
                case "dummy-tool.write" -> {
                    return BuildTool.from(BuildToolId.from("dummy_tool.write"), PackageVersion.from("0.1.0"),
                                PackageName.from("dummypkg"), PackageOrg.from("gayaldassanayake"));
                }
                default -> {
                    return BuildTool.from(BuildToolId.from("dummy_tool"), PackageVersion.from("0.1.0"),
                            PackageName.from("dummypkg"), PackageOrg.from("gayaldassanayake"));
                }
            }
        }).toList();
    }

    private void reportBuildToolNotFoundDiagnostic(String toolId, ToolContext toolContext) {
        String message = "Build tool '" + toolId + "' not found";
        DiagnosticInfo diagnosticInfo = new DiagnosticInfo(
                ProjectDiagnosticErrorCode.BUILD_TOOL_NOT_FOUND.diagnosticId(), message, DiagnosticSeverity.ERROR);
        PackageDiagnostic diagnostic = new PackageDiagnostic(diagnosticInfo, toolId);
        this.outStream.println(diagnostic);
        toolContext.reportDiagnostic(diagnostic);
    }

    private boolean isToolLocallyAvailable(String org, String name, String version) {
        // TODO: Local repo support
        Path toolCacheDir = RepoUtils.createAndGetHomeReposPath()
                .resolve(REPOSITORIES_DIR).resolve(CENTRAL_REPOSITORY_CACHE_NAME)
                .resolve(ProjectConstants.BALA_DIR_NAME).resolve(org).resolve(name);
        if (toolCacheDir.toFile().isDirectory()) {
            try (Stream<Path> versions = Files.list(toolCacheDir)) {
                return versions.anyMatch(path -> path.getFileName().toString().equals(version));
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
        Path balaCacheDirPath = RepoUtils.createAndGetHomeReposPath()
                .resolve(REPOSITORIES_DIR).resolve(CENTRAL_REPOSITORY_CACHE_NAME)
                .resolve(ProjectConstants.BALA_DIR_NAME);
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

    // TODO: try to use common methods for this and the ToolCommand
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

    private static List<File> getToolCommandJarAndDependencyJars(List<BuildTool> resolvedTools) {
        Path userHomeDirPath = RepoUtils.createAndGetHomeReposPath();
        Path centralBalaDirPath = userHomeDirPath.resolve(
                Path.of(REPOSITORIES_DIR, CENTRAL_REPOSITORY_CACHE_NAME, BALA_DIR_NAME));
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
