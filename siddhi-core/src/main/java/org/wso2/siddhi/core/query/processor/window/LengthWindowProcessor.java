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

import org.wso2.siddhi.core.config.SiddhiContext;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.event.StreamEvent;
import org.wso2.siddhi.core.event.in.InEvent;
import org.wso2.siddhi.core.event.in.InListEvent;
import org.wso2.siddhi.core.event.remove.RemoveEvent;
import org.wso2.siddhi.core.query.QueryPostProcessingElement;
import org.wso2.siddhi.core.util.collection.queue.ISiddhiQueue;
import org.wso2.siddhi.core.util.collection.queue.SiddhiQueue;
import org.wso2.siddhi.core.util.collection.queue.SiddhiQueueGrid;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.expression.constant.IntConstant;

import java.util.Iterator;

public class LengthWindowProcessor extends WindowProcessor {

    private int lengthToKeep;
    private ISiddhiQueue<StreamEvent> window;

    @Override
    protected void processEvent(InEvent event) {
        acquireLock();
        try {
            window.put(new RemoveEvent(event, System.currentTimeMillis()));
            if (window.size() > lengthToKeep) {
                nextProcessor.process((Event) window.poll());
            }
            nextProcessor.process(event);
        } finally {
            releaseLock();
        }

    }

    @Override
    protected void processEvent(InListEvent listEvent) {
        acquireLock();
        try {
            int toFullQueueSize = (lengthToKeep - window.size());
            if (listEvent.getActiveEvents() > toFullQueueSize) {
                InEvent[] newEvents = new InEvent[toFullQueueSize];
                int index = 0;
                for (int i = 0; i < listEvent.getActiveEvents(); i++) {
                    InEvent inEvent = (InEvent) listEvent.getEvent(i);
                    if (index < toFullQueueSize - 1) {
                        newEvents[index] = inEvent;
                        window.put(new RemoveEvent(inEvent, Long.MAX_VALUE));
                        index++;
                    } else if (index == toFullQueueSize - 1) {
                        newEvents[index] = inEvent;
                        window.put(new RemoveEvent(inEvent, Long.MAX_VALUE));
                        index++;
                        nextProcessor.process(new InListEvent(newEvents));
                    } else {
                        RemoveEvent removeEvent = (RemoveEvent) window.poll();
                        removeEvent.setExpiryTime(System.currentTimeMillis());
                        nextProcessor.process(removeEvent);
                        window.put(new RemoveEvent(inEvent, Long.MAX_VALUE));
                        nextProcessor.process(inEvent);
                    }
                }
            } else {
                for (int i = 0; i < listEvent.getActiveEvents(); i++) {
                    window.put(new RemoveEvent(listEvent.getEvent(i), Long.MAX_VALUE));
                }
                nextProcessor.process(listEvent);
            }
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
            return ((SiddhiQueueGrid<StreamEvent>) window).iterator(predicate);
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
    protected void init(Expression[] parameters, QueryPostProcessingElement nextProcessor, AbstractDefinition streamDefinition, String elementId, boolean async, SiddhiContext siddhiContext) {
        lengthToKeep = ((IntConstant) parameters[0]).getValue();

        if (this.siddhiContext.isDistributedProcessingEnabled()) {
            window = new SiddhiQueueGrid<StreamEvent>(elementId, this.siddhiContext, this.async);
        } else {
            window = new SiddhiQueue<StreamEvent>();
        }

    }

    @Override
    public void destroy(){

    }

}
