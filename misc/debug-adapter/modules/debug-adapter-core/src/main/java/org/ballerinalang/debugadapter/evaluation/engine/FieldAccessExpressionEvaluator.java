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
import io.ballerina.compiler.syntax.tree.FieldAccessExpressionNode;
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
 * Evaluator implementation for field access expressions.
 *
 * @since 2.0.0
 */
public class FieldAccessExpressionEvaluator extends Evaluator {

    private final FieldAccessExpressionNode syntaxNode;
    private final Evaluator objectExpressionEvaluator;

    public FieldAccessExpressionEvaluator(SuspendedContext context, Evaluator expression,
                                          FieldAccessExpressionNode fieldAccessExpressionNode) {
        super(context);
        this.syntaxNode = fieldAccessExpressionNode;
        this.objectExpressionEvaluator = expression;
    }

    @Override
    public BExpressionValue evaluate() throws EvaluationException {
        // expression is evaluated resulting in a value v
        // if v has basic type error, the result is v
        // otherwise, if v does not have basic type mapping, the result is a new error value
        // otherwise, if v does not have a member whose key is field-name, the result is a new error value
        // otherwise, the result is the member of v whose key is field-name.
        try {
            BExpressionValue result = objectExpressionEvaluator.evaluate();
            BVariable resultVar = VariableFactory.getVariable(context, result.getJdiValue());
            if (resultVar.getBType() != BVariableType.OBJECT && resultVar.getBType() != BVariableType.RECORD &&
                    resultVar.getBType() != BVariableType.JSON) {
                throw new EvaluationException(String.format(EvaluationExceptionKind.CUSTOM_ERROR.getString(), "Field " +
                        "access is not supported on type '" + resultVar.getBType().getString() + "'."));
            }
            String fieldName = syntaxNode.fieldName().toSourceCode().trim();
            Value fieldValue = ((BCompoundVariable) resultVar).getChildByName(fieldName);
            return new BExpressionValue(context, fieldValue);
        } catch (EvaluationException e) {
            throw e;
        } catch (DebugVariableException e) {
            throw new EvaluationException(String.format(EvaluationExceptionKind.FIELD_NOT_FOUND.getString(),
                    syntaxNode.fieldName().toSourceCode().trim(), syntaxNode.expression().toSourceCode().trim()));
        } catch (Exception e) {
            throw new EvaluationException(String.format(EvaluationExceptionKind.INTERNAL_ERROR.getString(),
                    syntaxNode.toSourceCode().trim()));
        }
    }
}
