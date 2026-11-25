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

import io.ballerina.projects.PackageDescriptor;
import io.ballerina.projects.Project;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Fingerprints of the dependencies resolved in one environment.
 * <p>
 * The fingerprint of a package identifies the versions of all its direct and transitive dependencies. Compiled
 * artifacts of a package are only valid for the dependency versions they were compiled against, so they are cached
 * per fingerprint.
 * <p>
 * An environment has one compiler context, which compiles a package version once. The fingerprint first resolved
 * for a package version is therefore the one its artifacts are compiled against, even if another project of the
 * environment resolves it against other dependency versions. Workspace packages are placed in environments so that
 * this does not happen.
 *
 * @since 2201.14.1
 */
public final class DependencyFingerprints {

    private static final int FINGERPRINT_LENGTH = 16;

    // Keyed by org/name/version.
    private final Map<String, String> compiledFingerprints = new HashMap<>();
    // The version and fingerprint of each dependency resolved for a root package, keyed by its source root and then
    // by the org/name of the dependency.
    private final Map<Path, Map<String, String>> resolvedVariants = new HashMap<>();

    /**
     * Forgets the variants recorded for the given root project, before its dependencies are recorded again. Its
     * variants are then exactly those of its current dependency graph, e.g. after an edit removed a dependency.
     *
     * @param rootProject project whose dependencies are resolved again
     */
    public synchronized void clearResolvedVariants(Project rootProject) {
        resolvedVariants.remove(rootKey(rootProject));
    }

    /**
     * Records the fingerprint of a dependency resolved for the given root project.
     *
     * @param rootProject  project whose dependency graph contains the package
     * @param descriptor   resolved package
     * @param dependencies resolved direct and transitive dependencies of the package
     */
    public synchronized void record(Project rootProject, PackageDescriptor descriptor,
                                    Collection<PackageDescriptor> dependencies) {
        String fingerprint = hash(versions(dependencies));
        compiledFingerprints.putIfAbsent(packageKey(descriptor), fingerprint);
        resolvedVariants.computeIfAbsent(rootKey(rootProject), root -> new HashMap<>())
                .put(descriptor.org().value() + "/" + descriptor.name().value(),
                        descriptor.version() + "/" + fingerprint);
    }

    /**
     * Returns the fingerprint the given package version is compiled against in this environment.
     *
     * @param descriptor package
     * @return fingerprint, or empty if the package was not resolved in this environment
     */
    public synchronized Optional<String> fingerprint(PackageDescriptor descriptor) {
        return Optional.ofNullable(compiledFingerprints.get(packageKey(descriptor)));
    }

    /**
     * Returns the variants of the dependencies resolved for the given project: the version and fingerprint of each
     * dependency, keyed by org/name. Packages compiled in one compiler context must agree on the variant of every
     * dependency they share.
     *
     * @param rootProject project
     * @return variants of the project's dependencies, empty if the project was not resolved
     */
    public synchronized Map<String, String> resolvedVariants(Project rootProject) {
        return Map.copyOf(resolvedVariants.getOrDefault(rootKey(rootProject), Map.of()));
    }

    /**
     * Computes the fingerprint of the given dependencies of a package.
     *
     * @param dependencies direct and transitive dependencies of the package
     * @return fingerprint
     */
    public static String of(Collection<PackageDescriptor> dependencies) {
        return hash(versions(dependencies));
    }

    private static String versions(Collection<PackageDescriptor> dependencies) {
        return new HashSet<>(dependencies.stream().map(PackageDescriptor::toString).toList()).stream()
                .sorted()
                .collect(Collectors.joining("\n"));
    }

    private static String hash(String versions) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(versions.getBytes(StandardCharsets.UTF_8));
            StringBuilder fingerprint = new StringBuilder();
            for (int i = 0; i < FINGERPRINT_LENGTH / 2; i++) {
                fingerprint.append(String.format("%02x", digest[i]));
            }
            return fingerprint.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    private static String packageKey(PackageDescriptor descriptor) {
        return descriptor.org().value() + "/" + descriptor.name().value() + "/" + descriptor.version();
    }

    private static Path rootKey(Project rootProject) {
        return rootProject.sourceRoot().toAbsolutePath().normalize();
    }
}
