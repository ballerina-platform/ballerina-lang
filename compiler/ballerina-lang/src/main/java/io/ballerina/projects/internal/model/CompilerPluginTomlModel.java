/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import com.google.gson.annotations.SerializedName;
import io.ballerina.projects.TomlDocument;
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
 * {@code CompilerPluginTomlModel} Model for `Compiler-plugin.toml` file.
 *
 * @since 2.0.0
 */
public class CompilerPluginTomlModel {
    private Plugin plugin;

    private static final String DEPENDENCY = "dependency";
    @SerializedName(DEPENDENCY) private List<Dependency> dependencies;

    private CompilerPluginTomlModel(Plugin plugin, List<Dependency> dependencies) {
        this.plugin = plugin;
        this.dependencies = dependencies;
    }

    public static CompilerPluginTomlModel from(TomlDocument tomlDocument) {
        TomlTableNode tomlTableNode = tomlDocument.toml().rootNode();
        if (tomlTableNode.entries().isEmpty()) {
            return new CompilerPluginTomlModel(null, Collections.emptyList());
        }
        return new CompilerPluginTomlModel(new Plugin(getPluginClass(tomlTableNode)), getDependencies(tomlTableNode));
    }

    public Plugin plugin() {
        return plugin;
    }

    public void setPlugin(Plugin plugin) {
        this.plugin = plugin;
    }

    public List<Dependency> dependencies() {
        return dependencies;
    }

    public void setDependencies(List<Dependency> dependencies) {
        this.dependencies = dependencies;
    }

    public List<String> getCompilerPluginDependencies() {
        List<String> compilerPluginDependencies = new ArrayList<>();
        for (Dependency dependency : this.dependencies) {
            compilerPluginDependencies.add(dependency.getPath());
        }
        return compilerPluginDependencies;
    }

    /**
     * {@code Plugin} Model for plugin toml table of `Compiler-plugin.toml` file.
     */
    public static class Plugin {
        private static final String CLASS = "class";
        @SerializedName(CLASS) private String className;

        Plugin(String className) {
            this.className = className;
        }

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }
    }

    /**
     * {@code Dependency} Model for dependency of `Compiler-plugin.toml` file.
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

    private static List<Dependency> getDependencies(TomlTableNode tomlTableNode) {
        List<Dependency> dependencies = new ArrayList<>();
        TopLevelNode dependenciesNode = tomlTableNode.entries().get(DEPENDENCY);

        if (dependenciesNode != null && dependenciesNode.kind() == TomlType.TABLE_ARRAY) {
            TomlTableArrayNode dependencyTableArray = (TomlTableArrayNode) dependenciesNode;

            for (TomlTableNode dependencyNode : dependencyTableArray.children()) {
                TopLevelNode pathNode = dependencyNode.entries().get("path");
                dependencies.add(new Dependency(getStringFromTomlTableNode(pathNode)));
            }
        }
        return dependencies;
    }

    private static String getPluginClass(TomlTableNode tomlTableNode) {
        TomlTableNode pluginNode = (TomlTableNode) tomlTableNode.entries().get("plugin");
        if (pluginNode != null && pluginNode.kind() != TomlType.NONE && pluginNode.kind() == TomlType.TABLE) {
            TopLevelNode topLevelNode = pluginNode.entries().get(Plugin.CLASS);
            if (!(topLevelNode == null || topLevelNode.kind() == TomlType.NONE)) {
                return getStringFromTomlTableNode(topLevelNode);
            }
        }
        return null;
    }

    private static String getStringFromTomlTableNode(TopLevelNode topLevelNode) {
        if (topLevelNode.kind() == TomlType.KEY_VALUE) {
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
