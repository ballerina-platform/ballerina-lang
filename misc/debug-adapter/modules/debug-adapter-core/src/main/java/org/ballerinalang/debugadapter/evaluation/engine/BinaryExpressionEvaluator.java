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
import io.ballerinalang.compiler.syntax.tree.BinaryExpressionNode;
import io.ballerinalang.compiler.syntax.tree.SyntaxKind;
import org.ballerinalang.debugadapter.SuspendedContext;
import org.ballerinalang.debugadapter.evaluation.BExpressionValue;
import org.ballerinalang.debugadapter.evaluation.EvaluationException;
import org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind;
import org.ballerinalang.debugadapter.evaluation.EvaluationUtils;
import org.ballerinalang.debugadapter.variable.BVariable;
import org.ballerinalang.debugadapter.variable.BVariableType;
import org.ballerinalang.debugadapter.variable.VariableFactory;

/**
 * Binary expression evaluator implementation.
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
            BExpressionValue lhsValue = lhsEvaluator.evaluate();
            BExpressionValue rhsValue = rhsEvaluator.evaluate();
            return performOperation(lhsValue, rhsValue);
        } catch (EvaluationException e) {
            throw e;
        } catch (Exception e) {
            throw new EvaluationException(String.format(EvaluationExceptionKind.CUSTOM_ERROR.getString(), "An " +
                    "internal error is occurred while evaluating the binary expression: " + syntaxNode.toSourceCode()));
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
        BVariable lVar = VariableFactory.getVariable(context, lValue, lValue.type().name(), "lVar");
        BVariable rVar = VariableFactory.getVariable(context, rValue, rValue.type().name(), "rVar");
        SyntaxKind operatorType = syntaxNode.operator().kind();

        if (operatorType == SyntaxKind.PLUS_TOKEN) {
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
                // Todo - Add support
                // decimal + decimal
                // decimal + int , int + decimal
                // decimal + float , float + decimal
                throw createUnsupportedOperationException(lVar, rVar, operatorType);
            } else if (lVar.getBType() == BVariableType.STRING && rVar.getBType() == BVariableType.STRING) {
                // string + string
                String result = lVar.computeValue() + rVar.computeValue();
                return EvaluationUtils.make(context, result);
            } else if (lVar.getBType() == BVariableType.XML && rVar.getBType() == BVariableType.XML) {
                // xml + xml
                // Todo - Add support
                throw createUnsupportedOperationException(lVar, rVar, operatorType);
            } else {
                throw createUnsupportedOperationException(lVar, rVar, operatorType);
            }
        } else if (operatorType == SyntaxKind.MINUS_TOKEN) {
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
                // Todo - Add support
                // decimal - decimal
                // decimal - int , int - decimal
                // decimal - float , float - decimal
                throw createUnsupportedOperationException(lVar, rVar, operatorType);
            } else {
                throw createUnsupportedOperationException(lVar, rVar, operatorType);
            }
        } else if (operatorType == SyntaxKind.ASTERISK_TOKEN) {
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
                // Todo - Add support
                // decimal * decimal
                // decimal * int , int * decimal
                // decimal * float , float * decimal
                throw createUnsupportedOperationException(lVar, rVar, operatorType);
            } else {
                throw createUnsupportedOperationException(lVar, rVar, operatorType);
            }
        } else if (operatorType == SyntaxKind.SLASH_TOKEN) {
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
                // Todo - Add support
                // decimal / decimal
                // decimal / int , int / decimal
                // decimal / float , float / decimal
                throw createUnsupportedOperationException(lVar, rVar, operatorType);
            } else {
                throw createUnsupportedOperationException(lVar, rVar, operatorType);
            }
        } else if (operatorType == SyntaxKind.PERCENT_TOKEN) {
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
                // Todo - Add support
                // decimal % decimal
                // decimal % int , int % decimal
                // decimal % float , float % decimal
                throw createUnsupportedOperationException(lVar, rVar, operatorType);
            } else {
                throw createUnsupportedOperationException(lVar, rVar, operatorType);
            }
        }
        throw createUnsupportedOperationException(lVar, rVar, operatorType);
    }

    private EvaluationException createUnsupportedOperationException(BVariable lVar, BVariable rVar,
                                                                    SyntaxKind operator) {
        String reason = String.format(EvaluationExceptionKind.UNSUPPORTED_OPERATION.getString(), operator.stringValue(),
                lVar.getBType().getString(), rVar.getBType().getString());
        return new EvaluationException(
                String.format(EvaluationExceptionKind.UNSUPPORTED_EXPRESSION.getString(), reason));
    }
}
