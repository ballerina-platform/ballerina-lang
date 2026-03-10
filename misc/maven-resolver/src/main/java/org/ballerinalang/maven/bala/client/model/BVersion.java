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

import java.util.ArrayList;
import java.util.List;

/**
 * Data class representing a single Ballerina package version parsed from maven-metadata.xml.
 *
 * @since 2201.8.0
 */
public class BVersion {
    private String number;
    private String platform;
    private String languageSpecificationVersion;
    private boolean isDeprecated;
    private String deprecateMessage;
    private String ballerinaVersion;
    private String balToolId;
    private String graalvmCompatible;
    private List<Module> modules;

    public BVersion() {
        this.modules = new ArrayList<>();
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getLanguageSpecificationVersion() {
        return languageSpecificationVersion;
    }

    public void setLanguageSpecificationVersion(String languageSpecificationVersion) {
        this.languageSpecificationVersion = languageSpecificationVersion;
    }

    public boolean isDeprecated() {
        return isDeprecated;
    }

    public void setIsDeprecated(boolean deprecated) {
        isDeprecated = deprecated;
    }

    public String getDeprecateMessage() {
        return deprecateMessage;
    }

    public void setDeprecateMessage(String deprecateMessage) {
        this.deprecateMessage = deprecateMessage;
    }

    public String getBallerinaVersion() {
        return ballerinaVersion;
    }

    public void setBallerinaVersion(String ballerinaVersion) {
        this.ballerinaVersion = ballerinaVersion;
    }

    public String getBalToolId() {
        return balToolId;
    }

    public void setBalToolId(String balToolId) {
        this.balToolId = balToolId;
    }

    public String getGraalvmCompatible() {
        return graalvmCompatible;
    }

    public void setGraalvmCompatible(String graalvmCompatible) {
        this.graalvmCompatible = graalvmCompatible;
    }

    public List<Module> getModules() {
        return modules;
    }

    public void setModules(List<Module> modules) {
        this.modules = modules;
    }

    @Override
    public String toString() {
        return "BVersion{" +
                "number='" + number + '\'' +
                ", platform='" + platform + '\'' +
                ", languageSpecificationVersion='" + languageSpecificationVersion + '\'' +
                ", isDeprecated=" + isDeprecated +
                ", ballerinaVersion='" + ballerinaVersion + '\'' +
                ", graalvmCompatible='" + graalvmCompatible + '\'' +
                ", modules=" + modules +
                '}';
    }
}
