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
import org.wso2.siddhi.core.event.state.StateEvent;
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
import org.wso2.siddhi.core.util.collection.operator.MatchingMetaStateHolder;
import org.wso2.siddhi.core.util.parser.OperatorParser;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.exception.ExecutionPlanValidationException;
import org.wso2.siddhi.query.api.expression.Expression;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UniqueExternalTimeBatchWindowProcessor extends WindowProcessor implements SchedulingProcessor, FindableProcessor {
    private Map<Object, StreamEvent> currentEvents = new HashMap<Object, StreamEvent>();
    private Map<Object, StreamEvent> expiredEvents = null;
    private StreamEvent resetEvent = null;
    private ExpressionExecutor timestampExpressionExecutor;
    private long timeToKeep;
    private long endTime = -1;
    private long startTime = 0;
    private boolean isStartTimeEnabled = false;
    private long schedulerTimeout = 0;
    private Scheduler scheduler;
    private long lastScheduledTime;
    private long lastCurrentEventTime;
    private boolean flushed = false;
    private boolean outputExpectsExpiredEvents;
    private boolean storeExpiredEvents = false;
    private VariableExpressionExecutor variableExpressionExecutor;

    @Override
    protected void init(ExpressionExecutor[] attributeExpressionExecutors, ExecutionPlanContext executionPlanContext, boolean outputExpectsExpiredEvents) {
        this.outputExpectsExpiredEvents = outputExpectsExpiredEvents;
        if (outputExpectsExpiredEvents) {
            this.expiredEvents = new HashMap<Object, StreamEvent>();
            this.storeExpiredEvents = true;
        }
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
        if (schedulerTimeout > 0) {
            if (expiredEvents == null) {
                this.expiredEvents = new HashMap<Object, StreamEvent>();
            }
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

            StreamEvent nextStreamEvent = streamEventChunk.getFirst();
            while (nextStreamEvent != null) {

                StreamEvent currStreamEvent = nextStreamEvent;
                nextStreamEvent = nextStreamEvent.getNext();

                if (currStreamEvent.getType() == ComplexEvent.Type.TIMER) {
                    if (lastScheduledTime <= currStreamEvent.getTimestamp()) {
                        // implies that there have not been any more events after this schedule has been done.
                        if (!flushed) {
                            flushToOutputChunk(streamEventCloner, complexEventChunks, lastCurrentEventTime, true);
                            flushed = true;
                        } else {
                            if (currentEvents.size() > 0) {
                                appendToOutputChunk(streamEventCloner, complexEventChunks, lastCurrentEventTime, true);
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
                        appendToOutputChunk(streamEventCloner, complexEventChunks, lastCurrentEventTime, false);
                        flushed = false;
                    } else {
                        flushToOutputChunk(streamEventCloner, complexEventChunks, lastCurrentEventTime, false);
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

    private void flushToOutputChunk(StreamEventCloner streamEventCloner, List<ComplexEventChunk<StreamEvent>> complexEventChunks,
                                    long currentTime, boolean preserveCurrentEvents) {
        ComplexEventChunk<StreamEvent> newEventChunk = new ComplexEventChunk<StreamEvent>(true);

        if (outputExpectsExpiredEvents) {
            if (expiredEvents.size() > 0) {
                // mark the timestamp for the expiredType event
                for (StreamEvent expiredEvent : expiredEvents.values()) {
                    expiredEvent.setTimestamp(currentTime);
                    // add expired event to newEventChunk.
                    newEventChunk.add(expiredEvent);
                }
            }
        }
        if (expiredEvents != null) {
            expiredEvents.clear();
        }


        if (currentEvents.size() > 0) {

            // add reset event in front of current events
            resetEvent.setTimestamp(currentTime);
            newEventChunk.add(resetEvent);
            resetEvent = null;

            // move to expired events
            if (preserveCurrentEvents || storeExpiredEvents) {
                for (Map.Entry<Object, StreamEvent> currentEventSet : currentEvents.entrySet()) {
                    StreamEvent toExpireEvent = streamEventCloner.copyStreamEvent(currentEventSet.getValue());
                    toExpireEvent.setType(StreamEvent.Type.EXPIRED);
                    expiredEvents.put(currentEventSet.getKey(), toExpireEvent);
                }
            }

            for (StreamEvent currentEvent : currentEvents.values()) {
                // add current event to next processor
                newEventChunk.add(currentEvent);
            }

        }
        currentEvents.clear();

        if (newEventChunk.getFirst() != null) {
            complexEventChunks.add(newEventChunk);
        }
    }

    private void appendToOutputChunk(StreamEventCloner streamEventCloner, List<ComplexEventChunk<StreamEvent>> complexEventChunks,
                                     long currentTime, boolean preserveCurrentEvents) {
        ComplexEventChunk<StreamEvent> newEventChunk = new ComplexEventChunk<StreamEvent>(true);
        Map<Object, StreamEvent> currentEventSet = new HashMap<Object, StreamEvent>();

        if (currentEvents.size() > 0) {

            if (expiredEvents.size() > 0) {
                // mark the timestamp for the expiredType event
                for (Map.Entry<Object, StreamEvent> expiredEventEntry : expiredEvents.entrySet()) {
                    if (outputExpectsExpiredEvents) {
                        // add expired event to newEventChunk.
                        StreamEvent toExpireEvent = streamEventCloner.copyStreamEvent(expiredEventEntry.getValue());
                        toExpireEvent.setTimestamp(currentTime);
                        newEventChunk.add(toExpireEvent);
                    }

                    StreamEvent toSendEvent = streamEventCloner.copyStreamEvent(expiredEventEntry.getValue());
                    toSendEvent.setType(ComplexEvent.Type.CURRENT);
                    currentEventSet.put(expiredEventEntry.getKey(), toSendEvent);
                }
            }

            // add reset event in front of current events
            StreamEvent toResetEvent = streamEventCloner.copyStreamEvent(resetEvent);
            toResetEvent.setTimestamp(currentTime);
            newEventChunk.add(toResetEvent);


            for (Map.Entry<Object, StreamEvent> currentEventEntry : currentEvents.entrySet()) {
                // move to expired events
                if (preserveCurrentEvents || storeExpiredEvents) {
                    StreamEvent toExpireEvent = streamEventCloner.copyStreamEvent(currentEventEntry.getValue());
                    toExpireEvent.setType(StreamEvent.Type.EXPIRED);
                    expiredEvents.put(currentEventEntry.getKey(), toExpireEvent);
                }
                currentEventSet.put(currentEventEntry.getKey(), currentEventEntry.getValue());
            }

            for (StreamEvent currentEventEntry : currentEventSet.values()) {
                newEventChunk.add(currentEventEntry);
            }
        }
        currentEvents.clear();

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
        if (resetEvent == null) {
            resetEvent = streamEventCloner.copyStreamEvent(currStreamEvent);
            resetEvent.setType(ComplexEvent.Type.RESET);
        }
    }

    public void start() {
        //Do nothing
    }

    public void stop() {
        //Do nothing
    }

    public Object[] currentState() {
        return new Object[]{currentEvents, currentEvents != null ? currentEvents : null, resetEvent, endTime, startTime, lastScheduledTime, lastCurrentEventTime, flushed};
    }

    public void restoreState(Object[] state) {
        currentEvents = (Map<Object, StreamEvent>) state[0];
        if (state[1] != null) {
            expiredEvents = (Map<Object, StreamEvent>) state[1];
        } else {
            expiredEvents = null;
        }
        resetEvent = (StreamEvent) state[2];
        endTime = (Long) state[3];
        startTime = (Long) state[4];
        lastScheduledTime = (Long) state[5];
        lastCurrentEventTime = (Long) state[6];
        flushed = (Boolean) state[7];
    }

    @Override
    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public Scheduler getScheduler() {
        return this.scheduler;
    }

    @Override
    public synchronized StreamEvent find(StateEvent matchingEvent, Finder finder) {
        return finder.find(matchingEvent, expiredEvents, streamEventCloner);
    }

    @Override
    public Finder constructFinder(Expression expression, MatchingMetaStateHolder matchingMetaStateHolder, ExecutionPlanContext executionPlanContext, List<VariableExpressionExecutor> variableExpressionExecutors, Map<String, EventTable> eventTableMap) {
        if (expiredEvents == null) {
            expiredEvents = new HashMap<Object, StreamEvent>();
            storeExpiredEvents = true;
        }
        return OperatorParser.constructOperator(expiredEvents, expression, matchingMetaStateHolder,executionPlanContext,variableExpressionExecutors,eventTableMap);
    }
}
