package io.ballerina.projects.internal.repositories;

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
import io.ballerina.projects.environment.PackageLockingMode;
import io.ballerina.projects.environment.PackageMetadataResponse;
import io.ballerina.projects.environment.PackageRepository;
import io.ballerina.projects.environment.ResolutionOptions;
import io.ballerina.projects.environment.ResolutionRequest;
import io.ballerina.projects.environment.ResolutionResponse;
import io.ballerina.projects.internal.ImportModuleRequest;
import io.ballerina.projects.internal.ImportModuleResponse;
import io.ballerina.projects.internal.index.IndexPackage;
import io.ballerina.projects.internal.index.PackageIndex;
import io.ballerina.projects.internal.index.PackageIndexBuilder;
import io.ballerina.projects.util.ProjectUtils;
import org.ballerinalang.central.client.CentralAPIClient;
import org.ballerinalang.central.client.CentralClientConstants;
import org.ballerinalang.central.client.exceptions.CentralClientException;
import org.ballerinalang.central.client.exceptions.ConnectionErrorException;
import org.ballerinalang.central.client.model.PackageNameResolutionRequest;
import org.ballerinalang.central.client.model.PackageNameResolutionResponse;
import org.ballerinalang.central.client.model.PackageResolutionRequest;
import org.ballerinalang.central.client.model.PackageResolutionResponse;
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.PrintStream;
import java.net.Proxy;
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

import static io.ballerina.projects.util.ProjectUtils.getAccessTokenOfCLI;
import static io.ballerina.projects.util.ProjectUtils.getLatest;
import static io.ballerina.projects.util.ProjectUtils.initializeProxy;

/**
 * This class represents the remote package repository.
 *
 * @since 2.0.0
 */
public class RemotePackageRepository implements PackageRepository {

    private final FileSystemRepository fileSystemRepo;
    private final CentralAPIClient client;
    private final PackageIndex packageIndex;

    private static final String ANY = "any";

    public RemotePackageRepository(FileSystemRepository fileSystemRepo, CentralAPIClient client,
                                   PackageIndex packageIndex) {
        this.fileSystemRepo = fileSystemRepo;
        this.client = client;
        this.packageIndex = packageIndex;
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
        CentralAPIClient client = new CentralAPIClient(repoUrl, proxy, settings.getProxy().username(),
                settings.getProxy().password(), getAccessTokenOfCLI(settings),
                settings.getCentral().getConnectTimeout(),
                settings.getCentral().getReadTimeout(), settings.getCentral().getWriteTimeout(),
                settings.getCentral().getCallTimeout(), settings.getCentral().getMaxRetries());
        PackageIndex packageIndex = new PackageIndexBuilder().build();
        return new RemotePackageRepository(fileSystemRepository, client, packageIndex);
    }

    public static RemotePackageRepository from(Environment environment, Path cacheDirectory, Settings settings) {
        String repoUrl = RepoUtils.getRemoteRepoURL();
        if ("".equals(repoUrl)) {
            throw new ProjectException("remote repo url is empty");
        }

        return from(environment, cacheDirectory, repoUrl, settings);
    }

    @Override
    public Optional<Package> getPackage(ResolutionRequest request, ResolutionOptions options) {
        // Check if the package is in cache
        Optional<Package> cachedPackage = this.fileSystemRepo.getPackage(request, options);
        if (cachedPackage.isPresent()) {
            return cachedPackage;
        }

        String packageName = request.packageName().value();
        String orgName = request.orgName().value();
        String version = request.version().isPresent() ? request.version().get().toString() : null;

        Path packagePathInBalaCache = this.fileSystemRepo.bala.resolve(orgName).resolve(packageName);

        // If environment is online pull from central
        if (!options.offline()) {
            String supportedPlatform = Arrays.stream(JvmTarget.values())
                    .map(JvmTarget::code)
                    .collect(Collectors.joining(","));
            try {
                this.client.pullPackage(orgName, packageName, version, packagePathInBalaCache, supportedPlatform,
                        RepoUtils.getBallerinaVersion(), true);
            } catch (CentralClientException e) {
                boolean enableOutputStream =
                        Boolean.parseBoolean(System.getProperty(CentralClientConstants.ENABLE_OUTPUT_STREAM));
                if (enableOutputStream) {
                    final PrintStream out = System.out;
                    out.println("Error while pulling package [" + orgName + "/" + packageName + ":" + version +
                            "]: " + e.getMessage());

                }
            }
        }

        return this.fileSystemRepo.getPackage(request, options);
    }

    @Override
    public Collection<PackageVersion> getPackageVersions(ResolutionRequest request, ResolutionOptions options) {
        String langRepoBuild = System.getProperty("LANG_REPO_BUILD");
        if (langRepoBuild != null) {
            return Collections.emptyList();
        }
        // TODO: Can use the index instead. If offline, set offline flag in the index and find.
        // First, Get local versions
        Set<PackageVersion> packageVersions = new HashSet<>(fileSystemRepo.getPackageVersions(request, options));

        // If the resolution request specifies to resolve offline, we return the local version
        if (options.offline()) {
            return new ArrayList<>(packageVersions);
        }

        List<IndexPackage> packagesFromIndex = this.packageIndex.getPackage(request.orgName(), request.packageName());
        if (packagesFromIndex != null) {
            List<IndexPackage> distCompatiblePackages = filterDistributionCompatiblePackages(packagesFromIndex);
            List<IndexPackage> platformCompatiblePackages = filterPackagesByPlatform(distCompatiblePackages);
            List<PackageVersion> versionsFromIndex = platformCompatiblePackages.stream().map(IndexPackage::version)
                    .toList();
            packageVersions.addAll(versionsFromIndex);
            return new ArrayList<>(packageVersions);
        }

        // if the package is not in the index, we try to get the versions from the ballerina central,
        // assuming it's a private package.
        getPackageVersionsFromRemote(request, packageVersions);
        return new ArrayList<>(packageVersions);
    }

    private void getPackageVersionsFromRemote(ResolutionRequest request, Set<PackageVersion> packageVersions) {
        String orgName = request.orgName().value();
        String packageName = request.packageName().value();
        try {
            String supportedPlatform = Arrays.stream(JvmTarget.values())
                    .map(JvmTarget::code)
                    .collect(Collectors.joining(","));
            for (String version : this.client.getPackageVersions(orgName, packageName, supportedPlatform,
                    RepoUtils.getBallerinaVersion())) {
                packageVersions.add(PackageVersion.from(version));
            }
        } catch (ConnectionErrorException e) {
            // ignore connect to remote repo failure
        } catch (CentralClientException e) {
            throw new ProjectException(e.getMessage());
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
        Collection<ImportModuleResponse> filesystem = fileSystemRepo.getPackageNames(requests, options);
        if (options.offline()) {
            return filesystem;
        }

        Collection<ImportModuleResponse> index = getPackageNamesFromIndex(requests);
        List<ImportModuleRequest> unresolved = index.stream()
                .filter(r -> r.resolutionStatus().equals(ResolutionResponse.ResolutionStatus.UNRESOLVED))
                .map(ImportModuleResponse::importModuleRequest)
                .toList();
        Collection<ImportModuleResponse> remote = Collections.emptyList();
        if (!unresolved.isEmpty()) {
            // Unresolved modules might be from the private packages. Let's try to resolve with the central API.
            remote = getPackageNamesFromRemote(unresolved);
        }
        return mergeNameResolution(filesystem, index, remote);
    }

    private List<ImportModuleResponse> getPackageNamesFromIndex(Collection<ImportModuleRequest> requests) {
        List<ImportModuleResponse> responses = new ArrayList<>();
        for (ImportModuleRequest request: requests) {
            List<IndexPackage> indexPackages = packageIndex.getPackageContainingModule(
                    request.packageOrg(), request.moduleName());
            List<IndexPackage> distributionCompatiblePackages = filterDistributionCompatiblePackages(indexPackages);
            if (distributionCompatiblePackages.isEmpty()) {
                responses.add(ImportModuleResponse.createUnresolvedResponse(request));
                continue;
            }

            // Try to find a match from the list of possible packages
            Optional<IndexPackage> resolved = resolveModuleFromPossiblePackages(
                    request.possiblePackages(), distributionCompatiblePackages);
            if (resolved.isPresent()) {
                responses.add(new ImportModuleResponse(PackageDescriptor.from(resolved.get().org(),
                        resolved.get().name(), resolved.get().version()), request));
                continue;
            }

            // If not found, find the latest version from the indexPackages.
            resolved = findLatestInRange(distributionCompatiblePackages);
            if (resolved.isPresent()) {
                responses.add(new ImportModuleResponse(PackageDescriptor.from(resolved.get().org(),
                        resolved.get().name(), resolved.get().version()), request));
                continue;
            }

            // If there is still no matching version, return unresolved response.
            responses.add(ImportModuleResponse.createUnresolvedResponse(request));
        }
        return responses;
    }

    private Collection<ImportModuleResponse> getPackageNamesFromRemote(Collection<ImportModuleRequest> requests) {
        try {
            PackageNameResolutionRequest resolutionRequest = toPackageNameResolutionRequest(requests);
            String supportedPlatform = Arrays.stream(JvmTarget.values())
                    .map(JvmTarget::code)
                    .collect(Collectors.joining(","));
            PackageNameResolutionResponse response = this.client.resolvePackageNames(resolutionRequest,
                    supportedPlatform, RepoUtils.getBallerinaVersion());
            return new ArrayList<>(toImportModuleResponses(requests, response));
        } catch (ConnectionErrorException ignored) {
            return new ArrayList<>();
        } catch (CentralClientException e) {
            throw new ProjectException(e.getMessage());
        }
    }

    private Optional<IndexPackage> resolveModuleFromPossiblePackages(List<PackageDescriptor> possiblePackages,
                                                             List<IndexPackage> indexPackages) {
        PackageVersion resolvedVersion;
        for (PackageDescriptor possiblePkg : possiblePackages) {
            for (IndexPackage indexPkg: indexPackages) {
                if (!possiblePkg.org().equals(indexPkg.org()) || !possiblePkg.name().equals(indexPkg.name())) {
                    continue;
                }
                resolvedVersion = possiblePkg.version() != null ? possiblePkg.version() : indexPkg.version();
                List<IndexPackage> pkgsInRange = filterPackagesInRange(
                        indexPackages,
                        Optional.ofNullable(resolvedVersion),
                        PackageLockingMode.MEDIUM);
                Optional<IndexPackage> pkg = findLatestInRange(pkgsInRange);
                if (pkg.isPresent()) {
                    return pkg;
                }
            }
        }
        return Optional.empty();
    }

    private PackageNameResolutionRequest toPackageNameResolutionRequest(Collection<ImportModuleRequest> unresolved) {
        PackageNameResolutionRequest request = new PackageNameResolutionRequest();
        for (ImportModuleRequest module : unresolved) {
            if (module.possiblePackages().isEmpty()) {
                request.addModule(module.packageOrg().value(),
                        module.moduleName());
                continue;
            }
            List<PackageNameResolutionRequest.Module.PossiblePackage> possiblePackages = new ArrayList<>();
            for (PackageDescriptor possiblePackage : module.possiblePackages()) {
                possiblePackages.add(new PackageNameResolutionRequest.Module.PossiblePackage(
                        possiblePackage.org().toString(),
                        possiblePackage.name().toString(),
                        possiblePackage.version().toString()));
            }
            request.addModule(module.packageOrg().value(),
                    module.moduleName(), possiblePackages, PackageResolutionRequest.Mode.MEDIUM);
        }
        return request;
    }

    private List<ImportModuleResponse> toImportModuleResponses(Collection<ImportModuleRequest> requests,
                                                               PackageNameResolutionResponse response) {
        List<ImportModuleResponse> result = new ArrayList<>();
        for (ImportModuleRequest module : requests) {
            PackageOrg packageOrg = module.packageOrg();
            String moduleName = module.moduleName();
            Optional<PackageNameResolutionResponse.Module> resolvedModule = response.resolvedModules().stream()
                    .filter(m -> m.getModuleName().equals(moduleName)
                            && m.getOrganization().equals(packageOrg.value())).findFirst();
            if (resolvedModule.isPresent()) {
                PackageDescriptor packageDescriptor = PackageDescriptor.from(packageOrg,
                        PackageName.from(resolvedModule.get().getPackageName()),
                        PackageVersion.from(resolvedModule.get().getVersion()));
                ImportModuleResponse importModuleResponse = new ImportModuleResponse(packageDescriptor, module);
                result.add(importModuleResponse);
            } else {
                result.add(new ImportModuleResponse(module));
            }
        }
        return result;
    }

    private List<ImportModuleResponse> mergeNameResolution(Collection<ImportModuleResponse> filesystem,
                                                           Collection<ImportModuleResponse> index,
                                                           Collection<ImportModuleResponse> remote) {
        return new ArrayList<>(
            Stream.of(filesystem, index, remote)
                .flatMap(Collection::stream).collect(Collectors.toMap(
                    ImportModuleResponse::importModuleRequest, Function.identity(),
                    (ImportModuleResponse x, ImportModuleResponse y) -> {
                        if (y.resolutionStatus().equals(ResolutionResponse.ResolutionStatus.UNRESOLVED)) {
                            return x;
                        } else if (x.resolutionStatus().equals(ResolutionResponse.ResolutionStatus.UNRESOLVED)) {
                            return y;
                        } else if (getLatest(x.packageDescriptor().version(),
                                y.packageDescriptor().version()).equals(
                                y.packageDescriptor().version())) {
                            return y;
                        }
                        return x;
                    })).values());
    }

    @Override
    public PackageMetadataResponse getPackageMetadata(ResolutionRequest request,
                                                                  ResolutionOptions options) {
        // Is this wanted?
        if (request == null) {
            return null;
        }

        packageIndex.setOffline(options.offline());

        // If the locking mode is LOCKED or HARD, we should return the resolved package from the index.
        if (request.packageLockingMode().equals(PackageLockingMode.LOCKED)
                || request.packageLockingMode().equals(PackageLockingMode.HARD)) {
            IndexPackage resolvedPackage = packageIndex.getVersion(
                    request.orgName(),
                    request.packageName(),
                    request.version().orElse(null));
            return createResolutionResponse(request, resolvedPackage);
        }

        // If the locking mode is MEDIUM or SOFT, we should return the latest compatible package from the index.
        List<IndexPackage> indexPackages = packageIndex.getPackage(request.orgName(), request.packageName());
        if (indexPackages != null) {
            indexPackages = filterDistributionCompatiblePackages(indexPackages);
            indexPackages = filterDeprecatedPackages(indexPackages);
            indexPackages = filterPackagesByPlatform(indexPackages);
            indexPackages = filterStablePackages(indexPackages);
            indexPackages = filterPackagesInRange(indexPackages, request.version(), request.packageLockingMode());
            Optional<IndexPackage> latest = findLatestInRange(indexPackages);
            if (latest.isPresent()) {
                return createResolutionResponse(request, latest.get());
            }
        }

        // If not resolved, might be a private package. Let's try to resolve with the central API.
        PackageResolutionRequest packageResolutionRequest = toPackageResolutionRequest(request);
        try {
            return fromPackageResolutionResponse(request, packageResolutionRequest); // TODO: check why the error
        } catch (CentralClientException e) {
            throw new ProjectException(e.getMessage());
        }
    }

    private PackageMetadataResponse createResolutionResponse(ResolutionRequest request, IndexPackage resolvedPackage) {
        if (resolvedPackage == null) {
            return PackageMetadataResponse.createUnresolvedResponse(request);
        }
        if (!ProjectUtils.isDistributionSupported(resolvedPackage.ballerinaVersion())) {
            return PackageMetadataResponse.createUnresolvedResponse(request);
        }
        return PackageMetadataResponse.from(request, resolvedPackage.packageDescriptor(),
                resolvedPackage.dependencies());
    }

    private List<IndexPackage> filterDistributionCompatiblePackages(List<IndexPackage> indexPackages) {
        return indexPackages.stream().filter(pkg -> ProjectUtils.isDistributionSupported(pkg.ballerinaVersion()))
                .toList();
    }

    private List<IndexPackage> filterDeprecatedPackages(List<IndexPackage> indexPackages) {
        List<IndexPackage> nonDepPkgs =  indexPackages.stream().filter(pkg -> !pkg.isDeprecated()).toList();
        return nonDepPkgs.isEmpty() ? indexPackages : nonDepPkgs;
    }

    private List<IndexPackage> filterPackagesByPlatform(List<IndexPackage> indexPackages) {
        List<String> supportedPlatforms = Arrays.stream(JvmTarget.values()).map(JvmTarget::code).toList();
        return indexPackages.stream().filter(pkg ->
                ANY.equals(pkg.supportedPlatform()) || supportedPlatforms.contains(pkg.supportedPlatform())).toList();
    }

    private List<IndexPackage> filterStablePackages(List<IndexPackage> indexPackages) {
        List<IndexPackage> stableVersions = indexPackages.stream().filter(
                pkg -> !pkg.version().value().isPreReleaseVersion()).toList();
        return stableVersions.isEmpty() ? indexPackages : stableVersions;
    }

    private List<IndexPackage> filterPackagesInRange(List<IndexPackage> indexPackages,
                                                         Optional<PackageVersion> refVersionOpt,
                                                     PackageLockingMode packageLockingMode) {
        if (refVersionOpt.isEmpty()) {
            return indexPackages;
        }
        SemanticVersion refVersion = refVersionOpt.get().value();
        return indexPackages.stream().filter(pkg -> {
            SemanticVersion pkgVersion = pkg.version().value();
            return switch (packageLockingMode) {
                case LATEST -> true;
                case SOFT -> pkgVersion.major() == refVersion.major();
                case MEDIUM -> pkgVersion.major() == refVersion.major() && pkgVersion.minor() == refVersion.minor();
                case HARD, LOCKED, INVALID -> // We should not reach here because we handled locked and hard before.
                        throw new IllegalStateException("Unexpected value: " + packageLockingMode);
            };
        }).toList();
    }

    private Optional<IndexPackage> findLatestInRange(List<IndexPackage> indexPackages) {
        if (indexPackages.isEmpty()) {
            return Optional.empty();
        }
        IndexPackage latest = indexPackages.get(0);
        for (IndexPackage indexPackage : indexPackages) {
            if (indexPackage.version().value().greaterThan(latest.version().value())) {
                latest = indexPackage;
            }
        }
        return Optional.of(latest);
    }



    // Each dependency will contain only the direct dependencies of the package after the indexing implementation.
    private PackageMetadataResponse fromPackageResolutionResponse(
            ResolutionRequest resolutionRequest, PackageResolutionRequest packageResolutionRequest)
            throws CentralClientException {
        String supportedPlatform = Arrays.stream(JvmTarget.values())
                .map(JvmTarget::code)
                .collect(Collectors.joining(","));
        PackageResolutionResponse packageResolutionResponse = this.client.resolveDependencies(
                packageResolutionRequest, supportedPlatform, RepoUtils.getBallerinaVersion());
        // find response from server
        // checked in resolved group
        Optional<PackageResolutionResponse.Package> match = packageResolutionResponse.resolved().stream()
                .filter(p -> p.name().equals(resolutionRequest.packageName().value()) &&
                        p.org().equals(resolutionRequest.orgName().value())).findFirst();
        // If we found a match we will add it to response
        if (match.isPresent()) {
            PackageVersion version = PackageVersion.from(match.get().version());
            Collection<PackageDescriptor> dependencies = getDirectDependencies(match.get());
            PackageDescriptor packageDescriptor = PackageDescriptor.from(resolutionRequest.orgName(),
                    resolutionRequest.packageName(),
                    version, match.get().getDeprecated(), match.get().getDeprecateMessage());
            return PackageMetadataResponse.from(resolutionRequest,
                    packageDescriptor,
                    dependencies);
        }
        // If the package is not in resolved for all jvm platforms we assume the package is unresolved
        return PackageMetadataResponse.createUnresolvedResponse(resolutionRequest);
    }

    private static Collection<PackageDescriptor> getDirectDependencies(PackageResolutionResponse.Package aPackage) {
        List<PackageDescriptor> dependencies = new ArrayList<>();
        for (PackageResolutionResponse.Dependency dependency : aPackage.dependencyGraph()) {
            if (aPackage.org().equals(dependency.org()) && aPackage.name().equals(dependency.name())) {
                continue;
            }
            PackageDescriptor pkg = PackageDescriptor.from(PackageOrg.from(dependency.org()),
                    PackageName.from(dependency.name()), PackageVersion.from(dependency.version()));
            dependencies.add(pkg);
        }
        return dependencies;
    }

    private PackageResolutionRequest toPackageResolutionRequest(ResolutionRequest resolutionRequest) {
        PackageResolutionRequest packageResolutionRequest = new PackageResolutionRequest();
        PackageResolutionRequest.Mode mode = switch (resolutionRequest.packageLockingMode()) {
            case HARD, LOCKED -> PackageResolutionRequest.Mode.HARD;
            case MEDIUM -> PackageResolutionRequest.Mode.MEDIUM;
            case SOFT, LATEST -> PackageResolutionRequest.Mode.SOFT;
            default -> throw new IllegalStateException("Unexpected value: " + resolutionRequest.packageLockingMode());
        };
        String version = resolutionRequest.version().map(v -> v.value().toString()).orElse("");
        packageResolutionRequest.addPackage(resolutionRequest.orgName().value(),
                resolutionRequest.packageName().value(),
                version,
                mode);
        return packageResolutionRequest;
    }
}
