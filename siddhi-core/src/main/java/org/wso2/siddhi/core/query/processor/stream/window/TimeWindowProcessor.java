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

import org.wso2.siddhi.annotation.Extension;
import org.wso2.siddhi.annotation.Parameter;
import org.wso2.siddhi.annotation.ReturnAttribute;
import org.wso2.siddhi.annotation.util.DataType;
import org.wso2.siddhi.core.config.ExecutionPlanContext;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Extension(
        name = "time",
        namespace = "",
        description = "A sliding time window that holds events that arrived during the last windowTime period " +
                "at a given time, and gets updated for each event arrival and expiry.",
        parameters = {
                @Parameter(name = "windowTime",
                        description = "The sliding time period for which the window should hold events.",
                        type = {DataType.INT, DataType.LONG, DataType.TIME})
        },
        returnAttributes = @ReturnAttribute(
                description = "Returns current and expired events.",
                type = {})
)
public class TimeWindowProcessor extends WindowProcessor implements SchedulingProcessor, FindableProcessor {

    private long timeInMilliSeconds;
    private ComplexEventChunk<StreamEvent> expiredEventChunk;
    private Scheduler scheduler;
    private ExecutionPlanContext executionPlanContext;
    private volatile long lastTimestamp = Long.MIN_VALUE;

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
    protected void init(ExpressionExecutor[] attributeExpressionExecutors, ExecutionPlanContext executionPlanContext) {
        this.executionPlanContext = executionPlanContext;
        this.expiredEventChunk = new ComplexEventChunk<StreamEvent>(false);
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
            throw new ExecutionPlanValidationException("Time window should only have one parameter (<int|long|time> windowTime), but found " + attributeExpressionExecutors.length + " input attributes");
        }
    }

    @Override
    protected void process(ComplexEventChunk<StreamEvent> streamEventChunk, Processor nextProcessor, StreamEventCloner streamEventCloner) {
        synchronized (this) {

            while (streamEventChunk.hasNext()) {
                StreamEvent streamEvent = streamEventChunk.next();
                long currentTime = executionPlanContext.getTimestampGenerator().currentTime();

                expiredEventChunk.reset();
                while (expiredEventChunk.hasNext()) {
                    StreamEvent expiredEvent = expiredEventChunk.next();
                    long timeDiff = expiredEvent.getTimestamp() - currentTime + timeInMilliSeconds;
                    if (timeDiff <= 0) {
                        expiredEventChunk.remove();
                        expiredEvent.setTimestamp(currentTime);
                        streamEventChunk.insertBeforeCurrent(expiredEvent);
                    } else {
                        break;
                    }
                }

                if (streamEvent.getType() == StreamEvent.Type.CURRENT) {

                    StreamEvent clonedEvent = streamEventCloner.copyStreamEvent(streamEvent);
                    clonedEvent.setType(StreamEvent.Type.EXPIRED);
                    this.expiredEventChunk.add(clonedEvent);

                    if (lastTimestamp < clonedEvent.getTimestamp()) {
                        scheduler.notifyAt(clonedEvent.getTimestamp() + timeInMilliSeconds);
                        lastTimestamp = clonedEvent.getTimestamp();
                    }
                } else {
                    streamEventChunk.remove();
                }
            }
            expiredEventChunk.reset();
        }
        nextProcessor.process(streamEventChunk);
    }

    @Override
    public synchronized StreamEvent find(StateEvent matchingEvent, Finder finder) {
        return finder.find(matchingEvent, expiredEventChunk, streamEventCloner);
    }

    @Override
    public Finder constructFinder(Expression expression, MatchingMetaStateHolder matchingMetaStateHolder, ExecutionPlanContext executionPlanContext,
                                  List<VariableExpressionExecutor> variableExpressionExecutors, Map<String, EventTable> eventTableMap) {
        return OperatorParser.constructOperator(expiredEventChunk, expression, matchingMetaStateHolder, executionPlanContext, variableExpressionExecutors, eventTableMap, queryName);
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
