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

import io.ballerina.compiler.syntax.tree.ConditionalExpressionNode;
import org.ballerinalang.debugadapter.SuspendedContext;
import org.ballerinalang.debugadapter.evaluation.BExpressionValue;
import org.ballerinalang.debugadapter.evaluation.EvaluationException;
import org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind;
import org.ballerinalang.debugadapter.variable.BVariableType;

/**
 * Evaluator implementation for conditional expressions.
 *
 * @since 2.0.0
 */
public class ConditionalExpressionEvaluator extends Evaluator {

    private final ConditionalExpressionNode syntaxNode;
    private final Evaluator lhsEvaluator;
    private final Evaluator middleEvaluator;
    private final Evaluator endEvaluator;

    public ConditionalExpressionEvaluator(SuspendedContext context, ConditionalExpressionNode conditionalExpressionNode,
                                          Evaluator lhsExprEvaluator, Evaluator middleExprEvaluator,
                                          Evaluator endExprEvaluator) {
        super(context);
        this.syntaxNode = conditionalExpressionNode;
        this.lhsEvaluator = lhsExprEvaluator;
        this.middleEvaluator = middleExprEvaluator;
        this.endEvaluator = endExprEvaluator;
    }

    @Override
    public BExpressionValue evaluate() throws EvaluationException {
        try {
            BExpressionValue lhsResult = lhsEvaluator.evaluate();
            if (lhsResult.getType() != BVariableType.BOOLEAN) {
                throw new EvaluationException(String.format(EvaluationExceptionKind.TYPE_MISMATCH.getString(),
                        BVariableType.BOOLEAN.getString(), lhsResult.getType().getString(),
                        syntaxNode.lhsExpression().toSourceCode().trim()));
            }
            return Boolean.parseBoolean(lhsResult.getStringValue()) ? middleEvaluator.evaluate() :
                    endEvaluator.evaluate();
        } catch (EvaluationException e) {
            throw e;
        } catch (Exception e) {
            throw new EvaluationException(String.format(EvaluationExceptionKind.INTERNAL_ERROR.getString(),
                    syntaxNode.toSourceCode().trim()));
        }
    }
}
