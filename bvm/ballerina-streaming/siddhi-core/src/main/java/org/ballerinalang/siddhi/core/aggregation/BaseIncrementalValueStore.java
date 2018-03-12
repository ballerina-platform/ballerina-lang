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

package org.ballerinalang.siddhi.core.aggregation;

import org.ballerinalang.siddhi.core.config.SiddhiAppContext;
import org.ballerinalang.siddhi.core.event.stream.StreamEvent;
import org.ballerinalang.siddhi.core.event.stream.StreamEventPool;
import org.ballerinalang.siddhi.core.executor.ExpressionExecutor;
import org.ballerinalang.siddhi.core.util.snapshot.Snapshotable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Store for maintaining the base values related to incremental aggregation. (e.g. for average,
 * the base incremental values would be sum and count. The timestamp too is stored here.
 */
public class BaseIncrementalValueStore implements Snapshotable {
    private long timestamp; // This is the starting timeStamp of aggregates
    private Object[] values;
    private List<ExpressionExecutor> expressionExecutors;
    private boolean isProcessed = false;
    private StreamEventPool streamEventPool;
    private String elementId;
    private SiddhiAppContext siddhiAppContext;
    private String aggregatorName;

    public BaseIncrementalValueStore(long timeStamp, List<ExpressionExecutor> expressionExecutors,
                                     StreamEventPool streamEventPool,
                                     SiddhiAppContext siddhiAppContext, String aggregatorName) {
        this.timestamp = timeStamp;
        this.values = new Object[expressionExecutors.size() + 1];
        this.expressionExecutors = expressionExecutors;
        this.streamEventPool = streamEventPool;
        this.siddhiAppContext = siddhiAppContext;
        this.aggregatorName = aggregatorName;
        if (elementId == null) {
            elementId = "IncrementalBaseStore-" + siddhiAppContext.getElementIdGenerator().createNewId();
        }
        siddhiAppContext.getSnapshotService().addSnapshotable(aggregatorName, this);
    }

    public void clearValues() {
        this.values = new Object[expressionExecutors.size() + 1];
    }

    public void setValue(Object value, int position) {
        values[position] = value;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public List<ExpressionExecutor> getExpressionExecutors() {
        return expressionExecutors;
    }

    public boolean isProcessed() {
        return isProcessed;
    }

    public void setProcessed(boolean isProcessed) {
        this.isProcessed = isProcessed;
    }

    public StreamEvent createStreamEvent() {
        StreamEvent streamEvent = streamEventPool.borrowEvent();
        streamEvent.setTimestamp(timestamp);
        setValue(timestamp, 0);
        streamEvent.setOutputData(values);
        return streamEvent;
    }

    public BaseIncrementalValueStore cloneStore(String key, long timestamp) {
        List<ExpressionExecutor> newExpressionExecutors = new ArrayList<>(expressionExecutors.size());
        expressionExecutors
                .forEach(expressionExecutor -> newExpressionExecutors.add(expressionExecutor.cloneExecutor(key)));
        return new BaseIncrementalValueStore(timestamp, newExpressionExecutors, streamEventPool, siddhiAppContext,
                aggregatorName);

    }

    @Override
    public Map<String, Object> currentState() {
        Map<String, Object> state = new HashMap<>();
        state.put("Timestamp", timestamp);
        state.put("Values", values);
        state.put("IsProcessed", isProcessed);
        return state;
    }

    @Override
    public void restoreState(Map<String, Object> state) {
        timestamp = (long) state.get("Timestamp");
        values = (Object[]) state.get("Values");
        isProcessed = (boolean) state.get("IsProcessed");
    }

    @Override
    public String getElementId() {
        return elementId;
    }
}
