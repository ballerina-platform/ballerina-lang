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

import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.util.Name;

/**
 * Holds module name and containing package details.
 *
 * @since 2.0.0
 */
public class ModuleDescriptor {
    private final PackageName packageName;
    private final PackageOrg packageOrg;
    private final PackageVersion packageVersion;
    private final ModuleName moduleName;

    private final PackageID moduleCompilationId;

    private ModuleDescriptor(PackageName packageName,
                             PackageOrg packageOrg,
                             PackageVersion packageVersion,
                             ModuleName moduleName) {
        this.packageName = packageName;
        this.packageOrg = packageOrg;
        this.packageVersion = packageVersion;
        this.moduleName = moduleName;

        if (packageName.value().equals(".") && packageOrg.anonymous()) {
            moduleCompilationId = PackageID.DEFAULT;
        } else {
            moduleCompilationId = new PackageID(new Name(packageOrg.value()),
                    new Name(moduleName.toString()), new Name(packageVersion.toString()));
        }
    }

    public static ModuleDescriptor from(PackageName packageName,
                                        PackageOrg packageOrg,
                                        PackageVersion packageVersion,
                                        ModuleName moduleName) {
        return new ModuleDescriptor(packageName, packageOrg, packageVersion, moduleName);
    }

    public PackageName packageName() {
        return packageName;
    }

    public PackageOrg org() {
        return packageOrg;
    }

    public PackageVersion version() {
        return packageVersion;
    }

    public ModuleName name() {
        return moduleName;
    }

    PackageID moduleCompilationId() {
        return moduleCompilationId;
    }
}
