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

import io.ballerina.projects.DependencyGraph;
import io.ballerina.projects.JvmTarget;
import io.ballerina.projects.PackageDescriptor;
import io.ballerina.projects.PackageName;
import io.ballerina.projects.PackageOrg;
import io.ballerina.projects.PackageVersion;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.environment.Environment;
import io.ballerina.projects.environment.ResolutionRequest;
import io.ballerina.projects.environment.PackageMetadataResponse;
import io.ballerina.projects.internal.BalaFiles;
import io.ballerina.projects.util.ProjectUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
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

    @Override
    public List<PackageMetadataResponse> resolveDependencyVersions(List<ResolutionRequest> packageLoadRequests) {
        List<PackageMetadataResponse> descriptorSet = new ArrayList<>();
        for (ResolutionRequest resolutionRequest : packageLoadRequests) {
            if (resolutionRequest.version().isEmpty()) {
                // TODO proper diagnostic
                throw new ProjectException("The version must be specified for packages in the local repository. " +
                        "org: `" + resolutionRequest.orgName() + "` name: `" + resolutionRequest.packageName() + "`");
            }
            Path balaPath = getPackagePath(resolutionRequest.orgName().toString(),
                    resolutionRequest.packageName().toString(), resolutionRequest.version().get().toString());

            if (!Files.exists(balaPath)) {
                descriptorSet.add(PackageMetadataResponse.createUnresolvedResponse(resolutionRequest));
                continue;
            }

            BalaFiles.DependencyGraphResult packageDependencyGraph = BalaFiles.createPackageDependencyGraph(balaPath);
            DependencyGraph<PackageDescriptor> dependencyGraph = packageDependencyGraph.packageDependencyGraph();
            PackageMetadataResponse responseDescriptor = PackageMetadataResponse
                    .from(resolutionRequest, resolutionRequest.packageDescriptor(), dependencyGraph);
            descriptorSet.add(responseDescriptor);
        }
        return descriptorSet;
    }


    public Optional<Collection<String>> getModuleNames(PackageOrg org, PackageName name, PackageVersion version) {
        Path balaPackagePath = getPackagePath(org.value(), name.value(), version.toString());
        if (!Files.exists(balaPackagePath)) {
            return Optional.empty();
        }

        BalaFiles.DependencyGraphResult dependencyGraphResult =
                BalaFiles.createPackageDependencyGraph(balaPackagePath);
        Collection<String> moduleNames = dependencyGraphResult.moduleDependencies().keySet()
                .stream()
                .map(moduleDescriptor -> moduleDescriptor.name().toString())
                .collect(Collectors.toList());
        return Optional.of(moduleNames);
    }

    // TODO Duplicate. Double check the logic
    private Path getPackagePath(String org, String name, String version) {
        //First we will check for a bala that match any platform
        Path balaPath = this.bala.resolve(
                ProjectUtils.getRelativeBalaPath(org, name, version, null));
        if (!Files.exists(balaPath)) {
            // If bala for any platform not exist check for specific platform
            balaPath = this.bala.resolve(
                    ProjectUtils.getRelativeBalaPath(org, name, version, JvmTarget.JAVA_11.code()));
        }
        return balaPath;
    }
}
