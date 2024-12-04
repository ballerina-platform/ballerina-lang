/*
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.projects.internal.index;

import io.ballerina.projects.PackageDescriptor;
import io.ballerina.projects.PackageName;
import io.ballerina.projects.PackageOrg;
import io.ballerina.projects.PackageVersion;
import io.ballerina.projects.SemanticVersion;

import java.util.List;

public class IndexPackage {
    private final PackageDescriptor descriptor;
    private final SemanticVersion ballerinaVersion;
    private final List<IndexDependency> dependencies;

    // TODO: add other fields as necessary
    private IndexPackage(PackageDescriptor descriptor,
                         SemanticVersion ballerinaVersion,
                         List<IndexDependency> dependencies) {
        this.descriptor = descriptor;
        this.ballerinaVersion = ballerinaVersion;
        this.dependencies = dependencies;
    }

    public static IndexPackage from(
            PackageOrg packageOrg,
            PackageName packageName,
            PackageVersion packageVersion,
            SemanticVersion ballerinaVersion,
            List<IndexDependency> dependencies) {
        return new IndexPackage(
                PackageDescriptor.from(packageOrg, packageName, packageVersion),
                ballerinaVersion,
                dependencies);
    }

    public static IndexPackage from(
            PackageDescriptor descriptor,
            SemanticVersion ballerinaVersion,
            List<IndexDependency> dependencies) {
        return new IndexPackage(descriptor, ballerinaVersion, dependencies);
    }

    public PackageName name() {
        return descriptor.name();
    }

    public PackageOrg org() {
        return descriptor.org();
    }

    public PackageVersion version() {
        return descriptor.version();
    }

    public PackageDescriptor descriptor() {
        return descriptor;
    }

    public List<IndexDependency> dependencies() {
        return dependencies;
    }

    public SemanticVersion ballerinaVersion() {
        return ballerinaVersion;
    }
}
