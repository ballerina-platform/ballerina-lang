/*
*  Copyright (c) 2005-2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
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
import org.wso2.siddhi.core.exception.OperationNotSupportedException;
import org.wso2.siddhi.core.executor.conditon.AndConditionExecutor;
import org.wso2.siddhi.core.executor.conditon.BooleanConditionExecutor;
import org.wso2.siddhi.core.executor.conditon.ConditionExecutor;
import org.wso2.siddhi.core.executor.conditon.InConditionExecutor;
import org.wso2.siddhi.core.executor.conditon.NotConditionExecutor;
import org.wso2.siddhi.core.executor.conditon.OrConditionExecutor;
import org.wso2.siddhi.core.executor.expression.ConstantExpressionExecutor;
import org.wso2.siddhi.core.executor.expression.ExpressionExecutor;
import org.wso2.siddhi.core.executor.expression.TypeExpressionExecutor;
import org.wso2.siddhi.core.executor.expression.VariableExpressionExecutor;
import org.wso2.siddhi.core.executor.expression.add.AddExpressionExecutorDouble;
import org.wso2.siddhi.core.executor.expression.add.AddExpressionExecutorFloat;
import org.wso2.siddhi.core.executor.expression.add.AddExpressionExecutorInt;
import org.wso2.siddhi.core.executor.expression.add.AddExpressionExecutorLong;
import org.wso2.siddhi.core.executor.expression.divide.DivideExpressionExecutorDouble;
import org.wso2.siddhi.core.executor.expression.divide.DivideExpressionExecutorFloat;
import org.wso2.siddhi.core.executor.expression.divide.DivideExpressionExecutorInt;
import org.wso2.siddhi.core.executor.expression.divide.DivideExpressionExecutorLong;
import org.wso2.siddhi.core.executor.function.FunctionExecutor;
import org.wso2.siddhi.core.executor.expression.minus.MinusExpressionExecutorDouble;
import org.wso2.siddhi.core.executor.expression.minus.MinusExpressionExecutorFloat;
import org.wso2.siddhi.core.executor.expression.minus.MinusExpressionExecutorInt;
import org.wso2.siddhi.core.executor.expression.minus.MinusExpressionExecutorLong;
import org.wso2.siddhi.core.executor.expression.mod.ModExpressionExecutorDouble;
import org.wso2.siddhi.core.executor.expression.mod.ModExpressionExecutorFloat;
import org.wso2.siddhi.core.executor.expression.mod.ModExpressionExecutorInt;
import org.wso2.siddhi.core.executor.expression.mod.ModExpressionExecutorLong;
import org.wso2.siddhi.core.executor.expression.multiply.MultiplyExpressionExecutorDouble;
import org.wso2.siddhi.core.executor.expression.multiply.MultiplyExpressionExecutorFloat;
import org.wso2.siddhi.core.executor.expression.multiply.MultiplyExpressionExecutorInt;
import org.wso2.siddhi.core.executor.expression.multiply.MultiplyExpressionExecutorLong;
import org.wso2.siddhi.core.extension.holder.ExecutorExtensionHolder;
import org.wso2.siddhi.core.table.EventTable;
import org.wso2.siddhi.core.util.SiddhiClassLoader;
import org.wso2.siddhi.query.api.condition.AndCondition;
import org.wso2.siddhi.query.api.condition.BooleanCondition;
import org.wso2.siddhi.query.api.condition.Compare;
import org.wso2.siddhi.query.api.condition.Condition;
import org.wso2.siddhi.query.api.condition.ConditionExtension;
import org.wso2.siddhi.query.api.condition.FunctionCondition;
import org.wso2.siddhi.query.api.condition.InCondition;
import org.wso2.siddhi.query.api.condition.NotCondition;
import org.wso2.siddhi.query.api.condition.OrCondition;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.expression.Add;
import org.wso2.siddhi.query.api.expression.Divide;
import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.expression.ExpressionExtension;
import org.wso2.siddhi.query.api.expression.FunctionExpression;
import org.wso2.siddhi.query.api.expression.Minus;
import org.wso2.siddhi.query.api.expression.Mod;
import org.wso2.siddhi.query.api.expression.Multiply;
import org.wso2.siddhi.query.api.expression.Type;
import org.wso2.siddhi.query.api.expression.Variable;
import org.wso2.siddhi.query.api.expression.constant.BoolConstant;
import org.wso2.siddhi.query.api.expression.constant.Constant;
import org.wso2.siddhi.query.api.expression.constant.DoubleConstant;
import org.wso2.siddhi.query.api.expression.constant.FloatConstant;
import org.wso2.siddhi.query.api.expression.constant.IntConstant;
import org.wso2.siddhi.query.api.expression.constant.LongConstant;
import org.wso2.siddhi.query.api.expression.constant.StringConstant;
import org.wso2.siddhi.query.api.query.QueryEventSource;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ExecutorParser {


    public static ConditionExecutor parseCondition(Condition condition,
                                                   List<QueryEventSource> queryEventSourceList,
                                                   String currentStreamReference,
                                                   Map<String, EventTable> eventTableMap, boolean processInDefinition,
                                                   SiddhiContext siddhiContext) {
        if (condition instanceof AndCondition) {
            return new AndConditionExecutor(parseCondition(((AndCondition) condition).getLeftCondition(), queryEventSourceList, currentStreamReference, eventTableMap, processInDefinition, siddhiContext),
                                            parseCondition(((AndCondition) condition).getRightCondition(), queryEventSourceList, currentStreamReference, eventTableMap, processInDefinition, siddhiContext));
        } else if (condition instanceof OrCondition) {
            return new OrConditionExecutor(parseCondition(((OrCondition) condition).getLeftCondition(), queryEventSourceList, currentStreamReference, eventTableMap, processInDefinition, siddhiContext),
                                           parseCondition(((OrCondition) condition).getRightCondition(), queryEventSourceList, currentStreamReference, eventTableMap, processInDefinition, siddhiContext));
        } else if (condition instanceof InCondition) {
            EventTable eventTable = eventTableMap.get(((InCondition) condition).getSourceId());
            queryEventSourceList.add(new QueryEventSource(((InCondition) condition).getSourceId(), ((InCondition) condition).getSourceId(), eventTable.getTableDefinition(), null, null, null));
            ConditionExecutor conditionExecutor = parseCondition(((InCondition) condition).getCondition(), queryEventSourceList, currentStreamReference, eventTableMap, processInDefinition, siddhiContext);
            queryEventSourceList.remove(queryEventSourceList.size() - 1);
            return new InConditionExecutor(eventTable, conditionExecutor);
        } else if (condition instanceof NotCondition) {
            return new NotConditionExecutor(parseCondition(((NotCondition) condition).getCondition(), queryEventSourceList, currentStreamReference, eventTableMap, processInDefinition, siddhiContext));
        } else if (condition instanceof BooleanCondition) {
            return new BooleanConditionExecutor(parseExpression(((BooleanCondition) condition).getExpression(), queryEventSourceList, currentStreamReference, processInDefinition, siddhiContext));
        } else if (condition instanceof Compare) {
            if (((Compare) condition).getOperator() == Condition.Operator.EQUAL) {
                return ComparatorParser.parseEqualCompare(parseExpression(((Compare) condition).getLeftExpression(), queryEventSourceList, currentStreamReference, processInDefinition, siddhiContext),
                                                          parseExpression(((Compare) condition).getRightExpression(), queryEventSourceList, currentStreamReference, processInDefinition, siddhiContext));
            } else if (((Compare) condition).getOperator() == Condition.Operator.NOT_EQUAL) {
                return ComparatorParser.parseNotEqualCompare(parseExpression(((Compare) condition).getLeftExpression(), queryEventSourceList, currentStreamReference, processInDefinition, siddhiContext),
                                                             parseExpression(((Compare) condition).getRightExpression(), queryEventSourceList, currentStreamReference, processInDefinition, siddhiContext));
            } else if (((Compare) condition).getOperator() == Condition.Operator.GREATER_THAN) {
                return ComparatorParser.parseGreaterThanCompare(parseExpression(((Compare) condition).getLeftExpression(), queryEventSourceList, currentStreamReference, processInDefinition, siddhiContext),
                                                                parseExpression(((Compare) condition).getRightExpression(), queryEventSourceList, currentStreamReference, processInDefinition, siddhiContext));
            } else if (((Compare) condition).getOperator() == Condition.Operator.GREATER_THAN_EQUAL) {
                return ComparatorParser.parseGreaterThanEqualCompare(parseExpression(((Compare) condition).getLeftExpression(), queryEventSourceList, currentStreamReference, processInDefinition, siddhiContext),
                                                                     parseExpression(((Compare) condition).getRightExpression(), queryEventSourceList, currentStreamReference, processInDefinition, siddhiContext));
            } else if (((Compare) condition).getOperator() == Condition.Operator.LESS_THAN) {
                return ComparatorParser.parseLessThanCompare(parseExpression(((Compare) condition).getLeftExpression(), queryEventSourceList, currentStreamReference, processInDefinition, siddhiContext),
                                                             parseExpression(((Compare) condition).getRightExpression(), queryEventSourceList, currentStreamReference, processInDefinition, siddhiContext));
            } else if (((Compare) condition).getOperator() == Condition.Operator.LESS_THAN_EQUAL) {
                return ComparatorParser.parseLessThanEqualCompare(parseExpression(((Compare) condition).getLeftExpression(), queryEventSourceList, currentStreamReference, processInDefinition, siddhiContext),
                                                                  parseExpression(((Compare) condition).getRightExpression(), queryEventSourceList, currentStreamReference, processInDefinition, siddhiContext));

            } else if (((Compare) condition).getOperator() == Condition.Operator.CONTAINS) {
                return ComparatorParser.parseContainsCompare(parseExpression(((Compare) condition).getLeftExpression(), queryEventSourceList, currentStreamReference, processInDefinition, siddhiContext),
                                                             parseExpression(((Compare) condition).getRightExpression(), queryEventSourceList, currentStreamReference, processInDefinition, siddhiContext));
            } else if (((Compare) condition).getOperator() == Condition.Operator.INSTANCE_OF) {
                return ComparatorParser.parseInstanceOfCompare(parseExpression(((Compare) condition).getLeftExpression(), queryEventSourceList, currentStreamReference, processInDefinition, siddhiContext),
                                                               parseExpression(((Compare) condition).getRightExpression(), queryEventSourceList, currentStreamReference, processInDefinition, siddhiContext));
            }
        } else if (condition instanceof ConditionExtension) {
            List<ExpressionExecutor> expressionExecutors = new LinkedList<ExpressionExecutor>();
            for (Expression expression : ((ConditionExtension) condition).getParameters()) {
                expressionExecutors.add(parseExpression(expression, queryEventSourceList, currentStreamReference, processInDefinition, siddhiContext));
            }
            FunctionExecutor expressionExecutor = (FunctionExecutor) SiddhiClassLoader.loadExtensionImplementation(((ExpressionExtension) Expression.extension(((ConditionExtension) condition).getNamespace(), ((ConditionExtension) condition).getFunction(), ((ConditionExtension) condition).getParameters())), ExecutorExtensionHolder.getInstance(siddhiContext));
            siddhiContext.addEternalReferencedHolder(expressionExecutor);
            expressionExecutor.setSiddhiContext(siddhiContext);
            expressionExecutor.setAttributeExpressionExecutors(expressionExecutors);
            expressionExecutor.init();
            if(expressionExecutor.getReturnType()!= Attribute.Type.BOOL){
                throw new OperationNotSupportedException(((FunctionCondition) condition).getFunction()+" of class "+expressionExecutor.getClass().getName()+" does not return bool hence it cannot be used as a condition");
            }
            return new BooleanConditionExecutor(expressionExecutor);
        } else if (condition instanceof FunctionCondition) {
            List<ExpressionExecutor> expressionExecutors = new LinkedList<ExpressionExecutor>();
            for (Expression expression : ((FunctionCondition) condition).getParameters()) {
                expressionExecutors.add(parseExpression(expression, queryEventSourceList, currentStreamReference, processInDefinition, siddhiContext));
            }
            FunctionExecutor expressionExecutor= (FunctionExecutor) SiddhiClassLoader.loadSiddhiImplementation(((FunctionExpression) Expression.function(((FunctionCondition) condition).getFunction(),((FunctionCondition) condition).getParameters())).getFunction(), FunctionExecutor.class);
            siddhiContext.addEternalReferencedHolder(expressionExecutor);
            expressionExecutor.setSiddhiContext(siddhiContext);
            expressionExecutor.setAttributeExpressionExecutors(expressionExecutors);
            expressionExecutor.init();
            if(expressionExecutor.getReturnType()!= Attribute.Type.BOOL){
                throw new OperationNotSupportedException(((FunctionCondition) condition).getFunction()+" of class "+expressionExecutor.getClass().getName()+" does not return bool hence it cannot be used as a condition");
            }
            return new BooleanConditionExecutor(expressionExecutor);
        }
        throw new OperationNotSupportedException(condition + " not supported !");
    }

    public static ExpressionExecutor parseExpression(Expression expression,
                                                     List<QueryEventSource> queryEventSourceList,
                                                     String currentStreamReference,
                                                     boolean processInStreamDefinition,
                                                     SiddhiContext siddhiContext) {
        if (expression instanceof Constant) {
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
            return new VariableExpressionExecutor(((Variable) expression).getStreamId(), ((Variable) expression).getAttributeName(), ((Variable) expression).getPosition(), queryEventSourceList, currentStreamReference, processInStreamDefinition);

        } else if (expression instanceof Multiply) {
            ExpressionExecutor left = parseExpression(((Multiply) expression).getLeftValue(), queryEventSourceList, currentStreamReference, processInStreamDefinition, siddhiContext);
            ExpressionExecutor right = parseExpression(((Multiply) expression).getRightValue(), queryEventSourceList, currentStreamReference, processInStreamDefinition, siddhiContext);
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
            }
        } else if (expression instanceof Add) {
            ExpressionExecutor left = parseExpression(((Add) expression).getLeftValue(), queryEventSourceList, currentStreamReference, processInStreamDefinition, siddhiContext);
            ExpressionExecutor right = parseExpression(((Add) expression).getRightValue(), queryEventSourceList, currentStreamReference, processInStreamDefinition, siddhiContext);
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
            }
        } else if (expression instanceof Minus) {
            ExpressionExecutor left = parseExpression(((Minus) expression).getLeftValue(), queryEventSourceList, currentStreamReference, processInStreamDefinition, siddhiContext);
            ExpressionExecutor right = parseExpression(((Minus) expression).getRightValue(), queryEventSourceList, currentStreamReference, processInStreamDefinition, siddhiContext);
            Attribute.Type type = parseArithmeticOperationResultType(left, right);
            switch (type) {
                case INT:
                    return new MinusExpressionExecutorInt(left, right);
                case LONG:
                    return new MinusExpressionExecutorLong(left, right);
                case FLOAT:
                    return new MinusExpressionExecutorFloat(left, right);
                case DOUBLE:
                    return new MinusExpressionExecutorDouble(left, right);
            }
        } else if (expression instanceof Mod) {
            ExpressionExecutor left = parseExpression(((Mod) expression).getLeftValue(), queryEventSourceList, currentStreamReference, processInStreamDefinition, siddhiContext);
            ExpressionExecutor right = parseExpression(((Mod) expression).getRightValue(), queryEventSourceList, currentStreamReference, processInStreamDefinition, siddhiContext);
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
            }
        } else if (expression instanceof Divide) {
            ExpressionExecutor left = parseExpression(((Divide) expression).getLeftValue(), queryEventSourceList, currentStreamReference, processInStreamDefinition, siddhiContext);
            ExpressionExecutor right = parseExpression(((Divide) expression).getRightValue(), queryEventSourceList, currentStreamReference, processInStreamDefinition, siddhiContext);
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
            }
        } else if (expression instanceof Type) {
            return new TypeExpressionExecutor(((Type) expression).getType());
        } else if (expression instanceof ExpressionExtension) {
            List<ExpressionExecutor> innerExpressionExecutors = new LinkedList<ExpressionExecutor>();
            for (Expression innerExpression : ((ExpressionExtension) expression).getParameters()) {
                innerExpressionExecutors.add(parseExpression(innerExpression, queryEventSourceList, currentStreamReference, processInStreamDefinition, siddhiContext));
            }
            FunctionExecutor expressionExecutor = (FunctionExecutor) SiddhiClassLoader.loadExtensionImplementation((ExpressionExtension) expression, ExecutorExtensionHolder.getInstance(siddhiContext));
            siddhiContext.addEternalReferencedHolder(expressionExecutor);
            expressionExecutor.setSiddhiContext(siddhiContext);
            expressionExecutor.setAttributeExpressionExecutors(innerExpressionExecutors);
            expressionExecutor.init();
            return expressionExecutor;
        } else if (expression instanceof FunctionExpression) {
            List<ExpressionExecutor> innerExpressionExecutors = new LinkedList<ExpressionExecutor>();
            for (Expression innerExpression : ((FunctionExpression) expression).getParameters()) {
                innerExpressionExecutors.add(parseExpression(innerExpression, queryEventSourceList, currentStreamReference, processInStreamDefinition, siddhiContext));
            }
            FunctionExecutor expressionExecutor= (FunctionExecutor) SiddhiClassLoader.loadSiddhiImplementation(((FunctionExpression) expression).getFunction(), FunctionExecutor.class);
            siddhiContext.addEternalReferencedHolder(expressionExecutor);
            expressionExecutor.setSiddhiContext(siddhiContext);
            expressionExecutor.setAttributeExpressionExecutors(innerExpressionExecutors);
            expressionExecutor.init();
            return expressionExecutor;
        }
        throw new UnsupportedOperationException(expression.toString() + " not supported!");

    }

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
            throw new ArithmeticException(leftExpressionExecutor.getReturnType() + " or " + rightExpressionExecutor.getReturnType() + " cannot be multiplied");
        }
    }

    public static long getLong(Constant constant) {
        long value;
        if (constant instanceof IntConstant) {
            value = ((IntConstant) (constant)).getValue();
        } else if (constant instanceof LongConstant) {
            value = ((LongConstant) (constant)).getValue();
        } else {
            throw new OperationNotSupportedException("Within only supports long and int and not supports others");
        }
        return value;
    }
}
