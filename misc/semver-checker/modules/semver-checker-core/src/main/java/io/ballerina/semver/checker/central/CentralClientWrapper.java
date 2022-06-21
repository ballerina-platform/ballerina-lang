/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.semver.checker.central;

import io.ballerina.projects.JvmTarget;
import io.ballerina.projects.SemanticVersion;
import io.ballerina.projects.Settings;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.projects.util.ProjectUtils;
import io.ballerina.semver.checker.exception.SemverToolException;
import org.ballerinalang.central.client.CentralAPIClient;
import org.ballerinalang.central.client.exceptions.CentralClientException;
import org.ballerinalang.central.client.exceptions.PackageAlreadyExistsException;
import org.ballerinalang.toml.exceptions.SettingsTomlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static io.ballerina.projects.util.ProjectUtils.getAccessTokenOfCLI;
import static io.ballerina.projects.util.ProjectUtils.initializeProxy;

/**
 * Ballerina central client wrapper implementation.
 *
 * @since 2201.2.0
 */
public class CentralClientWrapper {

    private final PrintStream outStream;
    private final PrintStream errStream;
    private CentralAPIClient centralClient;
    public static final String SUPPORTED_PLATFORM_JAVA_11 = "java11";
    private static final Logger LOGGER = LoggerFactory.getLogger(CentralClientWrapper.class);

    public CentralClientWrapper(PrintStream outStream, PrintStream errStream) {
        this.outStream = outStream;
        this.errStream = errStream;
        initializeClient();
    }

    /**
     * Retrieves all the released versions from the central and returns the latest package version that is
     * compatible with the current semantic version of the package.
     *
     * @param orgName        org name
     * @param pkgName        package name
     * @param currentVersion current package version
     * @return all the released versions available in the Ballerina central
     */
    public SemanticVersion getLatestCompatibleVersion(String orgName, String pkgName, SemanticVersion currentVersion)
            throws SemverToolException {
        try {
            List<String> publishedVersions = centralClient.getPackageVersions(orgName, pkgName,
                    JvmTarget.JAVA_11.code(), RepoUtils.getBallerinaVersion());
            if (publishedVersions == null || publishedVersions.isEmpty()) {
                throw new SemverToolException(String.format("couldn't find any published packages in " +
                        "Ballerina central under the org '%s' with name '%s'", orgName, pkgName));
            }
            List<SemanticVersion> availableVersions = publishedVersions.stream().map(SemanticVersion::from)
                    .collect(Collectors.toList());

            Optional<SemanticVersion> compatibleVersion = selectCompatibleVersion(availableVersions, currentVersion);
            if (compatibleVersion.isEmpty()) {
                SemanticVersion highestVersion = getHighestVersion(availableVersions);
                LOGGER.warn(String.format("current package version is lower than all the available versions in the " +
                                "central. local changes will be compared against the latest release version ('%s').",
                        highestVersion));
                return highestVersion;
            } else {
                LOGGER.debug("local changes will be compared against the release version: " + compatibleVersion.get());
                return compatibleVersion.get();
            }
        } catch (CentralClientException e) {
            throw new SemverToolException(e);
        }
    }

    /**
     * Pulls the specified package from the central.
     *
     * @param orgName organization name of the package
     * @param pkgName name of the package
     * @param version version of the package
     * @return path of the pulled bala package
     * @throws SemverToolException if unexpected error occurred while pulling package
     */
    public Path pullPackage(String orgName, String pkgName, SemanticVersion version) throws SemverToolException {
        Path packagePathInBalaCache = ProjectUtils.createAndGetHomeReposPath()
                .resolve(ProjectConstants.REPOSITORIES_DIR).resolve(ProjectConstants.CENTRAL_REPOSITORY_CACHE_NAME)
                .resolve(ProjectConstants.BALA_DIR_NAME)
                .resolve(orgName).resolve(pkgName);
        try {
            // avoids pulling from the central if the package version already available in the local repository.
            if (Files.exists(packagePathInBalaCache) && Files.isDirectory(packagePathInBalaCache)) {
                throw new PackageAlreadyExistsException("package already exists in the home repository: " +
                        packagePathInBalaCache);
            }

            errStream.printf("pulling package version '%s/%s:%s' from central...%n", orgName, pkgName, version);
            centralClient.pullPackage(orgName, pkgName, version.toString(), packagePathInBalaCache,
                    SUPPORTED_PLATFORM_JAVA_11, RepoUtils.getBallerinaVersion(), false);
        } catch (PackageAlreadyExistsException e) {
            // can be ignored
        } catch (CentralClientException e) {
            throw new SemverToolException("unexpected error occurred while pulling package from the central: "
                    + e.getMessage());
        }

        return packagePathInBalaCache.resolve(version.toString()).resolve(SUPPORTED_PLATFORM_JAVA_11);
    }

    /**
     * Returns the "closest" compatible version for the given semantic version, from the provided list of versions.
     *
     * @param versionList list of available versions in the Ballerina central
     * @param pkgVersion  target version
     */
    private Optional<SemanticVersion> selectCompatibleVersion(List<SemanticVersion> versionList,
                                                              SemanticVersion pkgVersion) {
        AtomicReference<SemanticVersion> latestCompatibleVersion = new AtomicReference<>(SemanticVersion.from("0.0.0"));
        versionList.forEach(version -> {
            if (version.lessThanOrEqualTo(pkgVersion) && version.greaterThan(latestCompatibleVersion.get())) {
                latestCompatibleVersion.set(version);
            }
        });

        if (latestCompatibleVersion.get().equals(SemanticVersion.from("0.0.0"))) {
            return Optional.empty();
        }

        return Optional.ofNullable(latestCompatibleVersion.get());
    }

    private void initializeClient() {
        Settings settings;
        try {
            settings = RepoUtils.readSettings();
            // Ignore Settings.toml diagnostics in the search command
        } catch (SettingsTomlException e) {
            // Ignore 'Settings.toml' parsing errors and return empty Settings object
            settings = Settings.from();
        }
        this.centralClient = new CentralAPIClient(RepoUtils.getRemoteRepoURL(),
                initializeProxy(settings.getProxy()), getAccessTokenOfCLI(settings));
    }

    private SemanticVersion getHighestVersion(List<SemanticVersion> availableVersions) {
        AtomicReference<SemanticVersion> highestVersion = new AtomicReference<>(SemanticVersion.from("0.0.0"));
        availableVersions.forEach(version -> {
            if (version.greaterThan(highestVersion.get())) {
                highestVersion.set(version);
            }
        });

        return highestVersion.get();
    }
}
