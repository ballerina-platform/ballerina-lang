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

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.ballerina.projects.BalToolsManifest;
import io.ballerina.projects.JvmTarget;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.SemanticVersion;
import io.ballerina.projects.Settings;
import io.ballerina.projects.internal.BalaFiles;
import io.ballerina.projects.internal.model.PackageJson;
import io.ballerina.projects.internal.model.Repository;
import org.apache.commons.io.FileUtils;
import org.ballerinalang.central.client.CentralAPIClient;
import org.ballerinalang.central.client.CentralClientConstants;
import org.ballerinalang.central.client.exceptions.CentralClientException;
import org.ballerinalang.central.client.model.ToolResolutionCentralRequest;
import org.ballerinalang.central.client.model.ToolResolutionCentralResponse;
import org.ballerinalang.maven.bala.client.MavenResolverClient;
import org.ballerinalang.maven.bala.client.MavenResolverClientException;
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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
        Optional<SemanticVersion> toolDistVersion = getToolDistVersionFromCache(org, name, version, repository);
        return toolDistVersion.filter(semanticVersion ->
                isCompatibleWithDistVersion(currentDistVersion, semanticVersion)).isPresent();
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
        return isCompatibleWithPlatform(org, name, version, CENTRAL_REPOSITORY_CACHE_NAME);
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
        Optional<SemanticVersion> toolDistVersion = getToolDistVersionFromCache(org, name, versions, repository);
        if (toolDistVersion.isEmpty()) {
            return SemanticVersion.VersionCompatibilityResult.INCOMPATIBLE;
        }
        return toolDistVersion.get().compareTo(currentDistVersion);
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

    private static Optional<SemanticVersion> getToolDistVersionFromCache(
            String org, String name, String version, String repository) {
        Path balaPath = ProjectUtils.getPackagePath(getRepoPath(repository), org, name, version);
        PackageJson packageJson;
        try {
            packageJson = BalaFiles.readPackageJson(balaPath);
        } catch (ProjectException e) {
            return Optional.empty();
        }

        return Optional.of(SemanticVersion.from(packageJson.getBallerinaVersion()));
    }

    /**
     * Get the latest compatible tool version from Maven proxy central repository.
     *
     * @param toolId        the tool ID (command name)
     * @param localRepoPath path to the local Maven repository
     * @return the latest compatible version string
     * @throws MavenResolverClientException when Maven resolution fails
     */
    public static String getLatestToolVersionFromCentralProxy(String toolId, Path localRepoPath)
            throws MavenResolverClientException {
        Settings settings = RepoUtils.readSettings();
        Repository proxyCentralRepo = findProxyCentralRepository(settings);
        if (proxyCentralRepo == null) {
            throw new MavenResolverClientException("ProxyCentral repository not configured");
        }
        MavenResolverClient mavenClient = new MavenResolverClient();
        mavenClient.addRepository(proxyCentralRepo.id(), proxyCentralRepo.url(),
                proxyCentralRepo.username(), proxyCentralRepo.password());
        String ballerinaVersion = RepoUtils.getBallerinaVersion();
        List<String> compatibleVersions = mavenClient.getCompatibleToolVersions(
                toolId, ballerinaVersion, localRepoPath);
        if (compatibleVersions.isEmpty()) {
            throw new MavenResolverClientException(
                    "No compatible versions found for tool: " + toolId);
        }

        // Return the first (latest) version
        return compatibleVersions.getFirst();
    }

    /**
     * Check if a repository with proxyCentral enabled is configured in the settings.
     *
     * @param settings the settings to search
     * @return true if a proxyCentral repository is configured, false otherwise
     */
    public static boolean hasProxyCentralRepository(Settings settings) {
        return findProxyCentralRepository(settings) != null;
    }

    /**
     * Extract a downloaded tool bala file and place it in the repository location.
     * The tool bala is downloaded to a temporary location and extracted to the proper repository path.
     *
     * @param org             tool organization
     * @param name            tool name
     * @param version         tool version
     * @param localRepoPath   path to the local repository where the tool was downloaded
     * @throws MavenResolverClientException if extraction fails
     */
    public static void extractAndPlaceTool(String org, String name, String version, Path localRepoPath)
            throws MavenResolverClientException {
        try {
            // Construct paths for the downloaded bala and extraction
            Path balaDownloadPath = localRepoPath.resolve(org).resolve(name).resolve(version)
                    .resolve(name + "-" + version + ProjectConstants.BALA_EXTENSION);
            Path temporaryExtractionPath = localRepoPath.resolve(org).resolve(name)
                    .resolve(version).resolve(ProjectConstants.PLATFORM);

            // Extract the bala file
            ProjectUtils.extractBala(balaDownloadPath, temporaryExtractionPath);

            // Read package.json to get platform info
            Path packageJsonPath = temporaryExtractionPath.resolve("package.json");
            try (BufferedReader bufferedReader = Files.newBufferedReader(packageJsonPath, StandardCharsets.UTF_8)) {
                JsonObject resultObj = new Gson().fromJson(bufferedReader, JsonObject.class);
                String platform = resultObj.get(ProjectConstants.PLATFORM).getAsString();

                // Determine the final repository path
                Path toolRepositoryPath = RepoUtils.createAndGetHomeReposPath()
                        .resolve(ProjectConstants.REPOSITORIES_DIR)
                        .resolve(ProjectConstants.CENTRAL_REPOSITORY_CACHE_NAME)
                        .resolve(ProjectConstants.BALA_DIR_NAME)
                        .resolve(org)
                        .resolve(name)
                        .resolve(version)
                        .resolve(platform);

                // Copy extracted files to the final repository location
                FileUtils.copyDirectory(temporaryExtractionPath.toFile(), toolRepositoryPath.toFile());
            }
        } catch (IOException e) {
            throw new MavenResolverClientException("Failed to extract and place tool: " + e.getMessage());
        }
    }

    /**
     * Find a repository with proxyCentral enabled in the settings.
     *
     * @param settings the settings to search
     * @return the proxyCentral repository if found, null otherwise
     */
    public static Repository findProxyCentralRepository(Settings settings) {
        for (Repository repo : settings.getRepositories()) {
            if (repo.proxyCentral()) {
                return repo;
            }
        }
        return null;
    }

    /**
     * Initialize a MavenResolverClient with the proxyCentral repository configured in settings.
     *
     * @param settings the settings containing proxy repository configuration
     * @return the initialized MavenResolverClient with proxyCentral repository added
     * @throws MavenResolverClientException if no proxyCentral repository is configured
     */
    public static MavenResolverClient initializeMavenClientWithProxyRepo(Settings settings)
            throws MavenResolverClientException {
        Repository proxyCentralRepo = findProxyCentralRepository(settings);
        if (proxyCentralRepo == null) {
            throw new MavenResolverClientException("ProxyCentral repository not configured");
        }
        MavenResolverClient client = new MavenResolverClient();
        client.addRepository(proxyCentralRepo.id(), proxyCentralRepo.url(),
                proxyCentralRepo.username(), proxyCentralRepo.password());
        return client;
    }

    /**
     * Pull a tool package from Maven proxy repository and extract it to the repository location.
     *
     * @param org         the tool organization
     * @param name        the tool name
     * @param version     the tool version
     * @param mavenClient the initialized MavenResolverClient
     * @throws MavenResolverClientException if pulling or extracting fails
     */
    public static void pullAndExtractToolFromMavenProxy(String org, String name, String version,
                                                        MavenResolverClient mavenClient)
            throws MavenResolverClientException {
        try {
            Path tmpDownloadDirectory = Files.createTempDirectory("ballerina-tool-" + System.nanoTime());
            try {
                mavenClient.pullPackage(org, name, version, tmpDownloadDirectory.toAbsolutePath().toString());
                extractAndPlaceTool(org, name, version, tmpDownloadDirectory);
            } finally {
                FileUtils.deleteDirectory(tmpDownloadDirectory.toFile());
            }
        } catch (IOException e) {
            throw new MavenResolverClientException("Failed to download tool: " + e.getMessage());
        }
    }
}
