/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.composer.service.workspace.utils;

import com.google.gson.JsonArray;
import org.ballerinalang.composer.service.workspace.model.ModelPackage;
import org.ballerinalang.model.SymbolScope;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.util.program.BLangPrograms;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;

/**
 * BLang program content provider
 */
public class BallerinaProgramContentProvider {

    private static final Logger logger = LoggerFactory.getLogger(BallerinaProgramContentProvider.class);
    private SymbolScope globalScope;
    private static BallerinaProgramContentProvider instance = null;
    private static final Object lock = new Object();

    private BallerinaProgramContentProvider() {
        this.globalScope = BLangPrograms.populateGlobalScope();
    }

    public static synchronized BallerinaProgramContentProvider getInstance() {
        if (instance == null) {
            instance = new BallerinaProgramContentProvider();
        }
        return instance;
    }

    /**
     * Returns native types
     * @return JsonArray
     */
    public JsonArray builtinTypes() {
        JsonArray nativeTypes = new JsonArray();
        globalScope.getSymbolMap().values().stream().forEach(symbol -> {
            if (symbol instanceof BType) {
                nativeTypes.add(symbol.getName());
            }
        });
        return nativeTypes;
    }

    /**
     * Get All Native Packages
     * @return {Map} <Package name, package functions and connectors>
     * */
    public Map<String, ModelPackage> getAllPackages() {
        return org.ballerinalang.composer.service.workspace.util.WorkspaceUtils.getAllPackages();
    }
}
