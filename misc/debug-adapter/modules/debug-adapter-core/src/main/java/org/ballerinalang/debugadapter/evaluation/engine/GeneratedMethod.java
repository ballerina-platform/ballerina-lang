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
import com.sun.jdi.DoubleType;
import com.sun.jdi.FloatType;
import com.sun.jdi.IntegerType;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.LongType;
import com.sun.jdi.Method;
import com.sun.jdi.ShortType;
import com.sun.jdi.Type;
import com.sun.jdi.Value;
import io.ballerina.runtime.api.types.BooleanType;
import io.ballerina.runtime.api.types.ByteType;
import org.ballerinalang.debugadapter.SuspendedContext;
import org.ballerinalang.debugadapter.evaluation.EvaluationException;
import org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind;
import org.ballerinalang.debugadapter.evaluation.utils.VMUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.ballerinalang.debugadapter.evaluation.engine.InvocationArgProcessor.DEFAULTABLE_PARAM_SUFFIX;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.STRAND_VAR_NAME;

/**
 * JVM generated method representation of a ballerina function.
 *
 * @since 2.0.0
 */
public abstract class GeneratedMethod extends JvmMethod {

    protected Map<String, Value> namedArgValues;

    GeneratedMethod(SuspendedContext context, Method methodRef) {
        super(context, methodRef);
        this.namedArgValues = null;
    }

    protected void setNamedArgValues(Map<String, Value> namedArgValues) {
        this.namedArgValues = namedArgValues;
    }

    @Override
    protected List<Value> getMethodArgs(JvmMethod method) throws EvaluationException {
        try {
            if (argValues == null && argEvaluators == null && namedArgValues == null) {
                throw new EvaluationException(String.format(EvaluationExceptionKind.FUNCTION_EXECUTION_ERROR.getString()
                        , methodRef.name()));
            }

            List<Type> types = method.methodRef.argumentTypes();
            // Removes injected arguments added during the jvm method gen phase.
            for (int index = types.size() - 1; index >= 0; index -= 2) {
                types.remove(index);
            }

            List<Value> argValueList = new ArrayList<>();
            if (argValues != null) {
                argValues.forEach(value -> {
                    argValueList.add(value);
                    // Assuming all the arguments are positional args.
                    argValueList.add(VMUtils.make(context, true).getJdiValue());
                });
                // Here we use the existing strand instance to execute the function invocation expression.
                Value strand = getCurrentStrand();
                argValueList.add(0, strand);
                return argValueList;
            }

            if (namedArgValues != null) {
                // Here we use the existing strand instance to execute the function invocation expression.
                Value strand = getCurrentStrand();
                namedArgValues.put(STRAND_VAR_NAME, strand);
                List<LocalVariable> args = method.methodRef.arguments();
                List<String> argNames = args.stream().map(LocalVariable::name).collect(Collectors.toList());

                for (int i = 0, argNamesSize = argNames.size(); i < argNamesSize; i++) {
                    String argName = argNames.get(i);
                    // If this is a defaultable parameter
                    if (namedArgValues.get(argName) == null &&
                            namedArgValues.get(argName + DEFAULTABLE_PARAM_SUFFIX) != null) {
                        argValueList.add(getDefaultValue(args.get(i)));
                        argValueList.add(VMUtils.make(context, false).getJdiValue());
                    } else {
                        argValueList.add(namedArgValues.get(argName));
                        if (!argName.equals(STRAND_VAR_NAME)) {
                            argValueList.add(VMUtils.make(context, true).getJdiValue());
                        }
                    }
                }
                return argValueList;
            }

            // Evaluates all function argument expressions at first.
            for (Map.Entry<String, Evaluator> argEvaluator : argEvaluators) {
                argValueList.add(argEvaluator.getValue().evaluate().getJdiValue());
                // Assuming all the arguments are positional args.
                argValueList.add(VMUtils.make(context, true).getJdiValue());
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

    private Value getDefaultValue(LocalVariable localVariable) {
        try {
            Type type = localVariable.type();
            if (type instanceof ByteType || type instanceof ShortType || type instanceof IntegerType ||
                    type instanceof LongType) {
                return VMUtils.make(context, 0).getJdiValue();
            } else if (type instanceof FloatType || type instanceof DoubleType) {
                return VMUtils.make(context, 0.0).getJdiValue();
            } else if (type instanceof BooleanType) {
                return VMUtils.make(context, false).getJdiValue();
            } else {
                return null;
            }
        } catch (ClassNotLoadedException e) {
            return null;
        }
    }
}
