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
import java.util.Optional;

/**
 * Represents a bal-tools.toml file.
 *
 * @since 2201.6.0
 */
public class BalToolsManifest {
    private final Map<String, Map<String, Map<String, Tool>>> tools;

    private BalToolsManifest(Map<String, Map<String, Map<String, Tool>>> tools) {
        this.tools = tools;
    }

    public static BalToolsManifest from() {
        return new BalToolsManifest(new HashMap<>());
    }

    public static BalToolsManifest from(Map<String, Map<String, Map<String, Tool>>> tools) {
        return new BalToolsManifest(tools);
    }

    public Map<String, Map<String, Map<String, Tool>>> tools() {
        return tools;
    }

    public void addTool(String id, String org, String name, String version, Boolean active, String repository) {
        if (!tools.containsKey(id)) {
            tools.put(id, new HashMap<>());
        }
        if (!tools.get(id).containsKey(version)) {
            tools.get(id).put(version, new HashMap<>());
        }

        if (active) {
            flipCurrentActiveToolVersion(id);
        }
        tools.get(id).get(version).put(repository, new Tool(id, org, name, version, active, repository));
    }

    public Optional<Tool> getTool(String id, String version, String repository) {
        if (tools.containsKey(id) && tools.get(id).containsKey(version)) {
            return Optional.ofNullable(tools.get(id).get(version).get(repository));
        }
        return Optional.empty();
    }

    public Optional<Tool> getActiveTool(String id) {
        if (tools.containsKey(id)) {
            return tools.get(id).values().stream().flatMap(v -> v.values().stream()).filter(Tool::active).findFirst();
        }
        return Optional.empty();
    }

    public void setActiveToolVersion(String id, String version, String repository) {
        if (tools.containsKey(id)) {
            flipCurrentActiveToolVersion(id);
            tools.get(id).get(version).get(repository).setActive(true);
        }
    }

    public void removeTool(String id) {
        if (!tools.containsKey(id)) {
            return;
        }
        tools.remove(id);
    }

    public void removeToolVersion(String id, String version, String repository) {
        if (tools.containsKey(id) && tools.get(id).containsKey(version)) {
            tools.get(id).get(version).remove(repository);
        }
    }

    private void flipCurrentActiveToolVersion(String id) {
        tools.get(id).forEach((k, v) -> v.forEach((k1, v1) -> v1.setActive(false)));
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
        private Boolean active;
        private String repository;

        public Tool(String id, String org, String name, String version, Boolean active, String repository) {
            this.id = id;
            this.org = org;
            this.name = name;
            this.version = version;
            this.active = active;
            this.repository = repository;
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

        public String repository() {
            return repository;
        }

        public void setActive(boolean active) {
            this.active = active;
        }
    }
}
