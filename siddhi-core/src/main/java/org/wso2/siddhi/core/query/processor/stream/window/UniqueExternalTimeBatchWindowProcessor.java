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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UniqueExternalTimeBatchWindowProcessor extends WindowProcessor implements SchedulingProcessor, FindableProcessor {
    private Map<Object, StreamEvent> currentEvents = new HashMap<Object, StreamEvent>();
    private Map<Object, StreamEvent> expiredEvents = new HashMap<Object, StreamEvent>();
    private ComplexEventChunk<StreamEvent> duplicateEventChunk = new ComplexEventChunk<StreamEvent>(false);
    private ExpressionExecutor timestampExpressionExecutor;
    private long timeToKeep;
    private long endTime = -1;
    private long startTime = 0;
    private boolean isStartTimeEnabled = false;
    private long schedulerTimeout = -1;
    private Scheduler scheduler;
    private long lastScheduledTime;
    private long lastCurrentEventTime;
    private boolean flushed = false;
    private VariableExpressionExecutor variableExpressionExecutor;

    @Override
    protected void init(ExpressionExecutor[] attributeExpressionExecutors, ExecutionPlanContext executionPlanContext) {
        if (attributeExpressionExecutors.length >= 3 && attributeExpressionExecutors.length <= 5) {

            if (!(attributeExpressionExecutors[0] instanceof VariableExpressionExecutor)) {
                throw new ExecutionPlanValidationException("ExternalTime window's 1st parameter uniqueAttribute should be a variable, but found " + attributeExpressionExecutors[0].getClass());
            }
            variableExpressionExecutor = (VariableExpressionExecutor) attributeExpressionExecutors[0];

            if ((attributeExpressionExecutors[1] instanceof ConstantExpressionExecutor)) {
                throw new ExecutionPlanValidationException("ExternalTime window's 2nd parameter timestamp should not be a constant ");
            }
            if (attributeExpressionExecutors[1].getReturnType() != Attribute.Type.LONG) {
                throw new ExecutionPlanValidationException("ExternalTime window's 2nd parameter timestamp should be type long, but found " + attributeExpressionExecutors[1].getReturnType());
            }
            timestampExpressionExecutor = attributeExpressionExecutors[1];

            if (attributeExpressionExecutors[2].getReturnType() == Attribute.Type.INT) {
                timeToKeep = (Integer) ((ConstantExpressionExecutor) attributeExpressionExecutors[2]).getValue();
            } else if (attributeExpressionExecutors[2].getReturnType() == Attribute.Type.LONG) {
                timeToKeep = (Long) ((ConstantExpressionExecutor) attributeExpressionExecutors[2]).getValue();
            } else {
                throw new ExecutionPlanValidationException("ExternalTimeBatch window's 3rd parameter windowTime should be either int or long, but found " + attributeExpressionExecutors[2].getReturnType());
            }

            if (attributeExpressionExecutors.length >= 4) {
                isStartTimeEnabled = true;
                if (attributeExpressionExecutors[3].getReturnType() == Attribute.Type.INT) {
                    startTime = Integer.parseInt(String.valueOf(((ConstantExpressionExecutor) attributeExpressionExecutors[3]).getValue()));
                } else if (attributeExpressionExecutors[3].getReturnType() == Attribute.Type.LONG) {
                    startTime = Long.parseLong(String.valueOf(((ConstantExpressionExecutor) attributeExpressionExecutors[3]).getValue()));
                } else {
                    throw new ExecutionPlanValidationException("ExternalTimeBatch window's 4th parameter startTime should be either int or long, but found " + attributeExpressionExecutors[3].getReturnType());
                }
            }

            if (attributeExpressionExecutors.length == 5) {
                if (attributeExpressionExecutors[4].getReturnType() == Attribute.Type.INT) {
                    schedulerTimeout = Integer.parseInt(String.valueOf(((ConstantExpressionExecutor) attributeExpressionExecutors[4]).getValue()));
                } else if (attributeExpressionExecutors[4].getReturnType() == Attribute.Type.LONG) {
                    schedulerTimeout = Long.parseLong(String.valueOf(((ConstantExpressionExecutor) attributeExpressionExecutors[4]).getValue()));
                } else {
                    throw new ExecutionPlanValidationException("ExternalTimeBatch window's 5th parameter timeout should be either int or long, but found " + attributeExpressionExecutors[4].getReturnType());
                }
            }
        } else {
            throw new ExecutionPlanValidationException("ExternalTimeBatch window should only have three to five parameters (<variable> uniqueAttribute, <long> timestamp, <int|long|time> windowTime, <long> startTime, <int|long|time> timeout), but found " + attributeExpressionExecutors.length + " input attributes");
        }
    }

    /**
     * Here an assumption is taken:
     * Parameter: timestamp: The time which the window determines as current time and will act upon,
     * the value of this parameter should be monotonically increasing.
     * from https://docs.wso2.com/display/CEP400/Inbuilt+Windows#InbuiltWindows-externalTime
     */
    @Override
    protected void process(ComplexEventChunk<StreamEvent> streamEventChunk, Processor nextProcessor, StreamEventCloner streamEventCloner) {

        // event incoming trigger process. No events means no action
        if (streamEventChunk.getFirst() == null) {
            return;
        }

        List<ComplexEventChunk<StreamEvent>> complexEventChunks = new ArrayList<ComplexEventChunk<StreamEvent>>();
        synchronized (this) {
            initTiming(streamEventChunk.getFirst());

            StreamEvent streamEvent = streamEventChunk.getFirst();
            while (streamEvent != null) {

                StreamEvent currStreamEvent = streamEvent;
                streamEvent = streamEvent.getNext();

                if (currStreamEvent.getType() == ComplexEvent.Type.TIMER) {
                    if (lastScheduledTime <= currStreamEvent.getTimestamp()) {
                        // implies that there have not been any more events after this schedule has been done.
                        if (!flushed) {
                            flushToOutputChunk(streamEventCloner, complexEventChunks, lastCurrentEventTime);
                            flushed = true;
                        } else {
                            if (currentEvents.size() > 0) {
                                appendToOutputChunk(streamEventCloner, complexEventChunks);
                            }
                        }

                        // rescheduling to emit the current batch after expiring it if no further events arrive.
                        lastScheduledTime = executionPlanContext.getTimestampGenerator().currentTime() + schedulerTimeout;
                        scheduler.notifyAt(lastScheduledTime);
                    }
                    continue;
                } else if (currStreamEvent.getType() != ComplexEvent.Type.CURRENT) {
                    continue;
                }

                long currentEventTime = (Long) timestampExpressionExecutor.execute(currStreamEvent);
                if (lastCurrentEventTime < currentEventTime) {
                    lastCurrentEventTime = currentEventTime;
                }

                if (currentEventTime < endTime) {
                    cloneAppend(streamEventCloner, currStreamEvent);
                } else {
                    if (flushed) {
                        appendToOutputChunk(streamEventCloner, complexEventChunks);
                        flushed = false;
                    } else {
                        flushToOutputChunk(streamEventCloner, complexEventChunks, lastCurrentEventTime);
                    }
                    // update timestamp, call next processor
                    endTime = findEndTime(lastCurrentEventTime, startTime, timeToKeep);
                    cloneAppend(streamEventCloner, currStreamEvent);
                    // triggering the last batch expiration.
                    if (schedulerTimeout > 0) {
                        lastScheduledTime = executionPlanContext.getTimestampGenerator().currentTime() + schedulerTimeout;
                        scheduler.notifyAt(lastScheduledTime);
                    }
                }
            }
        }
        for (ComplexEventChunk<StreamEvent> complexEventChunk : complexEventChunks) {
            nextProcessor.process(complexEventChunk);
        }
    }

    private void initTiming(StreamEvent firstStreamEvent) {
        // for window beginning, if window is empty, set lastSendTime to incomingChunk first.
        if (endTime < 0) {
            if (isStartTimeEnabled) {
                endTime = findEndTime((Long) timestampExpressionExecutor.execute(firstStreamEvent), startTime, timeToKeep);
            } else {
                startTime = (Long) timestampExpressionExecutor.execute(firstStreamEvent);
                endTime = startTime + timeToKeep;
            }
            if (schedulerTimeout > 0) {
                lastScheduledTime = executionPlanContext.getTimestampGenerator().currentTime() + schedulerTimeout;
                scheduler.notifyAt(lastScheduledTime);
            }
        }
    }

    private void flushToOutputChunk(StreamEventCloner streamEventCloner, List<ComplexEventChunk<StreamEvent>> complexEventChunks, long currentTime) {
        ComplexEventChunk<StreamEvent> newEventChunk = new ComplexEventChunk<StreamEvent>(true);

        if (expiredEvents.size() > 0) {
            // mark the timestamp for the expiredType event
            for (StreamEvent expiredEvent : expiredEvents.values()) {
                expiredEvent.setTimestamp(currentTime);
                // add expired event to newEventChunk.
                newEventChunk.add(expiredEvent);
            }
            expiredEvents.clear();
        }

        if (currentEvents.size() > 0) {
            // need flush the currentEvents
            for (Map.Entry<Object, StreamEvent> currentEventSet : currentEvents.entrySet()) {
                StreamEvent toExpireEvent = streamEventCloner.copyStreamEvent(currentEventSet.getValue());
                toExpireEvent.setType(StreamEvent.Type.EXPIRED);
                StreamEvent duplicateEvent = expiredEvents.put(currentEventSet.getKey(), toExpireEvent);
                if (duplicateEvent != null) {
                    newEventChunk.add(duplicateEvent);
                }
                // add current event to next processor
                newEventChunk.add(currentEventSet.getValue());
            }
            currentEvents.clear();
        }

        if (newEventChunk.getFirst() != null) {
            complexEventChunks.add(newEventChunk);
        }
    }

    private void appendToOutputChunk(StreamEventCloner streamEventCloner, List<ComplexEventChunk<StreamEvent>> complexEventChunks) {
        ComplexEventChunk<StreamEvent> newEventChunk = new ComplexEventChunk<StreamEvent>(true);

        // add current event to newEventChunk.
        if (currentEvents.size() > 0) {
            // need flush the currentEvents
            for (Map.Entry<Object, StreamEvent> currentEventSet : currentEvents.entrySet()) {
                StreamEvent toExpireEvent = streamEventCloner.copyStreamEvent(currentEventSet.getValue());
                toExpireEvent.setType(StreamEvent.Type.EXPIRED);
                StreamEvent duplicateEvent = expiredEvents.put(currentEventSet.getKey(), toExpireEvent);
                if (duplicateEvent != null) {
                    newEventChunk.add(duplicateEvent);
                }
                // add current event to next processor
                newEventChunk.add(currentEventSet.getValue());
            }
            currentEvents.clear();
        }

        if (newEventChunk.getFirst() != null) {
            complexEventChunks.add(newEventChunk);
        }
    }

    private long findEndTime(long currentTime, long startTime, long timeToKeep) {
        // returns the next emission time based on system clock round time values.
        long elapsedTimeSinceLastEmit = (currentTime - startTime) % timeToKeep;
        return (currentTime + (timeToKeep - elapsedTimeSinceLastEmit));
    }

    private void cloneAppend(StreamEventCloner streamEventCloner, StreamEvent currStreamEvent) {
        StreamEvent clonedStreamEvent = streamEventCloner.copyStreamEvent(currStreamEvent);
        currentEvents.put(variableExpressionExecutor.execute(clonedStreamEvent), clonedStreamEvent);
    }

    public void start() {
        //Do nothing
    }

    public void stop() {
        //Do nothing
    }

    public Object[] currentState() {
        return new Object[]{currentEvents, expiredEvents, endTime, startTime, isStartTimeEnabled, schedulerTimeout};
    }

    public void restoreState(Object[] state) {
        currentEvents = (Map<Object, StreamEvent>) state[0];
        expiredEvents = (Map<Object, StreamEvent>) state[1];
        endTime = (Long) state[2];
        startTime = (Long) state[3];
        isStartTimeEnabled = (Boolean) state[4];
        schedulerTimeout = (Long) state[5];
    }

    public synchronized StreamEvent find(ComplexEvent matchingEvent, Finder finder) {
        return finder.find(matchingEvent, expiredEvents, streamEventCloner);
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
