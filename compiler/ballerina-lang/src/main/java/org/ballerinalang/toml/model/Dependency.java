/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.toml.model;

import org.ballerinalang.compiler.BLangCompilerException;

/**
 * Dependency definition in Ballerina.toml manifest file.
 */
public class Dependency {
    private String moduleID;
    private DependencyMetadata metadata;
    
    public String getModuleID() {
        return moduleID == null ? null : moduleID.replaceAll("^\"|\"$", "");
    }
    
    public void setModuleID(String moduleID) {
        this.moduleID = moduleID.replaceAll("^\"|\"$", "");
    }
    
    public String getOrgName() {
        String[] moduleIDParts = this.getModuleID().split("/");
        if (moduleIDParts.length == 2) {
            return moduleIDParts[0];
        }
        throw new BLangCompilerException("invalid dependency name. dependency should be in the format " +
                                         "<org-name>/<module-name>.");
    }
    
    public String getModuleName() {
        String[] moduleIDParts = this.getModuleID().split("/");
        if (moduleIDParts.length == 2) {
            return moduleIDParts[1];
        }
        throw new BLangCompilerException("invalid dependency name. dependency should be in the format " +
                                         "<org-name>/<module-name>.");
    }
    
    public DependencyMetadata getMetadata() {
        return metadata;
    }
    
    public void setMetadata(DependencyMetadata metadata) {
        this.metadata = metadata;
    }
}
