/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
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
package io.ballerina.projects.internal.repositories;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.ballerina.projects.DependencyGraph;
import io.ballerina.projects.ModuleDescriptor;
import io.ballerina.projects.Package;
import io.ballerina.projects.PackageDependencyScope;
import io.ballerina.projects.PackageDescriptor;
import io.ballerina.projects.PackageName;
import io.ballerina.projects.PackageOrg;
import io.ballerina.projects.PackageVersion;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.SemanticVersion;
import io.ballerina.projects.Settings;
import io.ballerina.projects.environment.Environment;
import io.ballerina.projects.environment.PackageMetadataResponse;
import io.ballerina.projects.environment.PackageRepository;
import io.ballerina.projects.environment.ResolutionOptions;
import io.ballerina.projects.environment.ResolutionRequest;
import io.ballerina.projects.internal.ImportModuleRequest;
import io.ballerina.projects.internal.ImportModuleResponse;
import io.ballerina.projects.internal.model.Proxy;
import io.ballerina.projects.internal.model.Repository;
import io.ballerina.projects.util.ProjectUtils;
import org.apache.commons.io.FileUtils;
import org.ballerinalang.central.client.CentralClientConstants;
import org.ballerinalang.maven.bala.client.MavenResolverClient;
import org.ballerinalang.maven.bala.client.MavenResolverClientException;
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static io.ballerina.projects.util.ProjectConstants.BALA_EXTENSION;
import static io.ballerina.projects.util.ProjectUtils.getLatest;

/**
 * This class represents the remote package repository.
 *
 * @since 2.0.0
 */
public class MavenPackageRepository implements PackageRepository {

    public static final String PLATFORM = "platform";
    private final FileSystemRepository fileSystemRepo;
    private final MavenResolverClient client;
    private final String repoLocation;

    public MavenPackageRepository(Environment environment, Path cacheDirectory, String distributionVersion,
                                     MavenResolverClient client, String repoLocation) {
        this.fileSystemRepo = new FileSystemRepository(environment, cacheDirectory, distributionVersion);;
        this.client = client;
        this.repoLocation = repoLocation;
    }

    public static MavenPackageRepository from(Environment environment, Path cacheDirectory, Repository repository) {
        if (Files.notExists(cacheDirectory)) {
            throw new ProjectException("cache directory does not exists: " + cacheDirectory);
        }

        if (repository.url().isEmpty()) {
            throw new ProjectException("repository url is not provided");
        }
        String ballerinaShortVersion = RepoUtils.getBallerinaShortVersion();
        MavenResolverClient mvnClient = new MavenResolverClient();
        if (!repository.username().isEmpty() && !repository.password().isEmpty()) {
            mvnClient.addRepository(repository.id(), repository.url(), repository.username(), repository.password());
        } else {
            mvnClient.addRepository(repository.id(), repository.url());
        }

        Settings settings;
        settings = RepoUtils.readSettings();
        Proxy proxy = settings.getProxy();
        mvnClient.setProxy(proxy.host(), proxy.port(), proxy.username(), proxy.password());

        String repoLocation = cacheDirectory.resolve("bala").toAbsolutePath().toString();

        return new MavenPackageRepository(environment, cacheDirectory, ballerinaShortVersion, mvnClient,
                repoLocation);
    }

    @Override
    public Optional<Package> getPackage(ResolutionRequest request, ResolutionOptions options) {
        // Check if the package is in cache
        Optional<Package> cachedPackage = this.fileSystemRepo.getPackage(request, options);
        if (cachedPackage.isPresent()) {
            return cachedPackage;
        }

        if (options.offline()) {
            return Optional.empty();
        }

        String packageName = request.packageName().value();
        String orgName = request.orgName().value();
        if (request.version().isEmpty()) {
            boolean enableOutputStream =
                    Boolean.parseBoolean(System.getProperty(CentralClientConstants.ENABLE_OUTPUT_STREAM));
            if (enableOutputStream) {
                final PrintStream out = System.out;
                out.println("Version not found for package [" + orgName + "/" + packageName + "]: ");
            }
            return Optional.empty();
        }
        String version = request.version().get().value().toString();
        boolean isSuccess = getPackageFromRemoteRepo(orgName, packageName, version);
        if (!isSuccess) {
            return cachedPackage;
        }

        return this.fileSystemRepo.getPackage(request, options);
    }

    @Override
    public Collection<PackageVersion> getPackageVersions(ResolutionRequest request, ResolutionOptions options) {
        String orgName = request.orgName().value();
        String packageName = request.packageName().value();

        // First, Get local versions
        Set<PackageVersion> packageVersions = new HashSet<>(fileSystemRepo.getPackageVersions(request, options));

        // If the resolution request specifies to resolve offline, we return the local version
        if (!options.offline()) {
            try {
                List<String> remotePackageVersions = this.client.getPackageVersions(orgName, packageName,
                        Paths.get(repoLocation));
                remotePackageVersions.stream().map(PackageVersion::from).forEach(packageVersions::add);
            } catch (MavenResolverClientException e) {
                // ignore and return the list from the FS cache location
            }
        }
        SemanticVersion minSemVer = null;
        PackageVersion packageVersion = request.version().orElse(null);
        if (packageVersion != null) {
            minSemVer = SemanticVersion.from(packageVersion.toString());
        }
        List<SemanticVersion> semVers = packageVersions.stream()
                .map(version -> SemanticVersion.from(version.toString())).toList();
        ProjectUtils.CompatibleRange compatibilityRange = ProjectUtils.getCompatibleRange(
                minSemVer, options.packageLockingMode());
        List<SemanticVersion> compatibleVersions = ProjectUtils.getVersionsInCompatibleRange(
                minSemVer, semVers, compatibilityRange);
        return compatibleVersions.stream().map(PackageVersion::from).collect(Collectors.toList());
    }

    @Override
    public Map<String, List<String>> getPackages() {
        // We only return locally cached packages
        return fileSystemRepo.getPackages();
    }

    @Override
    public Collection<ImportModuleResponse> getPackageNames(Collection<ImportModuleRequest> requests,
                                                            ResolutionOptions options) {
        Set<ImportModuleResponse> filesystem = new HashSet<>(fileSystemRepo.getPackageNames(requests, options));
        if (options.offline()) {
            return filesystem;
        }

        List<ImportModuleResponse> importModuleResponseList = new ArrayList<>();
        try {
            for (ImportModuleRequest importModuleRequest : requests) {
                String orgName = importModuleRequest.packageOrg().value();
                List<PackageName> possiblePackageNames = ProjectUtils.getPossiblePackageNames(
                        importModuleRequest.packageOrg(), importModuleRequest.moduleName());
                for (PackageName packageName : possiblePackageNames) {
                    List<String> packageVersions = this.client.getPackageVersions(
                            orgName, packageName.toString(), Paths.get(repoLocation));
                    if (!packageVersions.isEmpty()) {
                        List<PackageVersion> packageVersionsList = new ArrayList<>();
                        packageVersions.stream().map(PackageVersion::from).forEach(packageVersionsList::add);
                        PackageVersion latest = findLatest(packageVersionsList);
                        PackageDescriptor resolvedDescriptor = PackageDescriptor.from(
                                PackageOrg.from(orgName), PackageName.from(packageName.toString()), latest);
                        importModuleResponseList.add(
                                new ImportModuleResponse(resolvedDescriptor, importModuleRequest));
                        break;
                    }
                }
            }
        } catch (MavenResolverClientException e) {
            // ignore and return the list from the FS cache location
        }
        return importModuleResponseList;
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
            // get the latest package version
            PackageVersion latest = findLatest(packageVersions);
            descriptorSet.add(createMetadataResponse(request, latest));
        }
        return descriptorSet;

    }

    public boolean getPackageFromRemoteRepo(String org, String name, String version) {
        try {
            Path tmpDownloadDirectory = Files.createTempDirectory("ballerina-" + System.nanoTime());
            client.pullPackage(org, name, version,
                    String.valueOf(tmpDownloadDirectory.toAbsolutePath()));
            Path balaDownloadPath = tmpDownloadDirectory.resolve(org).resolve(name).resolve(version)
                    .resolve(name + "-" + version + BALA_EXTENSION);
            Path temporaryExtractionPath = tmpDownloadDirectory.resolve(org).resolve(name)
                    .resolve(version).resolve(PLATFORM);
            ProjectUtils.extractBala(balaDownloadPath, temporaryExtractionPath);
            Path packageJsonPath = temporaryExtractionPath.resolve("package.json");
            try (BufferedReader bufferedReader = Files.newBufferedReader(packageJsonPath, StandardCharsets.UTF_8)) {
                JsonObject resultObj = new Gson().fromJson(bufferedReader, JsonObject.class);
                String platform = resultObj.get(PLATFORM).getAsString();
                Path actualBalaPath = Path.of(this.repoLocation).resolve(org).resolve(name)
                        .resolve(version).resolve(platform);
                FileUtils.copyDirectory(temporaryExtractionPath.toFile(),
                        actualBalaPath.toFile());
            }
        } catch (IOException | MavenResolverClientException e) {
            return false;
        }
        return true;
    }

    private PackageVersion findLatest(Collection<PackageVersion> packageVersions) {
        if (packageVersions.isEmpty()) {
            return null;
        }

        PackageVersion latestVersion = packageVersions.iterator().next();
        for (PackageVersion pkgVersion : packageVersions) {
            latestVersion = getLatest(latestVersion, pkgVersion);
        }
        return latestVersion;
    }

    private PackageMetadataResponse createMetadataResponse(ResolutionRequest resolutionRequest,
                                                           PackageVersion latest) {
        PackageDescriptor resolvedDescriptor = PackageDescriptor.from(
                resolutionRequest.orgName(), resolutionRequest.packageName(), latest,
                resolutionRequest.repositoryName().orElse(null));
        DependencyGraph<PackageDescriptor> dependencyGraph = getDependencyGraph(resolutionRequest.orgName(),
                resolutionRequest.packageName(), latest);
        return PackageMetadataResponse.from(resolutionRequest, resolvedDescriptor, dependencyGraph);
    }

    private DependencyGraph<PackageDescriptor> getDependencyGraph(PackageOrg org, PackageName name,
                                                                  PackageVersion version) {
        boolean packageExists = isPackageExists(org, name, version);
        if (!packageExists) {
            PackageDescriptor pkdDesc = PackageDescriptor.from(org, name, version);
            ResolutionRequest request = ResolutionRequest.from(pkdDesc, PackageDependencyScope.DEFAULT);
            Optional<Package> pkg = getPackage(request, ResolutionOptions.builder().build());
            if (pkg.isEmpty()) {
                return DependencyGraph.emptyGraph();
            }
        }
        return this.fileSystemRepo.getDependencyGraph(org, name, version);
    }

    public Collection<ModuleDescriptor> getModules(PackageOrg org, PackageName name, PackageVersion version) {
        return this.fileSystemRepo.getModules(org, name, version);
    }

    public boolean isPackageExists(PackageOrg org, PackageName name, PackageVersion version) {
        return this.fileSystemRepo.isPackageExists(org, name, version);
    }
}
