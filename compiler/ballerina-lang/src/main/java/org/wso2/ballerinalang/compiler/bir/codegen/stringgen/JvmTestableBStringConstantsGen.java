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
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.TEST_MODULE_STRING_CONSTANT_CLASS_NAME;

/**
 * Generates Jvm class for the ballerina string constants for given test module.
 *
 * @since 2.0.0
 */
public class JvmTestableBStringConstantsGen extends JvmBStringConstantsGen {

    private final Map<String, JvmBStringConstant> bStringVarMap;

    private final String testModStringConstantsClass;

    private final AtomicInteger constantIndex;

    public JvmTestableBStringConstantsGen(BIRNode.BIRPackage module,
                                          Map<String, JvmBStringConstant> moduleBStringVarMap) {
        super(module, moduleBStringVarMap);
        bStringVarMap = new ConcurrentHashMap<>();
        this.testModStringConstantsClass = getModuleLevelClassName(module.packageID,
                                                                   TEST_MODULE_STRING_CONSTANT_CLASS_NAME);
        constantIndex = new AtomicInteger(moduleBStringVarMap.size());
    }

    public void generateConstantInit(Map<String, byte[]> jarEntries) {
        super.generateConstantInit(jarEntries);
        JVMBStringGenUtils.generateConstantInit(jarEntries, testModStringConstantsClass, bStringVarMap);
    }

    public JvmBStringConstant addBString(String val) {
        if (super.getBStringVarMap().containsKey(val)) {
            return super.getBStringVarMap().get(val);
        }
        return bStringVarMap.computeIfAbsent(val, s -> new JvmBStringConstant(
                JvmConstants.B_STRING_VAR_PREFIX + constantIndex.getAndIncrement(),
                testModStringConstantsClass));
    }
}
