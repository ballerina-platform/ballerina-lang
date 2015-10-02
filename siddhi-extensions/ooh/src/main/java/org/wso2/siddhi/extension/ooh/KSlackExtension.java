/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.siddhi.extension.ooh;

import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.event.stream.populater.ComplexEventPopulater;
import org.wso2.siddhi.core.exception.ExecutionPlanCreationException;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.query.processor.Processor;
import org.wso2.siddhi.core.query.processor.stream.StreamProcessor;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.Attribute;

import java.util.*;

/**
 * The following code conducts reordering of an out-of-order event stream.
 * This implements the K-Slack based disorder handling algorithm which was originally described in
 * https://www2.informatik.uni-erlangen.de/publication/download/IPDPS2013.pdf
 */
public class KSlackExtension extends StreamProcessor {
    private long k = 0; //In the beginning the K is zero.
    private long greatestTimestamp = 0; //Used to track the greatest timestamp of tuples seen so far in the stream history.
    private TreeMap<Long, ArrayList<StreamEvent>> eventTreeMap;
    private TreeMap<Long, ArrayList<StreamEvent>> expiredEventTreeMap;
    private ExpressionExecutor timestampExecutor;

    @Override
    public void start() {
        //Do nothing
    }

    @Override
    public void stop() {
        //Do nothing
    }

    @Override
    public Object[] currentState() {
        return new Object[0];
    }

    @Override
    public void restoreState(Object[] state) {
        //Do nothing
    }

    @Override
    protected void process(ComplexEventChunk<StreamEvent> streamEventChunk, Processor nextProcessor,
                           StreamEventCloner streamEventCloner, ComplexEventPopulater complexEventPopulater) {
        ComplexEventChunk<StreamEvent> complexEventChunk = new ComplexEventChunk<StreamEvent>();

        try {
            while (streamEventChunk.hasNext()) {
                StreamEvent event = streamEventChunk.next();
                streamEventChunk.remove(); //We might have the rest of the events linked to this event forming a chain.

                long timestamp = (Long) timestampExecutor.execute(event);

                ArrayList<StreamEvent> eventList = eventTreeMap.get(timestamp);
                if (eventList == null) {
                    eventList = new ArrayList<StreamEvent>();
                }
                eventList.add(event);
                eventTreeMap.put(timestamp, eventList);

                if (timestamp > greatestTimestamp) {

                    greatestTimestamp = timestamp;
                    long minTimestamp = eventTreeMap.firstKey();

                    if ((greatestTimestamp - minTimestamp) > k) {
                        k = greatestTimestamp - minTimestamp;
                    }

                    Iterator<Map.Entry<Long, ArrayList<StreamEvent>>> entryIterator = eventTreeMap.entrySet().iterator();
                    while (entryIterator.hasNext()) {
                        Map.Entry<Long, ArrayList<StreamEvent>> entry = entryIterator.next();
                        ArrayList<StreamEvent> list = expiredEventTreeMap.get(entry.getKey());
                        if (list != null) {
                            list.addAll(entry.getValue());
                        } else {
                            expiredEventTreeMap.put(entry.getKey(), entry.getValue());
                        }
                    }
                    eventTreeMap = new TreeMap<Long, ArrayList<StreamEvent>>();

                    entryIterator = expiredEventTreeMap.entrySet().iterator();
                    while (entryIterator.hasNext()) {
                        Map.Entry<Long, ArrayList<StreamEvent>> entry = entryIterator.next();

                        if (entry.getKey() + k <= greatestTimestamp) {
                            entryIterator.remove();
                            ArrayList<StreamEvent> timeEventList = entry.getValue();

                            for (StreamEvent aTimeEventList : timeEventList) {
                                complexEventChunk.add(aTimeEventList);
                            }
                        }
                    }
                }
            }
        } catch (ArrayIndexOutOfBoundsException ec) {
            //This happens due to user specifying an invalid field index.
            throw new ExecutionPlanCreationException("The very first parameter must be an Integer with a valid field index (0 to (fieldsLength-1)).");
        }

        nextProcessor.process(complexEventChunk);
    }

    @Override
    protected List<Attribute> init(AbstractDefinition inputDefinition, ExpressionExecutor[] attributeExpressionExecutors,
                                   ExecutionPlanContext executionPlanContext) {

        if (attributeExpressionLength == 0) {
            throw new ExecutionPlanCreationException("At least one parameter is required specifying the timestamp field having long return type.");
        }
        if (attributeExpressionLength > 1) {
            throw new ExecutionPlanCreationException("Only one parameter is required for KSlack that's timestamp field having long return type, but found " + attributeExpressionLength);
        }

        if (attributeExpressionExecutors[0].getReturnType() == Attribute.Type.LONG) {
            timestampExecutor = attributeExpressionExecutors[0];
        } else {
            throw new ExecutionPlanCreationException("Return type expected by KSlack is LONG but found " + attributeExpressionExecutors[0].getReturnType());
        }

        eventTreeMap = new TreeMap<Long, ArrayList<StreamEvent>>();
        expiredEventTreeMap = new TreeMap<Long, ArrayList<StreamEvent>>();
        ArrayList<Attribute> attributes = new ArrayList<Attribute>();
        attributes.add(new Attribute("beta0", Attribute.Type.INT));

        return attributes;
    }
}
