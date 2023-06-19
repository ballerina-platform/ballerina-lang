/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.docgen.generator.model.search;

import com.google.gson.annotations.Expose;

/**
 * JSON to hold search data for constructs.
 */
public class ConstructSearchJson {
    @Expose
    private String id;
    @Expose
    private String description;
    @Expose
    private String moduleId;
    @Expose
    private String moduleOrgName;
    @Expose
    private String moduleVersion;

    public ConstructSearchJson(String id, String moduleId, String moduleOrgName,
                               String moduleVersion, String description) {
        this.setId(id);
        this.setDescription(description);
        this.setModuleId(moduleId);
        this.setModuleOrgName(moduleOrgName);
        this.setModuleVersion(moduleVersion);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }

    public String getModuleOrgName() {
        return moduleOrgName;
    }

    public void setModuleOrgName(String moduleOrgName) {
        this.moduleOrgName = moduleOrgName;
    }

    public String getModuleVersion() {
        return moduleVersion;
    }

    public void setModuleVersion(String moduleVersion) {
        this.moduleVersion = moduleVersion;
    }
}
