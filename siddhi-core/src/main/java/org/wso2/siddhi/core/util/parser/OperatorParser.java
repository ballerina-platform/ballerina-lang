package org.wso2.siddhi.core.util.parser;

import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.stream.MetaStreamEvent;
import org.wso2.siddhi.core.exception.OperationNotSupportedException;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.executor.VariableExpressionExecutor;
import org.wso2.siddhi.core.table.EventTable;
import org.wso2.siddhi.core.table.holder.IndexedEventHolder;
import org.wso2.siddhi.core.util.collection.executor.CollectionExecutor;
import org.wso2.siddhi.core.util.collection.expression.CollectionExpression;
import org.wso2.siddhi.core.util.collection.operator.*;
import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.expression.Variable;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class OperatorParser {

    public static Operator constructOperator(Object candidateEvents, Expression expression,
                                             MatchingMetaStateHolder matchingMetaStateHolder,
                                             ExecutionPlanContext executionPlanContext,
                                             List<VariableExpressionExecutor> variableExpressionExecutors,
                                             Map<String, EventTable> eventTableMap) {
        if (candidateEvents instanceof IndexedEventHolder) {
            CollectionExpression collectionExpression = CollectionExpressionParser.parseCollectionExpression(expression,
                    matchingMetaStateHolder, (IndexedEventHolder) candidateEvents);
            CollectionExecutor collectionExecutor = CollectionExpressionParser.buildCollectionExecutor(collectionExpression,
                    matchingMetaStateHolder, variableExpressionExecutors, eventTableMap, executionPlanContext, true);
            return new IndexOperator(collectionExecutor);
        } else if (candidateEvents instanceof ComplexEventChunk) {
            ExpressionExecutor expressionExecutor = ExpressionParser.parseExpression(expression,
                    matchingMetaStateHolder.getMetaStateEvent(), matchingMetaStateHolder.getDefaultStreamEventIndex(), eventTableMap, variableExpressionExecutors, executionPlanContext, false, 0);
            return new EventChunkOperator(expressionExecutor, matchingMetaStateHolder.getCandidateEventIndex());
        } else if (candidateEvents instanceof Map) {
            ExpressionExecutor expressionExecutor = ExpressionParser.parseExpression(expression,
                    matchingMetaStateHolder.getMetaStateEvent(), matchingMetaStateHolder.getDefaultStreamEventIndex(), eventTableMap, variableExpressionExecutors, executionPlanContext, false, 0);
            return new MapOperator(expressionExecutor, matchingMetaStateHolder.getCandidateEventIndex());
        } else if (candidateEvents instanceof Collection) {
            ExpressionExecutor expressionExecutor = ExpressionParser.parseExpression(expression,
                    matchingMetaStateHolder.getMetaStateEvent(), matchingMetaStateHolder.getDefaultStreamEventIndex(), eventTableMap, variableExpressionExecutors, executionPlanContext, false, 0);
            return new CollectionOperator(expressionExecutor, matchingMetaStateHolder.getCandidateEventIndex());
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
                }
            }
        }
        return false;
    }
}
