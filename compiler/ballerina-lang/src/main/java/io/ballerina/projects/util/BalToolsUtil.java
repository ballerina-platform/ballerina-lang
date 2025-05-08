/*
 *  Copyright (c) 2025, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
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
import io.ballerina.projects.SemanticVersion;
import io.ballerina.projects.internal.BalaFiles;
import io.ballerina.projects.internal.model.PackageJson;
import org.wso2.ballerinalang.util.RepoUtils;

import java.nio.file.Path;
import java.util.Set;

import static io.ballerina.projects.util.ProjectConstants.CENTRAL_REPOSITORY_CACHE_NAME;
import static io.ballerina.projects.util.ProjectConstants.REPOSITORIES_DIR;

/**
 * Utility class for Bal tools.
 *
 * @since 2201.13.0
 */
public class BalToolsUtil {

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
    public static boolean checkPlatformCompatibility(String org, String name, String version, String repository) {
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
    public static boolean checkPlatformCompatibility(String org, String name, String version) {
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

    private static SemanticVersion getToolDistVersionFromCache(
            String org, String name, String version, String repository) {
        if (repository == null) {
            repository = CENTRAL_REPOSITORY_CACHE_NAME;
        }
        Path balaDirPath = RepoUtils.createAndGetHomeReposPath().resolve(REPOSITORIES_DIR).resolve(repository)
                .resolve(ProjectConstants.BALA_DIR_NAME);
        Path balaPath = ProjectUtils.getPackagePath(balaDirPath, org, name, version);
        PackageJson packageJson = BalaFiles.readPackageJson(balaPath);
        return SemanticVersion.from(packageJson.getBallerinaVersion());
    }
}
