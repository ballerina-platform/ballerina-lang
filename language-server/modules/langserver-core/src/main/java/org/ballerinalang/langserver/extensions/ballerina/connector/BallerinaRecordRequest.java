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
 * Request to get record AST.
 *
 * @since 2.0.0
 */
public class BallerinaRecordRequest {

    private String org;
    private String module;
    private String version = "";
    private String name;
    private Boolean beta;

    public BallerinaRecordRequest(String org, String module, String version, String name, Boolean beta) {
        this.org = org;
        this.module = module;
        this.version = version;
        this.name = name;
        this.beta = beta;
    }

    public String getOrg() {
        return org;
    }

    public String getModule() {
        return module;
    }

    public String getVersion() {
        return version;
    }

    public String getName() {
        return name;
    }

    public Boolean getBeta() {
        return beta;
    }

    @Override
    public String toString() {
        return "BallerinaConnectorRequest{" +
                "org='" + org + '\'' +
                ", module='" + module + '\'' +
                ", version='" + version + '\'' +
                ", name='" + name + '\'' +
                ", beta=" + beta +
                '}';
    }
}
