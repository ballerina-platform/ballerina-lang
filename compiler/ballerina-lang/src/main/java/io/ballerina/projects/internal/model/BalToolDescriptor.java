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
package io.ballerina.projects.internal.model;

import io.ballerina.projects.TomlDocument;
import io.ballerina.projects.internal.bala.BalToolJson;
import io.ballerina.toml.semantic.TomlType;
import io.ballerina.toml.semantic.ast.TomlKeyValueNode;
import io.ballerina.toml.semantic.ast.TomlStringValueNode;
import io.ballerina.toml.semantic.ast.TomlTableArrayNode;
import io.ballerina.toml.semantic.ast.TomlTableNode;
import io.ballerina.toml.semantic.ast.TomlValueNode;
import io.ballerina.toml.semantic.ast.TopLevelNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * {@code BalToolDescriptor} Model for `BalTool.toml` file.
 *
 * @since 2201.6.0
 */
public class BalToolDescriptor {
    public static final String ID = "id";
    private static final String TOOL = "tool";
    private static final String DEPENDENCY = "dependency";
    public static final String PATH = "path";

    private BalToolDescriptor.Tool tool;
    private List<BalToolDescriptor.Dependency> dependencies;

    private BalToolDescriptor(BalToolDescriptor.Tool tool, List<BalToolDescriptor.Dependency> dependencies) {
        this.tool = tool;
        this.dependencies = dependencies;
    }

    public static BalToolDescriptor from(TomlDocument tomlDocument) {
        TomlTableNode tomlTableNode = tomlDocument.toml().rootNode();
        if (tomlTableNode.entries().isEmpty()) {
            return new BalToolDescriptor(null, Collections.emptyList());
        }
        return new BalToolDescriptor(
                new BalToolDescriptor.Tool(getToolID(tomlTableNode)), getDependencies(tomlTableNode));
    }

    public static BalToolDescriptor from(BalToolJson balToolJson) {
        List<BalToolDescriptor.Dependency> dependencyList = new ArrayList<>();
        for (String path : balToolJson.dependencyPaths()) {
            dependencyList.add(new BalToolDescriptor.Dependency(path));
        }
        return new BalToolDescriptor(new BalToolDescriptor.Tool(balToolJson.toolId()), dependencyList);
    }

    public BalToolDescriptor.Tool tool() {
        return tool;
    }

    public List<BalToolDescriptor.Dependency> dependencies() {
        return dependencies;
    }

    public List<String> getBalToolDependencies() {
        List<String> balToolDependencies = new ArrayList<>();
        for (BalToolDescriptor.Dependency dependency : this.dependencies) {
            balToolDependencies.add(dependency.getPath());
        }
        return balToolDependencies;
    }

    /**
     * {@code Tool} Model for tool toml table of `BalTool.toml` file.
     */
    public static class Tool {
        private String id;

        Tool(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }

    /**
     * {@code Dependency} Model for dependency of `BalTool.toml` file.
     */
    public static class Dependency {
        private String path;

        Dependency(String path) {
            this.path = path;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }
    }

    private static List<BalToolDescriptor.Dependency> getDependencies(TomlTableNode tomlTableNode) {
        List<BalToolDescriptor.Dependency> dependencies = new ArrayList<>();
        TopLevelNode dependenciesNode = tomlTableNode.entries().get(DEPENDENCY);

        if (dependenciesNode != null && dependenciesNode.kind() == TomlType.TABLE_ARRAY) {
            TomlTableArrayNode dependencyTableArray = (TomlTableArrayNode) dependenciesNode;

            for (TomlTableNode dependencyNode : dependencyTableArray.children()) {
                TopLevelNode pathNode = dependencyNode.entries().get(PATH);
                dependencies.add(new BalToolDescriptor.Dependency(getStringFromTomlTableNode(pathNode)));
            }
        }
        return dependencies;
    }

    private static String getToolID(TomlTableNode tomlTableNode) {
        TomlTableNode toolNode = (TomlTableNode) tomlTableNode.entries().get(TOOL);
        if (toolNode != null && toolNode.kind() != TomlType.NONE && toolNode.kind() == TomlType.TABLE) {
            TopLevelNode topLevelNode = toolNode.entries().get(ID);
            if (!(topLevelNode == null || topLevelNode.kind() == TomlType.NONE)) {
                return getStringFromTomlTableNode(topLevelNode);
            }
        }
        return null;
    }

    private static String getStringFromTomlTableNode(TopLevelNode topLevelNode) {
        if (topLevelNode != null && topLevelNode.kind() == TomlType.KEY_VALUE) {
            TomlKeyValueNode keyValueNode = (TomlKeyValueNode) topLevelNode;
            TomlValueNode value = keyValueNode.value();
            if (value.kind() == TomlType.STRING) {
                TomlStringValueNode stringValueNode = (TomlStringValueNode) value;
                return stringValueNode.getValue();
            }
        }
        return null;
    }
}
