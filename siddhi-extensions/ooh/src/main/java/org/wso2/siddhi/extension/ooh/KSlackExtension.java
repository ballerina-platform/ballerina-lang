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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

/**
 * The following code conducts reordering of an out-of-order event stream.
 * This implements the K-Slack based disorder handling algorithm which was originally described in
 * https://www2.informatik.uni-erlangen.de/publication/download/IPDPS2013.pdf
 */
public class KSlackExtension extends StreamProcessor {
    private long k = 0; //In the beginning the K is zero.
    private long greatestTimestamp = 0; //Used to track the greatest timestamp of tuples seen so far in the stream history.
    private ArrayList<StreamEvent> expiredEventBuffer;
    private ArrayList<StreamEvent> eventBuffer;
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
                //To break such occurrences we call remove()
                long timestamp = (Long) timestampExecutor.execute(event);

                //The variable greatestTimestamp is used to track the greatest timestamp of tuples seen so far in the stream history.
                if (timestamp > greatestTimestamp) {
                    eventBuffer.add(event);
                    greatestTimestamp = timestamp;
                    long minTimestamp = Long.MAX_VALUE;

                    for (StreamEvent aEvent : eventBuffer) {
                        timestamp = (Long) timestampExecutor.execute(aEvent);

                        if (timestamp < minTimestamp) {
                            minTimestamp = timestamp;
                        }
                    }

                    if ((greatestTimestamp - minTimestamp) > k) {
                        k = greatestTimestamp - minTimestamp;
                    }

                    ArrayList<StreamEvent> buff = new ArrayList<StreamEvent>();
                    buff.addAll(eventBuffer);
                    buff.addAll(expiredEventBuffer);
                    expiredEventBuffer = new ArrayList<StreamEvent>();
                    //Below we need to have an ArrayList of StreamEvents as value because we may have multiple events
                    //with same timestamp value. So we must makesure we retain all of those events in the Map.
                    TreeMap<Long, ArrayList<StreamEvent>> treeMapOutput = new TreeMap<Long, ArrayList<StreamEvent>>();

                    //The following loop should be optimized in future since for each new event, it passes through the
                    //entire event buffer.
                    for (StreamEvent aEvent : buff) {
                        timestamp = (Long) timestampExecutor.execute(aEvent);

                        if (timestamp + k <= greatestTimestamp) {
                            ArrayList<StreamEvent> eventList = treeMapOutput.get(timestamp);

                            if (eventList == null) {
                                eventList = new ArrayList<StreamEvent>();
                            }
                            eventList.add(aEvent);
                            treeMapOutput.put(timestamp, eventList);
                        } else {
                            expiredEventBuffer.add(aEvent);
                        }
                        //We need to rethink whether just using remove() would be sufficient here. It may remove all
                        //the events having a specific feature from the eventBuffer at once.
                        eventBuffer.remove(aEvent);
                    }
                    //At this point the size of the eventBuffer should be zero.
                    Iterator<ArrayList<StreamEvent>> itr = treeMapOutput.values().iterator();
                    while (itr.hasNext()) {
                        ArrayList<StreamEvent> eventList = itr.next();

                        Iterator<StreamEvent> itr2 = eventList.iterator();
                        while (itr2.hasNext()) {
                            complexEventChunk.add(itr2.next());
                        }
                    }
                } else {
                    eventBuffer.add(event);
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
            throw new ExecutionPlanCreationException("Only one parameter is required for KSlack that's timestamp field having long return type, but found " + attributeExpressionExecutors);
        }

        if (attributeExpressionExecutors[0].getReturnType() == Attribute.Type.LONG) {
            timestampExecutor = attributeExpressionExecutors[0];
        } else {
            throw new ExecutionPlanCreationException("Return type expected by KSlack is LONG but found " + attributeExpressionExecutors[0].getReturnType());
        }

        expiredEventBuffer = new ArrayList<StreamEvent>();
        eventBuffer = new ArrayList<StreamEvent>();
        ArrayList<Attribute> attributes = new ArrayList<Attribute>();
        attributes.add(new Attribute("beta0", Attribute.Type.INT));

        return attributes;
    }
}
