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
import io.ballerina.projects.PackageManifest;
import io.ballerina.projects.PackageName;
import io.ballerina.projects.PackageOrg;
import io.ballerina.projects.PackageVersion;
import io.ballerina.projects.SemanticVersion.VersionCompatibilityResult;
import io.ballerina.projects.internal.repositories.LocalPackageRepository;
import io.ballerina.projects.util.ProjectConstants;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

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

    public static BlendedManifest from(PackageManifest packageManifest,
                                       DependencyManifest dependencyManifest,
                                       LocalPackageRepository localPackageRepository) {
        PackageContainer<Dependency> depContainer = new PackageContainer<>();
        for (DependencyManifest.Package pkgInDepManifest : dependencyManifest.packages()) {
            depContainer.add(pkgInDepManifest.org(), pkgInDepManifest.name(),
                    new Dependency(pkgInDepManifest.org(),
                            pkgInDepManifest.name(), pkgInDepManifest.version(),
                            getRelation(pkgInDepManifest.isTransitive()),
                            Repository.NOT_SPECIFIED, moduleNames(pkgInDepManifest)));
        }

        for (PackageManifest.Dependency depInPkgManifest : packageManifest.dependencies()) {
            Optional<Dependency> existingDepOptional = depContainer.get(
                    depInPkgManifest.org(), depInPkgManifest.name());
            if (existingDepOptional.isEmpty()) {
                depContainer.add(depInPkgManifest.org(), depInPkgManifest.name(),
                        new Dependency(depInPkgManifest.org(),
                                depInPkgManifest.name(), depInPkgManifest.version(), DependencyRelation.UNKNOWN,
                                Repository.LOCAL, moduleNames(depInPkgManifest, localPackageRepository)));

            } else {
                Dependency existingDep = existingDepOptional.get();
                VersionCompatibilityResult compatibilityResult =
                        depInPkgManifest.version().compareTo(existingDep.version());
                if (compatibilityResult == VersionCompatibilityResult.EQUAL ||
                        compatibilityResult == VersionCompatibilityResult.GREATER_THAN) {
                    depContainer.add(depInPkgManifest.org(), depInPkgManifest.name(),
                            new Dependency(depInPkgManifest.org(),
                                    depInPkgManifest.name(), depInPkgManifest.version(), DependencyRelation.UNKNOWN,
                                    Repository.LOCAL, moduleNames(depInPkgManifest, localPackageRepository)));
                }
                // TODO Report a diagnostic else if (compatibilityResult == VersionCompatibilityResult.INCOMPATIBLE)
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
                                                  LocalPackageRepository localPackageRepository) {
        Optional<Collection<String>> optionalModuleNameList = localPackageRepository.getModuleNames(
                dependency.org(), dependency.name(), dependency.version());
        return optionalModuleNameList.orElseThrow(
                () -> new IllegalStateException("Package in local repository with no modules"));
    }


    public Optional<Dependency> dependency(PackageOrg org, PackageName name) {
        return depContainer.get(org, name);
    }

    public Dependency dependencyOrThrow(PackageOrg org, PackageName name) {
        return depContainer.get(org, name).orElseThrow(() -> new IllegalStateException("Dependency with org `" +
                org + "` and name `" + name + "` must exists."));
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


        private Dependency(PackageOrg org,
                           PackageName name,
                           PackageVersion version,
                           DependencyRelation relation,
                           Repository repository,
                           Collection<String> modules) {
            this.org = org;
            this.name = name;
            this.version = version;
            this.repository = repository;
            this.relation = relation;
            this.modules = modules;
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

        public String repositoryName() {
            return isFromLocalRepository() ? ProjectConstants.LOCAL_REPOSITORY_NAME : null;
        }

        public DependencyRelation relation() {
            return relation;
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
     * Specifies the repository kind.
     */
    private enum Repository {
        LOCAL,
        NOT_SPECIFIED,
    }
}
