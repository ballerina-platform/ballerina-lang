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
import io.ballerina.compiler.syntax.tree.OptionalFieldAccessExpressionNode;
import org.ballerinalang.debugadapter.SuspendedContext;
import org.ballerinalang.debugadapter.evaluation.BExpressionValue;
import org.ballerinalang.debugadapter.evaluation.EvaluationException;
import org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind;
import org.ballerinalang.debugadapter.variable.BCompoundVariable;
import org.ballerinalang.debugadapter.variable.BVariable;
import org.ballerinalang.debugadapter.variable.BVariableType;
import org.ballerinalang.debugadapter.variable.DebugVariableException;
import org.ballerinalang.debugadapter.variable.VariableFactory;

/**
 * Evaluator implementation for optional field access expressions.
 *
 * @since 2.0.0
 */
public class OptionalFieldAccessExpressionEvaluator extends Evaluator {

    private final OptionalFieldAccessExpressionNode syntaxNode;
    private final Evaluator objectExpressionEvaluator;

    public OptionalFieldAccessExpressionEvaluator(SuspendedContext context, Evaluator expression,
                                                  OptionalFieldAccessExpressionNode fieldAccessExpressionNode) {
        super(context);
        this.syntaxNode = fieldAccessExpressionNode;
        this.objectExpressionEvaluator = expression;
    }

    @Override
    public BExpressionValue evaluate() throws EvaluationException {
        // expression is evaluated resulting in a value v
        // if v is (), the result is ()
        // otherwise, if v has basic type error, the result is v
        // otherwise, if v does not have basic type mapping, the result is a new error value
        // otherwise, if v does not have a member whose key is field-name, the result is ()
        // otherwise, the result is the member of v whose key is field-name.
        try {
            BExpressionValue exprResult = objectExpressionEvaluator.evaluate();
            BVariable exprResultVar = VariableFactory.getVariable(context, exprResult.getJdiValue());
            // if v is (), the result should be ().
            if (exprResultVar.getBType() == BVariableType.NIL) {
                return new BExpressionValue(context, null);
            }
            // if expression result does not have basic type mapping, the result is a new error value.
            if (exprResultVar.getBType() != BVariableType.OBJECT && exprResultVar.getBType() != BVariableType.RECORD &&
                    exprResultVar.getBType() != BVariableType.JSON) {
                throw new EvaluationException(String.format(EvaluationExceptionKind.CUSTOM_ERROR.getString(), " " +
                        "Optional field access is not supported on type '" + exprResultVar.getBType().getString() +
                        "'."));
            }
            String fieldName = syntaxNode.fieldName().toSourceCode().trim();
            try {
                Value fieldValue = ((BCompoundVariable) exprResultVar).getChildByName(fieldName);
                return new BExpressionValue(context, fieldValue);
            } catch (DebugVariableException e) {
                // if expression result does not have a member whose key is field-name, the result is ().
                return new BExpressionValue(context, null);
            }
        } catch (EvaluationException e) {
            throw e;
        } catch (Exception e) {
            throw new EvaluationException(String.format(EvaluationExceptionKind.INTERNAL_ERROR.getString(),
                    syntaxNode.toSourceCode().trim()));
        }
    }
}
