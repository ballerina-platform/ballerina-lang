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

import org.wso2.siddhi.annotation.Example;
import org.wso2.siddhi.annotation.Extension;
import org.wso2.siddhi.annotation.Parameter;
import org.wso2.siddhi.annotation.util.DataType;
import org.wso2.siddhi.core.config.SiddhiAppContext;
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
import org.wso2.siddhi.core.table.Table;
import org.wso2.siddhi.core.util.Scheduler;
import org.wso2.siddhi.core.util.collection.operator.CompiledCondition;
import org.wso2.siddhi.core.util.collection.operator.MatchingMetaInfoHolder;
import org.wso2.siddhi.core.util.collection.operator.Operator;
import org.wso2.siddhi.core.util.config.ConfigReader;
import org.wso2.siddhi.core.util.parser.OperatorParser;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.exception.SiddhiAppValidationException;
import org.wso2.siddhi.query.api.expression.Expression;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of {@link WindowProcessor} which represent a Batch Window operating based on external time.
 */
@Extension(
        name = "externalTimeBatch",
        namespace = "",
        description = "A batch (tumbling) time window based on external time, that holds events arrived " +
                "during windowTime periods, and gets updated for every windowTime.",
        parameters = {
                @Parameter(name = "timestamp",
                        description = "The time which the window determines as current time and will act upon. " +
                                "The value of this parameter should be monotonically increasing.",
                        type = {DataType.LONG}),
                @Parameter(name = "window.time",
                        description = "The batch time period for which the window should hold events.",
                        type = {DataType.INT, DataType.LONG, DataType.TIME}),
                @Parameter(name = "start.time",
                        description = "User defined start time. This could either be a constant (of type int, " +
                                "long or time) or an attribute of the corresponding stream (of type long). " +
                                "If an attribute is provided, initial value of attribute would be considered as " +
                                "startTime. When startTime is not given, initial value of timestamp " +
                                "is used as the default.",
                        type = {DataType.INT, DataType.LONG, DataType.TIME},
                        optional = true,
                        defaultValue = "0"),
                @Parameter(name = "timeout",
                        description = "Time to wait for arrival of new event, before flushing " +
                                "and giving output for events belonging to a specific batch. If timeout is " +
                                "not provided, system waits till an event from next batch arrives to " +
                                "flush current batch.",
                        type = {DataType.INT, DataType.LONG, DataType.TIME},
                        optional = true,
                        defaultValue = "0")
        },
        examples = {
                @Example(
                        syntax = "define window cseEventWindow (symbol string, price float, volume int) " +
                                "externalTimeBatch(eventTime, 1 sec) output expired events;\n" +
                                "@info(name = 'query0')\n" +
                                "from cseEventStream\n" +
                                "insert into cseEventWindow;\n" +
                                "@info(name = 'query1')\n" +
                                "from cseEventWindow\n" +
                                "select symbol, sum(price) as price\n" +
                                "insert expired events into outputStream ;",
                        description = "This will processing events that arrive every 1 seconds " +
                                "from the eventTime."
                ),
                @Example(
                        syntax = "define window cseEventWindow (symbol string, price float, volume int) " +
                                "externalTimeBatch(eventTime, 20 sec, 0) output expired events;",
                        description = "This will processing events that arrive every 1 seconds " +
                                "from the eventTime. Starts on 0th millisecond of an hour."
                ),
                @Example(
                        syntax = "define window cseEventWindow (symbol string, price float, volume int) " +
                                "externalTimeBatch(eventTime, 2 sec, eventTimestamp, 100) output expired events;",
                        description = "This will processing events that arrive every 2 seconds from the " +
                                "eventTim. Considers the first event's eventTimestamp value as startTime. " +
                                "Waits 100 milliseconds for the arrival of a new event before flushing current batch."
                )
        }
)
public class ExternalTimeBatchWindowProcessor extends WindowProcessor implements SchedulingProcessor,
        FindableProcessor {
    private ComplexEventChunk<StreamEvent> currentEventChunk = new ComplexEventChunk<StreamEvent>(false);
    private ComplexEventChunk<StreamEvent> expiredEventChunk = null;
    private StreamEvent resetEvent = null;
    private VariableExpressionExecutor timestampExpressionExecutor;
    private ExpressionExecutor startTimeAsVariable;
    private long timeToKeep;
    private long endTime = -1;
    private long startTime = 0;
    private boolean isStartTimeEnabled = false;
    private long schedulerTimeout = 0;
    private Scheduler scheduler;
    private long lastScheduledTime;
    private long lastCurrentEventTime;
    private boolean flushed = false;
    private boolean storeExpiredEvents = false;
    private boolean replaceTimestampWithBatchEndTime = false;
    private boolean outputExpectsExpiredEvents;

    @Override
    protected void init(ExpressionExecutor[] attributeExpressionExecutors, ConfigReader configReader, boolean
            outputExpectsExpiredEvents, SiddhiAppContext siddhiAppContext) {
        this.outputExpectsExpiredEvents = outputExpectsExpiredEvents;
        if (outputExpectsExpiredEvents) {
            this.expiredEventChunk = new ComplexEventChunk<StreamEvent>(false);
            this.storeExpiredEvents = true;
        }
        if (attributeExpressionExecutors.length >= 2 && attributeExpressionExecutors.length <= 5) {

            if (!(attributeExpressionExecutors[0] instanceof VariableExpressionExecutor)) {
                throw new SiddhiAppValidationException("ExternalTime window's 1st parameter timestamp should be a" +
                        " variable, but found " + attributeExpressionExecutors[0].getClass());
            }
            if (attributeExpressionExecutors[0].getReturnType() != Attribute.Type.LONG) {
                throw new SiddhiAppValidationException("ExternalTime window's 1st parameter timestamp should be " +
                        "type long, but found " + attributeExpressionExecutors[0].getReturnType());
            }
            timestampExpressionExecutor = (VariableExpressionExecutor) attributeExpressionExecutors[0];

            if (attributeExpressionExecutors[1].getReturnType() == Attribute.Type.INT) {
                timeToKeep = (Integer) ((ConstantExpressionExecutor) attributeExpressionExecutors[1]).getValue();

            } else if (attributeExpressionExecutors[1].getReturnType() == Attribute.Type.LONG) {
                timeToKeep = (Long) ((ConstantExpressionExecutor) attributeExpressionExecutors[1]).getValue();
            } else {
                throw new SiddhiAppValidationException("ExternalTimeBatch window's 2nd parameter windowTime " +
                        "should be either int or long, but found " + attributeExpressionExecutors[1].getReturnType());
            }

            if (attributeExpressionExecutors.length >= 3) {
                isStartTimeEnabled = true;
                if ((attributeExpressionExecutors[2] instanceof ConstantExpressionExecutor)) {
                    if (attributeExpressionExecutors[2].getReturnType() == Attribute.Type.INT) {
                        startTime = Integer.parseInt(String.valueOf(((ConstantExpressionExecutor)
                                attributeExpressionExecutors[2]).getValue()));
                    } else if (attributeExpressionExecutors[2].getReturnType() == Attribute.Type.LONG) {
                        startTime = Long.parseLong(String.valueOf(((ConstantExpressionExecutor)
                                attributeExpressionExecutors[2]).getValue()));
                    } else {
                        throw new SiddhiAppValidationException("ExternalTimeBatch window's 3rd parameter " +
                                "startTime should either be a constant (of type int or long) or an attribute (of type" +
                                " long), but found " + attributeExpressionExecutors[2].getReturnType());
                    }
                } else if (attributeExpressionExecutors[2].getReturnType() != Attribute.Type.LONG) {
                    throw new SiddhiAppValidationException("ExternalTimeBatch window's 3rd parameter startTime " +
                            "should either be a constant (of type int or long) or an attribute (of type long), but " +
                            "found " + attributeExpressionExecutors[2].getReturnType());
                } else {
                    startTimeAsVariable = attributeExpressionExecutors[2];
                }
            }

            if (attributeExpressionExecutors.length >= 4) {
                if (attributeExpressionExecutors[3].getReturnType() == Attribute.Type.INT) {
                    schedulerTimeout = Integer.parseInt(String.valueOf(((ConstantExpressionExecutor)
                            attributeExpressionExecutors[3]).getValue()));
                } else if (attributeExpressionExecutors[3].getReturnType() == Attribute.Type.LONG) {
                    schedulerTimeout = Long.parseLong(String.valueOf(((ConstantExpressionExecutor)
                            attributeExpressionExecutors[3]).getValue()));
                } else {
                    throw new SiddhiAppValidationException("ExternalTimeBatch window's 4th parameter timeout " +
                            "should be either int or long, but found " + attributeExpressionExecutors[3]
                            .getReturnType());
                }
            }

            if (attributeExpressionExecutors.length == 5) {
                if (attributeExpressionExecutors[4].getReturnType() == Attribute.Type.BOOL) {
                    replaceTimestampWithBatchEndTime = Boolean.parseBoolean(String.valueOf((
                            (ConstantExpressionExecutor) attributeExpressionExecutors[4]).getValue()));
                } else {
                    throw new SiddhiAppValidationException("ExternalTimeBatch window's 5th parameter " +
                            "replaceTimestampWithBatchEndTime should be bool, but found " +
                            attributeExpressionExecutors[4].getReturnType());
                }
            }
        } else {
            throw new SiddhiAppValidationException("ExternalTimeBatch window should only have two to five " +
                    "parameters (<long> timestamp, <int|long|time> windowTime, <long> startTime, <int|long|time> " +
                    "timeout, <bool> replaceTimestampWithBatchEndTime), but found " + attributeExpressionExecutors
                    .length + " input attributes");
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
    protected void process(ComplexEventChunk<StreamEvent> streamEventChunk, Processor nextProcessor,
                           StreamEventCloner streamEventCloner) {

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
                        lastScheduledTime = siddhiAppContext.getTimestampGenerator().currentTime() +
                                schedulerTimeout;
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
                        lastScheduledTime = siddhiAppContext.getTimestampGenerator().currentTime() +
                                schedulerTimeout;
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
                if (startTimeAsVariable == null) {
                    endTime = findEndTime((Long) timestampExpressionExecutor.execute(firstStreamEvent), startTime,
                            timeToKeep);
                } else {
                    startTime = (Long) startTimeAsVariable.execute(firstStreamEvent);
                    endTime = startTime + timeToKeep;
                }
            } else {
                startTime = (Long) timestampExpressionExecutor.execute(firstStreamEvent);
                endTime = startTime + timeToKeep;
            }
            if (schedulerTimeout > 0) {
                lastScheduledTime = siddhiAppContext.getTimestampGenerator().currentTime() + schedulerTimeout;
                scheduler.notifyAt(lastScheduledTime);
            }
        }
    }

    private void flushToOutputChunk(StreamEventCloner streamEventCloner, List<ComplexEventChunk<StreamEvent>>
            complexEventChunks,
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

    private void appendToOutputChunk(StreamEventCloner streamEventCloner, List<ComplexEventChunk<StreamEvent>>
            complexEventChunks,
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
        if (replaceTimestampWithBatchEndTime) {
            clonedStreamEvent.setAttribute(endTime, timestampExpressionExecutor.getPosition());
        }
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

    @Override
    public Map<String, Object> currentState() {
        Map<String, Object> state = new HashMap<>();
        synchronized (this) {
            state.put("StartTime", startTime);
            state.put("EndTime", endTime);
            state.put("LastScheduledTime", lastScheduledTime);
            state.put("LastCurrentEventTime", lastCurrentEventTime);
            state.put("CurrentEventChunk", currentEventChunk.getFirst());
            state.put("ExpiredEventChunk", expiredEventChunk != null ? expiredEventChunk.getFirst() : null);
            state.put("ResetEvent", resetEvent);
            state.put("Flushed", flushed);
        }
        return state;
    }


    @Override
    public synchronized void restoreState(Map<String, Object> state) {
        startTime = (long) state.get("StartTime");
        endTime = (long) state.get("EndTime");
        lastScheduledTime = (long) state.get("LastScheduledTime");
        lastCurrentEventTime = (long) state.get("LastCurrentEventTime");
        currentEventChunk.clear();
        currentEventChunk.add((StreamEvent) state.get("CurrentEventChunk"));
        if (expiredEventChunk != null) {
            expiredEventChunk.clear();
            expiredEventChunk.add((StreamEvent) state.get("ExpiredEventChunk"));
        } else {
            if (outputExpectsExpiredEvents) {
                expiredEventChunk = new ComplexEventChunk<StreamEvent>(false);
            }
            if (schedulerTimeout > 0) {
                expiredEventChunk = new ComplexEventChunk<StreamEvent>(false);
            }
        }
        resetEvent = (StreamEvent) state.get("ResetEvent");
        flushed = (boolean) state.get("Flushed");

    }

    public synchronized StreamEvent find(StateEvent matchingEvent, CompiledCondition compiledCondition) {
        return ((Operator) compiledCondition).find(matchingEvent, expiredEventChunk, streamEventCloner);
    }

    @Override
    public CompiledCondition compileCondition(Expression expression, MatchingMetaInfoHolder matchingMetaInfoHolder,
                                              SiddhiAppContext siddhiAppContext,
                                              List<VariableExpressionExecutor> variableExpressionExecutors,
                                              Map<String, Table> tableMap, String queryName) {
        if (expiredEventChunk == null) {
            expiredEventChunk = new ComplexEventChunk<StreamEvent>(false);
            storeExpiredEvents = true;
        }
        return OperatorParser.constructOperator(expiredEventChunk, expression, matchingMetaInfoHolder,
                siddhiAppContext, variableExpressionExecutors, tableMap, this.queryName);
    }

    @Override
    public Scheduler getScheduler() {
        return this.scheduler;
    }

    @Override
    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }
}
