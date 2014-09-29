/*
 *
 *  * Copyright (c) 2005 - 2014, WSO2 Inc. (http://www.wso2.org)
 *  * All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.siddhi.core.util.parser;

import org.wso2.siddhi.core.config.SiddhiContext;
import org.wso2.siddhi.core.event.ComplexMetaEvent;
import org.wso2.siddhi.core.event.stream.MetaStreamEvent;
import org.wso2.siddhi.core.exception.OperationNotSupportedException;
import org.wso2.siddhi.core.exception.QueryCreationException;
import org.wso2.siddhi.core.executor.ConstantExpressionExecutor;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.executor.VariableExpressionExecutor;
import org.wso2.siddhi.core.executor.condition.AndConditionExpressionExecutor;
import org.wso2.siddhi.core.executor.condition.ConditionExpressionExecutor;
import org.wso2.siddhi.core.executor.condition.NotConditionExpressionExecutor;
import org.wso2.siddhi.core.executor.condition.OrConditionExpressionExecutor;
import org.wso2.siddhi.core.executor.condition.compare.contains.ContainsCompareConditionExpressionExecutor;
import org.wso2.siddhi.core.executor.condition.compare.equal.*;
import org.wso2.siddhi.core.executor.condition.compare.greater_than.*;
import org.wso2.siddhi.core.executor.condition.compare.greater_than_equal.*;
import org.wso2.siddhi.core.executor.condition.compare.less_than.*;
import org.wso2.siddhi.core.executor.condition.compare.less_than_equal.*;
import org.wso2.siddhi.core.executor.condition.compare.not_equal.*;
import org.wso2.siddhi.core.executor.function.FunctionExecutor;
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
import org.wso2.siddhi.core.extension.holder.ExecutorExtensionHolder;
import org.wso2.siddhi.core.extension.holder.OutputAttributeExtensionHolder;
import org.wso2.siddhi.core.query.selector.attribute.handler.AttributeAggregator;
import org.wso2.siddhi.core.query.selector.attribute.processor.executor.AbstractAggregationAttributeExecutor;
import org.wso2.siddhi.core.query.selector.attribute.processor.executor.AggregationAttributeExecutor;
import org.wso2.siddhi.core.query.selector.attribute.processor.executor.GroupByAggregationAttributeExecutor;
import org.wso2.siddhi.core.util.SiddhiClassLoader;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.expression.Variable;
import org.wso2.siddhi.query.api.expression.condition.And;
import org.wso2.siddhi.query.api.expression.condition.Compare;
import org.wso2.siddhi.query.api.expression.condition.Not;
import org.wso2.siddhi.query.api.expression.condition.Or;
import org.wso2.siddhi.query.api.expression.constant.*;
import org.wso2.siddhi.query.api.expression.function.AttributeFunction;
import org.wso2.siddhi.query.api.expression.function.AttributeFunctionExtension;
import org.wso2.siddhi.query.api.expression.math.*;

import java.util.LinkedList;
import java.util.List;

/**
 * Class to parse Expressions and create Expression executors.
 */
public class ExpressionParser {

    /**
     * Parse the given expression and create the appropriate Executor by recursively traversing the expression
     *
     * @param expression    Expression to be parsed
     * @param siddhiContext Associated Siddhi Context to be used when parsing Function Expressions
     * @param metaEvent
     * @param executorList  List to hold VariableExpressionExecutors to update after query parsing  @return
     * @
     */
    public static ExpressionExecutor parseExpression(Expression expression, SiddhiContext siddhiContext,
                                                     ComplexMetaEvent metaEvent,
                                                     List<VariableExpressionExecutor> executorList, boolean groupBy) {
        if (expression instanceof And) {
            return new AndConditionExpressionExecutor(
                    parseExpression(((And) expression).getLeftExpression(), siddhiContext, metaEvent, executorList, groupBy),
                    parseExpression(((And) expression).getRightExpression(), siddhiContext, metaEvent, executorList, groupBy));
        } else if (expression instanceof Or) {
            return new OrConditionExpressionExecutor(
                    parseExpression(((Or) expression).getLeftExpression(), siddhiContext, metaEvent, executorList, groupBy),
                    parseExpression(((Or) expression).getRightExpression(), siddhiContext, metaEvent, executorList, groupBy));
        } else if (expression instanceof Not) {
            return new NotConditionExpressionExecutor(parseExpression(((Not) expression).getExpression(), siddhiContext, metaEvent, executorList, groupBy));
        } else if (expression instanceof Compare) {
            if (((Compare) expression).getOperator() == Compare.Operator.EQUAL) {
                return parseEqualCompare(
                        parseExpression(((Compare) expression).getLeftExpression(), siddhiContext, metaEvent, executorList, groupBy),
                        parseExpression(((Compare) expression).getRightExpression(), siddhiContext, metaEvent, executorList, groupBy));
            } else if (((Compare) expression).getOperator() == Compare.Operator.NOT_EQUAL) {
                return parseNotEqualCompare(
                        parseExpression(((Compare) expression).getLeftExpression(), siddhiContext, metaEvent, executorList, groupBy),
                        parseExpression(((Compare) expression).getRightExpression(), siddhiContext, metaEvent, executorList, groupBy));
            } else if (((Compare) expression).getOperator() == Compare.Operator.GREATER_THAN) {
                return parseGreaterThanCompare(
                        parseExpression(((Compare) expression).getLeftExpression(), siddhiContext, metaEvent, executorList, groupBy),
                        parseExpression(((Compare) expression).getRightExpression(), siddhiContext, metaEvent, executorList, groupBy));
            } else if (((Compare) expression).getOperator() == Compare.Operator.GREATER_THAN_EQUAL) {
                return parseGreaterThanEqualCompare(
                        parseExpression(((Compare) expression).getLeftExpression(), siddhiContext, metaEvent, executorList, groupBy),
                        parseExpression(((Compare) expression).getRightExpression(), siddhiContext, metaEvent, executorList, groupBy));
            } else if (((Compare) expression).getOperator() == Compare.Operator.LESS_THAN) {
                return parseLessThanCompare(
                        parseExpression(((Compare) expression).getLeftExpression(), siddhiContext, metaEvent, executorList, groupBy),
                        parseExpression(((Compare) expression).getRightExpression(), siddhiContext, metaEvent, executorList, groupBy));
            } else if (((Compare) expression).getOperator() == Compare.Operator.LESS_THAN_EQUAL) {
                return parseLessThanEqualCompare(
                        parseExpression(((Compare) expression).getLeftExpression(), siddhiContext, metaEvent, executorList, groupBy),
                        parseExpression(((Compare) expression).getRightExpression(), siddhiContext, metaEvent, executorList, groupBy));
            } else if (((Compare) expression).getOperator() == Compare.Operator.CONTAINS) {
                return parseContainsCompare(
                        parseExpression(((Compare) expression).getLeftExpression(), siddhiContext, metaEvent, executorList, groupBy),
                        parseExpression(((Compare) expression).getRightExpression(), siddhiContext, metaEvent, executorList, groupBy));
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
            return parseVariable((Variable) expression, metaEvent, executorList);

        } else if (expression instanceof Multiply) {
            ExpressionExecutor left = parseExpression(((Multiply) expression).getLeftValue(), siddhiContext, metaEvent, executorList, groupBy);
            ExpressionExecutor right = parseExpression(((Multiply) expression).getRightValue(), siddhiContext, metaEvent, executorList, groupBy);
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
            ExpressionExecutor left = parseExpression(((Add) expression).getLeftValue(), siddhiContext, metaEvent, executorList, groupBy);
            ExpressionExecutor right = parseExpression(((Add) expression).getRightValue(), siddhiContext, metaEvent, executorList, groupBy);
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
            ExpressionExecutor left = parseExpression(((Subtract) expression).getLeftValue(), siddhiContext, metaEvent, executorList, groupBy);
            ExpressionExecutor right = parseExpression(((Subtract) expression).getRightValue(), siddhiContext, metaEvent, executorList, groupBy);
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
            ExpressionExecutor left = parseExpression(((Mod) expression).getLeftValue(), siddhiContext, metaEvent, executorList, groupBy);
            ExpressionExecutor right = parseExpression(((Mod) expression).getRightValue(), siddhiContext, metaEvent, executorList, groupBy);
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
            ExpressionExecutor left = parseExpression(((Divide) expression).getLeftValue(), siddhiContext, metaEvent, executorList, groupBy);
            ExpressionExecutor right = parseExpression(((Divide) expression).getRightValue(), siddhiContext, metaEvent, executorList, groupBy);
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
                executor = SiddhiClassLoader.loadExtensionImplementation((AttributeFunctionExtension) expression, ExecutorExtensionHolder.getInstance(siddhiContext));
            } catch (QueryCreationException ex) {
                try {
                    executor = SiddhiClassLoader.loadExtensionImplementation((AttributeFunctionExtension) expression, OutputAttributeExtensionHolder.getInstance(siddhiContext));
                } catch (QueryCreationException e) {
                    throw new QueryCreationException(((AttributeFunctionExtension) expression).getFunction() + " is neither a function extension nor an aggregated attribute extension");
                }
            }
            if(executor instanceof  FunctionExecutor) {
                FunctionExecutor expressionExecutor = (FunctionExecutor) executor;
                List<ExpressionExecutor> innerExpressionExecutors = new LinkedList<ExpressionExecutor>();
                for (Expression innerExpression : ((AttributeFunctionExtension) expression).getParameters()) {
                    innerExpressionExecutors.add(parseExpression(innerExpression, siddhiContext, metaEvent, executorList, groupBy));
                }
                siddhiContext.addEternalReferencedHolder(expressionExecutor);
                expressionExecutor.setSiddhiContext(siddhiContext);
                expressionExecutor.setAttributeExpressionExecutors(innerExpressionExecutors);
                expressionExecutor.init();
                return expressionExecutor;
            } else {
                AttributeAggregator attributeAggregator = (AttributeAggregator) executor;
                if (((AttributeFunction) expression).getParameters().length > 1) {
                    throw new QueryCreationException(((AttributeFunction) expression).getFunction() + " can only have one parameter");
                }
                List<ExpressionExecutor> innerExpressionExecutors = new LinkedList<ExpressionExecutor>();
                for (Expression innerExpression : ((AttributeFunctionExtension) expression).getParameters()) {
                    innerExpressionExecutors.add(parseExpression(innerExpression, siddhiContext, metaEvent, executorList, groupBy));
                }
                (attributeAggregator).init(innerExpressionExecutors.get(0).getReturnType());
                AbstractAggregationAttributeExecutor aggregationAttributeProcessor;
                if (groupBy) {
                    aggregationAttributeProcessor = new GroupByAggregationAttributeExecutor(attributeAggregator, innerExpressionExecutors, siddhiContext);
                } else {
                    aggregationAttributeProcessor = new AggregationAttributeExecutor(attributeAggregator, innerExpressionExecutors, siddhiContext);
                }
                return aggregationAttributeProcessor;
            }
        } else if (expression instanceof AttributeFunction) {
            Object executor;
            try {
                executor = SiddhiClassLoader.loadSiddhiImplementation(((AttributeFunction) expression).getFunction(), AttributeAggregator.class);
            } catch (QueryCreationException ex) {
                try {
                    executor = SiddhiClassLoader.loadSiddhiImplementation(((AttributeFunction) expression).getFunction(), FunctionExecutor.class);
                } catch (QueryCreationException e) {
                    throw new QueryCreationException(((AttributeFunction) expression).getFunction() + " is neither a function nor an aggregated attribute");
                }
            }
            if (executor instanceof AttributeAggregator) {
                if (((AttributeFunction) expression).getParameters().length > 1) {
                    throw new QueryCreationException(((AttributeFunction) expression).getFunction() + " can only have one parameter");
                }
                List<ExpressionExecutor> innerExpressionExecutors = new LinkedList<ExpressionExecutor>();
                innerExpressionExecutors.add(parseExpression(((AttributeFunction) expression).getParameters()[0], siddhiContext, metaEvent, executorList, groupBy));
                ((AttributeAggregator) executor).init(innerExpressionExecutors.get(0).getReturnType());
                AbstractAggregationAttributeExecutor aggregationAttributeProcessor;
                if (groupBy) {
                    aggregationAttributeProcessor = new GroupByAggregationAttributeExecutor((AttributeAggregator) executor, innerExpressionExecutors, siddhiContext);
                } else {
                    aggregationAttributeProcessor = new AggregationAttributeExecutor((AttributeAggregator) executor, innerExpressionExecutors, siddhiContext);
                }
                return aggregationAttributeProcessor;
            } else {
                FunctionExecutor functionExecutor = (FunctionExecutor) executor;
                List<ExpressionExecutor> innerExpressionExecutors = new LinkedList<ExpressionExecutor>();
                for (Expression innerExpression : ((AttributeFunction) expression).getParameters()) {
                    innerExpressionExecutors.add(parseExpression(innerExpression, siddhiContext, metaEvent, executorList, groupBy));
                }
                siddhiContext.addEternalReferencedHolder(functionExecutor);
                functionExecutor.setSiddhiContext(siddhiContext);
                functionExecutor.setAttributeExpressionExecutors(innerExpressionExecutors);
                functionExecutor.init();
                return functionExecutor;

            }
        }

        //TODO else if parts
        throw new UnsupportedOperationException(expression.toString() + " not supported!");

    }

    /**
     * Create greater than Compare Condition Expression Executor which evaluates whether value of leftExpressionExecutor
     * is greater than value of rightExpressionExecutor.
     *
     * @param leftExpressionExecutor
     * @param rightExpressionExecutor
     * @return
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
     * @param leftExpressionExecutor
     * @param rightExpressionExecutor
     * @return
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
     * @param leftExpressionExecutor
     * @param rightExpressionExecutor
     * @return
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
     * @param leftExpressionExecutor
     * @param rightExpressionExecutor
     * @return
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
     * @param leftExpressionExecutor
     * @param rightExpressionExecutor
     * @return
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
                        return new EqualCompareConditionExecutorIntFloat(leftExpressionExecutor, rightExpressionExecutor);
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
                        return new EqualCompareConditionExecutorDoubleFloat(leftExpressionExecutor, rightExpressionExecutor);
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
     * @param leftExpressionExecutor
     * @param rightExpressionExecutor
     * @return
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
     * Create contains Compare Condition Expression Executor.
     *
     * @param leftExpressionExecutor
     * @param rightExpressionExecutor
     * @return
     */
    private static ConditionExpressionExecutor parseContainsCompare(
            ExpressionExecutor leftExpressionExecutor, ExpressionExecutor rightExpressionExecutor) {
        switch (leftExpressionExecutor.getReturnType()) {
            case STRING:
                switch (rightExpressionExecutor.getReturnType()) {
                    case STRING:
                        return new ContainsCompareConditionExpressionExecutor(leftExpressionExecutor, rightExpressionExecutor);
                    default:
                        throw new OperationNotSupportedException(rightExpressionExecutor.getReturnType() + " cannot be used in contains comparisons");
                }
            default:
                throw new OperationNotSupportedException(leftExpressionExecutor.getReturnType() + " cannot be used in contains comparisons");
        }
    }


    /**
     * Parse and validate the given Siddhi variable and return a VariableExpressionExecutor
     *
     * @param variable     Variable to be parsed
     * @param metaEvent    Meta event used to collect execution info of stream associated with query
     * @param executorList List to hold VariableExpressionExecutors to update after query parsing
     * @return VariableExpressionExecutor representing given variable
     */
    private static ExpressionExecutor parseVariable(Variable variable,
                                                    ComplexMetaEvent metaEvent, List<VariableExpressionExecutor> executorList) {
        String attributeName = variable.getAttributeName();

        if (metaEvent instanceof MetaStreamEvent) {
            MetaStreamEvent metaStreamEvent = (MetaStreamEvent) metaEvent;
            metaEvent.addData(new Attribute(attributeName, metaStreamEvent.getInputDefinition().getAttributeType(attributeName)));
            VariableExpressionExecutor variableExpressionExecutor = new VariableExpressionExecutor(attributeName, (StreamDefinition) (metaStreamEvent.getInputDefinition()));
            executorList.add(variableExpressionExecutor);
            return variableExpressionExecutor;
        } else {
            //TODO handle meta state events
            throw new OperationNotSupportedException("MetaStateEvents are not supported at the moment");
        }
    }

    /**
     * Calculate the return type of arithmetic operation executors.(Ex: add, subtract, etc)
     *
     * @param leftExpressionExecutor
     * @param rightExpressionExecutor
     * @return
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


}
