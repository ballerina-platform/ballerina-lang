/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
 * All Rights Reserved.
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
package io.ballerina.projects.internal.environment;

import io.ballerina.projects.CompilationCache;
import io.ballerina.projects.CompilerBackend;
import io.ballerina.projects.DependencyManifest;
import io.ballerina.projects.ModuleName;
import io.ballerina.projects.PackageDescriptor;
import io.ballerina.projects.Project;
import io.ballerina.projects.SemanticVersion;
import io.ballerina.projects.repos.FileSystemCache;
import io.ballerina.projects.util.ProjectConstants;
import org.apache.commons.io.FileUtils;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** Project-local compiled dependency artifacts. Bala package resolution remains repository-backed. */
public final class ProjectDependencyCache {
    // Records the distribution that produced the cache. Used when Dependencies.toml has no packages yet,
    // e.g. before the first successful build or in a dependency compilation subprocess.
    static final String DISTRIBUTION_VERSION_FILE = ".distribution-version";
    // Records the resolved dependency versions a cached package was compiled against.
    static final String DEPENDENCIES_FILE = ".dependencies";

    private final Path targetDir;
    private final Path cacheDir;
    private final boolean writable;
    private boolean cacheValidForDistribution;
    private boolean distributionVersionChecked;
    private boolean cacheClearedForWrite;
    private boolean markerWritten;
    // Keyed by org/name/version of the cached package.
    private final Map<String, String> dependencyFingerprints = new HashMap<>();
    private final Set<String> stalePackages = new HashSet<>();
    private final Set<String> dependencyMarkersWritten = new HashSet<>();

    public ProjectDependencyCache(Path targetDir, boolean writable) {
        this.targetDir = targetDir;
        this.cacheDir = targetDir.resolve(ProjectConstants.CACHES_DIR_NAME);
        this.writable = writable;
    }

    public boolean isWritable() {
        return writable;
    }

    /** Returns the project target directory that holds this cache. */
    public Path targetDir() {
        return targetDir;
    }

    /** Update cache validity from the distribution version recorded in Dependencies.toml. */
    public synchronized void updateDistributionVersion(DependencyManifest dependencyManifest) {
        if (distributionVersionChecked || dependencyManifest.packages().isEmpty()) {
            return;
        }
        distributionVersionChecked = true;
        SemanticVersion distributionVersion = dependencyManifest.distributionVersion();
        applyValidity(distributionVersion != null
                && distributionVersion.toString().equals(RepoUtils.getBallerinaShortVersion()));
    }

    /**
     * Checks a package's cached artifacts against the dependency versions resolved for this build.
     * When they were compiled against different versions, a writable cache deletes them so the package is
     * compiled from source, and a read-only cache ignores them in favour of the repository cache.
     *
     * @param packageDescriptor     cached package
     * @param dependencyFingerprint resolved versions of all direct and transitive dependencies of the package
     */
    public synchronized void validatePackage(PackageDescriptor packageDescriptor, String dependencyFingerprint) {
        if (usesDistributionCache(packageDescriptor) || !isCacheValid()) {
            return;
        }
        String key = packageKey(packageDescriptor);
        dependencyFingerprints.put(key, dependencyFingerprint);
        Path packageCacheDir = packageCacheDir(packageDescriptor);
        if (!Files.isDirectory(packageCacheDir)
                || dependencyFingerprint.equals(readDependencyMarker(packageCacheDir))) {
            stalePackages.remove(key);
            return;
        }
        if (!writable) {
            stalePackages.add(key);
            return;
        }
        try {
            FileUtils.deleteDirectory(packageCacheDir.toFile());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to clear stale project dependency cache: " + packageCacheDir, e);
        }
        dependencyMarkersWritten.remove(key);
    }

    private synchronized boolean isPackageStale(PackageDescriptor packageDescriptor) {
        return stalePackages.contains(packageKey(packageDescriptor));
    }

    private synchronized void writeDependencyMarker(PackageDescriptor packageDescriptor) {
        String key = packageKey(packageDescriptor);
        String dependencyFingerprint = dependencyFingerprints.get(key);
        // Without a fingerprint the artifacts stay unmarked and are recompiled by the next build.
        if (dependencyFingerprint == null || dependencyMarkersWritten.contains(key)) {
            return;
        }
        Path marker = packageCacheDir(packageDescriptor).resolve(DEPENDENCIES_FILE);
        try {
            Files.createDirectories(marker.getParent());
            Files.writeString(marker, dependencyFingerprint);
            dependencyMarkersWritten.add(key);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write project dependency cache metadata: " + marker, e);
        }
    }

    private static String readDependencyMarker(Path packageCacheDir) {
        Path marker = packageCacheDir.resolve(DEPENDENCIES_FILE);
        if (!Files.isRegularFile(marker)) {
            return null;
        }
        try {
            return Files.readString(marker);
        } catch (IOException e) {
            return null;
        }
    }

    private Path packageCacheDir(PackageDescriptor packageDescriptor) {
        return cacheDir.resolve(packageDescriptor.org().value())
                .resolve(packageDescriptor.name().value())
                .resolve(packageDescriptor.version().toString());
    }

    private static String packageKey(PackageDescriptor packageDescriptor) {
        return packageDescriptor.org().value() + "/" + packageDescriptor.name().value() + "/"
                + packageDescriptor.version();
    }

    private synchronized boolean isCacheValid() {
        if (!distributionVersionChecked) {
            // No usable Dependencies.toml. Fall back to the marker written alongside the cache.
            distributionVersionChecked = true;
            applyValidity(markerMatchesDistribution());
        }
        return cacheValidForDistribution;
    }

    private void applyValidity(boolean distributionMatches) {
        cacheValidForDistribution = distributionMatches;
        if (!writable) {
            return;
        }
        if (!distributionMatches) {
            clearPackageCaches();
            cacheClearedForWrite = true;
            cacheValidForDistribution = true;
        }
        writeMarker();
    }

    private synchronized void prepareForWrite() {
        if (!isCacheValid() && !cacheClearedForWrite) {
            clearPackageCaches();
            cacheClearedForWrite = true;
            cacheValidForDistribution = true;
        }
        writeMarker();
    }

    private boolean markerMatchesDistribution() {
        Path marker = cacheDir.resolve(DISTRIBUTION_VERSION_FILE);
        if (!Files.isRegularFile(marker)) {
            return false;
        }
        try {
            return Files.readString(marker).trim().equals(RepoUtils.getBallerinaShortVersion());
        } catch (IOException e) {
            return false;
        }
    }

    private void writeMarker() {
        if (markerWritten) {
            return;
        }
        try {
            Files.createDirectories(cacheDir);
            Files.writeString(cacheDir.resolve(DISTRIBUTION_VERSION_FILE), RepoUtils.getBallerinaShortVersion());
            markerWritten = true;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write project dependency cache metadata: " + cacheDir, e);
        }
    }

    private void clearPackageCaches() {
        if (!cacheDir.toFile().exists()) {
            return;
        }
        try {
            // The project package cache shares target/cache with other build caches. Remove only
            // package version directories, preserving fixed-purpose caches and unrelated entries.
            List<String> fixedCaches = List.of(ProjectConstants.BIR_CACHE_DIR_NAME, ProjectConstants.JAR_CACHE_DIR_NAME,
                    ProjectConstants.JSON_CACHE_DIR_NAME, ProjectConstants.TESTS_CACHE_DIR_NAME,
                    ProjectConstants.BALA_CACHE_DIR_NAME);
            try (var children = Files.list(cacheDir)) {
                for (Path child : children.toList()) {
                    Path childName = child.getFileName();
                    if (!Files.isDirectory(child) || childName == null
                            || fixedCaches.contains(childName.toString())) {
                        continue;
                    }
                    try (var packages = Files.list(child)) {
                        for (Path packageDir : packages.toList()) {
                            if (!Files.isDirectory(packageDir)) {
                                continue;
                            }
                            try (var versions = Files.list(packageDir)) {
                                for (Path versionDir : versions.toList()) {
                                    if (Files.isDirectory(versionDir)) {
                                        FileUtils.deleteDirectory(versionDir.toFile());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to clear stale project dependency cache: " + cacheDir, e);
        }
    }

    public CompilationCache wrap(Project project, CompilationCache repositoryCache) {
        return new ProjectDependencyCompilationCache(this, project, cacheDir, repositoryCache, writable);
    }

    private static boolean usesDistributionCache(PackageDescriptor descriptor) {
        return descriptor.isLangLibPackage()
                || (descriptor.org().isBallerinaOrg() && descriptor.name().toString().equals(Names.TEST.getValue()));
    }

    private boolean useProjectCache(Project project) {
        return isCacheValid() && !isPackageStale(project.currentPackage().descriptor());
    }

    private static final class ProjectDependencyCompilationCache extends CompilationCache {
        private final ProjectDependencyCache cache;
        private final FileSystemCache projectCache;
        private final CompilationCache repositoryCache;
        private final boolean writable;
        private final Project project;

        private ProjectDependencyCompilationCache(ProjectDependencyCache cache, Project project, Path cacheDir,
                                                  CompilationCache repositoryCache, boolean writable) {
            super(project);
            this.cache = cache;
            this.projectCache = new FileSystemCache(project, cacheDir);
            this.repositoryCache = repositoryCache;
            this.writable = writable;
            this.project = project;
        }

        private boolean usesDistributionCache() {
            // The compilation cache is created from the Project constructor, before the package
            // is assigned. Defer package classification until the first cache operation.
            return project.currentPackage() != null
                    && ProjectDependencyCache.usesDistributionCache(project.currentPackage().descriptor());
        }

        @Override
        public byte[] getBir(ModuleName moduleName) {
            if (usesDistributionCache()) {
                return repositoryCache.getBir(moduleName);
            }
            byte[] bir = cache.useProjectCache(project) ? projectCache.getBir(moduleName) : new byte[0];
            if (bir.length > 0) {
                return bir;
            }
            // CLI builds must compile from the resolved bala sources on a project-cache miss. A
            // shared repository BIR may have been produced by an incompatible distribution.
            return writable ? new byte[0] : repositoryCache.getBir(moduleName);
        }

        @Override
        public void cacheBir(ModuleName moduleName, ByteArrayOutputStream birContent) {
            if (usesDistributionCache()) {
                repositoryCache.cacheBir(moduleName, birContent);
                return;
            }
            if (writable) {
                cache.prepareForWrite();
                projectCache.cacheBir(moduleName, birContent);
                cache.writeDependencyMarker(project.currentPackage().descriptor());
                return;
            }
            repositoryCache.cacheBir(moduleName, birContent);
        }

        @Override
        public Optional<Path> getPlatformSpecificLibrary(CompilerBackend backend, String libraryName) {
            if (usesDistributionCache()) {
                return repositoryCache.getPlatformSpecificLibrary(backend, libraryName);
            }
            Optional<Path> library = cache.useProjectCache(project) ?
                    projectCache.getPlatformSpecificLibrary(backend, libraryName) : Optional.empty();
            if (library.isPresent()) {
                return library;
            }
            // Avoid shared compiled libraries in CLI builds as well; missing JARs are generated
            // from source and written to the project cache by the backend.
            return writable ? Optional.empty() : repositoryCache.getPlatformSpecificLibrary(backend, libraryName);
        }

        @Override
        public void cachePlatformSpecificLibrary(CompilerBackend backend, String libraryName,
                                                 ByteArrayOutputStream libraryContent) {
            if (usesDistributionCache()) {
                repositoryCache.cachePlatformSpecificLibrary(backend, libraryName, libraryContent);
                return;
            }
            if (writable) {
                cache.prepareForWrite();
                projectCache.cachePlatformSpecificLibrary(backend, libraryName, libraryContent);
                cache.writeDependencyMarker(project.currentPackage().descriptor());
                return;
            }
            repositoryCache.cachePlatformSpecificLibrary(backend, libraryName, libraryContent);
        }
    }
}
