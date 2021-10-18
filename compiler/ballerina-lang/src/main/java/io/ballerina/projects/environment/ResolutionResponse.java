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
import io.ballerina.projects.PackageDescriptor;

/**
 * {@code ResolutionResponse} is used to return a response to a given {@code PackageLoadRequest}.
 *
 * @since 2.0.0
 */
public class ResolutionResponse {
    private final Package resolvedPackage;
    private final ResolutionRequest resolutionRequest;
    private final ResolutionStatus status;
    private final PackageDescriptor requestedPkgDesc;

    // TODO We can use this call to send diagnostics if any
    private ResolutionResponse(ResolutionStatus resolutionStatus,
                               Package resolvedPackage,
                               ResolutionRequest resolutionRequest,
                               PackageDescriptor packageDescriptor) {
        this.resolvedPackage = resolvedPackage;
        this.resolutionRequest = resolutionRequest;
        this.status = resolutionStatus;
        this.requestedPkgDesc = packageDescriptor;
    }

    public static ResolutionResponse from(ResolutionStatus resolutionStatus,
                                          Package resolvedPackage,
                                          ResolutionRequest resolutionRequest) {
        return new ResolutionResponse(resolutionStatus, resolvedPackage, resolutionRequest,
                resolutionRequest.packageDescriptor());
    }

    public ResolutionStatus resolutionStatus() {
        return status;
    }

    public Package resolvedPackage() {
        return resolvedPackage;
    }

    public ResolutionRequest resolutionRequest() {
        return resolutionRequest;
    }

    public PackageDescriptor responseDescriptor() {
        return requestedPkgDesc;
    }

    /**
     * Represents the package resolution status of a {@code ResolutionRequest}.
     *
     * @since 2.0.0
     */
    public enum ResolutionStatus {
        /**
         * Package resolution is successful.
         */
        RESOLVED,
        /**
         * Failed to resolve the specified package.
         */
        UNRESOLVED
    }
}
