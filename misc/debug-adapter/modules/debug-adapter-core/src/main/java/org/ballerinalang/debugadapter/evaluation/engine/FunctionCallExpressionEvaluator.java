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

import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.ClassNotPreparedException;
import com.sun.jdi.ClassObjectReference;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.Value;
import io.ballerinalang.compiler.syntax.tree.FunctionCallExpressionNode;
import org.ballerinalang.debugadapter.SuspendedContext;
import org.ballerinalang.debugadapter.evaluation.EvaluationException;
import org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FunctionCallExpressionEvaluator extends Evaluator {

    private final FunctionCallExpressionNode syntaxNode;
    private final List<Evaluator> argEvaluators;

    public FunctionCallExpressionEvaluator(SuspendedContext context, FunctionCallExpressionNode node,
                                           List<Evaluator> argEvaluators) {
        super(context);
        this.syntaxNode = node;
        this.argEvaluators = argEvaluators;
    }

    @Override
    public Value evaluate() throws EvaluationException {
        try {
            Optional<JvmMethod> jvmMethod = findFunction(syntaxNode);
            if (!jvmMethod.isPresent()) {
                throw new EvaluationException(String.format(EvaluationExceptionKind.FUNCTION_NOT_FOUND.getString(),
                        syntaxNode.functionName().toString()));
            }
            // Evaluates function arguments.
            List<Value> argValueList = new ArrayList<>();
            for (Evaluator argEvaluator : argEvaluators) {
                argValueList.add(argEvaluator.evaluate());
            }
            ClassObjectReference classRef = jvmMethod.get().classRef.classObject();
            Method methodRef = jvmMethod.get().methodRef;
            return classRef.invokeMethod(context.getOwningThread(), methodRef, argValueList,
                    ObjectReference.INVOKE_SINGLE_THREADED);
        } catch (ClassNotLoadedException e) {
            throw new EvaluationException(String.format(EvaluationExceptionKind.FUNCTION_NOT_FOUND.getString(),
                    syntaxNode.functionName().toString()));
        } catch (Exception e) {
            throw new EvaluationException(String.format(EvaluationExceptionKind.FUNCTION_EXECUTION_ERROR.getString(),
                    syntaxNode.toString()));
        }
    }

    private Optional<JvmMethod> findFunction(FunctionCallExpressionNode functionNode) throws ClassNotLoadedException {
        List<ReferenceType> allClasses = context.getAttachedVm().allClasses();
        String orgName = context.getOrgName().isPresent() ? context.getOrgName().get() : null;
        for (ReferenceType cls : allClasses) {
            try {
                // Generated class name should start with the org name of ballerina module sources.
                if (orgName != null && !cls.name().startsWith(orgName)) {
                    continue;
                }

                List<Method> methods = cls.methodsByName(functionNode.functionName().toString());
                for (Method method : methods) {
                    // All the ballerina functions are represented as java static methods.
                    if (method.isStatic() && method.argumentTypes().size() == functionNode.arguments().size() + 1) {
                        return Optional.of(new JvmMethod(cls, method));
                    }
                }
            } catch (ClassNotPreparedException ignored) {
                // Unprepared classes should be skipped.
            }
        }
        return Optional.empty();
    }

    static class JvmMethod {
        final ReferenceType classRef;
        final Method methodRef;

        JvmMethod(ReferenceType classRef, Method methodRef) {
            this.classRef = classRef;
            this.methodRef = methodRef;
        }
    }
}
