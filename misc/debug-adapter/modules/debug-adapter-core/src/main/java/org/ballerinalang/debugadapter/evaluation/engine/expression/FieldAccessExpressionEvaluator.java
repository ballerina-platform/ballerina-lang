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
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.FieldAccessExpressionNode;
import io.ballerina.compiler.syntax.tree.OptionalFieldAccessExpressionNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import org.ballerinalang.debugadapter.EvaluationContext;
import org.ballerinalang.debugadapter.evaluation.BExpressionValue;
import org.ballerinalang.debugadapter.evaluation.EvaluationException;
import org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind;
import org.ballerinalang.debugadapter.evaluation.engine.Evaluator;
import org.ballerinalang.debugadapter.variable.BVariable;
import org.ballerinalang.debugadapter.variable.BVariableType;
import org.ballerinalang.debugadapter.variable.DebugVariableException;
import org.ballerinalang.debugadapter.variable.VariableFactory;
import org.ballerinalang.debugadapter.variable.types.BXmlComment;
import org.ballerinalang.debugadapter.variable.types.BXmlPi;
import org.ballerinalang.debugadapter.variable.types.BXmlSequence;
import org.ballerinalang.debugadapter.variable.types.BXmlText;

import static org.ballerinalang.debugadapter.variable.VariableUtils.getChildVarByName;

/**
 * Evaluator implementation for field access expressions.
 *
 * @since 2.0.0
 */
public class FieldAccessExpressionEvaluator extends Evaluator {

    private final ExpressionNode syntaxNode;
    private final Evaluator objectExpressionEvaluator;
    private String expression;
    private String fieldName;
    private static final String FIELD_NAME_ATTRIBUTES = "attributes";

    public FieldAccessExpressionEvaluator(EvaluationContext context, Evaluator expression,
                                          ExpressionNode fieldAccessExpressionNode) {
        super(context);
        this.syntaxNode = fieldAccessExpressionNode;
        this.objectExpressionEvaluator = expression;
    }

    @Override
    public BExpressionValue evaluate() throws EvaluationException {
        if (syntaxNode.kind() == SyntaxKind.FIELD_ACCESS) {
            expression = ((FieldAccessExpressionNode) syntaxNode).expression().toSourceCode().trim();
            fieldName = ((FieldAccessExpressionNode) syntaxNode).fieldName().toSourceCode().trim();
        } else if (syntaxNode.kind() == SyntaxKind.OPTIONAL_FIELD_ACCESS) {
            expression = ((OptionalFieldAccessExpressionNode) syntaxNode).expression().toSourceCode().trim();
            fieldName = ((OptionalFieldAccessExpressionNode) syntaxNode).fieldName().toSourceCode().trim();
        } else {
            throw new EvaluationException(String.format(EvaluationExceptionKind.CUSTOM_ERROR.getString(), "Field " +
                    "access is not supported on type '" + syntaxNode.kind().stringValue() + "'."));
        }

        // expression is evaluated resulting in a value v
        // if v has basic type error, the result is v
        // otherwise, if v does not have basic type mapping, the result is a new error value
        // otherwise, if v does not have a member whose key is field-name, the result is a new error value
        // otherwise, the result is the member of v whose key is field-name.
        try {
            BExpressionValue result = objectExpressionEvaluator.evaluate();
            BVariable resultVar = VariableFactory.getVariable(context, result.getJdiValue());
            if (resultVar.getBType() != BVariableType.OBJECT
                    && resultVar.getBType() != BVariableType.SERVICE
                    && resultVar.getBType() != BVariableType.RECORD
                    && resultVar.getBType() != BVariableType.JSON
                    && result.getType() != BVariableType.XML) {
                throw new EvaluationException(String.format(EvaluationExceptionKind.CUSTOM_ERROR.getString(), "Field " +
                        "access is not supported on type '" + resultVar.getBType().getString() + "'."));
            }

            Value fieldValue;
            if (result.getType() == BVariableType.XML) {
                fieldValue = getXmlAttributeValue(resultVar);
            } else {
                fieldValue = getChildVarByName(resultVar, fieldName);
            }
            return new BExpressionValue(context, fieldValue);
        } catch (EvaluationException e) {
            throw e;
        } catch (DebugVariableException e) {
            throw new EvaluationException(String.format(EvaluationExceptionKind.FIELD_NOT_FOUND.getString(),
                    fieldName, expression));
        } catch (Exception e) {
            throw new EvaluationException(String.format(EvaluationExceptionKind.INTERNAL_ERROR.getString(),
                    syntaxNode.toSourceCode().trim()));
        }
    }

    private Value getXmlAttributeValue(BVariable variable) throws EvaluationException {
        if (variable instanceof BXmlSequence) {
            throw new EvaluationException(String.format(EvaluationExceptionKind.INVALID_XML_ATTRIBUTE.getString(),
                    "xml sequence"));
        } else if (variable instanceof BXmlText) {
            throw new EvaluationException(String.format(EvaluationExceptionKind.INVALID_XML_ATTRIBUTE.getString(),
                    "xml text"));
        } else if (variable instanceof BXmlComment) {
            throw new EvaluationException(String.format(EvaluationExceptionKind.INVALID_XML_ATTRIBUTE.getString(),
                    "xml comment"));
        } else if (variable instanceof BXmlPi) {
            throw new EvaluationException(String.format(EvaluationExceptionKind.INVALID_XML_ATTRIBUTE.getString(),
                    "xml pi"));
        }

        try {
            Value attributes = getChildVarByName(variable, FIELD_NAME_ATTRIBUTES);
            BVariable attributesVar = VariableFactory.getVariable(context, attributes);
            return getChildVarByName(attributesVar, fieldName);
        } catch (Exception e) {
            throw new EvaluationException(String.format(EvaluationExceptionKind.CUSTOM_ERROR.getString(),
                    "Undefined attribute `" + fieldName + "' in: '" + expression + "'"));
        }
    }
}
