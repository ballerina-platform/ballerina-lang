/*
 *  Copyright (c) 2023, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.projects;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a global bal-tools.toml file.
 *
 * @since 2201.8.0
 */
public class GlobalToolsManifest implements BalToolsManifest {
    private final Map<String, Tool> tools;

    private GlobalToolsManifest(Map<String, Tool> tools) {
        this.tools = tools;
    }

    public static GlobalToolsManifest from() {
            return new GlobalToolsManifest(new HashMap<>());
    }

    public static GlobalToolsManifest from(Map<String, Tool> tools) {
        return new GlobalToolsManifest(tools);
    }

    public Map<String, Tool> tools() {
        return tools;
    }

    public void addTool(String id, String org, String name) {
        tools.put(id, new Tool(id, org, name));
    }

    public void removeTool(String id) {
        if (!tools.containsKey(id)) {
            return;
        }
        tools.remove(id);
    }

    @Override
    public String toString() {
        StringBuilder content = new StringBuilder();
        for (Map.Entry<String, Tool> tool: this.tools().entrySet()) {
            content.append("[[tool]]\n");
            content.append("id = \"").append(tool.getValue().id()).append("\"\n");
            content.append("org = \"").append(tool.getValue().org()).append("\"\n");
            content.append("name = \"").append(tool.getValue().name()).append("\"\n");
            content.append("\n");
        }
        return String.valueOf(content);
    }

    /**
     * Represents a tool in bal-tools.toml.
     *
     * @since 2201.6.0
     */
    public static class Tool implements BalToolsManifest.Tool {
        private final String id;
        private final String org;
        private final String name;

        public Tool(String id, String org, String name) {
            this.id = id;
            this.org = org;
            this.name = name;
        }

        public String id() {
            return id;
        }

        public String org() {
            return org;
        }

        public String name() {
            return name;
        }
    }
}
