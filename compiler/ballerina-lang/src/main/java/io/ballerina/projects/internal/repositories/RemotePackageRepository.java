package io.ballerina.projects.internal.repositories;

import io.ballerina.projects.DependencyGraph;
import io.ballerina.projects.JvmTarget;
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
import io.ballerina.projects.environment.PackageRepository;
import io.ballerina.projects.environment.ResolutionRequest;
import io.ballerina.projects.environment.ResolutionResponse;
import io.ballerina.projects.internal.ImportModuleRequest;
import io.ballerina.projects.internal.ImportModuleResponse;
import org.ballerinalang.central.client.CentralAPIClient;
import org.ballerinalang.central.client.exceptions.CentralClientException;
import org.ballerinalang.central.client.exceptions.ConnectionErrorException;
import org.ballerinalang.central.client.model.PackageNameResolutionRequest;
import org.ballerinalang.central.client.model.PackageNameResolutionResponse;
import org.ballerinalang.central.client.model.PackageResolutionRequest;
import org.ballerinalang.central.client.model.PackageResolutionResponse;
import org.wso2.ballerinalang.util.RepoUtils;

import java.net.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static io.ballerina.projects.DependencyGraph.DependencyGraphBuilder.getBuilder;
import static io.ballerina.projects.util.ProjectUtils.getAccessTokenOfCLI;
import static io.ballerina.projects.util.ProjectUtils.initializeProxy;
import static org.wso2.ballerinalang.programfile.ProgramFileConstants.SUPPORTED_PLATFORMS;

/**
 * This class represents the remote package repository.
 *
 * @since 2.0.0
 */
public class RemotePackageRepository implements PackageRepository {

    private final FileSystemRepository fileSystemRepo;
    private final CentralAPIClient client;

    public RemotePackageRepository(FileSystemRepository fileSystemRepo, CentralAPIClient client) {
        this.fileSystemRepo = fileSystemRepo;
        this.client = client;
    }

    public static RemotePackageRepository from(Environment environment, Path cacheDirectory, String repoUrl,
                                               Settings settings) {
        if (Files.notExists(cacheDirectory)) {
            throw new ProjectException("cache directory does not exists: " + cacheDirectory);
        }
        String ballerinaShortVersion = RepoUtils.getBallerinaShortVersion();
        FileSystemRepository fileSystemRepository = new FileSystemRepository(
                environment, cacheDirectory, ballerinaShortVersion);
        Proxy proxy = initializeProxy(settings.getProxy());
        CentralAPIClient client = new CentralAPIClient(repoUrl, proxy, getAccessTokenOfCLI(settings));

        return new RemotePackageRepository(fileSystemRepository, client);
    }

    public static RemotePackageRepository from(Environment environment, Path cacheDirectory, Settings settings) {
        String repoUrl = RepoUtils.getRemoteRepoURL();
        if ("".equals(repoUrl)) {
            throw new ProjectException("remote repo url is empty");
        }

        return from(environment, cacheDirectory, repoUrl, settings);
    }

    @Override
    public Optional<Package> getPackage(ResolutionRequest resolutionRequest) {
        // Avoid resolving from remote repository for lang repo tests
        String langRepoBuild = System.getProperty("LANG_REPO_BUILD");
        if (langRepoBuild != null) {
            return Optional.empty();
        }

        // Check if the package is in cache
        Optional<Package> cachedPackage = this.fileSystemRepo.getPackage(resolutionRequest);
        if (cachedPackage.isPresent()) {
            return cachedPackage;
        }

        String packageName = resolutionRequest.packageName().value();
        String orgName = resolutionRequest.orgName().value();
        String version = resolutionRequest.version().isPresent() ? resolutionRequest.version().get().toString() : null;

        Path packagePathInBalaCache = this.fileSystemRepo.bala.resolve(orgName).resolve(packageName);

        // If environment is online pull from central
        if (!resolutionRequest.offline()) {
            for (String supportedPlatform : SUPPORTED_PLATFORMS) {
                try {
                    this.client.pullPackage(orgName, packageName, version, packagePathInBalaCache, supportedPlatform,
                            RepoUtils.getBallerinaVersion(), true);
                } catch (CentralClientException e) {
                    // ignore when get package fail
                }
            }
        }

        return this.fileSystemRepo.getPackage(resolutionRequest);
    }

    @Override
    public List<PackageVersion> getPackageVersions(ResolutionRequest resolutionRequest) {
        String langRepoBuild = System.getProperty("LANG_REPO_BUILD");
        if (langRepoBuild != null) {
            return Collections.emptyList();
        }
        String orgName = resolutionRequest.orgName().value();
        String packageName = resolutionRequest.packageName().value();

        // First, Get local versions
        Set<PackageVersion> packageVersions = new HashSet<>(fileSystemRepo.getPackageVersions(resolutionRequest));

        // If the resolution request specifies to resolve offline, we return the local version
        if (resolutionRequest.offline()) {
            return new ArrayList<>(packageVersions);
        }

        try {
            for (String version : this.client.getPackageVersions(orgName, packageName, JvmTarget.JAVA_11.code(),
                    RepoUtils.getBallerinaVersion())) {
                packageVersions.add(PackageVersion.from(version));
            }
        } catch (ConnectionErrorException e) {
            // ignore connect to remote repo failure
            return new ArrayList<>(packageVersions);
        } catch (CentralClientException e) {
            throw new ProjectException(e.getMessage());
        }
        return new ArrayList<>(packageVersions);
    }

    @Override
    public Map<String, List<String>> getPackages() {
        // We only return locally cached packages
        return fileSystemRepo.getPackages();
    }

    @Override
    public List<ImportModuleResponse> resolvePackageNames(List<ImportModuleRequest> importModuleRequests) {
        List<ImportModuleResponse> filesystem = fileSystemRepo.resolvePackageNames(importModuleRequests);
        List<ImportModuleResponse> unresolved = filesystem.stream()
                .filter(r -> r.resolutionStatus().equals(ResolutionResponse.ResolutionStatus.UNRESOLVED))
                .collect(Collectors.toList());

        try {
            if (unresolved.size() > 0) {
                PackageNameResolutionRequest resolutionRequest = toPackageNameResolutionRequest(unresolved);
                PackageNameResolutionResponse response = this.client.resolvePackageNames(resolutionRequest,
                        JvmTarget.JAVA_11.code(), RepoUtils.getBallerinaVersion(), true);
                List<ImportModuleResponse> remote = toImportModuleResponses(unresolved, response);
                return mergeNameResolution(filesystem, remote);
            }
        } catch (ConnectionErrorException e) {
            // ignore connect to remote repo failure
            // TODO we need to add diagnostics for resolution errors
        } catch (CentralClientException e) {
            throw new ProjectException(e.getMessage());
        }
        return filesystem;
    }

    private List<ImportModuleResponse> mergeNameResolution(List<ImportModuleResponse> filesystem,
                                                           List<ImportModuleResponse> remote) {
        List<ImportModuleResponse> all = new ArrayList<>();
        // We assume file system responses will have all module requests
        for (ImportModuleResponse fileResponse : filesystem) {
            if (fileResponse.resolutionStatus() == ResolutionResponse.ResolutionStatus.RESOLVED) {
                all.add(fileResponse);
            } else {
                // If remote has resolutions of unresolved we will substitute
                Optional<ImportModuleResponse> remoteResponse = remote.stream()
                        .filter(r -> r.importModuleRequest().equals(fileResponse.importModuleRequest())).findFirst();
                if (remoteResponse.isPresent()) {
                    all.add(remoteResponse.get());
                }
            }
        }
        return all;
    }

    private List<ImportModuleResponse> toImportModuleResponses(List<ImportModuleResponse> requests,
                                                               PackageNameResolutionResponse response) {
        List<ImportModuleResponse> result = new ArrayList<>();
        for (ImportModuleResponse module : requests) {
            PackageOrg packageOrg = module.importModuleRequest().packageOrg();
            String moduleName = module.importModuleRequest().moduleName();
            Optional<PackageNameResolutionResponse.Module> resolvedModule = response.resolvedModules().stream()
                    .filter(m -> m.getModuleName().equals(moduleName)
                            && m.getOrganization().equals(packageOrg.value())).findFirst();
            if (resolvedModule.isPresent()) {
                PackageDescriptor packageDescriptor = PackageDescriptor.from(packageOrg,
                        PackageName.from(resolvedModule.get().getPackageName()));
                ImportModuleResponse importModuleResponse = new ImportModuleResponse(packageDescriptor,
                        module.importModuleRequest());
                result.add(importModuleResponse);
            } else {
                result.add(new ImportModuleResponse(module.importModuleRequest()));
            }
        }
        return result;
    }


    private PackageNameResolutionRequest toPackageNameResolutionRequest(List<ImportModuleResponse> unresolved) {
        PackageNameResolutionRequest request = new PackageNameResolutionRequest();
        for (ImportModuleResponse module : unresolved) {
            request.addModule(module.importModuleRequest().packageOrg().value(),
                    module.importModuleRequest().moduleName());
        }
        return request;
    }

    public List<PackageMetadataResponse> resolvePackageMetadata(
            List<ResolutionRequest> resolutionRequests) {
        // Resolve all the requests locally
        List<PackageMetadataResponse> filesystem = fileSystemRepo.resolvePackageMetadata(resolutionRequests);

        // Filter out the requests that can be resolved online
        List<ResolutionRequest> online = resolutionRequests.stream()
                .filter(i -> {
                    return i.offline() == false;
                }).collect(Collectors.toList());
        // Resolve the requests from remote repository
        try {
            List<PackageMetadataResponse> remoteResolution;
            if (online.size() > 0) {
                PackageResolutionRequest packageResolutionRequest = toPackageResolutionRequest(online);
                PackageResolutionResponse packageResolutionResponse = client.resolveDependencies(
                        packageResolutionRequest, JvmTarget.JAVA_11.code(),
                        RepoUtils.getBallerinaVersion(), true);

                remoteResolution = fromPackageResolutionResponse(resolutionRequests, packageResolutionResponse);
                // Merge central requests and local requests
                // Here we will pick the latest package from remote or local
                return mergeResolution(remoteResolution, filesystem);
            }
        } catch (ConnectionErrorException e) {
            // ignore connect to remote repo failure
            // TODO we need to add diagnostics for resolution errors
        } catch (CentralClientException e) {
            throw new ProjectException(e.getMessage());
        }
        // If any issue we will return filesystem results
        return filesystem;
    }

    private List<PackageMetadataResponse> mergeResolution(
            List<PackageMetadataResponse> remoteResolution,
            List<PackageMetadataResponse> filesystem) {
        // accumulate the result to a new list
        List<PackageMetadataResponse> mergedResults = new ArrayList<>();
        // Iterate resolutions in original list
        for (PackageMetadataResponse remote : remoteResolution) {
            Optional<PackageMetadataResponse> local;
            local = filesystem.stream().filter(d -> {
                return d.packageLoadRequest().equals(remote.packageLoadRequest());
            }).findFirst();

            // if a local dependency is not found this case cannot be true
            if (local.isEmpty()) {
                // Both remote and local should have the same number of results
                throw new AssertionError("Un resolved local dependency");
            }
            PackageMetadataResponse localDescriptor = local.get();
            // if local is unresolved add remote
            if (localDescriptor.resolutionStatus() == ResolutionResponse.ResolutionStatus.UNRESOLVED) {
                mergedResults.add(remote);
                continue;
            }
            if (remote.resolutionStatus() == ResolutionResponse.ResolutionStatus.UNRESOLVED) {
                mergedResults.add(localDescriptor);
                continue;
            }
            // pick the latest of both
            SemanticVersion.VersionCompatibilityResult versionCompatibilityResult =
                    remote.resolvedDescriptor().version().compareTo(
                            localDescriptor.resolvedDescriptor().version());

            if (versionCompatibilityResult.equals(SemanticVersion.VersionCompatibilityResult.GREATER_THAN)) {
                mergedResults.add(remote);
            } else {
                mergedResults.add(localDescriptor);
            }
        }
        return mergedResults;
    }

    private List<PackageMetadataResponse> fromPackageResolutionResponse(
            List<ResolutionRequest> packageLoadRequests, PackageResolutionResponse packageResolutionResponse) {
        // List<PackageResolutionResponse.Package> resolved = packageResolutionResponse.resolved();
        List<PackageMetadataResponse> response = new ArrayList<>();
        for (ResolutionRequest resolutionRequest : packageLoadRequests) {
            // find response from server
            // checked in resolved group
            Optional<PackageResolutionResponse.Package> match = packageResolutionResponse.resolved().stream()
                    .filter(p -> p.name().equals(resolutionRequest.packageName().value()) &&
                            p.orgName().equals(resolutionRequest.orgName().value())).findFirst();
            // If we found a match we will add it to response
            if (match.isPresent()) {
                PackageVersion version = PackageVersion.from(match.get().version());
                DependencyGraph<PackageDescriptor> dependancies = createPackageDependencyGraph(match.get());
                PackageDescriptor packageDescriptor = PackageDescriptor.from(resolutionRequest.orgName(),
                        resolutionRequest.packageName(),
                        version);
                PackageMetadataResponse responseDescriptor = PackageMetadataResponse.from(resolutionRequest,
                        packageDescriptor,
                        dependancies);
                response.add(responseDescriptor);
            } else {
                // If the package is not in resolved we assume the package is unresolved
                response.add(PackageMetadataResponse.createUnresolvedResponse(resolutionRequest));
            }
        }
        return response;
    }

    private static DependencyGraph<PackageDescriptor> createPackageDependencyGraph(
            PackageResolutionResponse.Package aPackage) {
        DependencyGraph.DependencyGraphBuilder<PackageDescriptor> graphBuilder = getBuilder();

        for (PackageResolutionResponse.Package dependency : aPackage.dependencies()) {
            PackageDescriptor pkg = PackageDescriptor.from(PackageOrg.from(dependency.orgName()),
                    PackageName.from(dependency.name()), PackageVersion.from(dependency.version()));
            Set<PackageDescriptor> dependentPackages = new HashSet<>();
            for (PackageResolutionResponse.Package dependencyPkg : dependency.dependencies()) {
                dependentPackages.add(PackageDescriptor.from(PackageOrg.from(dependencyPkg.orgName()),
                        PackageName.from(dependencyPkg.name()),
                        PackageVersion.from(dependencyPkg.version())));
            }
            graphBuilder.addDependencies(pkg, dependentPackages);
        }

        return graphBuilder.build();
    }


    private PackageResolutionRequest toPackageResolutionRequest(List<ResolutionRequest> resolutionRequests) {
        PackageResolutionRequest packageResolutionRequest = new PackageResolutionRequest();
        for (ResolutionRequest resolutionRequest : resolutionRequests) {
            PackageResolutionRequest.Mode mode = PackageResolutionRequest.Mode.HARD;
            switch (resolutionRequest.packageLockingMode()) {
                case HARD:
                    mode = PackageResolutionRequest.Mode.HARD;
                    break;
                case MEDIUM:
                    mode = PackageResolutionRequest.Mode.MEDIUM;
                    break;
                case SOFT:
                    mode = PackageResolutionRequest.Mode.SOFT;
                    break;
            }
            String version = resolutionRequest.version().map(v -> v.value().toString()).orElse("");
            packageResolutionRequest.addPackage(resolutionRequest.orgName().value(),
                    resolutionRequest.packageName().value(),
                    version,
                    mode);
        }
        return packageResolutionRequest;
    }
}
