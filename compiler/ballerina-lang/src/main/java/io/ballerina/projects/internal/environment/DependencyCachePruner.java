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

import io.ballerina.projects.util.ProjectConstants;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Removes compiled dependency variants that no build has used for a period.
 * <p>
 * Variants are shared by all projects, so a variant is not removed when one project stops using it. Builds record
 * when they use a variant, and variants unused for longer than the given period are removed. A removed variant is
 * compiled again by the next build that needs it.
 * <p>
 * Only variant directories, {@code <repositories>/<repo>/cache*}/{@code <org>/<name>/<version>/<fingerprint>}, are
 * removed. Caches of other layouts are left as they are.
 *
 * @since 2201.14.1
 */
public final class DependencyCachePruner {

    /** Period after which a variant that no build used is removed by default. */
    public static final Duration DEFAULT_UNUSED_PERIOD = Duration.ofDays(30);

    private static final String TRASH_PREFIX = ".trash-";
    private static final String TEMP_FILE_SUFFIX = ".tmp";
    private static final Pattern FINGERPRINT = Pattern.compile("[0-9a-f]{16}");
    // Temporary files of a write that did not complete, e.g. of an interrupted build.
    private static final Duration STALE_TEMP_FILE_AGE = Duration.ofDays(1);

    private DependencyCachePruner() {
    }

    /**
     * Result of removing unused variants.
     *
     * @param removedVariants number of variants removed
     * @param freedBytes      size of the removed variants
     */
    public record Result(int removedVariants, long freedBytes) {
    }

    /**
     * Removes variants that no build used for the given period, in all repositories of the given user home.
     *
     * @param userHome   Ballerina user home, e.g. {@code ~/.ballerina}
     * @param unusedFor  period without use after which a variant is removed; zero removes every variant
     * @return number and size of the removed variants
     */
    public static Result prune(Path userHome, Duration unusedFor) {
        Instant threshold = Instant.now().minus(unusedFor);
        int removed = 0;
        long freed = 0;
        for (Path cacheRoot : cacheRoots(userHome)) {
            removeTrash(cacheRoot);
            for (Path variant : variants(cacheRoot)) {
                if (lastUsed(variant).isAfter(threshold) && !unusedFor.isZero()) {
                    removeStaleTempFiles(variant);
                    continue;
                }
                long size = sizeOf(variant);
                if (remove(cacheRoot, variant)) {
                    removed++;
                    freed += size;
                    removeEmptyParents(cacheRoot, variant.getParent());
                }
            }
        }
        return new Result(removed, freed);
    }

    /**
     * Removes the variants compiled against the given package version, in all repositories of the given user home.
     * Used when a version is published to the local repository again with other contents, as variants compiled
     * against the previous contents have the same fingerprint.
     *
     * @param userHome Ballerina user home, e.g. {@code ~/.ballerina}
     * @param org      organization of the package
     * @param name     name of the package
     * @param version  version of the package
     * @return number of variants removed
     */
    public static int removeDependants(Path userHome, String org, String name, String version) {
        String dependency = org + "/" + name + ":" + version;
        int removed = 0;
        for (Path cacheRoot : cacheRoots(userHome)) {
            for (Path variant : variants(cacheRoot)) {
                if (dependsOn(variant, dependency) && remove(cacheRoot, variant)) {
                    removed++;
                    removeEmptyParents(cacheRoot, variant.getParent());
                }
            }
        }
        return removed;
    }

    private static boolean dependsOn(Path variant, String dependency) {
        Path dependencies = variant.resolve(FingerprintCompilationCache.DEPENDENCIES_FILE);
        try {
            return Files.isRegularFile(dependencies)
                    && Files.readAllLines(dependencies).stream().anyMatch(dependency::equals);
        } catch (IOException e) {
            return false;
        }
    }

    /** Cache directories of all repositories, e.g. {@code repositories/central.ballerina.io/cache-2201.14.1}. */
    private static List<Path> cacheRoots(Path userHome) {
        Path repositories = userHome.resolve(ProjectConstants.REPOSITORIES_DIR);
        List<Path> roots = new ArrayList<>();
        for (Path repository : list(repositories)) {
            for (Path dir : list(repository)) {
                Path name = dir.getFileName();
                if (name != null && name.toString().startsWith(ProjectConstants.CACHES_DIR_NAME)
                        && Files.isDirectory(dir)) {
                    roots.add(dir);
                }
            }
        }
        return roots;
    }

    private static List<Path> variants(Path cacheRoot) {
        List<Path> variants = new ArrayList<>();
        for (Path org : list(cacheRoot)) {
            for (Path pkg : list(org)) {
                for (Path version : list(pkg)) {
                    for (Path variant : list(version)) {
                        Path name = variant.getFileName();
                        if (name != null && FINGERPRINT.matcher(name.toString()).matches()
                                && Files.isDirectory(variant)) {
                            variants.add(variant);
                        }
                    }
                }
            }
        }
        return variants;
    }

    private static Instant lastUsed(Path variant) {
        Path lastUsed = variant.resolve(FingerprintCompilationCache.LAST_USED_FILE);
        try {
            return Files.getLastModifiedTime(Files.exists(lastUsed) ? lastUsed : variant).toInstant();
        } catch (IOException e) {
            return Instant.now();
        }
    }

    /**
     * Removes a variant by first moving it out of its version directory, so that a build never finds a variant that
     * is partly removed.
     */
    private static boolean remove(Path cacheRoot, Path variant) {
        Path trash = cacheRoot.resolve(TRASH_PREFIX + UUID.randomUUID());
        try {
            Files.move(variant, trash);
        } catch (IOException e) {
            return false;
        }
        try {
            FileUtils.deleteDirectory(trash.toFile());
        } catch (IOException e) {
            // Removed with the next removal.
        }
        return true;
    }

    /** Removes variants that a previous removal moved out but could not delete. */
    private static void removeTrash(Path cacheRoot) {
        for (Path dir : list(cacheRoot)) {
            Path name = dir.getFileName();
            if (name != null && name.toString().startsWith(TRASH_PREFIX)) {
                try {
                    FileUtils.deleteDirectory(dir.toFile());
                } catch (IOException e) {
                    // Removed with the next removal.
                }
            }
        }
    }

    private static void removeStaleTempFiles(Path variant) {
        Instant threshold = Instant.now().minus(STALE_TEMP_FILE_AGE);
        try (Stream<Path> files = Files.walk(variant)) {
            for (Path file : files.filter(Files::isRegularFile).toList()) {
                Path name = file.getFileName();
                if (name != null && name.toString().endsWith(TEMP_FILE_SUFFIX)
                        && Files.getLastModifiedTime(file).toInstant().isBefore(threshold)) {
                    Files.deleteIfExists(file);
                }
            }
        } catch (IOException e) {
            // Removed with the next removal.
        }
    }

    /** Removes the version, package and org directories that no longer hold any variant. */
    private static void removeEmptyParents(Path cacheRoot, Path dir) {
        Path current = dir;
        while (current != null && !current.equals(cacheRoot) && current.startsWith(cacheRoot)) {
            try {
                Files.delete(current);
            } catch (IOException e) {
                // Not empty, or used concurrently.
                return;
            }
            current = current.getParent();
        }
    }

    private static long sizeOf(Path dir) {
        try (Stream<Path> files = Files.walk(dir)) {
            return files.filter(Files::isRegularFile).mapToLong(file -> {
                try {
                    return Files.size(file);
                } catch (IOException e) {
                    return 0;
                }
            }).sum();
        } catch (IOException e) {
            return 0;
        }
    }

    private static List<Path> list(Path dir) {
        if (!Files.isDirectory(dir)) {
            return List.of();
        }
        try (Stream<Path> children = Files.list(dir)) {
            return children.sorted().toList();
        } catch (IOException e) {
            return List.of();
        }
    }
}
