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
package io.ballerina.projects.environment;

import io.ballerina.projects.DependencyGraph;
import io.ballerina.projects.PackageDescriptor;

import java.util.Optional;

/**
 * {@code PackageMetadataResponse} is used to return a response descriptor to a given {@code ResolutionRequest}.
 *
 * @since 2.0.0
 */
public class PackageMetadataResponse {
    private final ResolutionRequest packageLoadRequest;
    private final PackageDescriptor packageDescriptor;
    private final DependencyGraph<PackageDescriptor> dependencyGraph;
    private final ResolutionResponse.ResolutionStatus resolutionStatus;

    private PackageMetadataResponse(ResolutionRequest packageLoadRequest,
                                    PackageDescriptor resolvedDescriptor,
                                    DependencyGraph<PackageDescriptor> dependencyGraph,
                                    ResolutionResponse.ResolutionStatus resolutionStatus) {
        this.packageLoadRequest = packageLoadRequest;
        this.packageDescriptor = resolvedDescriptor;
        this.dependencyGraph = dependencyGraph;
        this.resolutionStatus = resolutionStatus;
    }

    public static PackageMetadataResponse from(ResolutionRequest packageLoadRequest,
                                               PackageDescriptor resolvedDescriptor,
                                               DependencyGraph<PackageDescriptor> dependencyGraph) {
        return new PackageMetadataResponse(
                packageLoadRequest, resolvedDescriptor, dependencyGraph, ResolutionResponse.ResolutionStatus.RESOLVED);
    }

    public static PackageMetadataResponse createUnresolvedResponse(ResolutionRequest packageLoadRequest) {
        return new PackageMetadataResponse(
                packageLoadRequest, null, null, ResolutionResponse.ResolutionStatus.UNRESOLVED);
    }

    public PackageDescriptor resolvedDescriptor() {
        return packageDescriptor;
    }

    public ResolutionRequest packageLoadRequest() {
        return packageLoadRequest;
    }

    public Optional<DependencyGraph<PackageDescriptor>> dependencyGraph() {
        return Optional.ofNullable(dependencyGraph);
    }

    public ResolutionResponse.ResolutionStatus resolutionStatus() {
        return resolutionStatus;
    }
}
