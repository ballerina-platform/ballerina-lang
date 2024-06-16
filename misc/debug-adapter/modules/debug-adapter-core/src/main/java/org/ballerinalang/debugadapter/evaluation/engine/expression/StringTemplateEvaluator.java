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

import com.sun.jdi.StringReference;
import com.sun.jdi.Value;
import io.ballerina.compiler.syntax.tree.TemplateExpressionNode;
import org.ballerinalang.debugadapter.EvaluationContext;
import org.ballerinalang.debugadapter.evaluation.BExpressionValue;
import org.ballerinalang.debugadapter.evaluation.EvaluationException;
import org.ballerinalang.debugadapter.evaluation.engine.Evaluator;
import org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils;

import java.util.ArrayList;
import java.util.List;

import static org.ballerinalang.debugadapter.evaluation.EvaluationException.createEvaluationException;
import static org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind.INTERNAL_ERROR;
import static org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind.TYPE_MISMATCH;

/**
 * String template expression evaluator implementation.
 *
 * @since 2.0.0
 */
public class StringTemplateEvaluator extends Evaluator {

    private final TemplateExpressionNode syntaxNode;
    private final List<Evaluator> templateMemberEvaluators;

    public StringTemplateEvaluator(EvaluationContext context, TemplateExpressionNode templateExpressionNode,
                                   List<Evaluator> templateMemberEvaluators) {
        super(context);
        this.syntaxNode = templateExpressionNode;
        this.templateMemberEvaluators = templateMemberEvaluators;
    }

    @Override
    public BExpressionValue evaluate() throws EvaluationException {
        // A string-template-expr interpolates the results of evaluating expressions into a literal string. The static
        // type of the expression in each interpolation must be a simple type and must not be nil.
        // A string-template-expr is evaluated by evaluating the expression in each interpolation in the order in
        // which they occur, and converting the result of the each evaluation to a string using the ToString abstract
        // operation with the direct style.
        try {
            List<Value> templateMemberValues = new ArrayList<>();
            for (int i = 0; i < templateMemberEvaluators.size(); i++) {
                Evaluator evaluator = templateMemberEvaluators.get(i);
                BExpressionValue result = evaluator.evaluate();
                switch (result.getType()) {
                    case INT:
                    case FLOAT:
                    case DECIMAL:
                    case STRING:
                    case BOOLEAN:
                        templateMemberValues.add(EvaluationUtils.getStringValue(context, result.getJdiValue()));
                        break;
                    default:
                        // Interpolation expression results can only be (int|float|decimal|string|boolean).
                        throw createEvaluationException(TYPE_MISMATCH, "(int|float|decimal|string|boolean)",
                                result.getType().getString(), syntaxNode.content().get(i).toSourceCode());
                }
            }
            Value result = EvaluationUtils.concatBStrings(context, templateMemberValues.toArray(new Value[0]));
            // Converts the result into a BString, if the result is a java string reference.
            if (result instanceof StringReference stringReference) {
                result = EvaluationUtils.getAsBString(context, stringReference);
            }
            return new BExpressionValue(context, result);
        } catch (EvaluationException e) {
            throw e;
        } catch (Exception e) {
            throw createEvaluationException(INTERNAL_ERROR, syntaxNode.toSourceCode().trim());
        }
    }
}
