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
import io.ballerina.projects.internal.BalToolsManifestBuilder;
import io.ballerina.projects.util.BuildToolsUtil;
import io.ballerina.projects.internal.PackageDiagnostic;
import io.ballerina.projects.internal.ProjectDiagnosticErrorCode;
import io.ballerina.projects.util.ProjectUtils;
import io.ballerina.toml.semantic.diagnostics.TomlDiagnostic;
import io.ballerina.toml.semantic.diagnostics.TomlNodeLocation;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticInfo;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import org.ballerinalang.central.client.CentralAPIClient;
import org.ballerinalang.central.client.exceptions.CentralClientException;
import org.ballerinalang.central.client.model.ToolResolutionCentralRequest;
import org.ballerinalang.central.client.model.ToolResolutionCentralResponse;
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

import static io.ballerina.projects.util.BalToolsUtil.BAL_TOOLS_TOML_PATH;
import static io.ballerina.projects.util.BalToolsUtil.DIST_BAL_TOOLS_TOML_PATH;
import static io.ballerina.projects.util.ProjectConstants.CENTRAL_REPOSITORY_CACHE_NAME;
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
        Map<PackageManifest.Tool.Field, ToolContext> toolContextMap = currentProject.getToolContextMap();
        if (toolContextMap == null || toolContextMap.isEmpty()) {
            return;
        }
        List<BuildTool> buildTools = new ArrayList<>();
        Set<String> toolIds = new HashSet<>();
        for (ToolContext toolContext: toolContextMap.values()) {
            // Populate the tools needed to resolve
            BuildToolId toolId = BuildToolId.from(toolContext.type().split("\\.")[0]);
            TomlNodeLocation location = BuildToolsUtil.getFirstToolEntryLocation(
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
            Optional<CodeGeneratorTool> targetTool = BuildToolsUtil.getTargetTool(tool.id().value(), toolServiceLoader);
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
        PackageLockingMode packageLockingMode = getPackageLockingMode(currentProject);
        updateLockedToolDependencyVersions(resolutionRequiredTools, currentProject);
        try {
            resolvedTools.addAll(resolveToolVersions(packageLockingMode, currentProject.buildOptions().offlineBuild(),
                    resolutionRequiredTools));
        } catch (CentralClientException e) {
            DiagnosticInfo diagnosticInfo = new DiagnosticInfo(
                    ProjectDiagnosticErrorCode.CENTRAL_CONNECTION_ERROR.diagnosticId(),
                    "connection to central failed. Continuing the tool resolution offline, reason: '"
                            + e.getMessage() + "'",
                    DiagnosticSeverity.WARNING);
            PackageDiagnostic diagnostic = new PackageDiagnostic(diagnosticInfo,
                    currentProject.currentPackage().descriptor().name().toString());
            diagnosticList.add(diagnostic);
            resolvedTools.addAll(getToolResolutionResponseOffline(resolutionRequiredTools, packageLockingMode));
        }
    }

    private List<BuildTool> resolveToolVersions(PackageLockingMode packageLockingMode, boolean offline,
                                                List<BuildTool> unresolvedTools)
            throws CentralClientException {
        if (offline) {
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
            String id = tool.id().toString();
            PackageVersion version = tool.version();

            Optional<BalToolsManifest.Tool> latestCompatibleVersion = BuildToolsUtil
                    .getCompatibleToolVersionsAvailableLocally(tool, version, packageLockingMode);;
            if (latestCompatibleVersion.isEmpty()) {
                String toolIdAndVersionOpt = id + (version == null ? "" : ":" + tool.version().toString());
                TomlDiagnostic diagnostic = BuildToolsUtil.getCannotResolveBuildToolDiagnostic(toolIdAndVersionOpt,
                        tool.location());
                diagnosticList.add(diagnostic);
                continue;
            }
            BalToolsManifest.Tool latestTool = latestCompatibleVersion.get();
            resolvedTools.add(BuildTool.from(
                    tool.id(),
                    PackageOrg.from(latestTool.org()),
                    PackageName.from(latestTool.name()),
                    PackageVersion.from(latestTool.version()),
                    tool.location(),
                    latestTool.repository()));
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
        settings = RepoUtils.readSettings();
        CentralAPIClient client = new CentralAPIClient(RepoUtils.getRemoteRepoURL(),
                initializeProxy(settings.getProxy()), settings.getProxy().username(),
                settings.getProxy().password(), getAccessTokenOfCLI(settings),
                settings.getCentral().getConnectTimeout(),
                settings.getCentral().getReadTimeout(), settings.getCentral().getWriteTimeout(),
                settings.getCentral().getCallTimeout(), settings.getCentral().getMaxRetries());
        String supportedPlatform = Arrays.stream(JvmTarget.values())
                .map(JvmTarget::code)
                .collect(Collectors.joining(","));
        ToolResolutionCentralResponse packageResolutionResponse;
        packageResolutionResponse = client.resolveToolDependencies(
                toolResolutionRequest, supportedPlatform, RepoUtils.getBallerinaVersion());
        List<ToolResolutionCentralResponse.ResolvedTool> resolved = packageResolutionResponse.resolved();
        List<ToolResolutionCentralResponse.UnresolvedTool> unresolved = packageResolutionResponse.unresolved();
        for (ToolResolutionCentralResponse.UnresolvedTool tool : unresolved) {
            TomlNodeLocation location = BuildToolsUtil.getFirstToolEntryLocation(
                    tool.id(), packageContext.packageManifest().tools());
            TomlDiagnostic diagnostic = BuildToolsUtil.getCannotResolveBuildToolDiagnostic(tool.id(), location);
            diagnosticList.add(diagnostic);
        }
        List<BuildTool> resolvedTools = new ArrayList<>();
        for (ToolResolutionCentralResponse.ResolvedTool tool : resolved) {
            TomlNodeLocation location = BuildToolsUtil.getFirstToolEntryLocation(tool.id(),
                    packageContext.packageManifest().tools());
            if (tool.version() == null || tool.name() == null || tool.org() == null) {
                TomlDiagnostic diagnostic = BuildToolsUtil.getCannotResolveBuildToolDiagnostic(tool.id(), location);
                diagnosticList.add(diagnostic);
                continue;
            }
            try {
                PackageVersion.from(tool.version());
            } catch (ProjectException ignore) {
                TomlDiagnostic diagnostic = BuildToolsUtil.getCannotResolveBuildToolDiagnostic(tool.id(), location);
                diagnosticList.add(diagnostic);
                continue;
            }
            BuildTool buildTool = BuildTool.from(
                    BuildToolId.from(tool.id()),
                    PackageOrg.from(tool.org()),
                    PackageName.from(tool.name()),
                    PackageVersion.from(tool.version()),
                    location
            );
            resolvedTools.add(buildTool);
            BuildToolsUtil.addToolToBalToolsToml(buildTool);
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
