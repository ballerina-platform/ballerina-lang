/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.debugadapter.evaluation.engine.expression;

import com.sun.jdi.Value;
import io.ballerina.compiler.syntax.tree.BinaryExpressionNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import org.ballerinalang.debugadapter.EvaluationContext;
import org.ballerinalang.debugadapter.evaluation.BExpressionValue;
import org.ballerinalang.debugadapter.evaluation.EvaluationException;
import org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind;
import org.ballerinalang.debugadapter.evaluation.engine.Evaluator;
import org.ballerinalang.debugadapter.evaluation.engine.invokable.GeneratedStaticMethod;
import org.ballerinalang.debugadapter.evaluation.engine.invokable.RuntimeStaticMethod;
import org.ballerinalang.debugadapter.evaluation.utils.VMUtils;
import org.ballerinalang.debugadapter.variable.BVariable;
import org.ballerinalang.debugadapter.variable.BVariableType;
import org.ballerinalang.debugadapter.variable.VariableFactory;

import java.util.ArrayList;
import java.util.List;

import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.B_ADD_METHOD;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.B_ARITHMETIC_EXPR_HELPER_CLASS;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.B_BITWISE_AND_METHOD;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.B_BITWISE_EXPR_HELPER_CLASS;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.B_BITWISE_OR_METHOD;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.B_BITWISE_XOR_METHOD;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.B_DIV_METHOD;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.B_GT_EQUALS_METHOD;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.B_GT_METHOD;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.B_LEFT_SHIFT_METHOD;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.B_LOGICAL_AND_METHOD;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.B_LOGICAL_EXPR_HELPER_CLASS;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.B_LOGICAL_OR_METHOD;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.B_LT_EQUALS_METHOD;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.B_LT_METHOD;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.B_MOD_METHOD;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.B_MUL_METHOD;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.B_RELATIONAL_EXPR_HELPER_CLASS;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.B_SHIFT_EXPR_HELPER_CLASS;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.B_SIGNED_RIGHT_SHIFT_METHOD;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.B_SUB_METHOD;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.B_TYPE_CHECKER_CLASS;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.B_UNSIGNED_RIGHT_SHIFT_METHOD;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.B_XML_FACTORY_CLASS;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.B_XML_VALUE_CLASS;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.JAVA_OBJECT_CLASS;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.REF_EQUAL_METHOD;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.VALUE_EQUAL_METHOD;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.XML_CONCAT_METHOD;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.getGeneratedMethod;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.getRuntimeMethod;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.getValueAsObject;

/**
 * Evaluator implementation for binary expressions.
 *
 * @since 2.0.0
 */
public class BinaryExpressionEvaluator extends Evaluator {

    protected final BinaryExpressionNode syntaxNode;
    protected final Evaluator lhsEvaluator;
    protected final Evaluator rhsEvaluator;

    public BinaryExpressionEvaluator(EvaluationContext context, BinaryExpressionNode node, Evaluator lhsEvaluator,
                                     Evaluator rhsEvaluator) {
        super(context);
        this.syntaxNode = node;
        this.lhsEvaluator = lhsEvaluator;
        this.rhsEvaluator = rhsEvaluator;
    }

    @Override
    public BExpressionValue evaluate() throws EvaluationException {
        try {
            // Evaluates the lhs and rhs expressions.
            BExpressionValue lhsResult = lhsEvaluator.evaluate();
            BExpressionValue rhsResult = rhsEvaluator.evaluate();
            return performOperation(lhsResult, rhsResult);
        } catch (EvaluationException e) {
            throw e;
        } catch (Exception e) {
            throw new EvaluationException(String.format(EvaluationExceptionKind.INTERNAL_ERROR.getString(),
                    syntaxNode.toSourceCode().trim()));
        }
    }

    /**
     * Performs the binary operation and returns the evaluation result.
     *
     * @param lhs LHS evaluation result
     * @param rhs RHS evaluation result
     * @return returns the evaluation result of the binary operation.
     */
    private BExpressionValue performOperation(BExpressionValue lhs, BExpressionValue rhs) throws EvaluationException {
        Value lValue = lhs.getJdiValue();
        Value rValue = rhs.getJdiValue();
        BVariable lVar = VariableFactory.getVariable(context, lValue);
        BVariable rVar = VariableFactory.getVariable(context, rValue);
        SyntaxKind operatorType = syntaxNode.operator().kind();

        switch (operatorType) {
            case PLUS_TOKEN:
            case MINUS_TOKEN:
            case ASTERISK_TOKEN:
            case SLASH_TOKEN:
            case PERCENT_TOKEN:
                return performArithmeticOperation(lVar, rVar, operatorType);
            case LT_TOKEN:
            case GT_TOKEN:
            case LT_EQUAL_TOKEN:
            case GT_EQUAL_TOKEN:
                return compare(lVar, rVar, operatorType);
            case BITWISE_AND_TOKEN:
            case PIPE_TOKEN:
            case BITWISE_XOR_TOKEN:
                return performBitwiseOperation(lVar, rVar, operatorType);
            case DOUBLE_LT_TOKEN:
            case DOUBLE_GT_TOKEN:
            case TRIPPLE_GT_TOKEN:
                return performShiftOperation(lVar, rVar, operatorType);
            case LOGICAL_AND_TOKEN:
            case LOGICAL_OR_TOKEN:
                return performLogicalOperation(lVar, rVar, operatorType);
            case ELVIS_TOKEN:
                return conditionalReturn(lVar, rVar);
            case DOUBLE_EQUAL_TOKEN:
            case NOT_EQUAL_TOKEN:
                return checkValueEquality(lVar, rVar, operatorType);
            case TRIPPLE_EQUAL_TOKEN:
            case NOT_DOUBLE_EQUAL_TOKEN:
                return checkReferenceEquality(lVar, rVar, operatorType);
            default:
                throw createUnsupportedOperationException(lVar, rVar, operatorType);
        }
    }

    /**
     * Performs addition/concatenation operation on the given ballerina variable values and returns the result.
     */
    private BExpressionValue performArithmeticOperation(BVariable lVar, BVariable rVar, SyntaxKind operator)
            throws EvaluationException {

        // XML concatenation.
        if (lVar.getBType() == BVariableType.XML && rVar.getBType() == BVariableType.XML &&
                operator == SyntaxKind.PLUS_TOKEN) {
            // Prepares to invoke the JVM runtime util function which is responsible for XML concatenation.
            List<Value> argList = new ArrayList<>();
            argList.add(getValueAsObject(context, lVar));
            argList.add(getValueAsObject(context, rVar));
            List<String> argTypeNames = new ArrayList<>();
            argTypeNames.add(B_XML_VALUE_CLASS);
            argTypeNames.add(B_XML_VALUE_CLASS);
            RuntimeStaticMethod runtimeMethod = getRuntimeMethod(context, B_XML_FACTORY_CLASS, XML_CONCAT_METHOD,
                    argTypeNames);
            runtimeMethod.setArgValues(argList);
            Value result = runtimeMethod.invokeSafely();
            return new BExpressionValue(context, result);
        } else {
            List<Value> argList = new ArrayList<>();
            argList.add(getValueAsObject(context, lVar));
            argList.add(getValueAsObject(context, rVar));

            GeneratedStaticMethod genMethod;
            switch (operator) {
                case PLUS_TOKEN:
                    genMethod = getGeneratedMethod(context, B_ARITHMETIC_EXPR_HELPER_CLASS, B_ADD_METHOD);
                    break;
                case MINUS_TOKEN:
                    genMethod = getGeneratedMethod(context, B_ARITHMETIC_EXPR_HELPER_CLASS, B_SUB_METHOD);
                    break;
                case ASTERISK_TOKEN:
                    genMethod = getGeneratedMethod(context, B_ARITHMETIC_EXPR_HELPER_CLASS, B_MUL_METHOD);
                    break;
                case SLASH_TOKEN:
                    genMethod = getGeneratedMethod(context, B_ARITHMETIC_EXPR_HELPER_CLASS, B_DIV_METHOD);
                    break;
                case PERCENT_TOKEN:
                    genMethod = getGeneratedMethod(context, B_ARITHMETIC_EXPR_HELPER_CLASS, B_MOD_METHOD);
                    break;
                default:
                    throw createUnsupportedOperationException(lVar, rVar, operator);
            }
            genMethod.setArgValues(argList);
            Value result = genMethod.invokeSafely();
            return new BExpressionValue(context, result);
        }
    }

    private BExpressionValue compare(BVariable lVar, BVariable rVar, SyntaxKind operator) throws EvaluationException {

        // Tests the relative order of two values. There must be an ordered type to which the static type of both
        // operands belong. The static type of the result is boolean.
        //
        // OrderedType ::= ()|boolean|int|float|decimal|string|OrderedType[]
        List<Value> argList = new ArrayList<>();
        argList.add(getValueAsObject(context, lVar));
        argList.add(getValueAsObject(context, rVar));

        GeneratedStaticMethod genMethod;
        switch (operator) {
            case LT_TOKEN:
                genMethod = getGeneratedMethod(context, B_RELATIONAL_EXPR_HELPER_CLASS, B_LT_METHOD);
                break;
            case LT_EQUAL_TOKEN:
                genMethod = getGeneratedMethod(context, B_RELATIONAL_EXPR_HELPER_CLASS, B_LT_EQUALS_METHOD);
                break;
            case GT_TOKEN:
                genMethod = getGeneratedMethod(context, B_RELATIONAL_EXPR_HELPER_CLASS, B_GT_METHOD);
                break;
            case GT_EQUAL_TOKEN:
                genMethod = getGeneratedMethod(context, B_RELATIONAL_EXPR_HELPER_CLASS, B_GT_EQUALS_METHOD);
                break;
            default:
                throw createUnsupportedOperationException(lVar, rVar, operator);
        }
        genMethod.setArgValues(argList);
        Value result = genMethod.invokeSafely();
        return new BExpressionValue(context, result);
    }

    private BExpressionValue performBitwiseOperation(BVariable lVar, BVariable rVar, SyntaxKind operator)
            throws EvaluationException {
        List<Value> argList = new ArrayList<>();
        argList.add(getValueAsObject(context, lVar));
        argList.add(getValueAsObject(context, rVar));

        GeneratedStaticMethod genMethod;
        switch (operator) {
            case BITWISE_AND_TOKEN:
                genMethod = getGeneratedMethod(context, B_BITWISE_EXPR_HELPER_CLASS, B_BITWISE_AND_METHOD);
                break;
            case PIPE_TOKEN:
                genMethod = getGeneratedMethod(context, B_BITWISE_EXPR_HELPER_CLASS, B_BITWISE_OR_METHOD);
                break;
            case BITWISE_XOR_TOKEN:
                genMethod = getGeneratedMethod(context, B_BITWISE_EXPR_HELPER_CLASS, B_BITWISE_XOR_METHOD);
                break;
            default:
                throw createUnsupportedOperationException(lVar, rVar, operator);
        }
        genMethod.setArgValues(argList);
        Value result = genMethod.invokeSafely();
        return new BExpressionValue(context, result);
    }

    private BExpressionValue performShiftOperation(BVariable lVar, BVariable rVar, SyntaxKind operator)
            throws EvaluationException {
        List<Value> argList = new ArrayList<>();
        argList.add(getValueAsObject(context, lVar));
        argList.add(getValueAsObject(context, rVar));

        GeneratedStaticMethod genMethod;
        switch (operator) {
            case DOUBLE_LT_TOKEN:
                genMethod = getGeneratedMethod(context, B_SHIFT_EXPR_HELPER_CLASS, B_LEFT_SHIFT_METHOD);
                break;
            case DOUBLE_GT_TOKEN:
                genMethod = getGeneratedMethod(context, B_SHIFT_EXPR_HELPER_CLASS, B_SIGNED_RIGHT_SHIFT_METHOD);
                break;
            case TRIPPLE_GT_TOKEN:
                genMethod = getGeneratedMethod(context, B_SHIFT_EXPR_HELPER_CLASS, B_UNSIGNED_RIGHT_SHIFT_METHOD);
                break;
            default:
                throw createUnsupportedOperationException(lVar, rVar, operator);
        }
        genMethod.setArgValues(argList);
        Value result = genMethod.invokeSafely();
        return new BExpressionValue(context, result);
    }

    private BExpressionValue performLogicalOperation(BVariable lVar, BVariable rVar, SyntaxKind operator)
            throws EvaluationException {
        List<Value> argList = new ArrayList<>();
        argList.add(getValueAsObject(context, lVar));
        argList.add(getValueAsObject(context, rVar));

        GeneratedStaticMethod genMethod;
        switch (operator) {
            case LOGICAL_AND_TOKEN:
                genMethod = getGeneratedMethod(context, B_LOGICAL_EXPR_HELPER_CLASS, B_LOGICAL_AND_METHOD);
                break;
            case LOGICAL_OR_TOKEN:
                genMethod = getGeneratedMethod(context, B_LOGICAL_EXPR_HELPER_CLASS, B_LOGICAL_OR_METHOD);
                break;
            default:
                throw createUnsupportedOperationException(lVar, rVar, operator);
        }
        genMethod.setArgValues(argList);
        Value result = genMethod.invokeSafely();
        return new BExpressionValue(context, result);
    }

    /**
     * Performs elvis conditional operation.
     */
    private BExpressionValue conditionalReturn(BVariable lVar, BVariable rVar) {
        // Evaluate LHS to get a value x.
        // If x is not nil, then return x.
        // Otherwise, return the result of evaluating RHS.
        if (lVar.getBType() != BVariableType.NIL) {
            return new BExpressionValue(context, lVar.getJvmValue());
        } else {
            return new BExpressionValue(context, rVar.getJvmValue());
        }
    }

    /**
     * Checks for deep value equality.
     */
    private BExpressionValue checkValueEquality(BVariable lVar, BVariable rVar, SyntaxKind operatorType)
            throws EvaluationException {
        List<Value> argList = new ArrayList<>();
        argList.add(getValueAsObject(context, lVar));
        argList.add(getValueAsObject(context, rVar));

        List<String> argTypeNames = new ArrayList<>();
        argTypeNames.add(JAVA_OBJECT_CLASS);
        argTypeNames.add(JAVA_OBJECT_CLASS);
        RuntimeStaticMethod runtimeMethod = getRuntimeMethod(context, B_TYPE_CHECKER_CLASS, VALUE_EQUAL_METHOD,
                argTypeNames);
        runtimeMethod.setArgValues(argList);
        Value result = runtimeMethod.invokeSafely();
        BVariable variable = VariableFactory.getVariable(context, result);
        boolean booleanValue = Boolean.parseBoolean(variable.getDapVariable().getValue());
        booleanValue = operatorType == SyntaxKind.DOUBLE_EQUAL_TOKEN ? booleanValue : !booleanValue;
        return VMUtils.make(context, booleanValue);
    }

    /**
     * Checks for reference equality.
     */
    private BExpressionValue checkReferenceEquality(BVariable lVar, BVariable rVar, SyntaxKind operatorType)
            throws EvaluationException {
        List<Value> argList = new ArrayList<>();
        argList.add(getValueAsObject(context, lVar));
        argList.add(getValueAsObject(context, rVar));

        List<String> argTypeNames = new ArrayList<>();
        argTypeNames.add(JAVA_OBJECT_CLASS);
        argTypeNames.add(JAVA_OBJECT_CLASS);
        RuntimeStaticMethod runtimeMethod = getRuntimeMethod(context, B_TYPE_CHECKER_CLASS, REF_EQUAL_METHOD,
                argTypeNames);
        runtimeMethod.setArgValues(argList);
        Value result = runtimeMethod.invokeSafely();
        BVariable variable = VariableFactory.getVariable(context, result);
        boolean booleanValue = Boolean.parseBoolean(variable.getDapVariable().getValue());
        booleanValue = operatorType == SyntaxKind.TRIPPLE_EQUAL_TOKEN ? booleanValue : !booleanValue;
        return VMUtils.make(context, booleanValue);
    }

    protected EvaluationException createUnsupportedOperationException(BVariable lVar, BVariable rVar,
                                                                      SyntaxKind operator) {
        return new EvaluationException(String.format(EvaluationExceptionKind.UNSUPPORTED_OPERATION.getString(),
                operator.stringValue(), lVar.getBType().getString(), rVar.getBType().getString()));
    }
}
