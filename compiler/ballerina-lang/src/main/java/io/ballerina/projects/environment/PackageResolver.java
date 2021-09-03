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

import io.ballerina.projects.internal.ImportModuleRequest;
import io.ballerina.projects.internal.ImportModuleResponse;

import java.util.Collection;

/**
 * Defines the interface that will be used by the resolution logic to resolve
 * packages from available repositories.
 *
 * @since 2.0.0
 */
public interface PackageResolver {

    /**
     * Resolve requested import declaration with hierarchical module name to packages.
     *
     * @param requests import declaration collection
     * @param options  resolution options
     * @return a collection of resolved package metadata
     */
    Collection<ImportModuleResponse> resolvePackageNames(Collection<ImportModuleRequest> requests,
                                                         ResolutionOptions options);

    /**
     * Loads requested packages metadata such as the availability, dependency graph.
     * <p>
     * Metadata requests provide an efficient way to complete the dependency graph without
     * downloading physical packages from Ballerina central.
     * <p>
     * An implementation of the {@code PackageResolver} should issue separate requests
     * to local, dist and central repositories and aggregate their responses.
     *
     * @param requests requested package collection
     * @param options  resolution options
     * @return a collection of {@code PackageMetadataResponse} instances
     */
    Collection<PackageMetadataResponse> resolvePackageMetadata(Collection<ResolutionRequest> requests,
                                                               ResolutionOptions options);

    /**
     * Loads the packages specified in {@code ResolutionRequest} collection.
     *
     * @param requests package requests
     * @param options  resolution options
     * @return a collection of loaded packages
     */
    Collection<ResolutionResponse> resolvePackages(Collection<ResolutionRequest> requests,
                                                   ResolutionOptions options);
}
