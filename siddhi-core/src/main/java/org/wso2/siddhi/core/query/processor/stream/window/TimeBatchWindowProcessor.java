/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
    private long nextEmitTime = -1;
    private ComplexEventChunk<StreamEvent> currentEventChunk = new ComplexEventChunk<StreamEvent>();
    private ComplexEventChunk<StreamEvent> expiredEventChunk = new ComplexEventChunk<StreamEvent>();
    private Scheduler scheduler;
    private ExecutionPlanContext executionPlanContext;
    private boolean isStartTimeEnabled = false;
    private long startTime = 0;

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
        this.expiredEventChunk = new ComplexEventChunk<StreamEvent>();
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
        } else if (attributeExpressionExecutors.length == 2) {
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
            // start time
            isStartTimeEnabled = true;
            if (attributeExpressionExecutors[1].getReturnType() == Attribute.Type.INT) {
                startTime = Integer.parseInt(String.valueOf(((ConstantExpressionExecutor) attributeExpressionExecutors[1]).getValue()));
            } else {
                startTime = Long.parseLong(String.valueOf(((ConstantExpressionExecutor) attributeExpressionExecutors[1]).getValue()));
            }
        } else {
            throw new ExecutionPlanValidationException("Time window should only have one or two parameters. (<int|long|time> windowTime), but found " + attributeExpressionExecutors.length + " input attributes");
        }
    }


    @Override
    protected void process(ComplexEventChunk<StreamEvent> streamEventChunk, Processor nextProcessor, StreamEventCloner streamEventCloner) {
        synchronized (this) {
            if (nextEmitTime == -1) {
                long currentTime = executionPlanContext.getTimestampGenerator().currentTime();
                if (isStartTimeEnabled) {
                    nextEmitTime = getNextEmitTime(currentTime) + timeInMilliSeconds;
                } else {
                    nextEmitTime = executionPlanContext.getTimestampGenerator().currentTime();
                }
                scheduler.notifyAt(nextEmitTime);
            }
            long currentTime = executionPlanContext.getTimestampGenerator().currentTime();
            boolean sendEvents;
            if (currentTime >= nextEmitTime) {
                nextEmitTime += timeInMilliSeconds;
                if (currentEventChunk.getFirst() != null || expiredEventChunk.getFirst() != null) {
                    scheduler.notifyAt(nextEmitTime);
                }
                sendEvents = true;
            } else {
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
            streamEventChunk.clear();
            if (sendEvents) {
                currentEventChunk.reset();
                while (expiredEventChunk.hasNext()) {
                    StreamEvent expiredEvent = expiredEventChunk.next();
                    expiredEvent.setTimestamp(currentTime);
                }
                if (expiredEventChunk.getFirst() != null) {
                    streamEventChunk.add(expiredEventChunk.getFirst());
                }
                expiredEventChunk.clear();
                while (currentEventChunk.hasNext()) {
                    StreamEvent currentEvent = currentEventChunk.next();
                    StreamEvent toExpireEvent = streamEventCloner.copyStreamEvent(currentEvent);
                    toExpireEvent.setType(StreamEvent.Type.EXPIRED);
                    expiredEventChunk.add(toExpireEvent);
                }
                if (currentEventChunk.getFirst() != null) {
                    streamEventChunk.add(currentEventChunk.getFirst());
                }
                currentEventChunk.clear();

            }
        }
        if (streamEventChunk.getFirst() != null) {
            nextProcessor.process(streamEventChunk);
        }
    }

    private long getNextEmitTime(long currentTime) {
        // returns the next emission time based on system clock round time values.
        long elapsedTimeSinceLastEmit = (currentTime - startTime) % timeInMilliSeconds;
        long emitTime = currentTime + (timeInMilliSeconds - elapsedTimeSinceLastEmit);
        return emitTime;
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
        return new Object[]{expiredEventChunk.getFirst()};
    }

    @Override
    public void restoreState(Object[] state) {
        expiredEventChunk.clear();
        expiredEventChunk.add((StreamEvent) state[0]);
    }

    @Override
    public synchronized StreamEvent find(ComplexEvent matchingEvent, Finder finder) {
        return finder.find(matchingEvent, expiredEventChunk, streamEventCloner);
    }

    @Override
    public Finder constructFinder(Expression expression, MetaComplexEvent matchingMetaComplexEvent, ExecutionPlanContext executionPlanContext, List<VariableExpressionExecutor> variableExpressionExecutors, Map<String, EventTable> eventTableMap, int matchingStreamIndex, long withinTime) {
        return CollectionOperatorParser.parse(expression, matchingMetaComplexEvent, executionPlanContext, variableExpressionExecutors, eventTableMap, matchingStreamIndex, inputDefinition, withinTime);
    }
}
