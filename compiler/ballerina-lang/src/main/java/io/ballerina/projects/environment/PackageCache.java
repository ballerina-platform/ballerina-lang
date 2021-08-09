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
import io.ballerina.projects.PackageId;
import io.ballerina.projects.PackageName;
import io.ballerina.projects.PackageOrg;
import io.ballerina.projects.PackageVersion;

import java.util.List;
import java.util.Optional;

/**
 * Represents a cache of loaded packages in the {@code Environment}.
 *
 * @since 2.0.0
 */
public interface PackageCache {

    /**
     * Returns the package with the given {@code PackageId}.
     *
     * @param packageId the packageId
     * @return the package with the given {@code PackageId}.
     */
    Optional<Package> getPackage(PackageId packageId);

    /**
     * Returns the package with the given {@code PackageId} or throw an {@code IllegalStateException}.
     *
     * @param packageId the packageId
     * @return the package with the given {@code PackageId} or throw an {@code IllegalStateException.
     */
    Package getPackageOrThrow(PackageId packageId);

    /**
     * Returns the package with the given organization, name and version.
     *
     * @param packageOrg      organization name
     * @param packageName     package name
     * @param version package version
     * @return the package with given organization, name and version
     */
    Optional<Package> getPackage(PackageOrg packageOrg, PackageName packageName, PackageVersion version);

    /**
     * Returns all the package versions with the given org and name.
     *
     * @param packageOrg  organization name
     * @param packageName package name
     * @return all the package versions with the given org and name
     */
    List<Package> getPackages(PackageOrg packageOrg, PackageName packageName);

}
