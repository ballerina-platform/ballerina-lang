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
package io.ballerina.projects.internal.environment;

import io.ballerina.projects.CompilationCache;
import io.ballerina.projects.CompilerBackend;
import io.ballerina.projects.ModuleName;
import io.ballerina.projects.PackageDescriptor;
import io.ballerina.projects.Project;
import io.ballerina.projects.environment.ProjectEnvironment;
import io.ballerina.projects.repos.FileSystemCache;
import io.ballerina.projects.util.ProjectConstants;
import org.wso2.ballerinalang.compiler.util.Names;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.UUID;

/**
 * Compilation cache of a dependency, keyed by the fingerprint of the dependency versions it is compiled against.
 * <p>
 * The artifacts of a package are kept in {@code <cache>/<org>/<name>/<version>/<fingerprint>}, so a package compiled
 * against other dependency versions is never reused, and the cache can be shared by all projects.
 * <p>
 * A package from the distribution is read from the distribution cache when that was compiled against the same
 * dependency versions, which the distribution records in a fingerprint file. Otherwise it is compiled and cached in
 * the user home like any other package. Lang libs and the test package are always read from the distribution cache.
 *
 * @since 2201.14.1
 */
public final class FingerprintCompilationCache extends CompilationCache {

    /** File in a distribution package cache that records the fingerprint the package is compiled against. */
    public static final String FINGERPRINT_FILE = ".fingerprint";
    /** File in a package cache that lists the dependency versions the package is compiled against. */
    public static final String DEPENDENCIES_FILE = ".dependencies";

    private final Path cacheDir;
    private final Path distributionCacheDir;
    private final FileSystemCache fixedCache;
    private Path dir;
    private boolean recorded;

    /**
     * Creates the cache of a package from a home repository.
     *
     * @param project  project of the package
     * @param cacheDir cache directory of the repository, which holds a directory per package version and fingerprint
     */
    public FingerprintCompilationCache(Project project, Path cacheDir) {
        this(project, cacheDir, null);
    }

    /**
     * Creates the cache of a package from the distribution repository.
     *
     * @param project              project of the package
     * @param cacheDir             cache directory in the user home, for packages compiled against other versions
     * @param distributionCacheDir cache directory of the distribution
     */
    public FingerprintCompilationCache(Project project, Path cacheDir, Path distributionCacheDir) {
        super(project);
        this.cacheDir = cacheDir;
        this.distributionCacheDir = distributionCacheDir;
        this.fixedCache = new FileSystemCache(project, distributionCacheDir != null ? distributionCacheDir : cacheDir);
    }

    /**
     * Returns the directory that holds the artifacts of this package for the dependency versions it is compiled
     * against, if they are known.
     *
     * @return variant directory in the user home cache
     */
    public Optional<Path> variantDir() {
        if (usesFixedCache()) {
            return Optional.empty();
        }
        return fingerprint().map(fingerprint -> variantDir(descriptor(), fingerprint));
    }

    @Override
    public byte[] getBir(ModuleName moduleName) {
        if (usesFixedCache()) {
            return fixedCache.getBir(moduleName);
        }
        Optional<Path> dir = readDir();
        if (dir.isEmpty()) {
            return new byte[0];
        }
        Path birFile = birDir(dir.get()).resolve(moduleName + ProjectConstants.BLANG_COMPILED_PKG_BIR_EXT);
        try {
            return Files.isRegularFile(birFile) ? Files.readAllBytes(birFile) : new byte[0];
        } catch (IOException e) {
            // A cache that cannot be read is a cache miss: the package is compiled from its sources.
            return new byte[0];
        }
    }

    @Override
    public void cacheBir(ModuleName moduleName, ByteArrayOutputStream birContent) {
        if (usesFixedCache()) {
            fixedCache.cacheBir(moduleName, birContent);
            return;
        }
        writeDir().ifPresent(dir -> write(birDir(dir), moduleName + ProjectConstants.BLANG_COMPILED_PKG_BIR_EXT,
                birContent));
    }

    @Override
    public Optional<Path> getPlatformSpecificLibrary(CompilerBackend compilerBackend, String libraryName) {
        if (usesFixedCache()) {
            return fixedCache.getPlatformSpecificLibrary(compilerBackend, libraryName);
        }
        return readDir().map(dir -> platformDir(dir, compilerBackend)
                        .resolve(libraryName + compilerBackend.libraryFileExtension()))
                .filter(Files::isRegularFile);
    }

    @Override
    public void cachePlatformSpecificLibrary(CompilerBackend compilerBackend, String libraryName,
                                             ByteArrayOutputStream libraryContent) {
        if (usesFixedCache()) {
            fixedCache.cachePlatformSpecificLibrary(compilerBackend, libraryName, libraryContent);
            return;
        }
        writeDir().ifPresent(dir -> write(platformDir(dir, compilerBackend),
                libraryName + compilerBackend.libraryFileExtension(), libraryContent));
    }

    /** Directory to read the artifacts from, or empty if the package is not resolved against known versions. */
    private Optional<Path> readDir() {
        return dir();
    }

    /** Directory to write the artifacts to, or empty if the package is not resolved against known versions. */
    private synchronized Optional<Path> writeDir() {
        Optional<Path> dir = dir();
        if (dir.isEmpty() || recorded) {
            return dir;
        }
        // Record what the package is compiled against: the readable dependency versions, and for a distribution
        // package cached while the distribution is built, the fingerprint a build compares with.
        if (distributionCacheDir != null && dir.get().startsWith(distributionCacheDir)) {
            fingerprint().filter(fingerprint -> !fingerprint.equals(readFingerprint(dir.get())))
                    .ifPresent(fingerprint -> writeFingerprint(dir.get(), fingerprint));
        }
        fingerprints().flatMap(fingerprints -> fingerprints.dependencies(descriptor()))
                .ifPresent(dependencies -> writeDependencies(dir.get(), dependencies));
        recorded = true;
        return dir;
    }

    /**
     * Directory of the artifacts of this package for the versions it is compiled against. A distribution package
     * uses the distribution cache if that is compiled against the same versions, or if it has no cache of the
     * package yet, e.g. while the distribution is built. A distribution cache compiled against other versions is
     * never replaced, as an installed distribution may be shared.
     */
    private synchronized Optional<Path> dir() {
        if (dir == null) {
            Optional<String> fingerprint = fingerprint();
            if (fingerprint.isPresent()) {
                dir = resolveDir(fingerprint.get());
            }
        }
        return Optional.ofNullable(dir);
    }

    private Path resolveDir(String fingerprint) {
        if (distributionCacheDir != null) {
            Path distributionDir = packageDir(distributionCacheDir, descriptor());
            if (fingerprint.equals(readFingerprint(distributionDir)) || !Files.exists(distributionDir)) {
                return distributionDir;
            }
        }
        return variantDir(descriptor(), fingerprint);
    }

    private Optional<String> fingerprint() {
        return fingerprints().flatMap(fingerprints -> fingerprints.fingerprint(descriptor()));
    }

    private Optional<DependencyFingerprints> fingerprints() {
        ProjectEnvironment projectEnvironment = project.projectEnvironmentContext();
        if (projectEnvironment == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(projectEnvironment.environment().getService(DependencyFingerprints.class));
    }

    private boolean usesFixedCache() {
        // The compilation cache is created from the Project constructor, before the package is assigned.
        // Defer package classification until the first cache operation.
        if (project.currentPackage() == null) {
            return false;
        }
        return usesDistributionCache(descriptor());
    }

    private PackageDescriptor descriptor() {
        return project.currentPackage().descriptor();
    }

    private Path variantDir(PackageDescriptor descriptor, String fingerprint) {
        return packageDir(cacheDir, descriptor).resolve(fingerprint);
    }

    /**
     * Returns whether the given package is always read from the distribution cache: lang libs and the test package
     * are compiled with the distribution and do not depend on other packages.
     *
     * @param descriptor package
     * @return true if the package is read from the distribution cache only
     */
    public static boolean usesDistributionCache(PackageDescriptor descriptor) {
        return descriptor.isLangLibPackage()
                || (descriptor.org().isBallerinaOrg() && descriptor.name().toString().equals(Names.TEST.getValue()));
    }

    /**
     * Records the fingerprint a distribution package cache is compiled against.
     *
     * @param packageCacheDir cache directory of the package version
     * @param fingerprint     fingerprint of the package's dependency versions
     */
    public static void writeFingerprint(Path packageCacheDir, String fingerprint) {
        try {
            Files.createDirectories(packageCacheDir);
            Files.writeString(packageCacheDir.resolve(FINGERPRINT_FILE), fingerprint);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write the fingerprint of the package cache: "
                    + packageCacheDir, e);
        }
    }

    /**
     * Records the dependency versions a package cache is compiled against, one {@code org/name:version} per line.
     *
     * @param dir          cache directory of the package
     * @param dependencies dependency versions
     */
    public static void writeDependencies(Path dir, String dependencies) {
        try {
            Files.createDirectories(dir);
            Files.writeString(dir.resolve(DEPENDENCIES_FILE), dependencies);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to record the dependency versions of the package cache: " + dir,
                    e);
        }
    }

    private static String readFingerprint(Path packageCacheDir) {
        Path file = packageCacheDir.resolve(FINGERPRINT_FILE);
        try {
            return Files.isRegularFile(file) ? Files.readString(file).trim() : null;
        } catch (IOException e) {
            return null;
        }
    }

    private static Path packageDir(Path cacheDir, PackageDescriptor descriptor) {
        return cacheDir.resolve(descriptor.org().value())
                .resolve(descriptor.name().value())
                .resolve(descriptor.version().toString());
    }

    private static Path birDir(Path dir) {
        return dir.resolve(ProjectConstants.REPO_BIR_CACHE_NAME);
    }

    private static Path platformDir(Path dir, CompilerBackend compilerBackend) {
        return dir.resolve(compilerBackend.targetPlatform().code());
    }

    /** Writes a file through a temporary file, so that a concurrent build never reads a partial file. */
    private static void write(Path dir, String fileName, ByteArrayOutputStream content) {
        Path file = dir.resolve(fileName);
        Path tempFile = dir.resolve(fileName + "." + UUID.randomUUID() + ".tmp");
        try {
            Files.createDirectories(dir);
            Files.write(tempFile, content.toByteArray());
            Files.move(tempFile, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            try {
                Files.deleteIfExists(tempFile);
            } catch (IOException ignored) {
                // The temporary file is removed when the cache is cleaned.
            }
            throw new IllegalStateException("Failed to cache the compiled artifact: " + file, e);
        }
    }
}
