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

package org.wso2.siddhi.core.query.selector.attribute.aggregator.incremental;

import org.wso2.siddhi.core.executor.ExpressionExecutor;

import java.util.ArrayList;
import java.util.List;

/**
 * Store for maintaining the base values related to incremental aggregation. (e.g. for average,
 * the base incremental values would be sum and count. The timestamp too is stored here.
 */
public class BaseIncrementalValueStore {
    private long timestamp; // This is the starting timeStamp of aggregates
    private Object[] values;
    private List<ExpressionExecutor> expressionExecutors;
    private boolean isProcessed = false;

    public BaseIncrementalValueStore(long timeStamp, List<ExpressionExecutor> expressionExecutors) {
        this.timestamp = timeStamp;
        this.values = new Object[expressionExecutors.size() + 1];
        this.expressionExecutors = expressionExecutors;
    }

    public void clearValues() {
        this.values = new Object[expressionExecutors.size() + 1];
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setValue(Object value, int position) {
        values[position] = value;
    }

    public void setProcessed(boolean isProcessed) {
        this.isProcessed = isProcessed;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Object[] getValues() {
        return values;
    }

    public List<ExpressionExecutor> getExpressionExecutors() {
        return expressionExecutors;
    }

    public boolean isProcessed() {
        return isProcessed;
    }

    public BaseIncrementalValueStore cloneStore(String key, long timestamp) {
        List<ExpressionExecutor> newExpressionExecutors = new ArrayList<>(expressionExecutors.size());
        expressionExecutors
                .forEach(expressionExecutor -> newExpressionExecutors.add(expressionExecutor.cloneExecutor(key)));
        return new BaseIncrementalValueStore(timestamp, newExpressionExecutors);
    }

}
