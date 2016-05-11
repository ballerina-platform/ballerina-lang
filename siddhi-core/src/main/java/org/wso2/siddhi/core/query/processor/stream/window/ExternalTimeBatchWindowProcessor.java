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

public class ExternalTimeBatchWindowProcessor extends WindowProcessor implements SchedulingProcessor, FindableProcessor {
    private ComplexEventChunk<StreamEvent> currentEventChunk = new ComplexEventChunk<StreamEvent>();
    private ComplexEventChunk<StreamEvent> expiredEventChunk = new ComplexEventChunk<StreamEvent>();
    private VariableExpressionExecutor timeStampVariableExpressionExecutor;
    private long timeToKeep;
    private long lastSendTime = -1;
    private long endTime = 0;
    private long startTime = 0;
    private boolean isStartTimeEnabled = false;
    private long schedulerTimeout;
    private Scheduler scheduler;
    private long lastScheduledTime;
    private long lastCurrentEventTime;

    @Override
    protected void init(ExpressionExecutor[] attributeExpressionExecutors, ExecutionPlanContext executionPlanContext) {
        this.expiredEventChunk = new ComplexEventChunk<StreamEvent>();
        if (attributeExpressionExecutors.length == 2) {
            isStartTimeEnabled = false;
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
        } else if (attributeExpressionExecutors.length == 3 || attributeExpressionExecutors.length == 4) {
            isStartTimeEnabled = true;
            if (attributeExpressionExecutors[1].getReturnType() == Attribute.Type.INT) {
                timeToKeep = Integer.parseInt(String.valueOf(((ConstantExpressionExecutor) attributeExpressionExecutors[1]).getValue()));
            } else {
                timeToKeep = Long.parseLong(String.valueOf(((ConstantExpressionExecutor) attributeExpressionExecutors[1]).getValue()));
            }
            if (attributeExpressionExecutors[2].getReturnType() == Attribute.Type.INT) {
                startTime = Integer.parseInt(String.valueOf(((ConstantExpressionExecutor) attributeExpressionExecutors[2]).getValue()));
            } else {
                startTime = Long.parseLong(String.valueOf(((ConstantExpressionExecutor) attributeExpressionExecutors[2]).getValue()));
            }
            if (!(attributeExpressionExecutors[0] instanceof VariableExpressionExecutor)) {
                throw new ExecutionPlanValidationException("ExternalTime window's 1st parameter timeStamp should be a type long stream attribute but found " + attributeExpressionExecutors[0].getClass());
            }
            timeStampVariableExpressionExecutor = ((VariableExpressionExecutor) attributeExpressionExecutors[0]);
            if (timeStampVariableExpressionExecutor.getReturnType() != Attribute.Type.LONG) {
                throw new ExecutionPlanValidationException("ExternalTime window's 1st parameter timeStamp should be type long, but found " + timeStampVariableExpressionExecutor.getReturnType());
            }

            if (attributeExpressionExecutors.length == 4 ) {
                if (attributeExpressionExecutors[3].getReturnType() == Attribute.Type.INT) {
                    schedulerTimeout = Integer.parseInt(String.valueOf(((ConstantExpressionExecutor) attributeExpressionExecutors[3]).getValue()));
                } else {
                    schedulerTimeout = Long.parseLong(String.valueOf(((ConstantExpressionExecutor) attributeExpressionExecutors[3]).getValue()));
                }
            }
        } else {
            throw new ExecutionPlanValidationException("ExternalTime window should only have two/three parameter (<long> timeStamp, <int|long|time> windowTime (and <int> startTime) ), but found " + attributeExpressionExecutors.length + " input attributes");
        }
    }

    /**
     * Here an assumption is taken:
     * Parameter: timestamp: The time which the window determines as current time and will act upon,
     * the value of this parameter should be monotonically increasing.
     * from https://docs.wso2.com/display/CEP400/Inbuilt+Windows#InbuiltWindows-externalTime
     */
    @Override
    protected synchronized void process(ComplexEventChunk<StreamEvent> streamEventChunk, Processor nextProcessor, StreamEventCloner streamEventCloner) {
        if (!isStartTimeEnabled) {
            processWithoutStartTime(streamEventChunk, nextProcessor, streamEventCloner);
        } else {
            processWithStartTime(streamEventChunk, nextProcessor, streamEventCloner);
        }
    }

    private void processWithoutStartTime(ComplexEventChunk<StreamEvent> streamEventChunk, Processor nextProcessor, StreamEventCloner streamEventCloner) {
        // event incoming trigger process. No events means no action
        if (!streamEventChunk.hasNext()) {
            return;
        }

        // for window beginning, if window is empty, set lastSendTime to incomingChunk first.
        if (currentEventChunk.getFirst() == null && lastSendTime < 0) {
            lastSendTime = (Long) streamEventChunk.getFirst().getAttribute(timeStampVariableExpressionExecutor.getPosition());
            endTime = lastSendTime;
        }

        while (streamEventChunk.hasNext()) {
            StreamEvent currStreamEvent = streamEventChunk.next();
            if (currStreamEvent.getType() != ComplexEvent.Type.CURRENT) {
                continue;
            }

            long currentTime = (Long) currStreamEvent.getAttribute(timeStampVariableExpressionExecutor.getPosition());

            if (currentTime < endTime) {
                cloneAppend(streamEventCloner, currStreamEvent);
            } else if (currentTime >= endTime) {
                flushCurrentChunk(nextProcessor, streamEventCloner, currentTime);
                cloneAppend(streamEventCloner, currStreamEvent);
            }
        }
    }

    private void processWithStartTime(ComplexEventChunk<StreamEvent> streamEventChunk, Processor nextProcessor, StreamEventCloner streamEventCloner) {
        // event incoming trigger process. No events means no action
        if (!streamEventChunk.hasNext()) {
            return;
        }

        // for window beginning, if window is empty, set lastSendTime to incomingChunk first.
        if (currentEventChunk.getFirst() == null && lastSendTime < 0) {
            lastSendTime = (Long) streamEventChunk.getFirst().getAttribute(timeStampVariableExpressionExecutor.getPosition());
            endTime = addTimeShift(lastSendTime);
        }

        while (streamEventChunk.hasNext()) {

            StreamEvent currStreamEvent = streamEventChunk.next();

            if (currStreamEvent.getType() == ComplexEvent.Type.TIMER) {

                if (lastScheduledTime <= currStreamEvent.getTimestamp()) {
                    // implies that there have not been any more events after this schedule has been done.
                    flushCurrentChunk(nextProcessor, streamEventCloner, lastCurrentEventTime);

                    // rescheduling to emit the current batch after expiring it if no further events arrive.
                    lastScheduledTime = executionPlanContext.getTimestampGenerator().currentTime() + schedulerTimeout;
                    lastCurrentEventTime = lastCurrentEventTime + timeToKeep;
                    scheduler.notifyAt(lastScheduledTime);

                }
                continue;
            } else if (currStreamEvent.getType() != ComplexEvent.Type.CURRENT) {
                continue;
            }

            long currentTime = (Long) currStreamEvent.getAttribute(timeStampVariableExpressionExecutor.getPosition());

            if (currentTime < endTime) {

                cloneAppend(streamEventCloner, currStreamEvent);
            } else {
                flushCurrentChunk(nextProcessor, streamEventCloner, currentTime);
                cloneAppend(streamEventCloner, currStreamEvent);
                // triggering the last batch expiration.
                if (schedulerTimeout > 0) {
                    lastScheduledTime = executionPlanContext.getTimestampGenerator().currentTime() + schedulerTimeout;
                    lastCurrentEventTime = currentTime;
                    scheduler.notifyAt(lastScheduledTime);
                }

            }
        }
    }


    private long addTimeShift(long currentTime) {
        // returns the next emission time based on system clock round time values.
        long elapsedTimeSinceLastEmit = (currentTime - startTime) % timeToKeep;
        return (currentTime + (timeToKeep - elapsedTimeSinceLastEmit));
    }

    private void cloneAppend(StreamEventCloner streamEventCloner, StreamEvent currStreamEvent) {
        StreamEvent clonedStreamEvent = streamEventCloner.copyStreamEvent(currStreamEvent);
        currentEventChunk.add(clonedStreamEvent);
    }

    private void flushCurrentChunk(Processor nextProcessor, StreamEventCloner streamEventCloner, long currentTime) {
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
        endTime += timeToKeep;

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

    public Finder constructFinder(Expression expression, MetaComplexEvent matchingMetaComplexEvent, ExecutionPlanContext executionPlanContext, List<VariableExpressionExecutor> variableExpressionExecutors, Map<String, EventTable> eventTableMap, int matchingStreamIndex, long withinTime) {
        return CollectionOperatorParser.parse(expression, matchingMetaComplexEvent, executionPlanContext, variableExpressionExecutors, eventTableMap, matchingStreamIndex, inputDefinition, withinTime);
    }

    @Override
    public void setScheduler(Scheduler scheduler) {
       this.scheduler = scheduler;
    }

    @Override
    public Scheduler getScheduler() {
        return this.scheduler;
    }
}
