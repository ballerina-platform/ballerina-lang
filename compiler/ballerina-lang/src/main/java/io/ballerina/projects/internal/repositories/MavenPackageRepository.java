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

import io.ballerina.projects.DependencyGraph;
import io.ballerina.projects.ModuleDescriptor;
import io.ballerina.projects.Package;
import io.ballerina.projects.PackageDependencyScope;
import io.ballerina.projects.PackageDescriptor;
import io.ballerina.projects.PackageName;
import io.ballerina.projects.PackageOrg;
import io.ballerina.projects.PackageVersion;
import io.ballerina.projects.ProjectException;
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
import io.ballerina.projects.internal.repositories.CustomPkgRepositoryUtils.RemotePackageInfo;
import org.ballerinalang.central.client.CentralClientConstants;
import org.ballerinalang.maven.bala.client.MavenResolverClient;
import org.ballerinalang.maven.bala.client.MavenResolverClientException;
import org.ballerinalang.maven.bala.client.model.PackageResolutionResponse;
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static io.ballerina.projects.DependencyGraph.DependencyGraphBuilder.getBuilder;

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
    private final boolean isProxyCentral;

    protected MavenPackageRepository(Environment environment, Path cacheDirectory, String distributionVersion,
                                     MavenResolverClient client, String repoLocation, boolean isProxyCentral) {
        this.fileSystemRepo = new FileSystemRepository(environment, cacheDirectory, distributionVersion);
        this.client = client;
        this.repoLocation = repoLocation;
        this.isProxyCentral = isProxyCentral;
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
            mvnClient.addRepository(repository.proxyCentral() ? "" : repository.id(), repository.url(),
                    repository.username(), repository.password());
        } else {
            mvnClient.addRepository(repository.proxyCentral() ? "" : repository.id(), repository.url());
        }

        Settings settings;
        settings = RepoUtils.readSettings();
        Proxy proxy = settings.getProxy();
        mvnClient.setProxy(proxy.host(), proxy.port(), proxy.username(), proxy.password());

        String repoLocation = cacheDirectory.resolve("bala").toAbsolutePath().toString();

        return new MavenPackageRepository(environment, cacheDirectory, ballerinaShortVersion, mvnClient,
                repoLocation, repository.proxyCentral());
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
        return CustomPkgRepositoryUtils.getPackageVersions(request, options, fileSystemRepo,
                () -> listRemoteVersions(request.orgName().value(), request.packageName().value()));
    }

    private List<String> listRemoteVersions(String orgName, String packageName) {
        try {
            if (isProxyCentral) {
                return this.client.getPackageVersionsInCentralProxy(orgName, packageName,
                        RepoUtils.getBallerinaShortVersion(), Paths.get(repoLocation));
            }
            return this.client.getPackageVersions(orgName, packageName, Paths.get(repoLocation));
        } catch (MavenResolverClientException e) {
            // ignore and return the list from the FS cache location
            return Collections.emptyList();
        }
    }

    @Override
    public Map<String, List<String>> getPackages() {
        // We only return locally cached packages
        return fileSystemRepo.getPackages();
    }

    @Override
    public Collection<ImportModuleResponse> getPackageNames(Collection<ImportModuleRequest> requests,
                                                            ResolutionOptions options) {
        return CustomPkgRepositoryUtils.getPackageNames(requests, options, fileSystemRepo,
                this::listRemoteVersions);
    }


    @Override
    public Collection<PackageMetadataResponse> getPackageMetadata(Collection<ResolutionRequest> requests,
                                                                  ResolutionOptions options) {
        if (isProxyCentral) {
            return CustomPkgRepositoryUtils.resolveWithCache(requests, options, fileSystemRepo,
                    remainingRequests -> resolvePackageMetadata(remainingRequests, options));
        }
        return resolvePackageMetadata(requests, options);
    }

    private List<PackageMetadataResponse> resolvePackageMetadata(Collection<ResolutionRequest> requests,
                                                                 ResolutionOptions options) {
        return CustomPkgRepositoryUtils.resolveLatestVersions(requests,
                request -> getPackageVersions(request, options),
                (request, latest) -> CustomPkgRepositoryUtils.createMetadataResponse(
                        request, latest, this::fetchPackageInfo));
    }

    public boolean getPackageFromRemoteRepo(String org, String name, String version) {
        Path versionDir = Path.of(this.repoLocation).resolve(org).resolve(name).resolve(version);
        try {
            CustomPkgRepositoryUtils.pullIntoCache(org, name, version, versionDir,
                    downloadDirectory -> client.pullPackage(org, name, version,
                            String.valueOf(downloadDirectory.toAbsolutePath())));
        } catch (IOException | MavenResolverClientException e) {
            return false;
        }
        return true;
    }

    private RemotePackageInfo fetchPackageInfo(ResolutionRequest request, PackageVersion version) {
        PackageOrg org = request.orgName();
        PackageName name = request.packageName();
        if (isProxyCentral) {
            try {
                PackageResolutionResponse pkgResolutionResp = this.client.resolveDependency(org.value(), name.value(),
                        version.toString(), RepoUtils.getBallerinaShortVersion(), repoLocation);
                PackageResolutionResponse.Package resolved = pkgResolutionResp.resolved().getFirst();
                // The proxy reports the deprecated status alongside the dependency graph
                return new RemotePackageInfo(createPackageDependencyGraph(resolved),
                        Optional.ofNullable(resolved.getDeprecated()),
                        Objects.requireNonNullElse(resolved.getDeprecateMessage(), ""));
            } catch (MavenResolverClientException e) {
                // ignore and return the dependency graph from the FS cache location
            }
        }
        return RemotePackageInfo.withUnknownDeprecation(getDependencyGraph(org, name, version));
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

    private static DependencyGraph<PackageDescriptor> createPackageDependencyGraph(
            PackageResolutionResponse.Package aPackage) {
        DependencyGraph.DependencyGraphBuilder<PackageDescriptor> graphBuilder = getBuilder();

        for (PackageResolutionResponse.Dependency dependency : aPackage.dependencyGraph()) {
            PackageDescriptor pkg = PackageDescriptor.from(PackageOrg.from(dependency.org()),
                    PackageName.from(dependency.name()), PackageVersion.from(dependency.version()));
            Set<PackageDescriptor> dependentPackages = new HashSet<>();
            for (PackageResolutionResponse.Dependency dependencyPkg : dependency.dependencies()) {
                dependentPackages.add(PackageDescriptor.from(PackageOrg.from(dependencyPkg.org()),
                        PackageName.from(dependencyPkg.name()),
                        PackageVersion.from(dependencyPkg.version())));
            }
            graphBuilder.addDependencies(pkg, dependentPackages);
        }

        return graphBuilder.build();
    }


}
