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
 * JSON to hold search data for modules.
 */
public class ModuleSearchJson {
    @Expose
    private String id;
    @Expose
    private String description;
    @Expose
    private String orgName;
    @Expose
    private String version;
    @Expose
    private boolean isDefaultModule;

    public ModuleSearchJson(String id, String orgName, String version, String description, boolean isDefaultModule) {
        this.setId(id);
        this.setDescription(description);
        this.setOrgName(orgName);
        this.setVersion(version);
        this.setIsDefaultModule(isDefaultModule);
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

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public boolean getIsDefaultModule() {
        return isDefaultModule;
    }

    public void setIsDefaultModule(boolean isDefaultModule) {
        this.isDefaultModule = isDefaultModule;
    }
}
