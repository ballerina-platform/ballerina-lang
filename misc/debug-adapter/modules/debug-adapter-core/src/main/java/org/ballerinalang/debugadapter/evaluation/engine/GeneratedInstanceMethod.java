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
import com.sun.jdi.LocalVariable;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.Type;
import com.sun.jdi.Value;
import org.ballerinalang.debugadapter.SuspendedContext;
import org.ballerinalang.debugadapter.evaluation.EvaluationException;
import org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind;
import org.ballerinalang.debugadapter.evaluation.EvaluationUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.ballerinalang.debugadapter.evaluation.EvaluationUtils.STRAND_VAR_NAME;

/**
 * JVM generated instance method representation of a ballerina function.
 *
 * @since 2.0.0
 */
public class GeneratedInstanceMethod extends JvmMethod {

    private final Value objectValueRef;
    protected Map<String, Value> namedArgValues;

    GeneratedInstanceMethod(SuspendedContext context, Value objectRef, Method methodRef) {
        super(context, methodRef);
        this.objectValueRef = objectRef;
        this.namedArgValues = null;
    }

    GeneratedInstanceMethod(SuspendedContext context, Value objectRef, Method methodRef,
                            List<Map.Entry<String, Evaluator>> argEvaluators, List<Value> argsList) {
        super(context, methodRef, argEvaluators, argsList);
        this.objectValueRef = objectRef;
    }

    public void setNamedArgValues(Map<String, Value> namedArgValues) {
        this.namedArgValues = namedArgValues;
    }

    @Override
    public Value invoke() throws EvaluationException {
        try {
            if (!(objectValueRef instanceof ObjectReference)) {
                throw new EvaluationException(String.format(EvaluationExceptionKind.FUNCTION_EXECUTION_ERROR
                        .getString(), methodRef.name()));
            }
            List<Value> argValueList = getMethodArgs(this);
            return ((ObjectReference) objectValueRef).invokeMethod(context.getOwningThread().getThreadReference(),
                    methodRef, argValueList, ObjectReference.INVOKE_SINGLE_THREADED);
        } catch (ClassNotLoadedException e) {
            throw new EvaluationException(String.format(EvaluationExceptionKind.OBJECT_METHOD_NOT_FOUND.getString(),
                    methodRef.name()));
        } catch (EvaluationException e) {
            throw e;
        } catch (Exception e) {
            throw new EvaluationException(String.format(EvaluationExceptionKind.FUNCTION_EXECUTION_ERROR.getString(),
                    methodRef.name()));
        }
    }

    @Override
    protected List<Value> getMethodArgs(JvmMethod method) throws EvaluationException {
        try {
            if (argValues == null && argEvaluators == null && namedArgValues == null) {
                throw new EvaluationException(String.format(EvaluationExceptionKind.FUNCTION_EXECUTION_ERROR
                        .getString(), methodRef.name()));
            }

            List<Value> argValueList = new ArrayList<>();
            if (argValues != null && !argValues.isEmpty()) {
                argValues.forEach(value -> {
                    argValueList.add(value);
                    // Assuming all the arguments are positional args.
                    argValueList.add(EvaluationUtils.make(context, true).getJdiValue());
                });
                // Here we use the existing strand instance to execute the function invocation expression.
                Value strand = getCurrentStrand();
                argValueList.add(0, strand);
                return argValueList;
            }

            if (namedArgValues != null && !namedArgValues.isEmpty()) {
                // Here we use the existing strand instance to execute the function invocation expression.
                Value strand = getCurrentStrand();
                namedArgValues.put(STRAND_VAR_NAME, strand);
                List<LocalVariable> args = method.methodRef.arguments();
                List<String> argNames = args.stream().map(LocalVariable::name).collect(Collectors.toList());
                argNames.forEach(argName -> {
                    argValueList.add(namedArgValues.get(argName));
                    if (!argName.equals(STRAND_VAR_NAME)) {
                        argValueList.add(EvaluationUtils.make(context, true).getJdiValue());
                    }
                });
                return argValueList;
            }

            // Evaluates all function argument expressions at first.
            for (Map.Entry<String, Evaluator> argEvaluator : argEvaluators) {
                argValueList.add(argEvaluator.getValue().evaluate().getJdiValue());
                // Assuming all the arguments are positional args.
                argValueList.add(EvaluationUtils.make(context, true).getJdiValue());
            }
            List<Type> types = method.methodRef.argumentTypes();
            // Removes injected arguments added during the jvm method gen phase.
            for (int index = types.size() - 1; index >= 0; index -= 2) {
                types.remove(index);
            }

            // Todo - IMPORTANT: Add remaining steps to validate and match named, defaultable and rest args
            // Todo - verify
            // Here we use the existing strand instance to execute the function invocation expression.
            Value strand = getCurrentStrand();
            argValueList.add(0, strand);
            return argValueList;
        } catch (ClassNotLoadedException | AbsentInformationException e) {
            throw new EvaluationException(String.format(EvaluationExceptionKind.FUNCTION_EXECUTION_ERROR.getString(),
                    methodRef.name()));
        }
    }
}
