/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.siddhi.core.table.record;

import org.wso2.siddhi.core.config.SiddhiAppContext;
import org.wso2.siddhi.core.event.state.MetaStateEvent;
import org.wso2.siddhi.core.event.stream.MetaStreamEvent;
import org.wso2.siddhi.core.exception.OperationNotSupportedException;
import org.wso2.siddhi.core.exception.SiddhiAppCreationException;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.executor.VariableExpressionExecutor;
import org.wso2.siddhi.core.table.Table;
import org.wso2.siddhi.core.util.collection.operator.MatchingMetaInfoHolder;
import org.wso2.siddhi.core.util.parser.ExpressionParser;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.exception.AttributeNotExistException;
import org.wso2.siddhi.query.api.exception.SiddhiAppValidationException;
import org.wso2.siddhi.query.api.expression.AttributeFunction;
import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.expression.Variable;
import org.wso2.siddhi.query.api.expression.condition.And;
import org.wso2.siddhi.query.api.expression.condition.Compare;
import org.wso2.siddhi.query.api.expression.condition.In;
import org.wso2.siddhi.query.api.expression.condition.IsNull;
import org.wso2.siddhi.query.api.expression.condition.Not;
import org.wso2.siddhi.query.api.expression.condition.Or;
import org.wso2.siddhi.query.api.expression.constant.BoolConstant;
import org.wso2.siddhi.query.api.expression.constant.Constant;
import org.wso2.siddhi.query.api.expression.constant.DoubleConstant;
import org.wso2.siddhi.query.api.expression.constant.FloatConstant;
import org.wso2.siddhi.query.api.expression.constant.IntConstant;
import org.wso2.siddhi.query.api.expression.constant.LongConstant;
import org.wso2.siddhi.query.api.expression.constant.StringConstant;
import org.wso2.siddhi.query.api.expression.math.Add;
import org.wso2.siddhi.query.api.expression.math.Divide;
import org.wso2.siddhi.query.api.expression.math.Mod;
import org.wso2.siddhi.query.api.expression.math.Multiply;
import org.wso2.siddhi.query.api.expression.math.Subtract;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.siddhi.core.util.SiddhiConstants.UNKNOWN_STATE;

/**
 * Parse and build Siddhi Condition objects from @{link {@link Expression}s.
 */
public class ConditionBuilder {
    private final Map<String, ExpressionExecutor> variableExpressionExecutorMap;
    private final MatchingMetaInfoHolder matchingMetaInfoHolder;
    private final SiddhiAppContext siddhiAppContext;
    private final List<VariableExpressionExecutor> variableExpressionExecutors;
    private final Map<String, Table> tableMap;
    private final String queryName;
    private Expression expression;

    ConditionBuilder(Expression expression, MatchingMetaInfoHolder matchingMetaInfoHolder,
                     SiddhiAppContext siddhiAppContext,
                     List<VariableExpressionExecutor> variableExpressionExecutors, Map<String, Table> tableMap,
                     String queryName) {
        this.expression = expression;
        this.matchingMetaInfoHolder = matchingMetaInfoHolder;
        this.siddhiAppContext = siddhiAppContext;
        this.variableExpressionExecutors = variableExpressionExecutors;
        this.tableMap = tableMap;
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
            conditionVisitor.beginVisitAnd();
            conditionVisitor.beginVisitAndLeftOperand();
            buildVariableExecutors(((And) expression).getLeftExpression(), conditionVisitor);
            conditionVisitor.endVisitAndLeftOperand();

            conditionVisitor.beginVisitAndRightOperand();
            buildVariableExecutors(((And) expression).getRightExpression(), conditionVisitor);
            conditionVisitor.endVisitAndRightOperand();
            conditionVisitor.endVisitAnd();

        } else if (expression instanceof Or) {
            conditionVisitor.beginVisitOr();
            conditionVisitor.beginVisitOrLeftOperand();
            buildVariableExecutors(((Or) expression).getLeftExpression(), conditionVisitor);
            conditionVisitor.endVisitOrLeftOperand();

            conditionVisitor.beginVisitOrRightOperand();
            buildVariableExecutors(((Or) expression).getRightExpression(), conditionVisitor);
            conditionVisitor.endVisitOrRightOperand();
            conditionVisitor.endVisitOr();

        } else if (expression instanceof Not) {
            conditionVisitor.beginVisitNot();
            buildVariableExecutors(((Not) expression).getExpression(), conditionVisitor);
            conditionVisitor.endVisitNot();

        } else if (expression instanceof Compare) {
            conditionVisitor.beginVisitCompare(((Compare) expression).getOperator());
            conditionVisitor.beginVisitCompareLeftOperand(((Compare) expression).getOperator());
            buildVariableExecutors(((Compare) expression).getLeftExpression(), conditionVisitor);
            conditionVisitor.endVisitCompareLeftOperand(((Compare) expression).getOperator());

            conditionVisitor.beginVisitCompareRightOperand(((Compare) expression).getOperator());
            buildVariableExecutors(((Compare) expression).getRightExpression(), conditionVisitor);
            conditionVisitor.endVisitCompareRightOperand(((Compare) expression).getOperator());
            conditionVisitor.endVisitCompare(((Compare) expression).getOperator());

        } else if (expression instanceof Add) {
            conditionVisitor.beginVisitMath(ConditionVisitor.MathOperator.ADD);
            conditionVisitor.beginVisitMathLeftOperand(ConditionVisitor.MathOperator.ADD);
            buildVariableExecutors(((Add) expression).getLeftValue(), conditionVisitor);
            conditionVisitor.endVisitMathLeftOperand(ConditionVisitor.MathOperator.ADD);

            conditionVisitor.beginVisitMathRightOperand(ConditionVisitor.MathOperator.ADD);
            buildVariableExecutors(((Add) expression).getRightValue(), conditionVisitor);
            conditionVisitor.endVisitMathRightOperand(ConditionVisitor.MathOperator.ADD);
            conditionVisitor.endVisitMath(ConditionVisitor.MathOperator.ADD);
        } else if (expression instanceof Subtract) {
            conditionVisitor.beginVisitMath(ConditionVisitor.MathOperator.SUBTRACT);
            conditionVisitor.beginVisitMathLeftOperand(ConditionVisitor.MathOperator.SUBTRACT);
            buildVariableExecutors(((Subtract) expression).getLeftValue(), conditionVisitor);
            conditionVisitor.endVisitMathLeftOperand(ConditionVisitor.MathOperator.SUBTRACT);

            conditionVisitor.beginVisitMathRightOperand(ConditionVisitor.MathOperator.SUBTRACT);
            buildVariableExecutors(((Subtract) expression).getRightValue(), conditionVisitor);
            conditionVisitor.endVisitMathRightOperand(ConditionVisitor.MathOperator.SUBTRACT);
            conditionVisitor.endVisitMath(ConditionVisitor.MathOperator.SUBTRACT);

        } else if (expression instanceof Divide) {
            conditionVisitor.beginVisitMath(ConditionVisitor.MathOperator.DIVIDE);
            conditionVisitor.beginVisitMathLeftOperand(ConditionVisitor.MathOperator.DIVIDE);
            buildVariableExecutors(((Divide) expression).getLeftValue(), conditionVisitor);
            conditionVisitor.endVisitMathLeftOperand(ConditionVisitor.MathOperator.DIVIDE);

            conditionVisitor.beginVisitMathRightOperand(ConditionVisitor.MathOperator.DIVIDE);
            buildVariableExecutors(((Divide) expression).getRightValue(), conditionVisitor);
            conditionVisitor.endVisitMathRightOperand(ConditionVisitor.MathOperator.DIVIDE);
            conditionVisitor.endVisitMath(ConditionVisitor.MathOperator.DIVIDE);
        } else if (expression instanceof Multiply) {
            conditionVisitor.beginVisitMath(ConditionVisitor.MathOperator.MULTIPLY);
            conditionVisitor.beginVisitMathLeftOperand(ConditionVisitor.MathOperator.MULTIPLY);
            buildVariableExecutors(((Multiply) expression).getLeftValue(), conditionVisitor);
            conditionVisitor.endVisitMathLeftOperand(ConditionVisitor.MathOperator.MULTIPLY);

            conditionVisitor.beginVisitMathRightOperand(ConditionVisitor.MathOperator.MULTIPLY);
            buildVariableExecutors(((Multiply) expression).getRightValue(), conditionVisitor);
            conditionVisitor.endVisitMathRightOperand(ConditionVisitor.MathOperator.MULTIPLY);
            conditionVisitor.endVisitMath(ConditionVisitor.MathOperator.MULTIPLY);

        } else if (expression instanceof Mod) {
            conditionVisitor.beginVisitMath(ConditionVisitor.MathOperator.MOD);
            conditionVisitor.beginVisitMathLeftOperand(ConditionVisitor.MathOperator.MOD);
            buildVariableExecutors(((Mod) expression).getLeftValue(), conditionVisitor);
            conditionVisitor.endVisitMathLeftOperand(ConditionVisitor.MathOperator.MOD);

            conditionVisitor.beginVisitMathRightOperand(ConditionVisitor.MathOperator.MOD);
            buildVariableExecutors(((Mod) expression).getRightValue(), conditionVisitor);
            conditionVisitor.endVisitMathRightOperand(ConditionVisitor.MathOperator.MOD);
            conditionVisitor.endVisitMath(ConditionVisitor.MathOperator.MOD);

        } else if (expression instanceof IsNull) {
            IsNull isNull = (IsNull) expression;
            if (isNull.getExpression() != null) {
                conditionVisitor.beginVisitIsNull(null);
                buildVariableExecutors(((IsNull) expression).getExpression(), conditionVisitor);
                conditionVisitor.endVisitIsNull(null);
            } else {
                String streamId = isNull.getStreamId();
                MetaStateEvent metaStateEvent = matchingMetaInfoHolder.getMetaStateEvent();
                if (streamId == null) {
                    throw new SiddhiAppCreationException("IsNull does not support streamId being null");
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
                        conditionVisitor.beginVisitIsNull(definitionOutput.getId());
                        conditionVisitor.endVisitIsNull(definitionOutput.getId());
                    } else {
                        conditionVisitor.beginVisitIsNull(null);
                        conditionVisitor.endVisitIsNull(null);
                    }
                }
            }
        } else if (expression instanceof In) {
            conditionVisitor.beginVisitIn(((In) expression).getSourceId());
            buildVariableExecutors(((In) expression).getExpression(), conditionVisitor);
            conditionVisitor.endVisitIn(((In) expression).getSourceId());

        } else if (expression instanceof Constant) {
            if (expression instanceof DoubleConstant) {
                conditionVisitor.beginVisitConstant(((DoubleConstant) expression).getValue(), Attribute.Type.BOOL);
                conditionVisitor.endVisitConstant(((DoubleConstant) expression).getValue(), Attribute.Type.BOOL);
            } else if (expression instanceof StringConstant) {
                conditionVisitor.beginVisitConstant(((StringConstant) expression).getValue(), Attribute.Type.STRING);
                conditionVisitor.endVisitConstant(((StringConstant) expression).getValue(), Attribute.Type.STRING);
            } else if (expression instanceof IntConstant) {
                conditionVisitor.beginVisitConstant(((IntConstant) expression).getValue(), Attribute.Type.INT);
                conditionVisitor.endVisitConstant(((IntConstant) expression).getValue(), Attribute.Type.INT);
            } else if (expression instanceof BoolConstant) {
                conditionVisitor.beginVisitConstant(((BoolConstant) expression).getValue(), Attribute.Type.BOOL);
                conditionVisitor.endVisitConstant(((BoolConstant) expression).getValue(), Attribute.Type.BOOL);
            } else if (expression instanceof FloatConstant) {
                conditionVisitor.beginVisitConstant(((FloatConstant) expression).getValue(), Attribute.Type.FLOAT);
                conditionVisitor.endVisitConstant(((FloatConstant) expression).getValue(), Attribute.Type.FLOAT);
            } else if (expression instanceof LongConstant) {
                conditionVisitor.beginVisitConstant(((LongConstant) expression).getValue(), Attribute.Type.LONG);
                conditionVisitor.endVisitConstant(((LongConstant) expression).getValue(), Attribute.Type.LONG);
            } else {
                throw new OperationNotSupportedException("No constant exist with type " +
                        expression.getClass().getName());
            }
        } else if (expression instanceof AttributeFunction) {
            conditionVisitor.beginVisitAttributeFunction(
                    ((AttributeFunction) expression).getNamespace(),
                    ((AttributeFunction) expression).getName());
            Expression[] expressions = ((AttributeFunction) expression).getParameters();
            for (int i = 0; i < expressions.length; i++) {
                conditionVisitor.beginVisitParameterAttributeFunction(i);
                buildVariableExecutors(expressions[i], conditionVisitor);
                conditionVisitor.endVisitParameterAttributeFunction(i);
            }
            conditionVisitor.endVisitAttributeFunction(
                    ((AttributeFunction) expression).getNamespace(),
                    ((AttributeFunction) expression).getName());

        } else if (expression instanceof Variable) {
            Variable variable = ((Variable) expression);
            String attributeName = variable.getAttributeName();
            AbstractDefinition definition;
            Attribute.Type type = null;
            int streamEventChainIndex = matchingMetaInfoHolder.getCurrentState();

            if (variable.getStreamId() == null) {
                MetaStreamEvent[] metaStreamEvents = matchingMetaInfoHolder.getMetaStateEvent().getMetaStreamEvents();

                if (streamEventChainIndex == UNKNOWN_STATE) {
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
                                throw new SiddhiAppValidationException(firstInput + " and Input Stream: " +
                                        definition.getId() + " with " +
                                        "reference: " + metaStreamEvent
                                        .getInputReferenceId() + " contains attribute " +
                                        "with same" +
                                        " name '" + attributeName + "'");
                            } catch (AttributeNotExistException e) {
                                //do nothing as its expected
                            }
                        }
                    }
                    if (streamEventChainIndex != UNKNOWN_STATE) {
                        if (matchingMetaInfoHolder.getMatchingStreamEventIndex() == streamEventChainIndex) {
                            buildStreamVariableExecutor(variable, streamEventChainIndex, conditionVisitor, type);
                        } else {
                            buildStoreVariableExecutor(variable, conditionVisitor, type, matchingMetaInfoHolder
                                    .getStoreDefinition());
                        }
                    }
                } else {

                    MetaStreamEvent metaStreamEvent = matchingMetaInfoHolder.getMetaStateEvent().getMetaStreamEvent
                            (matchingMetaInfoHolder.getCurrentState());
                    definition = metaStreamEvent.getLastInputDefinition();
                    try {
                        type = definition.getAttributeType(attributeName);
                    } catch (AttributeNotExistException e) {
                        throw new SiddhiAppValidationException(e.getMessage() + " Input Stream: " +
                                definition.getId() + " with " + "reference: "
                                + metaStreamEvent.getInputReferenceId());
                    }

                    if (matchingMetaInfoHolder.getCurrentState() == matchingMetaInfoHolder
                            .getMatchingStreamEventIndex()) {
                        buildStreamVariableExecutor(variable, streamEventChainIndex, conditionVisitor, type);
                    } else {
                        buildStoreVariableExecutor(variable, conditionVisitor, type, matchingMetaInfoHolder
                                .getStoreDefinition());
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
                if (matchingMetaInfoHolder.getMatchingStreamEventIndex() == streamEventChainIndex) {
                    buildStreamVariableExecutor(variable, streamEventChainIndex, conditionVisitor, type);
                } else {
                    buildStoreVariableExecutor(variable, conditionVisitor, type, matchingMetaInfoHolder
                            .getStoreDefinition());
                }
            }
        }
    }

    private void buildStoreVariableExecutor(Variable variable, ConditionVisitor conditionVisitor, Attribute.Type type,
                                            AbstractDefinition storeDefinition) {
        conditionVisitor.beginVisitStoreVariable(storeDefinition.getId(), variable.getAttributeName(), type);
        conditionVisitor.endVisitStoreVariable(storeDefinition.getId(), variable.getAttributeName(), type);

    }

    private void buildStreamVariableExecutor(Variable variable, int streamEventChainIndex,
                                             ConditionVisitor conditionVisitor, Attribute.Type type) {
        String id = variable.getAttributeName();
        if (variable.getStreamId() != null) {
            id = variable.getStreamId() + "." + id;
        }
        conditionVisitor.beginVisitStreamVariable(id, variable.getStreamId(), variable.getAttributeName(), type);
        if (!variableExpressionExecutorMap.containsKey(id)) {
            ExpressionExecutor variableExpressionExecutor = ExpressionParser.parseExpression(
                    variable, matchingMetaInfoHolder.getMetaStateEvent(), streamEventChainIndex, tableMap,
                    variableExpressionExecutors, siddhiAppContext, false, 0, queryName);
            variableExpressionExecutorMap.put(id, variableExpressionExecutor);
        }
        conditionVisitor.endVisitStreamVariable(id, variable.getStreamId(), variable.getAttributeName(), type);

    }
}
