package org.wso2.siddhi.core.util.parser;

import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.stream.MetaStreamEvent;
import org.wso2.siddhi.core.exception.OperationNotSupportedException;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.executor.VariableExpressionExecutor;
import org.wso2.siddhi.core.table.EventTable;
import org.wso2.siddhi.core.table.holder.PrimaryKeyEventHolder;
import org.wso2.siddhi.core.util.collection.operator.*;
import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.expression.Variable;
import org.wso2.siddhi.query.api.expression.condition.Compare;
import org.wso2.siddhi.query.api.expression.constant.Constant;

import java.util.Arrays;
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
                                             Map<String, EventTable> eventTableMap, String queryName) {
        if (candidateEvents instanceof PrimaryKeyEventHolder) {
            if (expression instanceof Compare && ((Compare) expression).getOperator() == Compare.Operator.EQUAL) {
                Compare compare = (Compare) expression;
                if ((compare.getLeftExpression() instanceof Variable || compare.getLeftExpression() instanceof Constant)
                        && (compare.getRightExpression() instanceof Variable || compare.getRightExpression() instanceof Constant)) {

                    boolean leftSideIndexed = false;
                    boolean rightSideIndexed = false;

                    if (isTableIndexVariable(matchingMetaStateHolder, compare.getLeftExpression(), ((PrimaryKeyEventHolder) candidateEvents).getIndexAttribute())) {
                        leftSideIndexed = true;
                    }

                    if (isTableIndexVariable(matchingMetaStateHolder, compare.getRightExpression(), ((PrimaryKeyEventHolder) candidateEvents).getIndexAttribute())) {
                        rightSideIndexed = true;
                    }

                    if (leftSideIndexed && !rightSideIndexed) {
                        ExpressionExecutor expressionExecutor = ExpressionParser.parseExpression(compare.getRightExpression(),
                                matchingMetaStateHolder.getMetaStateEvent(), matchingMetaStateHolder.getDefaultStreamEventIndex(), eventTableMap, variableExpressionExecutors, executionPlanContext, false, 0, queryName);
                        return new PrimaryKeyOperator(expressionExecutor, matchingMetaStateHolder.getCandidateEventIndex(), ((PrimaryKeyEventHolder) candidateEvents).getIndexPosition());

                    } else if (!leftSideIndexed && rightSideIndexed) {
                        ExpressionExecutor expressionExecutor = ExpressionParser.parseExpression(compare.getLeftExpression(),
                                matchingMetaStateHolder.getMetaStateEvent(), matchingMetaStateHolder.getDefaultStreamEventIndex(), eventTableMap, variableExpressionExecutors, executionPlanContext, false, 0, queryName);
                        return new PrimaryKeyOperator(expressionExecutor, matchingMetaStateHolder.getCandidateEventIndex(), ((PrimaryKeyEventHolder) candidateEvents).getIndexPosition());

                    }
                }
            }
            //fallback to not using primary key
            ExpressionExecutor expressionExecutor = ExpressionParser.parseExpression(expression,
                    matchingMetaStateHolder.getMetaStateEvent(), matchingMetaStateHolder.getDefaultStreamEventIndex(), eventTableMap, variableExpressionExecutors, executionPlanContext, false, 0, queryName);
            return new MapOperator(expressionExecutor, matchingMetaStateHolder.getCandidateEventIndex());
        } else if (candidateEvents instanceof ComplexEventChunk) {
            ExpressionExecutor expressionExecutor = ExpressionParser.parseExpression(expression,
                    matchingMetaStateHolder.getMetaStateEvent(), matchingMetaStateHolder.getDefaultStreamEventIndex(), eventTableMap, variableExpressionExecutors, executionPlanContext, false, 0, queryName);
            return new EventChunkOperator(expressionExecutor, matchingMetaStateHolder.getCandidateEventIndex());
        } else if (candidateEvents instanceof Map) {
            ExpressionExecutor expressionExecutor = ExpressionParser.parseExpression(expression,
                    matchingMetaStateHolder.getMetaStateEvent(), matchingMetaStateHolder.getDefaultStreamEventIndex(), eventTableMap, variableExpressionExecutors, executionPlanContext, false, 0, queryName);
            return new MapOperator(expressionExecutor, matchingMetaStateHolder.getCandidateEventIndex());
        } else if (candidateEvents instanceof Collection) {
            ExpressionExecutor expressionExecutor = ExpressionParser.parseExpression(expression,
                    matchingMetaStateHolder.getMetaStateEvent(), matchingMetaStateHolder.getDefaultStreamEventIndex(), eventTableMap, variableExpressionExecutors, executionPlanContext, false, 0, queryName);
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
