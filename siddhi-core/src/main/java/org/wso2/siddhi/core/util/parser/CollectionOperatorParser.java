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
package org.wso2.siddhi.core.util.parser;

import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.event.MetaComplexEvent;
import org.wso2.siddhi.core.event.state.MetaStateEvent;
import org.wso2.siddhi.core.event.stream.MetaStreamEvent;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.executor.VariableExpressionExecutor;
import org.wso2.siddhi.core.table.EventTable;
import org.wso2.siddhi.core.util.collection.operator.Operator;
import org.wso2.siddhi.core.util.collection.operator.SimpleIndexedOperator;
import org.wso2.siddhi.core.util.collection.operator.SimpleOperator;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.expression.Variable;
import org.wso2.siddhi.query.api.expression.condition.Compare;
import org.wso2.siddhi.query.api.expression.constant.Constant;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created on 1/19/15.
 */
public class CollectionOperatorParser {

    public static Operator parse(Expression expression, MetaComplexEvent matchingMetaComplexEvent, ExecutionPlanContext executionPlanContext, List<VariableExpressionExecutor> variableExpressionExecutors,
                                 Map<String, EventTable> eventTableMap, int matchingStreamIndex, AbstractDefinition candidateDefinition, long withinTime, int indexedPosition) {

        int candidateEventPosition = 0;
        int streamEventSize = 0;

        MetaStreamEvent eventTableStreamEvent = new MetaStreamEvent();
        eventTableStreamEvent.setTableEvent(true);
        eventTableStreamEvent.addInputDefinition(candidateDefinition);
        for (Attribute attribute : candidateDefinition.getAttributeList()) {
            eventTableStreamEvent.addOutputData(attribute);
        }

        MetaStateEvent metaStateEvent = null;
        if (matchingMetaComplexEvent instanceof MetaStreamEvent) {
            metaStateEvent = new MetaStateEvent(2);
            metaStateEvent.addEvent(((MetaStreamEvent) matchingMetaComplexEvent));
            metaStateEvent.addEvent(eventTableStreamEvent);
            candidateEventPosition = 1;
            matchingStreamIndex = 0;
            streamEventSize = 2;
        } else {

            MetaStreamEvent[] metaStreamEvents = ((MetaStateEvent) matchingMetaComplexEvent).getMetaStreamEvents();

            //for join
            for (; candidateEventPosition < metaStreamEvents.length; candidateEventPosition++) {
                MetaStreamEvent metaStreamEvent = metaStreamEvents[candidateEventPosition];
                if (candidateEventPosition != matchingStreamIndex && metaStreamEvent.getLastInputDefinition().equalsIgnoreAnnotations(candidateDefinition)) {
                    metaStateEvent = ((MetaStateEvent) matchingMetaComplexEvent);
                    streamEventSize = metaStreamEvents.length;
                    break;
                }
            }

            if (metaStateEvent == null) {
                metaStateEvent = new MetaStateEvent(metaStreamEvents.length + 1);
                for (MetaStreamEvent metaStreamEvent : metaStreamEvents) {
                    metaStateEvent.addEvent(metaStreamEvent);
                }
                metaStateEvent.addEvent(eventTableStreamEvent);
                candidateEventPosition = metaStreamEvents.length;
                streamEventSize = metaStreamEvents.length + 1;
            }
        }

        ExpressionExecutor expressionExecutor = ExpressionParser.parseExpression(expression,
                metaStateEvent, matchingStreamIndex, eventTableMap, variableExpressionExecutors, executionPlanContext, false, 0);
        return new SimpleOperator(expressionExecutor, candidateEventPosition, matchingStreamIndex, streamEventSize, withinTime, metaStateEvent.getMetaStreamEvent(matchingStreamIndex).getLastInputDefinition().getAttributeList().size(), indexedPosition);
    }

    public static Operator parse(Expression expression, MetaComplexEvent matchingMetaComplexEvent, ExecutionPlanContext executionPlanContext, List<VariableExpressionExecutor> variableExpressionExecutors,
                                 Map<String, EventTable> eventTableMap, int matchingStreamIndex, AbstractDefinition candidateDefinition, long withinTime, String indexedAttribute, int indexedPosition) {

        if (indexedAttribute == null) {
            return CollectionOperatorParser.parse(expression, matchingMetaComplexEvent, executionPlanContext, variableExpressionExecutors, eventTableMap, matchingStreamIndex, candidateDefinition, withinTime, indexedPosition);
        }

        if (expression instanceof Compare && ((Compare) expression).getOperator() == Compare.Operator.EQUAL) {
            Compare compare = (Compare) expression;
            if ((compare.getLeftExpression() instanceof Variable || compare.getLeftExpression() instanceof Constant)
                    && (compare.getRightExpression() instanceof Variable || compare.getRightExpression() instanceof Constant)) {

                boolean leftSideIndexed = false;
                boolean rightSideIndexed = false;

                if (isTableIndexVariable(matchingMetaComplexEvent, matchingStreamIndex, compare.getLeftExpression(), candidateDefinition, indexedAttribute)) {
                    leftSideIndexed = true;
                }

                if (isTableIndexVariable(matchingMetaComplexEvent, matchingStreamIndex, compare.getRightExpression(), candidateDefinition, indexedAttribute)) {
                    rightSideIndexed = true;
                }

                AbstractDefinition matchingStreamDefinition;
                if (matchingMetaComplexEvent instanceof MetaStateEvent) {
                    matchingStreamDefinition = ((MetaStateEvent) matchingMetaComplexEvent).getMetaStreamEvent(matchingStreamIndex).getLastInputDefinition();
                } else {
                    matchingStreamDefinition = ((MetaStreamEvent) matchingMetaComplexEvent).getLastInputDefinition();
                }

                if (leftSideIndexed && !rightSideIndexed) {
                    ExpressionExecutor expressionExecutor = ExpressionParser.parseExpression(compare.getRightExpression(),
                            matchingMetaComplexEvent, matchingStreamIndex, eventTableMap, variableExpressionExecutors, executionPlanContext, false, 0);
                    return new SimpleIndexedOperator(expressionExecutor, matchingStreamIndex, withinTime, matchingStreamDefinition.getAttributeList().size(), indexedPosition);

                } else if (!leftSideIndexed && rightSideIndexed) {
                    ExpressionExecutor expressionExecutor = ExpressionParser.parseExpression(compare.getLeftExpression(),
                            matchingMetaComplexEvent, matchingStreamIndex, eventTableMap, variableExpressionExecutors, executionPlanContext, false, 0);
                    return new SimpleIndexedOperator(expressionExecutor, matchingStreamIndex, withinTime, matchingStreamDefinition.getAttributeList().size(), indexedPosition);

                }

            }
        }
        return CollectionOperatorParser.parse(expression, matchingMetaComplexEvent, executionPlanContext, variableExpressionExecutors, eventTableMap, matchingStreamIndex, candidateDefinition, withinTime, indexedPosition);
    }

    public static Operator parse(Expression expression, MetaComplexEvent matchingMetaComplexEvent, ExecutionPlanContext executionPlanContext, List<VariableExpressionExecutor> variableExpressionExecutors,
                                 Map<String, EventTable> eventTableMap, int matchingStreamIndex, AbstractDefinition candidateDefinition, long withinTime) {
        return CollectionOperatorParser.parse(expression, matchingMetaComplexEvent, executionPlanContext, variableExpressionExecutors, eventTableMap, matchingStreamIndex, candidateDefinition, withinTime, 0);
    }


    private static boolean isTableIndexVariable(MetaComplexEvent metaComplexEvent, int matchingStreamIndex, Expression expression, AbstractDefinition candidateDefinition, String indexedAttribute) {
        if (expression instanceof Variable) {
            Variable variable = (Variable) expression;
            if (variable.getStreamId() != null) {
                MetaStreamEvent tableStreamEvent = getTableMetaStreamEvent(metaComplexEvent, matchingStreamIndex, candidateDefinition);
                if (tableStreamEvent != null) {
                    if ((tableStreamEvent.getInputReferenceId() != null && variable.getStreamId().equals(tableStreamEvent.getInputReferenceId())) ||
                            (tableStreamEvent.getLastInputDefinition().getId().equals(variable.getStreamId()))) {
                        if (Arrays.asList(tableStreamEvent.getLastInputDefinition().getAttributeNameArray()).contains(indexedAttribute)) {
                            return true;
                        }
                    }
                } else {
                    if (candidateDefinition.getId().equals(variable.getStreamId())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static MetaStreamEvent getTableMetaStreamEvent(MetaComplexEvent metaComplexEvent, int matchingStreamIndex, AbstractDefinition candidateDefinition) {
        if (metaComplexEvent instanceof MetaStateEvent) {
            MetaStreamEvent[] metaStreamEvents = ((MetaStateEvent) metaComplexEvent).getMetaStreamEvents();
            int candidateEventPosition = 0;
            //for join
            for (; candidateEventPosition < metaStreamEvents.length; candidateEventPosition++) {
                MetaStreamEvent metaStreamEvent = metaStreamEvents[candidateEventPosition];
                if (candidateEventPosition != matchingStreamIndex && metaStreamEvent.getLastInputDefinition().equalsIgnoreAnnotations(candidateDefinition)) {
                    return metaStreamEvent;
                }
            }
        }
        return null;
    }


}
