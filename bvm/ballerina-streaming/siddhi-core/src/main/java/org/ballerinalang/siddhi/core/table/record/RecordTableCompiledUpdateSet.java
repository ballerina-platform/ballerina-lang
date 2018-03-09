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

package org.ballerinalang.siddhi.core.table.record;

import org.ballerinalang.siddhi.core.executor.ExpressionExecutor;
import org.ballerinalang.siddhi.core.table.CompiledUpdateSet;
import org.ballerinalang.siddhi.core.util.collection.operator.CompiledExpression;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * {@link CompiledUpdateSet} implementation for Record Table.
 */
public class RecordTableCompiledUpdateSet implements CompiledUpdateSet {
    private LinkedHashMap<String, CompiledExpression> updateSetMap = new LinkedHashMap<>();
    private Map<String, ExpressionExecutor> expressionExecutorMap = new HashMap<>();

    public LinkedHashMap<String, CompiledExpression> getUpdateSetMap() {
        return updateSetMap;
    }

    public void put(String attributeName, CompiledExpression compiledExpression) {
        updateSetMap.put(attributeName, compiledExpression);
    }

    public Map<String, ExpressionExecutor> getExpressionExecutorMap() {
        return expressionExecutorMap;
    }

    public void setExpressionExecutorMap(Map<String, ExpressionExecutor> expressionExecutorMap) {
        this.expressionExecutorMap = expressionExecutorMap;
    }
}
