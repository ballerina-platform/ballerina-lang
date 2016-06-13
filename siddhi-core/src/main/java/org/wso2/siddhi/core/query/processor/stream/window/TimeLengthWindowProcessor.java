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

public class TimeLengthWindowProcessor extends WindowProcessor implements SchedulingProcessor, FindableProcessor {

    private long timeInMilliSeconds;
    private int length;
    private int count = 0;
    private ComplexEventChunk<StreamEvent> expiredEventChunk;
    private Scheduler scheduler;
    private ExecutionPlanContext executionPlanContext;

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
        expiredEventChunk = new ComplexEventChunk<StreamEvent>(false);
        if (attributeExpressionExecutors.length == 2) {
            length = (Integer) ((ConstantExpressionExecutor) attributeExpressionExecutors[1]).getValue();
            if (attributeExpressionExecutors[0] instanceof ConstantExpressionExecutor) {
                if (attributeExpressionExecutors[0].getReturnType() == Attribute.Type.INT) {
                    timeInMilliSeconds = (Integer) ((ConstantExpressionExecutor) attributeExpressionExecutors[0]).getValue();

                } else if (attributeExpressionExecutors[0].getReturnType() == Attribute.Type.LONG) {
                    timeInMilliSeconds = (Long) ((ConstantExpressionExecutor) attributeExpressionExecutors[0]).getValue();
                } else {
                    throw new ExecutionPlanValidationException("TimeLength window's first parameter attribute should be either int or long, but found " + attributeExpressionExecutors[0].getReturnType());
                }
            } else {
                throw new ExecutionPlanValidationException("TimeLength window should have constant parameter attributes but found a dynamic attribute " + attributeExpressionExecutors[0].getClass().getCanonicalName());
            }
        } else {
            throw new ExecutionPlanValidationException("TimeLength window should only have two parameters (<int> windowTime,<int> windowLength), but found " + attributeExpressionExecutors.length + " input attributes");
        }
    }

    @Override
    protected void process(ComplexEventChunk<StreamEvent> streamEventChunk, Processor nextProcessor, StreamEventCloner streamEventCloner) {
        synchronized (this) {
            long currentTime = executionPlanContext.getTimestampGenerator().currentTime();

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
    public synchronized StreamEvent find(ComplexEvent matchingEvent, Finder finder) {
        return finder.find(matchingEvent, expiredEventChunk, streamEventCloner);
    }

    @Override
    public Finder constructFinder(Expression expression, MetaComplexEvent matchingMetaComplexEvent, ExecutionPlanContext executionPlanContext, List<VariableExpressionExecutor> variableExpressionExecutors, Map<String, EventTable> eventTableMap, int matchingStreamIndex, long withinTime) {
        return CollectionOperatorParser.parse(expression, matchingMetaComplexEvent, executionPlanContext, variableExpressionExecutors, eventTableMap, matchingStreamIndex, inputDefinition, withinTime);
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
        return new Object[]{expiredEventChunk};
    }

    @Override
    public void restoreState(Object[] state) {
        expiredEventChunk = (ComplexEventChunk<StreamEvent>) state[0];
    }

}

