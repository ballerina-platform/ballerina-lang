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
    private final PackageOrg packageOrg;
    private final PackageName packageName;
    private final PackageVersion packageVersion;
    private final String repository;
    private final String supportedPlatform;
    private final SemanticVersion ballerinaVersion;
    private final List<IndexDependency> dependencies;
    private final List<Module> modules;

    // TODO: add other fields as necessary
    private IndexPackage(
            PackageOrg packageOrg,
            PackageName packageName,
            PackageVersion packageVersion,
            String repository,
            String supportedPlatform,
            SemanticVersion ballerinaVersion,
            List<IndexDependency> dependencies,
            List<Module> modules) {
        this.packageOrg = packageOrg;
        this.packageName = packageName;
        this.packageVersion = packageVersion;
        this.repository = repository;
        this.supportedPlatform = supportedPlatform;
        this.ballerinaVersion = ballerinaVersion;
        this.dependencies = dependencies;
        this.modules = modules;
    }

    // TODO: If we pass all data as-it-is, we don't need this from method
    public static IndexPackage from(
            PackageOrg packageOrg,
            PackageName packageName,
            PackageVersion packageVersion,
            String repository,
            String supportedPlatform,
            SemanticVersion ballerinaVersion,
            List<IndexDependency> dependencies,
            List<Module> modules) {
        return new IndexPackage(packageOrg, packageName, packageVersion, repository, supportedPlatform, ballerinaVersion, dependencies, modules);
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

    public String repository() {
        return repository;
    }

    public List<IndexDependency> dependencies() {
        return dependencies;
    }

    public SemanticVersion ballerinaVersion() {
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
}
