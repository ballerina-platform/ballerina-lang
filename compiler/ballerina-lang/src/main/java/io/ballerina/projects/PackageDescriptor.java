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

import java.util.Collections;
import java.util.List;

/**
 * Represents a Ballerina.toml file.
 *
 * @since 2.0.0
 */
public class PackageDescriptor {
    private final PackageName packageName;
    private final PackageOrg packageOrg;
    private final PackageVersion packageVersion;

    private final List<Dependency> dependencies;
    private final CompilationOptions compilationOptions;

    private PackageDescriptor(PackageName packageName,
                              PackageOrg packageOrg,
                              PackageVersion packageVersion,
                              List<Dependency> dependencies,
                              CompilationOptions compilationOptions) {
        this.packageName = packageName;
        this.packageOrg = packageOrg;
        this.packageVersion = packageVersion;
        this.dependencies = Collections.unmodifiableList(dependencies);
        this.compilationOptions = compilationOptions;
    }

    public static PackageDescriptor from(PackageName packageName,
                                         PackageOrg packageOrg,
                                         PackageVersion packageVersion) {
        return new PackageDescriptor(packageName, packageOrg, packageVersion,
                Collections.emptyList(), new CompilationOptionsBuilder().build());
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

    public List<Dependency> dependencies() {
        return dependencies;
    }

    public CompilationOptions compilationOptions() {
        return compilationOptions;
    }

    /**
     * Represents a dependency of a package.
     *
     * @since 2.0.0
     */
    public static class Dependency {
        private final PackageName packageName;
        private final PackageOrg packageOrg;
        private final PackageVersion semanticVersion;

        public Dependency(PackageName packageName, PackageOrg packageOrg, PackageVersion semanticVersion) {
            this.packageName = packageName;
            this.packageOrg = packageOrg;
            this.semanticVersion = semanticVersion;
        }

        public PackageName name() {
            return packageName;
        }

        public PackageOrg org() {
            return packageOrg;
        }

        public PackageVersion version() {
            return semanticVersion;
        }
    }
}
