/*
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
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

package io.ballerina.projects;

import com.google.gson.JsonSyntaxException;
import io.ballerina.projects.buildtools.CodeGeneratorTool;
import io.ballerina.projects.buildtools.ToolContext;
import io.ballerina.projects.environment.PackageLockingMode;
import io.ballerina.projects.environment.ToolResolutionRequest;
import io.ballerina.projects.internal.PackageDiagnostic;
import io.ballerina.projects.internal.ProjectDiagnosticErrorCode;
import io.ballerina.projects.internal.model.BuildJson;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticInfo;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import org.ballerinalang.central.client.CentralAPIClient;
import org.ballerinalang.central.client.exceptions.CentralClientException;
import org.ballerinalang.central.client.model.ToolResolutionCentralRequest;
import org.ballerinalang.central.client.model.ToolResolutionCentralResponse;
import org.ballerinalang.toml.exceptions.SettingsTomlException;
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Collectors;

import static io.ballerina.projects.util.ProjectConstants.BUILD_FILE;
import static io.ballerina.projects.util.ProjectConstants.DEFAULT_VERSION;
import static io.ballerina.projects.util.ProjectUtils.getAccessTokenOfCLI;
import static io.ballerina.projects.util.ProjectUtils.initializeProxy;
import static io.ballerina.projects.util.ProjectUtils.readBuildJson;

/**
 * {@code ToolResolution} Model for resolving tool dependencies.
 *
 * @since 2201.9.0
 */
public class ToolResolution {
    private final PackageContext packageContext;
    private final List<BuildTool> resolvedTools = new ArrayList<>();
    private final List<Diagnostic> diagnosticList = new ArrayList<>();

    private ToolResolution(PackageContext packageContext) {
        this.packageContext = packageContext;
        resolveToolDependencies(packageContext);
    }

    static ToolResolution from(PackageContext packageContext) {
        return new ToolResolution(packageContext);
    }

    public List<Diagnostic> getDiagnosticList() {
        return diagnosticList;
    }

    public List<BuildTool> getResolvedTools() {
        return resolvedTools;
    }

    private void resolveToolDependencies(PackageContext packageContext) {
        Project currentProject = packageContext.project();
        Map<String, ToolContext> toolContextMap = currentProject.getToolContextMap();
        if (toolContextMap == null || toolContextMap.isEmpty()) {
            return;
        }
        List<BuildTool> buildTools = new ArrayList<>();
        Set<String> toolIds = new HashSet<>();
        for (ToolContext toolContext: toolContextMap.values()) {
            // Populate the tools needed to resolve
            BuildToolId toolId = BuildToolId.from(toolContext.type().split("\\.")[0]);
            if (toolIds.add(toolId.value())) {
                buildTools.add(BuildTool.from(toolId, null, null, null));
            }
        }
        ClassLoader toolClassLoader = this.getClass().getClassLoader();
        ServiceLoader<CodeGeneratorTool> toolServiceLoader = ServiceLoader.load(
                CodeGeneratorTool.class, toolClassLoader);

        // Find tools which need resolution
        List<BuildTool> resolutionRequiredTools = new ArrayList<>();
        for (BuildTool tool : buildTools) {
            Optional<CodeGeneratorTool> targetTool = getTargetTool(tool.id().value(), toolServiceLoader);
            if (targetTool.isEmpty()) {
                resolutionRequiredTools.add(tool);
            } else {
                // If platform-provided, add to resolved tools with 0.0.0 version
                tool.setVersion(PackageVersion.from(DEFAULT_VERSION));
                resolvedTools.add(tool);
            }
        }
        resolvedTools.addAll(resolveToolVersions(currentProject, resolutionRequiredTools));
    }

    // TODO: redundant here and in RunBallerinaPreBuildToolsTask
    private Optional<CodeGeneratorTool> getTargetTool(
            String commandName, ServiceLoader<CodeGeneratorTool> buildRunners) {
        for (CodeGeneratorTool buildRunner : buildRunners) {
            if (deriveSubcommandName(buildRunner.toolName()).equals(commandName)) {
                return Optional.of(buildRunner);
            }
        }
        return Optional.empty();
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

    private Set<ToolResolutionRequest> getToolResolutionRequests(List<BuildTool> unresolvedTools, Project project,
                                                                 PackageLockingMode packageLockingMode) {
        Set<ToolResolutionRequest> resolutionRequests = new HashSet<>();
        updateLockedToolDependencyVersions(unresolvedTools, project);
        for (BuildTool tool : unresolvedTools) {
            resolutionRequests.add(ToolResolutionRequest.from(tool, packageLockingMode));
        }
        return resolutionRequests;
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

    private List<BuildTool> getToolResolutionResponse(ToolResolutionCentralRequest toolResolutionRequest)
            throws CentralClientException {
        Map<String, ToolContext> toolContextMap = packageContext.project().getToolContextMap();
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
        packageResolutionResponse = client.resolveToolDependencies(
                toolResolutionRequest, supportedPlatform, RepoUtils.getBallerinaVersion());
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
                default -> {
                    return BuildTool.from(BuildToolId.from("dummy_tool"), PackageVersion.from("0.2.0"),
                            PackageName.from("dummypkg"), PackageOrg.from("gayaldassanayake"));
                }
            }
        }).toList();
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

    private void reportBuildToolNotFoundDiagnostic(String toolId, ToolContext toolContext) {
        String message = "Build tool '" + toolId + "' not found";
        DiagnosticInfo diagnosticInfo = new DiagnosticInfo(
                ProjectDiagnosticErrorCode.BUILD_TOOL_NOT_FOUND.diagnosticId(), message, DiagnosticSeverity.ERROR);
        PackageDiagnostic diagnostic = new PackageDiagnostic(diagnosticInfo, toolId);
        diagnosticList.add(diagnostic);
        toolContext.reportDiagnostic(diagnostic);
    }

    private String deriveSubcommandName(String[] toolName) {
        return Arrays.stream(toolName).reduce((s1, s2) -> s1 + "." + s2).orElse("");
    }
}
