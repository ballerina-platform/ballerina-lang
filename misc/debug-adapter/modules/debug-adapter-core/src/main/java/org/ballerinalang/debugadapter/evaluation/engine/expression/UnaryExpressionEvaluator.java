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
import io.ballerina.compiler.syntax.tree.UnaryExpressionNode;
import org.ballerinalang.debugadapter.EvaluationContext;
import org.ballerinalang.debugadapter.evaluation.BExpressionValue;
import org.ballerinalang.debugadapter.evaluation.EvaluationException;
import org.ballerinalang.debugadapter.evaluation.engine.Evaluator;
import org.ballerinalang.debugadapter.evaluation.engine.invokable.GeneratedStaticMethod;

import java.util.Collections;

import static org.ballerinalang.debugadapter.evaluation.EvaluationException.createEvaluationException;
import static org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind.INTERNAL_ERROR;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.B_UNARY_EXPR_HELPER_CLASS;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.B_UNARY_INVERT_METHOD;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.B_UNARY_MINUS_METHOD;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.B_UNARY_NOT_METHOD;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.B_UNARY_PLUS_METHOD;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.getGeneratedMethod;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.getValueAsObject;

/**
 * Unary expression evaluator implementation.
 *
 * @since 2.0.0
 */
public class UnaryExpressionEvaluator extends Evaluator {

    private final UnaryExpressionNode syntaxNode;
    private final Evaluator exprEvaluator;

    public UnaryExpressionEvaluator(EvaluationContext context, UnaryExpressionNode unaryExpressionNode,
                                    Evaluator subExprEvaluator) {
        super(context);
        this.syntaxNode = unaryExpressionNode;
        this.exprEvaluator = subExprEvaluator;
    }

    @Override
    public BExpressionValue evaluate() throws EvaluationException {
        try {
            BExpressionValue result = exprEvaluator.evaluate();
            Value valueAsObject = getValueAsObject(context, result.getJdiValue());

            GeneratedStaticMethod genMethod;
            switch (syntaxNode.unaryOperator().kind()) {
                case PLUS_TOKEN:
                    genMethod = getGeneratedMethod(context, B_UNARY_EXPR_HELPER_CLASS, B_UNARY_PLUS_METHOD);
                    break;
                case MINUS_TOKEN:
                    genMethod = getGeneratedMethod(context, B_UNARY_EXPR_HELPER_CLASS, B_UNARY_MINUS_METHOD);
                    break;
                case NEGATION_TOKEN:
                    genMethod = getGeneratedMethod(context, B_UNARY_EXPR_HELPER_CLASS, B_UNARY_INVERT_METHOD);
                    break;
                case EXCLAMATION_MARK_TOKEN:
                    genMethod = getGeneratedMethod(context, B_UNARY_EXPR_HELPER_CLASS, B_UNARY_NOT_METHOD);
                    break;
                default:
                    throw createEvaluationException(INTERNAL_ERROR, syntaxNode.toSourceCode().trim());
            }
            genMethod.setArgValues(Collections.singletonList(valueAsObject));
            return new BExpressionValue(context, genMethod.invokeSafely());
        } catch (EvaluationException e) {
            throw e;
        } catch (Exception e) {
            throw createEvaluationException(INTERNAL_ERROR, syntaxNode.toSourceCode().trim());
        }
    }
}
