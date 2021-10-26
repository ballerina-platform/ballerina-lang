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

import java.util.ArrayList;
import java.util.List;

/**
 * Package name resolution request model.
 *
 * @since 2.0.0
 */
public class PackageNameResolutionRequest {

    List<Module> modules;

    /**
     * Package name resolution request Module model.
     */
    public static class Module {

        /**
         * Package name resolution request PossiblePackage model in Module.
         */
        public static class PossiblePackage {
            String org;
            String name;
            String version;

            public PossiblePackage(String org, String name, String version) {
                this.org = org;
                this.name = name;
                this.version = version;
            }

            public String org() {
                return org;
            }

            public String name() {
                return name;
            }

            public String version() {
                return version;
            }
        }

        String organization;
        String moduleName;
        List<PossiblePackage> possiblePackages;
        PackageResolutionRequest.Mode mode;

        Module(String organization, String moduleName, List<PossiblePackage> possiblePackages,
                      PackageResolutionRequest.Mode mode) {
            this.organization = organization;
            this.moduleName = moduleName;
            this.possiblePackages = possiblePackages;
            this.mode = mode;
        }

        public String organization() {
            return organization;
        }

        public String moduleName() {
            return moduleName;
        }

        public List<PossiblePackage> possiblePackages() {
            return possiblePackages;
        }

        public PackageResolutionRequest.Mode mode() {
            return mode;
        }
    }

    public PackageNameResolutionRequest() {
        this.modules = new ArrayList<>();
    }

    public void addModule(String orgName, String moduleName, List<Module.PossiblePackage> possiblePackages,
                          PackageResolutionRequest.Mode mode) {
        modules.add(new Module(orgName, moduleName, possiblePackages, mode));
    }

    public void addModule(String orgName, String moduleName) {
        modules.add(new Module(orgName, moduleName, null, null));
    }
}
