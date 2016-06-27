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

import org.wso2.siddhi.core.event.stream.MetaStreamEvent;
import org.wso2.siddhi.core.util.collection.expression.*;
import org.wso2.siddhi.core.util.collection.operator.MatchingMetaStateHolder;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.expression.Variable;
import org.wso2.siddhi.query.api.expression.condition.*;
import org.wso2.siddhi.query.api.expression.constant.Constant;
import org.wso2.siddhi.query.api.expression.function.AttributeFunction;
import org.wso2.siddhi.query.api.expression.function.AttributeFunctionExtension;
import org.wso2.siddhi.query.api.expression.math.*;

import java.util.List;

/**
 * Class to parse Expressions and create Expression executors.
 */
public class CollectionExpressionParser {

    /**
     * Parse the given expression and create the appropriate Executor by recursively traversing the expression
     *
     * @param expression              Expression to be parsed
     * @param matchingMetaStateHolder matchingMetaStateHolder
     * @param indexAttributes
     * @return ExpressionExecutor
     */
    public static CollectionExpression parseCollectionExpression(Expression expression, MatchingMetaStateHolder matchingMetaStateHolder, List<String> indexAttributes) {
        if (expression instanceof And) {

            CollectionExpression leftCollectionExpression = parseCollectionExpression(((And) expression).getLeftExpression(), matchingMetaStateHolder, indexAttributes);
            CollectionExpression rightCollectionExpression = parseCollectionExpression(((And) expression).getRightExpression(), matchingMetaStateHolder, indexAttributes);

            if (leftCollectionExpression.getCollectionScope() == CollectionExpression.CollectionScope.NON &&
                    rightCollectionExpression.getCollectionScope() == CollectionExpression.CollectionScope.NON) {
                return new BasicCollectionExpression(expression, CollectionExpression.CollectionScope.NON);
            } else if (leftCollectionExpression.getCollectionScope() == CollectionExpression.CollectionScope.EXHAUSTIVE &&
                    rightCollectionExpression.getCollectionScope() == CollectionExpression.CollectionScope.EXHAUSTIVE) {
                return new BasicCollectionExpression(expression, CollectionExpression.CollectionScope.EXHAUSTIVE);
            } else {
                return new AndCollectionExpression(expression, CollectionExpression.CollectionScope.OPTIMISED_RESULT_SET,
                        leftCollectionExpression, rightCollectionExpression);
            }
        } else if (expression instanceof Or) {
            CollectionExpression leftCollectionExpression = parseCollectionExpression(((Or) expression).getLeftExpression(), matchingMetaStateHolder, indexAttributes);
            CollectionExpression rightCollectionExpression = parseCollectionExpression(((Or) expression).getRightExpression(), matchingMetaStateHolder, indexAttributes);

            if (leftCollectionExpression.getCollectionScope() == CollectionExpression.CollectionScope.NON &&
                    rightCollectionExpression.getCollectionScope() == CollectionExpression.CollectionScope.NON) {
                return new BasicCollectionExpression(expression, CollectionExpression.CollectionScope.NON);
            } else if (leftCollectionExpression.getCollectionScope() == CollectionExpression.CollectionScope.EXHAUSTIVE ||
                    rightCollectionExpression.getCollectionScope() == CollectionExpression.CollectionScope.EXHAUSTIVE) {
                return new BasicCollectionExpression(expression, CollectionExpression.CollectionScope.EXHAUSTIVE);
            } else {
                return new OrCollectionExpression(expression, CollectionExpression.CollectionScope.OPTIMISED_RESULT_SET,
                        leftCollectionExpression, rightCollectionExpression);
            }
        } else if (expression instanceof Not) {
            CollectionExpression notCollectionExpression = parseCollectionExpression(((Not) expression).getExpression(), matchingMetaStateHolder, indexAttributes);

            if (notCollectionExpression.getCollectionScope() == CollectionExpression.CollectionScope.EXHAUSTIVE) {
                return new BasicCollectionExpression(expression, CollectionExpression.CollectionScope.EXHAUSTIVE);
            } else if (notCollectionExpression.getCollectionScope() == CollectionExpression.CollectionScope.NON) {
                return new BasicCollectionExpression(expression, CollectionExpression.CollectionScope.NON);
            } else if (notCollectionExpression.getCollectionScope() == CollectionExpression.CollectionScope.INDEXED_ATTRIBUTE) {
                return new NotCollectionExpression(expression,
                        CollectionExpression.CollectionScope.INDEXED_RESULT_SET, notCollectionExpression);
            } else {
                return new NotCollectionExpression(expression,
                        CollectionExpression.CollectionScope.OPTIMISED_RESULT_SET, notCollectionExpression);
            }
        } else if (expression instanceof Compare) {
//            if (((Compare) expression).getOperator() == Compare.Operator.EQUAL) {

            CollectionExpression leftCollectionExpression = parseCollectionExpression(((Compare) expression).getLeftExpression(), matchingMetaStateHolder, indexAttributes);
            CollectionExpression rightCollectionExpression = parseCollectionExpression(((Compare) expression).getRightExpression(), matchingMetaStateHolder, indexAttributes);

            if (leftCollectionExpression.getCollectionScope() == CollectionExpression.CollectionScope.NON &&
                    rightCollectionExpression.getCollectionScope() == CollectionExpression.CollectionScope.NON) {
                return new BasicCollectionExpression(expression, CollectionExpression.CollectionScope.NON);
            } else if (leftCollectionExpression.getCollectionScope() == CollectionExpression.CollectionScope.INDEXED_ATTRIBUTE &&
                    rightCollectionExpression.getCollectionScope() == CollectionExpression.CollectionScope.NON) {
                return new CompareCollectionExpression(expression,
                        CollectionExpression.CollectionScope.INDEXED_RESULT_SET, leftCollectionExpression, rightCollectionExpression);
            } else if (leftCollectionExpression.getCollectionScope() == CollectionExpression.CollectionScope.NON &&
                    rightCollectionExpression.getCollectionScope() == CollectionExpression.CollectionScope.INDEXED_ATTRIBUTE) {
                return new CompareCollectionExpression(expression,
                        CollectionExpression.CollectionScope.INDEXED_RESULT_SET, leftCollectionExpression, rightCollectionExpression);
            } else {
                return new BasicCollectionExpression(expression, CollectionExpression.CollectionScope.EXHAUSTIVE);
            }
//            } else if (((Compare) expression).getOperator() == Compare.Operator.NOT_EQUAL) {
//                return parseNotEqualCompare(
//                        buildCollectionExecutor(((Compare) expression).getLeftExpression(), matchingMetaStateHolder),
//                        buildCollectionExecutor(((Compare) expression).getRightExpression(), matchingMetaStateHolder));
//            } else if (((Compare) expression).getOperator() == Compare.Operator.GREATER_THAN) {
//                return parseGreaterThanCompare(
//                        buildCollectionExecutor(((Compare) expression).getLeftExpression(), matchingMetaStateHolder),
//                        buildCollectionExecutor(((Compare) expression).getRightExpression(), matchingMetaStateHolder));
//            } else if (((Compare) expression).getOperator() == Compare.Operator.GREATER_THAN_EQUAL) {
//                return parseGreaterThanEqualCompare(
//                        buildCollectionExecutor(((Compare) expression).getLeftExpression(), matchingMetaStateHolder),
//                        buildCollectionExecutor(((Compare) expression).getRightExpression(), matchingMetaStateHolder));
//            } else if (((Compare) expression).getOperator() == Compare.Operator.LESS_THAN) {
//                return parseLessThanCompare(
//                        buildCollectionExecutor(((Compare) expression).getLeftExpression(), matchingMetaStateHolder),
//                        buildCollectionExecutor(((Compare) expression).getRightExpression(), matchingMetaStateHolder));
//            } else if (((Compare) expression).getOperator() == Compare.Operator.LESS_THAN_EQUAL) {
//                return parseLessThanEqualCompare(
//                        buildCollectionExecutor(((Compare) expression).getLeftExpression(), matchingMetaStateHolder),
//                        buildCollectionExecutor(((Compare) expression).getRightExpression(), matchingMetaStateHolder));
//            }

        } else if (expression instanceof Constant) {
            return new BasicCollectionExpression(expression, CollectionExpression.CollectionScope.NON);
//            if (expression instanceof BoolConstant) {
//                return new ConstantExpressionExecutor(((BoolConstant) expression).getValue(), Attribute.Type.BOOL);
//            } else if (expression instanceof StringConstant) {
//                return new ConstantExpressionExecutor(((StringConstant) expression).getValue(), Attribute.Type.STRING);
//            } else if (expression instanceof IntConstant) {
//                return new ConstantExpressionExecutor(((IntConstant) expression).getValue(), Attribute.Type.INT);
//            } else if (expression instanceof LongConstant) {
//                return new ConstantExpressionExecutor(((LongConstant) expression).getValue(), Attribute.Type.LONG);
//            } else if (expression instanceof FloatConstant) {
//                return new ConstantExpressionExecutor(((FloatConstant) expression).getValue(), Attribute.Type.FLOAT);
//            } else if (expression instanceof DoubleConstant) {
//                return new ConstantExpressionExecutor(((DoubleConstant) expression).getValue(), Attribute.Type.DOUBLE);
//            }

        } else if (expression instanceof Variable) {
            if (isCollectionVariable(matchingMetaStateHolder, (Variable) expression)) {
                if (indexAttributes.contains(((Variable) expression).getAttributeName())) {
//                MetaStreamEvent CollectionStreamEvent = matchingMetaStateHolder.getMetaStateEvent().getMetaStreamEvent(matchingMetaStateHolder.getCandidateEventIndex());
                    return new AttributeCollectionExpression(expression, ((Variable) expression).getAttributeName());
                } else {
                    return new BasicCollectionExpression(expression, CollectionExpression.CollectionScope.EXHAUSTIVE);
                }
            } else {
                return new BasicCollectionExpression(expression, CollectionExpression.CollectionScope.NON);
            }
//            return "variable database t/f, stream/Collection name, attribute ";
//            return parseVariable((Variable) expression, matchingMetaStateHolder, currentState, executorList, defaultStreamEventIndex);

        } else if (expression instanceof Multiply) {
            CollectionExpression left = parseCollectionExpression(((Multiply) expression).getLeftValue(), matchingMetaStateHolder, indexAttributes);
            CollectionExpression right = parseCollectionExpression(((Multiply) expression).getRightValue(), matchingMetaStateHolder, indexAttributes);
            if (left.getCollectionScope() == CollectionExpression.CollectionScope.NON && right.getCollectionScope() == CollectionExpression.CollectionScope.NON) {
                return new BasicCollectionExpression(expression, CollectionExpression.CollectionScope.NON);
            } else {
                return new BasicCollectionExpression(expression, CollectionExpression.CollectionScope.EXHAUSTIVE);
            }

//            Attribute.Type type = parseArithmeticOperationResultType(left, right);
//            switch (type) {
//                case INT:
//                    return new MultiplyExpressionExecutorInt(left, right);
//                case LONG:
//                    return new MultiplyExpressionExecutorLong(left, right);
//                case FLOAT:
//                    return new MultiplyExpressionExecutorFloat(left, right);
//                case DOUBLE:
//                    return new MultiplyExpressionExecutorDouble(left, right);
//                default: //Will not happen. Handled in parseArithmeticOperationResultType()
//            }
        } else if (expression instanceof Add) {
            CollectionExpression left = parseCollectionExpression(((Add) expression).getLeftValue(), matchingMetaStateHolder, indexAttributes);
            CollectionExpression right = parseCollectionExpression(((Add) expression).getRightValue(), matchingMetaStateHolder, indexAttributes);
            if (left.getCollectionScope() == CollectionExpression.CollectionScope.NON && right.getCollectionScope() == CollectionExpression.CollectionScope.NON) {
                return new BasicCollectionExpression(expression, CollectionExpression.CollectionScope.NON);
            } else {
                return new BasicCollectionExpression(expression, CollectionExpression.CollectionScope.EXHAUSTIVE);
            }
//            ExpressionExecutor left = buildCollectionExecutor(((Add) expression).getLeftValue(), matchingMetaStateHolder);
//            ExpressionExecutor right = buildCollectionExecutor(((Add) expression).getRightValue(), matchingMetaStateHolder);
//            Attribute.Type type = parseArithmeticOperationResultType(left, right);
//            switch (type) {
//                case INT:
//                    return new AddExpressionExecutorInt(left, right);
//                case LONG:
//                    return new AddExpressionExecutorLong(left, right);
//                case FLOAT:
//                    return new AddExpressionExecutorFloat(left, right);
//                case DOUBLE:
//                    return new AddExpressionExecutorDouble(left, right);
//                default: //Will not happen. Handled in parseArithmeticOperationResultType()
//            }
        } else if (expression instanceof Subtract) {
            CollectionExpression left = parseCollectionExpression(((Subtract) expression).getLeftValue(), matchingMetaStateHolder, indexAttributes);
            CollectionExpression right = parseCollectionExpression(((Subtract) expression).getRightValue(), matchingMetaStateHolder, indexAttributes);
            if (left.getCollectionScope() == CollectionExpression.CollectionScope.NON && right.getCollectionScope() == CollectionExpression.CollectionScope.NON) {
                return new BasicCollectionExpression(expression, CollectionExpression.CollectionScope.NON);
            } else {
                return new BasicCollectionExpression(expression, CollectionExpression.CollectionScope.EXHAUSTIVE);
            }
//
//            ExpressionExecutor left = buildCollectionExecutor(((Subtract) expression).getLeftValue(), matchingMetaStateHolder);
//            ExpressionExecutor right = buildCollectionExecutor(((Subtract) expression).getRightValue(), matchingMetaStateHolder);
//            Attribute.Type type = parseArithmeticOperationResultType(left, right);
//            switch (type) {
//                case INT:
//                    return new SubtractExpressionExecutorInt(left, right);
//                case LONG:
//                    return new SubtractExpressionExecutorLong(left, right);
//                case FLOAT:
//                    return new SubtractExpressionExecutorFloat(left, right);
//                case DOUBLE:
//                    return new SubtractExpressionExecutorDouble(left, right);
//                default: //Will not happen. Handled in parseArithmeticOperationResultType()
//            }
        } else if (expression instanceof Mod) {
            CollectionExpression left = parseCollectionExpression(((Mod) expression).getLeftValue(), matchingMetaStateHolder, indexAttributes);
            CollectionExpression right = parseCollectionExpression(((Mod) expression).getRightValue(), matchingMetaStateHolder, indexAttributes);
            if (left.getCollectionScope() == CollectionExpression.CollectionScope.NON && right.getCollectionScope() == CollectionExpression.CollectionScope.NON) {
                return new BasicCollectionExpression(expression, CollectionExpression.CollectionScope.NON);
            } else {
                return new BasicCollectionExpression(expression, CollectionExpression.CollectionScope.EXHAUSTIVE);
            }
//
//            ExpressionExecutor left = buildCollectionExecutor(((Mod) expression).getLeftValue(), matchingMetaStateHolder);
//            ExpressionExecutor right = buildCollectionExecutor(((Mod) expression).getRightValue(), matchingMetaStateHolder);
//            Attribute.Type type = parseArithmeticOperationResultType(left, right);
//            switch (type) {
//                case INT:
//                    return new ModExpressionExecutorInt(left, right);
//                case LONG:
//                    return new ModExpressionExecutorLong(left, right);
//                case FLOAT:
//                    return new ModExpressionExecutorFloat(left, right);
//                case DOUBLE:
//                    return new ModExpressionExecutorDouble(left, right);
//                default: //Will not happen. Handled in parseArithmeticOperationResultType()
//            }
        } else if (expression instanceof Divide) {
            CollectionExpression left = parseCollectionExpression(((Divide) expression).getLeftValue(), matchingMetaStateHolder, indexAttributes);
            CollectionExpression right = parseCollectionExpression(((Divide) expression).getRightValue(), matchingMetaStateHolder, indexAttributes);
            if (left.getCollectionScope() == CollectionExpression.CollectionScope.NON && right.getCollectionScope() == CollectionExpression.CollectionScope.NON) {
                return new BasicCollectionExpression(expression, CollectionExpression.CollectionScope.NON);
            } else {
                return new BasicCollectionExpression(expression, CollectionExpression.CollectionScope.EXHAUSTIVE);
            }
//
//            ExpressionExecutor expressionExecutor = ExpressionParser.buildCollectionExecutor(expression,
//                    matchingMetaStateHolder.getMetaStateEvent(), matchingMetaStateHolder.getDefaultStreamEventIndex(), eventCollectionMap, variableExpressionExecutors, executionPlanContext, false, 0);


//            ExpressionExecutor left = buildCollectionExecutor(((Divide) expression).getLeftValue(), matchingMetaStateHolder);
//            ExpressionExecutor right = buildCollectionExecutor(((Divide) expression).getRightValue(), matchingMetaStateHolder);
//            Attribute.Type type = parseArithmeticOperationResultType(left, right);
//            switch (type) {
//                case INT:
//                    return new DivideExpressionExecutorInt(left, right);
//                case LONG:
//                    return new DivideExpressionExecutorLong(left, right);
//                case FLOAT:
//                    return new DivideExpressionExecutorFloat(left, right);
//                case DOUBLE:
//                    return new DivideExpressionExecutorDouble(left, right);
//                default: //Will not happen. Handled in parseArithmeticOperationResultType()
//            }

        } else if (expression instanceof AttributeFunctionExtension) {
            Expression[] innerExpressions = ((AttributeFunctionExtension) expression).getParameters();
            for (Expression aExpression : innerExpressions) {
                CollectionExpression collectionExpression = parseCollectionExpression(aExpression, matchingMetaStateHolder, indexAttributes);
                if (collectionExpression.getCollectionScope() != CollectionExpression.CollectionScope.NON) {
                    return new BasicCollectionExpression(expression, CollectionExpression.CollectionScope.EXHAUSTIVE);
                }
            }
            return new BasicCollectionExpression(expression, CollectionExpression.CollectionScope.NON);

//
//            //extensions
//            Object executor;
//            try {
//                executor = SiddhiClassLoader.loadExtensionImplementation((AttributeFunctionExtension) expression, FunctionExecutorExtensionHolder.getInstance(executionPlanContext));
//            } catch (ExecutionPlanCreationException ex) {
//                try {
//                    executor = SiddhiClassLoader.loadExtensionImplementation((AttributeFunctionExtension) expression, AttributeAggregatorExtensionHolder.getInstance(executionPlanContext));
//                    SelectorParser.getContainsAggregatorThreadLocal().set("true");
//                } catch (ExecutionPlanCreationException e) {
//                    throw new ExecutionPlanCreationException("'" + ((AttributeFunctionExtension) expression).getFunction() + "' is neither a function extension nor an aggregated attribute extension");
//                }
//            }
//            if (executor instanceof FunctionExecutor) {
//                FunctionExecutor expressionExecutor = (FunctionExecutor) executor;
//                Expression[] innerExpressions = ((AttributeFunctionExtension) expression).getParameters();
//                ExpressionExecutor[] innerExpressionExecutors = parseInnerExpression(innerExpressions, matchingMetaStateHolder, currentState, eventCollectionMap, executorList,
//                        executionPlanContext, groupBy, defaultStreamEventIndex);
//                expressionExecutor.initExecutor(innerExpressionExecutors, executionPlanContext);
//                if (expressionExecutor.getReturnType() == Attribute.Type.BOOL) {
//                    return new BoolConditionExpressionExecutor(expressionExecutor);
//                }
//                return expressionExecutor;
//            } else {
//                AttributeAggregator attributeAggregator = (AttributeAggregator) executor;
//                Expression[] innerExpressions = ((AttributeFunctionExtension) expression).getParameters();
//                ExpressionExecutor[] innerExpressionExecutors = parseInnerExpression(innerExpressions, matchingMetaStateHolder, currentState, eventCollectionMap, executorList,
//                        executionPlanContext, groupBy, defaultStreamEventIndex);
//                attributeAggregator.initAggregator(innerExpressionExecutors, executionPlanContext);
//                AbstractAggregationAttributeExecutor aggregationAttributeProcessor;
//                if (groupBy) {
//                    aggregationAttributeProcessor = new GroupByAggregationAttributeExecutor(attributeAggregator, innerExpressionExecutors, executionPlanContext);
//                } else {
//                    aggregationAttributeProcessor = new AggregationAttributeExecutor(attributeAggregator, innerExpressionExecutors, executionPlanContext);
//                }
//                SelectorParser.getContainsAggregatorThreadLocal().set("true");
//                return aggregationAttributeProcessor;
//            }
        } else if (expression instanceof AttributeFunction) {
            Expression[] innerExpressions = ((AttributeFunction) expression).getParameters();
            for (Expression aExpression : innerExpressions) {
                CollectionExpression aCollectionExpression = parseCollectionExpression(aExpression, matchingMetaStateHolder, indexAttributes);
                if (aCollectionExpression.getCollectionScope() != CollectionExpression.CollectionScope.NON) {
                    return new BasicCollectionExpression(expression, CollectionExpression.CollectionScope.EXHAUSTIVE);
                }
            }
            return new BasicCollectionExpression(expression, CollectionExpression.CollectionScope.NON);

//
//            Object executor;
//            try {
//                executor = SiddhiClassLoader.loadSiddhiImplementation(((AttributeFunction) expression).getFunction(), AttributeAggregator.class);
//            } catch (ExecutionPlanCreationException ex) {
//                try {
//                    if (executionPlanContext.isFunctionExist(((AttributeFunction) expression).getFunction())) {
//                        executor = new ScriptFunctionExecutor(((AttributeFunction) expression).getFunction());
//                    } else {
//                        try {
//                            executor = SiddhiClassLoader.loadSiddhiImplementation(((AttributeFunction) expression).getFunction(), FunctionExecutor.class);
//                        } catch (ExecutionPlanCreationException e) {
//                            throw new ExecutionPlanCreationException(e.getMessage(), e);
//                        }
//                    }
//                } catch (ExecutionPlanCreationException e) {
//                    throw new ExecutionPlanCreationException(((AttributeFunction) expression).getFunction() + " is neither a function nor an aggregated attribute");
//                }
//            }
//
//            if (executor instanceof AttributeAggregator) {
//                Expression[] innerExpressions = ((AttributeFunction) expression).getParameters();
//                ExpressionExecutor[] innerExpressionExecutors = parseInnerExpression(innerExpressions, matchingMetaStateHolder, currentState, eventCollectionMap, executorList,
//                        executionPlanContext, groupBy, defaultStreamEventIndex);
//                ((AttributeAggregator) executor).initAggregator(innerExpressionExecutors, executionPlanContext);
//                AbstractAggregationAttributeExecutor aggregationAttributeProcessor;
//                if (groupBy) {
//                    aggregationAttributeProcessor = new GroupByAggregationAttributeExecutor((AttributeAggregator) executor, innerExpressionExecutors, executionPlanContext);
//                } else {
//                    aggregationAttributeProcessor = new AggregationAttributeExecutor((AttributeAggregator) executor, innerExpressionExecutors, executionPlanContext);
//                }
//                SelectorParser.getContainsAggregatorThreadLocal().set("true");
//                return aggregationAttributeProcessor;
//            } else {
//                FunctionExecutor functionExecutor = (FunctionExecutor) executor;
//                Expression[] innerExpressions = ((AttributeFunction) expression).getParameters();
//                ExpressionExecutor[] innerExpressionExecutors = parseInnerExpression(innerExpressions, matchingMetaStateHolder, currentState, eventCollectionMap, executorList,
//                        executionPlanContext, groupBy, defaultStreamEventIndex);
//                functionExecutor.initExecutor(innerExpressionExecutors, executionPlanContext);
//                if (functionExecutor.getReturnType() == Attribute.Type.BOOL) {
//                    return new BoolConditionExpressionExecutor(functionExecutor);
//                }
//                return functionExecutor;
//            }
        } else if (expression instanceof In) {
            CollectionExpression inCollectionExpression = parseCollectionExpression(((In) expression).getExpression(), matchingMetaStateHolder, indexAttributes);
            if (inCollectionExpression.getCollectionScope() != CollectionExpression.CollectionScope.NON) {
                return new BasicCollectionExpression(expression, CollectionExpression.CollectionScope.EXHAUSTIVE);
            }

            return new BasicCollectionExpression(expression, CollectionExpression.CollectionScope.NON);

//
//            EventCollection eventCollection = eventCollectionMap.get(((In) expression).getSourceId());
//            MatchingMetaStateHolder matchingMetaStateHolder = MatcherParser.constructMatchingMetaStateHolder(matchingMetaStateHolder, defaultStreamEventIndex, eventCollection.getCollectionDefinition());
//            Finder finder = eventCollection.constructFinder(((In) expression).getExpression(), matchingMetaStateHolder, executionPlanContext, executorList, eventCollectionMap);
//            return new InConditionExpressionExecutor(eventCollection, finder, matchingMetaStateHolder.getStreamEventSize(), matchingMetaStateHolder instanceof StateEvent, matchingMetaStateHolder.getDefaultStreamEventIndex());

        } else if (expression instanceof IsNull) {

            CollectionExpression nullCollectionExpression = parseCollectionExpression(((IsNull) expression).getExpression(),
                    matchingMetaStateHolder, indexAttributes);

            if (nullCollectionExpression.getCollectionScope() == CollectionExpression.CollectionScope.NON) {
                return new BasicCollectionExpression(expression, CollectionExpression.CollectionScope.NON);
            } else if (nullCollectionExpression.getCollectionScope() == CollectionExpression.CollectionScope.INDEXED_ATTRIBUTE) {
                return new NullCollectionExpression(expression, CollectionExpression.CollectionScope.INDEXED_RESULT_SET,
                        nullCollectionExpression);
            } else {
                return new BasicCollectionExpression(expression, CollectionExpression.CollectionScope.EXHAUSTIVE);
            }
        }
        throw new UnsupportedOperationException(expression.toString() + " not supported!");
    }


    private static boolean isCollectionVariable(MatchingMetaStateHolder matchingMetaStateHolder, Variable variable) {
        if (variable.getStreamId() != null) {
            MetaStreamEvent CollectionStreamEvent = matchingMetaStateHolder.getMetaStateEvent().getMetaStreamEvent(matchingMetaStateHolder.getCandidateEventIndex());
            if (CollectionStreamEvent != null) {
                if ((CollectionStreamEvent.getInputReferenceId() != null && variable.getStreamId().equals(CollectionStreamEvent.getInputReferenceId())) ||
                        (CollectionStreamEvent.getLastInputDefinition().getId().equals(variable.getStreamId()))) {
//                    if (Arrays.asList(CollectionStreamEvent.getLastInputDefinition().getAttributeNameArray()).contains(indexAttribute)) {
                    return true;
//                    }
                }
            }
        }
        return false;
    }

//    public static CollectionExecutor buildCollectionExecutor(CollectionExpression expression, MatchingMetaStateHolder matchingMetaStateHolder, Map<String, EventCollection> eventCollectionMap, List<VariableExpressionExecutor> variableExpressionExecutors, ExecutionPlanContext executionPlanContext, List<String> indexedAttributes) {
//
//        if (expression instanceof CompareCollectionExpression) {
//
//            ExpressionExecutor expressionExecutor;
//            switch (expression.getCollectionScope()) {
//                case INDEX:
//                    if (indexedAttributes != null && indexedAttributes.containsAll(expression.getAttributeNames())) {
//
//                    }
//                    if(((CompareCollectionExpression) expression).getCollectionExpression().getCollectionScope()== CollectionExpression.CollectionScope.INDEX){
//                        ((CompareCollectionExpression) expression).getCollectionExpression()
//                    }
//                    break;
//                case NON:
//                    expressionExecutor = ExpressionParser.parseExpression(expression.getExpression(),
//                            matchingMetaStateHolder.getMetaStateEvent(), matchingMetaStateHolder.getDefaultStreamEventIndex(), eventCollectionMap, variableExpressionExecutors, executionPlanContext, false, 0);
//                    return new BasicCollectionExecutor(expressionExecutor);
//                break;
//                case EXHAUSTIVE:
//                    expressionExecutor = ExpressionParser.parseExpression(expression.getExpression(),
//                            matchingMetaStateHolder.getMetaStateEvent(), matchingMetaStateHolder.getDefaultStreamEventIndex(), eventCollectionMap, variableExpressionExecutors, executionPlanContext, false, 0);
//                    return new BasicExhaustiveCollectionExecutor(expressionExecutor);
//                break;
//            }
//        } else if (expression instanceof BasicCollectionExpression) {
//            ExpressionExecutor expressionExecutor;
//            switch (expression.getCollectionScope()) {
//                case NON:
//                    expressionExecutor = ExpressionParser.parseExpression(expression.getExpression(),
//                            matchingMetaStateHolder.getMetaStateEvent(), matchingMetaStateHolder.getDefaultStreamEventIndex(), eventCollectionMap, variableExpressionExecutors, executionPlanContext, false, 0);
//                    return new BasicCollectionExecutor(expressionExecutor);
//                break;
//                case INDEX:
//                    if (indexedAttributes != null && indexedAttributes.containsAll(expression.getAttributeNames())) {
//                        return new BasicIndexCollectionExecutor(expression.getAttributeNames(), indexedAttributes, matchingMetaStateHolder.getCandsidateDefinition());
//                    }
//                case EXHAUSTIVE:
//                    expressionExecutor = ExpressionParser.parseExpression(expression.getExpression(),
//                            matchingMetaStateHolder.getMetaStateEvent(), matchingMetaStateHolder.getDefaultStreamEventIndex(), eventCollectionMap, variableExpressionExecutors, executionPlanContext, false, 0);
//                    return new BasicExhaustiveCollectionExecutor(expressionExecutor);
//                break;
//            }
//        }
//        if (expression instanceof And) {
//
//            CollectionExpression leftCollectionExpression = buildCollectionExecutor(((And) expression).getLeftExpression(), matchingMetaStateHolder, eventCollectionMap, variableExpressionExecutors, executionPlanContext);
//            CollectionExpression rightCollectionExpression = buildCollectionExecutor(((And) expression).getRightExpression(), matchingMetaStateHolder, eventCollectionMap, variableExpressionExecutors, executionPlanContext);
//
//            if (leftCollectionExpression.getCollectionScope() == CollectionExpression.CollectionScope.EXHAUSTIVE ||
//                    rightCollectionExpression.getCollectionScope() == CollectionExpression.CollectionScope.EXHAUSTIVE) {
//                return new BasicCollectionExpression(expression, CollectionExpression.CollectionScope.EXHAUSTIVE);
//            } else if (leftCollectionExpression.getCollectionScope() == CollectionExpression.CollectionScope.INDEX &&
//                    rightCollectionExpression.getCollectionScope() == CollectionExpression.CollectionScope.NON) {
//                return new AndCollectionExpression(expression,
//                        CollectionExpression.CollectionScope.INDEX, leftCollectionExpression, rightCollectionExpression);
//            } else if (leftCollectionExpression.getCollectionScope() == CollectionExpression.CollectionScope.NON &&
//                    rightCollectionExpression.getCollectionScope() == CollectionExpression.CollectionScope.INDEX) {
//                return new AndCollectionExpression(expression,
//                        CollectionExpression.CollectionScope.INDEX, leftCollectionExpression, rightCollectionExpression);
//            } else if (leftCollectionExpression.getCollectionScope() == CollectionExpression.CollectionScope.INDEX &&
//                    rightCollectionExpression.getCollectionScope() == CollectionExpression.CollectionScope.INDEX) {
//                List<String> attributes = new ArrayList<String>();
//                attributes.addAll(leftCollectionExpression.getAttributeNames());
//                attributes.addAll(rightCollectionExpression.getAttributeNames());
//                return new AndCollectionExpression(expression, CollectionExpression.CollectionScope.INDEX,
//                        leftCollectionExpression, rightCollectionExpression);
//            } else {
//                return new BasicCollectionExpression(expression, CollectionExpression.CollectionScope.NON);
//            }
//        } else if (expression instanceof Or) {
//            CollectionExpression leftCollectionExpression = buildCollectionExecutor(((Or) expression).getLeftExpression(), matchingMetaStateHolder, eventCollectionMap, variableExpressionExecutors, executionPlanContext);
//            CollectionExpression rightCollectionExpression = buildCollectionExecutor(((Or) expression).getRightExpression(), matchingMetaStateHolder, eventCollectionMap, variableExpressionExecutors, executionPlanContext);
//
//            if (leftCollectionExpression.getCollectionScope() == CollectionExpression.CollectionScope.EXHAUSTIVE ||
//                    rightCollectionExpression.getCollectionScope() == CollectionExpression.CollectionScope.EXHAUSTIVE) {
//                return new BasicCollectionExpression(expression, CollectionExpression.CollectionScope.EXHAUSTIVE);
//            } else if (leftCollectionExpression.getCollectionScope() == CollectionExpression.CollectionScope.INDEX &&
//                    rightCollectionExpression.getCollectionScope() == CollectionExpression.CollectionScope.NON) {
//                return new OrCollectionExpression(expression,
//                        CollectionExpression.CollectionScope.INDEX, leftCollectionExpression, rightCollectionExpression);
//            } else if (leftCollectionExpression.getCollectionScope() == CollectionExpression.CollectionScope.NON &&
//                    rightCollectionExpression.getCollectionScope() == CollectionExpression.CollectionScope.INDEX) {
//                return new OrCollectionExpression(expression,
//                        CollectionExpression.CollectionScope.INDEX, leftCollectionExpression, rightCollectionExpression);
//            } else if (leftCollectionExpression.getCollectionScope() == CollectionExpression.CollectionScope.INDEX &&
//                    rightCollectionExpression.getCollectionScope() == CollectionExpression.CollectionScope.INDEX) {
//                List<String> attributes = new ArrayList<String>();
//                attributes.addAll(leftCollectionExpression.getAttributeNames());
//                attributes.addAll(rightCollectionExpression.getAttributeNames());
//                return new OrCollectionExpression(expression, CollectionExpression.CollectionScope.INDEX,
//                        leftCollectionExpression, rightCollectionExpression);
//            } else {
//                return new BasicCollectionExpression(expression, CollectionExpression.CollectionScope.NON);
//            }
//        } else if (expression instanceof Not) {
//            CollectionExpression notCollectionExpression = buildCollectionExecutor(((Not) expression).getExpression(), matchingMetaStateHolder, eventCollectionMap, variableExpressionExecutors, executionPlanContext);
//
//            if (notCollectionExpression.getCollectionScope() == CollectionExpression.CollectionScope.EXHAUSTIVE) {
//                return new BasicCollectionExpression(expression, CollectionExpression.CollectionScope.EXHAUSTIVE);
//            } else if (notCollectionExpression.getCollectionScope() == CollectionExpression.CollectionScope.INDEX) {
//                return new NotCollectionExpression(expression,
//                        CollectionExpression.CollectionScope.INDEX, notCollectionExpression);
//            } else {
//                return new BasicCollectionExpression(expression, CollectionExpression.CollectionScope.NON);
//            }
//        } else if (expression instanceof Compare) {
////            if (((Compare) expression).getOperator() == Compare.Operator.EQUAL) {
//
//            CollectionExpression leftCollectionExpression = buildCollectionExecutor(((Compare) expression).getLeftExpression(), matchingMetaStateHolder, eventCollectionMap, variableExpressionExecutors, executionPlanContext);
//            CollectionExpression rightCollectionExpression = buildCollectionExecutor(((Compare) expression).getRightExpression(), matchingMetaStateHolder, eventCollectionMap, variableExpressionExecutors, executionPlanContext);
//
//            if (leftCollectionExpression.getCollectionScope() == CollectionExpression.CollectionScope.EXHAUSTIVE ||
//                    rightCollectionExpression.getCollectionScope() == CollectionExpression.CollectionScope.EXHAUSTIVE) {
//                return new BasicCollectionExpression(expression, CollectionExpression.CollectionScope.EXHAUSTIVE);
//            } else if (leftCollectionExpression.getCollectionScope() == CollectionExpression.CollectionScope.INDEX &&
//                    rightCollectionExpression.getCollectionScope() == CollectionExpression.CollectionScope.NON) {
//                return new CompareCollectionExpression(expression, leftCollectionExpression.getAttributeNames(),
//                        CollectionExpression.CollectionScope.INDEX);
//            } else if (leftCollectionExpression.getCollectionScope() == CollectionExpression.CollectionScope.NON &&
//                    rightCollectionExpression.getCollectionScope() == CollectionExpression.CollectionScope.INDEX) {
//                return new CompareCollectionExpression(expression, rightCollectionExpression.getAttributeNames(),
//                        CollectionExpression.CollectionScope.INDEX);
//            } else if (leftCollectionExpression.getCollectionScope() == CollectionExpression.CollectionScope.INDEX &&
//                    rightCollectionExpression.getCollectionScope() == CollectionExpression.CollectionScope.INDEX) {
//                List<String> attributes = new ArrayList<String>();
//                attributes.addAll(leftCollectionExpression.getAttributeNames());
//                attributes.addAll(rightCollectionExpression.getAttributeNames());
//                return new CompareCollectionExpression(expression, attributes, CollectionExpression.CollectionScope.INDEX);
//            } else {
//                return new BasicCollectionExpression(expression, CollectionExpression.CollectionScope.NON);
//            }
////            } else if (((Compare) expression).getOperator() == Compare.Operator.NOT_EQUAL) {
////                return parseNotEqualCompare(
////                        buildCollectionExecutor(((Compare) expression).getLeftExpression(), matchingMetaStateHolder),
////                        buildCollectionExecutor(((Compare) expression).getRightExpression(), matchingMetaStateHolder));
////            } else if (((Compare) expression).getOperator() == Compare.Operator.GREATER_THAN) {
////                return parseGreaterThanCompare(
////                        buildCollectionExecutor(((Compare) expression).getLeftExpression(), matchingMetaStateHolder),
////                        buildCollectionExecutor(((Compare) expression).getRightExpression(), matchingMetaStateHolder));
////            } else if (((Compare) expression).getOperator() == Compare.Operator.GREATER_THAN_EQUAL) {
////                return parseGreaterThanEqualCompare(
////                        buildCollectionExecutor(((Compare) expression).getLeftExpression(), matchingMetaStateHolder),
////                        buildCollectionExecutor(((Compare) expression).getRightExpression(), matchingMetaStateHolder));
////            } else if (((Compare) expression).getOperator() == Compare.Operator.LESS_THAN) {
////                return parseLessThanCompare(
////                        buildCollectionExecutor(((Compare) expression).getLeftExpression(), matchingMetaStateHolder),
////                        buildCollectionExecutor(((Compare) expression).getRightExpression(), matchingMetaStateHolder));
////            } else if (((Compare) expression).getOperator() == Compare.Operator.LESS_THAN_EQUAL) {
////                return parseLessThanEqualCompare(
////                        buildCollectionExecutor(((Compare) expression).getLeftExpression(), matchingMetaStateHolder),
////                        buildCollectionExecutor(((Compare) expression).getRightExpression(), matchingMetaStateHolder));
////            }
//
//        } else if (expression instanceof Constant) {
//            return new BasicCollectionExpression(expression, CollectionExpression.CollectionScope.NON);
////            if (expression instanceof BoolConstant) {
////                return new ConstantExpressionExecutor(((BoolConstant) expression).getValue(), Attribute.Type.BOOL);
////            } else if (expression instanceof StringConstant) {
////                return new ConstantExpressionExecutor(((StringConstant) expression).getValue(), Attribute.Type.STRING);
////            } else if (expression instanceof IntConstant) {
////                return new ConstantExpressionExecutor(((IntConstant) expression).getValue(), Attribute.Type.INT);
////            } else if (expression instanceof LongConstant) {
////                return new ConstantExpressionExecutor(((LongConstant) expression).getValue(), Attribute.Type.LONG);
////            } else if (expression instanceof FloatConstant) {
////                return new ConstantExpressionExecutor(((FloatConstant) expression).getValue(), Attribute.Type.FLOAT);
////            } else if (expression instanceof DoubleConstant) {
////                return new ConstantExpressionExecutor(((DoubleConstant) expression).getValue(), Attribute.Type.DOUBLE);
////            }
//
//        } else if (expression instanceof Variable) {
//            if (isCollectionVariable(matchingMetaStateHolder, (Variable) expression)) {
////                MetaStreamEvent CollectionStreamEvent = matchingMetaStateHolder.getMetaStateEvent().getMetaStreamEvent(matchingMetaStateHolder.getCandidateEventIndex());
//                BasicCollectionExpression variableCollectionExpression = new BasicCollectionExpression(expression, CollectionExpression.CollectionScope.INDEX);
//                List<String> attributes = new ArrayList<String>();
//                attributes.add(((Variable) expression).getAttributeName());
//                variableCollectionExpression.setAttributeNames(attributes);
//            } else {
//                return new BasicCollectionExpression(expression, CollectionExpression.CollectionScope.NON);
//            }
////            return "variable database t/f, stream/Collection name, attribute ";
////            return parseVariable((Variable) expression, matchingMetaStateHolder, currentState, executorList, defaultStreamEventIndex);
//
//        } else if (expression instanceof Multiply) {
//            CollectionExpression left = buildCollectionExecutor(((Multiply) expression).getLeftValue(), matchingMetaStateHolder, eventCollectionMap, variableExpressionExecutors, executionPlanContext);
//            CollectionExpression right = buildCollectionExecutor(((Multiply) expression).getRightValue(), matchingMetaStateHolder, eventCollectionMap, variableExpressionExecutors, executionPlanContext);
//            if (left.getCollectionScope() == CollectionExpression.CollectionScope.NON && right.getCollectionScope() == CollectionExpression.CollectionScope.NON) {
//                return new BasicCollectionExpression(expression, CollectionExpression.CollectionScope.NON);
//            } else {
//                return new BasicCollectionExpression(expression, CollectionExpression.CollectionScope.EXHAUSTIVE);
//            }
//
////            Attribute.Type type = parseArithmeticOperationResultType(left, right);
////            switch (type) {
////                case INT:
////                    return new MultiplyExpressionExecutorInt(left, right);
////                case LONG:
////                    return new MultiplyExpressionExecutorLong(left, right);
////                case FLOAT:
////                    return new MultiplyExpressionExecutorFloat(left, right);
////                case DOUBLE:
////                    return new MultiplyExpressionExecutorDouble(left, right);
////                default: //Will not happen. Handled in parseArithmeticOperationResultType()
////            }
//        } else if (expression instanceof Add) {
//            CollectionExpression left = buildCollectionExecutor(((Add) expression).getLeftValue(), matchingMetaStateHolder, eventCollectionMap, variableExpressionExecutors, executionPlanContext);
//            CollectionExpression right = buildCollectionExecutor(((Add) expression).getRightValue(), matchingMetaStateHolder, eventCollectionMap, variableExpressionExecutors, executionPlanContext);
//            if (left.getCollectionScope() == CollectionExpression.CollectionScope.NON && right.getCollectionScope() == CollectionExpression.CollectionScope.NON) {
//                return new BasicCollectionExpression(expression, CollectionExpression.CollectionScope.NON);
//            } else {
//                return new BasicCollectionExpression(expression, CollectionExpression.CollectionScope.EXHAUSTIVE);
//            }
////            ExpressionExecutor left = buildCollectionExecutor(((Add) expression).getLeftValue(), matchingMetaStateHolder);
////            ExpressionExecutor right = buildCollectionExecutor(((Add) expression).getRightValue(), matchingMetaStateHolder);
////            Attribute.Type type = parseArithmeticOperationResultType(left, right);
////            switch (type) {
////                case INT:
////                    return new AddExpressionExecutorInt(left, right);
////                case LONG:
////                    return new AddExpressionExecutorLong(left, right);
////                case FLOAT:
////                    return new AddExpressionExecutorFloat(left, right);
////                case DOUBLE:
////                    return new AddExpressionExecutorDouble(left, right);
////                default: //Will not happen. Handled in parseArithmeticOperationResultType()
////            }
//        } else if (expression instanceof Subtract) {
//            CollectionExpression left = buildCollectionExecutor(((Subtract) expression).getLeftValue(), matchingMetaStateHolder, eventCollectionMap, variableExpressionExecutors, executionPlanContext);
//            CollectionExpression right = buildCollectionExecutor(((Subtract) expression).getRightValue(), matchingMetaStateHolder, eventCollectionMap, variableExpressionExecutors, executionPlanContext);
//            if (left.getCollectionScope() == CollectionExpression.CollectionScope.NON && right.getCollectionScope() == CollectionExpression.CollectionScope.NON) {
//                return new BasicCollectionExpression(expression, CollectionExpression.CollectionScope.NON);
//            } else {
//                return new BasicCollectionExpression(expression, CollectionExpression.CollectionScope.EXHAUSTIVE);
//            }
////
////            ExpressionExecutor left = buildCollectionExecutor(((Subtract) expression).getLeftValue(), matchingMetaStateHolder);
////            ExpressionExecutor right = buildCollectionExecutor(((Subtract) expression).getRightValue(), matchingMetaStateHolder);
////            Attribute.Type type = parseArithmeticOperationResultType(left, right);
////            switch (type) {
////                case INT:
////                    return new SubtractExpressionExecutorInt(left, right);
////                case LONG:
////                    return new SubtractExpressionExecutorLong(left, right);
////                case FLOAT:
////                    return new SubtractExpressionExecutorFloat(left, right);
////                case DOUBLE:
////                    return new SubtractExpressionExecutorDouble(left, right);
////                default: //Will not happen. Handled in parseArithmeticOperationResultType()
////            }
//        } else if (expression instanceof Mod) {
//            CollectionExpression left = buildCollectionExecutor(((Mod) expression).getLeftValue(), matchingMetaStateHolder, eventCollectionMap, variableExpressionExecutors, executionPlanContext);
//            CollectionExpression right = buildCollectionExecutor(((Mod) expression).getRightValue(), matchingMetaStateHolder, eventCollectionMap, variableExpressionExecutors, executionPlanContext);
//            if (left.getCollectionScope() == CollectionExpression.CollectionScope.NON && right.getCollectionScope() == CollectionExpression.CollectionScope.NON) {
//                return new BasicCollectionExpression(expression, CollectionExpression.CollectionScope.NON);
//            } else {
//                return new BasicCollectionExpression(expression, CollectionExpression.CollectionScope.EXHAUSTIVE);
//            }
////
////            ExpressionExecutor left = buildCollectionExecutor(((Mod) expression).getLeftValue(), matchingMetaStateHolder);
////            ExpressionExecutor right = buildCollectionExecutor(((Mod) expression).getRightValue(), matchingMetaStateHolder);
////            Attribute.Type type = parseArithmeticOperationResultType(left, right);
////            switch (type) {
////                case INT:
////                    return new ModExpressionExecutorInt(left, right);
////                case LONG:
////                    return new ModExpressionExecutorLong(left, right);
////                case FLOAT:
////                    return new ModExpressionExecutorFloat(left, right);
////                case DOUBLE:
////                    return new ModExpressionExecutorDouble(left, right);
////                default: //Will not happen. Handled in parseArithmeticOperationResultType()
////            }
//        } else if (expression instanceof Divide) {
//            CollectionExpression left = buildCollectionExecutor(((Divide) expression).getLeftValue(), matchingMetaStateHolder, eventCollectionMap, variableExpressionExecutors, executionPlanContext);
//            CollectionExpression right = buildCollectionExecutor(((Divide) expression).getRightValue(), matchingMetaStateHolder, eventCollectionMap, variableExpressionExecutors, executionPlanContext);
//            if (left.getCollectionScope() == CollectionExpression.CollectionScope.NON && right.getCollectionScope() == CollectionExpression.CollectionScope.NON) {
//                return new BasicCollectionExpression(expression, CollectionExpression.CollectionScope.NON);
//            } else {
//                return new BasicCollectionExpression(expression, CollectionExpression.CollectionScope.EXHAUSTIVE);
//            }
////
////            ExpressionExecutor expressionExecutor = ExpressionParser.buildCollectionExecutor(expression,
////                    matchingMetaStateHolder.getMetaStateEvent(), matchingMetaStateHolder.getDefaultStreamEventIndex(), eventCollectionMap, variableExpressionExecutors, executionPlanContext, false, 0);
//
//
////            ExpressionExecutor left = buildCollectionExecutor(((Divide) expression).getLeftValue(), matchingMetaStateHolder);
////            ExpressionExecutor right = buildCollectionExecutor(((Divide) expression).getRightValue(), matchingMetaStateHolder);
////            Attribute.Type type = parseArithmeticOperationResultType(left, right);
////            switch (type) {
////                case INT:
////                    return new DivideExpressionExecutorInt(left, right);
////                case LONG:
////                    return new DivideExpressionExecutorLong(left, right);
////                case FLOAT:
////                    return new DivideExpressionExecutorFloat(left, right);
////                case DOUBLE:
////                    return new DivideExpressionExecutorDouble(left, right);
////                default: //Will not happen. Handled in parseArithmeticOperationResultType()
////            }
//
//        } else if (expression instanceof AttributeFunctionExtension) {
//            Expression[] innerExpressions = ((AttributeFunctionExtension) expression).getParameters();
//            for (Expression aExpression : innerExpressions) {
//                CollectionExpression collectionExpression = buildCollectionExecutor(aExpression, matchingMetaStateHolder, eventCollectionMap, variableExpressionExecutors, executionPlanContext);
//                if (collectionExpression.getCollectionScope() != CollectionExpression.CollectionScope.NON) {
//                    return new BasicCollectionExpression(expression, CollectionExpression.CollectionScope.EXHAUSTIVE);
//                }
//            }
//            return new BasicCollectionExpression(expression, CollectionExpression.CollectionScope.NON);
//
////
////            //extensions
////            Object executor;
////            try {
////                executor = SiddhiClassLoader.loadExtensionImplementation((AttributeFunctionExtension) expression, FunctionExecutorExtensionHolder.getInstance(executionPlanContext));
////            } catch (ExecutionPlanCreationException ex) {
////                try {
////                    executor = SiddhiClassLoader.loadExtensionImplementation((AttributeFunctionExtension) expression, AttributeAggregatorExtensionHolder.getInstance(executionPlanContext));
////                    SelectorParser.getContainsAggregatorThreadLocal().set("true");
////                } catch (ExecutionPlanCreationException e) {
////                    throw new ExecutionPlanCreationException("'" + ((AttributeFunctionExtension) expression).getFunction() + "' is neither a function extension nor an aggregated attribute extension");
////                }
////            }
////            if (executor instanceof FunctionExecutor) {
////                FunctionExecutor expressionExecutor = (FunctionExecutor) executor;
////                Expression[] innerExpressions = ((AttributeFunctionExtension) expression).getParameters();
////                ExpressionExecutor[] innerExpressionExecutors = parseInnerExpression(innerExpressions, matchingMetaStateHolder, currentState, eventCollectionMap, executorList,
////                        executionPlanContext, groupBy, defaultStreamEventIndex);
////                expressionExecutor.initExecutor(innerExpressionExecutors, executionPlanContext);
////                if (expressionExecutor.getReturnType() == Attribute.Type.BOOL) {
////                    return new BoolConditionExpressionExecutor(expressionExecutor);
////                }
////                return expressionExecutor;
////            } else {
////                AttributeAggregator attributeAggregator = (AttributeAggregator) executor;
////                Expression[] innerExpressions = ((AttributeFunctionExtension) expression).getParameters();
////                ExpressionExecutor[] innerExpressionExecutors = parseInnerExpression(innerExpressions, matchingMetaStateHolder, currentState, eventCollectionMap, executorList,
////                        executionPlanContext, groupBy, defaultStreamEventIndex);
////                attributeAggregator.initAggregator(innerExpressionExecutors, executionPlanContext);
////                AbstractAggregationAttributeExecutor aggregationAttributeProcessor;
////                if (groupBy) {
////                    aggregationAttributeProcessor = new GroupByAggregationAttributeExecutor(attributeAggregator, innerExpressionExecutors, executionPlanContext);
////                } else {
////                    aggregationAttributeProcessor = new AggregationAttributeExecutor(attributeAggregator, innerExpressionExecutors, executionPlanContext);
////                }
////                SelectorParser.getContainsAggregatorThreadLocal().set("true");
////                return aggregationAttributeProcessor;
////            }
//        } else if (expression instanceof AttributeFunction) {
//            Expression[] innerExpressions = ((AttributeFunction) expression).getParameters();
//            for (Expression aExpression : innerExpressions) {
//                CollectionExpression aCollectionExpression = buildCollectionExecutor(aExpression, matchingMetaStateHolder, eventCollectionMap, variableExpressionExecutors, executionPlanContext);
//                if (aCollectionExpression.getCollectionScope() != CollectionExpression.CollectionScope.NON) {
//                    return new BasicCollectionExpression(expression, CollectionExpression.CollectionScope.EXHAUSTIVE);
//                }
//            }
//            return new BasicCollectionExpression(expression, CollectionExpression.CollectionScope.NON);
//
////
////            Object executor;
////            try {
////                executor = SiddhiClassLoader.loadSiddhiImplementation(((AttributeFunction) expression).getFunction(), AttributeAggregator.class);
////            } catch (ExecutionPlanCreationException ex) {
////                try {
////                    if (executionPlanContext.isFunctionExist(((AttributeFunction) expression).getFunction())) {
////                        executor = new ScriptFunctionExecutor(((AttributeFunction) expression).getFunction());
////                    } else {
////                        try {
////                            executor = SiddhiClassLoader.loadSiddhiImplementation(((AttributeFunction) expression).getFunction(), FunctionExecutor.class);
////                        } catch (ExecutionPlanCreationException e) {
////                            throw new ExecutionPlanCreationException(e.getMessage(), e);
////                        }
////                    }
////                } catch (ExecutionPlanCreationException e) {
////                    throw new ExecutionPlanCreationException(((AttributeFunction) expression).getFunction() + " is neither a function nor an aggregated attribute");
////                }
////            }
////
////            if (executor instanceof AttributeAggregator) {
////                Expression[] innerExpressions = ((AttributeFunction) expression).getParameters();
////                ExpressionExecutor[] innerExpressionExecutors = parseInnerExpression(innerExpressions, matchingMetaStateHolder, currentState, eventCollectionMap, executorList,
////                        executionPlanContext, groupBy, defaultStreamEventIndex);
////                ((AttributeAggregator) executor).initAggregator(innerExpressionExecutors, executionPlanContext);
////                AbstractAggregationAttributeExecutor aggregationAttributeProcessor;
////                if (groupBy) {
////                    aggregationAttributeProcessor = new GroupByAggregationAttributeExecutor((AttributeAggregator) executor, innerExpressionExecutors, executionPlanContext);
////                } else {
////                    aggregationAttributeProcessor = new AggregationAttributeExecutor((AttributeAggregator) executor, innerExpressionExecutors, executionPlanContext);
////                }
////                SelectorParser.getContainsAggregatorThreadLocal().set("true");
////                return aggregationAttributeProcessor;
////            } else {
////                FunctionExecutor functionExecutor = (FunctionExecutor) executor;
////                Expression[] innerExpressions = ((AttributeFunction) expression).getParameters();
////                ExpressionExecutor[] innerExpressionExecutors = parseInnerExpression(innerExpressions, matchingMetaStateHolder, currentState, eventCollectionMap, executorList,
////                        executionPlanContext, groupBy, defaultStreamEventIndex);
////                functionExecutor.initExecutor(innerExpressionExecutors, executionPlanContext);
////                if (functionExecutor.getReturnType() == Attribute.Type.BOOL) {
////                    return new BoolConditionExpressionExecutor(functionExecutor);
////                }
////                return functionExecutor;
////            }
//        } else if (expression instanceof In) {
//            CollectionExpression inCollectionExpression = buildCollectionExecutor(((In) expression).getExpression(), matchingMetaStateHolder, eventCollectionMap, variableExpressionExecutors, executionPlanContext);
//            if (inCollectionExpression.getCollectionScope() != CollectionExpression.CollectionScope.NON) {
//                return new BasicCollectionExpression(expression, CollectionExpression.CollectionScope.EXHAUSTIVE);
//            }
//
//            return new BasicCollectionExpression(expression, CollectionExpression.CollectionScope.NON);
//
////
////            EventCollection eventCollection = eventCollectionMap.get(((In) expression).getSourceId());
////            MatchingMetaStateHolder matchingMetaStateHolder = MatcherParser.constructMatchingMetaStateHolder(matchingMetaStateHolder, defaultStreamEventIndex, eventCollection.getCollectionDefinition());
////            Finder finder = eventCollection.constructFinder(((In) expression).getExpression(), matchingMetaStateHolder, executionPlanContext, executorList, eventCollectionMap);
////            return new InConditionExpressionExecutor(eventCollection, finder, matchingMetaStateHolder.getStreamEventSize(), matchingMetaStateHolder instanceof StateEvent, matchingMetaStateHolder.getDefaultStreamEventIndex());
//
//        } else if (expression instanceof IsNull) {
//
//            CollectionExpression nullCollectionExpression = buildCollectionExecutor(((IsNull) expression).getExpression(), matchingMetaStateHolder, eventCollectionMap, variableExpressionExecutors, executionPlanContext);
//
//            if (nullCollectionExpression.getCollectionScope() == CollectionExpression.CollectionScope.EXHAUSTIVE) {
//                return new BasicCollectionExpression(expression, CollectionExpression.CollectionScope.EXHAUSTIVE);
//            } else if (nullCollectionExpression.getCollectionScope() == CollectionExpression.CollectionScope.INDEX) {
//                return new NullCollectionExpression(expression, nullCollectionExpression.getAttributeNames(),
//                        CollectionExpression.CollectionScope.INDEX);
//            } else {
//                return new BasicCollectionExpression(expression, CollectionExpression.CollectionScope.NON);
//            }
//        }
//        throw new UnsupportedOperationException(expression.toString() + " not supported!");
//    }

    private static AbstractDefinition getCollectionDefinition(MatchingMetaStateHolder matchingMetaStateHolder, Variable variable) {
        if (variable.getStreamId() != null) {
            MetaStreamEvent CollectionStreamEvent = matchingMetaStateHolder.getMetaStateEvent().getMetaStreamEvent(matchingMetaStateHolder.getCandidateEventIndex());
            if (CollectionStreamEvent != null) {
                if ((CollectionStreamEvent.getInputReferenceId() != null && variable.getStreamId().equals(CollectionStreamEvent.getInputReferenceId())) ||
                        (CollectionStreamEvent.getLastInputDefinition().getId().equals(variable.getStreamId()))) {
                    return CollectionStreamEvent.getLastInputDefinition();
                }
            }
        }
        return null;
    }

}
