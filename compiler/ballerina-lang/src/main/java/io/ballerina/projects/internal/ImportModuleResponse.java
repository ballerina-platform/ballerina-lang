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

import io.ballerina.projects.PackageDescriptor;
import io.ballerina.projects.environment.ResolutionResponse;

/**
 * Represents a resolved imported module.
 *
 * @since 2.0.0
 */
public class ImportModuleResponse {

    private final PackageDescriptor packageDescriptor;
    private final ImportModuleRequest importModuleRequest;
    private final ResolutionResponse.ResolutionStatus resolutionStatus;

    public ImportModuleResponse(PackageDescriptor packageDescriptor,
                                ImportModuleRequest importModuleRequest) {
        this.packageDescriptor = packageDescriptor;
        this.importModuleRequest = importModuleRequest;
        this.resolutionStatus = ResolutionResponse.ResolutionStatus.RESOLVED;
    }

    public ImportModuleResponse(ImportModuleRequest importModuleRequest) {
        this.packageDescriptor = null;
        this.importModuleRequest = importModuleRequest;
        this.resolutionStatus = ResolutionResponse.ResolutionStatus.UNRESOLVED;
    }

    public ImportModuleRequest importModuleRequest() {
        return importModuleRequest;
    }

    public PackageDescriptor packageDescriptor() {
        return packageDescriptor;
    }

    public ResolutionResponse.ResolutionStatus resolutionStatus() {
        return resolutionStatus;
    }
}
