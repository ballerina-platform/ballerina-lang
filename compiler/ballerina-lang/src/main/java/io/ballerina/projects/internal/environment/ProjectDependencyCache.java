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
import io.ballerina.projects.ProjectKind;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Project-local compiled dependency artifacts. Bala package resolution remains repository-backed.
 * <p>
 * A cache serves the projects compiled in one compiler context: a single package, or a group of workspace
 * packages that depend on one another. Each project keeps the artifacts in its own target. As the projects of a
 * group share one compiler context, a dependency is compiled once for the group and written to the target of
 * every project that resolved it against the same dependency versions.
 */
public final class ProjectDependencyCache {
    // Records the distribution that produced the cache. Used when Dependencies.toml has no packages yet,
    // e.g. before the first successful build or in a dependency compilation subprocess.
    static final String DISTRIBUTION_VERSION_FILE = ".distribution-version";
    // Records the resolved dependency versions a cached package was compiled against.
    static final String DEPENDENCIES_FILE = ".dependencies";

    private final Map<Path, TargetCache> targets = new LinkedHashMap<>();
    private final boolean writable;
    // The dependency versions a package of the group is compiled against, keyed by org/name/version. The group
    // shares one compiler context, so a package is compiled once, against the versions resolved first.
    private final Map<String, String> compiledFingerprints = new HashMap<>();

    public ProjectDependencyCache(Path targetDir, boolean writable) {
        this(List.of(targetDir), writable);
    }

    public ProjectDependencyCache(Collection<Path> targetDirs, boolean writable) {
        if (targetDirs.isEmpty()) {
            throw new IllegalArgumentException("A project dependency cache requires at least one target");
        }
        for (Path targetDir : targetDirs) {
            targets.put(targetDir.toAbsolutePath().normalize(), new TargetCache(targetDir, writable));
        }
        this.writable = writable;
    }

    public boolean isWritable() {
        return writable;
    }

    /**
     * Returns the target directory that holds the cache of the given project. A cache with a single target
     * serves any project compiled with it, e.g. a dependency compiled in a subprocess on behalf of a project.
     *
     * @param project project compiled with this cache
     * @return target directory of the project's cache
     */
    public Path targetDir(Project project) {
        return target(project).targetDir;
    }

    /**
     * Update cache validity from the distribution version recorded in the project's Dependencies.toml.
     *
     * @param project            project whose dependency manifest is given
     * @param dependencyManifest dependency manifest of the project
     */
    public synchronized void updateDistributionVersion(Project project, DependencyManifest dependencyManifest) {
        targetOf(project).ifPresent(target -> target.updateDistributionVersion(dependencyManifest));
    }

    /**
     * Checks a package's cached artifacts in the given project's target against the dependency versions resolved
     * for that project. When they were compiled against different versions, a writable cache deletes them so the
     * package is compiled from source, and a read-only cache ignores them in favour of the repository cache.
     *
     * @param project               project whose dependency graph contains the package
     * @param packageDescriptor     cached package
     * @param dependencyFingerprint resolved versions of all direct and transitive dependencies of the package
     */
    public synchronized void validatePackage(Project project, PackageDescriptor packageDescriptor,
                                             String dependencyFingerprint) {
        if (usesDistributionCache(packageDescriptor)) {
            return;
        }
        Optional<TargetCache> target = targetOf(project);
        if (target.isEmpty()) {
            return;
        }
        compiledFingerprints.putIfAbsent(packageKey(packageDescriptor), dependencyFingerprint);
        target.get().validatePackage(packageDescriptor, dependencyFingerprint);
        copyFromGroup(target.get(), packageDescriptor, dependencyFingerprint);
    }

    /**
     * Copies a package into the given target from another target of the group that has it cached against the same
     * dependency versions. The packages of a group are compiled one after another in a shared compiler context, so
     * a package compiled for one of them is not compiled again, and would otherwise be missing in the others.
     */
    private void copyFromGroup(TargetCache target, PackageDescriptor packageDescriptor,
                               String dependencyFingerprint) {
        if (!writable || targets.size() < 2 || target.hasCached(packageDescriptor)) {
            return;
        }
        for (TargetCache other : targets.values()) {
            if (other != target && other.hasCached(packageDescriptor, dependencyFingerprint)) {
                target.copyFrom(other, packageDescriptor);
                return;
            }
        }
    }

    public CompilationCache wrap(Project project, CompilationCache repositoryCache) {
        return new ProjectDependencyCompilationCache(this, project, repositoryCache, writable);
    }

    /**
     * Returns the repository cache of a dependency, i.e. the cache in the distribution or a user home repository,
     * whether or not the dependency's compilation cache is wrapped by a project dependency cache.
     *
     * @param compilationCache compilation cache of a dependency
     * @return repository cache of the dependency
     */
    public static CompilationCache repositoryCache(CompilationCache compilationCache) {
        return compilationCache instanceof ProjectDependencyCompilationCache projectCache ?
                projectCache.repositoryCache : compilationCache;
    }

    private TargetCache target(Project project) {
        return targetOf(project).orElseGet(() -> targets.values().iterator().next());
    }

    private Optional<TargetCache> targetOf(Project project) {
        // A single target serves any project, including a bala compiled on behalf of a project, which has no
        // target of its own.
        if (targets.size() == 1) {
            return Optional.of(targets.values().iterator().next());
        }
        if (project.kind() != ProjectKind.BUILD_PROJECT) {
            return Optional.empty();
        }
        return Optional.ofNullable(targets.get(project.targetDir().toAbsolutePath().normalize()));
    }

    /** Targets to read the given package from, in the order of the projects of the group. */
    private synchronized List<TargetCache> readTargets(PackageDescriptor packageDescriptor) {
        String key = packageKey(packageDescriptor);
        List<TargetCache> checked = new ArrayList<>();
        for (TargetCache target : targets.values()) {
            if (target.hasChecked(key)) {
                checked.add(target);
            }
        }
        // A package not checked against any resolution, e.g. one loaded outside a project build, is read as before.
        return checked.isEmpty() ? new ArrayList<>(targets.values()) : checked;
    }

    /** Targets to write the given package to: those that resolved it against the versions it is compiled with. */
    private synchronized List<TargetCache> writeTargets(PackageDescriptor packageDescriptor) {
        String key = packageKey(packageDescriptor);
        String compiledFingerprint = compiledFingerprints.get(key);
        List<TargetCache> matching = new ArrayList<>();
        if (compiledFingerprint != null) {
            for (TargetCache target : targets.values()) {
                if (compiledFingerprint.equals(target.fingerprint(key))) {
                    matching.add(target);
                }
            }
        }
        return matching.isEmpty() ? List.of(targets.values().iterator().next()) : matching;
    }

    private static boolean usesDistributionCache(PackageDescriptor descriptor) {
        return descriptor.isLangLibPackage()
                || (descriptor.org().isBallerinaOrg() && descriptor.name().toString().equals(Names.TEST.getValue()));
    }

    private static String packageKey(PackageDescriptor packageDescriptor) {
        return packageDescriptor.org().value() + "/" + packageDescriptor.name().value() + "/"
                + packageDescriptor.version();
    }

    /** The dependency cache in the target of one project. */
    private static final class TargetCache {
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
        private final Map<Project, FileSystemCache> fileSystemCaches = new HashMap<>();

        private TargetCache(Path targetDir, boolean writable) {
            this.targetDir = targetDir;
            this.cacheDir = targetDir.resolve(ProjectConstants.CACHES_DIR_NAME);
            this.writable = writable;
        }

        private void updateDistributionVersion(DependencyManifest dependencyManifest) {
            if (distributionVersionChecked || dependencyManifest.packages().isEmpty()) {
                return;
            }
            distributionVersionChecked = true;
            SemanticVersion distributionVersion = dependencyManifest.distributionVersion();
            applyValidity(distributionVersion != null
                    && distributionVersion.toString().equals(RepoUtils.getBallerinaShortVersion()));
        }

        private void validatePackage(PackageDescriptor packageDescriptor, String dependencyFingerprint) {
            if (!isCacheValid()) {
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
                throw new IllegalStateException("Failed to clear stale project dependency cache: " + packageCacheDir,
                        e);
            }
            dependencyMarkersWritten.remove(key);
        }

        private boolean hasCached(PackageDescriptor packageDescriptor) {
            return Files.isDirectory(packageCacheDir(packageDescriptor));
        }

        private boolean hasCached(PackageDescriptor packageDescriptor, String dependencyFingerprint) {
            return isCacheValid()
                    && dependencyFingerprint.equals(readDependencyMarker(packageCacheDir(packageDescriptor)));
        }

        private void copyFrom(TargetCache source, PackageDescriptor packageDescriptor) {
            prepareForWrite();
            Path packageCacheDir = packageCacheDir(packageDescriptor);
            try {
                FileUtils.copyDirectory(source.packageCacheDir(packageDescriptor).toFile(), packageCacheDir.toFile());
            } catch (IOException e) {
                throw new IllegalStateException("Failed to copy the project dependency cache to: " + packageCacheDir,
                        e);
            }
            dependencyMarkersWritten.add(packageKey(packageDescriptor));
            stalePackages.remove(packageKey(packageDescriptor));
        }

        private boolean hasChecked(String key) {
            return dependencyFingerprints.containsKey(key);
        }

        private String fingerprint(String key) {
            return dependencyFingerprints.get(key);
        }

        private boolean usable(PackageDescriptor packageDescriptor) {
            return isCacheValid() && !stalePackages.contains(packageKey(packageDescriptor));
        }

        private FileSystemCache fileSystemCache(Project project) {
            return fileSystemCaches.computeIfAbsent(project, p -> new FileSystemCache(p, cacheDir));
        }

        private void writeDependencyMarker(PackageDescriptor packageDescriptor) {
            String key = packageKey(packageDescriptor);
            String dependencyFingerprint = dependencyFingerprints.get(key);
            // Without a fingerprint the artifacts stay unmarked and are recompiled by the next build.
            if (dependencyFingerprint == null || dependencyMarkersWritten.contains(key)) {
                return;
            }
            Path packageCacheDir = packageCacheDir(packageDescriptor);
            Path marker = packageCacheDir.resolve(DEPENDENCIES_FILE);
            try {
                Files.createDirectories(packageCacheDir);
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

        private boolean isCacheValid() {
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

        private void prepareForWrite() {
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
                List<String> fixedCaches = List.of(ProjectConstants.BIR_CACHE_DIR_NAME,
                        ProjectConstants.JAR_CACHE_DIR_NAME, ProjectConstants.JSON_CACHE_DIR_NAME,
                        ProjectConstants.TESTS_CACHE_DIR_NAME, ProjectConstants.BALA_CACHE_DIR_NAME);
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
    }

    private static final class ProjectDependencyCompilationCache extends CompilationCache {
        private final ProjectDependencyCache cache;
        private final CompilationCache repositoryCache;
        private final boolean writable;
        private final Project project;

        private ProjectDependencyCompilationCache(ProjectDependencyCache cache, Project project,
                                                  CompilationCache repositoryCache, boolean writable) {
            super(project);
            this.cache = cache;
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
            PackageDescriptor descriptor = project.currentPackage().descriptor();
            synchronized (cache) {
                for (TargetCache target : cache.readTargets(descriptor)) {
                    if (target.usable(descriptor)) {
                        byte[] bir = target.fileSystemCache(project).getBir(moduleName);
                        if (bir.length > 0) {
                            return bir;
                        }
                    }
                }
            }
            // CLI builds must compile from the resolved bala sources on a project-cache miss. A
            // shared repository BIR may have been produced by an incompatible distribution.
            return writable ? new byte[0] : repositoryCache.getBir(moduleName);
        }

        @Override
        public void cacheBir(ModuleName moduleName, ByteArrayOutputStream birContent) {
            if (usesDistributionCache() || !writable) {
                repositoryCache.cacheBir(moduleName, birContent);
                return;
            }
            PackageDescriptor descriptor = project.currentPackage().descriptor();
            synchronized (cache) {
                for (TargetCache target : cache.writeTargets(descriptor)) {
                    target.prepareForWrite();
                    target.fileSystemCache(project).cacheBir(moduleName, birContent);
                    target.writeDependencyMarker(descriptor);
                }
            }
        }

        @Override
        public Optional<Path> getPlatformSpecificLibrary(CompilerBackend backend, String libraryName) {
            if (usesDistributionCache()) {
                return repositoryCache.getPlatformSpecificLibrary(backend, libraryName);
            }
            PackageDescriptor descriptor = project.currentPackage().descriptor();
            synchronized (cache) {
                for (TargetCache target : cache.readTargets(descriptor)) {
                    if (target.usable(descriptor)) {
                        Optional<Path> library = target.fileSystemCache(project)
                                .getPlatformSpecificLibrary(backend, libraryName);
                        if (library.isPresent()) {
                            return library;
                        }
                    }
                }
            }
            // Avoid shared compiled libraries in CLI builds as well; missing JARs are generated
            // from source and written to the project cache by the backend.
            return writable ? Optional.empty() : repositoryCache.getPlatformSpecificLibrary(backend, libraryName);
        }

        @Override
        public void cachePlatformSpecificLibrary(CompilerBackend backend, String libraryName,
                                                 ByteArrayOutputStream libraryContent) {
            if (usesDistributionCache() || !writable) {
                repositoryCache.cachePlatformSpecificLibrary(backend, libraryName, libraryContent);
                return;
            }
            PackageDescriptor descriptor = project.currentPackage().descriptor();
            synchronized (cache) {
                for (TargetCache target : cache.writeTargets(descriptor)) {
                    target.prepareForWrite();
                    target.fileSystemCache(project).cachePlatformSpecificLibrary(backend, libraryName,
                            libraryContent);
                    target.writeDependencyMarker(descriptor);
                }
            }
        }
    }
}
