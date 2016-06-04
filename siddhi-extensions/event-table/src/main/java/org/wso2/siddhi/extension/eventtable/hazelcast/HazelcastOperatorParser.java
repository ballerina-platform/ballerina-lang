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
import org.wso2.siddhi.core.event.stream.MetaStreamEvent;
import org.wso2.siddhi.core.exception.OperationNotSupportedException;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.executor.VariableExpressionExecutor;
import org.wso2.siddhi.core.table.EventTable;
import org.wso2.siddhi.core.util.collection.operator.*;
import org.wso2.siddhi.core.util.parser.ExpressionParser;
import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.expression.Variable;
import org.wso2.siddhi.query.api.expression.condition.Compare;
import org.wso2.siddhi.query.api.expression.constant.Constant;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * HazelcastOperatorParser will parse an expression and constructs the most appropriate
 * Operator for the expression to perform Hazelcast event table related operations.
 */
public class HazelcastOperatorParser {

    private HazelcastOperatorParser() {
    }

    public static Operator constructOperator(Object candidateEvents, Expression expression,
                                             MatchingMetaStateHolder matchingMetaStateHolder,
                                             ExecutionPlanContext executionPlanContext,
                                             List<VariableExpressionExecutor> variableExpressionExecutors,
                                             Map<String, EventTable> eventTableMap) {
        if (candidateEvents instanceof HazelcastPrimaryKeyEventHolder) {
            if (expression instanceof Compare && ((Compare) expression).getOperator() == Compare.Operator.EQUAL) {
                Compare compare = (Compare) expression;
                if ((compare.getLeftExpression() instanceof Variable || compare.getLeftExpression() instanceof Constant)
                        && (compare.getRightExpression() instanceof Variable || compare.getRightExpression() instanceof Constant)) {

                    boolean leftSideIndexed = false;
                    boolean rightSideIndexed = false;

                    if (isTableIndexVariable(matchingMetaStateHolder, compare.getLeftExpression(), ((HazelcastPrimaryKeyEventHolder) candidateEvents).getIndexAttribute())) {
                        leftSideIndexed = true;
                    }

                    if (isTableIndexVariable(matchingMetaStateHolder, compare.getRightExpression(), ((HazelcastPrimaryKeyEventHolder) candidateEvents).getIndexAttribute())) {
                        rightSideIndexed = true;
                    }

                    if (leftSideIndexed && !rightSideIndexed) {
                        ExpressionExecutor expressionExecutor = ExpressionParser.parseExpression(compare.getRightExpression(),
                                matchingMetaStateHolder.getMetaStateEvent(), matchingMetaStateHolder.getDefaultStreamEventIndex(), eventTableMap, variableExpressionExecutors, executionPlanContext, false, 0);
                        return new HazelcastPrimaryKeyOperator(expressionExecutor, matchingMetaStateHolder.getCandidateEventIndex(), ((HazelcastPrimaryKeyEventHolder) candidateEvents).getIndexPosition());

                    } else if (!leftSideIndexed && rightSideIndexed) {
                        ExpressionExecutor expressionExecutor = ExpressionParser.parseExpression(compare.getLeftExpression(),
                                matchingMetaStateHolder.getMetaStateEvent(), matchingMetaStateHolder.getDefaultStreamEventIndex(), eventTableMap, variableExpressionExecutors, executionPlanContext, false, 0);
                        return new HazelcastPrimaryKeyOperator(expressionExecutor, matchingMetaStateHolder.getCandidateEventIndex(), ((HazelcastPrimaryKeyEventHolder) candidateEvents).getIndexPosition());

                    }
                }
            }
            //fallback to not using primary key
            ExpressionExecutor expressionExecutor = ExpressionParser.parseExpression(expression,
                    matchingMetaStateHolder.getMetaStateEvent(), matchingMetaStateHolder.getDefaultStreamEventIndex(), eventTableMap, variableExpressionExecutors, executionPlanContext, false, 0);
            return new HazelcastMapOperator(expressionExecutor, matchingMetaStateHolder.getCandidateEventIndex());
        } else if (candidateEvents instanceof Collection) {
            ExpressionExecutor expressionExecutor = ExpressionParser.parseExpression(expression,
                    matchingMetaStateHolder.getMetaStateEvent(), matchingMetaStateHolder.getDefaultStreamEventIndex(), eventTableMap, variableExpressionExecutors, executionPlanContext, false, 0);
            return new HazelcastCollectionOperator(expressionExecutor, matchingMetaStateHolder.getCandidateEventIndex());
        } else {
            throw new OperationNotSupportedException(candidateEvents.getClass() + " is not supported by OperatorParser!");
        }
    }

    private static boolean isTableIndexVariable(MatchingMetaStateHolder matchingMetaStateHolder, Expression expression, String indexAttribute) {
        if (expression instanceof Variable) {
            Variable variable = (Variable) expression;
            if (variable.getStreamId() != null) {
                MetaStreamEvent tableStreamEvent = matchingMetaStateHolder.getMetaStateEvent().getMetaStreamEvent(matchingMetaStateHolder.getCandidateEventIndex());
                if (tableStreamEvent != null) {
                    if ((tableStreamEvent.getInputReferenceId() != null && variable.getStreamId().equals(tableStreamEvent.getInputReferenceId())) ||
                            (tableStreamEvent.getLastInputDefinition().getId().equals(variable.getStreamId()))) {
                        if (Arrays.asList(tableStreamEvent.getLastInputDefinition().getAttributeNameArray()).contains(indexAttribute)) {
                            return true;
                        }
                    }
                } else {
                    if (matchingMetaStateHolder.getCandsidateDefinition().getId().equals(variable.getStreamId())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

}