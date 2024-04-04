/*
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
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

import io.ballerina.projects.buildtools.CodeGeneratorTool;
import io.ballerina.projects.buildtools.ToolContext;
import io.ballerina.projects.environment.PackageLockingMode;
import io.ballerina.projects.environment.ToolResolutionRequest;
import io.ballerina.projects.util.BuildToolUtils;
import io.ballerina.projects.util.ProjectUtils;
import io.ballerina.toml.semantic.diagnostics.TomlDiagnostic;
import io.ballerina.toml.semantic.diagnostics.TomlNodeLocation;
import io.ballerina.tools.diagnostics.Diagnostic;
import org.ballerinalang.central.client.CentralAPIClient;
import org.ballerinalang.central.client.exceptions.CentralClientException;
import org.ballerinalang.central.client.model.ToolResolutionCentralRequest;
import org.ballerinalang.central.client.model.ToolResolutionCentralResponse;
import org.ballerinalang.toml.exceptions.SettingsTomlException;
import org.wso2.ballerinalang.util.RepoUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Collectors;

import static io.ballerina.projects.util.ProjectConstants.DEFAULT_VERSION;
import static io.ballerina.projects.util.ProjectUtils.getAccessTokenOfCLI;
import static io.ballerina.projects.util.ProjectUtils.initializeProxy;

/**
 * {@code BuildToolResolution} Model for resolving tool dependencies.
 *
 * @since 2201.9.0
 */
public class BuildToolResolution {
    private static final String PACKAGE_NAME_PREFIX  = "tool_";
    private final PackageContext packageContext;
    private final List<BuildTool> resolvedTools;
    private final List<Diagnostic> diagnosticList;

    private BuildToolResolution(PackageContext packageContext) {
        resolvedTools = new ArrayList<>();
        diagnosticList = new ArrayList<>();
        this.packageContext = packageContext;
        resolveToolDependencies();
    }

    static BuildToolResolution from(PackageContext packageContext) {
        return new BuildToolResolution(packageContext);
    }

    /**
     * Return a list of all build-tool resolution diagnostics.
     *
     * @return the list of diagnostics
     */
    public List<Diagnostic> getDiagnosticList() {
        return diagnosticList;
    }

    /**
     * Get all tools that were resolved during the tool resolution process.
     *
     * @return the list of resolved tools
     */
    public List<BuildTool> getResolvedTools() {
        return resolvedTools;
    }

    private void resolveToolDependencies() {
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
            TomlNodeLocation location = BuildToolUtils.getFirstToolEntryLocation(
                    toolId.value(), packageContext.packageManifest().tools());
            if (toolIds.add(toolId.value())) {
                buildTools.add(BuildTool.from(toolId, null, null, null, location));
            }
        }
        ClassLoader toolClassLoader = this.getClass().getClassLoader();
        ServiceLoader<CodeGeneratorTool> toolServiceLoader = ServiceLoader.load(
                CodeGeneratorTool.class, toolClassLoader);

        // Find tools which need resolution
        List<BuildTool> resolutionRequiredTools = new ArrayList<>();
        for (BuildTool tool : buildTools) {
            Optional<CodeGeneratorTool> targetTool = BuildToolUtils.getTargetTool(tool.id().value(), toolServiceLoader);
            if (targetTool.isEmpty()) {
                resolutionRequiredTools.add(tool);
            } else {
                // If platform-provided, add to resolved tools with 0.0.0 version
                tool.setVersion(PackageVersion.from(DEFAULT_VERSION));
                tool.setOrg(PackageOrg.BALLERINA_ORG);
                tool.setName(PackageName.from(PACKAGE_NAME_PREFIX + tool.id()));
                resolvedTools.add(tool);
            }
        }
        if (resolutionRequiredTools.isEmpty()) {
            return;
        }
        try {
            resolvedTools.addAll(resolveToolVersions(currentProject, resolutionRequiredTools));
        } catch (CentralClientException e) {
            throw new ProjectException("Failed to resolve build tools: " + e.getMessage());
        }
    }

    private List<BuildTool> resolveToolVersions(Project project, List<BuildTool> unresolvedTools)
            throws CentralClientException {
        PackageLockingMode packageLockingMode = getPackageLockingMode(project);
        updateLockedToolDependencyVersions(unresolvedTools, project);
        if (project.buildOptions().offlineBuild()) {
            return getToolResolutionResponseOffline(unresolvedTools, packageLockingMode);
        }
        Set<ToolResolutionRequest> resolutionRequests = getToolResolutionRequests(unresolvedTools, packageLockingMode);
        ToolResolutionCentralRequest toolResolutionRequest = createToolResolutionRequests(resolutionRequests);
        return getToolResolutionResponse(toolResolutionRequest);
    }

    private PackageLockingMode getPackageLockingMode(Project project) {
        boolean sticky = ProjectUtils.getSticky(project);

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

    private List<BuildTool> getToolResolutionResponseOffline(List<BuildTool> unresolvedTools,
                                                             PackageLockingMode packageLockingMode) {
        List<BuildTool> resolvedTools = new ArrayList<>();
        for (BuildTool tool: unresolvedTools) {
            BuildToolId id = tool.id();
            PackageOrg org = tool.org();
            PackageName name = tool.name();
            PackageVersion version = tool.version();
            if (tool.org() == null || tool.name() == null) {
                TomlDiagnostic diagnostic = BuildToolUtils.getCannotResolveBuildToolDiagnostic(tool.id().value(),
                        tool.location());
                diagnosticList.add(diagnostic);
                continue;
            }
            List<SemanticVersion> versions = BuildToolUtils.getCompatibleToolVersionsInLocalCache(org, name);
            Optional<SemanticVersion> latestCompVersion =
                    BuildToolUtils.getLatestCompatibleVersion(version.value(), versions, packageLockingMode);
            if (latestCompVersion.isEmpty()) {
                String toolIdAndVersionOpt = tool.id().value()
                        + (tool.version() == null ? "" : ":" + tool.version().toString());
                TomlDiagnostic diagnostic = BuildToolUtils.getCannotResolveBuildToolDiagnostic(toolIdAndVersionOpt,
                        tool.location());
                diagnosticList.add(diagnostic);
                continue;
            }
            resolvedTools.add(BuildTool.from(id, org, name,
                    PackageVersion.from(latestCompVersion.get()), tool.location()));
        }
        return resolvedTools;
    }

    private Set<ToolResolutionRequest> getToolResolutionRequests(List<BuildTool> unresolvedTools,
                                                                 PackageLockingMode packageLockingMode) {
        Set<ToolResolutionRequest> resolutionRequests = new HashSet<>();
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
            TomlNodeLocation location = BuildToolUtils.getFirstToolEntryLocation(
                    tool.id(), packageContext.packageManifest().tools());
            TomlDiagnostic diagnostic = BuildToolUtils.getCannotResolveBuildToolDiagnostic(tool.id(), location);
            diagnosticList.add(diagnostic);
        }
        List<BuildTool> resolvedTools = new ArrayList<>();
        for (ToolResolutionCentralResponse.ResolvedTool tool : resolved) {
            TomlNodeLocation location = BuildToolUtils.getFirstToolEntryLocation(tool.id(),
                    packageContext.packageManifest().tools());
            if (tool.version() == null || tool.name() == null || tool.org() == null) {
                TomlDiagnostic diagnostic = BuildToolUtils.getCannotResolveBuildToolDiagnostic(tool.id(), location);
                diagnosticList.add(diagnostic);
                continue;
            }
            try {
                PackageVersion.from(tool.version());
            } catch (ProjectException ignore) {
                TomlDiagnostic diagnostic = BuildToolUtils.getCannotResolveBuildToolDiagnostic(tool.id(), location);
                diagnosticList.add(diagnostic);
                continue;
            }
            resolvedTools.add(BuildTool.from(
                    BuildToolId.from(tool.id()),
                    PackageOrg.from(tool.org()),
                    PackageName.from(tool.name()),
                    PackageVersion.from(tool.version()),
                    location
            ));
        }
        return resolvedTools;
    }

    private void updateLockedToolDependencyVersions(List<BuildTool> unresolvedTools, Project project) {
        DependencyManifest dependencyManifest = project.currentPackage().dependencyManifest();
        if (dependencyManifest == null || dependencyManifest.tools() == null) {
            return;
        }
        for (BuildTool tool : unresolvedTools) {
            for (DependencyManifest.Tool toolDependency : dependencyManifest.tools()) {
                if (toolDependency.id().equals(tool.id())) {
                    tool.setOrg(toolDependency.org());
                    tool.setName(toolDependency.name());
                    tool.setVersion(toolDependency.version());
                    break;
                }
            }
        }
    }
}
