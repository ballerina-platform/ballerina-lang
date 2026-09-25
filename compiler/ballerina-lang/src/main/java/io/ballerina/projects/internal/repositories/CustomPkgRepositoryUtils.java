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
import io.ballerina.projects.Package;
import io.ballerina.projects.PackageDescriptor;
import io.ballerina.projects.PackageName;
import io.ballerina.projects.PackageOrg;
import io.ballerina.projects.PackageVersion;
import io.ballerina.projects.SemanticVersion;
import io.ballerina.projects.environment.PackageLockingMode;
import io.ballerina.projects.environment.PackageMetadataResponse;
import io.ballerina.projects.environment.ResolutionOptions;
import io.ballerina.projects.environment.ResolutionRequest;
import io.ballerina.projects.environment.ResolutionResponse;
import io.ballerina.projects.internal.ImportModuleRequest;
import io.ballerina.projects.internal.ImportModuleResponse;
import io.ballerina.projects.util.ProjectUtils;
import org.ballerinalang.oci.OciClientUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.ballerina.projects.util.ProjectConstants.BALA_EXTENSION;
import static io.ballerina.projects.util.ProjectUtils.getLatest;

/**
 * Resolution logic shared by custom package repositories (Maven, OCI, ...) that front a remote
 * registry with a local {@link FileSystemRepository} cache.
 *
 * @since 2201.14.0
 */
final class CustomPkgRepositoryUtils {

    private static final String PLATFORM = "platform";
    private static final String PACKAGE_JSON = "package.json";

    private CustomPkgRepositoryUtils() {
    }

    /**
     * Returns the versions of the requested package that are compatible with the request, combining what the
     * local cache holds with what the remote registry lists.
     *
     * @param request        resolution request
     * @param options        resolution options; the remote registry is skipped when offline
     * @param fileSystemRepo local cache of the repository
     * @param remoteLookup   lists the versions in the remote registry; a failed lookup yields an empty list so
     *                       resolution falls back to the cache
     * @return compatible versions
     */
    static List<PackageVersion> getPackageVersions(ResolutionRequest request, ResolutionOptions options,
                                                   FileSystemRepository fileSystemRepo,
                                                   Supplier<List<String>> remoteLookup) {
        Set<PackageVersion> packageVersions = new HashSet<>(fileSystemRepo.getPackageVersions(request, options));
        if (!options.offline()) {
            remoteLookup.get().stream().map(PackageVersion::from).forEach(packageVersions::add);
        }
        return getCompatibleVersions(packageVersions, request, options);
    }

    /**
     * Resolves each import to the package that provides it, taking what the local cache resolves and going to
     * the remote registry only for imports the cache could not resolve.
     *
     * @param requests       import module requests
     * @param options        resolution options; the remote registry is skipped when offline
     * @param fileSystemRepo local cache of the repository
     * @param remoteLookup   lists the versions of an {@code (org, package)} in the remote registry; a failed
     *                       lookup yields an empty list so the next possible package name is tried
     * @return one response per request
     */
    static List<ImportModuleResponse> getPackageNames(Collection<ImportModuleRequest> requests,
                                                      ResolutionOptions options,
                                                      FileSystemRepository fileSystemRepo,
                                                      BiFunction<String, String, List<String>> remoteLookup) {
        List<ImportModuleResponse> importModuleResponseList = new ArrayList<>(
                fileSystemRepo.getPackageNames(requests, options));
        if (options.offline()) {
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
                List<String> versions = remoteLookup.apply(org.value(), packageName.value());
                if (versions.isEmpty()) {
                    continue;
                }
                PackageVersion latest = findLatest(versions.stream().map(PackageVersion::from).toList());
                PackageDescriptor resolvedDescriptor = PackageDescriptor.from(org, packageName, latest);
                // Replace the cache's unresolved response for this import with the registry's resolution
                importModuleResponseList.removeIf(
                        response -> response.importModuleRequest().equals(importModuleRequest));
                importModuleResponseList.add(new ImportModuleResponse(resolvedDescriptor, importModuleRequest));
                break;
            }
        }
        return importModuleResponseList;
    }

    /**
     * Narrows the given versions down to those within the compatible range of the request's
     * version and locking mode.
     *
     * @param versions candidate versions, local and remote
     * @param request  resolution request
     * @param options  resolution options
     * @return compatible versions
     */
    static List<PackageVersion> getCompatibleVersions(Collection<PackageVersion> versions,
                                                      ResolutionRequest request, ResolutionOptions options) {
        PackageVersion requestedVersion = request.version().orElse(null);
        SemanticVersion minSemVer = requestedVersion == null
                ? null : SemanticVersion.from(requestedVersion.toString());
        List<SemanticVersion> semVers = versions.stream()
                .map(version -> SemanticVersion.from(version.toString())).toList();
        ProjectUtils.CompatibleRange compatibleRange = ProjectUtils.getCompatibleRange(
                minSemVer, options.packageLockingMode());
        List<SemanticVersion> compatibleVersions = ProjectUtils.getVersionsInCompatibleRange(
                minSemVer, semVers, compatibleRange);
        return compatibleVersions.stream().map(PackageVersion::from).collect(Collectors.toList());
    }

    /**
     * Returns the latest of the given versions.
     *
     * @param packageVersions package versions
     * @return the latest version, or {@code null} if there are none
     */
    static PackageVersion findLatest(Collection<PackageVersion> packageVersions) {
        if (packageVersions.isEmpty()) {
            return null;
        }
        PackageVersion latestVersion = packageVersions.iterator().next();
        for (PackageVersion pkgVersion : packageVersions) {
            latestVersion = getLatest(latestVersion, pkgVersion);
        }
        return latestVersion;
    }

    /**
     * Resolves each request to the latest of its available versions.
     *
     * @param requests        resolution requests
     * @param versionLookup   looks up the available versions for a request
     * @param responseFactory builds the resolved response for a request and its latest version
     * @return one response per request, unresolved where no version is available
     */
    static List<PackageMetadataResponse> resolveLatestVersions(
            Collection<ResolutionRequest> requests,
            Function<ResolutionRequest, Collection<PackageVersion>> versionLookup,
            BiFunction<ResolutionRequest, PackageVersion, PackageMetadataResponse> responseFactory) {
        List<PackageMetadataResponse> responses = new ArrayList<>();
        for (ResolutionRequest request : requests) {
            Collection<PackageVersion> packageVersions = versionLookup.apply(request);
            if (packageVersions.isEmpty()) {
                responses.add(PackageMetadataResponse.createUnresolvedResponse(request));
                continue;
            }
            responses.add(responseFactory.apply(request, findLatest(packageVersions)));
        }
        return responses;
    }

    /**
     * What a registry reports about a resolved package version.
     *
     * @param dependencyGraph dependency graph of the version
     * @param deprecated      whether the version is deprecated, or empty when the registry does not report it
     * @param deprecationMsg  deprecation message, empty when there is none
     */
    record RemotePackageInfo(DependencyGraph<PackageDescriptor> dependencyGraph, Optional<Boolean> deprecated,
                             String deprecationMsg) {

        static RemotePackageInfo withUnknownDeprecation(DependencyGraph<PackageDescriptor> dependencyGraph) {
            return new RemotePackageInfo(dependencyGraph, Optional.empty(), "");
        }
    }

    /**
     * Builds the resolved response for a request and the version it resolved to.
     *
     * @param request    resolution request
     * @param latest     version the request resolved to
     * @param infoLookup fetches what the registry reports about that version
     * @return resolved response
     */
    static PackageMetadataResponse createMetadataResponse(
            ResolutionRequest request, PackageVersion latest,
            BiFunction<ResolutionRequest, PackageVersion, RemotePackageInfo> infoLookup) {
        RemotePackageInfo info = infoLookup.apply(request, latest);
        PackageDescriptor resolvedDescriptor = PackageDescriptor.from(
                request.orgName(), request.packageName(), latest, request.repositoryName().orElse(null),
                info.deprecated().orElse(null), info.deprecationMsg());
        return PackageMetadataResponse.from(request, resolvedDescriptor, info.dependencyGraph());
    }

    /**
     * Resolves requests against the local cache first and only goes to the remote registry for
     * requests the cache cannot settle, then merges both results. Used by repositories that proxy
     * Ballerina Central.
     *
     * @param requests       resolution requests
     * @param options        resolution options
     * @param fileSystemRepo local cache of the repository
     * @param remoteResolver resolves the remaining requests against the remote registry
     * @return merged resolution responses
     */
    static Collection<PackageMetadataResponse> resolveWithCache(
            Collection<ResolutionRequest> requests, ResolutionOptions options, FileSystemRepository fileSystemRepo,
            Function<List<ResolutionRequest>, Collection<PackageMetadataResponse>> remoteResolver) {
        if (requests.isEmpty()) {
            return Collections.emptyList();
        }

        Collection<PackageMetadataResponse> cachedPackages = fileSystemRepo.getPackageMetadata(requests, options);
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
                Optional<Package> pkg = fileSystemRepo.getPackage(response.packageLoadRequest(), options);
                if (pkg.isPresent() && Boolean.TRUE.equals(pkg.get().descriptor().getDeprecated())) {
                    deprecatedPackages.add(response);
                }
            }
        }
        if (updatedRequests.isEmpty()) {
            return cachedPackages;
        }
        return mergeResolution(remoteResolver.apply(updatedRequests), cachedPackages, deprecatedPackages,
                fileSystemRepo);
    }

    private static Collection<PackageMetadataResponse> mergeResolution(
            Collection<PackageMetadataResponse> remoteResolution,
            Collection<PackageMetadataResponse> filesystem,
            List<PackageMetadataResponse> deprecatedPackages,
            FileSystemRepository fileSystemRepo) {
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

                            Boolean remoteDeprecated = y.resolvedDescriptor().getDeprecated();
                            if (x.resolvedDescriptor().version().equals(y.resolvedDescriptor().version())) {
                                if (remoteDeprecated != null && deprecatedPackages.contains(x) != remoteDeprecated) {
                                    fileSystemRepo.updateDeprecatedStatusForPackage(y.resolvedDescriptor());
                                }
                                return x;
                            }
                            // The registry resolved a version the cache does not have; prefer the latest.
                            if (remoteDeprecated != null) {
                                fileSystemRepo.updateDeprecatedStatusForPackage(y.resolvedDescriptor());
                            }
                            return y;
                        })).values());
    }

    /**
     * Downloads a package version into a temporary directory and installs it into the local cache.
     *
     * @param org        package organization
     * @param name       package name
     * @param version    package version
     * @param versionDir the {@code <org>/<name>/<version>} directory of the repository's bala cache
     * @param downloader downloads the bala from the remote registry
     * @param <E>        exception the downloader may throw
     * @throws IOException if the bala cannot be extracted or installed
     * @throws E           if the download fails
     */
    static <E extends Exception> void pullIntoCache(String org, String name, String version, Path versionDir,
                                                    BalaDownloader<E> downloader) throws IOException, E {
        Path tmpDownloadDirectory = Files.createTempDirectory("ballerina-" + System.nanoTime());
        try {
            downloader.download(tmpDownloadDirectory);
            Path downloadedVersionDir = tmpDownloadDirectory.resolve(org).resolve(name).resolve(version);
            Path balaPath = downloadedVersionDir.resolve(name + "-" + version + BALA_EXTENSION);
            String platform = extractBalaAndReadPlatform(balaPath, downloadedVersionDir.resolve(PLATFORM));
            OciClientUtils.extractBalaToBalaCache(balaPath, versionDir, platform);
        } finally {
            ProjectUtils.deleteDirectory(tmpDownloadDirectory);
        }
    }

    /**
     * Downloads a bala from a remote registry into a local directory.
     *
     * @param <E> exception the download may throw
     */
    @FunctionalInterface
    interface BalaDownloader<E extends Exception> {
        void download(Path downloadDirectory) throws E;
    }

    /**
     * Extracts a downloaded bala and reads the platform it was built for from its
     * {@code package.json}, which decides the platform directory it is cached under.
     *
     * @param balaPath       downloaded bala file
     * @param extractionPath directory to extract the bala into
     * @return the bala's platform
     * @throws IOException if the bala cannot be extracted or its package.json read
     */
    static String extractBalaAndReadPlatform(Path balaPath, Path extractionPath) throws IOException {
        ProjectUtils.extractBala(balaPath, extractionPath);
        Path packageJsonPath = extractionPath.resolve(PACKAGE_JSON);
        try (BufferedReader bufferedReader = Files.newBufferedReader(packageJsonPath, StandardCharsets.UTF_8)) {
            JsonObject resultObj = new Gson().fromJson(bufferedReader, JsonObject.class);
            return resultObj.get(PLATFORM).getAsString();
        }
    }
}
