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
import org.wso2.siddhi.core.table.EventTable;
import org.wso2.siddhi.core.util.parser.OperatorParser;
import org.wso2.siddhi.core.util.collection.operator.Finder;
import org.wso2.siddhi.core.util.collection.operator.MatchingMetaStateHolder;
import org.wso2.siddhi.query.api.exception.ExecutionPlanValidationException;
import org.wso2.siddhi.query.api.expression.Expression;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LengthBatchWindowProcessor extends WindowProcessor implements FindableProcessor {

    private int length;
    private int count = 0;
    private ComplexEventChunk<StreamEvent> currentEventChunk = new ComplexEventChunk<StreamEvent>(false);
    private ComplexEventChunk<StreamEvent> expiredEventChunk = null;
    private ExecutionPlanContext executionPlanContext;
    private StreamEvent resetEvent = null;


    @Override
    protected void init(ExpressionExecutor[] attributeExpressionExecutors, ExecutionPlanContext executionPlanContext) {
        this.executionPlanContext = executionPlanContext;
        if (outputExpectsExpiredEvents) {
            expiredEventChunk = new ComplexEventChunk<StreamEvent>(false);
        }
        if (attributeExpressionExecutors.length == 1) {
            length = (Integer) (((ConstantExpressionExecutor) attributeExpressionExecutors[0]).getValue());
        } else {
            throw new ExecutionPlanValidationException("Length batch window should only have one parameter (<int> windowLength), but found " + attributeExpressionExecutors.length + " input attributes");
        }
    }

    @Override
    protected void process(ComplexEventChunk<StreamEvent> streamEventChunk, Processor nextProcessor, StreamEventCloner streamEventCloner) {
        List<ComplexEventChunk<StreamEvent>> streamEventChunks = new ArrayList<ComplexEventChunk<StreamEvent>>();
        synchronized (this) {
            ComplexEventChunk<StreamEvent> outputStreamEventChunk = new ComplexEventChunk<StreamEvent>(true);
            long currentTime = executionPlanContext.getTimestampGenerator().currentTime();
            while (streamEventChunk.hasNext()) {
                StreamEvent streamEvent = streamEventChunk.next();
                StreamEvent clonedStreamEvent = streamEventCloner.copyStreamEvent(streamEvent);
                currentEventChunk.add(clonedStreamEvent);
                count++;
                if (count == length) {
                    if (outputExpectsExpiredEvents) {
                        if (expiredEventChunk.getFirst() != null) {
                            while (expiredEventChunk.hasNext()) {
                                StreamEvent expiredEvent = expiredEventChunk.next();
                                expiredEvent.setTimestamp(currentTime);
                            }
                            outputStreamEventChunk.add(expiredEventChunk.getFirst());
                        }
                    }
                    if (expiredEventChunk != null) {
                        expiredEventChunk.clear();
                    }

                    if (currentEventChunk.getFirst() != null) {

                        // add reset event in front of current events
                        outputStreamEventChunk.add(resetEvent);
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
                        outputStreamEventChunk.add(currentEventChunk.getFirst());
                    }
                    currentEventChunk.clear();
                    count = 0;
                    if (outputStreamEventChunk.getFirst() != null) {
                        streamEventChunks.add(outputStreamEventChunk);
                    }
                }
            }
        }
        for (ComplexEventChunk<StreamEvent> outputStreamEventChunk : streamEventChunks) {
            nextProcessor.process(outputStreamEventChunk);
        }
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
        if (expiredEventChunk != null) {
            return new Object[]{currentEventChunk.getFirst(), expiredEventChunk.getFirst(), count, resetEvent};
        } else {
            return new Object[]{currentEventChunk.getFirst(), count, resetEvent};
        }
    }

    @Override
    public void restoreState(Object[] state) {
        if (state.length > 3) {
            currentEventChunk.clear();
            currentEventChunk.add((StreamEvent) state[0]);
            expiredEventChunk.clear();
            expiredEventChunk.add((StreamEvent) state[1]);
            count = (Integer) state[2];
            resetEvent = (StreamEvent) state[3];

        } else {
            currentEventChunk.clear();
            currentEventChunk.add((StreamEvent) state[0]);
            count = (Integer) state[1];
            resetEvent = (StreamEvent) state[2];
        }
    }

    @Override
    public synchronized StreamEvent find(StateEvent matchingEvent, Finder finder) {
        return finder.find(matchingEvent, expiredEventChunk, streamEventCloner);
    }

    @Override
    public Finder constructFinder(Expression expression, MatchingMetaStateHolder matchingMetaStateHolder, ExecutionPlanContext executionPlanContext,
                                  List<VariableExpressionExecutor> variableExpressionExecutors, Map<String, EventTable> eventTableMap) {
        if (expiredEventChunk == null) {
            expiredEventChunk = new ComplexEventChunk<StreamEvent>(false);
        }
        return OperatorParser.constructOperator(expiredEventChunk, expression, matchingMetaStateHolder,executionPlanContext,variableExpressionExecutors,eventTableMap);
    }
}
