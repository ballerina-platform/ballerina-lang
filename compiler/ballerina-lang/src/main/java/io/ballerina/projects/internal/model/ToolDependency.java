/*
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
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

package io.ballerina.projects.internal.model;

/**
 * {@code ToolDependency} Model for Tool Dependency.
 *
 * @since 2201.9.0
 */
public class ToolDependency {
    String id;
    String org;
    String name;
    String version;

    public ToolDependency(String id, String org, String name, String version) {
        this.id = id;
        this.org = org;
        this.name = name;
        this.version = version;
    }

    public String getId() {
        return id;
    }

    public String getOrg() {
        return org;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }
}
