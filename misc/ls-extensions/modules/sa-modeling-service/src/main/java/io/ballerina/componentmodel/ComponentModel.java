/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.componentmodel;

import io.ballerina.componentmodel.entitymodel.components.Entity;
import io.ballerina.componentmodel.servicemodel.components.Service;

import java.util.List;
import java.util.Map;

/**
 * Represents intermediate model to represent multi-service projects.
 */
public class ComponentModel {
    private final PackageId packageId;
    private final List<Service> services;
    private final Map<String, Entity> entities;

    public ComponentModel(PackageId packageId, List<Service> services, Map<String, Entity> entities) {
        this.packageId = packageId;
        this.services = services;
        this.entities = entities;
    }

    public PackageId getPackageId() {
        return packageId;
    }

    public List<Service> getServices() {
        return services;
    }

    public Map<String, Entity> getEntities() {
        return entities;
    }

    /**
     * Represent current package information.
     */
    public static class PackageId {
        private final String name;
        private final String org;
        private final String version;

        public PackageId(String name, String org, String version) {
            this.name = name;
            this.org = org;
            this.version = version;
        }

        public String getName() {
            return name;
        }

        public String getOrg() {
            return org;
        }

        public String getVersion() {
            return version;
        }
    }
}
