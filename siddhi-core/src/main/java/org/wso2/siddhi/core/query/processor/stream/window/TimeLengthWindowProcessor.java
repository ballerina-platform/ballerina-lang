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
 * Implementation of {@link WindowProcessor} which represent a Window operating based on pre-defined length.
 */
@Extension(
        name = "timeLength",
        namespace = "",
        description = "A sliding time window that, at a given time holds the last window.length events that arrived " +
                "during last window.time period, and gets updated for every event arrival and expiry.",
        parameters = {
                @Parameter(name = "window.time",
                        description = "The sliding time period for which the window should hold events.",
                        type = {DataType.INT, DataType.LONG, DataType.TIME}),
                @Parameter(name = "window.length",
                        description = "The number of events that should be be included in a sliding length window..",
                        type = {DataType.INT})
        },
        examples = @Example(
                syntax = "define stream cseEventStream (symbol string, price float, volume int);\n" +
                        "define window cseEventWindow (symbol string, price float, volume int) " +
                        "timeLength(2 sec, 10);\n" +
                        "@info(name = 'query0')\n" +
                        "from cseEventStream\n" +
                        "insert into cseEventWindow;\n" +
                        "@info(name = 'query1')\n" +
                        "from cseEventWindow select symbol, price, volume\n" +
                        "insert all events into outputStream;",
                description = "window.timeLength(2 sec, 10) holds the last 10 events that arrived during " +
                        "last 2 seconds and gets updated for every event arrival and expiry."
        )
)
public class TimeLengthWindowProcessor extends WindowProcessor implements SchedulingProcessor, FindableProcessor {

    private long timeInMilliSeconds;
    private int length;
    private int count = 0;
    private ComplexEventChunk<StreamEvent> expiredEventChunk;
    private Scheduler scheduler;
    private SiddhiAppContext siddhiAppContext;

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
        this.siddhiAppContext = siddhiAppContext;
        expiredEventChunk = new ComplexEventChunk<StreamEvent>(false);
        if (attributeExpressionExecutors.length == 2) {
            length = (Integer) ((ConstantExpressionExecutor) attributeExpressionExecutors[1]).getValue();
            if (attributeExpressionExecutors[0] instanceof ConstantExpressionExecutor) {
                if (attributeExpressionExecutors[0].getReturnType() == Attribute.Type.INT) {
                    timeInMilliSeconds = (Integer) ((ConstantExpressionExecutor) attributeExpressionExecutors[0])
                            .getValue();

                } else if (attributeExpressionExecutors[0].getReturnType() == Attribute.Type.LONG) {
                    timeInMilliSeconds = (Long) ((ConstantExpressionExecutor) attributeExpressionExecutors[0])
                            .getValue();
                } else {
                    throw new SiddhiAppValidationException("TimeLength window's first parameter attribute should " +
                            "be either int or long, but found " + attributeExpressionExecutors[0].getReturnType());
                }
            } else {
                throw new SiddhiAppValidationException("TimeLength window should have constant parameter " +
                        "attributes but found a dynamic attribute " + attributeExpressionExecutors[0].getClass()
                        .getCanonicalName());
            }
        } else {
            throw new SiddhiAppValidationException("TimeLength window should only have two parameters (<int> " +
                    "windowTime,<int> windowLength), but found " + attributeExpressionExecutors.length + " input " +
                    "attributes");
        }
    }

    @Override
    protected void process(ComplexEventChunk<StreamEvent> streamEventChunk, Processor nextProcessor,
                           StreamEventCloner streamEventCloner) {
        synchronized (this) {
            long currentTime = siddhiAppContext.getTimestampGenerator().currentTime();

            while (streamEventChunk.hasNext()) {
                StreamEvent streamEvent = streamEventChunk.next();

                expiredEventChunk.reset();
                while (expiredEventChunk.hasNext()) {
                    StreamEvent expiredEvent = expiredEventChunk.next();
                    long timeDiff = expiredEvent.getTimestamp() - currentTime + timeInMilliSeconds;
                    if (timeDiff <= 0) {
                        expiredEventChunk.remove();
                        count--;
                        expiredEvent.setTimestamp(currentTime);
                        streamEventChunk.insertBeforeCurrent(expiredEvent);
                    } else {
                        break;
                    }
                }

                expiredEventChunk.reset();

                if (streamEvent.getType() == StreamEvent.Type.CURRENT) {
                    StreamEvent clonedEvent = streamEventCloner.copyStreamEvent(streamEvent);
                    clonedEvent.setType(StreamEvent.Type.EXPIRED);
                    if (count < length) {
                        count++;
                        this.expiredEventChunk.add(clonedEvent);
                    } else {
                        StreamEvent firstEvent = this.expiredEventChunk.poll();
                        if (firstEvent != null) {
                            firstEvent.setTimestamp(currentTime);
                            streamEventChunk.insertBeforeCurrent(firstEvent);
                            this.expiredEventChunk.add(clonedEvent);
                        }
                    }
                    scheduler.notifyAt(clonedEvent.getTimestamp() + timeInMilliSeconds);
                } else {
                    streamEventChunk.remove();
                }
            }
        }
        streamEventChunk.reset();
        if (streamEventChunk.hasNext()) {
            nextProcessor.process(streamEventChunk);
        }
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
        return OperatorParser.constructOperator(expiredEventChunk, expression, matchingMetaInfoHolder,
                siddhiAppContext, variableExpressionExecutors, tableMap, this.queryName);
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
        state.put("ExpiredEventChunk", expiredEventChunk.getFirst());
        return state;
    }

    @Override
    public void restoreState(Map<String, Object> state) {
        expiredEventChunk.clear();
        expiredEventChunk.add((StreamEvent) state.get("ExpiredEventChunk"));
    }

}

