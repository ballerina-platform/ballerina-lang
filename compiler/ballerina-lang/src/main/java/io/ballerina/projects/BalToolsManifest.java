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
 * Represents a bal-tools.toml file.
 *
 * @since 2201.6.0
 */
public class BalToolsManifest {
    private final Map<String, Tool> tools;

    private BalToolsManifest(Map<String, Tool> tools) {
        this.tools = tools;
    }

    public static BalToolsManifest from() {
        return new BalToolsManifest(new HashMap<>());
    }

    public static BalToolsManifest from(Map<String, Tool> tools) {
        return new BalToolsManifest(tools);
    }

    public Map<String, Tool> tools() {
        return tools;
    }

    public void addTool(String id, String org, String name, String version) {
        tools.put(id, new Tool(id, org, name, version));
    }

    public void removeTool(String id) {
        if (!tools.containsKey(id)) {
            return;
        }
        tools.remove(id);
    }

    /**
     * Represents a tool in bal-tools.toml.
     *
     * @since 2201.6.0
     */
    public static class Tool {
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
