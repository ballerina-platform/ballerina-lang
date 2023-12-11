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

package io.ballerina.semver.checker;

import io.ballerina.projects.JvmTarget;
import io.ballerina.projects.Package;
import io.ballerina.projects.PackageDescriptor;
import io.ballerina.projects.PackageName;
import io.ballerina.projects.PackageOrg;
import io.ballerina.projects.PackageVersion;
import io.ballerina.projects.SemanticVersion;
import io.ballerina.projects.Settings;
import io.ballerina.projects.environment.Environment;
import io.ballerina.projects.environment.EnvironmentBuilder;
import io.ballerina.projects.environment.PackageResolver;
import io.ballerina.projects.environment.ResolutionOptions;
import io.ballerina.projects.environment.ResolutionRequest;
import io.ballerina.projects.environment.ResolutionResponse;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.projects.util.ProjectUtils;
import io.ballerina.semver.checker.exception.SemverToolException;
import org.ballerinalang.central.client.CentralAPIClient;
import org.ballerinalang.central.client.CentralClientConstants;
import org.ballerinalang.central.client.exceptions.CentralClientException;
import org.ballerinalang.toml.exceptions.SettingsTomlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static io.ballerina.projects.environment.ResolutionResponse.ResolutionStatus;
import static io.ballerina.projects.util.ProjectConstants.HOME_REPO_ENV_KEY;
import static io.ballerina.projects.util.ProjectUtils.getAccessTokenOfCLI;
import static io.ballerina.projects.util.ProjectUtils.initializeProxy;

/**
 * A Ballerina package resolver implementation to resolve Ballerina packages and versions from available repositories.
 *
 * @since 2201.2.0
 */
class BallerinaPackageResolver {

    private final PrintStream outStream;
    private final PrintStream errStream;
    private CentralAPIClient centralClient;
    private static final Logger LOGGER = LoggerFactory.getLogger(BallerinaPackageResolver.class);

    BallerinaPackageResolver(PrintStream outStream, PrintStream errStream) {
        this.outStream = outStream;
        this.errStream = errStream;
        initializeCentralClient();
    }

    /**
     * Retrieves all the released versions of a given package from the central and, returns the closest compatible
     * package version w.r.t the given package version.
     *
     * @param orgName      org name
     * @param pkgName      package name
     * @param localVersion local package version
     * @return all the released versions available in the Ballerina central
     */
    SemanticVersion resolveClosestCompatibleCentralVersion(String orgName, String pkgName, SemanticVersion localVersion)
            throws SemverToolException {
        try {
            List<String> publishedVersions = new ArrayList<>();
            String supportedPlatform = Arrays.stream(JvmTarget.values())
                    .map(target -> target.code())
                    .collect(Collectors.joining(","));
                publishedVersions.addAll(centralClient.getPackageVersions(orgName, pkgName,
                        supportedPlatform, RepoUtils.getBallerinaVersion()));
            if (publishedVersions.isEmpty()) {
                throw new SemverToolException(String.format("couldn't find any published packages in " +
                        "Ballerina central under the org '%s' with name '%s'", orgName, pkgName));
            }
            List<SemanticVersion> availableVersions = publishedVersions.stream().map(SemanticVersion::from)
                    .collect(Collectors.toList());

            Optional<SemanticVersion> compatibleVersion = selectCompatibleVersion(availableVersions, localVersion);
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
    Path resolvePackage(String orgName, String pkgName, SemanticVersion version) throws SemverToolException {
        Path localRepoPath = ProjectUtils.createAndGetHomeReposPath();
        Path centralCachePath = localRepoPath.resolve(ProjectConstants.REPOSITORIES_DIR)
                .resolve(ProjectConstants.CENTRAL_REPOSITORY_CACHE_NAME);

        // Avoids pulling from central if the package version already available in the local repo.
        Path packagePathInLocalRepo = localRepoPath.resolve(orgName).resolve(pkgName).resolve(version.toString());
        if (Files.exists(packagePathInLocalRepo) && Files.isDirectory(packagePathInLocalRepo)) {
            LOGGER.debug("target version: " + version + "is already available in local repository");
            return ProjectUtils.getPackagePath(localRepoPath, orgName, pkgName, version.toString());
        }

        // Avoids pulling from central if the package version already available in the local central cache.
        Path packagePathInCentralCache = centralCachePath.resolve(orgName).resolve(pkgName).resolve(version.toString());
        if (Files.exists(packagePathInCentralCache) && Files.isDirectory(packagePathInCentralCache)) {
            LOGGER.debug("target version: " + version + "is already available in central repository cache");
            return ProjectUtils.getPackagePath(centralCachePath, orgName, pkgName, version.toString());
        }

        outStream.printf("resolving package '%s/%s:%s'...%n", orgName, pkgName, version);
        return resolveBalaPath(orgName, pkgName, version.toString());
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

    private void initializeCentralClient() {
        Settings settings;
        try {
            settings = RepoUtils.readSettings();
            // Ignore Settings.toml diagnostics in the search command
        } catch (SettingsTomlException e) {
            // Ignore 'Settings.toml' parsing errors and return empty Settings object
            settings = Settings.from();
        }
        this.centralClient = new CentralAPIClient(RepoUtils.getRemoteRepoURL(),
                initializeProxy(settings.getProxy()), settings.getProxy().username(),
                settings.getProxy().password(), getAccessTokenOfCLI(settings),
                settings.getCentral().getConnectTimeout(),
                settings.getCentral().getReadTimeout(), settings.getCentral().getWriteTimeout(),
                settings.getCentral().getCallTimeout());
    }

    private Path resolveBalaPath(String org, String pkgName, String version) throws SemverToolException {
        // If user has provided a custom home repo path using `BALLERINA_HOME_DIR` env variable, switches to local repo.
        String repository = hasCustomHomeRepoPath() ? ProjectConstants.LOCAL_REPOSITORY_NAME : null;
        PackageDescriptor packageDescriptor = PackageDescriptor.from(PackageOrg.from(org), PackageName.from(pkgName),
                PackageVersion.from(version), repository);
        ResolutionRequest resolutionRequest = ResolutionRequest.from(packageDescriptor);

        System.setProperty(CentralClientConstants.ENABLE_OUTPUT_STREAM, Boolean.TRUE.toString());
        PackageResolver packageResolver = getEnvironment().getService(PackageResolver.class);
        Collection<ResolutionResponse> resolutionResponses = packageResolver.resolvePackages(
                Collections.singletonList(resolutionRequest), ResolutionOptions.builder().setOffline(false).build());
        ResolutionResponse resolutionResponse = resolutionResponses.stream().findFirst().orElse(null);

        if (resolutionResponse != null && resolutionResponse.resolutionStatus().equals(ResolutionStatus.RESOLVED)) {
            Package resolvedPackage = resolutionResponse.resolvedPackage();
            if (resolvedPackage != null) {
                return resolvedPackage.project().sourceRoot();
            }
        }

        throw new SemverToolException("failed to resolve package '" + packageDescriptor + "' from central");
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

    private Environment getEnvironment() {
        if (hasCustomHomeRepoPath()) {
            return EnvironmentBuilder.getBuilder().setUserHome(ProjectUtils.createAndGetHomeReposPath()).build();
        } else {
            return EnvironmentBuilder.buildDefault();
        }
    }

    /**
     * Returns whether user has provided a custom home repo path using `BALLERINA_HOME_DIR` env variable.
     */
    private boolean hasCustomHomeRepoPath() {
        return System.getenv(HOME_REPO_ENV_KEY) != null;
    }
}
