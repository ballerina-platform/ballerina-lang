/*
 * Copyright (c) 2026, WSO2 LLC. (http://wso2.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.ballerina.projects.internal.repositories;

import io.ballerina.projects.AnyTarget;
import io.ballerina.projects.DependencyGraph;
import io.ballerina.projects.JvmTarget;
import io.ballerina.projects.ModuleDescriptor;
import io.ballerina.projects.Package;
import io.ballerina.projects.PackageDescriptor;
import io.ballerina.projects.PackageName;
import io.ballerina.projects.PackageOrg;
import io.ballerina.projects.PackageVersion;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.SemanticVersion;
import io.ballerina.projects.Settings;
import io.ballerina.projects.environment.Environment;
import io.ballerina.projects.environment.PackageMetadataResponse;
import io.ballerina.projects.environment.ResolutionOptions;
import io.ballerina.projects.environment.ResolutionRequest;
import io.ballerina.projects.internal.BalaFiles;
import io.ballerina.projects.internal.ImportModuleRequest;
import io.ballerina.projects.internal.ImportModuleResponse;
import io.ballerina.projects.internal.model.Proxy;
import io.ballerina.projects.internal.model.Repository;
import io.ballerina.projects.internal.repositories.CustomPkgRepositoryUtils.RemotePackageInfo;
import org.ballerinalang.oci.OciClient;
import org.ballerinalang.oci.OciClientException;
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This class represents an OCI backed package repository.
 *
 * @since 2201.14.0
 */
public class OCIPackageRepository extends AbstractPackageRepository {

    private static final Set<String> SUPPORTED_PLATFORMS = Arrays.stream(JvmTarget.values())
            .map(JvmTarget::code).collect(Collectors.toSet());

    private final FileSystemRepository fileSystemRepository;
    private final OciClient ociClient;
    private final String repoLocation;
    private final String distributionVersion;

    private final boolean isProxyCentral;

    public OCIPackageRepository(Environment environment, Path repositoryPath, String distributionVersion,
                                 OciClient ociClient) {
        this(environment, repositoryPath, distributionVersion, ociClient, false);
    }

    public OCIPackageRepository(Environment environment, Path repositoryPath, String distributionVersion,
                                 OciClient ociClient, boolean isProxyCentral) {
        this.fileSystemRepository = new FileSystemRepository(environment, repositoryPath, distributionVersion);
        this.ociClient = ociClient;
        this.repoLocation = repositoryPath.toString();
        this.isProxyCentral = isProxyCentral;
        this.distributionVersion = distributionVersion;
    }

    public static OCIPackageRepository from(Environment environment, Path repositoryPath, Repository repository) {
        OciClient ociClient = new OciClient(repository.url(), repository.username(), repository.password());
        Settings settings = RepoUtils.readSettings();
        Proxy proxy = settings.getProxy();
        ociClient.setProxy(proxy.host(), proxy.port(), proxy.username(), proxy.password());
        String ballerinaShortVersion = RepoUtils.getBallerinaShortVersion();
        return new OCIPackageRepository(environment, repositoryPath, ballerinaShortVersion, ociClient,
                repository.proxyCentral());
    }

    private List<String> lookupVersions(String org, String pkg) {
        List<String> versions = this.isProxyCentral
                ? this.ociClient.pullMetadata(org, pkg) : this.ociClient.listTags(org, pkg);
        return versions.stream().filter(version -> isPkgDistVersionCompatible(org, pkg, version)).toList();
    }

    private void printWarning(String message) {
        final PrintStream out = System.out;
        out.println(message);
    }

    @Override
    public Optional<Package> getPackage(ResolutionRequest resolutionRequest, ResolutionOptions resolutionOptions) {
        Optional<Package> packageOpt = this.fileSystemRepository.getPackage(resolutionRequest, resolutionOptions);
        if (packageOpt.isPresent()) {
            return packageOpt;
        }

        if (!resolutionOptions.offline() && resolutionRequest.version().isPresent()) {
            getFromOci(resolutionRequest.orgName(), resolutionRequest.packageName(),
                    resolutionRequest.version().get());
        }

        return this.fileSystemRepository.getPackage(resolutionRequest, resolutionOptions);
    }

    public boolean getFromOci(PackageOrg org, PackageName name, PackageVersion pkgVersion) {
        String orgName = org.toString();
        String packageName = name.toString();
        String version = pkgVersion.toString();
        Path versionDir = Path.of(this.repoLocation).resolve("bala").resolve(orgName)
                .resolve(packageName).resolve(version);
        try {
            CustomPkgRepositoryUtils.pullIntoCache(orgName, packageName, version, versionDir,
                    downloadDirectory -> ociClient.pullMetadata(orgName, packageName, version,
                            downloadDirectory.toString(), versionDir.toString()));
            return true;
        } catch (IOException | RuntimeException e) {
            printWarning("warning: failed to pull package '" + orgName + "/" + packageName + ":" + version
                    + "' from OCI repository: " + e.getMessage());
            return false;
        }
    }

    @Override
    public Collection<PackageVersion> getPackageVersions(ResolutionRequest resolutionRequest,
                                                           ResolutionOptions resolutionOptions) {
        return CustomPkgRepositoryUtils.getPackageVersions(resolutionRequest, resolutionOptions,
                this.fileSystemRepository, () -> listRemoteVersions(resolutionRequest.orgName().toString(),
                        resolutionRequest.packageName().toString()));
    }

    @Override
    protected List<PackageVersion> getPackageVersions(PackageOrg org, PackageName name, PackageVersion version) {
        Set<PackageVersion> packageVersions = new HashSet<>(
                this.fileSystemRepository.getPackageVersions(org, name, version));
        listRemoteVersions(org.toString(), name.toString()).stream()
                .map(PackageVersion::from).forEach(packageVersions::add);
        return new ArrayList<>(packageVersions);
    }

    private List<String> listRemoteVersions(String org, String pkg) {
        if (this.ociClient == null) {
            return Collections.emptyList();
        }
        try {
            return lookupVersions(org, pkg);
        } catch (OciClientException e) {
            // ignore and use whatever is in the file system cache
            return Collections.emptyList();
        }
    }

    @Override
    public Map<String, List<String>> getPackages() {
        return this.fileSystemRepository.getPackages();
    }

    @Override
    protected DependencyGraph<PackageDescriptor> getDependencyGraph(PackageOrg org, PackageName name,
                                                                      PackageVersion version) {
        if (isPackageExists(org, name, version)) {
            return this.fileSystemRepository.getDependencyGraph(org, name, version);
        }

        if (version == null || this.ociClient == null) {
            return DependencyGraph.emptyGraph();
        }

        DependencyGraph<PackageDescriptor> referrerGraph = getDependencyGraphFromReferrer(org, name, version);
        if (referrerGraph != null) {
            return referrerGraph;
        }

        if (!getFromOci(org, name, version)) {
            return DependencyGraph.emptyGraph();
        }
        return this.fileSystemRepository.getDependencyGraph(org, name, version);
    }

    private DependencyGraph<PackageDescriptor> getDependencyGraphFromReferrer(PackageOrg org, PackageName name,
                                                                                PackageVersion version) {
        try {
            Optional<String> dependencyGraphJson = this.ociClient.pullDependencyGraph(
                    org.toString(), name.toString(), version.toString());

            if (dependencyGraphJson.isEmpty()) {
                return null;
            }
            return BalaFiles.createPackageDependencyGraphFromJsonContent(dependencyGraphJson.get())
                    .packageDependencyGraph();
        } catch (OciClientException | ProjectException e) {
            printWarning("warning: failed to pull dependency graph via referrers for '" + org + "/" + name
                    + ":" + version + "' from OCI repository: " + e.getMessage());
            return null;
        }
    }

    @Override
    public boolean isPackageExists(PackageOrg org, PackageName name, PackageVersion version) {
        if (version == null) {
            return false;
        }
        return this.fileSystemRepository.isPackageExists(org, name, version);
    }

    @Override
    public Collection<PackageMetadataResponse> getPackageMetadata(Collection<ResolutionRequest> requests,
                                                                    ResolutionOptions options) {
        if (isProxyCentral) {
            return CustomPkgRepositoryUtils.resolveWithCache(requests, options, this.fileSystemRepository,
                    remainingRequests -> resolvePackageMetadata(remainingRequests, options));
        }
        return resolvePackageMetadata(requests, options);
    }

    private Collection<PackageMetadataResponse> resolvePackageMetadata(Collection<ResolutionRequest> requests,
                                                                       ResolutionOptions options) {
        return CustomPkgRepositoryUtils.resolveLatestVersions(requests,
                request -> getPackageVersions(request, options),
                (request, latest) -> CustomPkgRepositoryUtils.createMetadataResponse(
                        request, latest, this::fetchPackageInfo));
    }

    private RemotePackageInfo fetchPackageInfo(ResolutionRequest request, PackageVersion version) {
        DependencyGraph<PackageDescriptor> dependencyGraph = getDependencyGraph(
                request.orgName(), request.packageName(), version);
        try {
            Map<String, String> labels = this.ociClient.pullLabels(
                    request.orgName().toString(), request.packageName().toString(), version.toString());
            String deprecated = labels.get(OciClient.DEPRECATED_LABEL);
            if (deprecated == null) {
                return RemotePackageInfo.withUnknownDeprecation(dependencyGraph);
            }
            return new RemotePackageInfo(dependencyGraph, Optional.of(Boolean.parseBoolean(deprecated)),
                    labels.getOrDefault(OciClient.DEPRECATION_MSG_LABEL, ""));
        } catch (OciClientException e) {
            // Deprecated status is best-effort metadata; resolution should not fail over it.
            return RemotePackageInfo.withUnknownDeprecation(dependencyGraph);
        }
    }

    @Override
    public Collection<ImportModuleResponse> getPackageNames(Collection<ImportModuleRequest> requests,
                                                              ResolutionOptions options) {
        return CustomPkgRepositoryUtils.getPackageNames(requests, options, this.fileSystemRepository,
                this::listRemoteVersions);
    }

    private boolean isPkgDistVersionCompatible(String org, String pkg, String version) {
        Map<String, String> labels;
        try {
            labels = this.ociClient.pullLabels(org, pkg, version);
        } catch (OciClientException e) {
            printWarning("warning: failed to read compatibility labels for '" + org + "/" + pkg + ":" + version
                    + "' from OCI repository: " + e.getMessage());
            return false;
        }

        String platform = labels.get(OciClient.PLATFORM_LABEL);
        String packageDistributionVersion = labels.get(OciClient.DISTRIBUTION_LABEL);
        if (platform == null || packageDistributionVersion == null) {
            return this.isProxyCentral;
        }
        if (!AnyTarget.ANY.code().equals(platform) && !SUPPORTED_PLATFORMS.contains(platform)) {
            return false;
        }

        try {
            SemanticVersion packageDistribution = SemanticVersion.from(packageDistributionVersion);
            SemanticVersion currentDistribution = SemanticVersion.from(this.distributionVersion);
            return packageDistribution.lessThanOrEqualTo(currentDistribution);
        } catch (ProjectException e) {
            return false;
        }
    }


    @Override
    public Collection<ModuleDescriptor> getModules(PackageOrg org, PackageName name, PackageVersion version) {
        return this.fileSystemRepository.getModules(org, name, version);
    }
}
