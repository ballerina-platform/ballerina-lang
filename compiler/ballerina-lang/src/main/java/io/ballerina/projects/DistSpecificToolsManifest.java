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
 * Represents a dist<version>.toml file.
 *
 * @since 2201.8.0
 */
public class DistSpecificToolsManifest implements BalToolsManifest {
    private final Map<String, Map<String, Tool>> tools;

    private DistSpecificToolsManifest(Map<String, Map<String, Tool>> tools) {
        this.tools = tools;
    }

    public static DistSpecificToolsManifest from() {
        return new DistSpecificToolsManifest(new HashMap<>());
    }

    public static DistSpecificToolsManifest from(Map<String, Map<String, Tool>> tools) {
        return new DistSpecificToolsManifest(tools);
    }

    public Map<String, Map<String, Tool>> tools() {
        return tools;
    }

    public void addTool(String id, String org, String name, String version, Boolean active) {
        if (!tools.containsKey(id)) {
            tools.put(id, new HashMap<>());
        }
        tools.get(id).put(version, new Tool(id, org, name, version, active));
    }

    public void removeTool(String id) {
        if (!tools.containsKey(id)) {
            return;
        }
        tools.remove(id);
    }

    public void removeToolVersion(String id, String version) {
        if (tools.containsKey(id)) {
            tools.get(id).remove(version);
        }
    }

    @Override
    public String toString() {
        StringBuilder content = new StringBuilder();
        for (Map.Entry<String, Map<String, Tool>> toolVersions: this.tools().entrySet()) {
            for (Map.Entry<String, Tool> tool: toolVersions.getValue().entrySet()) {
                content.append("[[tool]]\n");
                content.append("id = \"").append(tool.getValue().id()).append("\"\n");
                content.append("org = \"").append(tool.getValue().org()).append("\"\n");
                content.append("name = \"").append(tool.getValue().name()).append("\"\n");
                content.append("version = \"").append(tool.getValue().version()).append("\"\n");
                content.append("active = ").append(tool.getValue().active()).append("\n");
                content.append("\n");
            }
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
        private final String version;
        private Boolean active;

        public Tool(String id, String org, String name, String version, Boolean active) {
            this.id = id;
            this.org = org;
            this.name = name;
            this.version = version;
            this.active = active;
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

        public String version() {
            return version;
        }

        public Boolean active() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }
    }
}
