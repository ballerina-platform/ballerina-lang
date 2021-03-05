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

import java.util.ArrayList;
import java.util.List;

/**
 * {@code CompilerPluginJson} Model for `Compiler-plugin.toml` file.
 *
 * @since 2.0.0
 */
public class CompilerPluginToml {
    private Plugin plugin;

    private static final String DEPENDENCY = "dependency";
    @SerializedName(DEPENDENCY) private List<Dependency> dependencies;

    public Plugin getPlugin() {
        return plugin;
    }

    public void setPlugin(Plugin plugin) {
        this.plugin = plugin;
    }

    public List<Dependency> getDependencies() {
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

    public static class Plugin {
        private static final String SERIALIZED_CLASS = "class";
        @SerializedName(SERIALIZED_CLASS) private String className;

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }
    }

    public static class Dependency {
        private String path;

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }
    }
}
