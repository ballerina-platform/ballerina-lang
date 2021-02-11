/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.ballerinalang.compiler.bir.codegen.stringgen;

import org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil.getModuleLevelClassName;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE_STRING_CONSTANT_CLASS_NAME;

/**
 * Generates Jvm class for the ballerina string constants for given module.
 *
 * @since 2.0.0
 */
public class JvmBStringConstantsGen {

    private final Map<String, JvmBStringConstant> bStringVarMap;

    private final String stringConstantsClass;

    private final AtomicInteger constantIndex = new AtomicInteger();

    public JvmBStringConstantsGen(BIRNode.BIRPackage module) {
        this(module, new ConcurrentHashMap<>());
    }

    JvmBStringConstantsGen(BIRNode.BIRPackage module, Map<String, JvmBStringConstant> bStringVarMap) {
        this.stringConstantsClass = getModuleLevelClassName(module.packageID, MODULE_STRING_CONSTANT_CLASS_NAME);
        this.bStringVarMap = bStringVarMap;
    }

    public JvmBStringConstant addBString(String val) {
        return bStringVarMap.computeIfAbsent(val, s -> new JvmBStringConstant(
                JvmConstants.B_STRING_VAR_PREFIX + constantIndex.getAndIncrement(),
                stringConstantsClass));
    }

    public void generateConstantInit(Map<String, byte[]> jarEntries) {
        JVMBStringGenUtils.generateConstantInit(jarEntries, stringConstantsClass, bStringVarMap);
    }

    public Map<String, JvmBStringConstant> getBStringVarMap() {
        return bStringVarMap;
    }
}
