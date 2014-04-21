/*
*  Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
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
package org.wso2.siddhi.core.query.processor.transform;

import org.apache.log4j.Logger;
import org.wso2.siddhi.core.config.SiddhiContext;
import org.wso2.siddhi.core.event.AtomicEvent;
import org.wso2.siddhi.core.event.BundleEvent;
import org.wso2.siddhi.core.event.in.InEvent;
import org.wso2.siddhi.core.event.in.InListEvent;
import org.wso2.siddhi.core.event.in.InStream;
import org.wso2.siddhi.core.executor.expression.ExpressionExecutor;
import org.wso2.siddhi.core.extension.EternalReferencedHolder;
import org.wso2.siddhi.core.snapshot.SnapshotObject;
import org.wso2.siddhi.core.snapshot.Snapshotable;
import org.wso2.siddhi.core.query.MarkedElement;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.query.api.expression.Expression;

import java.util.List;

public abstract class TransformProcessor implements Snapshotable, MarkedElement, EternalReferencedHolder {
    static final Logger log = Logger.getLogger(TransformProcessor.class);
    protected String elementId;
    protected SiddhiContext siddhiContext;
    protected StreamDefinition inStreamDefinition;
    protected StreamDefinition outStreamDefinition;
    private List<ExpressionExecutor> expressionExecutors;
    private Expression[] parameters;


    public InStream process(AtomicEvent atomicEvent) {
        if (atomicEvent instanceof InEvent) {
            InStream output = processEvent((InEvent) atomicEvent);
            if (output != null) {
                return output;
            }
        } else {
            log.error("Un expected message type " + atomicEvent.getClass().getCanonicalName() + " for event " + atomicEvent);
        }
        return null;
    }

    public InStream process(BundleEvent bundleEvent) {
        if (bundleEvent instanceof InListEvent) {
            InStream output = processEvent((InListEvent) bundleEvent);
            if (output != null) {
                return output;
            }
        } else {
            log.error("Un expected message type " + bundleEvent.getClass().getCanonicalName() + " for event " + bundleEvent);
        }
        return null;
    }


    protected abstract InStream processEvent(InEvent event);

    protected abstract InStream processEvent(InListEvent listEvent);

    @Override
    public void setElementId(String elementId) {
        this.elementId = elementId;
    }

    @Override
    public String getElementId() {
        return elementId;
    }

    @Override
    public final SnapshotObject snapshot() {
        return new SnapshotObject(currentState());
    }

    protected abstract Object[] currentState();

    @Override
    public final void restore(SnapshotObject snapshotObject) {
        restoreState(snapshotObject.getData());
    }

    protected abstract void restoreState(Object[] data);

    public void setSiddhiContext(SiddhiContext siddhiContext) {
        this.siddhiContext = siddhiContext;
    }

    public void setInStreamDefinition(StreamDefinition streamDefinition) {
        this.inStreamDefinition = streamDefinition;
    }

    public StreamDefinition getInStreamDefinition() {
        return inStreamDefinition;
    }

    public StreamDefinition getOutStreamDefinition() {
        return outStreamDefinition;
    }

    public void initTransformProcessor() {
        init(parameters, expressionExecutors, inStreamDefinition, outStreamDefinition, elementId, siddhiContext);
    }

    protected abstract void init(Expression[] parameters, List<ExpressionExecutor> expressionExecutors, StreamDefinition inStreamDefinition, StreamDefinition outStreamDefinition, String elementId, SiddhiContext siddhiContext);


    public void setExpressionExecutors(List<ExpressionExecutor> expressionExecutors) {
        this.expressionExecutors = expressionExecutors;
    }

    public void setParameters(Expression[] parameters) {
        this.parameters = parameters;
    }
}
