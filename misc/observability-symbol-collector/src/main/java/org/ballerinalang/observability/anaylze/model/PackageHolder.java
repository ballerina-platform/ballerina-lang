/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.observability.anaylze.model;

import com.google.gson.JsonElement;
import io.ballerina.projects.ModuleDescriptor;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Hold data related to a package.
 *
 * @since 2.0.0
 */
public class PackageHolder {

    private String org;
    private String name;
    private String version;
    private final Map<String, ModuleHolder> modulesMap = new ConcurrentHashMap<>();

    public void addSyntaxTree(ModuleDescriptor moduleDescriptor, String documentName, JsonElement syntaxTreeJson) {
        String moduleName = moduleDescriptor.name().toString();
        ModuleHolder moduleHolder = this.modulesMap.computeIfAbsent(moduleName, k -> new ModuleHolder(moduleName));
        moduleHolder.addSyntaxTree(documentName, syntaxTreeJson);
    }

    public Map<String, ModuleHolder> getModules() {
        return Collections.unmodifiableMap(modulesMap);
    }

    public String getOrg() {
        return org;
    }

    public void setOrg(String org) {
        this.org = org;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
