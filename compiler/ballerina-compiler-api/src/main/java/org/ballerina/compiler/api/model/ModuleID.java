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
package org.ballerina.compiler.api.model;

import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.util.Name;

import java.util.stream.Collectors;

/**
 * Represents a module information in ballerina.
 *
 * @since 1.3.0
 */
public class ModuleID {
    
    private static final String ANON_ORG = "$anon";
    
    
    
    private PackageID moduleID;

    public ModuleID(PackageID moduleID) {
        this.moduleID = moduleID;
    }

    public String getOrgName() {
        return this.moduleID.getOrgName().getValue();
    }

    public String getVersion() {
        return this.moduleID.getPackageVersion().getValue();
    }

    public String getModulePrefix() {
        return null;
    }
    
    public String getModuleName() {
        return this.moduleID.getNameComps().stream().map(Name::getValue).collect(Collectors.joining("."));
    } 

    public boolean isLangLib() {
        // todo: implement the logic 
        return false;
    }

    @Override
    public String toString() {
        if (ANON_ORG.equals(this.getOrgName())) {
            return ".";
        }
        return this.getOrgName() + "/" + this.getModuleName() + ":" + this.getVersion();
    }
}
