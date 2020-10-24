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

import io.ballerina.projects.PackageName;
import io.ballerina.projects.PackageOrg;
import io.ballerina.projects.SemanticVersion;

import java.util.Objects;
import java.util.Optional;

/**
 * {@code PackageLoadRequest} is ised to load package using repository.
 *
 * @since 2.0.0
 */
public class PackageLoadRequest {
    private final PackageOrg orgName;
    private final PackageName packageName;
    private final SemanticVersion version;

    public PackageLoadRequest(PackageOrg orgName, PackageName packageName, SemanticVersion version) {
        if (orgName != null && orgName.value().isEmpty()) {
            throw new IllegalArgumentException("The orgName cannot be an empty string. " +
                    "It should be either null or a non-empty string value");
        }
        this.orgName = orgName;
        this.packageName = packageName;
        this.version = version;
    }

    public static PackageLoadRequest from(ModuleLoadRequest moduleLoadRequest) {
        return new PackageLoadRequest(moduleLoadRequest.orgName().orElse(null),
                moduleLoadRequest.packageName(),
                moduleLoadRequest.version().orElse(null));
    }

    public Optional<PackageOrg> orgName() {
        return Optional.of(orgName);
    }

    public PackageName packageName() {
        return packageName;
    }

    public Optional<SemanticVersion> version() {
        return Optional.ofNullable(version);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PackageLoadRequest that = (PackageLoadRequest) o;
        return Objects.equals(orgName, that.orgName) &&
                packageName.equals(that.packageName) &&
                Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orgName, packageName, version);
    }
}
