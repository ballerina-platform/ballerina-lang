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

package org.wso2.siddhi.extension.eventtable.hazelcast;

import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.event.MetaComplexEvent;
import org.wso2.siddhi.core.event.state.MetaStateEvent;
import org.wso2.siddhi.core.event.stream.MetaStreamEvent;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.executor.VariableExpressionExecutor;
import org.wso2.siddhi.core.table.EventTable;
import org.wso2.siddhi.core.util.collection.operator.Operator;
import org.wso2.siddhi.core.util.parser.ExpressionParser;
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
 * HazelcastOperatorParser will parse an expression and constructs the most appropriate
 * Operator for the expression to perform Hazelcast event table related operations.
 */
public class HazelcastOperatorParser {

    private HazelcastOperatorParser() {
    }

    /**
     * Method that constructs the Operator for non-indexed Hazelcast event table related operations.
     *
     * @param expression                  Expression.
     * @param matchingMetaComplexEvent    Meta information about ComplexEvent.
     * @param executionPlanContext        Execution plan context.
     * @param variableExpressionExecutors Variable expression executors.
     * @param eventTableMap               Event table map.
     * @param matchingStreamIndex         Matching stream index.
     * @param candidateDefinition         candidate definition.
     * @param withinTime                  Within time frame.
     * @param indexedPosition             Indexed position.
     * @return HazelcastOperator
     */
    public static Operator parse(Expression expression, MetaComplexEvent matchingMetaComplexEvent,
                                 ExecutionPlanContext executionPlanContext,
                                 List<VariableExpressionExecutor> variableExpressionExecutors,
                                 Map<String, EventTable> eventTableMap, int matchingStreamIndex,
                                 AbstractDefinition candidateDefinition, long withinTime, int indexedPosition) {
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
            // For join.
            for (; candidateEventPosition < metaStreamEvents.length; candidateEventPosition++) {
                MetaStreamEvent metaStreamEvent = metaStreamEvents[candidateEventPosition];
                if (candidateEventPosition != matchingStreamIndex &&
                        metaStreamEvent.getLastInputDefinition().equalsIgnoreAnnotations(candidateDefinition)) {
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
        ExpressionExecutor expressionExecutor = ExpressionParser.parseExpression(expression, metaStateEvent,
                matchingStreamIndex, eventTableMap, variableExpressionExecutors, executionPlanContext, false, 0);
        return new HazelcastOperator(expressionExecutor, candidateEventPosition, matchingStreamIndex, streamEventSize,
                withinTime, metaStateEvent.getMetaStreamEvent(matchingStreamIndex)
                .getLastInputDefinition().getAttributeList().size(), indexedPosition);
    }

    /**
     * Method that constructs the Operator for Indexed Hazelcast event table related operations.
     *
     * @param expression                  Expression.
     * @param matchingMetaComplexEvent    Meta information about ComplexEvent.
     * @param executionPlanContext        Execution plan context.
     * @param variableExpressionExecutors Variable expression executors.
     * @param eventTableMap               Event table map.
     * @param matchingStreamIndex         Matching stream index.
     * @param candidateDefinition         candidate definition.
     * @param withinTime                  Within time frame.
     * @param indexedAttribute            Indexed attribute.
     * @param indexedPosition             Indexed position.
     * @return HazelcastIndexedOperator or a HazelcastOperator.
     */
    public static Operator parse(Expression expression, MetaComplexEvent matchingMetaComplexEvent,
                                 ExecutionPlanContext executionPlanContext,
                                 List<VariableExpressionExecutor> variableExpressionExecutors,
                                 Map<String, EventTable> eventTableMap,
                                 int matchingStreamIndex, AbstractDefinition candidateDefinition, long withinTime,
                                 String indexedAttribute, int indexedPosition) {
        if (indexedAttribute == null) {
            return HazelcastOperatorParser.parse(expression, matchingMetaComplexEvent, executionPlanContext,
                    variableExpressionExecutors, eventTableMap, matchingStreamIndex, candidateDefinition,
                    withinTime, indexedPosition);
        }
        if (expression instanceof Compare && ((Compare) expression).getOperator() == Compare.Operator.EQUAL) {
            Compare compare = (Compare) expression;
            if ((compare.getLeftExpression() instanceof Variable || compare.getLeftExpression() instanceof Constant) &&
                    (compare.getRightExpression() instanceof Variable ||
                            compare.getRightExpression() instanceof Constant)) {
                boolean leftSideIndexed = false;
                boolean rightSideIndexed = false;
                if (isTableIndexVariable(matchingMetaComplexEvent, matchingStreamIndex, compare.getLeftExpression(),
                        candidateDefinition, indexedAttribute)) {
                    leftSideIndexed = true;
                }
                if (isTableIndexVariable(matchingMetaComplexEvent, matchingStreamIndex, compare.getRightExpression(),
                        candidateDefinition, indexedAttribute)) {
                    rightSideIndexed = true;
                }

                AbstractDefinition matchingStreamDefinition;
                if (matchingMetaComplexEvent instanceof MetaStateEvent) {
                    matchingStreamDefinition = ((MetaStateEvent) matchingMetaComplexEvent)
                            .getMetaStreamEvent(matchingStreamIndex).getLastInputDefinition();
                } else {
                    matchingStreamDefinition = ((MetaStreamEvent) matchingMetaComplexEvent).getLastInputDefinition();
                }

                if (leftSideIndexed && !rightSideIndexed) {
                    ExpressionExecutor expressionExecutor = ExpressionParser.parseExpression(compare.getRightExpression(),
                            matchingMetaComplexEvent, matchingStreamIndex, eventTableMap,
                            variableExpressionExecutors, executionPlanContext, false, 0);
                    return new HazelcastIndexedOperator(expressionExecutor, matchingStreamIndex, withinTime,
                            matchingStreamDefinition.getAttributeList().size(), indexedPosition);
                } else if (!leftSideIndexed && rightSideIndexed) {
                    ExpressionExecutor expressionExecutor = ExpressionParser.parseExpression(compare.getLeftExpression(),
                            matchingMetaComplexEvent, matchingStreamIndex, eventTableMap,
                            variableExpressionExecutors, executionPlanContext, false, 0);
                    return new HazelcastIndexedOperator(expressionExecutor, matchingStreamIndex, withinTime,
                            matchingStreamDefinition.getAttributeList().size(), indexedPosition);
                }
            }
        }
        return HazelcastOperatorParser.parse(expression, matchingMetaComplexEvent, executionPlanContext,
                variableExpressionExecutors, eventTableMap, matchingStreamIndex, candidateDefinition,
                withinTime, indexedPosition);
    }

    /**
     * Checks whether a give expression is a table index variable.
     *
     * @param metaComplexEvent    Meta information about ComplexEvent.
     * @param matchingStreamIndex Matching stream index.
     * @param expression          Expression.
     * @param candidateDefinition candidate definition.
     * @param indexedAttribute    Indexed attribute.
     * @return boolean representing whether an expression is a indexed variable.
     */
    private static boolean isTableIndexVariable(MetaComplexEvent metaComplexEvent, int matchingStreamIndex,
                                                Expression expression, AbstractDefinition candidateDefinition,
                                                String indexedAttribute) {
        if (expression instanceof Variable) {
            Variable variable = (Variable) expression;
            if (variable.getStreamId() != null) {
                MetaStreamEvent tableStreamEvent = getTableMetaStreamEvent(metaComplexEvent, matchingStreamIndex,
                        candidateDefinition);
                if (tableStreamEvent != null) {
                    if ((tableStreamEvent.getInputReferenceId() != null &&
                            variable.getStreamId().equals(tableStreamEvent.getInputReferenceId())) ||
                            (tableStreamEvent.getLastInputDefinition().getId().equals(variable.getStreamId()))) {
                        if (Arrays.asList(tableStreamEvent.getLastInputDefinition().getAttributeNameArray())
                                .contains(indexedAttribute)) {
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

    /**
     * Returns the table meta stream event.
     *
     * @param metaComplexEvent    Meta information about ComplexEvent.
     * @param matchingStreamIndex Matching stream index.
     * @param candidateDefinition candidate definition.
     * @return Meta stream event.
     */
    private static MetaStreamEvent getTableMetaStreamEvent(MetaComplexEvent metaComplexEvent, int matchingStreamIndex,
                                                           AbstractDefinition candidateDefinition) {
        if (metaComplexEvent instanceof MetaStateEvent) {
            MetaStreamEvent[] metaStreamEvents = ((MetaStateEvent) metaComplexEvent).getMetaStreamEvents();
            int candidateEventPosition = 0;
            // For join.
            for (; candidateEventPosition < metaStreamEvents.length; candidateEventPosition++) {
                MetaStreamEvent metaStreamEvent = metaStreamEvents[candidateEventPosition];
                if (candidateEventPosition != matchingStreamIndex &&
                        metaStreamEvent.getLastInputDefinition().equalsIgnoreAnnotations(candidateDefinition)) {
                    return metaStreamEvent;
                }
            }
        }
        return null;
    }
}
