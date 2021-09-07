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
    private String id;
    private String orgName;
    private String packageName;
    private String version;
    private String name;
    private String platform;
    private String ballerinaVersion;
    private String displayName;

    public BallerinaConnectorInfo(String id, String orgName, String packageName, String version,
                                  String name, String platform, String ballerinaVersion) {
        this.id = id;
        this.orgName = orgName;
        this.packageName = packageName;
        this.version = version;
        this.name = name;
        this.platform = platform;
        this.ballerinaVersion = ballerinaVersion;
    }

    public BallerinaConnectorInfo(String id, String orgName, String packageName, String version,
                                  String name, String platform, String ballerinaVersion, String displayName) {
        this.id = id;
        this.orgName = orgName;
        this.packageName = packageName;
        this.version = version;
        this.name = name;
        this.platform = platform;
        this.ballerinaVersion = ballerinaVersion;
        this.displayName = displayName;
    }

    public String getId() {
        return id;
    }

    public String getOrgName() {
        return orgName;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getVersion() {
        return version;
    }

    public String getName() {
        return name;
    }

    public String getPlatform() {
        return platform;
    }

    public String getBallerinaVersion() {
        return ballerinaVersion;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return "BallerinaConnectorInfo{" +
                "connectorId='" + id + '\'' +
                ", organization='" + orgName + '\'' +
                ", package='" + packageName + '\'' +
                ", name='" + name + '\'' +
                ", version='" + version + '\'' +
                ", displayName='" + displayName + '\'' +
                ", platform='" + platform + '\'' +
                ", ballerinaVersion=" + ballerinaVersion +
                '}';
    }
}
