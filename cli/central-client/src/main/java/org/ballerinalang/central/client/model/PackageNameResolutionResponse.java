/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.central.client.model;

import java.util.List;

/**
 * Package Name resolution response model.
 *
 * @since 2.0.0
 */
public class PackageNameResolutionResponse {

    List<Module> resolvedModules;
    List<Module> unresolvedModules;

    public PackageNameResolutionResponse(List<Module> resolvedModules, List<Module> unresolvedModules) {
        this.resolvedModules = resolvedModules;
        this.unresolvedModules = unresolvedModules;
    }

    /**
     * Package Name resolution response Module.
     *
     * @since 2.0.0
     */
    public static class Module {
        String organization;
        String moduleName;
        String version;
        String packageName;
        String reason;

        public String getOrganization() {
            return organization;
        }

        public void setOrganization(String organization) {
            this.organization = organization;
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

        public String getPackageName() {
            return packageName;
        }

        public void setPackageName(String packageName) {
            this.packageName = packageName;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }

        public Module(String organization, String moduleName, String version, String packageName, String reason) {
            this.organization = organization;
            this.moduleName = moduleName;
            this.version = version;
            this.packageName = packageName;
            this.reason = reason;
        }
    }

    public List<Module> resolvedModules() {
        return resolvedModules;
    }

    public List<Module> unresolvedModules() {
        return unresolvedModules;
    }
}
