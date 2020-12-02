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
import org.ballerinalang.model.elements.PackageID;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Hold data related to a package.
 *
 * @since 2.0.0
 */
public class PackageHolder {

    private static final PackageHolder INSTANCE = new PackageHolder();

    private final Map<String, ModuleHolder> modulesMap = new ConcurrentHashMap<>();

    private PackageHolder() {   // Prevent initialization
    }

    public void addSyntaxTree(PackageID moduleId, String documentName, JsonElement syntaxTreeJson) {
        ModuleHolder moduleHolder = this.modulesMap.computeIfAbsent(moduleId.toString(), k ->
                new ModuleHolder(moduleId.orgName.value, moduleId.name.value, moduleId.version.value));
        moduleHolder.addSyntaxTree(documentName, syntaxTreeJson);
    }

    public static PackageHolder getInstance() {
        return INSTANCE;
    }

    public Map<String, ModuleHolder> getModules() {
        return Collections.unmodifiableMap(modulesMap);
    }
}
