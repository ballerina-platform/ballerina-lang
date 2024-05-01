/*
 * Copyright (c) 2024, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler.bir.codegen.internal;

import org.wso2.ballerinalang.compiler.bir.model.BIRInstruction;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.ballerinalang.compiler.bir.codegen.JvmCodeGenUtil.getModuleLevelClassName;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MAX_GENERATED_LAMBDAS_PER_CLASS;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmConstants.MODULE_LAMBDAS_CLASS_NAME;

/**
 * A data holder to record default values related lambda information.
 *
 * @since 2202.9.0
 */
public class RecordDefaultValueDataCollector {

    private final String moduleLambdaClassPrefix;
    private static int classIndex = 0;
    private final Map<String, List<RecordDefaultValueLambda>> recordDefaultValuesLambdas;
    private final Map<String, String> recordDefaultValuesLambdasClassMap;

    public RecordDefaultValueDataCollector(BIRNode.BIRPackage module) {
        this.recordDefaultValuesLambdasClassMap = new HashMap<>();
        this.recordDefaultValuesLambdas = new HashMap<>();
        this.moduleLambdaClassPrefix = getModuleLevelClassName(module.packageID, MODULE_LAMBDAS_CLASS_NAME);
    }

    public void add(String lambdaName, BIRInstruction callInstruction) {
        if (recordDefaultValuesLambdasClassMap.containsKey(lambdaName)) {
            return;
        }
        String enclosingClass = moduleLambdaClassPrefix + classIndex;
        List<RecordDefaultValueLambda> defaultValueLambdas =
                recordDefaultValuesLambdas.get(enclosingClass);
        if (defaultValueLambdas == null) {
            defaultValueLambdas = new ArrayList<>();
            recordDefaultValuesLambdas.put(enclosingClass, defaultValueLambdas);
        } else if (defaultValueLambdas.size() > MAX_GENERATED_LAMBDAS_PER_CLASS) {
            enclosingClass = moduleLambdaClassPrefix + ++classIndex;
            defaultValueLambdas = new ArrayList<>();
            recordDefaultValuesLambdas.put(enclosingClass, defaultValueLambdas);
        }
        defaultValueLambdas.add(new RecordDefaultValueLambda(lambdaName, callInstruction));
        recordDefaultValuesLambdasClassMap.put(lambdaName, enclosingClass);
    }

    public String getEnclosingClass(String lambdaName) {
        return recordDefaultValuesLambdasClassMap.get(lambdaName);
    }

    public Map<String, List<RecordDefaultValueLambda>> getRecordDefaultValuesLambdas() {
        return recordDefaultValuesLambdas;
    }

    /**
     * A data holder to information of lambdas created for record default values.
     *
     * @since 2201.9.0
     */
    public static class RecordDefaultValueLambda {

        public String lambdaName;

        public BIRInstruction callInstruction;

        public RecordDefaultValueLambda(String lambdaName, BIRInstruction callInstruction) {
            this.lambdaName = lambdaName;
            this.callInstruction = callInstruction;
        }
    }
}
