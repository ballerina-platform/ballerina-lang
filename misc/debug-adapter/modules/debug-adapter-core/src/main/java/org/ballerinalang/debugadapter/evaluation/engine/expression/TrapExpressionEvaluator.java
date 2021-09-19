/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
import io.ballerina.compiler.syntax.tree.TrapExpressionNode;
import org.ballerinalang.debugadapter.EvaluationContext;
import org.ballerinalang.debugadapter.evaluation.BExpressionValue;
import org.ballerinalang.debugadapter.evaluation.EvaluationException;
import org.ballerinalang.debugadapter.evaluation.engine.Evaluator;
import org.ballerinalang.debugadapter.evaluation.engine.invokable.GeneratedStaticMethod;

import java.util.ArrayList;
import java.util.List;

import static org.ballerinalang.debugadapter.evaluation.EvaluationException.createEvaluationException;
import static org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind.INTERNAL_ERROR;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.B_GET_TRAP_RESULT_METHOD;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.B_UTILS_HELPER_CLASS;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.getGeneratedMethod;

/**
 * Trap expression evaluator implementation.
 *
 * @since 2.0.0
 */
public class TrapExpressionEvaluator extends Evaluator {

    private final TrapExpressionNode syntaxNode;
    private final Evaluator exprEvaluator;

    public TrapExpressionEvaluator(EvaluationContext context, TrapExpressionNode trapExpressionNode,
                                   Evaluator exprEvaluator) {
        super(context);
        this.syntaxNode = trapExpressionNode;
        this.exprEvaluator = exprEvaluator;
    }

    @Override
    public BExpressionValue evaluate() throws EvaluationException {
        try {
            // The trap expression stops a panic and gives access to the error value associated with the panic.
            // Evaluate expression resulting in value v
            // - If evaluation completes abruptly with panic with associated value e, then result of trap-exp is e
            // - Otherwise result of trap-expr is v
            BExpressionValue result = exprEvaluator.evaluate();
            GeneratedStaticMethod trapResultMethod = getGeneratedMethod(context, B_UTILS_HELPER_CLASS,
                    B_GET_TRAP_RESULT_METHOD);
            List<Value> trapResultArgs = new ArrayList<>();
            trapResultArgs.add(result.getJdiValue());
            trapResultMethod.setArgValues(trapResultArgs);
            return new BExpressionValue(context, trapResultMethod.invokeSafely());
        } catch (EvaluationException e) {
            throw e;
        } catch (Exception e) {
            throw createEvaluationException(INTERNAL_ERROR, syntaxNode.toSourceCode().trim());
        }
    }
}
