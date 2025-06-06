/*
 *  Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com)
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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
package io.ballerina.projects.util;

import io.ballerina.projects.BalToolsManifest;
import io.ballerina.projects.JvmTarget;
import io.ballerina.projects.SemanticVersion;
import io.ballerina.projects.Settings;
import io.ballerina.projects.internal.BalaFiles;
import io.ballerina.projects.internal.model.PackageJson;
import org.ballerinalang.central.client.CentralAPIClient;
import org.ballerinalang.central.client.CentralClientConstants;
import org.ballerinalang.central.client.exceptions.CentralClientException;
import org.ballerinalang.central.client.model.ToolResolutionCentralRequest;
import org.ballerinalang.central.client.model.ToolResolutionCentralResponse;
import org.wso2.ballerinalang.util.RepoUtils;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static io.ballerina.projects.util.BuildToolsUtil.getCentralBalaDirPath;
import static io.ballerina.projects.util.ProjectConstants.BAL_TOOLS_TOML;
import static io.ballerina.projects.util.ProjectConstants.CENTRAL_REPOSITORY_CACHE_NAME;
import static io.ballerina.projects.util.ProjectConstants.CONFIG_DIR;
import static io.ballerina.projects.util.ProjectConstants.DIST_CACHE_DIRECTORY;
import static io.ballerina.projects.util.ProjectConstants.REPOSITORIES_DIR;
import static io.ballerina.projects.util.ProjectConstants.RESOURCE_DIR_NAME;
import static io.ballerina.projects.util.ProjectUtils.getAccessTokenOfCLI;
import static io.ballerina.projects.util.ProjectUtils.initializeProxy;

/**
 * Utility class for Bal tools.
 *
 * @since 2201.13.0
 */
public class BalToolsUtil {

    public static final Path BAL_TOOLS_TOML_PATH = RepoUtils.createAndGetHomeReposPath().resolve(
            Path.of(CONFIG_DIR, BAL_TOOLS_TOML));
    public static final Path DIST_BAL_TOOLS_TOML_PATH = ProjectUtils.getBalHomePath().resolve(RESOURCE_DIR_NAME)
            .resolve(BAL_TOOLS_TOML);

    private BalToolsUtil() {}

    /**
     * Check if the tool distribution version is compatible with the distribution version.
     *
     * @param distVersion   Local distribution version
     * @param toolDistVersion    Tool distribution version
     * @return true if compatible, false otherwise
     */
    public static boolean isCompatibleWithDistVersion(SemanticVersion distVersion, SemanticVersion toolDistVersion) {
        return distVersion.major() == toolDistVersion.major()
                && distVersion.minor() >= toolDistVersion.minor();
    }

    /**
     * Check if the tool distribution version is compatible with the current distribution version.
     *
     * @param org          organization name
     * @param name         tool name
     * @param version      tool version
     * @param repository   repository name
     * @return true if compatible, false otherwise
     */
    public static boolean isCompatibleWithPlatform(String org, String name, String version, String repository) {
        SemanticVersion currentDistVersion = SemanticVersion.from(RepoUtils.getBallerinaShortVersion());
        SemanticVersion toolDistVersion = getToolDistVersionFromCache(org, name, version, repository);
        return isCompatibleWithDistVersion(currentDistVersion, toolDistVersion);
    }

    /**
     * Check if the tool distribution version is compatible with the current distribution version.
     * Supports only tools in the central cache.
     *
     * @param org          organization name
     * @param name         tool name
     * @param version      tool version
     * @return true if compatible, false otherwise
     */
    public static boolean isCompatibleWithPlatform(String org, String name, String version) {
        SemanticVersion currentDistVersion = SemanticVersion.from(RepoUtils.getBallerinaShortVersion());
        SemanticVersion toolDistVersion = getToolDistVersionFromCache(org, name, version,
                CENTRAL_REPOSITORY_CACHE_NAME);
        return isCompatibleWithDistVersion(currentDistVersion, toolDistVersion);
    }

    /**
     * Compare the tool distribution version with the current distribution version.
     *
     * @param org          organization name
     * @param name         tool name
     * @param versions     tool version
     * @param repository   repository name
     * @return comparison result as SemanticVersion.VersionCompatibilityResult
     */
    public static SemanticVersion.VersionCompatibilityResult compareToolDistWithCurrentDist(
            String org, String name, String versions, String repository) {
        SemanticVersion currentDistVersion = SemanticVersion.from(RepoUtils.getBallerinaShortVersion());
        SemanticVersion toolDistVersion = getToolDistVersionFromCache(org, name, versions, repository);
        return toolDistVersion.compareTo(currentDistVersion);
    }

    /**
     * Get the in-built tool commands from the distribution.
     *
     * @param distBalToolsManifest   distribution bal tools manifest
     * @return set of in-built tool commands
     */
    public static Set<String> getInBuiltToolCommands(BalToolsManifest distBalToolsManifest) {
        return distBalToolsManifest.tools().keySet();
    }

    public static Path getRepoPath(String repoName) {
        if (ProjectConstants.LOCAL_REPOSITORY_NAME.equals(repoName)) {
            return ProjectUtils.createAndGetHomeReposPath().resolve(
                    Path.of(REPOSITORIES_DIR, ProjectConstants.LOCAL_REPOSITORY_NAME, ProjectConstants.BALA_DIR_NAME));
        } else if (ProjectConstants.DISTRIBUTION_REPOSITORY_NAME.equals(repoName)) {
            return ProjectUtils.getBalHomePath().resolve(
                    Path.of(DIST_CACHE_DIRECTORY, ProjectConstants.BALA_DIR_NAME));
        } else {
            return getCentralBalaDirPath();
        }
    }

    public static ToolResolutionCentralResponse getLatestVersionsInCentral(
            ToolResolutionCentralRequest toolResolutionRequest)
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
        return packageResolutionResponse;
    }

    public static BalToolsManifest.Tool pullToolPackageFromRemote(String toolId, String version)
            throws CentralClientException {
        String supportedPlatform = Arrays.stream(JvmTarget.values())
                .map(JvmTarget::code)
                .collect(Collectors.joining(","));
        Path balaCacheDirPath = BuildToolsUtil.getCentralBalaDirPath();
        Settings settings;
        settings = RepoUtils.readSettings();
        // Ignore Settings.toml diagnostics in the pull command

        System.setProperty(CentralClientConstants.ENABLE_OUTPUT_STREAM, Boolean.TRUE.toString());
        CentralAPIClient client = new CentralAPIClient(RepoUtils.getRemoteRepoURL(),
                initializeProxy(settings.getProxy()), settings.getProxy().username(),
                settings.getProxy().password(), getAccessTokenOfCLI(settings),
                settings.getCentral().getConnectTimeout(),
                settings.getCentral().getReadTimeout(), settings.getCentral().getWriteTimeout(),
                settings.getCentral().getCallTimeout(), settings.getCentral().getMaxRetries());
        String[] toolInfo = client.pullTool(toolId, version, balaCacheDirPath, supportedPlatform,
                RepoUtils.getBallerinaVersion(), false);

        return new BalToolsManifest.Tool(toolId, toolInfo[0], toolInfo[1], toolInfo[2], true, null);
    }

    private static SemanticVersion getToolDistVersionFromCache(
            String org, String name, String version, String repository) {
        Path balaPath = ProjectUtils.getPackagePath(getRepoPath(repository), org, name, version);
        PackageJson packageJson = BalaFiles.readPackageJson(balaPath);
        return SemanticVersion.from(packageJson.getBallerinaVersion());
    }
}
