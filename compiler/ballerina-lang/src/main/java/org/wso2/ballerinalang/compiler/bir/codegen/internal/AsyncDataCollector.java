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

    private Map<String, BIRInstruction> lambdas;
    private Map<String, ScheduleFunctionInfo> strandMetaDataMap;
    private int lambdaIndex = 0;
    private String enclosingClass;

    public AsyncDataCollector(String enclosingClass) {

        this.enclosingClass = enclosingClass;
        lambdas = new HashMap<>();
        strandMetaDataMap = new HashMap<>();
    }

    public void incrementLambdaIndex() {

        lambdaIndex++;
    }

    public int getLambdaIndex() {

        return lambdaIndex;
    }

    public void add(String lambdaName, BIRInstruction callInstruction) {

        lambdas.put(lambdaName, callInstruction);
    }

    public String getEnclosingClass() {

        return enclosingClass;
    }

    public Map<String, BIRInstruction> getLambdas() {

        return lambdas;
    }

    public Map<String, ScheduleFunctionInfo> getStrandMetadata() {
        return strandMetaDataMap;
    }
}
