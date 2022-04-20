/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.runtime.internal.configurable.providers.toml;

import io.ballerina.runtime.api.Module;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * This class contains the information of modules that have configurable variables.
 *
 * @since 2.0.0
 */
public class ModuleInfo {
    private final Set<String> moduleNames = new HashSet<>();
    private final Set<String> orgNames = new HashSet<>();

    private final Set<Module> moduleSet;
    private boolean hasModuleAmbiguity;

    public ModuleInfo(Set<Module> moduleSet) {
        this.moduleSet = moduleSet;
    }

    void analyseModules(Module rootModule) {
        for (Module entry : moduleSet) {
            String rootModuleName = rootModule.getName();
            String orgName = entry.getOrg();
            if (rootModuleName.startsWith(orgName + ".") || rootModuleName.equals(orgName)) {
                hasModuleAmbiguity = true;
            }
            orgNames.add(entry.getOrg());
            Collections.addAll(moduleNames, entry.getName().split("\\."));
        }
    }

    boolean hasModuleAmbiguity() {
        return hasModuleAmbiguity;
    }

    boolean containsOrg(String nodeName) {
        return orgNames.contains(nodeName);
    }

    boolean containsModule(String nodeName) {
        return moduleNames.contains(nodeName);
    }

    Module getModuleFromName(String nodeName) {
        for (Module entry : moduleSet) {
            if (entry.getName().equals(nodeName)) {
                return entry;
            }
        }
        return null;
    }

}
