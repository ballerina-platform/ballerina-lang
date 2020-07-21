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

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.ClassNotPreparedException;
import com.sun.jdi.ClassType;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.Value;
import io.ballerinalang.compiler.syntax.tree.FunctionCallExpressionNode;
import org.ballerinalang.debugadapter.DebugSourceType;
import org.ballerinalang.debugadapter.SuspendedContext;
import org.ballerinalang.debugadapter.evaluation.BExpressionValue;
import org.ballerinalang.debugadapter.evaluation.EvaluationException;
import org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FunctionInvocationExpressionEvaluator extends Evaluator {

    private final FunctionCallExpressionNode syntaxNode;
    private final List<Evaluator> argEvaluators;
    private static final String STRAND_VAR_NAME = "__strand";

    public FunctionInvocationExpressionEvaluator(SuspendedContext context, FunctionCallExpressionNode node,
                                                 List<Evaluator> argEvaluators) {
        super(context);
        this.syntaxNode = node;
        this.argEvaluators = argEvaluators;
    }

    @Override
    public BExpressionValue evaluate() throws EvaluationException {
        try {
            Optional<JvmMethod> jvmMethod = findFunction(syntaxNode);
            if (!jvmMethod.isPresent()) {
                throw new EvaluationException(String.format(EvaluationExceptionKind.FUNCTION_NOT_FOUND.getString(),
                        syntaxNode.functionName().toString()));
            }
            List<Value> argValueList = new ArrayList<>();
            // Todo - verify
            // Here we use the parent strand instance to execute the function invocation expression.
            Value parentStrand = getParentStrand();
            argValueList.add(parentStrand);
            // Evaluates function argument expressions before before the parent function execution.
            for (Evaluator argEvaluator : argEvaluators) {
                argValueList.add(argEvaluator.evaluate().getJdiValue());
            }
            ReferenceType classRef = jvmMethod.get().classRef;
            Method methodRef = jvmMethod.get().methodRef;

            if (!(classRef instanceof ClassType)) {
                throw new EvaluationException(String.format(EvaluationExceptionKind.FUNCTION_EXECUTION_ERROR.getString()
                        , syntaxNode.toString()));
            }
            Value result = ((ClassType) classRef).invokeMethod(context.getOwningThread(), methodRef, argValueList,
                    ObjectReference.INVOKE_SINGLE_THREADED);
            return new BExpressionValue(context, result);
        } catch (ClassNotLoadedException e) {
            throw new EvaluationException(String.format(EvaluationExceptionKind.FUNCTION_NOT_FOUND.getString(),
                    syntaxNode.functionName().toString()));
        } catch (Exception e) {
            throw new EvaluationException(String.format(EvaluationExceptionKind.FUNCTION_EXECUTION_ERROR.getString(),
                    syntaxNode.toString()));
        }
    }

    private Value getParentStrand() throws EvaluationException {
        try {
            Value strand = context.getFrame().getValue(context.getFrame().visibleVariableByName(STRAND_VAR_NAME));
            if (strand == null) {
                throw new EvaluationException(String.format(EvaluationExceptionKind.STRAND_NOT_FOUND.getString(),
                        syntaxNode.functionName().toString()));
            }
            return strand;
        } catch (AbsentInformationException e) {
            throw new EvaluationException(String.format(EvaluationExceptionKind.STRAND_NOT_FOUND.getString(),
                    syntaxNode.functionName().toString()));
        }
    }

    private Optional<JvmMethod> findFunction(FunctionCallExpressionNode functionNode) {
        List<ReferenceType> allClasses = context.getAttachedVm().allClasses();
        DebugSourceType sourceType = context.getSourceType();
        for (ReferenceType cls : allClasses) {
            try {
                // Expected class name should end with the file name of the ballerina source, only for single
                // ballerina sources. (We cannot be sure about the module context, as we can invoke any method
                // defined within the module.)
                if (sourceType == DebugSourceType.SINGLE_FILE && !cls.name().endsWith(context.getFileName())) {
                    continue;
                }
                // If the sources reside inside a ballerina module/project, generated class name should start with the
                // organization name of the ballerina module/project source.
                if (sourceType == DebugSourceType.MODULE && !cls.name().startsWith(context.getOrgName().get())) {
                    continue;
                }
                List<Method> methods = cls.methodsByName(functionNode.functionName().toString());
                for (Method method : methods) {
                    // Note - All the ballerina functions are represented as java static methods and all the generated
                    // jvm methods contain strand as its first argument.
                    if (method.isStatic() && method.argumentTypes().size() == functionNode.arguments().size() + 1) {
                        return Optional.of(new JvmMethod(cls, method));
                    }
                }
            } catch (ClassNotPreparedException ignored) {
                // Unprepared classes should be skipped.
            } catch (ClassNotLoadedException ignored) {
                // Todo - skipped or throw?
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
