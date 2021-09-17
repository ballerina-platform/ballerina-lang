/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.projects.internal;

import io.ballerina.projects.DependencyManifest;
import io.ballerina.projects.ModuleDescriptor;
import io.ballerina.projects.PackageManifest;
import io.ballerina.projects.PackageName;
import io.ballerina.projects.PackageOrg;
import io.ballerina.projects.PackageVersion;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.SemanticVersion.VersionCompatibilityResult;
import io.ballerina.projects.internal.repositories.AbstractPackageRepository;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.projects.util.ProjectUtils;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.ballerina.projects.PackageVersion.BUILTIN_PACKAGE_VERSION;

/**
 * Blends dependencies in Dependencies.toml with dependencies specified
 * in Ballerina.toml into a single representation.
 *
 * @since 2.0.0
 */
public class BlendedManifest {
    private final PackageContainer<Dependency> depContainer;

    private BlendedManifest(PackageContainer<Dependency> pkgContainer) {
        this.depContainer = pkgContainer;
    }

    public static BlendedManifest from(DependencyManifest dependencyManifest,
                                       PackageManifest packageManifest,
                                       AbstractPackageRepository localPackageRepository) {
        PackageContainer<Dependency> depContainer = new PackageContainer<>();
        for (DependencyManifest.Package pkgInDepManifest : dependencyManifest.packages()) {
            PackageOrg pkgOrg = pkgInDepManifest.org();
            PackageName pkgName = pkgInDepManifest.name();
            PackageVersion pkgVersion = ProjectUtils.isBuiltInPackage(pkgOrg, pkgName.toString()) ?
                    BUILTIN_PACKAGE_VERSION : pkgInDepManifest.version();
            depContainer.add(pkgOrg, pkgName, new Dependency(pkgOrg, pkgName, pkgVersion,
                    getRelation(pkgInDepManifest.isTransitive()),
                    Repository.NOT_SPECIFIED, moduleNames(pkgInDepManifest), DependencyOrigin.LOCKED));
        }

        for (PackageManifest.Dependency depInPkgManifest : packageManifest.dependencies()) {
            Optional<Dependency> existingDepOptional = depContainer.get(
                    depInPkgManifest.org(), depInPkgManifest.name());
            Repository depInPkgManifestRepo = depInPkgManifest.repository() != null &&
                    depInPkgManifest.repository().equals(ProjectConstants.LOCAL_REPOSITORY_NAME) ?
                    Repository.LOCAL : Repository.NOT_SPECIFIED;

            if (localPackageRepository.isPackageExists(depInPkgManifest.org(), depInPkgManifest.name(),
                                                       depInPkgManifest.version())) {
                if (existingDepOptional.isEmpty()) {
                    depContainer.add(depInPkgManifest.org(), depInPkgManifest.name(),
                            new Dependency(depInPkgManifest.org(),
                                    depInPkgManifest.name(), depInPkgManifest.version(), DependencyRelation.UNKNOWN,
                                    depInPkgManifestRepo, moduleNames(depInPkgManifest, localPackageRepository),
                                    DependencyOrigin.USER_SPECIFIED));
                } else {
                    Dependency existingDep = existingDepOptional.get();
                    VersionCompatibilityResult compatibilityResult =
                            depInPkgManifest.version().compareTo(existingDep.version());
                    if (compatibilityResult == VersionCompatibilityResult.EQUAL ||
                            compatibilityResult == VersionCompatibilityResult.GREATER_THAN) {
                        Dependency newDep = new Dependency(depInPkgManifest.org(), depInPkgManifest.name(),
                                depInPkgManifest.version(), DependencyRelation.UNKNOWN, depInPkgManifestRepo,
                                moduleNames(depInPkgManifest, localPackageRepository), DependencyOrigin.USER_SPECIFIED);
                        depContainer.add(depInPkgManifest.org(), depInPkgManifest.name(), newDep);
                    } else if (compatibilityResult == VersionCompatibilityResult.INCOMPATIBLE) {
                        // TODO update with proper diagnostics
                        // TODO report a diagnostic, skip this version and continue.
                        throw new ProjectException("Dependency version (" + depInPkgManifest.version() + ") " +
                                "specified in Ballerina.toml is incompatible with the " +
                                "dependency version (" + existingDep.version + ") locked in Dependencies.toml. " +
                                "org: `" + existingDep.org() + "` name: " + existingDep.name() + "");
                    }
                }
            }
        }

        return new BlendedManifest(depContainer);
    }

    private static DependencyRelation getRelation(boolean isTransitive) {
        return isTransitive ? DependencyRelation.TRANSITIVE : DependencyRelation.DIRECT;
    }

    private static Collection<String> moduleNames(DependencyManifest.Package dependency) {
        return dependency.modules()
                .stream()
                .map(DependencyManifest.Module::moduleName)
                .collect(Collectors.toList());
    }

    private static Collection<String> moduleNames(PackageManifest.Dependency dependency,
                                                  AbstractPackageRepository localPackageRepository) {
        Collection<ModuleDescriptor> moduleDescriptors = localPackageRepository.getModules(
                dependency.org(), dependency.name(), dependency.version());
        return moduleDescriptors.stream()
                .map(moduleDesc -> moduleDesc.name().toString())
                .collect(Collectors.toList());
    }

    public Optional<Dependency> lockedDependency(PackageOrg org, PackageName name) {
        return dependency(org, name, DependencyOrigin.LOCKED);
    }

    public Collection<Dependency> lockedDependencies() {
        return dependencies(DependencyOrigin.LOCKED);
    }

    public Optional<Dependency> userSpecifiedDependency(PackageOrg org, PackageName name) {
        return dependency(org, name, DependencyOrigin.USER_SPECIFIED);
    }

    public Collection<Dependency> userSpecifiedDependencies() {
        return dependencies(DependencyOrigin.USER_SPECIFIED);
    }

    public Optional<Dependency> dependency(PackageOrg org, PackageName name) {
        return depContainer.get(org, name);
    }

    public Collection<Dependency> dependencies() {
        return depContainer.getAll();
    }

    public Dependency dependencyOrThrow(PackageOrg org, PackageName name) {
        return depContainer.get(org, name).orElseThrow(() -> new IllegalStateException("Dependency with org `" +
                org + "` and name `" + name + "` must exists."));
    }

    private Optional<Dependency> dependency(PackageOrg org, PackageName name, DependencyOrigin origin) {
        return depContainer.get(org, name).filter(dep -> dep.origin == origin);
    }

    private Collection<Dependency> dependencies(DependencyOrigin origin) {
        return depContainer.getAll().stream()
                .filter(dep -> dep.origin == origin)
                .collect(Collectors.toList());
    }

    /**
     * Represents a local dependency package.
     *
     * @since 2.0.0
     */
    public static class Dependency {
        private final PackageOrg org;
        private final PackageName name;
        private final PackageVersion version;
        private final DependencyRelation relation;
        private final Repository repository;
        private final Collection<String> modules;
        private final DependencyOrigin origin;


        private Dependency(PackageOrg org,
                           PackageName name,
                           PackageVersion version,
                           DependencyRelation relation,
                           Repository repository,
                           Collection<String> modules, DependencyOrigin origin) {
            this.org = org;
            this.name = name;
            this.version = version;
            this.repository = repository;
            this.relation = relation;
            this.modules = modules;
            this.origin = origin;
        }

        public PackageName name() {
            return name;
        }

        public PackageOrg org() {
            return org;
        }

        public PackageVersion version() {
            return version;
        }

        public boolean isFromLocalRepository() {
            return repository == Repository.LOCAL;
        }

        public String repository() {
            return isFromLocalRepository() ? ProjectConstants.LOCAL_REPOSITORY_NAME : null;
        }

        public DependencyRelation relation() {
            return relation;
        }

        public DependencyOrigin origin() {
            return origin;
        }

        public Collection<String> moduleNames() {
            return modules;
        }
    }

    /**
     * Specifies the relation between the root package and the dependency.
     *
     * @since 2.0.0
     */
    public enum DependencyRelation {
        DIRECT,
        TRANSITIVE,
        UNKNOWN
    }

    /**
     * Indicates the origin of a dependency.
     */
    public enum DependencyOrigin {
        /**
         * Dependencies specified in Ballerina.toml file.
         */
        USER_SPECIFIED,

        /**
         * Dependencies specified in Dependencies.toml file.
         */
        LOCKED
    }

    /**
     * Specifies the repository kind.
     */
    private enum Repository {
        LOCAL,
        NOT_SPECIFIED,
    }
}
