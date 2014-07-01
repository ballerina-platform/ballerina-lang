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
import org.wso2.siddhi.core.util.collection.map.SiddhiMap;
import org.wso2.siddhi.core.util.collection.map.SiddhiMapGrid;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.expression.Variable;
import org.wso2.siddhi.query.api.expression.constant.IntConstant;
import org.wso2.siddhi.query.api.expression.constant.StringConstant;

import java.util.*;

/**
 * This is the implementation of a counting algorithm based on
 * Misra-Gries counting algorithm
 */
public class FrequentWindowProcessor extends WindowProcessor {

    private SiddhiMap<Integer> countMap;
    private SiddhiMap<StreamEvent> map;
    private int mostFrqntCount;
    private List<Integer> attributeIndexes;    // attribute indexes starts with 0

    @Override
    protected void processEvent(InEvent event) {
        acquireLock();
        try {
            StreamEvent oldEvent = map.put(generateKey(event), new RemoveEvent(event, Long.MAX_VALUE));
            if (oldEvent != null) {       // this event is already in the store
                countMap.put(generateKey(event), countMap.get(generateKey(event)) + 1);
                nextProcessor.process(event);
            } else {
                //  This is a new event
                if (map.size() > mostFrqntCount) {
                    List<String> keys = new ArrayList<String>();
                    keys.addAll(countMap.keySet());
                    for (int i = 0; i < mostFrqntCount; i++) {
                        int count = countMap.get(keys.get(i)) - 1;
                        if (count == 0) {
                            countMap.remove(keys.get(i));
                            nextProcessor.process((AtomicEvent) map.remove(keys.get(i)));
                        } else {
                            countMap.put(keys.get(i), count);
                        }
                    }
                    // now we have tried to remove one for newly added item
                    if (map.size() > mostFrqntCount) {
                        //nothing happend by the attempt to remove one from the
                        // map so we are ignoring this event
                        map.remove(generateKey(event));
                        // Here we do nothing just drop the message
                    } else {
                        // we got some space, event is already there in map object
                        // we just have to add it to the countMap
                        countMap.put(generateKey(event), 1);
                        nextProcessor.process(event);
                    }

                } else {
                    countMap.put(generateKey(event), 1);
                    nextProcessor.process(event);
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
        mostFrqntCount = ((IntConstant) parameters[0]).getValue();
        attributeIndexes = new ArrayList<Integer>();
        if (parameters.length > 1) {  // by-default all the attributes will be compared
            for (int i = 1; i < parameters.length; i++) {
                attributeIndexes.add(streamDefinition.getAttributePosition(((Variable) parameters[i]).getAttributeName()));
            }
        }
        if (attributeIndexes.size() == streamDefinition.getAttributeList().size()) {    // if all the attributes are configured or non-configured we compare all, so we ignore this index
            attributeIndexes.clear();
        }

        if (this.siddhiContext.isDistributedProcessingEnabled()) {
            countMap = new SiddhiMapGrid<Integer>(elementId, this.siddhiContext);
            map = new SiddhiMapGrid<StreamEvent>(elementId, this.siddhiContext);
        } else {
            countMap = new SiddhiMap<Integer>();
            map = new SiddhiMap<StreamEvent>();
        }
    }

    private String generateKey(InEvent event) {      // for performance reason if its all attribute we don't do the attribute list check
        StringBuilder stringBuilder = new StringBuilder();
        if (attributeIndexes.size() == 0) {
            for(Object data: event.getData()) {
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
