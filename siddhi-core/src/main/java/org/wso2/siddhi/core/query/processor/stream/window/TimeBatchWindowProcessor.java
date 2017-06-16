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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of {@link WindowProcessor} which represent a Batch Window operating based on time.
 */
@Extension(
        name = "timeBatch",
        namespace = "",
        description = "A batch (tumbling) time window that holds events that arrive during window.time periods, " +
                "and gets updated for each window.time.",
        parameters = {
                @Parameter(name = "window.time",
                        description = "The batch time period for which the window should hold events.",
                        type = {DataType.INT, DataType.LONG, DataType.TIME}),
                @Parameter(name = "start.time",
                        description = "This specifies an offset in milliseconds in order to start the " +
                                "window at a time different to the standard time.",
                        type = {DataType.INT})
        },
        examples = {
                @Example(
                        syntax = "define window cseEventWindow (symbol string, price float, volume int) " +
                                "timeBatch(20) output all events;\n" +
                                "@info(name = 'query0')\n" +
                                "from cseEventStream\n" +
                                "insert into cseEventWindow;\n" +
                                "@info(name = 'query1')\n" +
                                "from cseEventWindow\n" +
                                "select symbol, sum(price) as price\n" +
                                "insert all events into outputStream ;",
                        description = "This will processing events arrived every 20 milliseconds" +
                                " as a batch and out put all events."
                )
        }
)
public class TimeBatchWindowProcessor extends WindowProcessor implements SchedulingProcessor, FindableProcessor {

    private long timeInMilliSeconds;
    private long nextEmitTime = -1;
    private ComplexEventChunk<StreamEvent> currentEventChunk = new ComplexEventChunk<StreamEvent>(false);
    private ComplexEventChunk<StreamEvent> expiredEventChunk = null;
    private StreamEvent resetEvent = null;
    private Scheduler scheduler;
    private boolean outputExpectsExpiredEvents;
    private SiddhiAppContext siddhiAppContext;
    private boolean isStartTimeEnabled = false;
    private long startTime = 0;

    public void setTimeInMilliSeconds(long timeInMilliSeconds) {
        this.timeInMilliSeconds = timeInMilliSeconds;
    }

    @Override
    public Scheduler getScheduler() {
        return scheduler;
    }

    @Override
    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    protected void init(ExpressionExecutor[] attributeExpressionExecutors, ConfigReader configReader, boolean
            outputExpectsExpiredEvents, SiddhiAppContext siddhiAppContext) {
        this.outputExpectsExpiredEvents = outputExpectsExpiredEvents;
        this.siddhiAppContext = siddhiAppContext;
        if (outputExpectsExpiredEvents) {
            this.expiredEventChunk = new ComplexEventChunk<StreamEvent>(false);
        }
        if (attributeExpressionExecutors.length == 1) {
            if (attributeExpressionExecutors[0] instanceof ConstantExpressionExecutor) {
                if (attributeExpressionExecutors[0].getReturnType() == Attribute.Type.INT) {
                    timeInMilliSeconds = (Integer) ((ConstantExpressionExecutor) attributeExpressionExecutors[0])
                            .getValue();

                } else if (attributeExpressionExecutors[0].getReturnType() == Attribute.Type.LONG) {
                    timeInMilliSeconds = (Long) ((ConstantExpressionExecutor) attributeExpressionExecutors[0])
                            .getValue();
                } else {
                    throw new SiddhiAppValidationException("Time window's parameter attribute should be either " +
                                                                       "int or long, but found " +
                                                                       attributeExpressionExecutors[0].getReturnType());
                }
            } else {
                throw new SiddhiAppValidationException("Time window should have constant parameter attribute but " +
                                                                   "found a dynamic attribute " +
                                                                   attributeExpressionExecutors[0].getClass().
                                                                           getCanonicalName());
            }
        } else if (attributeExpressionExecutors.length == 2) {
            if (attributeExpressionExecutors[0] instanceof ConstantExpressionExecutor) {
                if (attributeExpressionExecutors[0].getReturnType() == Attribute.Type.INT) {
                    timeInMilliSeconds = (Integer) ((ConstantExpressionExecutor) attributeExpressionExecutors[0])
                            .getValue();

                } else if (attributeExpressionExecutors[0].getReturnType() == Attribute.Type.LONG) {
                    timeInMilliSeconds = (Long) ((ConstantExpressionExecutor) attributeExpressionExecutors[0])
                            .getValue();
                } else {
                    throw new SiddhiAppValidationException("Time window's parameter attribute should be either " +
                                                                       "int or long, but found " +
                                                                       attributeExpressionExecutors[0].getReturnType());
                }
            } else {
                throw new SiddhiAppValidationException("Time window should have constant parameter attribute but " +
                                                                   "found a dynamic attribute " +
                                                                   attributeExpressionExecutors[0].getClass()
                                                                           .getCanonicalName());
            }
            // start time
            isStartTimeEnabled = true;
            if (attributeExpressionExecutors[1].getReturnType() == Attribute.Type.INT) {
                startTime = Integer.parseInt(String.valueOf(((ConstantExpressionExecutor)
                        attributeExpressionExecutors[1]).getValue()));
            } else {
                startTime = Long.parseLong(String.valueOf(((ConstantExpressionExecutor)
                        attributeExpressionExecutors[1]).getValue()));
            }
        } else {
            throw new SiddhiAppValidationException("Time window should only have one or two parameters. " +
                                                               "(<int|long|time> windowTime), but found " +
                                                               attributeExpressionExecutors.length + " input " +
                                                               "attributes");
        }
    }


    @Override
    protected void process(ComplexEventChunk<StreamEvent> streamEventChunk, Processor nextProcessor,
                           StreamEventCloner streamEventCloner) {
        synchronized (this) {
            if (nextEmitTime == -1) {
                long currentTime = siddhiAppContext.getTimestampGenerator().currentTime();
                if (isStartTimeEnabled) {
                    nextEmitTime = getNextEmitTime(currentTime);
                } else {
                    nextEmitTime = siddhiAppContext.getTimestampGenerator().currentTime() + timeInMilliSeconds;
                }
                scheduler.notifyAt(nextEmitTime);
            }
            long currentTime = siddhiAppContext.getTimestampGenerator().currentTime();
            boolean sendEvents;

            if (currentTime >= nextEmitTime) {
                nextEmitTime += timeInMilliSeconds;
                scheduler.notifyAt(nextEmitTime);
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

                if (outputExpectsExpiredEvents) {
                    if (expiredEventChunk.getFirst() != null) {
                        while (expiredEventChunk.hasNext()) {
                            StreamEvent expiredEvent = expiredEventChunk.next();
                            expiredEvent.setTimestamp(currentTime);
                        }
                        streamEventChunk.add(expiredEventChunk.getFirst());
                    }
                }
                if (expiredEventChunk != null) {
                    expiredEventChunk.clear();
                }

                if (currentEventChunk.getFirst() != null) {

                    // add reset event in front of current events
                    streamEventChunk.add(resetEvent);
                    resetEvent = null;

                    if (expiredEventChunk != null) {
                        currentEventChunk.reset();
                        while (currentEventChunk.hasNext()) {
                            StreamEvent currentEvent = currentEventChunk.next();
                            StreamEvent toExpireEvent = streamEventCloner.copyStreamEvent(currentEvent);
                            toExpireEvent.setType(StreamEvent.Type.EXPIRED);
                            expiredEventChunk.add(toExpireEvent);
                        }
                    }

                    resetEvent = streamEventCloner.copyStreamEvent(currentEventChunk.getFirst());
                    resetEvent.setType(ComplexEvent.Type.RESET);
                    streamEventChunk.add(currentEventChunk.getFirst());
                }
                currentEventChunk.clear();
            }
        }
        if (streamEventChunk.getFirst() != null) {
            streamEventChunk.setBatch(true);
            nextProcessor.process(streamEventChunk);
            streamEventChunk.setBatch(false);
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
    public Map<String, Object> currentState() {
        Map<String, Object> state = new HashMap<>();
        synchronized (this) {
            state.put("CurrentEventChunk", currentEventChunk.getFirst());
            state.put("ExpiredEventChunk", expiredEventChunk != null ? expiredEventChunk.getFirst() : null);
            state.put("ResetEvent", resetEvent);
        }
        return state;
    }

    @Override
    public synchronized void restoreState(Map<String, Object> state) {
        if (expiredEventChunk != null) {
            expiredEventChunk.clear();
            expiredEventChunk.add((StreamEvent) state.get("ExpiredEventChunk"));
        }
        currentEventChunk.clear();
        currentEventChunk.add((StreamEvent) state.get("CurrentEventChunk"));
        resetEvent = (StreamEvent) state.get("ResetEvent");
    }

    @Override
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
        }
        return OperatorParser.constructOperator(expiredEventChunk, expression, matchingMetaInfoHolder,
                                                siddhiAppContext, variableExpressionExecutors, tableMap,
                                                this.queryName);
    }
}
