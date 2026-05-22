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

import io.ballerina.projects.buildtools.ToolContext;
import io.ballerina.projects.environment.PackageLockingMode;
import io.ballerina.projects.environment.ToolResolutionRequest;
import io.ballerina.projects.internal.PackageDiagnostic;
import io.ballerina.projects.internal.ProjectDiagnosticErrorCode;
import io.ballerina.projects.internal.model.Repository;
import io.ballerina.projects.util.BalToolsUtil;
import io.ballerina.projects.util.BuildToolsUtil;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.projects.util.ProjectUtils;
import io.ballerina.toml.semantic.diagnostics.TomlDiagnostic;
import io.ballerina.toml.semantic.diagnostics.TomlNodeLocation;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticInfo;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import org.ballerinalang.central.client.exceptions.CentralClientException;
import org.ballerinalang.central.client.model.ToolResolutionCentralRequest;
import org.ballerinalang.central.client.model.ToolResolutionCentralResponse;
import org.ballerinalang.maven.bala.client.MavenResolverClient;
import org.ballerinalang.maven.bala.client.MavenResolverClientException;
import org.ballerinalang.maven.bala.client.model.ToolMavenMetadata;
import org.wso2.ballerinalang.util.RepoUtils;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * {@code BuildToolResolution} Model for resolving tool dependencies.
 *
 * @since 2201.9.0
 */
public class BuildToolResolution {
    private final PackageContext packageContext;
    private final List<BuildTool> resolvedTools;
    private final List<Diagnostic> diagnosticList;
    private final boolean offline;

    private BuildToolResolution(PackageContext packageContext, boolean offline) {
        resolvedTools = new ArrayList<>();
        diagnosticList = new ArrayList<>();
        this.packageContext = packageContext;
        this.offline = offline;
        resolveToolDependencies();
    }

    static BuildToolResolution from(PackageContext packageContext) {
        return new BuildToolResolution(packageContext, packageContext.project().buildOptions().offlineBuild());
    }

    static BuildToolResolution from(PackageContext packageContext, CompilationOptions compilationOptions) {
        return new BuildToolResolution(packageContext, compilationOptions.offlineBuild());
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

        PackageLockingMode packageLockingMode = getPackageLockingMode(currentProject);
        updateLockedToolDependencyVersions(buildTools, currentProject);
        List<BuildTool> resolvedTools = resolveToolVersions(packageLockingMode, buildTools);
        this.resolvedTools.addAll(resolvedTools);
    }

    private List<BuildTool> resolveToolVersions(PackageLockingMode packageLockingMode,
                                                List<BuildTool> unresolvedTools) {
        List<BuildTool> toolResolutionResponseOffline =
                getToolResolutionResponseOffline(unresolvedTools, packageLockingMode);
        if (offline) {
            reportDiagnosticsForUnresolvedTools(unresolvedTools, toolResolutionResponseOffline);
            return toolResolutionResponseOffline;
        }
        // Check if proxyCentral repository is configured
        Settings settings = RepoUtils.readSettings();
        Repository proxyCentralRepo = BalToolsUtil.findProxyCentralRepository(settings);
        Set<ToolResolutionRequest> resolutionRequests = getToolResolutionRequests(unresolvedTools, packageLockingMode);
        List<BuildTool> toolResolutionResponse;
        try {
            if (proxyCentralRepo != null) {
                toolResolutionResponse = resolveToolsViaMavenProxy(resolutionRequests, proxyCentralRepo, settings);
            } else {
                toolResolutionResponse = resolveToolsViaCentral(resolutionRequests);
            }
        } catch (CentralClientException e) {
            DiagnosticInfo diagnosticInfo = new DiagnosticInfo(
                    ProjectDiagnosticErrorCode.CENTRAL_CONNECTION_ERROR.diagnosticId(),
                    "connection to central failed. Continuing the tool resolution offline, reason: '"
                            + e.getMessage() + "'",
                    DiagnosticSeverity.WARNING);
            PackageDiagnostic diagnostic = new PackageDiagnostic(diagnosticInfo,
                    this.packageContext.descriptor().name().toString());
            diagnosticList.add(diagnostic);
            return toolResolutionResponseOffline;
        }

        ArrayList<BuildTool> resolvedTools = new ArrayList<>(
                Stream.of(toolResolutionResponseOffline, toolResolutionResponse)
                        .flatMap(Collection::stream).collect(Collectors.toMap(
                                BuildTool::id, // Use the tool ID as the key
                                Function.identity(), (tool1, tool2) -> {
                                    // Resolve conflicts by selecting the tool with the latest version
                                    SemanticVersion.VersionCompatibilityResult versionCompatibilityResult =
                                            tool1.version().compareTo(tool2.version());
                                    if (versionCompatibilityResult.equals(
                                            SemanticVersion.VersionCompatibilityResult.EQUAL)) {
                                        return tool1;
                                    }
                                    return tool2;
                                })).values());

        // Report errors for unresolved tools
        reportDiagnosticsForUnresolvedTools(unresolvedTools, resolvedTools);
        return resolvedTools;
    }

    private List<BuildTool> resolveToolsViaCentral(Set<ToolResolutionRequest> resolutionRequests)
            throws CentralClientException {
        List<BuildTool> toolResolutionResponse;
        ToolResolutionCentralRequest toolResolutionRequest = createToolResolutionCentralRequests(resolutionRequests);
        toolResolutionResponse = getToolResolutionCentralResponse(toolResolutionRequest);
        return toolResolutionResponse;
    }

    private void reportDiagnosticsForUnresolvedTools(
            List<BuildTool> unresolvedTools, List<BuildTool> toolResolutionResponseOffline) {
        unresolvedTools.stream().filter(tool ->
                toolResolutionResponseOffline.stream().noneMatch(resolvedTool ->
                        resolvedTool.id().equals(tool.id()))).forEach(tool -> {
            String toolIdWithVersion = tool.id().toString();
            if (tool.version() != null && !tool.version().toString().isEmpty()) {
                toolIdWithVersion += ":" + tool.version().toString();
            }
            TomlNodeLocation location = BuildToolsUtil.getFirstToolEntryLocation(
                    tool.id().toString(), packageContext.packageManifest().tools());
            TomlDiagnostic diagnostic = BuildToolsUtil.getCannotResolveBuildToolDiagnostic(
                    toolIdWithVersion, location);
            diagnosticList.add(diagnostic);
        });
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
            PackageVersion version = tool.version();

            Optional<BalToolsManifest.Tool> latestCompatibleVersion = BuildToolsUtil
                    .getCompatibleToolVersionsAvailableLocally(tool, version, packageLockingMode);
            if (latestCompatibleVersion.isEmpty()) {
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

    private ToolResolutionCentralRequest createToolResolutionCentralRequests(
            Set<ToolResolutionRequest> resolutionRequests) {
        ToolResolutionCentralRequest toolResolutionRequest = new ToolResolutionCentralRequest();
        for (ToolResolutionRequest resolutionRequest : resolutionRequests) {
            ToolResolutionCentralRequest.Mode mode = switch (resolutionRequest.packageLockingMode()) {
                case HARD, LOCKED -> ToolResolutionCentralRequest.Mode.HARD;
                case MEDIUM -> ToolResolutionCentralRequest.Mode.MEDIUM;
                case SOFT -> ToolResolutionCentralRequest.Mode.SOFT;
            };
            String version = resolutionRequest.version().map(v -> v.value().toString()).orElse("");
            toolResolutionRequest.addTool(resolutionRequest.id().toString(), version, mode);
        }
        return toolResolutionRequest;
    }

    private List<BuildTool> getToolResolutionCentralResponse(ToolResolutionCentralRequest toolResolutionRequest)
            throws CentralClientException {
        ToolResolutionCentralResponse packageResolutionResponse =
                BalToolsUtil.getLatestVersionsInCentral(toolResolutionRequest);
        List<ToolResolutionCentralResponse.ResolvedTool> resolved = packageResolutionResponse.resolved();
        List<BuildTool> resolvedTools = new ArrayList<>();
        for (ToolResolutionCentralResponse.ResolvedTool tool : resolved) {
            String toolId = tool.id();
            String version = tool.version();
            TomlNodeLocation location = BuildToolsUtil.getFirstToolEntryLocation(toolId,
                    packageContext.packageManifest().tools());
            if (version == null || tool.name() == null || tool.org() == null) {
                TomlDiagnostic diagnostic = BuildToolsUtil.getCannotResolveBuildToolDiagnostic(toolId, location);
                diagnosticList.add(diagnostic);
                continue;
            }
            try {
                PackageVersion.from(version);
            } catch (ProjectException ignore) {
                TomlDiagnostic diagnostic = BuildToolsUtil.getCannotResolveBuildToolDiagnostic(toolId, location);
                diagnosticList.add(diagnostic);
                continue;
            }
            try {
                BalToolsUtil.pullToolPackageFromRemote(toolId, version);
            } catch (CentralClientException e) {
                String message = "failed to pull build tool '" + toolId + ":" + version + "' from Ballerina Central: "
                        + e.getMessage();
                DiagnosticInfo diagnosticInfo = new DiagnosticInfo(
                        ProjectDiagnosticErrorCode.BUILD_TOOL_NOT_FOUND.diagnosticId(),
                        ProjectDiagnosticErrorCode.BUILD_TOOL_NOT_FOUND.messageKey(),
                        DiagnosticSeverity.WARNING);
                TomlDiagnostic diagnostic = new TomlDiagnostic(location, diagnosticInfo, message);

                diagnosticList.add(diagnostic);
            }
            BuildTool buildTool = BuildTool.from(
                    BuildToolId.from(toolId),
                    PackageOrg.from(tool.org()),
                    PackageName.from(tool.name()),
                    PackageVersion.from(version),
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

    /**
     * Resolve tools via Maven central proxy repository.
     *
     * @param toolResolutionRequest the tool resolution request
     * @param proxyCentralRepo      the proxy central repository configuration
     * @param settings              the settings containing proxy and other configs
     * @return the resolved tools response
     * @throws CentralClientException if resolution fails
     */
    private List<BuildTool> resolveToolsViaMavenProxy(
            Set<ToolResolutionRequest> toolResolutionRequest,
            Repository proxyCentralRepo,
            Settings settings) throws CentralClientException {
        List<BuildTool> resolvedTools = new ArrayList<>();
        Path localRepoPath = getProxyCentralLocalRepoPath(proxyCentralRepo);
        MavenResolverClient mavenClient;
        try {
            mavenClient = BalToolsUtil.initializeMavenClientWithProxyRepo(settings);
        } catch (MavenResolverClientException e) {
            throw new CentralClientException(e.getMessage());
        }
        String ballerinaVersion = RepoUtils.getBallerinaVersion();

        for (ToolResolutionRequest toolRequest : toolResolutionRequest) {
            try {
                BuildTool resolvedTool = resolveSingleToolViaMaven(
                        mavenClient, toolRequest, ballerinaVersion, localRepoPath);
                if (resolvedTool != null) {
                    BalToolsUtil.pullAndExtractToolFromMavenProxy(
                            resolvedTool.org().value(), resolvedTool.name().value(),
                            resolvedTool.version().toString(), mavenClient);
                    resolvedTools.add(resolvedTool);
                    BuildToolsUtil.addToolToBalToolsToml(resolvedTool);
                }

            } catch (MavenResolverClientException e) {
                // Tool resolution failed via Maven, skip and continue with next tool
            }
        }

        return resolvedTools;
    }

    private Path getProxyCentralLocalRepoPath(Repository proxyCentralRepo) {
        return proxyCentralRepo.path().orElse(
                ProjectUtils.createAndGetHomeReposPath()
                        .resolve(ProjectConstants.REPOSITORIES_DIR)
                        .resolve(ProjectConstants.CENTRAL_REPOSITORY_CACHE_NAME)
                        .resolve(ProjectConstants.BALA_DIR_NAME));
    }

    private BuildTool resolveSingleToolViaMaven(MavenResolverClient mavenClient,
                                                ToolResolutionRequest toolRequest,
                                                String ballerinaVersion,
                                                Path localRepoPath) throws MavenResolverClientException {
        String toolId = toolRequest.id().toString();
        TomlNodeLocation location = BuildToolsUtil.getFirstToolEntryLocation(toolId,
                packageContext.packageManifest().tools());
        ToolMavenMetadata toolMetadata =
                mavenClient.getToolMetadata(toolId, ballerinaVersion, localRepoPath);
        List<String> compatibleToolVersions = toolMetadata.getVersions();
        if (compatibleToolVersions.isEmpty()) {
            return null;
        }
        PackageVersion latestVersion = getLatestCompatibleToolVersion(
                toolRequest, compatibleToolVersions);

        if (latestVersion == null) {
            return null;
        }

        return BuildTool.from(
                BuildToolId.from(toolId),
                PackageOrg.from(toolMetadata.getOrg()),
                PackageName.from(toolMetadata.getName()),
                latestVersion,
                location
        );
    }

    private PackageVersion getLatestCompatibleToolVersion(ToolResolutionRequest toolRequest,
                                                          List<String> allToolVersions) {
        // Convert to SemanticVersion
        List<SemanticVersion> toolSemVers = allToolVersions.stream()
                .map(SemanticVersion::from)
                .toList();

        // Get the minimum version if specified
        SemanticVersion minSemVer = null;
        if (toolRequest.version().isPresent()) {
            minSemVer = SemanticVersion.from(toolRequest.version().get().value().toString());
        }

        // Get compatible versions based on locking mode
        ProjectUtils.CompatibleRange compatibilityRange = ProjectUtils.getCompatibleRange(
                minSemVer, toolRequest.packageLockingMode());
        List<SemanticVersion> compatibleVersions = ProjectUtils.getVersionsInCompatibleRange(
                minSemVer, toolSemVers, compatibilityRange);

        if (compatibleVersions.isEmpty()) {
            return null;
        }

        // Find and return the latest compatible version
        List<PackageVersion> toolVersions = compatibleVersions.stream()
                .map(v -> PackageVersion.from(v.toString()))
                .toList();

        PackageVersion latestVersion = toolVersions.getFirst();
        for (PackageVersion version : toolVersions) {
            latestVersion = ProjectUtils.getLatest(latestVersion, version);
        }

        return latestVersion;
    }
}

