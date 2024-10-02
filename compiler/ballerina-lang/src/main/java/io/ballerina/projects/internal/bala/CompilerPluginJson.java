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
package io.ballerina.projects.internal.bala;

import java.util.List;

/**
 * {@code CompilerPluginJson} Model for compiler plugin JSON file.
 *
 * @since 2.0.0
 */
public class CompilerPluginJson {
    private final String plugin_id;
    private final String plugin_class;
    private List<String> dependency_paths;

    public CompilerPluginJson(String pluginId, String pluginClass, List<String> dependencyPaths) {
        this.plugin_id = pluginId;
        this.plugin_class = pluginClass;
        this.dependency_paths = dependencyPaths;
    }

    public String pluginId() {
        return plugin_id;
    }

    public String pluginClass() {
        return plugin_class;
    }

    public List<String> dependencyPaths() {
        return dependency_paths;
    }

    public void setDependencyPaths(List<String> dependencyPaths) {
        this.dependency_paths = dependencyPaths;
    }
}
