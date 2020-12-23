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

package org.ballerinalang.debugadapter.evaluation;

import com.sun.jdi.Value;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import org.ballerinalang.debugadapter.SuspendedContext;
import org.ballerinalang.debugadapter.evaluation.engine.Evaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Ballerina expression evaluator.
 *
 * @since 2.0.0
 */
public class ExpressionEvaluator {

    private final SuspendedContext context;
    private final EvaluatorBuilder evaluatorBuilder;
    private final DebugExpressionCompiler expressionCompiler;
    private static final Logger LOGGER = LoggerFactory.getLogger(ExpressionEvaluator.class);

    public ExpressionEvaluator(SuspendedContext context) {
        this.context = context;
        this.evaluatorBuilder = new EvaluatorBuilder(context);
        this.expressionCompiler = context.getDebugCompiler();
    }

    /**
     * Evaluates a given ballerina expression w.r.t. the debug context.
     */
    public Value evaluate(String expression) {
        try {
            ExpressionNode compiledExprNode = expressionCompiler.validateAndCompile(expression);
            Evaluator evaluator = evaluatorBuilder.build(compiledExprNode);
            return evaluator.evaluate().getJdiValue();
        } catch (EvaluationException e) {
            return context.getAttachedVm().mirrorOf(e.getMessage());
        } catch (Exception e) {
            String message = EvaluationExceptionKind.PREFIX + "internal error";
            LOGGER.error(message, e);
            return context.getAttachedVm().mirrorOf(message);
        }
    }
}
