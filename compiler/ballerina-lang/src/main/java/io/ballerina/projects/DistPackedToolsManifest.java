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
 * Represents a bal-tools.toml file packed in the distribution.
 *
 * @since 2201.8.0
 */
public class DistPackedToolsManifest implements BalToolsManifest {
    private final Map<String, Map<String, Tool>> tools;

    private DistPackedToolsManifest(Map<String, Map<String, Tool>> tools) {
        this.tools = tools;
    }

    public static DistPackedToolsManifest from() {
        return new DistPackedToolsManifest(new HashMap<>());
    }

    public static DistPackedToolsManifest from(Map<String, Map<String, Tool>> tools) {
        return new DistPackedToolsManifest(tools);
    }

    public Map<String, Map<String, Tool>> tools() {
        return tools;
    }

    public void addTool(String id, String org, String name, String version) {
        if (!tools.containsKey(id)) {
            tools.put(id, new HashMap<>());
        }
        tools.get(id).put(version, new Tool(id, org, name, version));
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

        public Tool(String id, String org, String name, String version) {
            this.id = id;
            this.org = org;
            this.name = name;
            this.version = version;
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
    }
}
