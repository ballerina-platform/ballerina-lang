/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.siddhi.core.query.processor.window;

import org.apache.log4j.Logger;
import org.wso2.siddhi.core.config.SiddhiContext;
import org.wso2.siddhi.core.event.AtomicEvent;
import org.wso2.siddhi.core.event.StreamEvent;
import org.wso2.siddhi.core.event.in.InEvent;
import org.wso2.siddhi.core.event.in.InListEvent;
import org.wso2.siddhi.core.event.remove.RemoveEvent;
import org.wso2.siddhi.core.event.remove.RemoveListEvent;
import org.wso2.siddhi.core.query.QueryPostProcessingElement;
import org.wso2.siddhi.core.util.EventConverter;
import org.wso2.siddhi.core.util.collection.queue.SiddhiQueue;
import org.wso2.siddhi.core.util.collection.queue.SiddhiQueueGrid;
import org.wso2.siddhi.core.util.collection.queue.scheduler.timestamp.SchedulerTimestampSiddhiQueueGrid;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.expression.Variable;
import org.wso2.siddhi.query.api.expression.constant.IntConstant;
import org.wso2.siddhi.query.api.expression.constant.LongConstant;

import java.util.Iterator;

public class ExternalTimeWindowProcessor extends WindowProcessor {

    static final Logger log = Logger.getLogger(ExternalTimeWindowProcessor.class);
    private long timeToKeep;
    private SiddhiQueue<StreamEvent> window;
    private String timeStampAttributeName;
    private int timeStampAttributePosition;

    @Override
    protected void init(Expression[] parameters, QueryPostProcessingElement nextProcessor, AbstractDefinition streamDefinition, String elementId, boolean async, SiddhiContext siddhiContext) {
        if (parameters[1] instanceof IntConstant) {
            timeToKeep = ((IntConstant) parameters[1]).getValue();
        } else {
            timeToKeep = ((LongConstant) parameters[1]).getValue();
        }
        timeStampAttributeName = ((Variable) parameters[0]).getAttributeName();
        timeStampAttributePosition = definition.getAttributePosition(timeStampAttributeName);

        if (this.siddhiContext.isDistributedProcessingEnabled()) {
            window = new SiddhiQueueGrid<StreamEvent>(elementId, this.siddhiContext, this.async);
        } else {
            window = new SiddhiQueue<StreamEvent>();
        }
    }

    @Override
    public void processEvent(InEvent event) {
        acquireLock();
        try {
            long currentTime = (Long) event.getData(timeStampAttributePosition);
            removeExpiredEvent(currentTime);
            window.put(new RemoveEvent(event, currentTime + timeToKeep));
            nextProcessor.process(event);
        } finally {
            releaseLock();
        }
    }

    private void removeExpiredEvent(long currentTime) {
        while (true) {
            RemoveEvent expiredEvent = (RemoveEvent) window.peek();
            if (expiredEvent != null && expiredEvent.getExpiryTime() < currentTime) {
                nextProcessor.process((AtomicEvent) window.poll());
            } else {
                break;
            }
        }
    }


    @Override
    public void processEvent(InListEvent listEvent) {
        acquireLock();
        try {
            long currentTime = (Long) listEvent.getEvent(listEvent.getActiveEvents() - 1).getData(timeStampAttributePosition);
            removeExpiredEvent(currentTime);
            long expireTime = currentTime + timeToKeep;
            if (!async && siddhiContext.isDistributedProcessingEnabled()) {
                for (int i = 0, activeEvents = listEvent.getActiveEvents(); i < activeEvents; i++) {
                    window.put(new RemoveEvent(listEvent.getEvent(i), expireTime));
                }
            } else {
                window.put(new RemoveListEvent(EventConverter.toRemoveEventArray(listEvent.getEvents(), listEvent.getActiveEvents(), expireTime)));
            }
            nextProcessor.process(listEvent);
        } finally {
            releaseLock();
        }
    }

    @Override
    public Iterator<StreamEvent> iterator() {
        return window.iterator();
    }

    @Override
    public Iterator<StreamEvent> iterator(String predicate) {
        if (siddhiContext.isDistributedProcessingEnabled()) {
            return ((SchedulerTimestampSiddhiQueueGrid<StreamEvent>) window).iterator(predicate);
        } else {
            return window.iterator();
        }
    }


    @Override
    protected Object[] currentState() {
        return window.currentState();
    }

    @Override
    protected void restoreState(Object[] data) {
        window.restoreState(data);
    }

    @Override
    public void destroy() {

    }
}

