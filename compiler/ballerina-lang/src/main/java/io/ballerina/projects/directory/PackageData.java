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
package io.ballerina.projects.directory;

import java.nio.file.Path;
import java.util.List;

/**
 * {@code PackageFileData} represents a Ballerina package directory.
 *
 * @since 2.0.0
 */
public class PackageData {
    private final Path packagePath;
    // Ballerina toml file config
    private final ModuleData defaultModule;
    private final List<ModuleData> otherModules;

    private PackageData(Path packagePath,
                        ModuleData defaultModule,
                        List<ModuleData> otherModules) {
        this.packagePath = packagePath;
        this.defaultModule = defaultModule;
        this.otherModules = otherModules;
    }

    public static PackageData from(Path packagePath,
                                   ModuleData defaultModule,
                                   List<ModuleData> otherModules) {
        return new PackageData(packagePath, defaultModule, otherModules);
    }

    public Path packagePath() {
        return packagePath;
    }

    public ModuleData defaultModule() {
        return defaultModule;
    }

    public List<ModuleData> otherModules() {
        return otherModules;
    }
}
