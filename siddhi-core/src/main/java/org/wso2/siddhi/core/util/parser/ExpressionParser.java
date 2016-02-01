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
package org.wso2.siddhi.core.util.parser;

import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.event.MetaComplexEvent;
import org.wso2.siddhi.core.event.state.MetaStateEvent;
import org.wso2.siddhi.core.event.stream.MetaStreamEvent;
import org.wso2.siddhi.core.exception.ExecutionPlanCreationException;
import org.wso2.siddhi.core.exception.OperationNotSupportedException;
import org.wso2.siddhi.core.executor.ConstantExpressionExecutor;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.executor.VariableExpressionExecutor;
import org.wso2.siddhi.core.executor.condition.*;
import org.wso2.siddhi.core.executor.condition.compare.equal.*;
import org.wso2.siddhi.core.executor.condition.compare.greater_than.*;
import org.wso2.siddhi.core.executor.condition.compare.greater_than_equal.*;
import org.wso2.siddhi.core.executor.condition.compare.less_than.*;
import org.wso2.siddhi.core.executor.condition.compare.less_than_equal.*;
import org.wso2.siddhi.core.executor.condition.compare.not_equal.*;
import org.wso2.siddhi.core.executor.function.FunctionExecutor;
import org.wso2.siddhi.core.executor.function.ScriptFunctionExecutor;
import org.wso2.siddhi.core.executor.math.Subtract.SubtractExpressionExecutorDouble;
import org.wso2.siddhi.core.executor.math.Subtract.SubtractExpressionExecutorFloat;
import org.wso2.siddhi.core.executor.math.Subtract.SubtractExpressionExecutorInt;
import org.wso2.siddhi.core.executor.math.Subtract.SubtractExpressionExecutorLong;
import org.wso2.siddhi.core.executor.math.add.AddExpressionExecutorDouble;
import org.wso2.siddhi.core.executor.math.add.AddExpressionExecutorFloat;
import org.wso2.siddhi.core.executor.math.add.AddExpressionExecutorInt;
import org.wso2.siddhi.core.executor.math.add.AddExpressionExecutorLong;
import org.wso2.siddhi.core.executor.math.divide.DivideExpressionExecutorDouble;
import org.wso2.siddhi.core.executor.math.divide.DivideExpressionExecutorFloat;
import org.wso2.siddhi.core.executor.math.divide.DivideExpressionExecutorInt;
import org.wso2.siddhi.core.executor.math.divide.DivideExpressionExecutorLong;
import org.wso2.siddhi.core.executor.math.mod.ModExpressionExecutorDouble;
import org.wso2.siddhi.core.executor.math.mod.ModExpressionExecutorFloat;
import org.wso2.siddhi.core.executor.math.mod.ModExpressionExecutorInt;
import org.wso2.siddhi.core.executor.math.mod.ModExpressionExecutorLong;
import org.wso2.siddhi.core.executor.math.multiply.MultiplyExpressionExecutorDouble;
import org.wso2.siddhi.core.executor.math.multiply.MultiplyExpressionExecutorFloat;
import org.wso2.siddhi.core.executor.math.multiply.MultiplyExpressionExecutorInt;
import org.wso2.siddhi.core.executor.math.multiply.MultiplyExpressionExecutorLong;
import org.wso2.siddhi.core.query.selector.attribute.aggregator.AttributeAggregator;
import org.wso2.siddhi.core.query.selector.attribute.processor.executor.AbstractAggregationAttributeExecutor;
import org.wso2.siddhi.core.query.selector.attribute.processor.executor.AggregationAttributeExecutor;
import org.wso2.siddhi.core.query.selector.attribute.processor.executor.GroupByAggregationAttributeExecutor;
import org.wso2.siddhi.core.table.EventTable;
import org.wso2.siddhi.core.util.SiddhiClassLoader;
import org.wso2.siddhi.core.util.SiddhiConstants;
import org.wso2.siddhi.core.util.collection.operator.Finder;
import org.wso2.siddhi.core.util.extension.holder.AttributeAggregatorExtensionHolder;
import org.wso2.siddhi.core.util.extension.holder.FunctionExecutorExtensionHolder;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.exception.AttributeNotExistException;
import org.wso2.siddhi.query.api.exception.DuplicateAttributeException;
import org.wso2.siddhi.query.api.exception.ExecutionPlanValidationException;
import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.expression.Variable;
import org.wso2.siddhi.query.api.expression.condition.*;
import org.wso2.siddhi.query.api.expression.constant.*;
import org.wso2.siddhi.query.api.expression.function.AttributeFunction;
import org.wso2.siddhi.query.api.expression.function.AttributeFunctionExtension;
import org.wso2.siddhi.query.api.expression.math.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.wso2.siddhi.core.util.SiddhiConstants.*;

/**
 * Class to parse Expressions and create Expression executors.
 */
public class ExpressionParser {

    /**
     * Parse the given expression and create the appropriate Executor by recursively traversing the expression
     *
     * @param expression              Expression to be parsed
     * @param metaEvent               Meta Event
     * @param currentState            Current state number
     * @param eventTableMap           Event Table Map
     * @param executorList            List to hold VariableExpressionExecutors to update after query parsing  @return
     * @param executionPlanContext    ExecutionPlanContext
     * @param groupBy                 is for groupBy expression
     * @param defaultStreamEventIndex Default StreamEvent Index
     * @return ExpressionExecutor
     */
    public static ExpressionExecutor parseExpression(Expression expression, MetaComplexEvent metaEvent, int currentState,
                                                     Map<String, EventTable> eventTableMap, List<VariableExpressionExecutor> executorList,
                                                     ExecutionPlanContext executionPlanContext, boolean groupBy, int defaultStreamEventIndex) {
        if (expression instanceof And) {
            return new AndConditionExpressionExecutor(
                    parseExpression(((And) expression).getLeftExpression(), metaEvent, currentState, eventTableMap, executorList, executionPlanContext,
                            groupBy, defaultStreamEventIndex),
                    parseExpression(((And) expression).getRightExpression(), metaEvent, currentState, eventTableMap, executorList, executionPlanContext, groupBy, defaultStreamEventIndex)
            );
        } else if (expression instanceof Or) {
            return new OrConditionExpressionExecutor(
                    parseExpression(((Or) expression).getLeftExpression(), metaEvent, currentState, eventTableMap, executorList, executionPlanContext, groupBy, defaultStreamEventIndex),
                    parseExpression(((Or) expression).getRightExpression(), metaEvent, currentState, eventTableMap, executorList, executionPlanContext, groupBy, defaultStreamEventIndex));
        } else if (expression instanceof Not) {
            return new NotConditionExpressionExecutor(parseExpression(((Not) expression).getExpression(), metaEvent, currentState, eventTableMap, executorList, executionPlanContext, groupBy, defaultStreamEventIndex));
        } else if (expression instanceof Compare) {
            if (((Compare) expression).getOperator() == Compare.Operator.EQUAL) {
                return parseEqualCompare(
                        parseExpression(((Compare) expression).getLeftExpression(), metaEvent, currentState, eventTableMap, executorList, executionPlanContext, groupBy, defaultStreamEventIndex),
                        parseExpression(((Compare) expression).getRightExpression(), metaEvent, currentState, eventTableMap, executorList, executionPlanContext, groupBy, defaultStreamEventIndex));
            } else if (((Compare) expression).getOperator() == Compare.Operator.NOT_EQUAL) {
                return parseNotEqualCompare(
                        parseExpression(((Compare) expression).getLeftExpression(), metaEvent, currentState, eventTableMap, executorList, executionPlanContext, groupBy, defaultStreamEventIndex),
                        parseExpression(((Compare) expression).getRightExpression(), metaEvent, currentState, eventTableMap, executorList, executionPlanContext, groupBy, defaultStreamEventIndex));
            } else if (((Compare) expression).getOperator() == Compare.Operator.GREATER_THAN) {
                return parseGreaterThanCompare(
                        parseExpression(((Compare) expression).getLeftExpression(), metaEvent, currentState, eventTableMap, executorList, executionPlanContext, groupBy, defaultStreamEventIndex),
                        parseExpression(((Compare) expression).getRightExpression(), metaEvent, currentState, eventTableMap, executorList, executionPlanContext, groupBy, defaultStreamEventIndex));
            } else if (((Compare) expression).getOperator() == Compare.Operator.GREATER_THAN_EQUAL) {
                return parseGreaterThanEqualCompare(
                        parseExpression(((Compare) expression).getLeftExpression(), metaEvent, currentState, eventTableMap, executorList, executionPlanContext, groupBy, defaultStreamEventIndex),
                        parseExpression(((Compare) expression).getRightExpression(), metaEvent, currentState, eventTableMap, executorList, executionPlanContext, groupBy, defaultStreamEventIndex));
            } else if (((Compare) expression).getOperator() == Compare.Operator.LESS_THAN) {
                return parseLessThanCompare(
                        parseExpression(((Compare) expression).getLeftExpression(), metaEvent, currentState, eventTableMap, executorList, executionPlanContext, groupBy, defaultStreamEventIndex),
                        parseExpression(((Compare) expression).getRightExpression(), metaEvent, currentState, eventTableMap, executorList, executionPlanContext, groupBy, defaultStreamEventIndex));
            } else if (((Compare) expression).getOperator() == Compare.Operator.LESS_THAN_EQUAL) {
                return parseLessThanEqualCompare(
                        parseExpression(((Compare) expression).getLeftExpression(), metaEvent, currentState, eventTableMap, executorList, executionPlanContext, groupBy, defaultStreamEventIndex),
                        parseExpression(((Compare) expression).getRightExpression(), metaEvent, currentState, eventTableMap, executorList, executionPlanContext, groupBy, defaultStreamEventIndex));
            }

        } else if (expression instanceof Constant) {
            if (expression instanceof BoolConstant) {
                return new ConstantExpressionExecutor(((BoolConstant) expression).getValue(), Attribute.Type.BOOL);
            } else if (expression instanceof StringConstant) {
                return new ConstantExpressionExecutor(((StringConstant) expression).getValue(), Attribute.Type.STRING);
            } else if (expression instanceof IntConstant) {
                return new ConstantExpressionExecutor(((IntConstant) expression).getValue(), Attribute.Type.INT);
            } else if (expression instanceof LongConstant) {
                return new ConstantExpressionExecutor(((LongConstant) expression).getValue(), Attribute.Type.LONG);
            } else if (expression instanceof FloatConstant) {
                return new ConstantExpressionExecutor(((FloatConstant) expression).getValue(), Attribute.Type.FLOAT);
            } else if (expression instanceof DoubleConstant) {
                return new ConstantExpressionExecutor(((DoubleConstant) expression).getValue(), Attribute.Type.DOUBLE);
            }

        } else if (expression instanceof Variable) {
            return parseVariable((Variable) expression, metaEvent, currentState, executorList, defaultStreamEventIndex);

        } else if (expression instanceof Multiply) {
            ExpressionExecutor left = parseExpression(((Multiply) expression).getLeftValue(), metaEvent, currentState, eventTableMap, executorList, executionPlanContext, groupBy, defaultStreamEventIndex);
            ExpressionExecutor right = parseExpression(((Multiply) expression).getRightValue(), metaEvent, currentState, eventTableMap, executorList, executionPlanContext, groupBy, defaultStreamEventIndex);
            Attribute.Type type = parseArithmeticOperationResultType(left, right);
            switch (type) {
                case INT:
                    return new MultiplyExpressionExecutorInt(left, right);
                case LONG:
                    return new MultiplyExpressionExecutorLong(left, right);
                case FLOAT:
                    return new MultiplyExpressionExecutorFloat(left, right);
                case DOUBLE:
                    return new MultiplyExpressionExecutorDouble(left, right);
                default: //Will not happen. Handled in parseArithmeticOperationResultType()
            }
        } else if (expression instanceof Add) {
            ExpressionExecutor left = parseExpression(((Add) expression).getLeftValue(), metaEvent, currentState, eventTableMap, executorList, executionPlanContext, groupBy, defaultStreamEventIndex);
            ExpressionExecutor right = parseExpression(((Add) expression).getRightValue(), metaEvent, currentState, eventTableMap, executorList, executionPlanContext, groupBy, defaultStreamEventIndex);
            Attribute.Type type = parseArithmeticOperationResultType(left, right);
            switch (type) {
                case INT:
                    return new AddExpressionExecutorInt(left, right);
                case LONG:
                    return new AddExpressionExecutorLong(left, right);
                case FLOAT:
                    return new AddExpressionExecutorFloat(left, right);
                case DOUBLE:
                    return new AddExpressionExecutorDouble(left, right);
                default: //Will not happen. Handled in parseArithmeticOperationResultType()
            }
        } else if (expression instanceof Subtract) {
            ExpressionExecutor left = parseExpression(((Subtract) expression).getLeftValue(), metaEvent, currentState, eventTableMap, executorList, executionPlanContext, groupBy, defaultStreamEventIndex);
            ExpressionExecutor right = parseExpression(((Subtract) expression).getRightValue(), metaEvent, currentState, eventTableMap, executorList, executionPlanContext, groupBy, defaultStreamEventIndex);
            Attribute.Type type = parseArithmeticOperationResultType(left, right);
            switch (type) {
                case INT:
                    return new SubtractExpressionExecutorInt(left, right);
                case LONG:
                    return new SubtractExpressionExecutorLong(left, right);
                case FLOAT:
                    return new SubtractExpressionExecutorFloat(left, right);
                case DOUBLE:
                    return new SubtractExpressionExecutorDouble(left, right);
                default: //Will not happen. Handled in parseArithmeticOperationResultType()
            }
        } else if (expression instanceof Mod) {
            ExpressionExecutor left = parseExpression(((Mod) expression).getLeftValue(), metaEvent, currentState, eventTableMap, executorList, executionPlanContext, groupBy, defaultStreamEventIndex);
            ExpressionExecutor right = parseExpression(((Mod) expression).getRightValue(), metaEvent, currentState, eventTableMap, executorList, executionPlanContext, groupBy, defaultStreamEventIndex);
            Attribute.Type type = parseArithmeticOperationResultType(left, right);
            switch (type) {
                case INT:
                    return new ModExpressionExecutorInt(left, right);
                case LONG:
                    return new ModExpressionExecutorLong(left, right);
                case FLOAT:
                    return new ModExpressionExecutorFloat(left, right);
                case DOUBLE:
                    return new ModExpressionExecutorDouble(left, right);
                default: //Will not happen. Handled in parseArithmeticOperationResultType()
            }
        } else if (expression instanceof Divide) {
            ExpressionExecutor left = parseExpression(((Divide) expression).getLeftValue(), metaEvent, currentState, eventTableMap, executorList, executionPlanContext, groupBy, defaultStreamEventIndex);
            ExpressionExecutor right = parseExpression(((Divide) expression).getRightValue(), metaEvent, currentState, eventTableMap, executorList, executionPlanContext, groupBy, defaultStreamEventIndex);
            Attribute.Type type = parseArithmeticOperationResultType(left, right);
            switch (type) {
                case INT:
                    return new DivideExpressionExecutorInt(left, right);
                case LONG:
                    return new DivideExpressionExecutorLong(left, right);
                case FLOAT:
                    return new DivideExpressionExecutorFloat(left, right);
                case DOUBLE:
                    return new DivideExpressionExecutorDouble(left, right);
                default: //Will not happen. Handled in parseArithmeticOperationResultType()
            }

        } else if (expression instanceof AttributeFunctionExtension) {
            //extensions
            Object executor;
            try {
                executor = SiddhiClassLoader.loadExtensionImplementation((AttributeFunctionExtension) expression, FunctionExecutorExtensionHolder.getInstance(executionPlanContext));
            } catch (ExecutionPlanCreationException ex) {
                try {
                    executor = SiddhiClassLoader.loadExtensionImplementation((AttributeFunctionExtension) expression, AttributeAggregatorExtensionHolder.getInstance(executionPlanContext));
                    SelectorParser.getContainsAggregatorThreadLocal().set("true");
                } catch (ExecutionPlanCreationException e) {
                    throw new ExecutionPlanCreationException("'" + ((AttributeFunctionExtension) expression).getFunction() + "' is neither a function extension nor an aggregated attribute extension");
                }
            }
            if (executor instanceof FunctionExecutor) {
                FunctionExecutor expressionExecutor = (FunctionExecutor) executor;
                Expression[] innerExpressions = ((AttributeFunctionExtension) expression).getParameters();
                ExpressionExecutor[] innerExpressionExecutors = parseInnerExpression(innerExpressions, metaEvent, currentState, eventTableMap, executorList,
                        executionPlanContext, groupBy, defaultStreamEventIndex);
                expressionExecutor.initExecutor(innerExpressionExecutors, executionPlanContext);
                if (expressionExecutor.getReturnType() == Attribute.Type.BOOL) {
                    return new BoolConditionExpressionExecutor(expressionExecutor);
                }
                return expressionExecutor;
            } else {
                AttributeAggregator attributeAggregator = (AttributeAggregator) executor;
                Expression[] innerExpressions = ((AttributeFunctionExtension) expression).getParameters();
                ExpressionExecutor[] innerExpressionExecutors = parseInnerExpression(innerExpressions, metaEvent, currentState, eventTableMap, executorList,
                        executionPlanContext, groupBy, defaultStreamEventIndex);
                attributeAggregator.initAggregator(innerExpressionExecutors, executionPlanContext);
                AbstractAggregationAttributeExecutor aggregationAttributeProcessor;
                if (groupBy) {
                    aggregationAttributeProcessor = new GroupByAggregationAttributeExecutor(attributeAggregator, innerExpressionExecutors, executionPlanContext);
                } else {
                    aggregationAttributeProcessor = new AggregationAttributeExecutor(attributeAggregator, innerExpressionExecutors, executionPlanContext);
                }
                SelectorParser.getContainsAggregatorThreadLocal().set("true");
                return aggregationAttributeProcessor;
            }
        } else if (expression instanceof AttributeFunction) {
            Object executor;
            try {
                executor = SiddhiClassLoader.loadSiddhiImplementation(((AttributeFunction) expression).getFunction(), AttributeAggregator.class);
            } catch (ExecutionPlanCreationException ex) {
                try {
                    if (executionPlanContext.isFunctionExist(((AttributeFunction) expression).getFunction())) {
                        executor = new ScriptFunctionExecutor(((AttributeFunction) expression).getFunction());
                    } else {
                        try {
                            executor = SiddhiClassLoader.loadSiddhiImplementation(((AttributeFunction) expression).getFunction(), FunctionExecutor.class);
                        } catch (ExecutionPlanCreationException e) {
                            throw new ExecutionPlanCreationException(e.getMessage(), e);
                        }
                    }
                } catch (ExecutionPlanCreationException e) {
                    throw new ExecutionPlanCreationException(((AttributeFunction) expression).getFunction() + " is neither a function nor an aggregated attribute");
                }
            }

            if (executor instanceof AttributeAggregator) {
                Expression[] innerExpressions = ((AttributeFunction) expression).getParameters();
                ExpressionExecutor[] innerExpressionExecutors = parseInnerExpression(innerExpressions, metaEvent, currentState, eventTableMap, executorList,
                        executionPlanContext, groupBy, defaultStreamEventIndex);
                ((AttributeAggregator) executor).initAggregator(innerExpressionExecutors, executionPlanContext);
                AbstractAggregationAttributeExecutor aggregationAttributeProcessor;
                if (groupBy) {
                    aggregationAttributeProcessor = new GroupByAggregationAttributeExecutor((AttributeAggregator) executor, innerExpressionExecutors, executionPlanContext);
                } else {
                    aggregationAttributeProcessor = new AggregationAttributeExecutor((AttributeAggregator) executor, innerExpressionExecutors, executionPlanContext);
                }
                SelectorParser.getContainsAggregatorThreadLocal().set("true");
                return aggregationAttributeProcessor;
            } else {
                FunctionExecutor functionExecutor = (FunctionExecutor) executor;
                Expression[] innerExpressions = ((AttributeFunction) expression).getParameters();
                ExpressionExecutor[] innerExpressionExecutors = parseInnerExpression(innerExpressions, metaEvent, currentState, eventTableMap, executorList,
                        executionPlanContext, groupBy, defaultStreamEventIndex);
                functionExecutor.initExecutor(innerExpressionExecutors, executionPlanContext);
                if (functionExecutor.getReturnType() == Attribute.Type.BOOL) {
                    return new BoolConditionExpressionExecutor(functionExecutor);
                }
                return functionExecutor;
            }
        } else if (expression instanceof In) {


            EventTable eventTable = eventTableMap.get(((In) expression).getSourceId());

            Finder finder = eventTable.constructFinder(((In) expression).getExpression(), metaEvent, executionPlanContext, executorList, eventTableMap, defaultStreamEventIndex, SiddhiConstants.ANY);


            return new InConditionExpressionExecutor(eventTable, finder);

        } else if (expression instanceof IsNull) {

            IsNull isNull = (IsNull) expression;

            if (isNull.getExpression() != null) {
                ExpressionExecutor innerExpressionExecutor = parseExpression(isNull.getExpression(), metaEvent, currentState, eventTableMap, executorList, executionPlanContext, groupBy, defaultStreamEventIndex);
                return new IsNullConditionExpressionExecutor(innerExpressionExecutor);
            } else {
                String streamId = isNull.getStreamId();
                Integer streamIndex = isNull.getStreamIndex();


                if (metaEvent instanceof MetaStateEvent) {
                    int[] eventPosition = new int[2];
                    if (streamIndex != null) {
                        if (streamIndex <= LAST) {
                            eventPosition[STREAM_EVENT_INDEX] = streamIndex + 1;
                        } else {
                            eventPosition[STREAM_EVENT_INDEX] = streamIndex;
                        }
                    } else {
                        eventPosition[STREAM_EVENT_INDEX] = defaultStreamEventIndex;
                    }
                    eventPosition[STREAM_EVENT_CHAIN_INDEX] = UNKNOWN_STATE;

                    MetaStateEvent metaStateEvent = (MetaStateEvent) metaEvent;
                    if (streamId == null) {
                        throw new ExecutionPlanCreationException("IsNull does not support streamId being null");
                    } else {
                        MetaStreamEvent[] metaStreamEvents = metaStateEvent.getMetaStreamEvents();
                        for (int i = 0, metaStreamEventsLength = metaStreamEvents.length; i < metaStreamEventsLength; i++) {
                            MetaStreamEvent metaStreamEvent = metaStreamEvents[i];
                            AbstractDefinition definition = metaStreamEvent.getLastInputDefinition();
                            if (metaStreamEvent.getInputReferenceId() == null) {
                                if (definition.getId().equals(streamId)) {
                                    eventPosition[STREAM_EVENT_CHAIN_INDEX] = i;
                                    break;
                                }
                            } else {
                                if (metaStreamEvent.getInputReferenceId().equals(streamId)) {
                                    eventPosition[STREAM_EVENT_CHAIN_INDEX] = i;
                                    if (currentState > -1 && metaStreamEvents[currentState].getInputReferenceId() != null && streamIndex != null && streamIndex <= LAST) {
                                        if (streamId.equals(metaStreamEvents[currentState].getInputReferenceId())) {
                                            eventPosition[STREAM_EVENT_INDEX] = streamIndex;
                                        }
                                    }
                                    break;
                                }
                            }
                        }
                    }
                    return new IsNullStreamConditionExpressionExecutor(eventPosition);
                } else {
                    return new IsNullStreamConditionExpressionExecutor(null);
                }

            }
        }
        throw new UnsupportedOperationException(expression.toString() + " not supported!");

    }

    /**
     * Create greater than Compare Condition Expression Executor which evaluates whether value of leftExpressionExecutor
     * is greater than value of rightExpressionExecutor.
     *
     * @param leftExpressionExecutor  left ExpressionExecutor
     * @param rightExpressionExecutor right ExpressionExecutor
     * @return Condition ExpressionExecutor
     */
    private static ConditionExpressionExecutor parseGreaterThanCompare(
            ExpressionExecutor leftExpressionExecutor, ExpressionExecutor rightExpressionExecutor) {
        switch (leftExpressionExecutor.getReturnType()) {
            case STRING:
                throw new OperationNotSupportedException("string cannot used in greater than comparisons");
            case INT:
                switch (rightExpressionExecutor.getReturnType()) {
                    case STRING:
                        throw new OperationNotSupportedException("int cannot be compared with string");
                    case INT:
                        return new GreaterThanCompareConditionExpressionExecutorIntInt(leftExpressionExecutor, rightExpressionExecutor);
                    case LONG:
                        return new GreaterThanCompareConditionExpressionExecutorIntLong(leftExpressionExecutor, rightExpressionExecutor);
                    case FLOAT:
                        return new GreaterThanCompareConditionExpressionExecutorIntFloat(leftExpressionExecutor, rightExpressionExecutor);
                    case DOUBLE:
                        return new GreaterThanCompareConditionExpressionExecutorIntDouble(leftExpressionExecutor, rightExpressionExecutor);
                    case BOOL:
                        throw new OperationNotSupportedException("int cannot be compared with bool");
                    default:
                        throw new OperationNotSupportedException("int cannot be compared with " + rightExpressionExecutor.getReturnType());
                }
            case LONG:
                switch (rightExpressionExecutor.getReturnType()) {
                    case STRING:
                        throw new OperationNotSupportedException("long cannot be compared with string");
                    case INT:
                        return new GreaterThanCompareConditionExpressionExecutorLongInt(leftExpressionExecutor, rightExpressionExecutor);
                    case LONG:
                        return new GreaterThanCompareConditionExpressionExecutorLongLong(leftExpressionExecutor, rightExpressionExecutor);
                    case FLOAT:
                        return new GreaterThanCompareConditionExpressionExecutorLongFloat(leftExpressionExecutor, rightExpressionExecutor);
                    case DOUBLE:
                        return new GreaterThanCompareConditionExpressionExecutorLongDouble(leftExpressionExecutor, rightExpressionExecutor);
                    case BOOL:
                        throw new OperationNotSupportedException("long cannot be compared with bool");
                    default:
                        throw new OperationNotSupportedException("long cannot be compared with " + rightExpressionExecutor.getReturnType());
                }
            case FLOAT:
                switch (rightExpressionExecutor.getReturnType()) {
                    case STRING:
                        throw new OperationNotSupportedException("float cannot be compared with string");
                    case INT:
                        return new GreaterThanCompareConditionExpressionExecutorFloatInt(leftExpressionExecutor, rightExpressionExecutor);
                    case LONG:
                        return new GreaterThanCompareConditionExpressionExecutorFloatLong(leftExpressionExecutor, rightExpressionExecutor);
                    case FLOAT:
                        return new GreaterThanCompareConditionExpressionExecutorFloatFloat(leftExpressionExecutor, rightExpressionExecutor);
                    case DOUBLE:
                        return new GreaterThanCompareConditionExpressionExecutorFloatDouble(leftExpressionExecutor, rightExpressionExecutor);
                    case BOOL:
                        throw new OperationNotSupportedException("float cannot be compared with bool");
                    default:
                        throw new OperationNotSupportedException("float cannot be compared with " + rightExpressionExecutor.getReturnType());
                }
            case DOUBLE:
                switch (rightExpressionExecutor.getReturnType()) {
                    case STRING:
                        throw new OperationNotSupportedException("double cannot be compared with string");
                    case INT:
                        return new GreaterThanCompareConditionExpressionExecutorDoubleInt(leftExpressionExecutor, rightExpressionExecutor);
                    case LONG:
                        return new GreaterThanCompareConditionExpressionExecutorDoubleLong(leftExpressionExecutor, rightExpressionExecutor);
                    case FLOAT:
                        return new GreaterThanCompareConditionExpressionExecutorDoubleFloat(leftExpressionExecutor, rightExpressionExecutor);
                    case DOUBLE:
                        return new GreaterThanCompareConditionExpressionExecutorDoubleDouble(leftExpressionExecutor, rightExpressionExecutor);
                    case BOOL:
                        throw new OperationNotSupportedException("double cannot be compared with bool");
                    default:
                        throw new OperationNotSupportedException("double cannot be compared with " + rightExpressionExecutor.getReturnType());
                }
            case BOOL:
                throw new OperationNotSupportedException("bool cannot used in greater than comparisons");
            default:
                throw new OperationNotSupportedException(leftExpressionExecutor.getReturnType() + " cannot be used in greater than comparisons");
        }
    }

    /**
     * Create greater than or equal Compare Condition Expression Executor which evaluates whether value of leftExpressionExecutor
     * is greater than or equal to value of rightExpressionExecutor.
     *
     * @param leftExpressionExecutor  left ExpressionExecutor
     * @param rightExpressionExecutor right ExpressionExecutor
     * @return Condition ExpressionExecutor
     */
    private static ConditionExpressionExecutor parseGreaterThanEqualCompare(
            ExpressionExecutor leftExpressionExecutor, ExpressionExecutor rightExpressionExecutor) {
        switch (leftExpressionExecutor.getReturnType()) {
            case STRING:
                throw new OperationNotSupportedException("string cannot used in greater than equal comparisons");
            case INT:
                switch (rightExpressionExecutor.getReturnType()) {
                    case STRING:
                        throw new OperationNotSupportedException("int cannot be compared with string");
                    case INT:
                        return new GreaterThanEqualCompareConditionExpressionExecutorIntInt(leftExpressionExecutor, rightExpressionExecutor);
                    case LONG:
                        return new GreaterThanEqualCompareConditionExpressionExecutorIntLong(leftExpressionExecutor, rightExpressionExecutor);
                    case FLOAT:
                        return new GreaterThanEqualCompareConditionExpressionExecutorIntFloat(leftExpressionExecutor, rightExpressionExecutor);
                    case DOUBLE:
                        return new GreaterThanEqualCompareConditionExpressionExecutorIntDouble(leftExpressionExecutor, rightExpressionExecutor);
                    case BOOL:
                        throw new OperationNotSupportedException("int cannot be compared with bool");
                    default:
                        throw new OperationNotSupportedException("int cannot be compared with " + rightExpressionExecutor.getReturnType());
                }
            case LONG:
                switch (rightExpressionExecutor.getReturnType()) {
                    case STRING:
                        throw new OperationNotSupportedException("long cannot be compared with string");
                    case INT:
                        return new GreaterThanEqualCompareConditionExpressionExecutorLongInt(leftExpressionExecutor, rightExpressionExecutor);
                    case LONG:
                        return new GreaterThanEqualCompareConditionExpressionExecutorLongLong(leftExpressionExecutor, rightExpressionExecutor);
                    case FLOAT:
                        return new GreaterThanEqualCompareConditionExpressionExecutorLongFloat(leftExpressionExecutor, rightExpressionExecutor);
                    case DOUBLE:
                        return new GreaterThanEqualCompareConditionExpressionExecutorLongDouble(leftExpressionExecutor, rightExpressionExecutor);
                    case BOOL:
                        throw new OperationNotSupportedException("long cannot be compared with bool");
                    default:
                        throw new OperationNotSupportedException("long cannot be compared with " + rightExpressionExecutor.getReturnType());
                }
            case FLOAT:
                switch (rightExpressionExecutor.getReturnType()) {
                    case STRING:
                        throw new OperationNotSupportedException("float cannot be compared with string");
                    case INT:
                        return new GreaterThanEqualCompareConditionExpressionExecutorFloatInt(leftExpressionExecutor, rightExpressionExecutor);
                    case LONG:
                        return new GreaterThanEqualCompareConditionExpressionExecutorFloatLong(leftExpressionExecutor, rightExpressionExecutor);
                    case FLOAT:
                        return new GreaterThanEqualCompareConditionExpressionExecutorFloatFloat(leftExpressionExecutor, rightExpressionExecutor);
                    case DOUBLE:
                        return new GreaterThanEqualCompareConditionExpressionExecutorFloatDouble(leftExpressionExecutor, rightExpressionExecutor);
                    case BOOL:
                        throw new OperationNotSupportedException("float cannot be compared with bool");
                    default:
                        throw new OperationNotSupportedException("float cannot be compared with " + rightExpressionExecutor.getReturnType());
                }
            case DOUBLE:
                switch (rightExpressionExecutor.getReturnType()) {
                    case STRING:
                        throw new OperationNotSupportedException("double cannot be compared with string");
                    case INT:
                        return new GreaterThanEqualCompareConditionExpressionExecutorDoubleInt(leftExpressionExecutor, rightExpressionExecutor);
                    case LONG:
                        return new GreaterThanEqualCompareConditionExpressionExecutorDoubleLong(leftExpressionExecutor, rightExpressionExecutor);
                    case FLOAT:
                        return new GreaterThanEqualCompareConditionExpressionExecutorDoubleFloat(leftExpressionExecutor, rightExpressionExecutor);
                    case DOUBLE:
                        return new GreaterThanEqualCompareConditionExpressionExecutorDoubleDouble(leftExpressionExecutor, rightExpressionExecutor);
                    case BOOL:
                        throw new OperationNotSupportedException("double cannot be compared with bool");
                    default:
                        throw new OperationNotSupportedException("double cannot be compared with " + rightExpressionExecutor.getReturnType());
                }
            case BOOL:
                throw new OperationNotSupportedException("bool cannot used in greater than equal comparisons");
            default:
                throw new OperationNotSupportedException(leftExpressionExecutor.getReturnType() + " cannot be used in greater than comparisons");
        }
    }

    /**
     * Create less than Compare Condition Expression Executor which evaluates whether value of leftExpressionExecutor
     * is less than value of rightExpressionExecutor.
     *
     * @param leftExpressionExecutor  left ExpressionExecutor
     * @param rightExpressionExecutor right ExpressionExecutor
     * @return Condition ExpressionExecutor
     */
    private static ConditionExpressionExecutor parseLessThanCompare(
            ExpressionExecutor leftExpressionExecutor, ExpressionExecutor rightExpressionExecutor) {
        switch (leftExpressionExecutor.getReturnType()) {
            case STRING:
                throw new OperationNotSupportedException("string cannot used in less than comparisons");
            case INT:
                switch (rightExpressionExecutor.getReturnType()) {
                    case STRING:
                        throw new OperationNotSupportedException("int cannot be compared with string");
                    case INT:
                        return new LessThanCompareConditionExpressionExecutorIntInt(leftExpressionExecutor, rightExpressionExecutor);
                    case LONG:
                        return new LessThanCompareConditionExpressionExecutorIntLong(leftExpressionExecutor, rightExpressionExecutor);
                    case FLOAT:
                        return new LessThanCompareConditionExpressionExecutorIntFloat(leftExpressionExecutor, rightExpressionExecutor);
                    case DOUBLE:
                        return new LessThanCompareConditionExpressionExecutorIntDouble(leftExpressionExecutor, rightExpressionExecutor);
                    case BOOL:
                        throw new OperationNotSupportedException("int cannot be compared with bool");
                    default:
                        throw new OperationNotSupportedException("int cannot be compared with " + rightExpressionExecutor.getReturnType());
                }
            case LONG:
                switch (rightExpressionExecutor.getReturnType()) {
                    case STRING:
                        throw new OperationNotSupportedException("long cannot be compared with string");
                    case INT:
                        return new LessThanCompareConditionExpressionExecutorLongInt(leftExpressionExecutor, rightExpressionExecutor);
                    case LONG:
                        return new LessThanCompareConditionExpressionExecutorLongLong(leftExpressionExecutor, rightExpressionExecutor);
                    case FLOAT:
                        return new LessThanCompareConditionExpressionExecutorLongFloat(leftExpressionExecutor, rightExpressionExecutor);
                    case DOUBLE:
                        return new LessThanCompareConditionExpressionExecutorLongDouble(leftExpressionExecutor, rightExpressionExecutor);
                    case BOOL:
                        throw new OperationNotSupportedException("long cannot be compared with bool");
                    default:
                        throw new OperationNotSupportedException("long cannot be compared with " + rightExpressionExecutor.getReturnType());
                }
            case FLOAT:
                switch (rightExpressionExecutor.getReturnType()) {
                    case STRING:
                        throw new OperationNotSupportedException("float cannot be compared with string");
                    case INT:
                        return new LessThanCompareConditionExpressionExecutorFloatInt(leftExpressionExecutor, rightExpressionExecutor);
                    case LONG:
                        return new LessThanCompareConditionExpressionExecutorFloatLong(leftExpressionExecutor, rightExpressionExecutor);
                    case FLOAT:
                        return new LessThanCompareConditionExpressionExecutorFloatFloat(leftExpressionExecutor, rightExpressionExecutor);
                    case DOUBLE:
                        return new LessThanCompareConditionExpressionExecutorFloatDouble(leftExpressionExecutor, rightExpressionExecutor);
                    case BOOL:
                        throw new OperationNotSupportedException("float cannot be compared with bool");
                    default:
                        throw new OperationNotSupportedException("float cannot be compared with " + rightExpressionExecutor.getReturnType());
                }
            case DOUBLE:
                switch (rightExpressionExecutor.getReturnType()) {
                    case STRING:
                        throw new OperationNotSupportedException("double cannot be compared with string");
                    case INT:
                        return new LessThanCompareConditionExpressionExecutorDoubleInt(leftExpressionExecutor, rightExpressionExecutor);
                    case LONG:
                        return new LessThanCompareConditionExpressionExecutorDoubleLong(leftExpressionExecutor, rightExpressionExecutor);
                    case FLOAT:
                        return new LessThanCompareConditionExpressionExecutorDoubleFloat(leftExpressionExecutor, rightExpressionExecutor);
                    case DOUBLE:
                        return new LessThanCompareConditionExpressionExecutorDoubleDouble(leftExpressionExecutor, rightExpressionExecutor);
                    case BOOL:
                        throw new OperationNotSupportedException("double cannot be compared with bool");
                    default:
                        throw new OperationNotSupportedException("double cannot be compared with " + rightExpressionExecutor.getReturnType());
                }
            case BOOL:
                throw new OperationNotSupportedException("bool cannot used in less than comparisons");
            default:
                throw new OperationNotSupportedException(leftExpressionExecutor.getReturnType() + " cannot be used in less than comparisons");
        }
    }

    /**
     * Create less than or equal Compare Condition Expression Executor which evaluates whether value of leftExpressionExecutor
     * is less than or equal to value of rightExpressionExecutor.
     *
     * @param leftExpressionExecutor  left ExpressionExecutor
     * @param rightExpressionExecutor right ExpressionExecutor
     * @return Condition ExpressionExecutor
     */
    private static ConditionExpressionExecutor parseLessThanEqualCompare(
            ExpressionExecutor leftExpressionExecutor, ExpressionExecutor rightExpressionExecutor) {
        switch (leftExpressionExecutor.getReturnType()) {
            case STRING:
                throw new OperationNotSupportedException("string cannot used in less than equal comparisons");
            case INT:
                switch (rightExpressionExecutor.getReturnType()) {
                    case STRING:
                        throw new OperationNotSupportedException("int cannot be compared with string");
                    case INT:
                        return new LessThanEqualCompareConditionExpressionExecutorIntInt(leftExpressionExecutor, rightExpressionExecutor);
                    case LONG:
                        return new LessThanEqualCompareConditionExpressionExecutorIntLong(leftExpressionExecutor, rightExpressionExecutor);
                    case FLOAT:
                        return new LessThanEqualCompareConditionExpressionExecutorIntFloat(leftExpressionExecutor, rightExpressionExecutor);
                    case DOUBLE:
                        return new LessThanEqualCompareConditionExpressionExecutorIntDouble(leftExpressionExecutor, rightExpressionExecutor);
                    case BOOL:
                        throw new OperationNotSupportedException("int cannot be compared with bool");
                    default:
                        throw new OperationNotSupportedException("int cannot be compared with " + rightExpressionExecutor.getReturnType());
                }
            case LONG:
                switch (rightExpressionExecutor.getReturnType()) {
                    case STRING:
                        throw new OperationNotSupportedException("long cannot be compared with string");
                    case INT:
                        return new LessThanEqualCompareConditionExpressionExecutorLongInt(leftExpressionExecutor, rightExpressionExecutor);
                    case LONG:
                        return new LessThanEqualCompareConditionExpressionExecutorLongLong(leftExpressionExecutor, rightExpressionExecutor);
                    case FLOAT:
                        return new LessThanEqualCompareConditionExpressionExecutorLongFloat(leftExpressionExecutor, rightExpressionExecutor);
                    case DOUBLE:
                        return new LessThanEqualCompareConditionExpressionExecutorLongDouble(leftExpressionExecutor, rightExpressionExecutor);
                    case BOOL:
                        throw new OperationNotSupportedException("long cannot be compared with bool");
                    default:
                        throw new OperationNotSupportedException("long cannot be compared with " + rightExpressionExecutor.getReturnType());
                }
            case FLOAT:
                switch (rightExpressionExecutor.getReturnType()) {
                    case STRING:
                        throw new OperationNotSupportedException("float cannot be compared with string");
                    case INT:
                        return new LessThanEqualCompareConditionExpressionExecutorFloatInt(leftExpressionExecutor, rightExpressionExecutor);
                    case LONG:
                        return new LessThanEqualCompareConditionExpressionExecutorFloatLong(leftExpressionExecutor, rightExpressionExecutor);
                    case FLOAT:
                        return new LessThanEqualCompareConditionExpressionExecutorFloatFloat(leftExpressionExecutor, rightExpressionExecutor);
                    case DOUBLE:
                        return new LessThanEqualCompareConditionExpressionExecutorFloatDouble(leftExpressionExecutor, rightExpressionExecutor);
                    case BOOL:
                        throw new OperationNotSupportedException("float cannot be compared with bool");
                    default:
                        throw new OperationNotSupportedException("float cannot be compared with " + rightExpressionExecutor.getReturnType());
                }
            case DOUBLE:
                switch (rightExpressionExecutor.getReturnType()) {
                    case STRING:
                        throw new OperationNotSupportedException("double cannot be compared with string");
                    case INT:
                        return new LessThanEqualCompareConditionExpressionExecutorDoubleInt(leftExpressionExecutor, rightExpressionExecutor);
                    case LONG:
                        return new LessThanEqualCompareConditionExpressionExecutorDoubleLong(leftExpressionExecutor, rightExpressionExecutor);
                    case FLOAT:
                        return new LessThanEqualCompareConditionExpressionExecutorDoubleFloat(leftExpressionExecutor, rightExpressionExecutor);
                    case DOUBLE:
                        return new LessThanEqualCompareConditionExpressionExecutorDoubleDouble(leftExpressionExecutor, rightExpressionExecutor);
                    case BOOL:
                        throw new OperationNotSupportedException("double cannot be compared with bool");
                    default:
                        throw new OperationNotSupportedException("double cannot be compared with " + rightExpressionExecutor.getReturnType());
                }
            case BOOL:
                throw new OperationNotSupportedException("bool cannot used in less than equal comparisons");
            default:
                throw new OperationNotSupportedException(leftExpressionExecutor.getReturnType() + " cannot be used in less than comparisons");
        }
    }

    /**
     * Create equal Compare Condition Expression Executor which evaluates whether value of leftExpressionExecutor
     * is equal to value of rightExpressionExecutor.
     *
     * @param leftExpressionExecutor  left ExpressionExecutor
     * @param rightExpressionExecutor right ExpressionExecutor
     * @return Condition ExpressionExecutor
     */
    private static ConditionExpressionExecutor parseEqualCompare(
            ExpressionExecutor leftExpressionExecutor, ExpressionExecutor rightExpressionExecutor) {
        switch (leftExpressionExecutor.getReturnType()) {
            case STRING:
                switch (rightExpressionExecutor.getReturnType()) {
                    case STRING:
                        return new EqualCompareConditionExpressionExecutorStringString(leftExpressionExecutor, rightExpressionExecutor);
                    default:
                        throw new OperationNotSupportedException("sting cannot be compared with " + rightExpressionExecutor.getReturnType());
                }
            case INT:
                switch (rightExpressionExecutor.getReturnType()) {
                    case STRING:
                        throw new OperationNotSupportedException("int cannot be compared with string");
                    case INT:
                        return new EqualCompareConditionExpressionExecutorIntInt(leftExpressionExecutor, rightExpressionExecutor);
                    case LONG:
                        return new EqualCompareConditionExpressionExecutorIntLong(leftExpressionExecutor, rightExpressionExecutor);
                    case FLOAT:
                        return new EqualCompareConditionExpressionExecutorIntFloat(leftExpressionExecutor, rightExpressionExecutor);
                    case DOUBLE:
                        return new EqualCompareConditionExpressionExecutorIntDouble(leftExpressionExecutor, rightExpressionExecutor);
                    case BOOL:
                        throw new OperationNotSupportedException("int cannot be compared with bool");
                    default:
                        throw new OperationNotSupportedException("int cannot be compared with " + rightExpressionExecutor.getReturnType());
                }
            case LONG:
                switch (rightExpressionExecutor.getReturnType()) {
                    case STRING:
                        throw new OperationNotSupportedException("long cannot be compared with string");
                    case INT:
                        return new EqualCompareConditionExpressionExecutorLongInt(leftExpressionExecutor, rightExpressionExecutor);
                    case LONG:
                        return new EqualCompareConditionExpressionExecutorLongLong(leftExpressionExecutor, rightExpressionExecutor);
                    case FLOAT:
                        return new EqualCompareConditionExpressionExecutorLongFloat(leftExpressionExecutor, rightExpressionExecutor);
                    case DOUBLE:
                        return new EqualCompareConditionExpressionExecutorLongDouble(leftExpressionExecutor, rightExpressionExecutor);
                    case BOOL:
                        throw new OperationNotSupportedException("long cannot be compared with bool");
                    default:
                        throw new OperationNotSupportedException("long cannot be compared with " + rightExpressionExecutor.getReturnType());
                }
            case FLOAT:
                switch (rightExpressionExecutor.getReturnType()) {
                    case STRING:
                        throw new OperationNotSupportedException("float cannot be compared with string");
                    case INT:
                        return new EqualCompareConditionExpressionExecutorFloatInt(leftExpressionExecutor, rightExpressionExecutor);
                    case LONG:
                        return new EqualCompareConditionExpressionExecutorFloatLong(leftExpressionExecutor, rightExpressionExecutor);
                    case FLOAT:
                        return new EqualCompareConditionExpressionExecutorFloatFloat(leftExpressionExecutor, rightExpressionExecutor);
                    case DOUBLE:
                        return new EqualCompareConditionExpressionExecutorFloatDouble(leftExpressionExecutor, rightExpressionExecutor);
                    case BOOL:
                        throw new OperationNotSupportedException("float cannot be compared with bool");
                    default:
                        throw new OperationNotSupportedException("float cannot be compared with " + rightExpressionExecutor.getReturnType());
                }
            case DOUBLE:
                switch (rightExpressionExecutor.getReturnType()) {
                    case STRING:
                        throw new OperationNotSupportedException("double cannot be compared with string");
                    case INT:
                        return new EqualCompareConditionExpressionExecutorDoubleInt(leftExpressionExecutor, rightExpressionExecutor);
                    case LONG:
                        return new EqualCompareConditionExpressionExecutorDoubleLong(leftExpressionExecutor, rightExpressionExecutor);
                    case FLOAT:
                        return new EqualCompareConditionExpressionExecutorDoubleFloat(leftExpressionExecutor, rightExpressionExecutor);
                    case DOUBLE:
                        return new EqualCompareConditionExpressionExecutorDoubleDouble(leftExpressionExecutor, rightExpressionExecutor);
                    case BOOL:
                        throw new OperationNotSupportedException("double cannot be compared with bool");
                    default:
                        throw new OperationNotSupportedException("double cannot be compared with " + rightExpressionExecutor.getReturnType());
                }
            case BOOL:
                switch (rightExpressionExecutor.getReturnType()) {
                    case BOOL:
                        return new EqualCompareConditionExpressionExecutorBoolBool(leftExpressionExecutor, rightExpressionExecutor);
                    default:
                        throw new OperationNotSupportedException("bool cannot be compared with " + rightExpressionExecutor.getReturnType());
                }
            default:
                throw new OperationNotSupportedException(leftExpressionExecutor.getReturnType() + " cannot be used in equal comparisons");
        }
    }

    /**
     * Create not equal Compare Condition Expression Executor which evaluates whether value of leftExpressionExecutor
     * is not equal to value of rightExpressionExecutor.
     *
     * @param leftExpressionExecutor  left ExpressionExecutor
     * @param rightExpressionExecutor right ExpressionExecutor
     * @return Condition ExpressionExecutor
     */
    private static ConditionExpressionExecutor parseNotEqualCompare(
            ExpressionExecutor leftExpressionExecutor, ExpressionExecutor rightExpressionExecutor) {
        switch (leftExpressionExecutor.getReturnType()) {
            case STRING:
                switch (rightExpressionExecutor.getReturnType()) {
                    case STRING:
                        return new NotEqualCompareConditionExpressionExecutorStringString(leftExpressionExecutor, rightExpressionExecutor);
                    default:
                        throw new OperationNotSupportedException("sting cannot be compared with " + rightExpressionExecutor.getReturnType());
                }
            case INT:
                switch (rightExpressionExecutor.getReturnType()) {
                    case STRING:
                        throw new OperationNotSupportedException("int cannot be compared with string");
                    case INT:
                        return new NotEqualCompareConditionExpressionExecutorIntInt(leftExpressionExecutor, rightExpressionExecutor);
                    case LONG:
                        return new NotEqualCompareConditionExpressionExecutorIntLong(leftExpressionExecutor, rightExpressionExecutor);
                    case FLOAT:
                        return new NotEqualCompareConditionExpressionExecutorIntFloat(leftExpressionExecutor, rightExpressionExecutor);
                    case DOUBLE:
                        return new NotEqualCompareConditionExpressionExecutorIntDouble(leftExpressionExecutor, rightExpressionExecutor);
                    case BOOL:
                        throw new OperationNotSupportedException("int cannot be compared with bool");
                    default:
                        throw new OperationNotSupportedException("int cannot be compared with " + rightExpressionExecutor.getReturnType());
                }
            case LONG:
                switch (rightExpressionExecutor.getReturnType()) {
                    case STRING:
                        throw new OperationNotSupportedException("long cannot be compared with string");
                    case INT:
                        return new NotEqualCompareConditionExpressionExecutorLongInt(leftExpressionExecutor, rightExpressionExecutor);
                    case LONG:
                        return new NotEqualCompareConditionExpressionExecutorLongLong(leftExpressionExecutor, rightExpressionExecutor);
                    case FLOAT:
                        return new NotEqualCompareConditionExpressionExecutorLongFloat(leftExpressionExecutor, rightExpressionExecutor);
                    case DOUBLE:
                        return new NotEqualCompareConditionExpressionExecutorLongDouble(leftExpressionExecutor, rightExpressionExecutor);
                    case BOOL:
                        throw new OperationNotSupportedException("long cannot be compared with bool");
                    default:
                        throw new OperationNotSupportedException("long cannot be compared with " + rightExpressionExecutor.getReturnType());
                }
            case FLOAT:
                switch (rightExpressionExecutor.getReturnType()) {
                    case STRING:
                        throw new OperationNotSupportedException("float cannot be compared with string");
                    case INT:
                        return new NotEqualCompareConditionExpressionExecutorFloatInt(leftExpressionExecutor, rightExpressionExecutor);
                    case LONG:
                        return new NotEqualCompareConditionExpressionExecutorFloatLong(leftExpressionExecutor, rightExpressionExecutor);
                    case FLOAT:
                        return new NotEqualCompareConditionExpressionExecutorFloatFloat(leftExpressionExecutor, rightExpressionExecutor);
                    case DOUBLE:
                        return new NotEqualCompareConditionExpressionExecutorFloatDouble(leftExpressionExecutor, rightExpressionExecutor);
                    case BOOL:
                        throw new OperationNotSupportedException("float cannot be compared with bool");
                    default:
                        throw new OperationNotSupportedException("float cannot be compared with " + rightExpressionExecutor.getReturnType());
                }
            case DOUBLE:
                switch (rightExpressionExecutor.getReturnType()) {
                    case STRING:
                        throw new OperationNotSupportedException("double cannot be compared with string");
                    case INT:
                        return new NotEqualCompareConditionExpressionExecutorDoubleInt(leftExpressionExecutor, rightExpressionExecutor);
                    case LONG:
                        return new NotEqualCompareConditionExpressionExecutorDoubleLong(leftExpressionExecutor, rightExpressionExecutor);
                    case FLOAT:
                        return new NotEqualCompareConditionExpressionExecutorDoubleFloat(leftExpressionExecutor, rightExpressionExecutor);
                    case DOUBLE:
                        return new NotEqualCompareConditionExpressionExecutorDoubleDouble(leftExpressionExecutor, rightExpressionExecutor);
                    case BOOL:
                        throw new OperationNotSupportedException("double cannot be compared with bool");
                    default:
                        throw new OperationNotSupportedException("double cannot be compared with " + rightExpressionExecutor.getReturnType());
                }
            case BOOL:
                switch (rightExpressionExecutor.getReturnType()) {
                    case BOOL:
                        return new NotEqualCompareConditionExpressionExecutorBoolBool(leftExpressionExecutor, rightExpressionExecutor);
                    default:
                        throw new OperationNotSupportedException("bool cannot be compared with " + rightExpressionExecutor.getReturnType());
                }
            default:
                throw new OperationNotSupportedException(leftExpressionExecutor.getReturnType() + " cannot be used in not equal comparisons");
        }
    }

    /**
     * Parse and validate the given Siddhi variable and return a VariableExpressionExecutor
     *
     * @param variable     Variable to be parsed
     * @param metaEvent    Meta event used to collect execution info of stream associated with query
     * @param currentState Current State Number
     * @param executorList List to hold VariableExpressionExecutors to update after query parsing  @return VariableExpressionExecutor representing given variable
     */
    private static ExpressionExecutor parseVariable(Variable variable,
                                                    MetaComplexEvent metaEvent,
                                                    int currentState,
                                                    List<VariableExpressionExecutor> executorList,
                                                    int defaultStreamEventIndex) {
        String attributeName = variable.getAttributeName();
        int[] eventPosition = new int[2];
        if (variable.getStreamIndex() != null) {
            if (variable.getStreamIndex() <= LAST) {
                eventPosition[STREAM_EVENT_INDEX] = variable.getStreamIndex() + 1;
            } else {
                eventPosition[STREAM_EVENT_INDEX] = variable.getStreamIndex();
            }
        } else {
            eventPosition[STREAM_EVENT_INDEX] = defaultStreamEventIndex;
        }
        eventPosition[STREAM_EVENT_CHAIN_INDEX] = UNKNOWN_STATE;
        if (metaEvent instanceof MetaStreamEvent) {
            MetaStreamEvent metaStreamEvent = (MetaStreamEvent) metaEvent;
            AbstractDefinition abstractDefinition;
            Attribute.Type type;
            if (currentState == HAVING_STATE) {
                abstractDefinition = metaStreamEvent.getOutputStreamDefinition();
                type = abstractDefinition.getAttributeType(attributeName);
                eventPosition[STREAM_EVENT_CHAIN_INDEX] = HAVING_STATE;
            } else {
                abstractDefinition = metaStreamEvent.getLastInputDefinition();
                type = abstractDefinition.getAttributeType(attributeName);
                ((MetaStreamEvent) metaEvent).addData(new Attribute(attributeName, type));
            }
            VariableExpressionExecutor variableExpressionExecutor = new VariableExpressionExecutor(new Attribute(attributeName, type), eventPosition[STREAM_EVENT_CHAIN_INDEX], eventPosition[STREAM_EVENT_INDEX]);
            if (((MetaStreamEvent) metaEvent).isTableEvent()) {
                variableExpressionExecutor.getPosition()[STREAM_ATTRIBUTE_TYPE_INDEX] = OUTPUT_DATA_INDEX;
                variableExpressionExecutor.getPosition()[STREAM_ATTRIBUTE_INDEX] = abstractDefinition.getAttributePosition(variableExpressionExecutor.getAttribute().getName());
            }
            if (executorList != null) {
                executorList.add(variableExpressionExecutor);
            }
            return variableExpressionExecutor;
        } else {
            MetaStateEvent metaStateEvent = (MetaStateEvent) metaEvent;
            Attribute.Type type = null;
            AbstractDefinition definition = null;
            String firstInput = null;
            if (variable.getStreamId() == null) {
                MetaStreamEvent[] metaStreamEvents = metaStateEvent.getMetaStreamEvents();
                if (currentState == HAVING_STATE) {
                    definition = metaStateEvent.getOutputStreamDefinition();
                    try {
                        type = definition.getAttributeType(attributeName);
                        eventPosition[STREAM_EVENT_CHAIN_INDEX] = HAVING_STATE;
                    } catch (AttributeNotExistException e) {
                        currentState = UNKNOWN_STATE;
                    }
                }
                if (currentState == UNKNOWN_STATE) {
                    for (int i = 0; i < metaStreamEvents.length; i++) {
                        MetaStreamEvent metaStreamEvent = metaStreamEvents[i];
                        definition = metaStreamEvent.getLastInputDefinition();
                        if (type == null) {
                            try {

                                type = definition.getAttributeType(attributeName);
                                firstInput = "Input Stream: " + definition.getId() + " with " +
                                        "reference: " + metaStreamEvent.getInputReferenceId();
                                eventPosition[STREAM_EVENT_CHAIN_INDEX] = i;
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
                } else if (currentState >= 0) {
                    MetaStreamEvent metaStreamEvent = metaStreamEvents[currentState];
                    definition = metaStreamEvent.getLastInputDefinition();
                    try {
                        type = definition.getAttributeType(attributeName);
                        eventPosition[STREAM_EVENT_CHAIN_INDEX] = currentState;
                    } catch (AttributeNotExistException e) {
                        throw new ExecutionPlanValidationException(e.getMessage() + " Input Stream: " +
                                definition.getId() + " with " + "reference: " + metaStreamEvent.getInputReferenceId());
                    }
                }
            } else {
                MetaStreamEvent[] metaStreamEvents = metaStateEvent.getMetaStreamEvents();
                for (int i = 0, metaStreamEventsLength = metaStreamEvents.length; i < metaStreamEventsLength; i++) {
                    MetaStreamEvent metaStreamEvent = metaStreamEvents[i];
                    definition = metaStreamEvent.getLastInputDefinition();
                    if (metaStreamEvent.getInputReferenceId() == null) {
                        if (definition.getId().equals(variable.getStreamId())) {
                            type = definition.getAttributeType(attributeName);
                            eventPosition[STREAM_EVENT_CHAIN_INDEX] = i;
                            break;
                        }
                    } else {
                        if (metaStreamEvent.getInputReferenceId().equals(variable.getStreamId())) {
                            type = definition.getAttributeType(attributeName);
                            eventPosition[STREAM_EVENT_CHAIN_INDEX] = i;
                            if (currentState > -1 && metaStreamEvents[currentState].getInputReferenceId() != null && variable.getStreamIndex() != null && variable.getStreamIndex() <= LAST) {
                                if (variable.getStreamId().equals(metaStreamEvents[currentState].getInputReferenceId())) {
                                    eventPosition[STREAM_EVENT_INDEX] = variable.getStreamIndex();
                                }
                            }
                            break;
                        }
                    }
                }
            }
            if (eventPosition[STREAM_EVENT_CHAIN_INDEX] == UNKNOWN_STATE) {
                throw new ExecutionPlanValidationException("Stream with reference : " +
                        variable.getStreamId() + " not found");
            }

            VariableExpressionExecutor variableExpressionExecutor = new VariableExpressionExecutor(new Attribute(attributeName, type), eventPosition[STREAM_EVENT_CHAIN_INDEX], eventPosition[STREAM_EVENT_INDEX]);
            if (eventPosition[STREAM_EVENT_CHAIN_INDEX] != HAVING_STATE) {
                MetaStreamEvent metaStreamEvent = ((MetaStateEvent) metaEvent).getMetaStreamEvent(eventPosition[STREAM_EVENT_CHAIN_INDEX]);
                if (metaStreamEvent.isTableEvent()) {
                    variableExpressionExecutor.getPosition()[STREAM_ATTRIBUTE_TYPE_INDEX] = OUTPUT_DATA_INDEX;
                    variableExpressionExecutor.getPosition()[STREAM_ATTRIBUTE_INDEX] = metaStreamEvent.getLastInputDefinition().getAttributePosition(variableExpressionExecutor.getAttribute().getName());
                    for (Attribute attribute : ((MetaStateEvent) metaEvent).getMetaStreamEvent(eventPosition[STREAM_EVENT_CHAIN_INDEX]).getLastInputDefinition().getAttributeList()) {
                        ((MetaStateEvent) metaEvent).getMetaStreamEvent(eventPosition[STREAM_EVENT_CHAIN_INDEX]).addOutputData(new Attribute(attribute.getName(), attribute.getType()));
                    }
                }
                ((MetaStateEvent) metaEvent).getMetaStreamEvent(eventPosition[STREAM_EVENT_CHAIN_INDEX]).addData(new Attribute(attributeName, type));
            }
            if (executorList != null) {
                executorList.add(variableExpressionExecutor);
            }
            return variableExpressionExecutor;
        }
    }

    /**
     * Calculate the return type of arithmetic operation executors.(Ex: add, subtract, etc)
     *
     * @param leftExpressionExecutor  left ExpressionExecutor
     * @param rightExpressionExecutor right ExpressionExecutor
     * @return Attribute Type
     */
    private static Attribute.Type parseArithmeticOperationResultType(
            ExpressionExecutor leftExpressionExecutor, ExpressionExecutor rightExpressionExecutor) {
        if (leftExpressionExecutor.getReturnType() == Attribute.Type.DOUBLE || rightExpressionExecutor.getReturnType() == Attribute.Type.DOUBLE) {
            return Attribute.Type.DOUBLE;
        } else if (leftExpressionExecutor.getReturnType() == Attribute.Type.FLOAT || rightExpressionExecutor.getReturnType() == Attribute.Type.FLOAT) {
            return Attribute.Type.FLOAT;
        } else if (leftExpressionExecutor.getReturnType() == Attribute.Type.LONG || rightExpressionExecutor.getReturnType() == Attribute.Type.LONG) {
            return Attribute.Type.LONG;
        } else if (leftExpressionExecutor.getReturnType() == Attribute.Type.INT || rightExpressionExecutor.getReturnType() == Attribute.Type.INT) {
            return Attribute.Type.INT;
        } else {
            throw new ArithmeticException("Arithmetic operation between " + leftExpressionExecutor.getReturnType() + " and " + rightExpressionExecutor.getReturnType() + " cannot be executed");
        }
    }

    /**
     * Parse the set of inner expression of AttributeFunctionExtensions and  handling all (*) cases
     * @param innerExpressions        InnerExpressions to be parsed
     * @param metaEvent               Meta Event
     * @param currentState            Current state number
     * @param eventTableMap           Event Table Map
     * @param executorList            List to hold VariableExpressionExecutors to update after query parsing  @return
     * @param executionPlanContext    ExecutionPlanContext
     * @param groupBy                 is for groupBy expression
     * @param defaultStreamEventIndex Default StreamEvent Index
     * @return  List of expressionExecutors
     */
    private static ExpressionExecutor[] parseInnerExpression(Expression[] innerExpressions, MetaComplexEvent metaEvent, int currentState,
                                                             Map<String, EventTable> eventTableMap, List<VariableExpressionExecutor> executorList,
                                                             ExecutionPlanContext executionPlanContext, boolean groupBy, int defaultStreamEventIndex) {
        ExpressionExecutor[] innerExpressionExecutors;
        if (innerExpressions != null) {
            if (innerExpressions.length > 0) {
                innerExpressionExecutors = new ExpressionExecutor[innerExpressions.length];
                for (int i = 0, innerExpressionsLength = innerExpressions.length; i < innerExpressionsLength; i++) {
                    innerExpressionExecutors[i] = parseExpression(innerExpressions[i], metaEvent, currentState, eventTableMap, executorList, executionPlanContext, groupBy, defaultStreamEventIndex);
                }
            } else {
                List<Expression> outputAttributes = new ArrayList<Expression>();
                if (metaEvent instanceof MetaStreamEvent) {

                    List<Attribute> attributeList = ((MetaStreamEvent) metaEvent).getLastInputDefinition().getAttributeList();
                    for (Attribute attribute : attributeList) {
                        outputAttributes.add(new Variable(attribute.getName()));
                    }
                } else {
                    for (MetaStreamEvent metaStreamEvent : ((MetaStateEvent) metaEvent).getMetaStreamEvents()) {
                        List<Attribute> attributeList = metaStreamEvent.getLastInputDefinition().getAttributeList();
                        for (Attribute attribute : attributeList) {
                            Expression outputAttribute = new Variable(attribute.getName());
                            if (!outputAttributes.contains(outputAttribute)) {
                                outputAttributes.add(outputAttribute);
                            } else {
                                List<AbstractDefinition> definitions = new ArrayList<AbstractDefinition>();
                                for (MetaStreamEvent aMetaStreamEvent : ((MetaStateEvent) metaEvent).getMetaStreamEvents()) {
                                    definitions.add(aMetaStreamEvent.getLastInputDefinition());
                                }
                                throw new DuplicateAttributeException("Duplicate attribute exist in streams " + definitions);
                            }
                        }
                    }
                }
                innerExpressionExecutors = new ExpressionExecutor[outputAttributes.size()];
                for (int i = 0, innerExpressionsLength = outputAttributes.size(); i < innerExpressionsLength; i++) {
                    innerExpressionExecutors[i] = parseExpression(outputAttributes.get(i), metaEvent, currentState, eventTableMap, executorList, executionPlanContext, groupBy, defaultStreamEventIndex);
                }
            }
        } else {
            innerExpressionExecutors = new ExpressionExecutor[0];
        }
        return innerExpressionExecutors;
    }

}
