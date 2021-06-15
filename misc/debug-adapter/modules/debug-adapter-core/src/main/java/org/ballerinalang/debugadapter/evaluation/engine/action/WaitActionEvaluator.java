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

package org.ballerinalang.debugadapter.evaluation.engine.action;

import com.sun.jdi.Value;
import io.ballerina.compiler.syntax.tree.WaitActionNode;
import org.ballerinalang.debugadapter.SuspendedContext;
import org.ballerinalang.debugadapter.evaluation.BExpressionValue;
import org.ballerinalang.debugadapter.evaluation.EvaluationException;
import org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind;
import org.ballerinalang.debugadapter.evaluation.engine.Evaluator;
import org.ballerinalang.debugadapter.evaluation.engine.invokable.RuntimeInstanceMethod;
import org.ballerinalang.debugadapter.evaluation.engine.invokable.RuntimeStaticMethod;
import org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils;
import org.ballerinalang.debugadapter.variable.BVariableType;

import java.util.ArrayList;
import java.util.List;

import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.B_FUTURE_ARRAY_CLASS;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.B_VALUE_UTILS_CLASS;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.CREATE_FUTURE_ARRAY_METHOD;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.HANDLE_WAIT_ANY_METHOD;

/**
 * Evaluator implementation for remote method call invocation actions.
 *
 * @since 2.0.0
 */
public class WaitActionEvaluator extends Evaluator {

    private final WaitActionNode syntaxNode;
    private final Evaluator futureExpressionEvaluator;

    public WaitActionEvaluator(SuspendedContext context, WaitActionNode waitActionNode,
                               Evaluator futureExpressionEvaluator) {
        super(context);
        this.syntaxNode = waitActionNode;
        this.futureExpressionEvaluator = futureExpressionEvaluator;
    }

    @Override
    public BExpressionValue evaluate() throws EvaluationException {
        try {
            BExpressionValue result = futureExpressionEvaluator.evaluate();
            // If the expression result is an object, try invoking as an object method invocation.
            if (result.getType() != BVariableType.FUTURE) {
                throw new EvaluationException(String.format(EvaluationExceptionKind.TYPE_MISMATCH.getString(),
                        BVariableType.FUTURE.getString(), result.getType().getString(),
                        syntaxNode.waitFutureExpr().toSourceCode()));
            }

            // Hack to classload the "FutureValue[]" class. Otherwise JVM method for invoking the wait action will fail
            // with "com.sun.jdi.ClassNotLoadedException".
            classLoadFutureArray();

            Value activeStrand = EvaluationUtils.getActiveStrand(context);
            List<String> argTypeNames = new ArrayList<>();
            argTypeNames.add(B_FUTURE_ARRAY_CLASS);
            RuntimeInstanceMethod handleWaitAnyDebugger = EvaluationUtils.getRuntimeMethod(context, activeStrand,
                    HANDLE_WAIT_ANY_METHOD, argTypeNames);

            List<Value> values = new ArrayList<>();
            values.add(result.getJdiValue());
            handleWaitAnyDebugger.setArgValues(values);
            return new BExpressionValue(context, handleWaitAnyDebugger.invokeSafely());
        } catch (EvaluationException e) {
            throw e;
        } catch (Exception e) {
            throw new EvaluationException(String.format(EvaluationExceptionKind.INTERNAL_ERROR.getString(),
                    syntaxNode.toSourceCode().trim()));
        }
    }

    private void classLoadFutureArray() throws EvaluationException {
        RuntimeStaticMethod futureArrayLoader = EvaluationUtils.getRuntimeMethod(context, B_VALUE_UTILS_CLASS,
                CREATE_FUTURE_ARRAY_METHOD, new ArrayList<>());
        futureArrayLoader.setArgValues(new ArrayList<>());
        futureArrayLoader.invokeSafely();
    }
}
