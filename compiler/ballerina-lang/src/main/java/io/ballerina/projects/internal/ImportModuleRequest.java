/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.projects.internal;

import io.ballerina.projects.PackageDescriptor;
import io.ballerina.projects.PackageOrg;
import io.ballerina.projects.environment.ModuleLoadRequest;

import java.util.List;
import java.util.Objects;

/**
 * Represents a imported request.
 *
 * @since 2.0.0
 */
public class ImportModuleRequest {
    private final PackageOrg packageOrg;
    private final String moduleName;
    private final List<PackageDescriptor> possiblePackages;

    private ModuleLoadRequest moduleLoadRequest;

    // TODO remove this constructor
    public ImportModuleRequest(PackageOrg packageOrg, String moduleName) {
        this.packageOrg = packageOrg;
        this.moduleName = moduleName;
        possiblePackages = null;
    }

    public ImportModuleRequest(PackageOrg packageOrg, String moduleName, List<PackageDescriptor> possiblePackages) {
        this.packageOrg = packageOrg;
        this.moduleName = moduleName;
        this.possiblePackages = possiblePackages;
    }

    public ImportModuleRequest(PackageOrg packageOrg, ModuleLoadRequest moduleLoadRequest) {
        this.packageOrg = packageOrg;
        this.moduleName = moduleLoadRequest.moduleName();
        this.moduleLoadRequest = moduleLoadRequest;
        this.possiblePackages = null;
    }

    public ImportModuleRequest(PackageOrg packageOrg,
                               ModuleLoadRequest moduleLoadRequest,
                               List<PackageDescriptor> possiblePackages) {
        this.packageOrg = packageOrg;
        this.moduleName = moduleLoadRequest.moduleName();
        this.moduleLoadRequest = moduleLoadRequest;
        this.possiblePackages = possiblePackages;
    }

    public PackageOrg packageOrg() {
        return packageOrg;
    }

    public String moduleName() {
        return moduleName;
    }

    public ModuleLoadRequest moduleLoadRequest() {
        return moduleLoadRequest;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ImportModuleRequest that = (ImportModuleRequest) o;
        return packageOrg.equals(that.packageOrg) && moduleName.equals(that.moduleName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(packageOrg, moduleName);
    }

    public List<PackageDescriptor> possiblePackages() {
        return possiblePackages;
    }
}
