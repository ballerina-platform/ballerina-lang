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
package org.wso2.siddhi.core.query.processor.stream.window;

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
import org.wso2.siddhi.core.query.processor.SchedulingProcessor;
import org.wso2.siddhi.core.table.EventTable;
import org.wso2.siddhi.core.util.Scheduler;
import org.wso2.siddhi.core.util.collection.operator.Finder;
import org.wso2.siddhi.core.util.parser.CollectionOperatorParser;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.exception.ExecutionPlanValidationException;
import org.wso2.siddhi.query.api.expression.Expression;

import java.util.List;
import java.util.Map;

public class TimeBatchWindowProcessor extends WindowProcessor implements SchedulingProcessor, FindableProcessor {

    private long timeInMilliSeconds;
    private long lastSentTime;
    private ComplexEventChunk<StreamEvent> currentEventChunk = new ComplexEventChunk<StreamEvent>();
    private StreamEvent lastExpiredEvent =null;
    private Scheduler scheduler;
    private ExecutionPlanContext executionPlanContext;

    public void setTimeInMilliSeconds(long timeInMilliSeconds) {
        this.timeInMilliSeconds = timeInMilliSeconds;
    }

    @Override
    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public Scheduler getScheduler() {
        return scheduler;
    }

    @Override
    protected void init(ExpressionExecutor[] attributeExpressionExecutors, ExecutionPlanContext executionPlanContext) {
        this.executionPlanContext = executionPlanContext;
//        this.expiredEventChunk = new ComplexEventChunk<StreamEvent>();
        if (attributeExpressionExecutors.length == 1) {
            if (attributeExpressionExecutors[0] instanceof ConstantExpressionExecutor) {
                if (attributeExpressionExecutors[0].getReturnType() == Attribute.Type.INT) {
                    timeInMilliSeconds = (Integer) ((ConstantExpressionExecutor) attributeExpressionExecutors[0]).getValue();

                } else if (attributeExpressionExecutors[0].getReturnType() == Attribute.Type.LONG) {
                    timeInMilliSeconds = (Long) ((ConstantExpressionExecutor) attributeExpressionExecutors[0]).getValue();
                } else {
                    throw new ExecutionPlanValidationException("Time window's parameter attribute should be either int or long, but found " + attributeExpressionExecutors[0].getReturnType());
                }
            } else {
                throw new ExecutionPlanValidationException("Time window should have constant parameter attribute but found a dynamic attribute " + attributeExpressionExecutors[0].getClass().getCanonicalName());
            }
        } else {
            throw new ExecutionPlanValidationException("Time window should only have one parameter (timeInterval int), but found " + attributeExpressionExecutors.length + " input attributes");
        }
        lastSentTime = executionPlanContext.getTimestampGenerator().currentTime();
    }

    @Override
    protected synchronized void process(ComplexEventChunk<StreamEvent> streamEventChunk, Processor nextProcessor, StreamEventCloner streamEventCloner) {
        long currentTime = executionPlanContext.getTimestampGenerator().currentTime();
        boolean sendEvents;
        if (currentTime >= lastSentTime + timeInMilliSeconds) {
            lastSentTime = currentTime;
            if (currentEventChunk.getFirst() != null || lastExpiredEvent != null) {
                scheduler.notifyAt(lastSentTime + timeInMilliSeconds);
            }
            sendEvents = true;
        } else {
            scheduler.notifyAt(lastSentTime + timeInMilliSeconds);
            sendEvents = false;
        }

        while (streamEventChunk.hasNext()) {
            StreamEvent streamEvent = streamEventChunk.next();
            if (streamEvent.getType() != ComplexEvent.Type.CURRENT) {
                continue;
            }
            StreamEvent clonedStreamEvent = streamEventCloner.copyStreamEvent(streamEvent);
            currentEventChunk.add(clonedStreamEvent);
        }
        if (sendEvents) {
            currentEventChunk.reset();
            ComplexEventChunk<StreamEvent> newEventChunk = new ComplexEventChunk<StreamEvent>();
//            while (expiredEventChunk.hasNext()) {
//                StreamEvent expiredEvent = expiredEventChunk.next();
//                expiredEvent.setTimestamp(currentTime);
//            }
            if (lastExpiredEvent != null) {
                lastExpiredEvent.setTimestamp(currentTime);
                lastExpiredEvent.setType(ComplexEvent.Type.RESET);
                newEventChunk.add(lastExpiredEvent);
            }
            lastExpiredEvent=null;
            while (currentEventChunk.hasNext()) {
                StreamEvent currentEvent = currentEventChunk.next();
                StreamEvent toExpireEvent = streamEventCloner.copyStreamEvent(currentEvent);
                toExpireEvent.setType(StreamEvent.Type.EXPIRED);
//                expiredEventChunk.add(toExpireEvent);
                lastExpiredEvent=toExpireEvent;
            }
            if (currentEventChunk.getFirst() != null) {
                newEventChunk.add(currentEventChunk.getFirst());
            }
            currentEventChunk.clear();
            if (newEventChunk.getFirst() != null) {
                nextProcessor.process(newEventChunk);
            }
        }
    }

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
//        return new Object[]{expiredEventChunk};
        return null;
    }

    @Override
    public void restoreState(Object[] state) {
//        expiredEventChunk = (ComplexEventChunk<StreamEvent>) state[0];
    }

    @Override
    public synchronized StreamEvent find(ComplexEvent matchingEvent, Finder finder) {
//        return finder.find(matchingEvent, expiredEventChunk, streamEventCloner);
        return null;
    }

    @Override
    public Finder constructFinder(Expression expression, MetaComplexEvent metaComplexEvent, ExecutionPlanContext executionPlanContext, List<VariableExpressionExecutor> variableExpressionExecutors, Map<String, EventTable> eventTableMap, int matchingStreamIndex, long withinTime) {
        return CollectionOperatorParser.parse(expression, metaComplexEvent, executionPlanContext, variableExpressionExecutors, eventTableMap, matchingStreamIndex, inputDefinition, withinTime);
    }
}
