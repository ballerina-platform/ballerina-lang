/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.siddhi.core.query.processor.stream.window;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.MetaComplexEvent;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.executor.ConstantExpressionExecutor;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.executor.VariableExpressionExecutor;
import org.wso2.siddhi.core.query.processor.Processor;
import org.wso2.siddhi.core.table.EventTable;
import org.wso2.siddhi.core.util.collection.operator.Finder;
import org.wso2.siddhi.core.util.parser.CollectionOperatorParser;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.exception.ExecutionPlanValidationException;
import org.wso2.siddhi.query.api.expression.Expression;

/**
 * @since Dec 23, 2015
 *
 */

public class ExternalTimeBatchWindowProcessor extends WindowProcessor implements FindableProcessor {
    
    private long timeToKeep;

    private ComplexEventChunk<StreamEvent> currentEventChunk = new ComplexEventChunk<StreamEvent>();
    private ComplexEventChunk<StreamEvent> expiredEventChunk = new ComplexEventChunk<StreamEvent>();
    
    static final Logger log = Logger.getLogger(ExternalTimeBatchWindowProcessor.class);
    private VariableExpressionExecutor timeStampVariableExpressionExecutor;

    private long lastSendTime = -1;

    @Override
    protected void init(ExpressionExecutor[] attributeExpressionExecutors, ExecutionPlanContext executionPlanContext) {
        this.expiredEventChunk = new ComplexEventChunk<StreamEvent>();
        if (attributeExpressionExecutors.length == 2) {
            if (attributeExpressionExecutors[1].getReturnType() == Attribute.Type.INT) {
                timeToKeep = Integer.parseInt(String.valueOf(((ConstantExpressionExecutor) attributeExpressionExecutors[1]).getValue()));
            } else {
                timeToKeep = Long.parseLong(String.valueOf(((ConstantExpressionExecutor) attributeExpressionExecutors[1]).getValue()));
            }
            if (!(attributeExpressionExecutors[0] instanceof VariableExpressionExecutor)) {
                throw new ExecutionPlanValidationException("ExternalTime window's 1st parameter timeStamp should be a type long stream attribute but found " + attributeExpressionExecutors[0].getClass());
            }
            timeStampVariableExpressionExecutor = ((VariableExpressionExecutor) attributeExpressionExecutors[0]);
            if (timeStampVariableExpressionExecutor.getReturnType() != Attribute.Type.LONG) {
                throw new ExecutionPlanValidationException("ExternalTime window's 1st parameter timeStamp should be type long, but found " + timeStampVariableExpressionExecutor.getReturnType());
            }
        } else {
            throw new ExecutionPlanValidationException("ExternalTime window should only have two parameter (<long> timeStamp, <int|long|time> windowTime), but found " + attributeExpressionExecutors.length + " input attributes");
        }
    }
    
    /**
     * Here an assumption is taken: 
     * Parameter: timestamp: The time which the window determines as current time and will act upon, 
     *              the value of this parameter should be monotonically increasing. 
     * from https://docs.wso2.com/display/CEP400/Inbuilt+Windows#InbuiltWindows-externalTime
     * 
     */
    @Override
    protected synchronized void process(ComplexEventChunk<StreamEvent> streamEventChunk, Processor nextProcessor, StreamEventCloner streamEventCloner) {
        // event incoming trigger process. No events means no action
        if (!streamEventChunk.hasNext()) {
            return;
        }

        // for window beginning, if window is empty, set lastSendTime to incomingChunk first.
        if (currentEventChunk.getFirst() == null && lastSendTime < 0) {
            lastSendTime = (Long) streamEventChunk.getFirst().getAttribute(timeStampVariableExpressionExecutor.getPosition());
        }

        while(streamEventChunk.hasNext()) {
            StreamEvent currStreamEvent = streamEventChunk.next();
            if (currStreamEvent.getType() != ComplexEvent.Type.CURRENT) {
                continue;
            }
            
            long currentTime = (Long) currStreamEvent.getAttribute(timeStampVariableExpressionExecutor.getPosition());
            if (currentTime < lastSendTime + timeToKeep) {
                cloneAppend(streamEventCloner, currStreamEvent);
            } else if (currentTime >= lastSendTime + timeToKeep) {
                flushCurentChunk(nextProcessor, streamEventCloner, currentTime);
                cloneAppend(streamEventCloner, currStreamEvent);
            }
        }
    }

    private void cloneAppend(StreamEventCloner streamEventCloner, StreamEvent currStreamEvent) {
        StreamEvent clonedStreamEvent = streamEventCloner.copyStreamEvent(currStreamEvent);
        currentEventChunk.add(clonedStreamEvent);
    }

    private void flushCurentChunk(Processor nextProcessor, StreamEventCloner streamEventCloner, long currentTime) {
        // need flush the currentEventChunk
        currentEventChunk.reset();
        ComplexEventChunk<StreamEvent> newEventChunk = new ComplexEventChunk<StreamEvent>();

        // mark the timestamp for the expiredType event
        while (expiredEventChunk.hasNext()) {
            StreamEvent expiredEvent = expiredEventChunk.next();
            expiredEvent.setTimestamp(currentTime);
        }
        // add expired event to newEventChunk too.
        if (expiredEventChunk.getFirst() != null) {
            newEventChunk.add(expiredEventChunk.getFirst());
        }
        
        // make current event chunk as expired in expiredChunk
        expiredEventChunk.clear();
        while (currentEventChunk.hasNext()) {
            StreamEvent currentEvent = currentEventChunk.next();
            StreamEvent toExpireEvent = streamEventCloner.copyStreamEvent(currentEvent);
            toExpireEvent.setType(StreamEvent.Type.EXPIRED);
            expiredEventChunk.add(toExpireEvent);
        }

        // add current event chunk to next processor
        if (currentEventChunk.getFirst() != null) {
            newEventChunk.add(currentEventChunk.getFirst());
        }
        currentEventChunk.clear();

        // update timestamp, call next processor
        lastSendTime = currentTime;
        if (newEventChunk.getFirst() != null) {
            nextProcessor.process(newEventChunk);
        }
    }

    public void start() {
        //Do nothing
    }

    public void stop() {
        //Do nothing
    }

    public Object[] currentState() {
        return new Object[]{currentEventChunk.getFirst(), expiredEventChunk.getFirst(), lastSendTime};
    }

    public void restoreState(Object[] state) {
        currentEventChunk.clear();
        currentEventChunk.add((StreamEvent) state[0]);
        expiredEventChunk.clear();
        expiredEventChunk.add((StreamEvent) state[1]);
        lastSendTime = (Long) state[2];
    }

    public synchronized StreamEvent find(ComplexEvent matchingEvent, Finder finder) {
        return finder.find(matchingEvent, expiredEventChunk, streamEventCloner);
    }

    public Finder constructFinder(Expression expression, MetaComplexEvent metaComplexEvent, ExecutionPlanContext executionPlanContext, List<VariableExpressionExecutor> variableExpressionExecutors, Map<String, EventTable> eventTableMap, int matchingStreamIndex, long withinTime) {
        return CollectionOperatorParser.parse(expression, metaComplexEvent, executionPlanContext, variableExpressionExecutors, eventTableMap, matchingStreamIndex, inputDefinition, withinTime);
    }

}
