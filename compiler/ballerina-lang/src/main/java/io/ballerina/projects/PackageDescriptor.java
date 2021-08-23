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
package io.ballerina.projects;

import io.ballerina.projects.util.ProjectUtils;

import java.util.Objects;
import java.util.Optional;

/**
 * Uniquely describes a Ballerina package in terms of its name, organization,
 * version and the loaded repository.
 *
 * @since 2.0.0
 */
public class PackageDescriptor {
    private final PackageName packageName;
    private final PackageOrg packageOrg;
    private final PackageVersion packageVersion;
    private final String repository;

    private PackageDescriptor(PackageOrg packageOrg,
                              PackageName packageName,
                              PackageVersion packageVersion,
                              String repository) {
        this.packageName = packageName;
        this.packageOrg = packageOrg;
        this.packageVersion = packageVersion;
        this.repository = repository;
    }

    public static PackageDescriptor from(PackageOrg packageOrg, PackageName packageName) {
        return new PackageDescriptor(packageOrg, packageName, null, null);
    }

    public static PackageDescriptor from(PackageOrg packageOrg, PackageName packageName,
                                         PackageVersion packageVersion) {
        return new PackageDescriptor(packageOrg, packageName, packageVersion, null);
    }

    public static PackageDescriptor from(PackageOrg packageOrg, PackageName packageName,
                                         PackageVersion packageVersion, String repository) {
        return new PackageDescriptor(packageOrg, packageName, packageVersion, repository);
    }

    public PackageName name() {
        return packageName;
    }

    public PackageOrg org() {
        return packageOrg;
    }

    public PackageVersion version() {
        return packageVersion;
    }

    public Optional<String> repository() {
        return Optional.ofNullable(repository);
    }

    public boolean isLangLibPackage() {
        return ProjectUtils.isLangLibPackage(org(), packageName);
    }

    public boolean isBuiltInPackage() {
        return ProjectUtils.isBuiltInPackage(org(), packageName.value());
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        PackageDescriptor that = (PackageDescriptor) other;
        return packageName.equals(that.packageName) &&
                packageOrg.equals(that.packageOrg) &&
                Objects.equals(packageVersion, that.packageVersion) &&
                Objects.equals(repository, that.repository);
    }

    @Override
    public int hashCode() {
        return Objects.hash(packageName, packageOrg, packageVersion);
    }

    @Override
    public String toString() {
        String pkgStr = packageOrg + "/" + packageName;
        if (packageVersion == null) {
            return pkgStr;
        }
        return pkgStr + ":" + packageVersion;
    }
}
