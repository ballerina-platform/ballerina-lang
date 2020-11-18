/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.compiler.api.impl;

import io.ballerina.compiler.api.ModuleID;
import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.util.Name;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a module information in ballerina.
 *
 * @since 2.0.0
 */
public class BallerinaModuleID implements ModuleID {

    private static final String ANON_ORG = "$anon";
    private PackageID moduleID;

    public BallerinaModuleID(PackageID moduleID) {
        this.moduleID = moduleID;
    }

    @Override
    public String orgName() {
        return this.moduleID.getOrgName().getValue();
    }

    @Override
    public String moduleName() {
        return this.moduleID.getNameComps().stream().map(Name::getValue).collect(Collectors.joining("."));
    }

    @Override
    public String version() {
        return this.moduleID.getPackageVersion().getValue();
    }

    @Override
    public String modulePrefix() {
        List<Name> nameComps = this.moduleID.getNameComps();
        return nameComps.get(nameComps.size() - 1).getValue();
    }

    @Override
    public String toString() {
        if (ANON_ORG.equals(this.orgName())) {
            return ".";
        }
        return this.orgName() + "/" + this.moduleName() + ":" + this.version();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        BallerinaModuleID target = (BallerinaModuleID) obj;
        return this.moduleID.equals(target.moduleID);
    }

    @Override
    public int hashCode() {
        return this.moduleID.hashCode();
    }
}
