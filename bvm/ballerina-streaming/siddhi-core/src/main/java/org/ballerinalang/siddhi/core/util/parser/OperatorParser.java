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

package org.ballerinalang.siddhi.core.util.parser;

import org.ballerinalang.siddhi.core.config.SiddhiAppContext;
import org.ballerinalang.siddhi.core.event.ComplexEventChunk;
import org.ballerinalang.siddhi.core.event.stream.MetaStreamEvent;
import org.ballerinalang.siddhi.core.exception.OperationNotSupportedException;
import org.ballerinalang.siddhi.core.executor.ExpressionExecutor;
import org.ballerinalang.siddhi.core.executor.VariableExpressionExecutor;
import org.ballerinalang.siddhi.core.table.Table;
import org.ballerinalang.siddhi.core.table.holder.IndexedEventHolder;
import org.ballerinalang.siddhi.core.util.collection.executor.CollectionExecutor;
import org.ballerinalang.siddhi.core.util.collection.expression.AndMultiPrimaryKeyCollectionExpression;
import org.ballerinalang.siddhi.core.util.collection.expression.AttributeCollectionExpression;
import org.ballerinalang.siddhi.core.util.collection.expression.CollectionExpression;
import org.ballerinalang.siddhi.core.util.collection.expression.CompareCollectionExpression;
import org.ballerinalang.siddhi.core.util.collection.operator.CollectionOperator;
import org.ballerinalang.siddhi.core.util.collection.operator.EventChunkOperator;
import org.ballerinalang.siddhi.core.util.collection.operator.IndexOperator;
import org.ballerinalang.siddhi.core.util.collection.operator.MapOperator;
import org.ballerinalang.siddhi.core.util.collection.operator.MatchingMetaInfoHolder;
import org.ballerinalang.siddhi.core.util.collection.operator.Operator;
import org.ballerinalang.siddhi.core.util.collection.operator.OverwriteTableIndexOperator;
import org.ballerinalang.siddhi.query.api.expression.Expression;
import org.ballerinalang.siddhi.query.api.expression.Variable;
import org.ballerinalang.siddhi.query.api.expression.condition.Compare;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.ballerinalang.siddhi.core.util.collection.expression.CollectionExpression.CollectionScope.INDEXED_RESULT_SET;
import static org.ballerinalang.siddhi.core.util.collection.expression.CollectionExpression.CollectionScope.PRIMARY_KEY_RESULT_SET;

/**
 * Class to parse {@link Operator}.
 */
public class OperatorParser {

    public static Operator constructOperator(Object storeEvents, Expression expression,
                                             MatchingMetaInfoHolder matchingMetaInfoHolder,
                                             SiddhiAppContext siddhiAppContext,
                                             List<VariableExpressionExecutor> variableExpressionExecutors,
                                             Map<String, Table> tableMap, String queryName) {
        if (storeEvents instanceof IndexedEventHolder) {
            CollectionExpression collectionExpression = CollectionExpressionParser.parseCollectionExpression(
                    expression, matchingMetaInfoHolder, (IndexedEventHolder) storeEvents);
            CollectionExecutor collectionExecutor = CollectionExpressionParser.buildCollectionExecutor(
                    collectionExpression, matchingMetaInfoHolder, variableExpressionExecutors, tableMap,
                    siddhiAppContext, true, queryName);
            if (collectionExpression instanceof CompareCollectionExpression &&
                    ((CompareCollectionExpression) collectionExpression).getOperator() == Compare.Operator.EQUAL &&
                    (collectionExpression.getCollectionScope() == INDEXED_RESULT_SET ||
                            collectionExpression.getCollectionScope() == PRIMARY_KEY_RESULT_SET) &&
                    ((IndexedEventHolder) storeEvents).getPrimaryKeyReferenceHolders() != null &&
                    ((IndexedEventHolder) storeEvents).getPrimaryKeyReferenceHolders().length == 1 &&
                    ((IndexedEventHolder) storeEvents).getPrimaryKeyReferenceHolders()[0].getPrimaryKeyAttribute().
                            equals(((AttributeCollectionExpression)
                                    ((CompareCollectionExpression) collectionExpression)
                                            .getAttributeCollectionExpression()).getAttribute())) {
                return new OverwriteTableIndexOperator(collectionExecutor, queryName);
            } else if (collectionExpression instanceof AndMultiPrimaryKeyCollectionExpression &&
                    collectionExpression.getCollectionScope() == PRIMARY_KEY_RESULT_SET) {
                return new OverwriteTableIndexOperator(collectionExecutor, queryName);
            } else {
                return new IndexOperator(collectionExecutor, queryName);
            }
        } else if (storeEvents instanceof ComplexEventChunk) {
            ExpressionExecutor expressionExecutor = ExpressionParser.parseExpression(expression,
                    matchingMetaInfoHolder.getMetaStateEvent(), matchingMetaInfoHolder.getCurrentState(), tableMap,
                    variableExpressionExecutors, siddhiAppContext, false, 0, queryName);
            return new EventChunkOperator(expressionExecutor, matchingMetaInfoHolder.getStoreEventIndex());
        } else if (storeEvents instanceof Map) {
            ExpressionExecutor expressionExecutor = ExpressionParser.parseExpression(expression,
                    matchingMetaInfoHolder.getMetaStateEvent(), matchingMetaInfoHolder.getCurrentState(), tableMap,
                    variableExpressionExecutors, siddhiAppContext, false, 0, queryName);
            return new MapOperator(expressionExecutor, matchingMetaInfoHolder.getStoreEventIndex());
        } else if (storeEvents instanceof Collection) {
            ExpressionExecutor expressionExecutor = ExpressionParser.parseExpression(expression,
                    matchingMetaInfoHolder.getMetaStateEvent(), matchingMetaInfoHolder.getCurrentState(), tableMap,
                    variableExpressionExecutors, siddhiAppContext, false, 0, queryName);
            return new CollectionOperator(expressionExecutor, matchingMetaInfoHolder.getStoreEventIndex());
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
                }
            }
        }
        return false;
    }
}
