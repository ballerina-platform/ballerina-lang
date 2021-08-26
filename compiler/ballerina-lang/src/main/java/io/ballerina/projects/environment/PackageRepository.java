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

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * This interface represent a repository of packages.
 *
 * @since 2.0.0
 */
public interface PackageRepository {

    Optional<Package> getPackage(ResolutionRequest resolutionRequest);

    List<PackageVersion> getPackageVersions(ResolutionRequest resolutionRequest);

    Map<String, List<String>> getPackages();

    List<PackageMetadataResponse> resolvePackageMetadata(List<ResolutionRequest> resolutionRequests);

    List<ImportModuleResponse> resolvePackageNames(List<ImportModuleRequest> importModuleRequests);
}
