/*
 * Copyright (c) 2005 - 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.wso2.siddhi.core.query.output.rateLimit;

import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.extension.EternalReferencedHolder;
import org.wso2.siddhi.core.query.output.callback.OutputCallback;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.util.snapshot.Snapshotable;

import java.util.ArrayList;
import java.util.List;


public abstract class OutputRateLimiter implements EternalReferencedHolder, Snapshotable{

    protected List<QueryCallback> queryCallbacks = new ArrayList<QueryCallback>();
    protected OutputCallback outputCallback = null;
    private boolean hasCallBack = false;
    private String elementId;

    public void init(ExecutionPlanContext executionPlanContext){
        if (elementId == null) {
            elementId = executionPlanContext.getElementIdGenerator().createNewId();
        }
        executionPlanContext.getSnapshotService().addSnapshotable(this);
    }

    protected void sendToCallBacks(ComplexEventChunk complexEventChunk) {
        if (outputCallback != null && complexEventChunk.getFirst()!=null) {
            outputCallback.send(complexEventChunk);
        }
        if (!queryCallbacks.isEmpty()) {
            for (QueryCallback callback : queryCallbacks) {
                callback.receiveStreamEvent(complexEventChunk);
            }
        }
    }

    public void addQueryCallback(QueryCallback callback) {
        queryCallbacks.add(callback);
        hasCallBack = true;
    }

    public void setOutputCallback(OutputCallback outputCallback) {
        this.outputCallback = outputCallback;
        if (outputCallback != null) {
            hasCallBack = true;
        }
    }

    public abstract void process(ComplexEventChunk complexEventChunk);

    public abstract void add(ComplexEvent complexEvent);

    public OutputCallback getOutputCallback() {
        return outputCallback;
    }

    public boolean hasCallBack() {
        return hasCallBack;
    }

    public abstract OutputRateLimiter clone(String key);

    public String getElementId() {
        return elementId;
    }
}

