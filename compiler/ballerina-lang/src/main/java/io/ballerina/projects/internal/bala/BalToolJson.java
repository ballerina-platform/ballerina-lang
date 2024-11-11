/*
 *  Copyright (c) 2023, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.projects.internal.bala;

import java.util.List;

/**
 * {@code BalToolJson} Model for bal tool JSON file.
 *
 * @since 2201.6.0
 */
public class BalToolJson {
    private final String tool_id;
    private List<String> dependency_paths;

    public BalToolJson(String tool_id, List<String> dependency_paths) {
        this.tool_id = tool_id;
        this.dependency_paths = dependency_paths;
    }

    public String toolId() {
        return tool_id;
    }

    public List<String> dependencyPaths() {
        return dependency_paths;
    }

    public void setDependencyPaths(List<String> dependencyPaths) {
        this.dependency_paths = dependencyPaths;
    }
}
