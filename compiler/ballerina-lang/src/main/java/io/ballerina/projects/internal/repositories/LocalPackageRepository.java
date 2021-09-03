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
package io.ballerina.projects.internal.repositories;

import io.ballerina.projects.PackageName;
import io.ballerina.projects.PackageOrg;
import io.ballerina.projects.PackageVersion;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.environment.Environment;
import io.ballerina.projects.internal.BalaFiles;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This class represents the local package repository.
 *
 * @since 2.0.0
 */
public class LocalPackageRepository extends FileSystemRepository {
    public LocalPackageRepository(Environment environment, Path cacheDirectory) {
        super(environment, cacheDirectory);
    }

    public LocalPackageRepository(Environment environment, Path cacheDirectory, String distributionVersion) {
        super(environment, cacheDirectory, distributionVersion);
    }

    protected List<PackageVersion> getPackageVersions(PackageOrg org, PackageName name, PackageVersion version) {
        if (version == null) {
            return Collections.emptyList();
        }

        Path balaPath = getPackagePath(org.toString(), name.toString(), version.toString());
        if (Files.exists(balaPath)) {
            return Collections.singletonList(version);
        } else {
            // TODO Do we need a diagnostic HERE
            return Collections.emptyList();
        }
    }

    public Optional<Collection<String>> getModuleNames(PackageOrg org, PackageName name, PackageVersion version) {
        Path balaPackagePath = getPackagePath(org.value(), name.value(), version.toString());
        if (!Files.exists(balaPackagePath)) {
            // TODO proper diagnostics
            throw new ProjectException("Cannot find the given package in your local repository. " +
                    "org:`" + org + "`, name:`" + name + "`, version:`" + version + "`");
        }

        BalaFiles.DependencyGraphResult dependencyGraphResult =
                BalaFiles.createPackageDependencyGraph(balaPackagePath);
        Collection<String> moduleNames = dependencyGraphResult.moduleDependencies().keySet()
                .stream()
                .map(moduleDescriptor -> moduleDescriptor.name().toString())
                .collect(Collectors.toList());
        return Optional.of(moduleNames);
    }
}
