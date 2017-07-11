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

package org.wso2.siddhi.extension.table.hazelcast;

import org.wso2.siddhi.core.config.SiddhiAppContext;
import org.wso2.siddhi.core.event.stream.MetaStreamEvent;
import org.wso2.siddhi.core.exception.OperationNotSupportedException;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.executor.VariableExpressionExecutor;
import org.wso2.siddhi.core.table.Table;
import org.wso2.siddhi.core.util.collection.operator.MatchingMetaInfoHolder;
import org.wso2.siddhi.core.util.collection.operator.Operator;
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

    public static Operator constructOperator(Object storeEvents, Expression expression,
                                             MatchingMetaInfoHolder matchingMetaInfoHolder,
                                             SiddhiAppContext siddhiAppContext,
                                             List<VariableExpressionExecutor> variableExpressionExecutors,
                                             Map<String, Table> tableMap, String queryName) {
        if (storeEvents instanceof HazelcastPrimaryKeyEventHolder) {
            if (expression instanceof Compare && ((Compare) expression).getOperator() == Compare.Operator.EQUAL) {
                Compare compare = (Compare) expression;
                if ((compare.getLeftExpression() instanceof Variable || compare.getLeftExpression() instanceof Constant)
                        && (compare.getRightExpression() instanceof Variable || compare.getRightExpression()
                        instanceof Constant)) {

                    boolean leftSideIndexed = false;
                    boolean rightSideIndexed = false;

                    if (isTableIndexVariable(matchingMetaInfoHolder, compare.getLeftExpression(), (
                            (HazelcastPrimaryKeyEventHolder) storeEvents).getIndexAttribute())) {
                        leftSideIndexed = true;
                    }

                    if (isTableIndexVariable(matchingMetaInfoHolder, compare.getRightExpression(), (
                            (HazelcastPrimaryKeyEventHolder) storeEvents).getIndexAttribute())) {
                        rightSideIndexed = true;
                    }

                    if (leftSideIndexed && !rightSideIndexed) {
                        ExpressionExecutor expressionExecutor = ExpressionParser.parseExpression(compare.getRightExpression(),
                                matchingMetaInfoHolder.getMetaStateEvent(), matchingMetaInfoHolder.getCurrentState(), tableMap, variableExpressionExecutors, siddhiAppContext, false, 0, queryName);
                        return new HazelcastPrimaryKeyOperator(expressionExecutor, matchingMetaInfoHolder.getStoreEventIndex(), ((HazelcastPrimaryKeyEventHolder) storeEvents).getIndexPosition());

                    } else if (!leftSideIndexed && rightSideIndexed) {
                        ExpressionExecutor expressionExecutor = ExpressionParser.parseExpression(compare.getLeftExpression(),
                                matchingMetaInfoHolder.getMetaStateEvent(), matchingMetaInfoHolder.getCurrentState(), tableMap, variableExpressionExecutors, siddhiAppContext, false, 0, queryName);
                        return new HazelcastPrimaryKeyOperator(expressionExecutor, matchingMetaInfoHolder.getStoreEventIndex(), ((HazelcastPrimaryKeyEventHolder) storeEvents).getIndexPosition());

                    }
                }
            }
            //fallback to not using primary key
            ExpressionExecutor expressionExecutor = ExpressionParser.parseExpression(expression,
                    matchingMetaInfoHolder.getMetaStateEvent(), matchingMetaInfoHolder.getCurrentState(), tableMap, variableExpressionExecutors, siddhiAppContext, false, 0, queryName);
            return new HazelcastMapOperator(expressionExecutor, matchingMetaInfoHolder.getStoreEventIndex());
        } else if (storeEvents instanceof Collection) {
            ExpressionExecutor expressionExecutor = ExpressionParser.parseExpression(expression,
                    matchingMetaInfoHolder.getMetaStateEvent(), matchingMetaInfoHolder.getCurrentState(), tableMap, variableExpressionExecutors, siddhiAppContext, false, 0, queryName);
            return new HazelcastCollectionOperator(expressionExecutor, matchingMetaInfoHolder.getStoreEventIndex());
        } else {
            throw new OperationNotSupportedException(storeEvents.getClass() + " is not supported by OperatorParser!");
        }
    }

    private static boolean isTableIndexVariable(MatchingMetaInfoHolder matchingMetaInfoHolder, Expression expression,
                                                String indexAttribute) {
        if (expression instanceof Variable) {
            Variable variable = (Variable) expression;
            if (variable.getStreamId() != null) {
                MetaStreamEvent tableStreamEvent = matchingMetaInfoHolder.getMetaStateEvent().getMetaStreamEvent
                        (matchingMetaInfoHolder.getStoreEventIndex());
                if (tableStreamEvent != null) {
                    if ((tableStreamEvent.getInputReferenceId() != null && variable.getStreamId().equals
                            (tableStreamEvent.getInputReferenceId())) ||
                            (tableStreamEvent.getLastInputDefinition().getId().equals(variable.getStreamId()))) {
                        if (Arrays.asList(tableStreamEvent.getLastInputDefinition().getAttributeNameArray()).contains
                                (indexAttribute)) {
                            return true;
                        }
                    }
                } else {
                    if (matchingMetaInfoHolder.getStoreDefinition().getId().equals(variable.getStreamId())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

}