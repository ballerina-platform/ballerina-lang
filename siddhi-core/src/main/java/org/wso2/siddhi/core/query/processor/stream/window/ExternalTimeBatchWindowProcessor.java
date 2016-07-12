/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.wso2.siddhi.core.util.parser.OperatorParser;
import org.wso2.siddhi.core.util.collection.operator.Finder;
import org.wso2.siddhi.core.util.collection.operator.MatchingMetaStateHolder;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.exception.ExecutionPlanValidationException;
import org.wso2.siddhi.query.api.expression.Expression;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ExternalTimeBatchWindowProcessor extends WindowProcessor implements SchedulingProcessor, FindableProcessor {
    private ComplexEventChunk<StreamEvent> currentEventChunk = new ComplexEventChunk<StreamEvent>(false);
    private ComplexEventChunk<StreamEvent> expiredEventChunk = null;
    private StreamEvent resetEvent = null;
    private ExpressionExecutor timestampExpressionExecutor;
    private ExpressionExecutor startTimeAsVariable;
    private long timeToKeep;
    private long endTime = -1;
    private long startTime;
    private boolean isStartTimeEnabled = false;
    private long schedulerTimeout = 0;
    private Scheduler scheduler;
    private long lastScheduledTime;
    private long lastCurrentEventTime;
    private boolean flushed = false;
    private boolean storeExpiredEvents = false;

    @Override
    protected void init(ExpressionExecutor[] attributeExpressionExecutors, ExecutionPlanContext executionPlanContext) {
        if (outputExpectsExpiredEvents) {
            this.expiredEventChunk = new ComplexEventChunk<StreamEvent>(false);
            this.storeExpiredEvents = true;
        }
        if (attributeExpressionExecutors.length >= 2 && attributeExpressionExecutors.length <= 4) {

            if ((attributeExpressionExecutors[0] instanceof ConstantExpressionExecutor)) {
                throw new ExecutionPlanValidationException("ExternalTime window's 1st parameter timestamp should not be a constant ");
            }
            if (attributeExpressionExecutors[0].getReturnType() != Attribute.Type.LONG) {
                throw new ExecutionPlanValidationException("ExternalTime window's 1st parameter timestamp should be type long, but found " + attributeExpressionExecutors[0].getReturnType());
            }
            timestampExpressionExecutor = attributeExpressionExecutors[0];

            if (attributeExpressionExecutors[1].getReturnType() == Attribute.Type.INT) {
                timeToKeep = (Integer) ((ConstantExpressionExecutor) attributeExpressionExecutors[1]).getValue();

            } else if (attributeExpressionExecutors[1].getReturnType() == Attribute.Type.LONG) {
                timeToKeep = (Long) ((ConstantExpressionExecutor) attributeExpressionExecutors[1]).getValue();
            } else {
                throw new ExecutionPlanValidationException("ExternalTimeBatch window's 2nd parameter windowTime should be either int or long, but found " + attributeExpressionExecutors[1].getReturnType());
            }

            if (attributeExpressionExecutors.length >= 3) {
                isStartTimeEnabled = true;
                if ((attributeExpressionExecutors[2] instanceof ConstantExpressionExecutor)) {
                    if (attributeExpressionExecutors[2].getReturnType() == Attribute.Type.INT) {
                        startTime = Integer.parseInt(String.valueOf(((ConstantExpressionExecutor) attributeExpressionExecutors[2]).getValue()));
                    } else if (attributeExpressionExecutors[2].getReturnType() == Attribute.Type.LONG) {
                        startTime = Long.parseLong(String.valueOf(((ConstantExpressionExecutor) attributeExpressionExecutors[2]).getValue()));
                    } else {
                        throw new ExecutionPlanValidationException("ExternalTimeBatch window's 3rd parameter startTime should either be a constant (of type int or long) or an attribute (of type long), but found " + attributeExpressionExecutors[2].getReturnType());
                    }
                }
                else if (attributeExpressionExecutors[2].getReturnType() != Attribute.Type.LONG) {
                    throw new ExecutionPlanValidationException("ExternalTimeBatch window's 3rd parameter startTime should either be a constant (of type int or long) or an attribute (of type long), but found " + attributeExpressionExecutors[2].getReturnType());
                }
                else {
                    startTimeAsVariable = attributeExpressionExecutors[2];
                }

            }

            if (attributeExpressionExecutors.length == 4) {
                if (attributeExpressionExecutors[3].getReturnType() == Attribute.Type.INT) {
                    schedulerTimeout = Integer.parseInt(String.valueOf(((ConstantExpressionExecutor) attributeExpressionExecutors[3]).getValue()));
                } else if (attributeExpressionExecutors[3].getReturnType() == Attribute.Type.LONG) {
                    schedulerTimeout = Long.parseLong(String.valueOf(((ConstantExpressionExecutor) attributeExpressionExecutors[3]).getValue()));
                } else {
                    throw new ExecutionPlanValidationException("ExternalTimeBatch window's 4th parameter timeout should be either int or long, but found " + attributeExpressionExecutors[3].getReturnType());
                }
            }
        } else {
            throw new ExecutionPlanValidationException("ExternalTimeBatch window should only have two to four parameters (<long> timestamp, <int|long|time> windowTime, <long> startTime, <int|long|time> timeout), but found " + attributeExpressionExecutors.length + " input attributes");
        }
        if (schedulerTimeout > 0) {
            if (expiredEventChunk == null) {
                this.expiredEventChunk = new ComplexEventChunk<StreamEvent>(false);
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
                            if (currentEventChunk.getFirst() != null) {
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
                if (startTimeAsVariable == null){
                    endTime = findEndTime((Long) timestampExpressionExecutor.execute(firstStreamEvent), startTime, timeToKeep);
                }
                else{
                    startTime = (Long) startTimeAsVariable.execute(firstStreamEvent);
                    endTime = startTime + timeToKeep;
                }

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
            if (expiredEventChunk.getFirst() != null) {
                // mark the timestamp for the expiredType event
                expiredEventChunk.reset();
                while (expiredEventChunk.hasNext()) {
                    StreamEvent expiredEvent = expiredEventChunk.next();
                    expiredEvent.setTimestamp(currentTime);
                }
                // add expired event to newEventChunk.
                newEventChunk.add(expiredEventChunk.getFirst());
            }
        }
        if (expiredEventChunk != null) {
            expiredEventChunk.clear();
        }

        if (currentEventChunk.getFirst() != null) {

            // add reset event in front of current events
            resetEvent.setTimestamp(currentTime);
            newEventChunk.add(resetEvent);
            resetEvent = null;

            // move to expired events
            if (preserveCurrentEvents || storeExpiredEvents) {
                currentEventChunk.reset();
                while (currentEventChunk.hasNext()) {
                    StreamEvent currentEvent = currentEventChunk.next();
                    StreamEvent toExpireEvent = streamEventCloner.copyStreamEvent(currentEvent);
                    toExpireEvent.setType(StreamEvent.Type.EXPIRED);
                    expiredEventChunk.add(toExpireEvent);
                }
            }

            // add current event chunk to next processor
            newEventChunk.add(currentEventChunk.getFirst());
        }
        currentEventChunk.clear();

        if (newEventChunk.getFirst() != null) {
            complexEventChunks.add(newEventChunk);
        }
    }

    private void appendToOutputChunk(StreamEventCloner streamEventCloner, List<ComplexEventChunk<StreamEvent>> complexEventChunks,
                                     long currentTime, boolean preserveCurrentEvents) {
        ComplexEventChunk<StreamEvent> newEventChunk = new ComplexEventChunk<StreamEvent>(true);
        ComplexEventChunk<StreamEvent> sentEventChunk = new ComplexEventChunk<StreamEvent>(true);
        if (currentEventChunk.getFirst() != null) {

            if (expiredEventChunk.getFirst() != null) {
                // mark the timestamp for the expiredType event
                expiredEventChunk.reset();
                while (expiredEventChunk.hasNext()) {
                    StreamEvent expiredEvent = expiredEventChunk.next();

                    if (outputExpectsExpiredEvents) {
                        // add expired event to newEventChunk.
                        StreamEvent toExpireEvent = streamEventCloner.copyStreamEvent(expiredEvent);
                        toExpireEvent.setTimestamp(currentTime);
                        newEventChunk.add(toExpireEvent);
                    }

                    StreamEvent toSendEvent = streamEventCloner.copyStreamEvent(expiredEvent);
                    toSendEvent.setType(ComplexEvent.Type.CURRENT);
                    sentEventChunk.add(toSendEvent);
                }
            }

            // add reset event in front of current events
            StreamEvent toResetEvent = streamEventCloner.copyStreamEvent(resetEvent);
            toResetEvent.setTimestamp(currentTime);
            newEventChunk.add(toResetEvent);

            //add old events
            newEventChunk.add(sentEventChunk.getFirst());

            // move to expired events
            if (preserveCurrentEvents || storeExpiredEvents) {
                currentEventChunk.reset();
                while (currentEventChunk.hasNext()) {
                    StreamEvent currentEvent = currentEventChunk.next();
                    StreamEvent toExpireEvent = streamEventCloner.copyStreamEvent(currentEvent);
                    toExpireEvent.setType(StreamEvent.Type.EXPIRED);
                    expiredEventChunk.add(toExpireEvent);
                }
            }

            // add current event chunk to next processor
            newEventChunk.add(currentEventChunk.getFirst());

        }
        currentEventChunk.clear();

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
        currentEventChunk.add(clonedStreamEvent);
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
        return new Object[]{currentEventChunk.getFirst(), expiredEventChunk != null ? expiredEventChunk.getFirst() : null, resetEvent, endTime, startTime, lastScheduledTime, lastCurrentEventTime, flushed};
    }

    public void restoreState(Object[] state) {
        currentEventChunk.clear();
        currentEventChunk.add((StreamEvent) state[0]);
        if (state[1] != null) {
            expiredEventChunk.clear();
            expiredEventChunk.add((StreamEvent) state[1]);
        } else {
            expiredEventChunk = null;
        }
        resetEvent = (StreamEvent) state[2];
        endTime = (Long) state[3];
        startTime = (Long) state[4];
        lastScheduledTime = (Long) state[5];
        lastCurrentEventTime = (Long) state[6];
        flushed = (Boolean) state[7];
    }

    public synchronized StreamEvent find(StateEvent matchingEvent, Finder finder) {
        return finder.find(matchingEvent, expiredEventChunk, streamEventCloner);
    }

    @Override
    public Finder constructFinder(Expression expression, MatchingMetaStateHolder matchingMetaStateHolder, ExecutionPlanContext executionPlanContext,
                                  List<VariableExpressionExecutor> variableExpressionExecutors, Map<String, EventTable> eventTableMap) {
        if (expiredEventChunk == null) {
            expiredEventChunk = new ComplexEventChunk<StreamEvent>(false);
            storeExpiredEvents = true;
        }
        return OperatorParser.constructOperator(expiredEventChunk, expression, matchingMetaStateHolder,executionPlanContext,variableExpressionExecutors,eventTableMap);
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
