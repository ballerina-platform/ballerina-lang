/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.siddhi.core.table;

import org.ballerinalang.siddhi.core.executor.ExpressionExecutor;

import java.util.Map;

/**
 * Compiled update-set created for {@link InMemoryTable}.
 */
public class InMemoryCompiledUpdateSet implements CompiledUpdateSet {
    //In this map, the key will be the attribute position of the updating event
    //which should be updated. Value is the expression executor which is obtained
    // from the expressions in the set clause.
    private Map<Integer, ExpressionExecutor> expressionExecutorMap;

    public InMemoryCompiledUpdateSet(Map<Integer, ExpressionExecutor> expressionExecutorMap) {
        this.expressionExecutorMap = expressionExecutorMap;
    }

    public Map<Integer, ExpressionExecutor> getExpressionExecutorMap() {
        return expressionExecutorMap;
    }
}
