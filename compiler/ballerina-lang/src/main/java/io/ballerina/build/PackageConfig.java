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
package io.ballerina.build;

import java.nio.file.Path;
import java.util.Collection;

/**
 * {@code PackageConfig} contains necessary configuration elements required to
 * create an instance of a {@code Package}.
 *
 * @since 2.0.0
 */
public class PackageConfig {
    // TODO this class represents the Ballerina.toml file

    // This class should contain Specific project-agnostic information
    private final PackageId packageId;
    private final PackageName packageName;
    private final PackageOrg packageOrg;
    private final PackageVersion packageVersion;
    private final Path packagePath;
    // Ballerina toml file config
    private final Collection<ModuleConfig> otherModules;

    private PackageConfig(PackageId packageId,
                          PackageName packageName,
                          PackageOrg packageOrg,
                          PackageVersion packageVersion,
                          Path packagePath,
                          Collection<ModuleConfig> moduleConfigs) {
        this.packageId = packageId;
        this.packageName = packageName;
        this.packageOrg = packageOrg;
        this.packageVersion = packageVersion;
        this.packagePath = packagePath;
        this.otherModules = moduleConfigs;
    }

    public static PackageConfig from(PackageId packageId,
                                     PackageName packageName,
                                     PackageOrg packageOrg,
                                     PackageVersion packageVersion,
                                     Path packagePath,
                                     Collection<ModuleConfig> moduleConfigs) {
        return new PackageConfig(packageId, packageName, packageOrg, packageVersion, packagePath, moduleConfigs);
    }

    public PackageId packageId() {
        return packageId;
    }

    public PackageName packageName() {
        return packageName;
    }

    public PackageOrg packageOrg() {
        return packageOrg;
    }

    public PackageVersion packageVersion() {
        return packageVersion;
    }

    // TODO Check whether it makes sense to expose Java Path in the API
    // TODO Should I use a String here
    public Path packagePath() {
        return packagePath;
    }

    public Collection<ModuleConfig> otherModules() {
        return otherModules;
    }
}
