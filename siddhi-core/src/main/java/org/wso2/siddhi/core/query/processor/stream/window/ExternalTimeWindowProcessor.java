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
import org.wso2.siddhi.core.table.EventTable;
import org.wso2.siddhi.core.util.collection.operator.Finder;
import org.wso2.siddhi.core.util.parser.CollectionOperatorParser;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.exception.ExecutionPlanValidationException;
import org.wso2.siddhi.query.api.expression.Expression;

import java.util.List;
import java.util.Map;

public class ExternalTimeWindowProcessor extends WindowProcessor implements FindableProcessor {
    static final Logger log = Logger.getLogger(ExternalTimeWindowProcessor.class);
    private long timeToKeep;
    private ComplexEventChunk<StreamEvent> expiredEventChunk;
    private VariableExpressionExecutor timeStampVariableExpressionExecutor;

    @Override
    protected void init(ExpressionExecutor[] attributeExpressionExecutors, ExecutionPlanContext executionPlanContext, boolean outputExpectsExpiredEvents) {
        this.expiredEventChunk = new ComplexEventChunk<StreamEvent>(false);
        if (attributeExpressionExecutors.length == 2) {
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
        } else {
            throw new ExecutionPlanValidationException("ExternalTime window should only have two parameter (<long> timeStamp, <int|long|time> windowTime), but found " + attributeExpressionExecutors.length + " input attributes");
        }
    }

    @Override
    protected synchronized void process(ComplexEventChunk<StreamEvent> streamEventChunk, Processor nextProcessor, StreamEventCloner streamEventCloner) {
        synchronized (this) {
            while (streamEventChunk.hasNext()) {

                StreamEvent streamEvent = streamEventChunk.next();
                long currentTime = (Long) streamEvent.getAttribute(timeStampVariableExpressionExecutor.getPosition());

                StreamEvent clonedEvent = streamEventCloner.copyStreamEvent(streamEvent);
                clonedEvent.setType(StreamEvent.Type.EXPIRED);

                while (expiredEventChunk.hasNext()) {
                    StreamEvent expiredEvent = expiredEventChunk.next();
                    long expiredEventTime = (Long) expiredEvent.getAttribute(timeStampVariableExpressionExecutor.getPosition());
                    long timeDiff = expiredEventTime - currentTime + timeToKeep;
                    if (timeDiff <= 0) {
                        expiredEventChunk.remove();
                        expiredEvent.setTimestamp(currentTime);
                        streamEventChunk.insertBeforeCurrent(expiredEvent);
                    } else {
                        expiredEventChunk.reset();
                        break;
                    }
                }

                if (streamEvent.getType() == StreamEvent.Type.CURRENT) {
                    this.expiredEventChunk.add(clonedEvent);
                }
                expiredEventChunk.reset();
            }
        }
        nextProcessor.process(streamEventChunk);
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
        return new Object[]{expiredEventChunk.getFirst()};
    }

    @Override
    public void restoreState(Object[] state) {
        expiredEventChunk.clear();
        expiredEventChunk.add((StreamEvent) state[0]);
    }

    @Override
    public synchronized StreamEvent find(ComplexEvent matchingEvent, Finder finder) {
        return finder.find(matchingEvent, expiredEventChunk, streamEventCloner);
    }

    @Override
    public Finder constructFinder(Expression expression, MetaComplexEvent matchingMetaComplexEvent, ExecutionPlanContext executionPlanContext, List<VariableExpressionExecutor> variableExpressionExecutors, Map<String, EventTable> eventTableMap, int matchingStreamIndex, long withinTime) {
        return CollectionOperatorParser.parse(expression, matchingMetaComplexEvent, executionPlanContext, variableExpressionExecutors, eventTableMap, matchingStreamIndex, inputDefinition, withinTime);
    }
}
