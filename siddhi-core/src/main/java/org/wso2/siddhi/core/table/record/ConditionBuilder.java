package org.wso2.siddhi.core.table.record;

import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.event.state.MetaStateEvent;
import org.wso2.siddhi.core.event.stream.MetaStreamEvent;
import org.wso2.siddhi.core.exception.ExecutionPlanCreationException;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.executor.VariableExpressionExecutor;
import org.wso2.siddhi.core.table.EventTable;
import org.wso2.siddhi.core.util.collection.operator.MatchingMetaInfoHolder;
import org.wso2.siddhi.core.util.parser.ExpressionParser;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.exception.AttributeNotExistException;
import org.wso2.siddhi.query.api.exception.ExecutionPlanValidationException;
import org.wso2.siddhi.query.api.expression.AttributeFunction;
import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.expression.Variable;
import org.wso2.siddhi.query.api.expression.condition.*;
import org.wso2.siddhi.query.api.expression.constant.Constant;
import org.wso2.siddhi.query.api.expression.math.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.siddhi.core.util.SiddhiConstants.UNKNOWN_STATE;

public class ConditionBuilder {
    private final Map<String, ExpressionExecutor> variableExpressionExecutorMap;
    private Expression expression;
    private final MatchingMetaInfoHolder matchingMetaInfoHolder;
    private final ExecutionPlanContext executionPlanContext;
    private final List<VariableExpressionExecutor> variableExpressionExecutors;
    private final Map<String, EventTable> eventTableMap;
    private final String queryName;

    ConditionBuilder(Expression expression, MatchingMetaInfoHolder matchingMetaInfoHolder, ExecutionPlanContext executionPlanContext, List<VariableExpressionExecutor> variableExpressionExecutors, Map<String, EventTable> eventTableMap, String queryName) {
        this.expression = expression;
        this.matchingMetaInfoHolder = matchingMetaInfoHolder;
        this.executionPlanContext = executionPlanContext;
        this.variableExpressionExecutors = variableExpressionExecutors;
        this.eventTableMap = eventTableMap;
        this.queryName = queryName;
        this.variableExpressionExecutorMap = new HashMap<String, ExpressionExecutor>();
    }

    Map<String, ExpressionExecutor> getVariableExpressionExecutorMap() {
        return variableExpressionExecutorMap;
    }

    public void build(ConditionVisitor conditionVisitor) {
        buildVariableExecutors(expression, conditionVisitor);
    }

    private void buildVariableExecutors(Expression expression, ConditionVisitor conditionVisitor) {
        if (expression instanceof And) {
            conditionVisitor.beginVisitAnd((And) expression);
            conditionVisitor.beginVisitAndLeftOperand(((And) expression).getLeftExpression());
            buildVariableExecutors(((And) expression).getLeftExpression(), conditionVisitor);
            conditionVisitor.endVisitAndLeftOperand(((And) expression).getLeftExpression());

            conditionVisitor.beginVisitAndRightOperand(((And) expression).getRightExpression());
            buildVariableExecutors(((And) expression).getRightExpression(), conditionVisitor);
            conditionVisitor.endVisitAndRightOperand(((And) expression).getRightExpression());
            conditionVisitor.endVisitAnd((And) expression);

        } else if (expression instanceof Or) {
            conditionVisitor.beginVisitOr((Or) expression);
            conditionVisitor.beginVisitOrLeftOperand(((Or) expression).getLeftExpression());
            buildVariableExecutors(((Or) expression).getLeftExpression(), conditionVisitor);
            conditionVisitor.endVisitOrLeftOperand(((Or) expression).getLeftExpression());

            conditionVisitor.beginVisitOrRightOperand(((Or) expression).getRightExpression());
            buildVariableExecutors(((Or) expression).getRightExpression(), conditionVisitor);
            conditionVisitor.endVisitOrRightOperand(((Or) expression).getRightExpression());
            conditionVisitor.endVisitOr((Or) expression);

        } else if (expression instanceof Not) {
            conditionVisitor.beginVisitNot(((Not) expression));
            buildVariableExecutors(((Not) expression).getExpression(), conditionVisitor);
            conditionVisitor.endVisitNot(((Not) expression));

        } else if (expression instanceof Compare) {
            conditionVisitor.beginVisitCompare((Compare) expression, ((Compare) expression).getOperator());
            conditionVisitor.beginVisitCompareLeftOperand(((Compare) expression).getLeftExpression(), ((Compare) expression).getOperator());
            buildVariableExecutors(((Compare) expression).getLeftExpression(), conditionVisitor);
            conditionVisitor.endVisitCompareLeftOperand(((Compare) expression).getLeftExpression(), ((Compare) expression).getOperator());

            conditionVisitor.beginVisitCompareRightOperand(((Compare) expression).getRightExpression(), ((Compare) expression).getOperator());
            buildVariableExecutors(((Compare) expression).getRightExpression(), conditionVisitor);
            conditionVisitor.endVisitCompareRightOperand(((Compare) expression).getRightExpression(), ((Compare) expression).getOperator());
            conditionVisitor.endVisitCompare((Compare) expression, ((Compare) expression).getOperator());

        } else if (expression instanceof Add) {
            conditionVisitor.beginVisitMath(expression, ConditionVisitor.MathOperator.ADD);
            conditionVisitor.beginVisitMathLeftOperand(((Add) expression).getLeftValue(), ConditionVisitor.MathOperator.ADD);
            buildVariableExecutors(((Add) expression).getLeftValue(), conditionVisitor);
            conditionVisitor.endVisitMathLeftOperand(((Add) expression).getRightValue(), ConditionVisitor.MathOperator.ADD);

            conditionVisitor.beginVisitMathRightOperand(((Add) expression).getRightValue(), ConditionVisitor.MathOperator.ADD);
            buildVariableExecutors(((Add) expression).getRightValue(), conditionVisitor);
            conditionVisitor.endVisitMathRightOperand(((Add) expression).getRightValue(), ConditionVisitor.MathOperator.ADD);
            conditionVisitor.endVisitMath(expression, ConditionVisitor.MathOperator.ADD);
        } else if (expression instanceof Subtract) {
            conditionVisitor.beginVisitMath(expression, ConditionVisitor.MathOperator.SUBTRACT);
            conditionVisitor.beginVisitMathLeftOperand(((Subtract) expression).getLeftValue(), ConditionVisitor.MathOperator.SUBTRACT);
            buildVariableExecutors(((Subtract) expression).getLeftValue(), conditionVisitor);
            conditionVisitor.endVisitMathLeftOperand(((Subtract) expression).getRightValue(), ConditionVisitor.MathOperator.SUBTRACT);

            conditionVisitor.beginVisitMathRightOperand(((Subtract) expression).getRightValue(), ConditionVisitor.MathOperator.SUBTRACT);
            buildVariableExecutors(((Subtract) expression).getRightValue(), conditionVisitor);
            conditionVisitor.endVisitMathRightOperand(((Subtract) expression).getRightValue(), ConditionVisitor.MathOperator.SUBTRACT);
            conditionVisitor.endVisitMath(expression, ConditionVisitor.MathOperator.SUBTRACT);

        } else if (expression instanceof Divide) {
            conditionVisitor.beginVisitMath(expression, ConditionVisitor.MathOperator.DIVIDE);
            conditionVisitor.beginVisitMathLeftOperand(((Divide) expression).getLeftValue(), ConditionVisitor.MathOperator.DIVIDE);
            buildVariableExecutors(((Divide) expression).getLeftValue(), conditionVisitor);
            conditionVisitor.endVisitMathLeftOperand(((Divide) expression).getRightValue(), ConditionVisitor.MathOperator.DIVIDE);

            conditionVisitor.beginVisitMathRightOperand(((Divide) expression).getRightValue(), ConditionVisitor.MathOperator.DIVIDE);
            buildVariableExecutors(((Divide) expression).getRightValue(), conditionVisitor);
            conditionVisitor.endVisitMathRightOperand(((Divide) expression).getRightValue(), ConditionVisitor.MathOperator.DIVIDE);
            conditionVisitor.endVisitMath(expression, ConditionVisitor.MathOperator.DIVIDE);
        } else if (expression instanceof Multiply) {
            conditionVisitor.beginVisitMath(expression, ConditionVisitor.MathOperator.MULTIPLY);
            conditionVisitor.beginVisitMathLeftOperand(((Multiply) expression).getLeftValue(), ConditionVisitor.MathOperator.MULTIPLY);
            buildVariableExecutors(((Multiply) expression).getLeftValue(), conditionVisitor);
            conditionVisitor.endVisitMathLeftOperand(((Multiply) expression).getRightValue(), ConditionVisitor.MathOperator.MULTIPLY);

            conditionVisitor.beginVisitMathRightOperand(((Multiply) expression).getRightValue(), ConditionVisitor.MathOperator.MULTIPLY);
            buildVariableExecutors(((Multiply) expression).getRightValue(), conditionVisitor);
            conditionVisitor.endVisitMathRightOperand(((Multiply) expression).getRightValue(), ConditionVisitor.MathOperator.MULTIPLY);
            conditionVisitor.endVisitMath(expression, ConditionVisitor.MathOperator.MULTIPLY);

        } else if (expression instanceof Mod) {
            conditionVisitor.beginVisitMath(expression, ConditionVisitor.MathOperator.MOD);
            conditionVisitor.beginVisitMathLeftOperand(((Mod) expression).getLeftValue(), ConditionVisitor.MathOperator.MOD);
            buildVariableExecutors(((Mod) expression).getLeftValue(), conditionVisitor);
            conditionVisitor.endVisitMathLeftOperand(((Mod) expression).getRightValue(), ConditionVisitor.MathOperator.MOD);

            conditionVisitor.beginVisitMathRightOperand(((Mod) expression).getRightValue(), ConditionVisitor.MathOperator.MOD);
            buildVariableExecutors(((Mod) expression).getRightValue(), conditionVisitor);
            conditionVisitor.endVisitMathRightOperand(((Mod) expression).getRightValue(), ConditionVisitor.MathOperator.MOD);
            conditionVisitor.endVisitMath(expression, ConditionVisitor.MathOperator.MOD);

        } else if (expression instanceof IsNull) {
            IsNull isNull = (IsNull) expression;
            if (isNull.getExpression() != null) {
                conditionVisitor.beginVisitIsNull(isNull, null);
                buildVariableExecutors(((IsNull) expression).getExpression(), conditionVisitor);
                conditionVisitor.endVisitIsNull(isNull, null);
            } else {
                String streamId = isNull.getStreamId();
                MetaStateEvent metaStateEvent = matchingMetaInfoHolder.getMetaStateEvent();
                if (streamId == null) {
                    throw new ExecutionPlanCreationException("IsNull does not support streamId being null");
                } else {
                    AbstractDefinition definitionOutput = null;
                    MetaStreamEvent[] metaStreamEvents = metaStateEvent.getMetaStreamEvents();
                    for (int i = 0, metaStreamEventsLength = metaStreamEvents.length; i < metaStreamEventsLength; i++) {
                        MetaStreamEvent metaStreamEvent = metaStreamEvents[i];
                        AbstractDefinition definition = metaStreamEvent.getLastInputDefinition();
                        if (metaStreamEvent.getInputReferenceId() == null) {
                            if (definition.getId().equals(streamId)) {
                                definitionOutput = definition;
                                break;
                            }
                        } else {
                            if (metaStreamEvent.getInputReferenceId().equals(streamId)) {
                                definitionOutput = definition;
                                break;
                            }
                        }
                    }
                    if (definitionOutput != null) {
                        conditionVisitor.beginVisitIsNull(isNull, definitionOutput.getId());
                        conditionVisitor.endVisitIsNull(isNull, definitionOutput.getId());
                    } else {
                        conditionVisitor.beginVisitIsNull(isNull, null);
                        conditionVisitor.endVisitIsNull(isNull, null);
                    }
                }
            }
        } else if (expression instanceof In) {
            conditionVisitor.beginVisitIn((In) expression, ((In) expression).getSourceId());
            buildVariableExecutors(((In) expression).getExpression(), conditionVisitor);
            conditionVisitor.endVisitIn((In) expression, ((In) expression).getSourceId());

        } else if (expression instanceof Constant) {
            conditionVisitor.beginVisitConstant((Constant) expression);
            conditionVisitor.endVisitConstant((Constant) expression);

        } else if (expression instanceof AttributeFunction) {
            conditionVisitor.beginVisitAttributeFunction((AttributeFunction) expression);
            Expression[] expressions = ((AttributeFunction) expression).getParameters();
            for (int i = 0; i < expressions.length; i++) {
                conditionVisitor.beginVisitParameterAttributeFunction(expressions[i], i);
                buildVariableExecutors(expressions[i], conditionVisitor);
                conditionVisitor.endVisitParameterAttributeFunction(expressions[i], i);
            }
            conditionVisitor.endVisitAttributeFunction((AttributeFunction) expression);

        } else if (expression instanceof Variable) {
            Variable variable = ((Variable) expression);
            String attributeName = variable.getAttributeName();
            AbstractDefinition definition;
            Attribute.Type type = null;
            int streamEventChainIndex = UNKNOWN_STATE;

            if (variable.getStreamId() == null) {
                MetaStreamEvent[] metaStreamEvents = matchingMetaInfoHolder.getMetaStateEvent().getMetaStreamEvents();

                if (matchingMetaInfoHolder.getCurrentState() == UNKNOWN_STATE) {
                    String firstInput = null;
                    for (int i = 0; i < metaStreamEvents.length; i++) {
                        MetaStreamEvent metaStreamEvent = metaStreamEvents[i];
                        definition = metaStreamEvent.getLastInputDefinition();
                        if (type == null) {
                            try {
                                type = definition.getAttributeType(attributeName);
                                firstInput = "Input Stream: " + definition.getId() + " with " +
                                        "reference: " + metaStreamEvent.getInputReferenceId();
                                streamEventChainIndex = i;
                            } catch (AttributeNotExistException e) {
                                //do nothing
                            }
                        } else {
                            try {
                                definition.getAttributeType(attributeName);
                                throw new ExecutionPlanValidationException(firstInput + " and Input Stream: " + definition.getId() + " with " +
                                        "reference: " + metaStreamEvent.getInputReferenceId() + " contains attribute with same" +
                                        " name '" + attributeName + "'");
                            } catch (AttributeNotExistException e) {
                                //do nothing as its expected
                            }
                        }
                    }
                    if (streamEventChainIndex != UNKNOWN_STATE) {
                        if (matchingMetaInfoHolder.getStreamEventIndex() == streamEventChainIndex) {
                            buildStreamVariableExecutor(variable, streamEventChainIndex, conditionVisitor, type);
                        } else {
                            buildStoreVariableExecutor(variable, conditionVisitor, type);
                        }
                    }
                } else {

                    MetaStreamEvent metaStreamEvent = matchingMetaInfoHolder.getMetaStateEvent().getMetaStreamEvent(matchingMetaInfoHolder.getCurrentState());
                    definition = metaStreamEvent.getLastInputDefinition();
                    try {
                        type = definition.getAttributeType(attributeName);
                    } catch (AttributeNotExistException e) {
                        throw new ExecutionPlanValidationException(e.getMessage() + " Input Stream: " +
                                definition.getId() + " with " + "reference: " + metaStreamEvent.getInputReferenceId());
                    }

                    if (matchingMetaInfoHolder.getCurrentState() == matchingMetaInfoHolder.getStreamEventIndex()) {
                        buildStreamVariableExecutor(variable, streamEventChainIndex, conditionVisitor, type);
                    } else {
                        buildStoreVariableExecutor(variable, conditionVisitor, type);
                    }
                }

            } else {

                MetaStreamEvent[] metaStreamEvents = matchingMetaInfoHolder.getMetaStateEvent().getMetaStreamEvents();
                for (int i = 0, metaStreamEventsLength = metaStreamEvents.length; i < metaStreamEventsLength; i++) {
                    MetaStreamEvent metaStreamEvent = metaStreamEvents[i];
                    definition = metaStreamEvent.getLastInputDefinition();
                    if (metaStreamEvent.getInputReferenceId() == null) {
                        if (definition.getId().equals(variable.getStreamId())) {
                            type = definition.getAttributeType(attributeName);
                            streamEventChainIndex = i;
                            break;
                        }
                    } else {
                        if (metaStreamEvent.getInputReferenceId().equals(variable.getStreamId())) {
                            type = definition.getAttributeType(attributeName);
                            streamEventChainIndex = i;
                            break;
                        }
                    }
                }
                if (matchingMetaInfoHolder.getStreamEventIndex() == streamEventChainIndex) {
                    buildStreamVariableExecutor(variable, streamEventChainIndex, conditionVisitor, type);
                } else {
                    buildStoreVariableExecutor(variable, conditionVisitor, type);
                }
            }
        }
    }

    private void buildStoreVariableExecutor(Variable variable, ConditionVisitor conditionVisitor, Attribute.Type type) {
        conditionVisitor.beginVisitStoreVariable(variable, type);
        conditionVisitor.endVisitStoreVariable(variable, type);

    }

    private void buildStreamVariableExecutor(Variable variable, int streamEventChainIndex, ConditionVisitor conditionVisitor, Attribute.Type type) {
        String id = variable.getAttributeName();
        if (variable.getStreamId() != null) {
            id = variable.getStreamId() + "." + id;
        }
        conditionVisitor.beginVisitStreamVariable(variable, id, type);
        if (!variableExpressionExecutorMap.containsKey(id)) {
            ExpressionExecutor variableExpressionExecutor = ExpressionParser.parseExpression(expression, matchingMetaInfoHolder.getMetaStateEvent(), streamEventChainIndex, eventTableMap, variableExpressionExecutors, executionPlanContext, false, 0, queryName);
            variableExpressionExecutorMap.put(id, variableExpressionExecutor);
        }
        conditionVisitor.endVisitStreamVariable(variable, id, type);

    }
}
