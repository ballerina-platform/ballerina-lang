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
import io.ballerina.projects.DependencyGraph;
import io.ballerina.projects.ModuleDescriptor;
import io.ballerina.projects.Package;
import io.ballerina.projects.PackageDescriptor;
import io.ballerina.projects.PackageName;
import io.ballerina.projects.PackageOrg;
import io.ballerina.projects.PackageVersion;
import io.ballerina.projects.SemanticVersion;
import io.ballerina.projects.Settings;
import io.ballerina.projects.environment.Environment;
import io.ballerina.projects.environment.PackageMetadataResponse;
import io.ballerina.projects.environment.ResolutionOptions;
import io.ballerina.projects.environment.ResolutionRequest;
import io.ballerina.projects.environment.ResolutionResponse;
import io.ballerina.projects.internal.ImportModuleRequest;
import io.ballerina.projects.internal.ImportModuleResponse;
import io.ballerina.projects.internal.model.Proxy;
import io.ballerina.projects.internal.model.Repository;
import io.ballerina.projects.util.ProjectUtils;
import org.ballerinalang.oci.OciClient;
import org.ballerinalang.oci.OciClientException;
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
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

    private static final String PLATFORM = "platform";
    private static final String BALA_EXTENSION = ".bala";

    private final FileSystemRepository fileSystemRepository;
    private final OciClient ociClient;
    private final String repoLocation;
    private final boolean hosted;

    public OCIPackageRepository(Environment environment, Path repositoryPath, String distributionVersion,
                                 OciClient ociClient) {
        this(environment, repositoryPath, distributionVersion, ociClient, true);
    }

    public OCIPackageRepository(Environment environment, Path repositoryPath, String distributionVersion,
                                 OciClient ociClient, boolean hosted) {
        this.fileSystemRepository = new FileSystemRepository(environment, repositoryPath, distributionVersion);
        this.ociClient = ociClient;
        this.repoLocation = repositoryPath.toString();
        this.hosted = hosted;
    }

    public static OCIPackageRepository from(Environment environment, Path repositoryPath, Repository repository) {
        OciClient ociClient = new OciClient(repository.url(), repository.username(), repository.password());
        Settings settings = RepoUtils.readSettings();
        Proxy proxy = settings.getProxy();
        ociClient.setProxy(proxy.host(), proxy.port(), proxy.username(), proxy.password());
        String ballerinaShortVersion = RepoUtils.getBallerinaShortVersion();
        boolean hosted = Repository.MODE_HOSTED.equals(repository.mode());
        return new OCIPackageRepository(environment, repositoryPath, ballerinaShortVersion, ociClient, hosted);
    }


    private List<String> lookupVersions(String org, String pkg) {
        return this.hosted ? this.ociClient.listTags(org, pkg) : this.ociClient.pullMetadata(org, pkg);
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
            try {
                getFromOci(resolutionRequest.orgName(), resolutionRequest.packageName(),
                        resolutionRequest.version().get());
            } catch (RuntimeException e) {
                printWarning("warning: failed to pull package '" + resolutionRequest.orgName() + "/"
                        + resolutionRequest.packageName() + "' from OCI repository: " + e.getMessage());
            }
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
                Path actualBalaPath = Path.of(this.repoLocation).resolve("bala").resolve(orgName)
                        .resolve(packageName).resolve(version).resolve(platform);
                if (Files.exists(actualBalaPath)) {
                    ProjectUtils.deleteDirectory(actualBalaPath);
                }
                Files.createDirectories(actualBalaPath);
                ProjectUtils.extractBala(balaDownloadPath, actualBalaPath);
            }
            return true;
        } catch (OciClientException | IOException e) {
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
        if (!isPackageExists(org, name, version)) {
            if (version == null || this.ociClient == null || !getFromOci(org, name, version)) {
                return DependencyGraph.emptyGraph();
            }
        }
        return this.fileSystemRepository.getDependencyGraph(org, name, version);
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
            PackageDescriptor resolvedDescriptor = PackageDescriptor.from(
                    request.orgName(), request.packageName(), latest, request.repositoryName().orElse(null));
            descriptorSet.add(PackageMetadataResponse.from(request, resolvedDescriptor, dependencyGraph));
        }
        return descriptorSet;
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


    @Override
    public Collection<ModuleDescriptor> getModules(PackageOrg org, PackageName name, PackageVersion version) {
        return this.fileSystemRepository.getModules(org, name, version);
    }
}
