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

import java.util.Objects;

/**
 * Uniquely describes a Ballerina module in terms of its name and {@code PackageDescriptor}.
 *
 * @since 2.0.0
 */
public class ModuleDescriptor implements Comparable<ModuleDescriptor> {
    private final ModuleName moduleName;
    private final PackageDescriptor packageDesc;

    private final PackageID moduleCompilationId;
    private final PackageID moduleTestCompilationId;

    private ModuleDescriptor(ModuleName moduleName, PackageDescriptor packageDesc) {
        this.moduleName = moduleName;
        this.packageDesc = packageDesc;

        if (packageDesc.name().value().equals(".") && packageDesc.org().anonymous()) {
            moduleCompilationId = PackageID.DEFAULT;
            moduleTestCompilationId = moduleCompilationId;
        } else {
            moduleCompilationId = new PackageID(new Name(packageDesc.org().value()),
                    new Name(packageDesc.name().value()), new Name(moduleName.toString()),
                    new Name(packageDesc.version().toString()), null);
            moduleTestCompilationId = new PackageID(new Name(packageDesc.org().value()),
                    new Name(packageDesc.name().value()), new Name(moduleName.toString()),
                    new Name(packageDesc.version().toString()), null, true);
        }
    }

    public static ModuleDescriptor from(ModuleName moduleName, PackageDescriptor packageDescriptor) {
        return new ModuleDescriptor(moduleName, packageDescriptor);
    }

    public PackageName packageName() {
        return packageDesc.name();
    }

    public PackageOrg org() {
        return packageDesc.org();
    }

    public PackageVersion version() {
        return packageDesc.version();
    }

    public ModuleName name() {
        return moduleName;
    }

    PackageID moduleCompilationId() {
        return moduleCompilationId;
    }

    public PackageID moduleTestCompilationId() {
        return moduleTestCompilationId;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        ModuleDescriptor that = (ModuleDescriptor) other;
        return Objects.equals(moduleName, that.moduleName) &&
                Objects.equals(packageDesc, that.packageDesc);
    }

    @Override
    public int hashCode() {
        return Objects.hash(moduleName, packageDesc);
    }

    @Override
    public int compareTo(ModuleDescriptor other) {
        return this.moduleName.toString().compareTo(other.moduleName.toString());
    }
}
