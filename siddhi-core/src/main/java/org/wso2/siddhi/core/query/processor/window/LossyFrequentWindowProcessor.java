/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
*/
package org.wso2.siddhi.core.query.processor.window;

import org.wso2.siddhi.core.config.SiddhiContext;
import org.wso2.siddhi.core.event.AtomicEvent;
import org.wso2.siddhi.core.event.StreamEvent;
import org.wso2.siddhi.core.event.in.InEvent;
import org.wso2.siddhi.core.event.in.InListEvent;
import org.wso2.siddhi.core.event.remove.RemoveEvent;
import org.wso2.siddhi.core.query.QueryPostProcessingElement;
import org.wso2.siddhi.core.util.collection.LossyCount;
import org.wso2.siddhi.core.util.collection.map.SiddhiMap;
import org.wso2.siddhi.core.util.collection.map.SiddhiMapGrid;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.expression.Variable;
import org.wso2.siddhi.query.api.expression.constant.DoubleConstant;
import org.wso2.siddhi.query.api.expression.constant.IntConstant;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class LossyFrequentWindowProcessor extends WindowProcessor {
    private SiddhiMap<LossyCount> countMap;
    private SiddhiMap<StreamEvent> map;
    private List<Integer> attributeIndexes;    // attribute indexes starts with 0

    private int totalCount = 0;
    private double currentBucketId=1;
    private double support;           // these will be initialize during init
    private double error;             // these will be initialize during init
    private double windowWidth;

    @Override
    protected void processEvent(InEvent event) {
        acquireLock();
        try {
            totalCount++;
            if (totalCount != 1) {
                currentBucketId = Math.ceil(totalCount / windowWidth);
            }
            StreamEvent oldEvent = map.put(generateKey(event), new RemoveEvent(event, Long.MAX_VALUE));
            if (oldEvent != null) {       // this event is already in the store
                countMap.put(generateKey(event), countMap.get(generateKey(event)).incrementCount());
            } else {
                //  This is a new event
                LossyCount lCount;
                lCount = new LossyCount(1, (int)currentBucketId - 1 );
                countMap.put(generateKey(event), lCount);

            }
            // calculating all the events in the system which match the
            // requirement provided by the user
            List<String> keys = new ArrayList<String>();
            keys.addAll(countMap.keySet());
            for (String key : keys) {
                LossyCount lossyCount = countMap.get(key);
                if (lossyCount.getCount() >= ((support - error) * totalCount)) {
                    // among the selected events, if the newly arrive event is there we mark it as an inEvent
                    if(key.equals(generateKey(event))) {
                        nextProcessor.process(event);
                    }
                }
            }
            if (totalCount % windowWidth == 0) {
                // its time to run the data-structure prune code
                keys = new ArrayList<String>();
                keys.addAll(countMap.keySet());
                for (String key : keys) {
                    LossyCount lossyCount = countMap.get(key);
                    if (lossyCount.getCount() + lossyCount.getBucketId() <= currentBucketId) {
                        log.info("Removing the Event: " + key + " from the window");
                        countMap.remove(key);
                        nextProcessor.process((AtomicEvent) map.remove(key));
                    }
                }
            }

        } finally {
            releaseLock();
        }
    }

    @Override
    protected void processEvent(InListEvent listEvent) {
        acquireLock();
        try {
            for (int i = 0; i < listEvent.getActiveEvents(); i++) {
                InEvent inEvent = (InEvent) listEvent.getEvent(i);
                processEvent(inEvent);
            }
        } catch (Throwable t) {
            System.out.println(t);
        } finally {
            releaseLock();
        }

    }

    @Override
    public Iterator<StreamEvent> iterator() {
        return map.iterator();
    }

    @Override
    public Iterator<StreamEvent> iterator(String predicate) {
        if (siddhiContext.isDistributedProcessingEnabled()) {
            return ((SiddhiMapGrid<StreamEvent>) map).iterator(predicate);
        } else {
            return map.iterator();
        }
    }

    @Override
    protected Object[] currentState() {
        return countMap.currentState();
    }

    @Override
    protected void restoreState(Object[] data) {
        countMap.restoreState(data);
    }

    @Override
    protected void init(Expression[] parameters, QueryPostProcessingElement nextProcessor, AbstractDefinition streamDefinition, String elementId, boolean async, SiddhiContext siddhiContext) {
        support = ((DoubleConstant) parameters[0]).getValue();
        if (parameters.length > 1) {
            error = ((DoubleConstant) parameters[1]).getValue();
        } else {
            error = support / 10; // recommended error is 10% of 20$ of support value;
        }
        if ((support > 1 || support < 0) || (error > 1 || error < 0)) {
            log.error("Wrong argument has provided, Error executing the window");
        }
        attributeIndexes = new ArrayList<Integer>();
        if (parameters.length > 2) {  // by-default all the attributes will be compared
            for (int i = 2; i < parameters.length; i++) {
                attributeIndexes.add(streamDefinition.getAttributePosition(((Variable) parameters[i]).getAttributeName()));
            }
        }
        if (attributeIndexes.size() == streamDefinition.getAttributeList().size()) {    // if all the attributes are configured or non-configured we compare all, so we ignore this index
            attributeIndexes.clear();
        }
        windowWidth = Math.ceil(1 / error);
        currentBucketId = 1;
        if (this.siddhiContext.isDistributedProcessingEnabled()) {
            countMap = new SiddhiMapGrid<LossyCount>(elementId, this.siddhiContext);
            map = new SiddhiMapGrid<StreamEvent>(elementId, this.siddhiContext);
        } else {
            countMap = new SiddhiMap<LossyCount>();
            map = new SiddhiMap<StreamEvent>();
        }
    }

    private String generateKey(InEvent event) {
        StringBuilder stringBuilder = new StringBuilder();
        if (attributeIndexes.size() == 0) {
            for (Object data : event.getData()) {
                stringBuilder.append(data);
            }
        } else {
            for (int i = 0; i < event.getData().length; i++) {
                if (attributeIndexes.contains(i))
                    stringBuilder.append(event.getData()[i]);
            }
        }
        return stringBuilder.toString();
    }

    @Override
    public void destroy() {

    }

}
