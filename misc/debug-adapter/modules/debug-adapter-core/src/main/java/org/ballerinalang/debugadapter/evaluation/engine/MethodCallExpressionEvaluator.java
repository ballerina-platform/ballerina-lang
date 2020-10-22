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

import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.Value;
import io.ballerina.compiler.syntax.tree.MethodCallExpressionNode;
import org.ballerinalang.debugadapter.SuspendedContext;
import org.ballerinalang.debugadapter.evaluation.BExpressionValue;
import org.ballerinalang.debugadapter.evaluation.EvaluationException;
import org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind;
import org.ballerinalang.debugadapter.variable.BVariable;
import org.ballerinalang.debugadapter.variable.BVariableType;
import org.ballerinalang.debugadapter.variable.VariableFactory;

import java.util.List;

/**
 * Evaluator implementation for method call invocation expressions.
 *
 * @since 2.0.0
 */
public class MethodCallExpressionEvaluator extends Evaluator {

    private final MethodCallExpressionNode syntaxNode;
    private final Evaluator objectExpressionEvaluator;
    private final List<Evaluator> argEvaluators;

    public MethodCallExpressionEvaluator(SuspendedContext context, MethodCallExpressionNode methodCallExpressionNode,
                                         Evaluator expression, List<Evaluator> argEvaluators) {
        super(context);
        this.syntaxNode = methodCallExpressionNode;
        this.objectExpressionEvaluator = expression;
        this.argEvaluators = argEvaluators;
    }

    @Override
    public BExpressionValue evaluate() throws EvaluationException {
        try {
            BExpressionValue result = objectExpressionEvaluator.evaluate();
            BVariable resultVar = VariableFactory.getVariable(context, result.getJdiValue());
            if (resultVar.getBType() != BVariableType.OBJECT && resultVar.getBType() != BVariableType.INT) {
                throw new EvaluationException(String.format(EvaluationExceptionKind.CUSTOM_ERROR.getString(), "Method" +
                        " calls are not supported on type '" + resultVar.getBType() + "'."));
            }
            String methodName = syntaxNode.methodName().toString().trim();
            JvmMethod method = getObjectMethodByName(resultVar.getJvmValue(), methodName);
            Value invocationResult = method.invoke();
            return new BExpressionValue(context, invocationResult);
        } catch (EvaluationException e) {
            throw e;
        } catch (Exception e) {
            throw new EvaluationException(String.format(EvaluationExceptionKind.INTERNAL_ERROR.getString(),
                    syntaxNode.toSourceCode().trim()));
        }
    }

    private JvmMethod getObjectMethodByName(Value objectValueRef, String methodName) throws EvaluationException {
        ReferenceType objectRef = ((ObjectReference) objectValueRef).referenceType();
        List<Method> methods = objectRef.methodsByName(methodName);
        if (methods == null || methods.size() != 1) {
            throw new EvaluationException(String.format(EvaluationExceptionKind.OBJECT_METHOD_NOT_FOUND.getString(),
                    syntaxNode.methodName().toString().trim()));
        }
        return new JvmInstanceMethod(context, objectValueRef, methods.get(0), argEvaluators, null);
    }
}
