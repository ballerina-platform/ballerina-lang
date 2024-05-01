/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import java.util.HashMap;
import java.util.Map;

/**
 * A data holder to keep async invocation related data which will be used by class generation.
 *
 * @since 1.3.0
 */
public class AsyncDataCollector {

    private final Map<String, BIRInstruction> lambdas;
    private final Map<String, ScheduleFunctionInfo> strandMetaDataMap;
    private final String enclosingClass;
    private int lambdaIndex = 0;
    private final RecordDefaultValueDataCollector defaultValueDataCollector;
    private static final String RECORD_LAMBDA_PREFIX = "$rec";

    public AsyncDataCollector(String enclosingClass, RecordDefaultValueDataCollector defaultValueDataCollector) {
        this.enclosingClass = enclosingClass;
        this.lambdas = new HashMap<>();
        this.strandMetaDataMap = new HashMap<>();
        this.defaultValueDataCollector = defaultValueDataCollector;
    }

    public int getLambdaIndex(String funcName) {
        if (funcName.contains(RECORD_LAMBDA_PREFIX)) {
            return defaultValueDataCollector.getLambdaIndex();
        }
        return lambdaIndex;
    }

    public void add(String lambdaName, BIRInstruction callInstruction) {
        if (lambdas.containsKey(lambdaName)) {
            return;
        }
        if (lambdaName.contains(RECORD_LAMBDA_PREFIX)) {
            defaultValueDataCollector.add(lambdaName, callInstruction);
        } else {
            lambdas.put(lambdaName, callInstruction);
        }
        lambdaIndex++;
    }

    public String getEnclosingClass(String lambdaName) {
        if (lambdaName.contains(RECORD_LAMBDA_PREFIX)) {
            return defaultValueDataCollector.getEnclosingClass(lambdaName);
        }
        return enclosingClass;
    }

    public Map<String, BIRInstruction> getLambdas() {
        return lambdas;
    }

    public Map<String, ScheduleFunctionInfo> getStrandMetadata() {
        return strandMetaDataMap;
    }
}
