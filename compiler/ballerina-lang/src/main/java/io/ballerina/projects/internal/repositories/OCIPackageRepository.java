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

import com.google.gson.Gson;
import com.google.gson.JsonObject;
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
import io.ballerina.projects.environment.PackageLockingMode;
import io.ballerina.projects.environment.PackageMetadataResponse;
import io.ballerina.projects.environment.ResolutionOptions;
import io.ballerina.projects.environment.ResolutionRequest;
import io.ballerina.projects.environment.ResolutionResponse;
import io.ballerina.projects.internal.BalaFiles;
import io.ballerina.projects.internal.ImportModuleRequest;
import io.ballerina.projects.internal.ImportModuleResponse;
import io.ballerina.projects.internal.model.Proxy;
import io.ballerina.projects.internal.model.Repository;
import io.ballerina.projects.util.ProjectUtils;
import org.ballerinalang.oci.OciClient;
import org.ballerinalang.oci.OciClientException;
import org.ballerinalang.oci.OciClientUtils;
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class represents an OCI backed package repository.
 *
 * @since 2201.14.0
 */
public class OCIPackageRepository extends AbstractPackageRepository {

    private static final String PLATFORM = "platform";
    private static final String BALA_EXTENSION = ".bala";
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
        Path tmpDownloadDirectory = null;
        boolean success;
        try {
            tmpDownloadDirectory = Files.createTempDirectory("ballerina-" + System.nanoTime());
            String displayLocation = Path.of(this.repoLocation).resolve("bala").resolve(orgName)
                    .resolve(packageName).resolve(version).toString();
            ociClient.pullMetadata(orgName, packageName, version, tmpDownloadDirectory.toString(), displayLocation);
            Path balaDownloadPath = tmpDownloadDirectory.resolve(orgName).resolve(packageName).resolve(version)
                    .resolve(packageName + "-" + version + BALA_EXTENSION);
            Path temporaryExtractionPath = tmpDownloadDirectory.resolve(orgName).resolve(packageName)
                    .resolve(version).resolve(PLATFORM);
            ProjectUtils.extractBala(balaDownloadPath, temporaryExtractionPath);
            Path packageJsonPath = temporaryExtractionPath.resolve("package.json");
            try (BufferedReader bufferedReader = Files.newBufferedReader(packageJsonPath, StandardCharsets.UTF_8)) {
                JsonObject resultObj = new Gson().fromJson(bufferedReader, JsonObject.class);
                String platform = resultObj.get(PLATFORM).getAsString();
                Path versionDir = Path.of(this.repoLocation).resolve("bala").resolve(orgName)
                        .resolve(packageName).resolve(version);
                OciClientUtils.extractBalaToBalaCache(balaDownloadPath, versionDir, platform);
            }
            return true;
        } catch (IOException | RuntimeException e) {
            printWarning("warning: failed to pull package '" + orgName + "/" + packageName + ":" + version
                    + "' from OCI repository: " + e.getMessage());
            success = false;
        } finally {
            if (tmpDownloadDirectory != null) {
                ProjectUtils.deleteDirectory(tmpDownloadDirectory);
            }
        }
        return success;
    }

    @Override
    public Collection<PackageVersion> getPackageVersions(ResolutionRequest resolutionRequest,
                                                           ResolutionOptions resolutionOptions) {
        PackageOrg org = resolutionRequest.orgName();
        PackageName name = resolutionRequest.packageName();
        Set<PackageVersion> packageVersions = new HashSet<>(this.fileSystemRepository.getPackageVersions(
                org, name, resolutionRequest.version().orElse(null)));
        if (!resolutionOptions.offline() && this.ociClient != null) {
            try {
                List<String> versions = lookupVersions(org.toString(), name.toString());
                versions.stream().map(PackageVersion::from).forEach(packageVersions::add);
            } catch (OciClientException e) {
                // fall through and use whatever is in the file system cache
                printWarning("warning: failed to look up versions for '" + org + "/" + name
                        + "' from OCI repository: " + e.getMessage());
            }
        }

        PackageVersion requestedVersion = resolutionRequest.version().orElse(null);
        SemanticVersion minSemVer = requestedVersion == null
                ? null : SemanticVersion.from(requestedVersion.toString());
        List<SemanticVersion> semVers = packageVersions.stream()
                .map(version -> SemanticVersion.from(version.toString())).toList();
        ProjectUtils.CompatibleRange compatibleRange = ProjectUtils.getCompatibleRange(
                minSemVer, resolutionOptions.packageLockingMode());
        List<SemanticVersion> compatibleVersions = ProjectUtils.getVersionsInCompatibleRange(
                minSemVer, semVers, compatibleRange);
        return compatibleVersions.stream().map(PackageVersion::from).collect(Collectors.toList());
    }

    @Override
    protected List<PackageVersion> getPackageVersions(PackageOrg org, PackageName name, PackageVersion version) {
        Set<PackageVersion> packageVersions = new HashSet<>(
                this.fileSystemRepository.getPackageVersions(org, name, version));
        if (this.ociClient != null) {
            try {
                List<String> versions = lookupVersions(org.toString(), name.toString());
                versions.stream().map(PackageVersion::from).forEach(packageVersions::add);
            } catch (OciClientException e) {
                // fall through and use whatever is in the file system cache
                printWarning("warning: failed to look up versions for '" + org + "/" + name
                        + "' from OCI repository: " + e.getMessage());
            }
        }
        return new ArrayList<>(packageVersions);
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
            return getPackageMetadataProxyCentral(requests, options);
        }
        return resolvePackageMetadata(requests, options);
    }

    private Collection<PackageMetadataResponse> resolvePackageMetadata(Collection<ResolutionRequest> requests,
                                                                       ResolutionOptions options) {
        List<PackageMetadataResponse> descriptorSet = new ArrayList<>();
        for (ResolutionRequest request : requests) {
            Collection<PackageVersion> packageVersions = getPackageVersions(request, options);
            if (packageVersions.isEmpty()) {
                descriptorSet.add(PackageMetadataResponse.createUnresolvedResponse(request));
                continue;
            }
            PackageVersion latest = findLatest(new ArrayList<>(packageVersions));
            DependencyGraph<PackageDescriptor> dependencyGraph = getDependencyGraph(
                    request.orgName(), request.packageName(), latest);
            boolean isDeprecated = false;
            String deprecationMsg = "";
            try {

                Map<String, String> labels = this.ociClient.pullLabels(
                        request.orgName().toString(), request.packageName().toString(), latest.toString());
                isDeprecated = Boolean.parseBoolean(labels.get(OciClient.DEPRECATED_LABEL));
                deprecationMsg = labels.getOrDefault(OciClient.DEPRECATION_MSG_LABEL, "");
            } catch (OciClientException e) {
                // Deprecated status is best-effort metadata; resolution should not fail over it.
            }
            PackageDescriptor resolvedDescriptor = PackageDescriptor.from(
                    request.orgName(), request.packageName(), latest, request.repositoryName().orElse(null),
                    isDeprecated, deprecationMsg);
            descriptorSet.add(PackageMetadataResponse.from(request, resolvedDescriptor, dependencyGraph));
        }
        return descriptorSet;
    }

    private Collection<PackageMetadataResponse> getPackageMetadataProxyCentral(Collection<ResolutionRequest> requests,
                                                                               ResolutionOptions options) {
        if (requests.isEmpty()) {
            return Collections.emptyList();
        }

        Collection<PackageMetadataResponse> cachedPackages =
                this.fileSystemRepository.getPackageMetadata(requests, options);
        if (options.offline()) {
            return cachedPackages;
        }

        // Requests locked to an exact version and already resolved locally need no registry round trip
        List<ResolutionRequest> updatedRequests = new ArrayList<>(requests);
        List<PackageMetadataResponse> deprecatedPackages = new ArrayList<>();
        for (PackageMetadataResponse response : cachedPackages) {
            if (response.packageLoadRequest().version().isPresent()
                    && response.packageLoadRequest().packageLockingMode().equals(PackageLockingMode.HARD)
                    && response.resolutionStatus().equals(ResolutionResponse.ResolutionStatus.RESOLVED)) {
                updatedRequests.remove(response.packageLoadRequest());
            }
            if (response.resolutionStatus().equals(ResolutionResponse.ResolutionStatus.RESOLVED)) {
                Optional<Package> pkg = this.fileSystemRepository.getPackage(response.packageLoadRequest(), options);
                if (pkg.isPresent() && Boolean.TRUE.equals(pkg.get().descriptor().getDeprecated())) {
                    deprecatedPackages.add(response);
                }
            }
        }
        if (updatedRequests.isEmpty()) {
            return cachedPackages;
        }
        return mergeResolution(resolvePackageMetadata(updatedRequests, options), cachedPackages, deprecatedPackages);
    }

    private Collection<PackageMetadataResponse> mergeResolution(
            Collection<PackageMetadataResponse> remoteResolution,
            Collection<PackageMetadataResponse> filesystem,
            List<PackageMetadataResponse> deprecatedPackages) {
        return new ArrayList<>(Stream.of(filesystem, remoteResolution)
                .flatMap(Collection::stream)
                .collect(Collectors.toMap(
                        PackageMetadataResponse::packageLoadRequest, Function.identity(),
                        (x, y) -> {
                            if (ResolutionResponse.ResolutionStatus.UNRESOLVED.equals(y.resolutionStatus())) {
                                return x;
                            }
                            if (ResolutionResponse.ResolutionStatus.UNRESOLVED.equals(x.resolutionStatus())) {
                                return y;
                            }
                            if (x.resolvedDescriptor().version().equals(y.resolvedDescriptor().version())) {

                                if (y.resolvedDescriptor() != null && deprecatedPackages.contains(x)
                                        ^ Boolean.TRUE.equals(y.resolvedDescriptor().getDeprecated())) {
                                    this.fileSystemRepository.updateDeprecatedStatusForPackage(y.resolvedDescriptor());
                                }
                                return x;
                            }
                            // The registry resolved a version the cache does not have; prefer the latest
                            this.fileSystemRepository.updateDeprecatedStatusForPackage(y.resolvedDescriptor());
                            return y;
                        })).values());
    }

    @Override
    public Collection<ImportModuleResponse> getPackageNames(Collection<ImportModuleRequest> requests,
                                                              ResolutionOptions options) {
        List<ImportModuleResponse> importModuleResponseList = new ArrayList<>(
                this.fileSystemRepository.getPackageNames(requests, options));

        if (options.offline() || this.ociClient == null) {
            return importModuleResponseList;
        }
        for (ImportModuleRequest importModuleRequest : requests) {
            boolean alreadyResolved = importModuleResponseList.stream()
                    .anyMatch(response -> response.importModuleRequest().equals(importModuleRequest)
                            && response.resolutionStatus() == ResolutionResponse.ResolutionStatus.RESOLVED);
            if (alreadyResolved) {
                continue;
            }
            PackageOrg org = importModuleRequest.packageOrg();
            List<PackageName> possiblePackageNames = ProjectUtils.getPossiblePackageNames(
                    org, importModuleRequest.moduleName());
            for (PackageName packageName : possiblePackageNames) {
                try {
                    List<String> versions = lookupVersions(org.toString(), packageName.toString());
                    if (versions.isEmpty()) {
                        continue;
                    }
                    PackageVersion latest = findLatest(
                            versions.stream().map(PackageVersion::from).collect(Collectors.toList()));
                    PackageDescriptor resolvedDescriptor = PackageDescriptor.from(org, packageName, latest);
                    importModuleResponseList.removeIf(
                            response -> response.importModuleRequest().equals(importModuleRequest));
                    importModuleResponseList.add(new ImportModuleResponse(resolvedDescriptor, importModuleRequest));
                    break;
                } catch (OciClientException ignored) {
                    // fall through and try the next possible package name
                }
            }
        }
        return importModuleResponseList;
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
