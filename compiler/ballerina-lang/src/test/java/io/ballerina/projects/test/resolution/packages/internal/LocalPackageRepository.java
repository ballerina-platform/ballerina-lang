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
package io.ballerina.projects.test.resolution.packages.internal;

import io.ballerina.projects.DependencyGraph;
import io.ballerina.projects.PackageDescriptor;
import io.ballerina.projects.PackageName;
import io.ballerina.projects.PackageOrg;
import io.ballerina.projects.PackageVersion;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Test implementation of the package repository.
 *
 * @since 2.0.0
 */
public class LocalPackageRepository extends DefaultPackageRepository {
    public LocalPackageRepository(PackageContainer<PackageDescriptor> pkgContainer,
                                  Map<PackageDescriptor, DependencyGraph<PackageDescriptor>> graphMap) {
        super(pkgContainer, graphMap);
    }

    @Override
    protected List<PackageVersion> getPackageVersions(PackageOrg org,
                                                      PackageName name,
                                                      PackageVersion version) {
        return pkgContainer.get(org, name, version)
                .map(Collections::singletonList)
                .orElseGet(Collections::emptyList)
                .stream()
                .map(PackageDescriptor::version)
                .collect(Collectors.toList());
    }
}
