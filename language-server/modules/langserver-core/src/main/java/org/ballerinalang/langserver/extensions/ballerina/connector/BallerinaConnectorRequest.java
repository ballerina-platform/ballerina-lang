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

package org.ballerinalang.langserver.extensions.ballerina.connector;

/**
 * Request to get connector AST.
 *
 * @since 2.0.0
 */
public class BallerinaConnectorRequest {

    private String id;
    private String orgName;
    private String moduleName;
    private String version = "";
    private String name;

    public BallerinaConnectorRequest(String org, String module, String version, String name) {
        this.orgName = org;
        this.moduleName = module;
        this.version = version;
        this.name = name;
    }

    public BallerinaConnectorRequest(String id, String orgName, String moduleName, String version, String name) {
        this.id = id;
        this.orgName = orgName;
        this.moduleName = moduleName;
        this.version = version;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getOrgName() {
        return orgName;
    }

    public String getModuleName() {
        return moduleName;
    }

    public String getVersion() {
        return version;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "BallerinaConnectorRequest{" +
                "org='" + orgName + '\'' +
                ", module='" + moduleName + '\'' +
                ", version='" + version + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
