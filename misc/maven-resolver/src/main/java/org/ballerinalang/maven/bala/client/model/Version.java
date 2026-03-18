/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.maven.bala.client.model;

/**
 * Data class representing a single Ballerina package version parsed from maven-metadata.xml.
 *
 * @since 2201.13.2
 */
public class Version {
    private String version;
    private String platform;
    private boolean isDeprecated;
    private String ballerinaVersion;

    public Version() {
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public boolean isDeprecated() {
        return isDeprecated;
    }

    public void setIsDeprecated(boolean deprecated) {
        isDeprecated = deprecated;
    }

    public String getBallerinaVersion() {
        return ballerinaVersion;
    }

    public void setBallerinaVersion(String ballerinaVersion) {
        this.ballerinaVersion = ballerinaVersion;
    }

    @Override
    public String toString() {
        return "Version{" +
                "version='" + version + '\'' +
                ", platform='" + platform + '\'' +
                ", isDeprecated=" + isDeprecated +
                ", ballerinaVersion='" + ballerinaVersion + '\'' +
                '}';
    }
}
