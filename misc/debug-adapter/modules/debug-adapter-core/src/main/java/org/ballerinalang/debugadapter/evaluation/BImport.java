/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

package org.ballerinalang.debugadapter.evaluation;

import io.ballerina.compiler.api.symbols.ModuleSymbol;

import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.MODULE_NAME_SEPARATOR_REGEX;

/**
 * A helper class to hold resolved imports information during evaluation.
 *
 * @since 2.0.0.
 */
public class BImport {

    private final String orgName;
    private final String moduleName;
    private final String alias;
    private ModuleSymbol resolvedModuleSymbol;

    public BImport(String orgName, String moduleName, String alias) {
        this.orgName = orgName;
        this.moduleName = moduleName;
        this.alias = alias;
    }

    public String orgName() {
        return orgName;
    }

    public String packageName() {
        return moduleName.split(MODULE_NAME_SEPARATOR_REGEX)[0];
    }

    public String moduleName() {
        return moduleName;
    }

    public String alias() {
        return alias;
    }

    public ModuleSymbol getResolvedSymbol() {
        return resolvedModuleSymbol;
    }

    public void setResolvedSymbol(ModuleSymbol moduleSymbol) {
        resolvedModuleSymbol = moduleSymbol;
    }
}
