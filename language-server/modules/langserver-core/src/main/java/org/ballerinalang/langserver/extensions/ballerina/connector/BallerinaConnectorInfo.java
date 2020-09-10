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
 * Object to contain connector information.
 *
 * @since 2.0.0
 */
public class BallerinaConnectorInfo {
    private String org;
    private String module;
    private String name;
    private String version;
    private String displayName;
    private String logoBase64Encoded;
    private Boolean beta;

    public BallerinaConnectorInfo(String org, String module, String name,
                                  String version, String displayName, Boolean beta) {
        this.org = org;
        this.module = module;
        this.name = name;
        this.version = version;
        this.displayName = displayName;
        this.beta = beta;
    }

    public BallerinaConnectorInfo(String org, String module, String name, String version, String displayName,
                                  String logoBase64Encoded, Boolean beta) {
        this.org = org;
        this.module = module;
        this.name = name;
        this.version = version;
        this.displayName = displayName;
        this.logoBase64Encoded = logoBase64Encoded;
        this.beta = beta;
    }

    public String getOrg() {
        return org;
    }

    public String getModule() {
        return module;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getLogoBase64Encoded() {
        return logoBase64Encoded;
    }

    public Boolean getBeta() {
        return beta;
    }

    @Override
    public String toString() {
        return "BallerinaConnectorInfo{" +
                "organization='" + org + '\'' +
                ", module='" + module + '\'' +
                ", name='" + name + '\'' +
                ", version='" + version + '\'' +
                ", displayName='" + displayName + '\'' +
                ", logoBase64Encoded='" + logoBase64Encoded + '\'' +
                ", beta=" + beta +
                '}';
    }
}
