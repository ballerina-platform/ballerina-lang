/*
 *  Copyright (c) 2026, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
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
import io.ballerina.projects.environment.Environment;
import io.ballerina.projects.environment.PackageMetadataResponse;
import io.ballerina.projects.environment.ResolutionOptions;
import io.ballerina.projects.environment.ResolutionRequest;
import io.ballerina.projects.internal.model.Repository;
import io.ballerina.projects.util.ProjectUtils;
import org.ballerinalang.harbor.HarborClient;
import org.ballerinalang.harbor.HarborClientException;
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This class represents an OCI (Harbor) backed package repository.
 *
 * @since 2201.14.0
 */
public class OCIPackageRepository extends AbstractPackageRepository {

    private static final String PLATFORM = "platform";
    private static final String BALA_EXTENSION = ".bala";

    private final FileSystemRepository fileSystemRepository;
    private final HarborClient harborClient;
    private final String repoLocation;

    public OCIPackageRepository(Environment environment, Path repositoryPath, String distributionVersion,
                                 HarborClient harborClient) {
        this.fileSystemRepository = new FileSystemRepository(environment, repositoryPath, distributionVersion);
        this.harborClient = harborClient;
        this.repoLocation = repositoryPath.toString();
    }

    public static OCIPackageRepository from(Environment environment, Path repositoryPath, Repository repository) {
        HarborClient harborClient = new HarborClient(repository.url(), repository.username(), repository.password());
        String ballerinaShortVersion = RepoUtils.getBallerinaShortVersion();
        return new OCIPackageRepository(environment, repositoryPath, ballerinaShortVersion, harborClient);
    }

    @Override
    public Optional<Package> getPackage(ResolutionRequest resolutionRequest, ResolutionOptions resolutionOptions) {
        Optional<Package> packageOpt = this.fileSystemRepository.getPackage(resolutionRequest, resolutionOptions);
        if (packageOpt.isPresent()) {
            return packageOpt;
        }

        if (!resolutionOptions.offline() && resolutionRequest.version().isPresent()) {
            try {
                getFromHarbor(resolutionRequest.orgName(), resolutionRequest.packageName(),
                        resolutionRequest.version().get());
            } catch (Exception ignored) {
                // fall through and return whatever is (not) in the file system cache
            }
        }

        return this.fileSystemRepository.getPackage(resolutionRequest, resolutionOptions);
    }

    public boolean getFromHarbor(PackageOrg org, PackageName name, PackageVersion pkgVersion) {
        String orgName = org.toString();
        String packageName = name.toString();
        String version = pkgVersion.toString();
        Path tmpDownloadDirectory = null;
        boolean success;
        try {
            tmpDownloadDirectory = Files.createTempDirectory("ballerina-" + System.nanoTime());
            harborClient.pullMetadata(orgName, packageName, version, "java21", tmpDownloadDirectory.toString());
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
        } catch (HarborClientException | IOException e) {
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
        List<PackageVersion> packageVersions = this.fileSystemRepository.getPackageVersions(
                org, name, resolutionRequest.version().orElse(null));
        if (!resolutionOptions.offline() && this.harborClient != null) {
            try {
                List<String> versions = this.harborClient.pullMetadata(org.toString(), name.toString(), "java21");
                if (versions.isEmpty()) {
                    return Collections.emptyList();
                }
                return versions.stream().map(PackageVersion::from).collect(Collectors.toList());
            } catch (HarborClientException ignored) {
                // fall through and return whatever is in the file system cache
            }
        }
        return packageVersions;
    }

    @Override
    protected List<PackageVersion> getPackageVersions(PackageOrg org, PackageName name, PackageVersion version) {
        List<PackageVersion> packageVersions = this.fileSystemRepository.getPackageVersions(org, name, version);
        if (this.harborClient != null) {
            try {
                List<String> versions = this.harborClient.pullMetadata(org.toString(), name.toString(), "java21");
                if (versions.isEmpty()) {
                    return Collections.emptyList();
                }
                return versions.stream().map(PackageVersion::from).collect(Collectors.toList());
            } catch (HarborClientException ignored) {
                // fall through and return whatever is in the file system cache
            }
        }
        return packageVersions;
    }

    @Override
    public Map<String, List<String>> getPackages() {
        return this.fileSystemRepository.getPackages();
    }

    @Override
    protected DependencyGraph<PackageDescriptor> getDependencyGraph(PackageOrg org, PackageName name,
                                                                      PackageVersion version) {
        return this.fileSystemRepository.getDependencyGraph(org, name, version);
    }

    @Override
    public boolean isPackageExists(PackageOrg org, PackageName name, PackageVersion version) {
        if (version == null) {
            return false;
        }
        if (this.fileSystemRepository.isPackageExists(org, name, version)) {
            return true;
        }
        if (this.harborClient == null) {
            return false;
        }
        try {
            List<String> versions = this.harborClient.pullMetadata(org.toString(), name.toString(), "java21");
            if (versions == null || versions.isEmpty()) {
                return false;
            }
            return versions.contains(version.toString()) && getFromHarbor(org, name, version);
        } catch (Exception e) {
            PrintStream out = System.out;
            out.println("Error while checking package existence [" + org + "/" + name + ":" + version + "]: "
                    + e.getMessage());
            return false;
        }
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
            isPackageExists(request.orgName(), request.packageName(), latest);
            DependencyGraph<PackageDescriptor> dependencyGraph = getDependencyGraph(
                    request.orgName(), request.packageName(), latest);
            PackageDescriptor resolvedDescriptor = PackageDescriptor.from(
                    request.orgName(), request.packageName(), latest, request.repositoryName().orElse(null));
            descriptorSet.add(PackageMetadataResponse.from(request, resolvedDescriptor, dependencyGraph));
        }
        return descriptorSet;
    }

    @Override
    public Collection<ModuleDescriptor> getModules(PackageOrg org, PackageName name, PackageVersion version) {
        return this.fileSystemRepository.getModules(org, name, version);
    }
}
