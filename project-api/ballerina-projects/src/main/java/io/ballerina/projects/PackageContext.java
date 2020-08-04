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
package io.ballerina.projects;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Maintains the internal state of a {@code Package} instance.
 * <p>
 * Works as a package cache.
 *
 * @since 2.0.0
 */
class PackageContext {
    private final PackageConfig packageConfig;
    private final ModuleContext defaultModuleContext;
    private final Map<ModuleId, ModuleContext> moduleContextMap;
    private final Collection<ModuleId> moduleIds;

    private PackageContext(PackageConfig packageConfig,
                           ModuleContext defaultModuleContext,
                           Map<ModuleId, ModuleContext> moduleContextMap) {
        this.packageConfig = packageConfig;
        this.defaultModuleContext = defaultModuleContext;
        this.moduleIds = Collections.unmodifiableCollection(moduleContextMap.keySet());
        this.moduleContextMap = moduleContextMap;
    }

    static PackageContext from(PackageConfig packageConfig) {
        final Map<ModuleId, ModuleContext> moduleContextMap = new HashMap<>();
        // TODO Do we need to handle the default module case separately like this.
        // TODO default module can be optional
        // TODO One solution is to
        final ModuleConfig defaultModuleConfig = packageConfig.defaultModule();
        final ModuleContext defaultModuleContext = ModuleContext.from(defaultModuleConfig);
        moduleContextMap.put(defaultModuleConfig.moduleId(), defaultModuleContext);

        for (ModuleConfig moduleConfig : packageConfig.otherModules()) {
            moduleContextMap.put(moduleConfig.moduleId(), ModuleContext.from(moduleConfig));
        }

        return new PackageContext(packageConfig, defaultModuleContext, moduleContextMap);
    }

    PackageId packageId() {
        return this.packageConfig.packageId();
    }

    Collection<ModuleId> moduleIds() {
        return this.moduleIds;
    }

    ModuleContext moduleContext(ModuleId moduleId) {
        return this.moduleContextMap.get(moduleId);
    }

    ModuleContext defaultModuleContext() {
        return this.defaultModuleContext;
    }
}
