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

package io.ballerina.projects.importresolver;

import java.util.List;

/**
 * The {@code SyntaxTree} represents a model for import in a ballerina source.
 *
 * @since 2.0.0
 */
public class Import {

    private String orgName;
    private String moduleName;
    private String version;
    private List<Import> dependencies;

    public Import(String orgName, String moduleName, String version) {
        this.orgName = orgName;
        this.moduleName = moduleName;
        this.version = version;
    }

    @Override
    public String toString() {
        return orgName + "/" + moduleName + ":" + version;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<Import> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<Import> dependencies) {
        this.dependencies = dependencies;
    }
}
