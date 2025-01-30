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

import java.util.List;

/**
 * Represents a package in the index.
 *
 * @since 2201.12.0
 */
public class IndexPackage {
    private final PackageDescriptor packageDescriptor;
    private final String supportedPlatform;
    private final String ballerinaVersion;
    private final List<PackageDescriptor> dependencies;
    private final List<Module> modules;
    private final boolean isDeprecated;
    private final String deprecationMsg;

    private IndexPackage(
            PackageDescriptor packageDescriptor,
            String supportedPlatform,
            String ballerinaVersion,
            List<PackageDescriptor> dependencies,
            List<Module> modules,
            boolean isDeprecated,
            String deprecationMsg) {
        this.packageDescriptor = packageDescriptor;
        this.supportedPlatform = supportedPlatform;
        this.ballerinaVersion = ballerinaVersion;
        this.dependencies = dependencies;
        this.modules = modules;
        this.isDeprecated = isDeprecated;
        this.deprecationMsg = deprecationMsg;
    }

    static IndexPackage from(
            PackageDescriptor packageDescriptor,
            String supportedPlatform,
            String ballerinaVersion,
            List<PackageDescriptor> dependencies,
            List<Module> modules,
            boolean isDeprecated,
            String deprecationMsg) {
        return new IndexPackage(packageDescriptor, supportedPlatform, ballerinaVersion, dependencies, modules,
                isDeprecated, deprecationMsg);
    }

    static IndexPackage from(
            PackageOrg packageOrg,
            PackageName packageName,
            PackageVersion packageVersion,
            String supportedPlatform,
            String ballerinaVersion,
            List<PackageDescriptor> dependencies,
            List<Module> modules,
            boolean isDeprecated,
            String deprecationMsg) {
        return from(PackageDescriptor.from(packageOrg, packageName, packageVersion),
                supportedPlatform, ballerinaVersion, dependencies, modules, isDeprecated, deprecationMsg);
    }

    public PackageDescriptor packageDescriptor() {
        return packageDescriptor;
    }

    public PackageName name() {
        return packageDescriptor.name();
    }

    public PackageOrg org() {
        return packageDescriptor.org();
    }

    public PackageVersion version() {
        return packageDescriptor.version();
    }

    public List<PackageDescriptor> dependencies() {
        return dependencies;
    }

    public String ballerinaVersion() {
        return ballerinaVersion;
    }

    public String supportedPlatform() {
        return supportedPlatform;
    }

    public List<Module> modules() {
        return modules;
    }

    public static class Module {
        String name;

        public Module(String name) {
            this.name = name;
        }

        public String name() {
            return name;
        }
    }

    public boolean isDeprecated() {
        return isDeprecated;
    }

    public String deprecationMsg() {
        return deprecationMsg;
    }
}
