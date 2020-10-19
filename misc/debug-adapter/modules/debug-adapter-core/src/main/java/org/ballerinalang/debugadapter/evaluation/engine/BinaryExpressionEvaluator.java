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

package org.ballerinalang.debugadapter.evaluation.engine;

import com.sun.jdi.Value;
import io.ballerina.compiler.syntax.tree.BinaryExpressionNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import org.ballerinalang.debugadapter.SuspendedContext;
import org.ballerinalang.debugadapter.evaluation.BExpressionValue;
import org.ballerinalang.debugadapter.evaluation.EvaluationException;
import org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind;
import org.ballerinalang.debugadapter.evaluation.EvaluationUtils;
import org.ballerinalang.debugadapter.variable.BVariable;
import org.ballerinalang.debugadapter.variable.BVariableType;
import org.ballerinalang.debugadapter.variable.VariableFactory;

import java.util.ArrayList;
import java.util.List;

import static org.ballerinalang.debugadapter.evaluation.EvaluationUtils.B_TYPE_CHECKER_CLASS;
import static org.ballerinalang.debugadapter.evaluation.EvaluationUtils.JAVA_OBJECT_CLASS;
import static org.ballerinalang.debugadapter.evaluation.EvaluationUtils.REF_EQUAL_METHOD;
import static org.ballerinalang.debugadapter.evaluation.EvaluationUtils.VALUE_EQUAL_METHOD;
import static org.ballerinalang.debugadapter.evaluation.EvaluationUtils.getRuntimeMethod;
import static org.ballerinalang.debugadapter.evaluation.EvaluationUtils.getValueAsObject;

/**
 * Evaluator implementation for binary expressions.
 *
 * @since 2.0.0
 */
public class BinaryExpressionEvaluator extends Evaluator {

    private final BinaryExpressionNode syntaxNode;
    private final Evaluator lhsEvaluator;
    private final Evaluator rhsEvaluator;

    public BinaryExpressionEvaluator(SuspendedContext context, BinaryExpressionNode node, Evaluator lhsEvaluator,
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
                return add(lVar, rVar);
            case MINUS_TOKEN:
                return sub(lVar, rVar);
            case ASTERISK_TOKEN:
                return mul(lVar, rVar);
            case SLASH_TOKEN:
                return div(lVar, rVar);
            case PERCENT_TOKEN:
                return mod(lVar, rVar);
            case LT_TOKEN:
            case GT_TOKEN:
            case LT_EQUAL_TOKEN:
            case GT_EQUAL_TOKEN:
                return compare(lVar, rVar, operatorType);
            case BITWISE_AND_TOKEN:
                return bitwiseAND(lVar, rVar);
            case PIPE_TOKEN:
                return bitwiseOR(lVar, rVar);
            case BITWISE_XOR_TOKEN:
                return bitwiseXOR(lVar, rVar);
            case LOGICAL_AND_TOKEN:
                return logicalAND(lVar, rVar);
            case LOGICAL_OR_TOKEN:
                return logicalOR(lVar, rVar);
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
    private BExpressionValue add(BVariable lVar, BVariable rVar) throws EvaluationException {
        if (lVar.getBType() == BVariableType.INT && rVar.getBType() == BVariableType.INT) {
            // int + int
            long result = Long.parseLong(lVar.computeValue()) + Long.parseLong(rVar.computeValue());
            return EvaluationUtils.make(context, result);
        } else if (lVar.getBType() == BVariableType.FLOAT && rVar.getBType() == BVariableType.FLOAT) {
            // float + float
            double result = Double.parseDouble(lVar.computeValue()) + Double.parseDouble(rVar.computeValue());
            return EvaluationUtils.make(context, result);
        } else if ((lVar.getBType() == BVariableType.INT && rVar.getBType() == BVariableType.FLOAT)
                || (lVar.getBType() == BVariableType.FLOAT && rVar.getBType() == BVariableType.INT)) {
            // int + float , float + int
            double result = Double.parseDouble(lVar.computeValue()) + Double.parseDouble(rVar.computeValue());
            return EvaluationUtils.make(context, result);
        } else if (lVar.getBType() == BVariableType.DECIMAL || rVar.getBType() == BVariableType.DECIMAL) {
            // Todo - Add support for
            // decimal + decimal
            // decimal + int , int + decimal
            // decimal + float , float + decimal
            throw createUnsupportedOperationException(lVar, rVar, SyntaxKind.PLUS_TOKEN);
        } else if (lVar.getBType() == BVariableType.STRING && rVar.getBType() == BVariableType.STRING) {
            // string + string
            String result = lVar.computeValue() + rVar.computeValue();
            return EvaluationUtils.make(context, result);
        } else if (lVar.getBType() == BVariableType.XML && rVar.getBType() == BVariableType.XML) {
            // xml + xml
            // Todo - Add support
            throw createUnsupportedOperationException(lVar, rVar, SyntaxKind.PLUS_TOKEN);
        } else {
            throw createUnsupportedOperationException(lVar, rVar, SyntaxKind.PLUS_TOKEN);
        }
    }

    /**
     * Performs subtraction operation on the given ballerina variable values and returns the result.
     */
    private BExpressionValue sub(BVariable lVar, BVariable rVar) throws EvaluationException {
        if (lVar.getBType() == BVariableType.INT && rVar.getBType() == BVariableType.INT) {
            // int - int
            long result = Long.parseLong(lVar.computeValue()) - Long.parseLong(rVar.computeValue());
            return EvaluationUtils.make(context, result);
        } else if (lVar.getBType() == BVariableType.FLOAT && rVar.getBType() == BVariableType.FLOAT) {
            // float - float
            double result = Double.parseDouble(lVar.computeValue()) - Double.parseDouble(rVar.computeValue());
            return EvaluationUtils.make(context, result);
        } else if ((lVar.getBType() == BVariableType.INT && rVar.getBType() == BVariableType.FLOAT)
                || (lVar.getBType() == BVariableType.FLOAT && rVar.getBType() == BVariableType.INT)) {
            // int - float , float - int
            double result = Double.parseDouble(lVar.computeValue()) - Double.parseDouble(rVar.computeValue());
            return EvaluationUtils.make(context, result);
        } else if (lVar.getBType() == BVariableType.DECIMAL || rVar.getBType() == BVariableType.DECIMAL) {
            // Todo - Add support for
            // decimal - decimal
            // decimal - int , int - decimal
            // decimal - float , float - decimal
            throw createUnsupportedOperationException(lVar, rVar, SyntaxKind.MINUS_TOKEN);
        } else {
            throw createUnsupportedOperationException(lVar, rVar, SyntaxKind.MINUS_TOKEN);
        }
    }

    /**
     * Performs multiplication operation on the given ballerina variable values and returns the result.
     */
    private BExpressionValue mul(BVariable lVar, BVariable rVar) throws EvaluationException {
        if (lVar.getBType() == BVariableType.INT && rVar.getBType() == BVariableType.INT) {
            // int * int
            long result = Long.parseLong(lVar.computeValue()) * Long.parseLong(rVar.computeValue());
            return EvaluationUtils.make(context, result);
        } else if (lVar.getBType() == BVariableType.FLOAT && rVar.getBType() == BVariableType.FLOAT) {
            // float * float
            double result = Double.parseDouble(lVar.computeValue()) * Double.parseDouble(rVar.computeValue());
            return EvaluationUtils.make(context, result);
        } else if ((lVar.getBType() == BVariableType.INT && rVar.getBType() == BVariableType.FLOAT)
                || (lVar.getBType() == BVariableType.FLOAT && rVar.getBType() == BVariableType.INT)) {
            // int * float , float * int
            double result = Double.parseDouble(lVar.computeValue()) * Double.parseDouble(rVar.computeValue());
            return EvaluationUtils.make(context, result);
        } else if (lVar.getBType() == BVariableType.DECIMAL || rVar.getBType() == BVariableType.DECIMAL) {
            // Todo - Add support for
            // decimal * decimal
            // decimal * int , int * decimal
            // decimal * float , float * decimal
            throw createUnsupportedOperationException(lVar, rVar, SyntaxKind.ASTERISK_TOKEN);
        } else {
            throw createUnsupportedOperationException(lVar, rVar, SyntaxKind.ASTERISK_TOKEN);
        }
    }

    /**
     * Performs division operation on the given ballerina variable values and returns the result.
     */
    private BExpressionValue div(BVariable lVar, BVariable rVar) throws EvaluationException {
        if (lVar.getBType() == BVariableType.INT && rVar.getBType() == BVariableType.INT) {
            // int / int
            long result = Long.parseLong(lVar.computeValue()) / Long.parseLong(rVar.computeValue());
            return EvaluationUtils.make(context, result);
        } else if (lVar.getBType() == BVariableType.FLOAT && rVar.getBType() == BVariableType.FLOAT) {
            // float / float
            double result = Double.parseDouble(lVar.computeValue()) / Double.parseDouble(rVar.computeValue());
            return EvaluationUtils.make(context, result);
        } else if ((lVar.getBType() == BVariableType.INT && rVar.getBType() == BVariableType.FLOAT)
                || (lVar.getBType() == BVariableType.FLOAT && rVar.getBType() == BVariableType.INT)) {
            // int / float , float / int
            double result = Double.parseDouble(lVar.computeValue()) / Double.parseDouble(rVar.computeValue());
            return EvaluationUtils.make(context, result);
        } else if (lVar.getBType() == BVariableType.DECIMAL || rVar.getBType() == BVariableType.DECIMAL) {
            // Todo - Add support for
            // decimal / decimal
            // decimal / int , int / decimal
            // decimal / float , float / decimal
            throw createUnsupportedOperationException(lVar, rVar, SyntaxKind.SLASH_TOKEN);
        } else {
            throw createUnsupportedOperationException(lVar, rVar, SyntaxKind.SLASH_TOKEN);
        }
    }

    /**
     * Performs modulus operation on the given ballerina variable values and returns the result.
     */
    private BExpressionValue mod(BVariable lVar, BVariable rVar) throws EvaluationException {
        if (lVar.getBType() == BVariableType.INT && rVar.getBType() == BVariableType.INT) {
            // int % int
            long result = Long.parseLong(lVar.computeValue()) % Long.parseLong(rVar.computeValue());
            return EvaluationUtils.make(context, result);
        } else if (lVar.getBType() == BVariableType.FLOAT && rVar.getBType() == BVariableType.FLOAT) {
            // float % float
            double result = Double.parseDouble(lVar.computeValue()) % Double.parseDouble(rVar.computeValue());
            return EvaluationUtils.make(context, result);
        } else if ((lVar.getBType() == BVariableType.INT && rVar.getBType() == BVariableType.FLOAT)
                || (lVar.getBType() == BVariableType.FLOAT && rVar.getBType() == BVariableType.INT)) {
            // int % float , float % int
            double result = Double.parseDouble(lVar.computeValue()) % Double.parseDouble(rVar.computeValue());
            return EvaluationUtils.make(context, result);
        } else if (lVar.getBType() == BVariableType.DECIMAL || rVar.getBType() == BVariableType.DECIMAL) {
            // Todo - Add support for
            // decimal % decimal
            // decimal % int , int % decimal
            // decimal % float , float % decimal
            throw createUnsupportedOperationException(lVar, rVar, SyntaxKind.PERCENT_TOKEN);
        } else {
            throw createUnsupportedOperationException(lVar, rVar, SyntaxKind.PERCENT_TOKEN);
        }
    }

    private BExpressionValue compare(BVariable lVar, BVariable rVar, SyntaxKind operator) throws EvaluationException {
        if (lVar.getBType() == BVariableType.INT && rVar.getBType() == BVariableType.INT) {
            // int <=> int
            boolean result;
            switch (operator) {
                case LT_TOKEN:
                    result = Long.parseLong(lVar.computeValue()) < Long.parseLong(rVar.computeValue());
                    return EvaluationUtils.make(context, result);
                case GT_TOKEN:
                    result = Long.parseLong(lVar.computeValue()) > Long.parseLong(rVar.computeValue());
                    return EvaluationUtils.make(context, result);
                case LT_EQUAL_TOKEN:
                    result = Long.parseLong(lVar.computeValue()) <= Long.parseLong(rVar.computeValue());
                    return EvaluationUtils.make(context, result);
                case GT_EQUAL_TOKEN:
                    result = Long.parseLong(lVar.computeValue()) >= Long.parseLong(rVar.computeValue());
                    return EvaluationUtils.make(context, result);
            }
        } else if ((lVar.getBType() == BVariableType.INT && rVar.getBType() == BVariableType.FLOAT)
                || (lVar.getBType() == BVariableType.FLOAT && rVar.getBType() == BVariableType.INT)
                || lVar.getBType() == BVariableType.FLOAT && rVar.getBType() == BVariableType.FLOAT) {
            // int <=> float or float <=> float
            boolean result;
            switch (operator) {
                case LT_TOKEN:
                    result = Double.parseDouble(lVar.computeValue()) < Double.parseDouble(rVar.computeValue());
                    return EvaluationUtils.make(context, result);
                case GT_TOKEN:
                    result = Double.parseDouble(lVar.computeValue()) > Double.parseDouble(rVar.computeValue());
                    return EvaluationUtils.make(context, result);
                case LT_EQUAL_TOKEN:
                    result = Double.parseDouble(lVar.computeValue()) <= Double.parseDouble(rVar.computeValue());
                    return EvaluationUtils.make(context, result);
                case GT_EQUAL_TOKEN:
                    result = Double.parseDouble(lVar.computeValue()) >= Double.parseDouble(rVar.computeValue());
                    return EvaluationUtils.make(context, result);
            }
        } else if (lVar.getBType() == BVariableType.DECIMAL || rVar.getBType() == BVariableType.DECIMAL) {
            // Todo - Add support for
            // decimal <=> decimal
            // decimal <=>  int or int <=> decimal
            // decimal <=> float or float <=> decimal
            throw createUnsupportedOperationException(lVar, rVar, SyntaxKind.PERCENT_TOKEN);
        }
        throw createUnsupportedOperationException(lVar, rVar, SyntaxKind.PERCENT_TOKEN);
    }

    /**
     * Performs bitwise AND operation on the given ballerina variable values and returns the result.
     */
    private BExpressionValue bitwiseAND(BVariable lVar, BVariable rVar) throws EvaluationException {
        if (lVar.getBType() == BVariableType.INT && rVar.getBType() == BVariableType.INT) {
            // int + int
            // Todo - filter unsigned integers and signed integers with 8, 16 and 32 bits
            long result = Long.parseLong(lVar.computeValue()) & Long.parseLong(rVar.computeValue());
            return EvaluationUtils.make(context, result);
        } else {
            // Todo - Add support for signed and unsigned integers
            throw createUnsupportedOperationException(lVar, rVar, SyntaxKind.BITWISE_AND_TOKEN);
        }
    }

    /**
     * Performs bitwise OR operation on the given ballerina variable values and returns the result.
     */
    private BExpressionValue bitwiseOR(BVariable lVar, BVariable rVar) throws EvaluationException {
        if (lVar.getBType() == BVariableType.INT && rVar.getBType() == BVariableType.INT) {
            // int + int
            // Todo - filter unsigned integers and signed integers with 8, 16 and 32 bits
            long result = Long.parseLong(lVar.computeValue()) | Long.parseLong(rVar.computeValue());
            return EvaluationUtils.make(context, result);
        } else {
            // Todo - Add support for signed and unsigned integers
            throw createUnsupportedOperationException(lVar, rVar, SyntaxKind.PIPE_TOKEN);
        }
    }

    /**
     * Performs bitwise XOR operation on the given ballerina variable values and returns the result.
     */
    private BExpressionValue bitwiseXOR(BVariable lVar, BVariable rVar) throws EvaluationException {
        if (lVar.getBType() == BVariableType.INT && rVar.getBType() == BVariableType.INT) {
            // int + int
            // Todo - filter unsigned integers and signed integers with 8, 16 and 32 bits
            long result = Long.parseLong(lVar.computeValue()) ^ Long.parseLong(rVar.computeValue());
            return EvaluationUtils.make(context, result);
        } else {
            // Todo - Add support for signed and unsigned integers
            throw createUnsupportedOperationException(lVar, rVar, SyntaxKind.BITWISE_XOR_TOKEN);
        }
    }

    /**
     * Performs logical AND operation on the given ballerina variable values and returns the result.
     */
    private BExpressionValue logicalAND(BVariable lVar, BVariable rVar) throws EvaluationException {
        if (lVar.getBType() == BVariableType.BOOLEAN && rVar.getBType() == BVariableType.BOOLEAN) {
            return !Boolean.parseBoolean(lVar.computeValue()) ? EvaluationUtils.make(context, false)
                    : EvaluationUtils.make(context, Boolean.parseBoolean(rVar.computeValue()));
        } else {
            throw createUnsupportedOperationException(lVar, rVar, SyntaxKind.LOGICAL_AND_TOKEN);
        }
    }

    /**
     * Performs logical OR operation on the given ballerina variable values and returns the result.
     */
    private BExpressionValue logicalOR(BVariable lVar, BVariable rVar) throws EvaluationException {
        if (lVar.getBType() == BVariableType.BOOLEAN && rVar.getBType() == BVariableType.BOOLEAN) {
            return Boolean.parseBoolean(lVar.computeValue()) ? EvaluationUtils.make(context, true)
                    : EvaluationUtils.make(context, Boolean.parseBoolean(rVar.computeValue()));
        } else {
            throw createUnsupportedOperationException(lVar, rVar, SyntaxKind.LOGICAL_OR_TOKEN);
        }
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
        Value result = runtimeMethod.invoke();
        BVariable variable = VariableFactory.getVariable(context, result);
        boolean booleanValue = Boolean.parseBoolean(variable.getDapVariable().getValue());
        booleanValue = operatorType == SyntaxKind.DOUBLE_EQUAL_TOKEN ? booleanValue : !booleanValue;
        return EvaluationUtils.make(context, booleanValue);
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
        Value result = runtimeMethod.invoke();
        BVariable variable = VariableFactory.getVariable(context, result);
        boolean booleanValue = Boolean.parseBoolean(variable.getDapVariable().getValue());
        booleanValue = operatorType == SyntaxKind.TRIPPLE_EQUAL_TOKEN ? booleanValue : !booleanValue;
        return EvaluationUtils.make(context, booleanValue);
    }

    private EvaluationException createUnsupportedOperationException(BVariable lVar, BVariable rVar,
                                                                    SyntaxKind operator) {
        String reason = String.format(EvaluationExceptionKind.UNSUPPORTED_OPERATION.getReason(), operator.stringValue(),
                lVar.getBType().getString(), rVar.getBType().getString());
        return new EvaluationException(
                String.format(EvaluationExceptionKind.UNSUPPORTED_EXPRESSION.getString(), reason));
    }
}
