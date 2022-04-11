/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.trigger.entity;

/**
 * Request to get Trigger details.
 *
 * @since 2.0.0
 */
public class BallerinaTriggerRequest {
    private String id;
    private String orgName;
    private String packageName;
    private String moduleName;
    private String version = "";
    private String name;
    private String targetFile;

    public BallerinaTriggerRequest(String id) {
        this.id = id;
    }

    public BallerinaTriggerRequest(String orgName, String packageName, String moduleName,
                                   String version, String name) {
        this.orgName = orgName;
        this.packageName = packageName;
        this.moduleName = moduleName;
        this.version = version;
        this.name = name;
    }

    public String getTriggerId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTargetFile() {
        return targetFile;
    }

    public void setTargetFile(String targetFile) {
        this.targetFile = targetFile;
    }

    @Override
    public String toString() {
        return "BallerinaTriggerRequest{" +
                "id='" + id + '\'' +
                ", orgName='" + orgName + '\'' +
                ", moduleName='" + moduleName + '\'' +
                ", version='" + version + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
