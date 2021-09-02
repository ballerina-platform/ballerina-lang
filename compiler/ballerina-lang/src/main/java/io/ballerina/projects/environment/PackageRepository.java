/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.projects.environment;

import io.ballerina.projects.Package;
import io.ballerina.projects.PackageVersion;
import io.ballerina.projects.internal.ImportModuleRequest;
import io.ballerina.projects.internal.ImportModuleResponse;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * This interface represent a repository of packages.
 *
 * @since 2.0.0
 */
public interface PackageRepository {

    /**
     * Return the package specified in {@code ResolutionRequest}.
     * <p>
     * if the package is not found, this method returns an empty response.
     *
     * @param request package request
     * @param options resolution options
     * @return The loaded package or else an empty container
     */
    Optional<Package> getPackage(ResolutionRequest request, ResolutionOptions options);

    /**
     * Returns the available package versions in the repository.
     *
     * @param request package request
     * @param options resolution options
     * @return the available package versions in the repository
     */
    Collection<PackageVersion> getPackageVersions(ResolutionRequest request, ResolutionOptions options);

    Map<String, List<String>> getPackages();

    /**
     * Returns requested packages metadata such as the availability, dependency graph.
     * <p>
     * Metadata requests provide an efficient way to complete the dependency graph without
     * downloading physical packages from Ballerina central.
     *
     * @param requests requested package collection
     * @param options  resolution options
     * @return a collection of {@code PackageMetadataResponse} instances
     */
    Collection<PackageMetadataResponse> getPackageMetadata(Collection<ResolutionRequest> requests,
                                                           ResolutionOptions options);

    /**
     * Resolve requested import declaration with hierarchical module name to packages.
     *
     * @param requests import declaration collection
     * @param options  resolution options
     * @return a collection of resolved package metadata
     */
    Collection<ImportModuleResponse> getPackageNames(Collection<ImportModuleRequest> requests,
                                                     ResolutionOptions options);
}
