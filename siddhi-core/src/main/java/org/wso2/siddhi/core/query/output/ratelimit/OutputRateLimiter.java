/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.siddhi.core.query.output.ratelimit;

import org.wso2.siddhi.core.config.SiddhiAppContext;
import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.query.output.callback.InsertIntoStreamCallback;
import org.wso2.siddhi.core.query.output.callback.OutputCallback;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.util.extension.holder.EternalReferencedHolder;
import org.wso2.siddhi.core.util.lock.LockWrapper;
import org.wso2.siddhi.core.util.snapshot.Snapshotable;
import org.wso2.siddhi.core.util.statistics.LatencyTracker;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract parent implementation of Output Rate Limiting. Output Rate Limiting is used to throttle the output of
 * Siddhi queries based on various criteria.
 */
public abstract class OutputRateLimiter implements EternalReferencedHolder, Snapshotable {

    protected List<QueryCallback> queryCallbacks = new ArrayList<QueryCallback>();
    protected OutputCallback outputCallback = null;
    protected SiddhiAppContext siddhiAppContext;
    protected LatencyTracker latencyTracker;
    protected LockWrapper lockWrapper;
    protected String queryName;
    private boolean hasCallBack = false;
    private String elementId;

    public void init(SiddhiAppContext siddhiAppContext, LockWrapper lockWrapper, String queryName) {
        this.siddhiAppContext = siddhiAppContext;
        this.queryName = queryName;
        if (outputCallback != null && (outputCallback instanceof InsertIntoStreamCallback)) {
            this.lockWrapper = lockWrapper;
        }
        if (elementId == null) {
            elementId = "OutputRateLimiter-" + siddhiAppContext.getElementIdGenerator().createNewId();
        }
        siddhiAppContext.getSnapshotService().addSnapshotable(queryName, this);
    }

    protected void sendToCallBacks(ComplexEventChunk complexEventChunk) {
        if (latencyTracker != null) {
            latencyTracker.markOut();
        }
        if (lockWrapper != null) {
            lockWrapper.unlock();
        }
        if (!queryCallbacks.isEmpty()) {
            for (QueryCallback callback : queryCallbacks) {
                callback.receiveStreamEvent(complexEventChunk);
            }
        }
        if (outputCallback != null && complexEventChunk.getFirst() != null) {
            complexEventChunk.reset();
            while (complexEventChunk.hasNext()) {
                ComplexEvent complexEvent = complexEventChunk.next();
                if (complexEvent.getType() == ComplexEvent.Type.EXPIRED) {
                    complexEvent.setType(ComplexEvent.Type.CURRENT);
                } else if (complexEvent.getType() == ComplexEvent.Type.RESET) {
                    complexEventChunk.remove();
                }
            }
            if (complexEventChunk.getFirst() != null) {
                outputCallback.send(complexEventChunk);
            }
        }

    }

    public void addQueryCallback(QueryCallback callback) {
        queryCallbacks.add(callback);
        hasCallBack = true;
    }

    public abstract void process(ComplexEventChunk complexEventChunk);

    public OutputCallback getOutputCallback() {
        return outputCallback;
    }

    public void setOutputCallback(OutputCallback outputCallback) {
        this.outputCallback = outputCallback;
        if (outputCallback != null) {
            hasCallBack = true;
        }
    }

    public boolean hasCallBack() {
        return hasCallBack;
    }

    public abstract OutputRateLimiter clone(String key);

    public String getElementId() {
        return elementId;
    }

    public void setLatencyTracker(LatencyTracker latencyTracker) {
        this.latencyTracker = latencyTracker;
    }
}

