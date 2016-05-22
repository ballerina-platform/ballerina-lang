package org.wso2.siddhi.core.util.parser;

import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.exception.OperationNotSupportedException;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.executor.VariableExpressionExecutor;
import org.wso2.siddhi.core.table.EventTable;
import org.wso2.siddhi.core.util.collection.operator.CollectionOperator;
import org.wso2.siddhi.core.util.collection.operator.EventChunkOperator;
import org.wso2.siddhi.core.util.collection.operator.MatchingMetaStateHolder;
import org.wso2.siddhi.core.util.collection.operator.Operator;
import org.wso2.siddhi.core.util.parser.ExpressionParser;
import org.wso2.siddhi.query.api.expression.Expression;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by suho on 5/22/16.
 */
public class OperatorParser {

    public static Operator constructOperator(Object candidateEvents, Expression expression,
                                             MatchingMetaStateHolder matchingMetaStateHolder,
                                             ExecutionPlanContext executionPlanContext,
                                             List<VariableExpressionExecutor> variableExpressionExecutors,
                                             Map<String, EventTable> eventTableMap) {
        ExpressionExecutor expressionExecutor = ExpressionParser.parseExpression(expression,
                matchingMetaStateHolder.getMetaStateEvent(), matchingMetaStateHolder.getDefaultStreamEventIndex(), eventTableMap, variableExpressionExecutors, executionPlanContext, false, 0);
        if (candidateEvents instanceof ComplexEventChunk) {
            return new EventChunkOperator(expressionExecutor, matchingMetaStateHolder.getCandidateEventIndex());
        } else if (candidateEvents instanceof Collection) {
            return new CollectionOperator(expressionExecutor, matchingMetaStateHolder.getCandidateEventIndex());
        } else {
            throw new OperationNotSupportedException(candidateEvents.getClass() + " is not supported by OperatorParser!");
        }
    }
}
