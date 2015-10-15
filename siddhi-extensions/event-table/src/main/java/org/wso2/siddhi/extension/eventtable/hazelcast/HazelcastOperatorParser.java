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

public class HazelcastOperatorParser {

    private HazelcastOperatorParser() {
    }

    /**
     * Method that constructs the Operator for non-indexed Hazelcast event table related operations
     *
     * @param expression                  Expression
     * @param metaComplexEvent            Meta information about ComplexEvent.
     * @param executionPlanContext        Execution plan context
     * @param variableExpressionExecutors Variable expression executors
     * @param eventTableMap               Event table map
     * @param matchingStreamIndex         Matching stream index
     * @param candidateDefinition         candidate definition
     * @param withinTime                  Within time frame
     * @return HazelcastOperator
     */
    public static Operator parse(Expression expression, MetaComplexEvent metaComplexEvent, ExecutionPlanContext executionPlanContext,
                                 List<VariableExpressionExecutor> variableExpressionExecutors, Map<String, EventTable> eventTableMap,
                                 int matchingStreamIndex, AbstractDefinition candidateDefinition, long withinTime) {

        int candidateEventPosition = 0;
        int streamEventSize = 0;

        MetaStreamEvent eventTableStreamEvent = new MetaStreamEvent();
        eventTableStreamEvent.setTableEvent(true);
        eventTableStreamEvent.addInputDefinition(candidateDefinition);
        for (Attribute attribute : candidateDefinition.getAttributeList()) {
            eventTableStreamEvent.addOutputData(attribute);
        }

        MetaStateEvent metaStateEvent = null;
        if (metaComplexEvent instanceof MetaStreamEvent) {
            metaStateEvent = new MetaStateEvent(2);
            metaStateEvent.addEvent((MetaStreamEvent) metaComplexEvent);
            metaStateEvent.addEvent(eventTableStreamEvent);
            candidateEventPosition = 1;
            matchingStreamIndex = 0;
            streamEventSize = 2;
        } else {

            MetaStreamEvent[] metaStreamEvents = ((MetaStateEvent) metaComplexEvent).getMetaStreamEvents();

            //for join
            for (; candidateEventPosition < metaStreamEvents.length; candidateEventPosition++) {
                MetaStreamEvent metaStreamEvent = metaStreamEvents[candidateEventPosition];
                if (candidateEventPosition != matchingStreamIndex && metaStreamEvent.getLastInputDefinition().equalsIgnoreAnnotations(candidateDefinition)) {
                    metaStateEvent = ((MetaStateEvent) metaComplexEvent);
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
        return new HazelcastOperator(expressionExecutor, candidateEventPosition, matchingStreamIndex, streamEventSize,
                withinTime, metaStateEvent.getMetaStreamEvent(matchingStreamIndex).getLastInputDefinition().getAttributeList().size());
    }

    /**
     * Method that constructs the Operator for Indexed Hazelcast event table related operations
     *
     * @param expression                  Expression
     * @param metaComplexEvent            Meta information about ComplexEvent.
     * @param executionPlanContext        Execution plan context
     * @param variableExpressionExecutors Variable expression executors
     * @param eventTableMap               Event table map
     * @param matchingStreamIndex         Matching stream index
     * @param candidateDefinition         candidate definition
     * @param withinTime                  Within time frame
     * @param indexedAttribute            Indexed attribute
     * @return HazelcastIndexedOperator or a HazelcastOperator
     */
    public static Operator parse(Expression expression, MetaComplexEvent metaComplexEvent, ExecutionPlanContext executionPlanContext,
                                 List<VariableExpressionExecutor> variableExpressionExecutors, Map<String, EventTable> eventTableMap,
                                 int matchingStreamIndex, AbstractDefinition candidateDefinition, long withinTime, String indexedAttribute) {

        if (indexedAttribute == null) {
            return HazelcastOperatorParser.parse(expression, metaComplexEvent, executionPlanContext, variableExpressionExecutors,
                    eventTableMap, matchingStreamIndex, candidateDefinition, withinTime);
        }

        if (expression instanceof Compare && ((Compare) expression).getOperator() == Compare.Operator.EQUAL) {
            Compare compare = (Compare) expression;
            if ((compare.getLeftExpression() instanceof Variable || compare.getLeftExpression() instanceof Constant)
                    && (compare.getRightExpression() instanceof Variable || compare.getRightExpression() instanceof Constant)) {

                boolean leftSideIndexed = false;
                boolean rightSideIndexed = false;

                if (isTableIndexVariable(metaComplexEvent, matchingStreamIndex, compare.getLeftExpression(), candidateDefinition, indexedAttribute)) {
                    leftSideIndexed = true;
                }

                if (isTableIndexVariable(metaComplexEvent, matchingStreamIndex, compare.getRightExpression(), candidateDefinition, indexedAttribute)) {
                    rightSideIndexed = true;
                }

                if (leftSideIndexed && !rightSideIndexed) {
                    ExpressionExecutor expressionExecutor = ExpressionParser.parseExpression(compare.getRightExpression(),
                            metaComplexEvent, matchingStreamIndex, eventTableMap,
                            variableExpressionExecutors, executionPlanContext, false, 0);
                    return new HazelcastIndexedOperator(expressionExecutor, matchingStreamIndex, withinTime);

                } else if (!leftSideIndexed && rightSideIndexed) {
                    ExpressionExecutor expressionExecutor = ExpressionParser.parseExpression(compare.getLeftExpression(),
                            metaComplexEvent, matchingStreamIndex, eventTableMap,
                            variableExpressionExecutors, executionPlanContext, false, 0);
                    return new HazelcastIndexedOperator(expressionExecutor, matchingStreamIndex, withinTime);

                }

            }
        }
        return HazelcastOperatorParser.parse(expression, metaComplexEvent, executionPlanContext, variableExpressionExecutors,
                eventTableMap, matchingStreamIndex, candidateDefinition, withinTime);
    }

    /**
     * Checks whether a give expression is a table index variable
     *
     * @param metaComplexEvent    Meta information about ComplexEvent.
     * @param matchingStreamIndex Matching stream index
     * @param expression          Expression
     * @param candidateDefinition candidate definition
     * @param indexedAttribute    Indexed attribute
     * @return boolean representing whether an expression is a indexed variable
     */
    private static boolean isTableIndexVariable(MetaComplexEvent metaComplexEvent, int matchingStreamIndex, Expression expression,
                                                AbstractDefinition candidateDefinition, String indexedAttribute) {
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

    /**
     * Returns the table meta stream event
     *
     * @param metaComplexEvent    Meta information about ComplexEvent.
     * @param matchingStreamIndex Matching stream index
     * @param candidateDefinition candidate definition
     * @return Meta stream event
     */
    private static MetaStreamEvent getTableMetaStreamEvent(MetaComplexEvent metaComplexEvent, int matchingStreamIndex,
                                                           AbstractDefinition candidateDefinition) {
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
