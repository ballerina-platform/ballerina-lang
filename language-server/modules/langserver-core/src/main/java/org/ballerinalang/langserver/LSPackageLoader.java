/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver;

import io.ballerina.projects.Package;
import io.ballerina.projects.PackageDependencyScope;
import io.ballerina.projects.PackageDescriptor;
import io.ballerina.projects.PackageName;
import io.ballerina.projects.PackageOrg;
import io.ballerina.projects.PackageVersion;
import io.ballerina.projects.Project;
import io.ballerina.projects.environment.PackageRepository;
import io.ballerina.projects.environment.ResolutionOptions;
import io.ballerina.projects.environment.ResolutionRequest;
import io.ballerina.projects.internal.environment.BallerinaDistribution;
import io.ballerina.projects.internal.environment.BallerinaUserHome;
import io.ballerina.projects.internal.environment.DefaultEnvironment;
import org.ballerinalang.langserver.commons.DocumentServiceContext;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.wso2.ballerinalang.compiler.util.Names;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Loads the Ballerina builtin core and builtin packages.
 */
public class LSPackageLoader {

    public static final LanguageServerContext.Key<LSPackageLoader> LS_PACKAGE_LOADER_KEY =
            new LanguageServerContext.Key<>();

    private final List<PackageInfo> distRepoPackages;
    private List<PackageInfo> remoteRepoPackages;
    private List<PackageInfo> localRepoPackages;

    public static LSPackageLoader getInstance(LanguageServerContext context) {
        LSPackageLoader lsPackageLoader = context.get(LS_PACKAGE_LOADER_KEY);
        if (lsPackageLoader == null) {
            lsPackageLoader = new LSPackageLoader(context);
        }

        return lsPackageLoader;
    }

    private LSPackageLoader(LanguageServerContext context) {
        distRepoPackages = this.getDistributionRepoPackages();
        context.put(LS_PACKAGE_LOADER_KEY, this);
    }

    /**
     * Get the local repo packages.
     *
     * @return {@link List} of local repo packages
     */
    public List<PackageInfo> getLocalRepoPackages(PackageRepository repository) {
        if (this.localRepoPackages != null) {
            return this.localRepoPackages;
        }
        this.localRepoPackages = getPackagesFromRepository(repository, Collections.emptyList());
        return localRepoPackages;
    }

    /**
     * Get the remote repo packages.
     *
     * @return {@link List} of remote repo packages
     */
    public List<PackageInfo> getRemoteRepoPackages(PackageRepository repository) {
        if (this.remoteRepoPackages != null) {
            return this.remoteRepoPackages;
        }
        this.remoteRepoPackages = getPackagesFromRepository(repository, Collections.emptyList());
        return this.remoteRepoPackages;
    }

    /**
     * Get the distribution repo packages.
     * Here the distRepoPackages does not contain the langlib packages and ballerinai packages
     *
     * @return {@link List} of distribution repo packages
     */
    public List<PackageInfo> getDistributionRepoPackages() {
        if (this.distRepoPackages != null) {
            return this.distRepoPackages;
        }
        DefaultEnvironment environment = new DefaultEnvironment();
        // Creating a Ballerina distribution instance
        BallerinaDistribution ballerinaDistribution = BallerinaDistribution.from(environment);
        PackageRepository packageRepository = ballerinaDistribution.packageRepository();
        List<String> skippedLangLibs = Arrays.asList("lang.annotations", "lang.__internal", "lang.query");
        return Collections.unmodifiableList(getPackagesFromRepository(packageRepository, skippedLangLibs));
    }

    /**
     * Get all visible repository and distribution packages.
     *
     * @return {@link List} packages
     */
    public List<PackageInfo> getAllVisiblePackages(DocumentServiceContext ctx) {
        Map<String, PackageInfo> packagesList = new HashMap<>();
        this.getDistributionRepoPackages().forEach(packageInfo ->
                packagesList.put(packageInfo.packageIdentifier(), packageInfo));

        Optional<Project> project = ctx.workspace().project(ctx.filePath());
        if (project.isEmpty()) {
            return Collections.emptyList();
        }
        BallerinaUserHome ballerinaUserHome = BallerinaUserHome
                .from(project.get().projectEnvironmentContext().environment());
        PackageRepository localRepository = ballerinaUserHome.localPackageRepository();
        PackageRepository remoteRepository = ballerinaUserHome.remotePackageRepository();
        this.getRemoteRepoPackages(remoteRepository).stream()
                .filter(packageInfo -> !packagesList.containsKey(packageInfo.packageIdentifier()))
                .forEach(packageInfo -> packagesList.put(packageInfo.packageIdentifier(), packageInfo));
        this.getLocalRepoPackages(localRepository).stream()
                .filter(packageInfo -> !packagesList.containsKey(packageInfo.packageIdentifier()))
                .forEach(packageInfo -> packagesList.put(packageInfo.packageIdentifier(), packageInfo));
        return new ArrayList<>(packagesList.values());
    }

    private List<PackageInfo> getPackagesFromRepository(PackageRepository repository, List<String> skipList) {
        Map<String, List<String>> packageMap = repository.getPackages();
        List<PackageInfo> packages = new ArrayList<>();
        packageMap.forEach((key, value) -> {
            if (key.equals(Names.BALLERINA_INTERNAL_ORG.getValue())) {
                return;
            }
            value.forEach(nameEntry -> {
                String[] components = nameEntry.split(":");
                if (components.length != 2 || skipList.contains(components[0])) {
                    return;
                }
                String nameComponent = components[0];
                String version = components[1];
                PackageOrg packageOrg = PackageOrg.from(key);
                PackageName packageName = PackageName.from(nameComponent);
                PackageVersion pkgVersion = PackageVersion.from(version);
                PackageDescriptor pkdDesc = PackageDescriptor.from(packageOrg, packageName, pkgVersion);
                ResolutionRequest request = ResolutionRequest.from(pkdDesc, PackageDependencyScope.DEFAULT);

                Optional<Package> repoPackage = repository.getPackage(request,
                        ResolutionOptions.builder().setOffline(true).build());
                repoPackage.ifPresent(pkg -> packages.add(new PackageInfo(pkg)));
            });
        });

        return packages;
    }

    /**
     * A light-weight package information holder.
     */
    public static class PackageInfo {

        private PackageOrg packageOrg;
        private PackageName packageName;
        private PackageVersion packageVersion;
        private Path sourceRoot;

        private String packageIdentifier;

        public PackageInfo(Package pkg) {
            this.packageOrg = pkg.packageOrg();
            this.packageName = pkg.packageName();
            this.packageVersion = pkg.packageVersion();
            this.sourceRoot = pkg.project().sourceRoot();
            this.packageIdentifier = packageOrg.toString() + "/" + packageName.toString();
        }

        public PackageName packageName() {
            return packageName;
        }

        public PackageOrg packageOrg() {
            return packageOrg;
        }

        public PackageVersion packageVersion() {
            return packageVersion;
        }

        public Path sourceRoot() {
            return sourceRoot;
        }

        public String packageIdentifier() {
            return packageIdentifier;
        }
    }
}
